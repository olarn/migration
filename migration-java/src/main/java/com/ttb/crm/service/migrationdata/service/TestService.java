//package com.ttb.crm.service.migrationdata.service;
//
//import com.ttb.crm.lib.crmssp_common_utils_lib.bean.BaseResponse;
//import com.ttb.crm.lib.crmssp_common_utils_lib.helper.BaseResponseUtil;
//import com.ttb.crm.service.migrationdata.bean.CreateCaseDTO;
//import com.ttb.crm.service.migrationdata.bean.InProgressMigrationResult;
//import com.ttb.crm.service.migrationdata.bean.response.CountCaseOnGoingCanMigrateResponse;
//import com.ttb.crm.service.migrationdata.enums.CaseStatus;
//import com.ttb.crm.service.migrationdata.enums.ServiceTypeMatrixTypeEnum;
//import com.ttb.crm.service.migrationdata.helper.Constant;
//import com.ttb.crm.service.migrationdata.model.batch.TempStgCaseInProgressLogModel;
//import com.ttb.crm.service.migrationdata.model.batch.TempStgSlaPerOwnerLogModel;
//import com.ttb.crm.service.migrationdata.model.masterManagement.ServiceTypeMatrixModel;
//import com.ttb.crm.service.migrationdata.model.secondary.StgCaseInProgressModel;
//import com.ttb.crm.service.migrationdata.model.secondary.StgSlaPerOwnerModel;
//import com.ttb.crm.service.migrationdata.repository.masterManagement.ServiceTypeMatrixRepository;
//import com.ttb.crm.service.migrationdata.repository.secondary.StgCaseInProgressRepository;
//import com.ttb.crm.service.migrationdata.repository.secondary.StgSlaPerOwnerRepository;
//import com.ttb.crm.service.migrationdata.service.preparecaesdataservice.CaseTransactionMapper;
//import com.ttb.crm.service.migrationdata.service.specification.StgCaseInProgressSpecification;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.BeanUtils;
//import org.springframework.stereotype.Service;
//
//import java.util.*;
//import java.util.function.Function;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class TestService {
//    private final CaseMigrationService caseMigrationService;
//    private final CaseTransactionMapper caseTransactionMapper;
//    private final StgCaseInProgressRepository stgCaseInProgressRepository;
//    private final StgSlaPerOwnerRepository stgSlaPerOwnerRepository;
//    private final ServiceTypeMatrixRepository serviceTypeMatrixRepository;
//
//    public BaseResponse<CountCaseOnGoingCanMigrateResponse> countCaseOnGoingCanMigrate() {
//        // 1. Load cases
//        List<StgCaseInProgressModel> cases = stgCaseInProgressRepository.findAll(
//                StgCaseInProgressSpecification.combineSpecifications(
//                        StgCaseInProgressSpecification.filterStatusCode(List.of(CaseStatus.NEW.toString(), CaseStatus.IN_PROGRESS.toString())),
//                        StgCaseInProgressSpecification.filterRecordStatus(Constant.RECORD_STATUS_SUCCESS),
//                        StgCaseInProgressSpecification.filterLoadStatus(Arrays.asList("", " ", null))
//                )
//        );
//
//        // 2. Preload STM entities
//        Set<String> stmCodes = cases.stream()
//                .map(StgCaseInProgressModel::getServiceTypeMatrixCodeNew)
//                .filter(Objects::nonNull)
//                .filter(code -> !code.isBlank())
//                .collect(Collectors.toSet());
//
//        Map<String, ServiceTypeMatrixModel> stmMap = serviceTypeMatrixRepository
//                .findAllByServiceTypeMatrixCodeInAndStatusCode(stmCodes, 0)
//                .stream()
//                .collect(Collectors.toMap(ServiceTypeMatrixModel::getServiceTypeMatrixCode, Function.identity()));
//
//        // 3. Preload SLA by caseId (bulk query)
//        List<String> externalIds = cases.stream()
//                .map(StgCaseInProgressModel::getSfId)
//                .filter(Objects::nonNull)
//                .toList();
//
//        Map<String, List<StgSlaPerOwnerModel>> slaMap = findAllSlaGroupedByCase(externalIds);
//
//        // 4. Init counters
//        int totalCanMigrate = 0, totalFixedCases = 0, totalDynamicCases = 0;
//        int canMigrateFixedCases = 0, canMatchDynamicCases = 0;
//        int cannotMigrateFixedButCreate = 0, nullStmCodeCount = 0, notFoundStmCount = 0;
//        int sameCreateAndOwnerTeamCase = 0, fcrCaseFixCount = 0, fcrCaseDynamicCount = 0;
//
//        for (StgCaseInProgressModel caseInProgress : cases) {
//            TempStgCaseInProgressLogModel caseLog = new TempStgCaseInProgressLogModel();
//            BeanUtils.copyProperties(caseInProgress, caseLog);
//            CreateCaseDTO dto = caseTransactionMapper.map(caseLog);
//            if (!isCaseOnGoing(dto.getCaseStatusCode())) continue;
//
//            String stmCode = dto.getServiceTypeMatrixCode();
//            if (stmCode == null || stmCode.isBlank()) {
//                nullStmCodeCount++;
//                continue;
//            }
//
//            ServiceTypeMatrixModel stm = stmMap.get(stmCode);
//            if (stm == null) {
//                notFoundStmCount++;
//                continue;
//            }
//
//            String integrationSystem = dto.getIntegrationSystem();
//            if (integrationSystem == null || integrationSystem.isBlank()) continue;
//
//            ServiceTypeMatrixTypeEnum matrixType = stm.getServiceTypeMatrixType();
//            if (matrixType == ServiceTypeMatrixTypeEnum.FIX) totalFixedCases++;
//            else if (matrixType == ServiceTypeMatrixTypeEnum.DYNAMIC) totalDynamicCases++;
//
//            caseInProgress.setIsNewcase(false);
//
//            if (Boolean.TRUE.equals(stm.getFcr())) {
//                totalCanMigrate++;
//                if (matrixType == ServiceTypeMatrixTypeEnum.FIX) {
//                    canMigrateFixedCases++;
//                    fcrCaseFixCount++;
//                } else {
//                    canMatchDynamicCases++;
//                    fcrCaseDynamicCount++;
//                }
//                continue;
//            }
//
//            List<TempStgSlaPerOwnerLogModel> slaLogs = slaMap
//                    .getOrDefault(dto.getExternalId(), List.of())
//                    .stream()
//                    .map(model -> {
//                        TempStgSlaPerOwnerLogModel log = new TempStgSlaPerOwnerLogModel();
//                        BeanUtils.copyProperties(model, log);
//                        return log;
//                    }).toList();
//
//            // กรณีทีมสร้างกับทีม owner เหมือนกัน
//            if (Objects.equals(caseInProgress.getCreatedByTeamNew(), caseInProgress.getOwnerTeamC())) {
//                boolean hasOtherTeam = slaLogs.stream()
//                        .anyMatch(sla -> !Objects.equals(sla.getOwnerTeamC(), caseInProgress.getCreatedByTeamNew()));
//
//                if (!hasOtherTeam) {
//                    totalCanMigrate++;
//                    sameCreateAndOwnerTeamCase++;
//                    if (matrixType == ServiceTypeMatrixTypeEnum.FIX) canMigrateFixedCases++;
//                    else canMatchDynamicCases++;
//                    continue;
//                }
//            }
//
//            InProgressMigrationResult migrationResult = caseMigrationService.shouldUseLegacySlaData(stm, slaLogs);
//            if (migrationResult.isCanMigrate()) {
//                totalCanMigrate++;
//                if (matrixType == ServiceTypeMatrixTypeEnum.FIX) canMigrateFixedCases++;
//                else canMatchDynamicCases++;
//            } else if (matrixType == ServiceTypeMatrixTypeEnum.FIX) {
//                cannotMigrateFixedButCreate++;
//                caseInProgress.setIsNewcase(true);
//            } else if (matrixType == ServiceTypeMatrixTypeEnum.DYNAMIC) {
//                totalCanMigrate++;
//            }
//        }
//
//        stgCaseInProgressRepository.saveAll(cases);
//
//        CountCaseOnGoingCanMigrateResponse response = new CountCaseOnGoingCanMigrateResponse(
//                totalCanMigrate, cases.size(), totalFixedCases, totalDynamicCases,
//                canMigrateFixedCases, canMatchDynamicCases, cannotMigrateFixedButCreate,
//                nullStmCodeCount, notFoundStmCount, sameCreateAndOwnerTeamCase,
//                fcrCaseFixCount, fcrCaseDynamicCount
//        );
//
//
//        return BaseResponseUtil.success(response);
//    }
//
//    public Map<String, List<StgSlaPerOwnerModel>> findAllSlaGroupedByCase(List<String> caseIds) {
//        Map<String, List<StgSlaPerOwnerModel>> resultMap = new HashMap<>();
//        int batchSize = 1000;
//
//        for (int i = 0; i < caseIds.size(); i += batchSize) {
//            int end = Math.min(i + batchSize, caseIds.size());
//            List<String> batch = caseIds.subList(i, end);
//
//            List<StgSlaPerOwnerModel> slaBatch = stgSlaPerOwnerRepository.findAllByCaseCIn(batch);
//
//            for (StgSlaPerOwnerModel sla : slaBatch) {
//                resultMap
//                        .computeIfAbsent(sla.getCaseC(), k -> new ArrayList<>())
//                        .add(sla);
//            }
//        }
//
//        return resultMap;
//    }
//
//    private boolean isCaseOnGoing(String status) {
//        return List.of(CaseStatus.NEW.toString(), CaseStatus.IN_PROGRESS.toString()).contains(status);
//    }
//}