package com.ttb.crm.service.migrationdata.service;

import com.ttb.crm.service.migrationdata.bean.response.CaseCaptureEventDTO;
import com.ttb.crm.service.migrationdata.bean.response.RetrieveCaseInfoResponse;
import com.ttb.crm.service.migrationdata.bean.response.SlaByHopDto;
import com.ttb.crm.service.migrationdata.helper.Constant;
import com.ttb.crm.service.migrationdata.model.caseManagement.CaseTransactionModel;
import com.ttb.crm.service.migrationdata.model.userManagement.EmployeeUserModel;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class CaseMovementDataUtils {

    final private UserService userService;

    private static List<SlaByHopDto> getSlaByHopDtoList(CaseTransactionModel nextHopData) {
        return Optional.ofNullable(nextHopData.getSlaHop())
                .orElse(Collections.emptyList())
                .stream()
                .filter(c -> c.getStatusCode() != null && c.getStatusCode().equals(Constant.ACTIVE_STATUS_CODE))
                .map(SlaByHopDto::new)
                .toList();
    }

    public CaseCaptureEventDTO captureAfterChange(CaseTransactionModel newCaseTransaction, CaseCaptureEventDTO caseCaptureEventDTO, String employeeId, String eventAction) {

        RetrieveCaseInfoResponse afterChange = prePairCaseInfoResponse(newCaseTransaction);

        BeanUtils.copyProperties(newCaseTransaction, afterChange);
        caseCaptureEventDTO.setAfterChange(afterChange);

        List<SlaByHopDto> slaByHopDtoList = getSlaByHopDtoList(newCaseTransaction);
        afterChange.setSlaHop(slaByHopDtoList);

        caseCaptureEventDTO.setEventAction(eventAction);
        caseCaptureEventDTO.setEmployeeId(employeeId);
        caseCaptureEventDTO.setBeforeChange(caseCaptureEventDTO.getBeforeChange());

        return caseCaptureEventDTO;
    }

    public RetrieveCaseInfoResponse prePairCaseInfoResponse(CaseTransactionModel caseTransaction) {
        return Optional.of(caseTransaction)
                .map(caseT -> setEmployeeIdCaseTransaction(new RetrieveCaseInfoResponse(caseT), caseT.getCreatedById(), caseT.getModifiedById(), caseT.getOwnerId()))
                .map(this::setEmployeeIdInHop)
                .orElseThrow();
    }

    public RetrieveCaseInfoResponse setEmployeeIdInHop(RetrieveCaseInfoResponse retrieveCaseInfoResponse) {
        Optional.ofNullable(retrieveCaseInfoResponse.getSlaHop())
                .ifPresent(slaByHop ->
                        slaByHop.forEach(slaByHopDto -> Optional.ofNullable(slaByHopDto.getOwnerId())
                                .map(userService::getUserById)
                                .ifPresent(userInternalResponse -> {
                                    slaByHopDto.setEmployeeId(userInternalResponse.getEmployeeId());
                                    slaByHopDto.setOwnerName(!userInternalResponse.getFullNameTH().isBlank() ? userInternalResponse.getFullNameTH() : userInternalResponse.getFirstNameTh());
                                }))
                );
        return retrieveCaseInfoResponse;
    }

    public RetrieveCaseInfoResponse setEmployeeIdCaseTransaction(
            RetrieveCaseInfoResponse retrieveCaseInfoResponse, UUID createById, UUID modifiedById, UUID ownerId) {

        Map<UUID, EmployeeUserModel> userMap = new HashMap<>();

        Stream.of(createById, modifiedById, ownerId)
                .filter(Objects::nonNull)
                .distinct()
                .forEach(userId -> {
                    EmployeeUserModel userInternalResponse = userService.getUserById(userId);
                    if (userInternalResponse != null) {
                        userMap.put(userId, userInternalResponse);
                    }
                });

        Optional.ofNullable(createById)
                .map(userMap::get)
                .ifPresent(userInternalResponse -> retrieveCaseInfoResponse.setCreatedByEmployeeId(userInternalResponse.getEmployeeId()));

        Optional.ofNullable(modifiedById)
                .map(userMap::get)
                .ifPresent(userInternalResponse -> retrieveCaseInfoResponse.setModifiedByEmployeeId(userInternalResponse.getEmployeeId()));

        Optional.ofNullable(ownerId)
                .map(userMap::get)
                .ifPresent(userInternalResponse -> {
                    retrieveCaseInfoResponse.setEmployeeId(userInternalResponse.getEmployeeId());
                    retrieveCaseInfoResponse.setOwnerEmployeeId(userInternalResponse.getEmployeeId());
                });
        return retrieveCaseInfoResponse;
    }
}
