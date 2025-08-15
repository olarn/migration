package com.ttb.crm.service.migrationdata.service;

import com.ttb.crm.lib.crmssp_common_utils_lib.exception.NotFoundException;
import com.ttb.crm.lib.crmssp_common_utils_lib.exception.ValidationException;
import com.ttb.crm.service.migrationdata.bean.CreateCaseDTO;
import com.ttb.crm.service.migrationdata.bean.request.client.RetrieveMasterDataByMasterGroupRequest;
import com.ttb.crm.service.migrationdata.bean.response.client.RetrieveMasterDataResponse;
import com.ttb.crm.service.migrationdata.config.redis.CachingGroup;
import com.ttb.crm.service.migrationdata.enums.ServiceTypeMatrixTypeEnum;
import com.ttb.crm.service.migrationdata.helper.Constant;
import com.ttb.crm.service.migrationdata.model.masterManagement.ServiceTypeMatrixModel;
import com.ttb.crm.service.migrationdata.model.masterManagement.ServiceTypeMatrixSla;
import com.ttb.crm.service.migrationdata.model.userManagement.TeamReadonlyModel;
import com.ttb.crm.service.migrationdata.repository.masterManagement.MasterDataRepository;
import com.ttb.crm.service.migrationdata.repository.masterManagement.MasterGroupRepository;
import com.ttb.crm.service.migrationdata.repository.masterManagement.ServiceTypeMatrixRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@CachingGroup("master-data")
public class MasterDataService {
    private final ServiceTypeMatrixRepository serviceTypeMatrixRepository;
    private final MasterDataRepository masterDataRepository;
    private final MasterGroupRepository masterGroupRepository;

    public ServiceTypeMatrixModel retrieveServiceTypeMatrix(CreateCaseDTO dto) {
        return Optional.ofNullable(dto)
                .filter(d -> isCompleted(d) && StringUtils.isBlank(d.getServiceTypeMatrixCode()))
                .map(this::prepareStmFromStg)
                .or(() -> Optional.ofNullable(dto.getServiceTypeMatrixCode())
                        .map(this::retrieveServiceTypeMatrix))
                .orElseThrow(() -> new NotFoundException("Service type matrix code is missing in DTO"));
    }

    @Cacheable(value = "serviceTypeMatrixByCode", key = "#serviceTypeMatrixCode")
    public ServiceTypeMatrixModel retrieveServiceTypeMatrix(String serviceTypeMatrixCode) {
        return Optional.ofNullable(serviceTypeMatrixRepository.findByServiceTypeMatrixCode(serviceTypeMatrixCode)) // use in active too
                .map(this::validateSlaFcr)
                .orElseThrow(() -> new NotFoundException("Service type matrix not found with service_type_matrix_code: " + serviceTypeMatrixCode));
    }

    @CacheEvict(value = "serviceTypeMatrixByCode", key = "#serviceTypeMatrixCode")
    public void updateHopWithCreatorTeam(TeamReadonlyModel createTeam, ServiceTypeMatrixSla hop, String serviceTypeMatrixCode) {
        hop.setResponsibleBuId(createTeam.getTeamId());
        hop.setResponsibleBu(createTeam.getNameTh());
    }

    private ServiceTypeMatrixModel validateSlaFcr(ServiceTypeMatrixModel response) {
        return Optional.of(response)
                .map(r -> Boolean.TRUE.equals(r.getFcr()) ? validateSlaFcrIsTrue(r) : validateSlaFcrIsFalse(r))
                .orElseThrow(() -> new ValidationException("ServiceTypeMatrix fcr is wrong"));
    }

    private ServiceTypeMatrixModel validateSlaFcrIsTrue(ServiceTypeMatrixModel res) {
        return Optional.of(res)
                .filter(r -> (Optional.ofNullable(r.getServiceTypeMatrixSlas()).map(List::isEmpty).orElse(true))) // fcr not have sla hop
                .orElseThrow(() -> new ValidationException("ServiceTypeMatrix SLA must be empty if fcr"));
    }

    private ServiceTypeMatrixModel validateSlaFcrIsFalse(ServiceTypeMatrixModel res) {
        return Optional.of(res)
                .filter(r -> (!r.getServiceTypeMatrixSlas().isEmpty()) && res.getServiceTypeMatrixSlas().size() >= 2) // sla hop should more or equals 2 than
                .orElseThrow(() -> new NotFoundException("Missing SLA hop"));
    }

    @Cacheable(value = "masterDataByGroup", key = "#request.masterGroupCode")
    public List<RetrieveMasterDataResponse> retrieveByMasterGroupCode(RetrieveMasterDataByMasterGroupRequest request) {
        return masterGroupRepository.findByCode(request.getMasterGroupCode())
                .map(masterGroupModels -> masterGroupModels.getMasterData().stream()
                        .map(RetrieveMasterDataResponse::new)
                        .toList()
                )
                .orElseThrow(() -> new NotFoundException("Master Group not found"));
    }

    @Cacheable(value = "masterDataByCodeAndGroup", key = "#masterGroupCode + '_' + #masterDataCode")
    public RetrieveMasterDataResponse retrieveMasterDataByCodeAndGroupCode(String masterGroupCode, String masterDataCode) {
        return masterDataRepository.findByCodeAndStatusCodeAndMasterGroup_CodeAndMasterGroup_StatusCode(masterDataCode, 0, masterGroupCode, 0)
                .map(RetrieveMasterDataResponse::new)
                .orElse(null);
    }

    public ServiceTypeMatrixModel prepareStmFromStg(CreateCaseDTO request) {
        ServiceTypeMatrixModel serviceTypeMatrixInternalResponse = new ServiceTypeMatrixModel(request, ServiceTypeMatrixTypeEnum.DYNAMIC);
        if (request.getClosedByTeam().trim().isEmpty()) {
            serviceTypeMatrixInternalResponse.setAutoCloseCaseAfterResolved(true);
        } else {
            serviceTypeMatrixInternalResponse.setAutoCloseCaseAfterResolved(false);
        }

        if (Boolean.TRUE.equals(request.getFcr())) {
            serviceTypeMatrixInternalResponse.setFcr(true);
        } else {
            serviceTypeMatrixInternalResponse.setFcr(false);
        }

        serviceTypeMatrixInternalResponse.setServiceTypeMatrixCode(request.getServiceTypeMatrixCodeOld());
        serviceTypeMatrixInternalResponse.setServiceTypeMatrixType(ServiceTypeMatrixTypeEnum.DYNAMIC);
        serviceTypeMatrixInternalResponse.setIssueNameTtbTouchEn(request.getIssueNameTtbTouchEn());
        serviceTypeMatrixInternalResponse.setIssueNameTtbTouchTh(request.getIssueNameTtbTouchTh());
        serviceTypeMatrixInternalResponse.setIssueTh(request.getIssueTh());
        serviceTypeMatrixInternalResponse.setProductServiceCode(request.getProductServiceCode());
        serviceTypeMatrixInternalResponse.setProductServiceValueTh(request.getProductServiceValueTh());
        serviceTypeMatrixInternalResponse.setPtaSegmentCode(request.getPtaSegmentCode());
        serviceTypeMatrixInternalResponse.setPtaSegmentValue(request.getPtaSegmentValue());
        serviceTypeMatrixInternalResponse.setSeverityCode(request.getSeverityCode());
        serviceTypeMatrixInternalResponse.setSeverityValue(request.getSeverityValue());
        serviceTypeMatrixInternalResponse.setServiceTemplateCode(request.getServiceTemplateCode());
        serviceTypeMatrixInternalResponse.setServiceTemplateValue(request.getServiceTemplateValue());
        serviceTypeMatrixInternalResponse.setServiceTypeMatrixCode(request.getServiceTypeMatrixCode());
        serviceTypeMatrixInternalResponse.setSla(request.getSla());
        serviceTypeMatrixInternalResponse.setSmsCodeNew(request.getSmsCodeNew());
        serviceTypeMatrixInternalResponse.setSmsCodeResolution1(request.getSmsCodeResolution1());
        serviceTypeMatrixInternalResponse.setSmsCodeResolution2(request.getSmsCodeResolution2());
        serviceTypeMatrixInternalResponse.setSmsCodeResolved(request.getSmsCodeResolved());
        serviceTypeMatrixInternalResponse.setVisibleOnTouch(request.getVisibleOnTouch());
        serviceTypeMatrixInternalResponse.setAutoCloseCaseAfterResolved(Boolean.FALSE);
        return serviceTypeMatrixInternalResponse;
    }

    private boolean isCompleted(CreateCaseDTO dto) {
        return Constant.CASE_STATUS_COMPLETED.equals(dto.getCaseStatusCode());
    }
}
