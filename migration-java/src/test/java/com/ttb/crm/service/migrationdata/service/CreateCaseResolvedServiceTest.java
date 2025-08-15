package com.ttb.crm.service.migrationdata.service;

import com.ttb.crm.lib.crmssp_encrypt_decrypt_lib.CrmEncryptKeyService;
import com.ttb.crm.lib.crmssp_encrypt_decrypt_lib.bean.RetrieveEncryptKeyResponse;
import com.ttb.crm.service.migrationdata.bean.StgToCaseWriterDTO;
import com.ttb.crm.service.migrationdata.enums.CaseSlaActivityAction;
import com.ttb.crm.service.migrationdata.enums.ServiceTypeMatrixTypeEnum;
import com.ttb.crm.service.migrationdata.helper.Constant;
import com.ttb.crm.service.migrationdata.helper.DateTimeUtils;
import com.ttb.crm.service.migrationdata.helper.MockEmployeeUser;
import com.ttb.crm.service.migrationdata.helper.MockSTM;
import com.ttb.crm.service.migrationdata.helper.MockTeam;
import com.ttb.crm.service.migrationdata.helper.MockTempCase;
import com.ttb.crm.service.migrationdata.helper.MockTempSla;
import com.ttb.crm.service.migrationdata.model.caseManagement.CaseSlaActivity;
import com.ttb.crm.service.migrationdata.model.caseManagement.CaseSlaHopModel;
import com.ttb.crm.service.migrationdata.model.caseManagement.CaseTransactionModel;
import com.ttb.crm.service.migrationdata.model.masterManagement.ServiceTypeMatrixModel;
import com.ttb.crm.service.migrationdata.model.secondary.StgCaseInProgressModel;
import com.ttb.crm.service.migrationdata.model.secondary.StgSlaPerOwnerModel;
import com.ttb.crm.service.migrationdata.model.userManagement.EmployeeUserModel;
import com.ttb.crm.service.migrationdata.model.userManagement.TeamReadonlyModel;
import com.ttb.crm.service.migrationdata.repository.caseManagement.CaseSlaActivityRepository;
import com.ttb.crm.service.migrationdata.repository.caseManagement.CaseSlaHopRepository;
import com.ttb.crm.service.migrationdata.repository.caseManagement.CaseTransactionRepository;
import com.ttb.crm.service.migrationdata.repository.masterManagement.ServiceTypeMatrixRepository;
import com.ttb.crm.service.migrationdata.repository.secondary.StgSlaPerOwnerRepository;
import com.ttb.crm.service.migrationdata.repository.userManagement.EmployeeUserRepository;
import com.ttb.crm.service.migrationdata.repository.userManagement.TeamReadonlyRepository;
import com.ttb.crm.service.migrationdata.service.preparecaesdataservice.CaseTransactionMapper;
import com.ttb.crm.service.migrationdata.service.preparecaesdataservice.CreateCaseService;
import org.apache.zookeeper.Op;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@SpringBootTest
class CreateCaseResolvedServiceTest {

    @Autowired
    private CreateCaseService createCaseService;

    @Autowired
    private CaseTransactionMapper caseTransactionMapper;

    @Autowired
    private MockTempCase mockTempCase;

    @Autowired
    private MockTempSla mockTempSla;

    @Autowired
    private MockSTM mockSTM;

    @Autowired
    private MockTeam mockTeam;

    @Autowired
    private MockEmployeeUser mockEmployeeUser;

    @MockitoBean
    private ServiceTypeMatrixRepository serviceTypeMatrixRepository;

    @MockitoBean
    private TeamReadonlyRepository teamReadonlyRepository;

    @MockitoBean
    private EmployeeUserRepository employeeUserRepository;

    @MockitoBean
    private StgSlaPerOwnerRepository stgSlaPerOwnerRepository;

    @MockitoBean
    private CrmEncryptKeyService crmEncryptKeyService;

    @MockitoBean
    private CaseTransactionRepository caseRepository;

    @MockitoBean
    private CaseSlaHopRepository caseSlaHopRepository;

    @MockitoBean
    private CaseSlaActivityRepository caseSlaActivityRepository;

    @MockitoBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @MockitoBean
    private CacheManager cacheManager;

    private StgCaseInProgressModel tempCaseModel;
    private List<StgSlaPerOwnerModel> stgSlaPerOwnerModels;

    @BeforeEach
    void setUp() {
        when(cacheManager.getCache(anyString())).thenReturn(mock(Cache.class));

        stgSlaPerOwnerModels = new ArrayList<>();
        tempCaseModel = mockTempCase.mockTempStgCaseInProgressResolved();

        RetrieveEncryptKeyResponse response = new RetrieveEncryptKeyResponse();
        response.setOwnerName("TEST");
        response.setEncryptKey("WG3+YMfeP1pdDYGuJfxzlOUhsYInK1WV2m+MMB/KcfI=");
        response.setEncryptFieldInitialVector("BW1+YMfeP1pdDYGuJfxzlOUhsYInK1WV2m+MMB/Kcrm=");
        response.setEncryptFieldKey("4e5f6d7c8b9a0d1e2f3c4b5a6d7e8f9a");

        when(crmEncryptKeyService.getEncryptionKey(Mockito.anyString())).thenReturn(response);

        try (MockedStatic<DateTimeUtils> mockedStatic = mockStatic(DateTimeUtils.class)) {
            mockedStatic.when(DateTimeUtils::getStaringLocalDateTime)
                    .thenReturn("2025-07-30T04:00:00.000Z");
        }
    }

    @Test
    void caseResolvedFcrWithCanMigrationCase() {
        ServiceTypeMatrixModel serviceTypeMatrixModel = mockSTM.mockSTMFixFCR();
        TeamReadonlyModel createdByTeam = mockTeam.mockTeam(tempCaseModel.getCreatedByTeamNew());
        EmployeeUserModel createByUser = mockEmployeeUser.mockEmployeeUser(tempCaseModel.getCreatedByEmployeeIdC());
        EmployeeUserModel naEmployee = mockEmployeeUser.mockEmployeeUser(Constant.NA_EMPLOYEE_ID);

       when(serviceTypeMatrixRepository.findByServiceTypeMatrixCode(Mockito.anyString()))
                .thenReturn(serviceTypeMatrixModel);

        when(teamReadonlyRepository.findByNameTh(Mockito.anyString())).thenReturn(Optional.ofNullable(createdByTeam));

        when(employeeUserRepository.findByEmployeeId(Mockito.anyString()))
                .thenReturn(
                        Optional.ofNullable(createByUser),
                        Optional.ofNullable(naEmployee)
                );

        when(stgSlaPerOwnerRepository.findAllByCaseCOrderByStartDateTimeCAsc(tempCaseModel.getSfId())).thenReturn(stgSlaPerOwnerModels);

        when(caseRepository.save(Mockito.any(CaseTransactionModel.class))).thenAnswer(invocation -> {
            CaseTransactionModel input = invocation.getArgument(0);

            // simulate JPA @GeneratedValue
            input.setCaseId(UUID.randomUUID());

            return input;
        });

        when(caseSlaHopRepository.saveAll(Mockito.anyList()))
                .thenAnswer(invocation -> {
                    List<CaseSlaHopModel> inputList = invocation.getArgument(0);
                    for (CaseSlaHopModel item : inputList) {
                        item.setCaseSlaHopId(UUID.randomUUID());
                    }

                    return inputList;
                });

        when(caseSlaActivityRepository.saveAll(Mockito.anyList()))
                .thenAnswer(invocation -> {
                    List<CaseSlaActivity> inputList = invocation.getArgument(0);
                    for (CaseSlaActivity item : inputList) {
                        item.setCaseSlaActivityId(UUID.randomUUID());
                    }

                    return inputList;
                });

        when(kafkaTemplate.send(Mockito.any(Message.class)))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        StgToCaseWriterDTO transaction = createCaseService.createCase(tempCaseModel);

        CaseTransactionModel caseModel = transaction.getCaseTransaction();

        // Case
        assertEquals(tempCaseModel.getCaseNumber(), caseModel.getCaseNumber());
        assertEquals(createdByTeam.getTeamId(), caseModel.getCreatedByTeamId());
        assertEquals(createByUser.getUserId(), caseModel.getCreatedById());
        assertEquals(createByUser.getUserId(), caseModel.getModifiedById());
        ZonedDateTime createDate = DateTimeUtils.parseToZoneDateTime(tempCaseModel.getCreatedDate());
        assertEquals(createDate, caseModel.getCreatedOn());
        assertNotNull(caseModel.getModifiedOn());
        assertEquals(Constant.CASE_STATUS_COMPLETED, caseModel.getCaseStatusCode());
        assertEquals(Constant.TEP, caseModel.getIntegrationSystem());
        assertNull(caseModel.getTotalDuration());

        // Case Resolved Info
        assertNull(caseModel.getResolvedBy());
        assertNull(caseModel.getResolvedDate());
        assertNull(caseModel.getResolvedById());
        assertNull(caseModel.getResolvedByTeamId());

        // Case Closed Info
        assertEquals(createByUser.getFullNameTH(), caseModel.getClosedBy());
        assertEquals(createDate.plusNanos(1_000_000L), caseModel.getClosedDate());
        assertEquals(createByUser.getUserId(), caseModel.getClosedById());
        assertEquals(createdByTeam.getTeamId(), caseModel.getClosedByTeamId());

        // Service type matrix
        assertNotNull(serviceTypeMatrixModel);
        assertEquals(serviceTypeMatrixModel.getServiceTypeMatrixCode(), caseModel.getServiceTypeMatrixCode());
        assertEquals(serviceTypeMatrixModel.getServiceTypeMatrixType(), caseModel.getServiceTypeMatrixType());
        assertEquals(serviceTypeMatrixModel.getSla(), caseModel.getSla());
        assertEquals(serviceTypeMatrixModel.getFcr(), caseModel.getFcr());
        assertEquals(ServiceTypeMatrixTypeEnum.FIX, caseModel.getServiceTypeMatrixType());

        // Case Sla hop
        List<CaseSlaHopModel> caseSlas = caseModel.getSlaHop();
        assertEquals(0, caseSlas.size());

        // Case sla activities
        List<CaseSlaActivity> caseSlaActivities = transaction.getCaseTransaction().getCaseSlaActivities();
        assertNotNull(caseSlaActivities);
        assertEquals(2, caseSlaActivities.size());

        // First sla activity
        CaseSlaActivity firstActivity = caseSlaActivities.getFirst();
        assertNull(firstActivity.getCaseSlaActivityId());
        assertEquals(createDate, firstActivity.getCreatedOn());
        assertEquals(createDate, firstActivity.getModifiedOn());
        assertEquals(createByUser.getUserId(), firstActivity.getCreatedById());
        assertEquals(createByUser.getUserId(), firstActivity.getModifiedById());
        assertEquals(createByUser.getUserId(), firstActivity.getOwnerId());
        assertEquals(createByUser.getFullNameTH(), firstActivity.getOwnerName());
        assertEquals(createdByTeam.getTeamId(), firstActivity.getTeamId());
        assertEquals(createdByTeam.getNameTh(), firstActivity.getTeamNameTh());
        assertEquals(createDate, firstActivity.getStartDate());
        assertEquals(createDate, firstActivity.getEndDate());
        assertEquals(createByUser.getFullNameTH(), firstActivity.getCreatedByName());
        assertEquals(createByUser.getFullNameTH(), firstActivity.getModifiedByName());
        assertEquals(caseModel.getCaseNumber(), firstActivity.getCases().getCaseNumber());
        assertEquals(1, firstActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.CREATE, firstActivity.getAction());
        assertEquals(0.0, firstActivity.getActualDuration(), 0.0000001);

        // Second sla activity
        CaseSlaActivity secondActivity = caseSlaActivities.get(1);
        assertNull(secondActivity.getCaseSlaActivityId());
        assertEquals(createDate.plusNanos(1_000_000L), secondActivity.getCreatedOn());
        assertEquals(createDate.plusNanos(1_000_000L), secondActivity.getModifiedOn());
        assertEquals(createByUser.getUserId(), secondActivity.getCreatedById());
        assertEquals(createByUser.getUserId(), secondActivity.getModifiedById());
        assertEquals(createByUser.getUserId(), secondActivity.getOwnerId());
        assertEquals(createByUser.getFullNameTH(), secondActivity.getOwnerName());
        assertEquals(createdByTeam.getTeamId(), secondActivity.getTeamId());
        assertEquals(createdByTeam.getNameTh(), secondActivity.getTeamNameTh());
        assertEquals(createDate.plusNanos(1_000_000L), secondActivity.getStartDate());
        assertEquals(createDate.plusNanos(1_000_000L), secondActivity.getEndDate());
        assertEquals(createByUser.getFullNameTH(), secondActivity.getCreatedByName());
        assertEquals(createByUser.getFullNameTH(), secondActivity.getModifiedByName());
        assertEquals(caseModel.getCaseNumber(), secondActivity.getCases().getCaseNumber());
        assertEquals(1, secondActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.AUTO_COMPLETED, secondActivity.getAction());
        assertNull(secondActivity.getActualDuration());
        assertEquals(secondActivity.getCreatedOn(), caseModel.getModifiedOn());
    }

    @Test
    void caseResolvedNextHistoryIsClosedByBuAndHaveOwnerCanMigrationCase() {
        mockTempSla.mockTempStgSlaPerOwnerResolvedNextHistoryIsClosedByBuAndHaveOwner(stgSlaPerOwnerModels);
        ServiceTypeMatrixModel serviceTypeMatrixModel = mockSTM.mockSTMFixWithoutFCR();
        TeamReadonlyModel createdByTeam = mockTeam.mockTeam(tempCaseModel.getCreatedByTeamNew());
        EmployeeUserModel createByUser = mockEmployeeUser.mockEmployeeUser(tempCaseModel.getCreatedByEmployeeIdC());
        EmployeeUserModel naEmployee = mockEmployeeUser.mockEmployeeUser(Constant.NA_EMPLOYEE_ID);
        EmployeeUserModel systemEmployee = mockEmployeeUser.mockEmployeeUser(Constant.SYSTEM_EMPLOYEE_ID);

        EmployeeUserModel userSecondHop = mockEmployeeUser.mockEmployeeUser(stgSlaPerOwnerModels.getFirst());
        EmployeeUserModel userThirdHop = mockEmployeeUser.mockEmployeeUser(stgSlaPerOwnerModels.get(1));
        EmployeeUserModel userFourthHop = mockEmployeeUser.mockEmployeeUser(stgSlaPerOwnerModels.getLast());

        TeamReadonlyModel teamSecondHop = mockTeam.mockTeam(stgSlaPerOwnerModels.getFirst().getOwnerTeamNew());
        TeamReadonlyModel teamThirdHop = mockTeam.mockTeam(stgSlaPerOwnerModels.get(1).getOwnerTeamNew());
        TeamReadonlyModel teamFourthHop = mockTeam.mockTeam(stgSlaPerOwnerModels.getLast().getOwnerTeamNew());

        when(serviceTypeMatrixRepository.findByServiceTypeMatrixCode(Mockito.anyString()))
                .thenReturn(serviceTypeMatrixModel);

        when(teamReadonlyRepository.findByNameTh(Mockito.anyString()))
                .thenReturn(
                        Optional.ofNullable(createdByTeam),
                        Optional.ofNullable(teamSecondHop),
                        Optional.ofNullable(teamThirdHop),
                        Optional.ofNullable(teamFourthHop));

        when(employeeUserRepository.findByEmployeeId(Mockito.anyString()))
                .thenReturn(
                        Optional.ofNullable(createByUser),
                        Optional.ofNullable(naEmployee),
                        Optional.ofNullable(systemEmployee),
                        Optional.ofNullable(userSecondHop),
                        Optional.ofNullable(userThirdHop),
                        Optional.ofNullable(userFourthHop));

        when(employeeUserRepository.findTeamIdByEmployeeId(Mockito.anyString()))
                .thenReturn(
                        List.of(teamSecondHop.getTeamId().toString()),
                        List.of(teamThirdHop.getTeamId().toString()),
                        List.of(teamFourthHop.getTeamId().toString()));

        when(teamReadonlyRepository.findAllByTeamIdInAndStatusCode(Mockito.any(List.class), Mockito.anyInt()))
                .thenReturn(List.of(teamSecondHop))
                .thenReturn(List.of(teamThirdHop))
                .thenReturn(List.of(teamFourthHop));

        when(stgSlaPerOwnerRepository.findAllByCaseCOrderByStartDateTimeCAsc(tempCaseModel.getSfId())).thenReturn(stgSlaPerOwnerModels);

        when(caseRepository.save(Mockito.any(CaseTransactionModel.class))).thenAnswer(invocation -> {
            CaseTransactionModel input = invocation.getArgument(0);

            // simulate JPA @GeneratedValue
            input.setCaseId(UUID.randomUUID());

            return input;
        });

        when(caseSlaHopRepository.saveAll(Mockito.anyList()))
                .thenAnswer(invocation -> {
                    List<CaseSlaHopModel> inputList = invocation.getArgument(0);
                    for (CaseSlaHopModel item : inputList) {
                        item.setCaseSlaHopId(UUID.randomUUID());
                    }

                    return inputList;
                });

        when(caseSlaActivityRepository.saveAll(Mockito.anyList()))
                .thenAnswer(invocation -> {
                    List<CaseSlaActivity> inputList = invocation.getArgument(0);
                    for (CaseSlaActivity item : inputList) {
                        item.setCaseSlaActivityId(UUID.randomUUID());
                    }

                    return inputList;
                });

        when(kafkaTemplate.send(Mockito.any(Message.class)))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        StgToCaseWriterDTO transaction = createCaseService.createCase(tempCaseModel);

        CaseTransactionModel caseModel = transaction.getCaseTransaction();

        // Case
        assertEquals(tempCaseModel.getCaseNumber(), caseModel.getCaseNumber());
        assertEquals(createdByTeam.getTeamId(), caseModel.getCreatedByTeamId());
        assertEquals(createByUser.getUserId(), caseModel.getCreatedById());
        assertEquals(teamFourthHop.getTeamId(), caseModel.getTeamId());
        assertEquals(teamFourthHop.getNameTh(), caseModel.getTeamName());
        assertNotNull(caseModel.getModifiedById());
        ZonedDateTime createDate = DateTimeUtils.parseToZoneDateTime(tempCaseModel.getCreatedDate());
        assertEquals(createDate, caseModel.getCreatedOn());
        assertNotNull(caseModel.getModifiedOn());
        assertEquals(Constant.CASE_STATUS_RESOLVED, caseModel.getCaseStatusCode());
        assertEquals(Constant.TEP, caseModel.getIntegrationSystem());
        assertEquals(4, caseModel.getActiveHopNumber());
        assertNull(caseModel.getTotalDuration());

        // Case Resolved Info
        assertEquals(userThirdHop.getFullNameTH(), caseModel.getResolvedBy());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()), caseModel.getResolvedDate());
        assertEquals(userThirdHop.getUserId(), caseModel.getResolvedById());
        assertEquals(teamThirdHop.getTeamId(), caseModel.getResolvedByTeamId());

        // Case Closed Info
        assertNull(caseModel.getClosedBy());
        assertNull(caseModel.getClosedDate());
        assertNull(caseModel.getClosedById());
        assertNull(caseModel.getClosedByTeamId());

        // Service type matrix
        assertNotNull(serviceTypeMatrixModel);
        assertEquals(serviceTypeMatrixModel.getServiceTypeMatrixCode(), caseModel.getServiceTypeMatrixCode());
        assertEquals(serviceTypeMatrixModel.getServiceTypeMatrixType(), caseModel.getServiceTypeMatrixType());
        assertEquals(serviceTypeMatrixModel.getSla(), caseModel.getSla());
        assertEquals(serviceTypeMatrixModel.getFcr(), caseModel.getFcr());
        assertEquals(ServiceTypeMatrixTypeEnum.FIX, caseModel.getServiceTypeMatrixType());

        // Case Sla hop
        List<CaseSlaHopModel> caseSlas = caseModel.getSlaHop();
        assertNotNull(caseSlas);
        assertEquals(4, caseSlas.size());

        // First case sla hop
        CaseSlaHopModel firstHop = caseSlas.getFirst();
        assertNull(firstHop.getCaseSlaHopId());
        assertEquals(createDate, firstHop.getStartDatetime());
        assertEquals(createDate, firstHop.getEndDatetime());
        assertEquals(createByUser.getUserId(), firstHop.getOwnerId());
        assertEquals(createByUser.getFullNameTH(), firstHop.getOwnerName());
        assertEquals(createdByTeam.getTeamId(), firstHop.getTeamId());
        assertEquals(createdByTeam.getNameTh(), firstHop.getTeamName());
        assertEquals(createDate, firstHop.getCreatedOn());
        assertEquals(createDate, firstHop.getModifiedOn());
        assertEquals(createByUser.getUserId(), firstHop.getCreatedById());
        assertEquals(createByUser.getUserId(), firstHop.getModifiedById());
        assertEquals(1, firstHop.getHopNumber());
        assertEquals(0, firstHop.getTotalDuration());
        assertEquals(false, firstHop.getCloseByBu());
        assertNull(firstHop.getSlaTarget());
        assertNull(firstHop.getSlaTargetDate());
        assertEquals(0.0, firstHop.getTotalDuration(), 0.0000001);
        assertEquals(createdByTeam.getNameTh(), firstHop.getTeamName());
        assertEquals(createdByTeam.getTeamId(), firstHop.getTeamId());
        assertEquals(caseModel.getCaseId(), firstHop.getCases().getCaseId());

        // Second Case Sla hop
        CaseSlaHopModel secondHop = caseSlas.get(1);
        assertNull(secondHop.getCaseSlaHopId());
        assertNull(secondHop.getTotalDuration());
        assertNull(secondHop.getSlaTarget());
        assertNull(secondHop.getSlaTargetDate());
        assertNull(secondHop.getTotalDuration());
        assertNotNull(secondHop.getOwnerId());
        assertEquals(stgSlaPerOwnerModels.getFirst().getName(), secondHop.getOwnerName());
        assertEquals(teamSecondHop.getNameTh(), secondHop.getTeamName());
        assertEquals(teamSecondHop.getTeamId(), secondHop.getTeamId());
        assertEquals(2, secondHop.getHopNumber());
        assertEquals(false, secondHop.getCloseByBu());
        assertEquals(createByUser.getUserId(), secondHop.getCreatedById());
        assertEquals(userSecondHop.getUserId(), secondHop.getModifiedById());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getStartDateTimeC()), secondHop.getStartDatetime());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()), secondHop.getEndDatetime());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getStartDateTimeC()), secondHop.getCreatedOn());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()), secondHop.getModifiedOn());
        assertEquals(caseModel.getCaseId(), secondHop.getCases().getCaseId());

        // Third(last) Case sla hop
        CaseSlaHopModel thirdHop = caseSlas.get(2);
        assertNull(thirdHop.getCaseSlaHopId());
        assertNull(thirdHop.getSlaTarget());
        assertNull(thirdHop.getSlaTargetDate());
        assertNull(thirdHop.getTotalDuration());
        assertNotNull(thirdHop.getOwnerId());
        assertEquals(stgSlaPerOwnerModels.get(1).getName(), thirdHop.getOwnerName());
        assertEquals(teamThirdHop.getNameTh(), thirdHop.getTeamName());
        assertEquals(teamThirdHop.getTeamId(), thirdHop.getTeamId());
        assertEquals(userSecondHop.getUserId(), thirdHop.getCreatedById());
        assertEquals(userThirdHop.getUserId(), thirdHop.getModifiedById());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getStartDateTimeC()), thirdHop.getStartDatetime());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()), thirdHop.getEndDatetime());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getStartDateTimeC()), thirdHop.getCreatedOn());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()), thirdHop.getModifiedOn());
        assertEquals(3, thirdHop.getHopNumber());
        assertEquals(false, thirdHop.getCloseByBu());
        assertEquals(caseModel.getCaseId(), thirdHop.getCases().getCaseId());

        // Fourth(last) Case sla hop
        CaseSlaHopModel fourthHop = caseSlas.getLast();
        assertNull(fourthHop.getCaseSlaHopId());
        assertNull(fourthHop.getSlaTarget());
        assertNull(fourthHop.getSlaTargetDate());
        assertNull(fourthHop.getTotalDuration());
        assertNotNull(fourthHop.getOwnerId());
        assertEquals(stgSlaPerOwnerModels.getLast().getName(), fourthHop.getOwnerName());
        assertEquals(teamFourthHop.getNameTh(), fourthHop.getTeamName());
        assertEquals(teamFourthHop.getTeamId(), fourthHop.getTeamId());
        assertEquals(userThirdHop.getUserId(), fourthHop.getCreatedById());
        assertEquals(userFourthHop.getUserId(), fourthHop.getModifiedById());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getLast().getStartDateTimeC()), fourthHop.getStartDatetime());
        assertNull(fourthHop.getEndDatetime());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getLast().getStartDateTimeC()), fourthHop.getCreatedOn());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getLast().getStartDateTimeC()), fourthHop.getModifiedOn());
        assertEquals(4, fourthHop.getHopNumber());
        assertEquals(true, fourthHop.getCloseByBu());
        assertEquals(caseModel.getCaseId(), fourthHop.getCases().getCaseId());

        // Case sla activities
        List<CaseSlaActivity> caseSlaActivities = transaction.getCaseTransaction().getCaseSlaActivities();
        assertNotNull(caseSlaActivities);
        assertEquals(11, caseSlaActivities.size());

        // First sla activity
        CaseSlaActivity firstActivity = caseSlaActivities.getFirst();
        assertNull(firstActivity.getCaseSlaActivityId());
        assertEquals(createByUser.getUserId(), firstActivity.getOwnerId());
        assertEquals(createByUser.getFullNameTH(), firstActivity.getOwnerName());
        assertEquals(createdByTeam.getTeamId(), firstActivity.getTeamId());
        assertEquals(createdByTeam.getNameTh(), firstActivity.getTeamNameTh());
        assertEquals(createByUser.getFullNameTH(), firstActivity.getCreatedByName());
        assertEquals(createByUser.getFullNameTH(), firstActivity.getModifiedByName());
        assertEquals(createByUser.getUserId(), firstActivity.getCreatedById());
        assertEquals(createByUser.getUserId(), firstActivity.getModifiedById());
        assertEquals(createDate, firstActivity.getStartDate());
        assertEquals(createDate, firstActivity.getEndDate());
        assertEquals(createDate, firstActivity.getCreatedOn());
        assertEquals(createDate, firstActivity.getModifiedOn());
        assertEquals(caseModel.getCaseNumber(), firstActivity.getCases().getCaseNumber());
        assertEquals(1, firstActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.CREATE, firstActivity.getAction());
        assertEquals(0.0, firstActivity.getActualDuration(), 0.0000001);

        // Second sla activity
        CaseSlaActivity secondActivity = caseSlaActivities.get(1);
        assertNull(secondActivity.getCaseSlaActivityId());
        assertEquals(createByUser.getUserId(), secondActivity.getOwnerId());
        assertEquals(createByUser.getFullNameTH(), secondActivity.getOwnerName());
        assertEquals(createdByTeam.getTeamId(), secondActivity.getTeamId());
        assertEquals(createdByTeam.getNameTh(), secondActivity.getTeamNameTh());
        assertEquals(createByUser.getFullNameTH(), secondActivity.getCreatedByName());
        assertEquals(createByUser.getFullNameTH(), secondActivity.getModifiedByName());
        assertEquals(createByUser.getUserId(), secondActivity.getCreatedById());
        assertEquals(createByUser.getUserId(), secondActivity.getModifiedById());
        assertEquals(createDate.plusNanos(1_000_000L), secondActivity.getStartDate());
        assertEquals(createDate.plusNanos(1_000_000L), secondActivity.getEndDate());
        assertEquals(createDate.plusNanos(1_000_000L), secondActivity.getCreatedOn());
        assertEquals(createDate.plusNanos(1_000_000L), secondActivity.getModifiedOn());
        assertNull(secondActivity.getActualDuration());
        assertEquals(caseModel.getCaseNumber(), secondActivity.getCases().getCaseNumber());
        assertEquals(1, secondActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.NEXT_HOP, secondActivity.getAction());

        // Third sla activity
        CaseSlaActivity thirdActivity = caseSlaActivities.get(2);
        assertNull(thirdActivity.getCaseSlaActivityId());
        assertNull(thirdActivity.getOwnerId());
        assertNull(thirdActivity.getOwnerName());
        assertEquals(teamSecondHop.getTeamId(), thirdActivity.getTeamId());
        assertEquals(teamSecondHop.getNameTh(), thirdActivity.getTeamNameTh());
        assertEquals(createByUser.getFullNameTH(), thirdActivity.getCreatedByName());
        assertEquals(userSecondHop.getFullNameTH(), thirdActivity.getModifiedByName());
        assertEquals(createByUser.getUserId(), thirdActivity.getCreatedById());
        assertEquals(userSecondHop.getUserId(), thirdActivity.getModifiedById());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getStartDateTimeC()).plusNanos(2_000_000L), thirdActivity.getStartDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()).plusNanos(2_000_000L), thirdActivity.getEndDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getStartDateTimeC()).plusNanos(2_000_000L), thirdActivity.getCreatedOn());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()).plusNanos(2_000_000L), thirdActivity.getModifiedOn());
        assertNotNull(thirdActivity.getActualDuration());
        assertEquals(caseModel.getCaseNumber(), thirdActivity.getCases().getCaseNumber());
        assertEquals(2, thirdActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.ASSIGN_TEAM, thirdActivity.getAction());

        // Fourth sla activity
        CaseSlaActivity fourthActivity = caseSlaActivities.get(3);
        assertNull(fourthActivity.getCaseSlaActivityId());
        assertEquals(userSecondHop.getUserId(), fourthActivity.getOwnerId());
        assertEquals(userSecondHop.getFullNameTH(), fourthActivity.getOwnerName());
        assertEquals(teamSecondHop.getTeamId(), fourthActivity.getTeamId());
        assertEquals(teamSecondHop.getNameTh(), fourthActivity.getTeamNameTh());
        assertEquals(userSecondHop.getFullNameTH(), fourthActivity.getCreatedByName());
        assertEquals(userSecondHop.getFullNameTH(), fourthActivity.getModifiedByName());
        assertEquals(userSecondHop.getUserId(), fourthActivity.getCreatedById());
        assertEquals(userSecondHop.getUserId(), fourthActivity.getModifiedById());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getStartDateTimeC()).plusNanos(3_000_000L), fourthActivity.getStartDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()).plusNanos(3_000_000L), fourthActivity.getEndDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getStartDateTimeC()).plusNanos(3_000_000L), fourthActivity.getCreatedOn());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()).plusNanos(3_000_000L), fourthActivity.getModifiedOn());
        assertNotNull(fourthActivity.getActualDuration());
        assertEquals(caseModel.getCaseNumber(), fourthActivity.getCases().getCaseNumber());
        assertEquals(2, fourthActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.ASSIGN_OWNER, fourthActivity.getAction());

        // Fifth sla activity
        CaseSlaActivity fifthActivity = caseSlaActivities.get(4);
        assertNull(fifthActivity.getCaseSlaActivityId());
        assertEquals(userSecondHop.getUserId(), fifthActivity.getOwnerId());
        assertEquals(userSecondHop.getFullNameTH(), fifthActivity.getOwnerName());
        assertEquals(teamSecondHop.getTeamId(), fifthActivity.getTeamId());
        assertEquals(teamSecondHop.getNameTh(), fifthActivity.getTeamNameTh());
        assertEquals(userSecondHop.getFullNameTH(), fifthActivity.getCreatedByName());
        assertEquals(userSecondHop.getFullNameTH(), fifthActivity.getModifiedByName());
        assertEquals(userSecondHop.getUserId(), fifthActivity.getCreatedById());
        assertEquals(userSecondHop.getUserId(), fifthActivity.getModifiedById());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()).plusNanos(4_000_000L), fifthActivity.getStartDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()).plusNanos(4_000_000L), fifthActivity.getEndDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()).plusNanos(4_000_000L), fifthActivity.getCreatedOn());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()).plusNanos(4_000_000L), fifthActivity.getModifiedOn());
        assertNull(fifthActivity.getActualDuration());
        assertEquals(caseModel.getCaseNumber(), fifthActivity.getCases().getCaseNumber());
        assertEquals(2, fifthActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.NEXT_HOP, fifthActivity.getAction());

        // Sixth sla activity
        CaseSlaActivity sixthActivity = caseSlaActivities.get(5);
        assertNull(sixthActivity.getCaseSlaActivityId());
        assertNull(sixthActivity.getOwnerId());
        assertNull(sixthActivity.getOwnerName());
        assertEquals(teamThirdHop.getTeamId(), sixthActivity.getTeamId());
        assertEquals(teamThirdHop.getNameTh(), sixthActivity.getTeamNameTh());
        assertEquals(userSecondHop.getFullNameTH(), sixthActivity.getCreatedByName());
        assertEquals(userThirdHop.getFullNameTH(), sixthActivity.getModifiedByName());
        assertEquals(userSecondHop.getUserId(), sixthActivity.getCreatedById());
        assertEquals(userThirdHop.getUserId(), sixthActivity.getModifiedById());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getStartDateTimeC()).plusNanos(5_000_000L), sixthActivity.getStartDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()).plusNanos(5_000_000L), sixthActivity.getEndDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getStartDateTimeC()).plusNanos(5_000_000L), sixthActivity.getCreatedOn());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()).plusNanos(5_000_000L), sixthActivity.getModifiedOn());
        assertEquals(0.625, sixthActivity.getActualDuration(), 0.0000001);
        assertEquals(caseModel.getCaseNumber(), sixthActivity.getCases().getCaseNumber());
        assertEquals(3, sixthActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.ASSIGN_TEAM, sixthActivity.getAction());

        // Seventh sla activity
        CaseSlaActivity seventhActivity = caseSlaActivities.get(6);
        assertNull(seventhActivity.getCaseSlaActivityId());
        assertEquals(userThirdHop.getUserId(), seventhActivity.getOwnerId());
        assertEquals(userThirdHop.getFullNameTH(), seventhActivity.getOwnerName());
        assertEquals(teamThirdHop.getTeamId(), seventhActivity.getTeamId());
        assertEquals(teamThirdHop.getNameTh(), seventhActivity.getTeamNameTh());
        assertEquals(userThirdHop.getFullNameTH(), seventhActivity.getCreatedByName());
        assertEquals(userThirdHop.getFullNameTH(), seventhActivity.getModifiedByName());
        assertEquals(userThirdHop.getUserId(), seventhActivity.getCreatedById());
        assertEquals(userThirdHop.getUserId(), seventhActivity.getModifiedById());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getStartDateTimeC()).plusNanos(6_000_000L), seventhActivity.getStartDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()).plusNanos(6_000_000L), seventhActivity.getEndDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getStartDateTimeC()).plusNanos(6_000_000L), seventhActivity.getCreatedOn());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()).plusNanos(6_000_000L), seventhActivity.getModifiedOn());
        assertEquals(0.625, seventhActivity.getActualDuration(), 0.0000001);
        assertEquals(caseModel.getCaseNumber(), seventhActivity.getCases().getCaseNumber());
        assertEquals(3, seventhActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.ASSIGN_OWNER, seventhActivity.getAction());

        // Eight sla activity
        CaseSlaActivity eightActivity = caseSlaActivities.get(7);
        assertNull(eightActivity.getCaseSlaActivityId());
        assertEquals(userThirdHop.getUserId(), eightActivity.getOwnerId());
        assertEquals(userThirdHop.getFullNameTH(), eightActivity.getOwnerName());
        assertEquals(teamThirdHop.getTeamId(), eightActivity.getTeamId());
        assertEquals(teamThirdHop.getNameTh(), eightActivity.getTeamNameTh());
        assertEquals(userThirdHop.getFullNameTH(), eightActivity.getCreatedByName());
        assertEquals(userThirdHop.getFullNameTH(), eightActivity.getModifiedByName());
        assertEquals(userThirdHop.getUserId(), eightActivity.getCreatedById());
        assertEquals(userThirdHop.getUserId(), eightActivity.getModifiedById());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()).plusNanos(7_000_000L), eightActivity.getStartDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()).plusNanos(7_000_000L), eightActivity.getEndDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()).plusNanos(7_000_000L), eightActivity.getCreatedOn());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()).plusNanos(7_000_000L), eightActivity.getModifiedOn());
        assertNull(eightActivity.getActualDuration());
        assertEquals(caseModel.getCaseNumber(), eightActivity.getCases().getCaseNumber());
        assertEquals(3, eightActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.RESOLVED, eightActivity.getAction());

        // Ninth sla activity
        CaseSlaActivity ninthActivity = caseSlaActivities.get(8);
        assertNull(ninthActivity.getCaseSlaActivityId());
        assertEquals(userThirdHop.getUserId(), ninthActivity.getOwnerId());
        assertEquals(userThirdHop.getFullNameTH(), ninthActivity.getOwnerName());
        assertEquals(teamThirdHop.getTeamId(), ninthActivity.getTeamId());
        assertEquals(teamThirdHop.getNameTh(), ninthActivity.getTeamNameTh());
        assertEquals(userThirdHop.getFullNameTH(), ninthActivity.getCreatedByName());
        assertEquals(userThirdHop.getFullNameTH(), ninthActivity.getModifiedByName());
        assertEquals(userThirdHop.getUserId(), ninthActivity.getCreatedById());
        assertEquals(userThirdHop.getUserId(), ninthActivity.getModifiedById());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()).plusNanos(8_000_000L), ninthActivity.getStartDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()).plusNanos(8_000_000L), ninthActivity.getEndDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()).plusNanos(8_000_000L), ninthActivity.getCreatedOn());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()).plusNanos(8_000_000L), ninthActivity.getModifiedOn());
        assertNull(ninthActivity.getActualDuration());
        assertEquals(caseModel.getCaseNumber(), ninthActivity.getCases().getCaseNumber());
        assertEquals(3, ninthActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.NEXT_HOP, ninthActivity.getAction());

        // Tenth sla activity
        CaseSlaActivity tenthActivity = caseSlaActivities.get(9);
        assertNull(tenthActivity.getCaseSlaActivityId());
        assertEquals(userThirdHop.getUserId(), ninthActivity.getOwnerId());
        assertEquals(userThirdHop.getFullNameTH(), ninthActivity.getOwnerName());
        assertEquals(teamFourthHop.getTeamId(), tenthActivity.getTeamId());
        assertEquals(teamFourthHop.getNameTh(), tenthActivity.getTeamNameTh());
        assertEquals(userThirdHop.getFullNameTH(), tenthActivity.getCreatedByName());
        assertEquals(userThirdHop.getFullNameTH(), tenthActivity.getModifiedByName());
        assertEquals(userThirdHop.getUserId(), tenthActivity.getCreatedById());
        assertEquals(userThirdHop.getUserId(), tenthActivity.getModifiedById());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getLast().getStartDateTimeC()).plusNanos(9_000_000L), tenthActivity.getStartDate());
        assertNull(tenthActivity.getEndDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getLast().getStartDateTimeC()).plusNanos(9_000_000L), tenthActivity.getCreatedOn());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getLast().getStartDateTimeC()).plusNanos(9_000_000L), tenthActivity.getModifiedOn());
        assertNull(tenthActivity.getActualDuration());
        assertEquals(caseModel.getCaseNumber(), tenthActivity.getCases().getCaseNumber());
        assertEquals(4, tenthActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.ASSIGN_TEAM, tenthActivity.getAction());

        // Eleventh sla activity
        CaseSlaActivity eleventhActivity = caseSlaActivities.get(10);
        assertNull(eleventhActivity.getCaseSlaActivityId());
        assertEquals(userFourthHop.getUserId(), eleventhActivity.getOwnerId());
        assertEquals(userFourthHop.getFullNameTH(), eleventhActivity.getOwnerName());
        assertEquals(teamFourthHop.getTeamId(), eleventhActivity.getTeamId());
        assertEquals(teamFourthHop.getNameTh(), eleventhActivity.getTeamNameTh());
        assertEquals(userFourthHop.getFullNameTH(), eleventhActivity.getCreatedByName());
        assertEquals(userFourthHop.getFullNameTH(), eleventhActivity.getModifiedByName());
        assertEquals(userFourthHop.getUserId(), eleventhActivity.getCreatedById());
        assertEquals(userFourthHop.getUserId(), eleventhActivity.getModifiedById());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getLast().getStartDateTimeC()).plusNanos(10_000_000L), eleventhActivity.getStartDate());
        assertNull(eleventhActivity.getEndDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getLast().getStartDateTimeC()).plusNanos(10_000_000L), eleventhActivity.getCreatedOn());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getLast().getStartDateTimeC()).plusNanos(10_000_000L), eleventhActivity.getModifiedOn());
        assertNull(eleventhActivity.getActualDuration());
        assertEquals(caseModel.getCaseNumber(), eleventhActivity.getCases().getCaseNumber());
        assertEquals(4, eleventhActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.ASSIGN_OWNER, eleventhActivity.getAction());

        // last modify data is same in case and last activity
        assertEquals(eleventhActivity.getModifiedOn(), caseModel.getModifiedOn());
        assertEquals(eleventhActivity.getModifiedById(), caseModel.getModifiedById());
    }

    @Test
    void caseResolvedNextHistoryIsClosedByBuAndNotHaveOwnerCanMigrationCase() {
        mockTempSla.mockTempStgSlaPerOwnerResolvedNextHistoryIsClosedByBuAndNotHaveOwner(stgSlaPerOwnerModels);
        ServiceTypeMatrixModel serviceTypeMatrixModel = mockSTM.mockSTMFixWithoutFCR();
        TeamReadonlyModel createdByTeam = mockTeam.mockTeam(tempCaseModel.getCreatedByTeamNew());
        EmployeeUserModel createByUser = mockEmployeeUser.mockEmployeeUser(tempCaseModel.getCreatedByEmployeeIdC());
        EmployeeUserModel naEmployee = mockEmployeeUser.mockEmployeeUser(Constant.NA_EMPLOYEE_ID);
        EmployeeUserModel systemEmployee = mockEmployeeUser.mockEmployeeUser(Constant.SYSTEM_EMPLOYEE_ID);

        EmployeeUserModel userSecondHop = mockEmployeeUser.mockEmployeeUser(stgSlaPerOwnerModels.getFirst());
        EmployeeUserModel userThirdHop = mockEmployeeUser.mockEmployeeUser(stgSlaPerOwnerModels.get(1));
        EmployeeUserModel userFourthHop = mockEmployeeUser.mockEmployeeUser(stgSlaPerOwnerModels.getLast());

        TeamReadonlyModel teamSecondHop = mockTeam.mockTeam(stgSlaPerOwnerModels.getFirst().getOwnerTeamNew());
        TeamReadonlyModel teamThirdHop = mockTeam.mockTeam(stgSlaPerOwnerModels.get(1).getOwnerTeamNew());
        TeamReadonlyModel teamFourthHop = mockTeam.mockTeam(stgSlaPerOwnerModels.getLast().getOwnerTeamNew());

       when(serviceTypeMatrixRepository.findByServiceTypeMatrixCode(Mockito.anyString()))
                .thenReturn(serviceTypeMatrixModel);

        when(teamReadonlyRepository.findByNameTh(Mockito.anyString()))
                .thenReturn(
                        Optional.ofNullable(createdByTeam),
                        Optional.ofNullable(teamSecondHop),
                        Optional.ofNullable(teamThirdHop),
                        Optional.ofNullable(teamFourthHop));

        when(employeeUserRepository.findByEmployeeId(Mockito.anyString()))
                .thenReturn(
                        Optional.ofNullable(createByUser),
                        Optional.ofNullable(naEmployee),
                        Optional.ofNullable(systemEmployee),
                        Optional.ofNullable(userSecondHop),
                        Optional.ofNullable(userThirdHop),
                        Optional.ofNullable(userFourthHop));

        when(employeeUserRepository.findTeamIdByEmployeeId(Mockito.anyString()))
                .thenReturn(
                        List.of(teamSecondHop.getTeamId().toString()),
                        List.of(teamThirdHop.getTeamId().toString()),
                        List.of(teamFourthHop.getTeamId().toString()));

        when(teamReadonlyRepository.findAllByTeamIdInAndStatusCode(Mockito.any(List.class), Mockito.anyInt()))
                .thenReturn(List.of(teamSecondHop))
                .thenReturn(List.of(teamThirdHop))
                .thenReturn(List.of(teamFourthHop));

        when(stgSlaPerOwnerRepository.findAllByCaseCOrderByStartDateTimeCAsc(tempCaseModel.getSfId())).thenReturn(stgSlaPerOwnerModels);

        when(caseRepository.save(Mockito.any(CaseTransactionModel.class))).thenAnswer(invocation -> {
            CaseTransactionModel input = invocation.getArgument(0);

            // simulate JPA @GeneratedValue
            input.setCaseId(UUID.randomUUID());

            return input;
        });

        when(caseSlaHopRepository.saveAll(Mockito.anyList()))
                .thenAnswer(invocation -> {
                    List<CaseSlaHopModel> inputList = invocation.getArgument(0);
                    for (CaseSlaHopModel item : inputList) {
                        item.setCaseSlaHopId(UUID.randomUUID());
                    }

                    return inputList;
                });

        when(caseSlaActivityRepository.saveAll(Mockito.anyList()))
                .thenAnswer(invocation -> {
                    List<CaseSlaActivity> inputList = invocation.getArgument(0);
                    for (CaseSlaActivity item : inputList) {
                        item.setCaseSlaActivityId(UUID.randomUUID());
                    }

                    return inputList;
                });

        when(kafkaTemplate.send(Mockito.any(Message.class)))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        StgToCaseWriterDTO transaction = createCaseService.createCase(tempCaseModel);

        CaseTransactionModel caseModel = transaction.getCaseTransaction();

        // Case
        assertEquals(tempCaseModel.getCaseNumber(), caseModel.getCaseNumber());
        assertEquals(createdByTeam.getTeamId(), caseModel.getCreatedByTeamId());
        assertEquals(createByUser.getUserId(), caseModel.getCreatedById());
        assertNotNull(caseModel.getModifiedById());
        ZonedDateTime createDate = DateTimeUtils.parseToZoneDateTime(tempCaseModel.getCreatedDate());
        assertEquals(createDate, caseModel.getCreatedOn());
        assertNotNull(caseModel.getModifiedOn());
        assertEquals(Constant.CASE_STATUS_RESOLVED, caseModel.getCaseStatusCode());
        assertEquals(Constant.TEP, caseModel.getIntegrationSystem());
        assertEquals(4, caseModel.getActiveHopNumber());

        // Case Resolved Info
        assertEquals(userThirdHop.getFullNameTH(), caseModel.getResolvedBy());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()), caseModel.getResolvedDate());
        assertEquals(userThirdHop.getUserId(), caseModel.getResolvedById());
        assertEquals(teamThirdHop.getTeamId(), caseModel.getResolvedByTeamId());

        // Case Closed Info
        assertNull(caseModel.getClosedBy());
        assertNull(caseModel.getClosedDate());
        assertNull(caseModel.getClosedById());
        assertNull(caseModel.getClosedByTeamId());

        // Service type matrix
        assertNotNull(serviceTypeMatrixModel);
        assertEquals(serviceTypeMatrixModel.getServiceTypeMatrixCode(), caseModel.getServiceTypeMatrixCode());
        assertEquals(serviceTypeMatrixModel.getServiceTypeMatrixType(), caseModel.getServiceTypeMatrixType());
        assertEquals(serviceTypeMatrixModel.getSla(), caseModel.getSla());
        assertEquals(serviceTypeMatrixModel.getFcr(), caseModel.getFcr());
        assertEquals(ServiceTypeMatrixTypeEnum.FIX, caseModel.getServiceTypeMatrixType());

        // Case Sla hop
        List<CaseSlaHopModel> caseSlas = caseModel.getSlaHop();
        assertNotNull(caseSlas);
        assertEquals(4, caseSlas.size());

        // First case sla hop
        CaseSlaHopModel firstHop = caseSlas.getFirst();
        assertNull(firstHop.getCaseSlaHopId());
        assertEquals(createDate, firstHop.getStartDatetime());
        assertEquals(createDate, firstHop.getEndDatetime());
        assertEquals(createByUser.getUserId(), firstHop.getOwnerId());
        assertEquals(createByUser.getFullNameTH(), firstHop.getOwnerName());
        assertEquals(createdByTeam.getTeamId(), firstHop.getTeamId());
        assertEquals(createdByTeam.getNameTh(), firstHop.getTeamName());
        assertEquals(createDate, firstHop.getCreatedOn());
        assertEquals(createDate, firstHop.getModifiedOn());
        assertEquals(createByUser.getUserId(), firstHop.getCreatedById());
        assertEquals(createByUser.getUserId(), firstHop.getModifiedById());
        assertEquals(1, firstHop.getHopNumber());
        assertEquals(0, firstHop.getTotalDuration());
        assertEquals(false, firstHop.getCloseByBu());
        assertNull(firstHop.getSlaTarget());
        assertNull(firstHop.getSlaTargetDate());
        assertEquals(0.0, firstHop.getTotalDuration(), 0.0000001);
        assertEquals(createdByTeam.getNameTh(), firstHop.getTeamName());
        assertEquals(createdByTeam.getTeamId(), firstHop.getTeamId());
        assertEquals(caseModel.getCaseId(), firstHop.getCases().getCaseId());

        // Second Case Sla hop
        CaseSlaHopModel secondHop = caseSlas.get(1);
        assertNull(secondHop.getCaseSlaHopId());
        assertNull(secondHop.getTotalDuration());
        assertNull(secondHop.getSlaTarget());
        assertNull(secondHop.getSlaTargetDate());
        assertNull(secondHop.getTotalDuration());
        assertNotNull(secondHop.getOwnerId());
        assertEquals(stgSlaPerOwnerModels.getFirst().getName(), secondHop.getOwnerName());
        assertEquals(teamSecondHop.getNameTh(), secondHop.getTeamName());
        assertEquals(teamSecondHop.getTeamId(), secondHop.getTeamId());
        assertEquals(2, secondHop.getHopNumber());
        assertEquals(false, secondHop.getCloseByBu());
        assertEquals(createByUser.getUserId(), secondHop.getCreatedById());
        assertEquals(userSecondHop.getUserId(), secondHop.getModifiedById());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getStartDateTimeC()), secondHop.getStartDatetime());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()), secondHop.getEndDatetime());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getStartDateTimeC()), secondHop.getCreatedOn());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()), secondHop.getModifiedOn());
        assertEquals(caseModel.getCaseId(), secondHop.getCases().getCaseId());

        // Third(last) Case sla hop
        CaseSlaHopModel thirdHop = caseSlas.get(2);
        assertNull(thirdHop.getCaseSlaHopId());
        assertNull(thirdHop.getSlaTarget());
        assertNull(thirdHop.getSlaTargetDate());
        assertNull(thirdHop.getTotalDuration());
        assertNotNull(thirdHop.getOwnerId());
        assertEquals(stgSlaPerOwnerModels.get(1).getName(), thirdHop.getOwnerName());
        assertEquals(teamThirdHop.getNameTh(), thirdHop.getTeamName());
        assertEquals(teamThirdHop.getTeamId(), thirdHop.getTeamId());
        assertEquals(userSecondHop.getUserId(), thirdHop.getCreatedById());
        assertEquals(userThirdHop.getUserId(), thirdHop.getModifiedById());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getStartDateTimeC()), thirdHop.getStartDatetime());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()), thirdHop.getEndDatetime());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getStartDateTimeC()), thirdHop.getCreatedOn());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()), thirdHop.getModifiedOn());
        assertEquals(3, thirdHop.getHopNumber());
        assertEquals(false, thirdHop.getCloseByBu());
        assertEquals(caseModel.getCaseId(), thirdHop.getCases().getCaseId());

        // Fourth(last) Case sla hop
        CaseSlaHopModel fourthHop = caseSlas.getLast();
        assertNull(fourthHop.getCaseSlaHopId());
        assertNull(fourthHop.getSlaTarget());
        assertNull(fourthHop.getSlaTargetDate());
        assertNull(fourthHop.getTotalDuration());
        assertNull(fourthHop.getOwnerId());
        assertNull(fourthHop.getOwnerName());
        assertEquals(teamFourthHop.getNameTh(), fourthHop.getTeamName());
        assertEquals(teamFourthHop.getTeamId(), fourthHop.getTeamId());
        assertEquals(userThirdHop.getUserId(), fourthHop.getCreatedById());
        assertEquals(userThirdHop.getUserId(), fourthHop.getModifiedById());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getLast().getStartDateTimeC()), fourthHop.getStartDatetime());
        assertNull(fourthHop.getEndDatetime());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getLast().getStartDateTimeC()), fourthHop.getCreatedOn());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getLast().getStartDateTimeC()), fourthHop.getModifiedOn());
        assertEquals(4, fourthHop.getHopNumber());
        assertEquals(true, fourthHop.getCloseByBu());
        assertEquals(caseModel.getCaseId(), fourthHop.getCases().getCaseId());

        // Case sla activities
        List<CaseSlaActivity> caseSlaActivities = transaction.getCaseTransaction().getCaseSlaActivities();
        assertNotNull(caseSlaActivities);
        assertEquals(10, caseSlaActivities.size());

        // First sla activity
        CaseSlaActivity firstActivity = caseSlaActivities.getFirst();
        assertNull(firstActivity.getCaseSlaActivityId());
        assertEquals(createByUser.getUserId(), firstActivity.getOwnerId());
        assertEquals(createByUser.getFullNameTH(), firstActivity.getOwnerName());
        assertEquals(createdByTeam.getTeamId(), firstActivity.getTeamId());
        assertEquals(createdByTeam.getNameTh(), firstActivity.getTeamNameTh());
        assertEquals(createByUser.getFullNameTH(), firstActivity.getCreatedByName());
        assertEquals(createByUser.getFullNameTH(), firstActivity.getModifiedByName());
        assertEquals(createByUser.getUserId(), firstActivity.getCreatedById());
        assertEquals(createByUser.getUserId(), firstActivity.getModifiedById());
        assertEquals(createDate, firstActivity.getStartDate());
        assertEquals(createDate, firstActivity.getEndDate());
        assertEquals(createDate, firstActivity.getCreatedOn());
        assertEquals(createDate, firstActivity.getModifiedOn());
        assertEquals(caseModel.getCaseNumber(), firstActivity.getCases().getCaseNumber());
        assertEquals(1, firstActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.CREATE, firstActivity.getAction());
        assertEquals(0.0, firstActivity.getActualDuration(), 0.0000001);

        // Second sla activity
        CaseSlaActivity secondActivity = caseSlaActivities.get(1);
        assertNull(secondActivity.getCaseSlaActivityId());
        assertEquals(createByUser.getUserId(), secondActivity.getOwnerId());
        assertEquals(createByUser.getFullNameTH(), secondActivity.getOwnerName());
        assertEquals(createdByTeam.getTeamId(), secondActivity.getTeamId());
        assertEquals(createdByTeam.getNameTh(), secondActivity.getTeamNameTh());
        assertEquals(createByUser.getFullNameTH(), secondActivity.getCreatedByName());
        assertEquals(createByUser.getFullNameTH(), secondActivity.getModifiedByName());
        assertEquals(createByUser.getUserId(), secondActivity.getCreatedById());
        assertEquals(createByUser.getUserId(), secondActivity.getModifiedById());
        assertEquals(createDate.plusNanos(1_000_000L), secondActivity.getStartDate());
        assertEquals(createDate.plusNanos(1_000_000L), secondActivity.getEndDate());
        assertEquals(createDate.plusNanos(1_000_000L), secondActivity.getCreatedOn());
        assertEquals(createDate.plusNanos(1_000_000L), secondActivity.getModifiedOn());
        assertNull(secondActivity.getActualDuration());
        assertEquals(caseModel.getCaseNumber(), secondActivity.getCases().getCaseNumber());
        assertEquals(1, secondActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.NEXT_HOP, secondActivity.getAction());

        // Third sla activity
        CaseSlaActivity thirdActivity = caseSlaActivities.get(2);
        assertNull(thirdActivity.getCaseSlaActivityId());
        assertNull(thirdActivity.getOwnerId());
        assertNull(thirdActivity.getOwnerName());
        assertEquals(teamSecondHop.getTeamId(), thirdActivity.getTeamId());
        assertEquals(teamSecondHop.getNameTh(), thirdActivity.getTeamNameTh());
        assertEquals(createByUser.getFullNameTH(), thirdActivity.getCreatedByName());
        assertEquals(userSecondHop.getFullNameTH(), thirdActivity.getModifiedByName());
        assertEquals(createByUser.getUserId(), thirdActivity.getCreatedById());
        assertEquals(userSecondHop.getUserId(), thirdActivity.getModifiedById());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getStartDateTimeC()).plusNanos(2_000_000L), thirdActivity.getStartDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()).plusNanos(2_000_000L), thirdActivity.getEndDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getStartDateTimeC()).plusNanos(2_000_000L), thirdActivity.getCreatedOn());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()).plusNanos(2_000_000L), thirdActivity.getModifiedOn());
        assertNotNull(thirdActivity.getActualDuration());
        assertEquals(caseModel.getCaseNumber(), thirdActivity.getCases().getCaseNumber());
        assertEquals(2, thirdActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.ASSIGN_TEAM, thirdActivity.getAction());

        // Fourth sla activity
        CaseSlaActivity fourthActivity = caseSlaActivities.get(3);
        assertNull(fourthActivity.getCaseSlaActivityId());
        assertEquals(userSecondHop.getUserId(), fourthActivity.getOwnerId());
        assertEquals(userSecondHop.getFullNameTH(), fourthActivity.getOwnerName());
        assertEquals(teamSecondHop.getTeamId(), fourthActivity.getTeamId());
        assertEquals(teamSecondHop.getNameTh(), fourthActivity.getTeamNameTh());
        assertEquals(userSecondHop.getFullNameTH(), fourthActivity.getCreatedByName());
        assertEquals(userSecondHop.getFullNameTH(), fourthActivity.getModifiedByName());
        assertEquals(userSecondHop.getUserId(), fourthActivity.getCreatedById());
        assertEquals(userSecondHop.getUserId(), fourthActivity.getModifiedById());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getStartDateTimeC()).plusNanos(3_000_000L), fourthActivity.getStartDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()).plusNanos(3_000_000L), fourthActivity.getEndDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getStartDateTimeC()).plusNanos(3_000_000L), fourthActivity.getCreatedOn());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()).plusNanos(3_000_000L), fourthActivity.getModifiedOn());
        assertNotNull(fourthActivity.getActualDuration());
        assertEquals(caseModel.getCaseNumber(), fourthActivity.getCases().getCaseNumber());
        assertEquals(2, fourthActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.ASSIGN_OWNER, fourthActivity.getAction());

        // Fifth sla activity
        CaseSlaActivity fifthActivity = caseSlaActivities.get(4);
        assertNull(fifthActivity.getCaseSlaActivityId());
        assertEquals(userSecondHop.getUserId(), fifthActivity.getOwnerId());
        assertEquals(userSecondHop.getFullNameTH(), fifthActivity.getOwnerName());
        assertEquals(teamSecondHop.getTeamId(), fifthActivity.getTeamId());
        assertEquals(teamSecondHop.getNameTh(), fifthActivity.getTeamNameTh());
        assertEquals(userSecondHop.getFullNameTH(), fifthActivity.getCreatedByName());
        assertEquals(userSecondHop.getFullNameTH(), fifthActivity.getModifiedByName());
        assertEquals(userSecondHop.getUserId(), fifthActivity.getCreatedById());
        assertEquals(userSecondHop.getUserId(), fifthActivity.getModifiedById());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()).plusNanos(4_000_000L), fifthActivity.getStartDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()).plusNanos(4_000_000L), fifthActivity.getEndDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()).plusNanos(4_000_000L), fifthActivity.getCreatedOn());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()).plusNanos(4_000_000L), fifthActivity.getModifiedOn());
        assertNull(fifthActivity.getActualDuration());
        assertEquals(caseModel.getCaseNumber(), fifthActivity.getCases().getCaseNumber());
        assertEquals(2, fifthActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.NEXT_HOP, fifthActivity.getAction());

        // Sixth sla activity
        CaseSlaActivity sixthActivity = caseSlaActivities.get(5);
        assertNull(sixthActivity.getCaseSlaActivityId());
        assertNull(sixthActivity.getOwnerId());
        assertNull(sixthActivity.getOwnerName());
        assertEquals(teamThirdHop.getTeamId(), sixthActivity.getTeamId());
        assertEquals(teamThirdHop.getNameTh(), sixthActivity.getTeamNameTh());
        assertEquals(userSecondHop.getFullNameTH(), sixthActivity.getCreatedByName());
        assertEquals(userThirdHop.getFullNameTH(), sixthActivity.getModifiedByName());
        assertEquals(userSecondHop.getUserId(), sixthActivity.getCreatedById());
        assertEquals(userThirdHop.getUserId(), sixthActivity.getModifiedById());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getStartDateTimeC()).plusNanos(5_000_000L), sixthActivity.getStartDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()).plusNanos(5_000_000L), sixthActivity.getEndDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getStartDateTimeC()).plusNanos(5_000_000L), sixthActivity.getCreatedOn());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()).plusNanos(5_000_000L), sixthActivity.getModifiedOn());
        assertEquals(0.625, sixthActivity.getActualDuration(), 0.0000001);
        assertEquals(caseModel.getCaseNumber(), sixthActivity.getCases().getCaseNumber());
        assertEquals(3, sixthActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.ASSIGN_TEAM, sixthActivity.getAction());

        // Seventh sla activity
        CaseSlaActivity seventhActivity = caseSlaActivities.get(6);
        assertNull(seventhActivity.getCaseSlaActivityId());
        assertEquals(userThirdHop.getUserId(), seventhActivity.getOwnerId());
        assertEquals(userThirdHop.getFullNameTH(), seventhActivity.getOwnerName());
        assertEquals(teamThirdHop.getTeamId(), seventhActivity.getTeamId());
        assertEquals(teamThirdHop.getNameTh(), seventhActivity.getTeamNameTh());
        assertEquals(userThirdHop.getFullNameTH(), seventhActivity.getCreatedByName());
        assertEquals(userThirdHop.getFullNameTH(), seventhActivity.getModifiedByName());
        assertEquals(userThirdHop.getUserId(), seventhActivity.getCreatedById());
        assertEquals(userThirdHop.getUserId(), seventhActivity.getModifiedById());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getStartDateTimeC()).plusNanos(6_000_000L), seventhActivity.getStartDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()).plusNanos(6_000_000L), seventhActivity.getEndDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getStartDateTimeC()).plusNanos(6_000_000L), seventhActivity.getCreatedOn());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()).plusNanos(6_000_000L), seventhActivity.getModifiedOn());
        assertEquals(0.625, seventhActivity.getActualDuration(), 0.0000001);
        assertEquals(caseModel.getCaseNumber(), seventhActivity.getCases().getCaseNumber());
        assertEquals(3, seventhActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.ASSIGN_OWNER, seventhActivity.getAction());

        // Eight sla activity
        CaseSlaActivity eightActivity = caseSlaActivities.get(7);
        assertNull(eightActivity.getCaseSlaActivityId());
        assertEquals(userThirdHop.getUserId(), eightActivity.getOwnerId());
        assertEquals(userThirdHop.getFullNameTH(), eightActivity.getOwnerName());
        assertEquals(teamThirdHop.getTeamId(), eightActivity.getTeamId());
        assertEquals(teamThirdHop.getNameTh(), eightActivity.getTeamNameTh());
        assertEquals(userThirdHop.getFullNameTH(), eightActivity.getCreatedByName());
        assertEquals(userThirdHop.getFullNameTH(), eightActivity.getModifiedByName());
        assertEquals(userThirdHop.getUserId(), eightActivity.getCreatedById());
        assertEquals(userThirdHop.getUserId(), eightActivity.getModifiedById());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()).plusNanos(7_000_000L), eightActivity.getStartDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()).plusNanos(7_000_000L), eightActivity.getEndDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()).plusNanos(7_000_000L), eightActivity.getCreatedOn());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()).plusNanos(7_000_000L), eightActivity.getModifiedOn());
        assertNull(eightActivity.getActualDuration());
        assertEquals(caseModel.getCaseNumber(), eightActivity.getCases().getCaseNumber());
        assertEquals(3, eightActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.RESOLVED, eightActivity.getAction());

        // Ninth sla activity
        CaseSlaActivity ninthActivity = caseSlaActivities.get(8);
        assertNull(ninthActivity.getCaseSlaActivityId());
        assertEquals(userThirdHop.getUserId(), ninthActivity.getOwnerId());
        assertEquals(userThirdHop.getFullNameTH(), ninthActivity.getOwnerName());
        assertEquals(teamThirdHop.getTeamId(), ninthActivity.getTeamId());
        assertEquals(teamThirdHop.getNameTh(), ninthActivity.getTeamNameTh());
        assertEquals(userThirdHop.getFullNameTH(), ninthActivity.getCreatedByName());
        assertEquals(userThirdHop.getFullNameTH(), ninthActivity.getModifiedByName());
        assertEquals(userThirdHop.getUserId(), ninthActivity.getCreatedById());
        assertEquals(userThirdHop.getUserId(), ninthActivity.getModifiedById());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()).plusNanos(8_000_000L), ninthActivity.getStartDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()).plusNanos(8_000_000L), ninthActivity.getEndDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()).plusNanos(8_000_000L), ninthActivity.getCreatedOn());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()).plusNanos(8_000_000L), ninthActivity.getModifiedOn());
        assertNull(ninthActivity.getActualDuration());
        assertEquals(caseModel.getCaseNumber(), ninthActivity.getCases().getCaseNumber());
        assertEquals(3, ninthActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.NEXT_HOP, ninthActivity.getAction());

        // Tenth sla activity
        CaseSlaActivity tenthActivity = caseSlaActivities.get(9);
        assertNull(tenthActivity.getCaseSlaActivityId());
        assertEquals(userThirdHop.getUserId(), ninthActivity.getOwnerId());
        assertEquals(userThirdHop.getFullNameTH(), ninthActivity.getOwnerName());
        assertEquals(teamFourthHop.getTeamId(), tenthActivity.getTeamId());
        assertEquals(teamFourthHop.getNameTh(), tenthActivity.getTeamNameTh());
        assertEquals(userThirdHop.getFullNameTH(), tenthActivity.getCreatedByName());
        assertEquals(userThirdHop.getFullNameTH(), tenthActivity.getModifiedByName());
        assertEquals(userThirdHop.getUserId(), tenthActivity.getCreatedById());
        assertEquals(userThirdHop.getUserId(), tenthActivity.getModifiedById());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getLast().getStartDateTimeC()).plusNanos(9_000_000L), tenthActivity.getStartDate());
        assertNull(tenthActivity.getEndDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getLast().getStartDateTimeC()).plusNanos(9_000_000L), tenthActivity.getCreatedOn());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getLast().getStartDateTimeC()).plusNanos(9_000_000L), tenthActivity.getModifiedOn());
        assertNull(tenthActivity.getActualDuration());
        assertEquals(caseModel.getCaseNumber(), tenthActivity.getCases().getCaseNumber());
        assertEquals(4, tenthActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.ASSIGN_TEAM, tenthActivity.getAction());

        // last modify data is same in case and last activity
        assertEquals(tenthActivity.getModifiedOn(), caseModel.getModifiedOn());
        assertEquals(tenthActivity.getModifiedById(), caseModel.getModifiedById());
    }

    @Test
    void caseResolvedNextHistoryNotIsClosedByBuCanMigrationCase() {
        mockTempSla.mockTempStgSlaPerOwnerResolvedNextHistoryNotIsClosedByBu(stgSlaPerOwnerModels);
        ServiceTypeMatrixModel serviceTypeMatrixModel = mockSTM.mockSTMFixWithoutFCR();
        TeamReadonlyModel createdByTeam = mockTeam.mockTeam(tempCaseModel.getCreatedByTeamNew());
        EmployeeUserModel createByUser = mockEmployeeUser.mockEmployeeUser(tempCaseModel.getCreatedByEmployeeIdC(), tempCaseModel.getCreatedNameC());
        EmployeeUserModel naEmployee = mockEmployeeUser.mockEmployeeUser(Constant.NA_EMPLOYEE_ID, Constant.NA_EMPLOYEE_FULL_NAME);
        EmployeeUserModel systemEmployee = mockEmployeeUser.mockEmployeeUser(Constant.SYSTEM_EMPLOYEE_ID);

        EmployeeUserModel userSecondHop = mockEmployeeUser.mockEmployeeUser(stgSlaPerOwnerModels.getFirst().getEmployeeIdC(), stgSlaPerOwnerModels.getFirst().getName());
        EmployeeUserModel userThirdHop = mockEmployeeUser.mockEmployeeUser(stgSlaPerOwnerModels.get(1).getEmployeeIdC(), stgSlaPerOwnerModels.get(1).getName());
        EmployeeUserModel userFourthHop = mockEmployeeUser.mockEmployeeUser(stgSlaPerOwnerModels.getLast().getEmployeeIdC(), stgSlaPerOwnerModels.getLast().getName());

        TeamReadonlyModel teamSecondHop = mockTeam.mockTeam(stgSlaPerOwnerModels.getFirst().getOwnerTeamNew());
        TeamReadonlyModel teamThirdHop = mockTeam.mockTeam(stgSlaPerOwnerModels.get(1).getOwnerTeamNew());
        TeamReadonlyModel teamFourthHop = mockTeam.mockTeam(stgSlaPerOwnerModels.getLast().getOwnerTeamNew());

       when(serviceTypeMatrixRepository.findByServiceTypeMatrixCode(Mockito.anyString()))
                .thenReturn(serviceTypeMatrixModel);

        when(teamReadonlyRepository.findByNameTh(Mockito.anyString()))
                .thenReturn(
                        Optional.ofNullable(createdByTeam),
                        Optional.ofNullable(teamSecondHop),
                        Optional.ofNullable(teamThirdHop),
                        Optional.ofNullable(teamFourthHop));

        when(employeeUserRepository.findByEmployeeId(Mockito.anyString()))
                .thenReturn(
                        Optional.ofNullable(createByUser),
                        Optional.ofNullable(naEmployee),
                        Optional.ofNullable(systemEmployee),
                        Optional.ofNullable(userSecondHop),
                        Optional.ofNullable(userThirdHop),
                        Optional.ofNullable(userFourthHop));

        when(employeeUserRepository.findTeamIdByEmployeeId(Mockito.anyString()))
                .thenReturn(
                        List.of(teamSecondHop.getTeamId().toString()),
                        List.of(teamThirdHop.getTeamId().toString()),
                        List.of(teamFourthHop.getTeamId().toString()));

        when(teamReadonlyRepository.findAllByTeamIdInAndStatusCode(Mockito.any(List.class), Mockito.anyInt()))
                .thenReturn(List.of(teamSecondHop))
                .thenReturn(List.of(teamThirdHop))
                .thenReturn(List.of(teamFourthHop));

        when(stgSlaPerOwnerRepository.findAllByCaseCOrderByStartDateTimeCAsc(tempCaseModel.getSfId())).thenReturn(stgSlaPerOwnerModels);

        when(caseRepository.save(Mockito.any(CaseTransactionModel.class))).thenAnswer(invocation -> {
            CaseTransactionModel input = invocation.getArgument(0);

            // simulate JPA @GeneratedValue
            input.setCaseId(UUID.randomUUID());

            return input;
        });

        when(caseSlaHopRepository.saveAll(Mockito.anyList()))
                .thenAnswer(invocation -> {
                    List<CaseSlaHopModel> inputList = invocation.getArgument(0);
                    for (CaseSlaHopModel item : inputList) {
                        item.setCaseSlaHopId(UUID.randomUUID());
                    }

                    return inputList;
                });

        when(caseSlaActivityRepository.saveAll(Mockito.anyList()))
                .thenAnswer(invocation -> {
                    List<CaseSlaActivity> inputList = invocation.getArgument(0);
                    for (CaseSlaActivity item : inputList) {
                        item.setCaseSlaActivityId(UUID.randomUUID());
                    }

                    return inputList;
                });

        when(kafkaTemplate.send(Mockito.any(Message.class)))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        StgToCaseWriterDTO transaction = createCaseService.createCase(tempCaseModel);

        CaseTransactionModel caseModel = transaction.getCaseTransaction();

        // Case
        assertEquals(tempCaseModel.getCaseNumber(), caseModel.getCaseNumber());
        assertEquals(createdByTeam.getTeamId(), caseModel.getCreatedByTeamId());
        assertEquals(createByUser.getUserId(), caseModel.getCreatedById());
        assertNotNull(caseModel.getModifiedById());
        ZonedDateTime createDate = DateTimeUtils.parseToZoneDateTime(tempCaseModel.getCreatedDate());
        assertEquals(createDate, caseModel.getCreatedOn());
        assertNotNull(caseModel.getModifiedOn());
        assertEquals(Constant.CASE_STATUS_RESOLVED, caseModel.getCaseStatusCode());
        assertEquals(Constant.TEP, caseModel.getIntegrationSystem());
        assertEquals(4, caseModel.getActiveHopNumber());

        // Case Resolved Info
        assertEquals(userThirdHop.getFullNameTH(), caseModel.getResolvedBy());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()), caseModel.getResolvedDate());
        assertEquals(userThirdHop.getUserId(), caseModel.getResolvedById());
        assertEquals(teamThirdHop.getTeamId(), caseModel.getResolvedByTeamId());

        // Case Closed Info
        assertNull(caseModel.getClosedBy());
        assertNull(caseModel.getClosedDate());
        assertNull(caseModel.getClosedById());
        assertNull(caseModel.getClosedByTeamId());

        // Service type matrix
        assertNotNull(serviceTypeMatrixModel);
        assertEquals(serviceTypeMatrixModel.getServiceTypeMatrixCode(), caseModel.getServiceTypeMatrixCode());
        assertEquals(serviceTypeMatrixModel.getServiceTypeMatrixType(), caseModel.getServiceTypeMatrixType());
        assertEquals(serviceTypeMatrixModel.getSla(), caseModel.getSla());
        assertEquals(serviceTypeMatrixModel.getFcr(), caseModel.getFcr());
        assertEquals(ServiceTypeMatrixTypeEnum.FIX, caseModel.getServiceTypeMatrixType());

        // Case Sla hop
        List<CaseSlaHopModel> caseSlas = caseModel.getSlaHop();
        assertNotNull(caseSlas);
        assertEquals(4, caseSlas.size());

        // First case sla hop
        CaseSlaHopModel firstHop = caseSlas.getFirst();
        assertNull(firstHop.getCaseSlaHopId());
        assertEquals(createDate, firstHop.getStartDatetime());
        assertEquals(createDate, firstHop.getEndDatetime());
        assertEquals(createByUser.getUserId(), firstHop.getOwnerId());
        assertEquals(createByUser.getFullNameTH(), firstHop.getOwnerName());
        assertEquals(createdByTeam.getTeamId(), firstHop.getTeamId());
        assertEquals(createdByTeam.getNameTh(), firstHop.getTeamName());
        assertEquals(createDate, firstHop.getCreatedOn());
        assertEquals(createDate, firstHop.getModifiedOn());
        assertEquals(createByUser.getUserId(), firstHop.getCreatedById());
        assertEquals(createByUser.getUserId(), firstHop.getModifiedById());
        assertEquals(1, firstHop.getHopNumber());
        assertEquals(0, firstHop.getTotalDuration());
        assertEquals(false, firstHop.getCloseByBu());
        assertNull(firstHop.getSlaTarget());
        assertNull(firstHop.getSlaTargetDate());
        assertEquals(0.0, firstHop.getTotalDuration(), 0.0000001);
        assertEquals(createdByTeam.getNameTh(), firstHop.getTeamName());
        assertEquals(createdByTeam.getTeamId(), firstHop.getTeamId());
        assertEquals(caseModel.getCaseId(), firstHop.getCases().getCaseId());

        // Second Case Sla hop
        CaseSlaHopModel secondHop = caseSlas.get(1);
        assertNull(secondHop.getCaseSlaHopId());
        assertNull(secondHop.getTotalDuration());
        assertNull(secondHop.getSlaTarget());
        assertNull(secondHop.getSlaTargetDate());
        assertNull(secondHop.getTotalDuration());
        assertNotNull(secondHop.getOwnerId());
        assertEquals(stgSlaPerOwnerModels.getFirst().getName(), secondHop.getOwnerName());
        assertEquals(teamSecondHop.getNameTh(), secondHop.getTeamName());
        assertEquals(teamSecondHop.getTeamId(), secondHop.getTeamId());
        assertEquals(2, secondHop.getHopNumber());
        assertEquals(false, secondHop.getCloseByBu());
        assertEquals(createByUser.getUserId(), secondHop.getCreatedById());
        assertEquals(userSecondHop.getUserId(), secondHop.getModifiedById());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getStartDateTimeC()), secondHop.getStartDatetime());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()), secondHop.getEndDatetime());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getStartDateTimeC()), secondHop.getCreatedOn());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()), secondHop.getModifiedOn());
        assertEquals(caseModel.getCaseId(), secondHop.getCases().getCaseId());

        // Third(last) Case sla hop
        CaseSlaHopModel thirdHop = caseSlas.get(2);
        assertNull(thirdHop.getCaseSlaHopId());
        assertNull(thirdHop.getSlaTarget());
        assertNull(thirdHop.getSlaTargetDate());
        assertNull(thirdHop.getTotalDuration());
        assertNotNull(thirdHop.getOwnerId());
        assertEquals(stgSlaPerOwnerModels.get(1).getName(), thirdHop.getOwnerName());
        assertEquals(teamThirdHop.getNameTh(), thirdHop.getTeamName());
        assertEquals(teamThirdHop.getTeamId(), thirdHop.getTeamId());
        assertEquals(userSecondHop.getUserId(), thirdHop.getCreatedById());
        assertEquals(userThirdHop.getUserId(), thirdHop.getModifiedById());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getStartDateTimeC()), thirdHop.getStartDatetime());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()), thirdHop.getEndDatetime());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getStartDateTimeC()), thirdHop.getCreatedOn());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()), thirdHop.getModifiedOn());
        assertEquals(3, thirdHop.getHopNumber());
        assertEquals(false, thirdHop.getCloseByBu());
        assertEquals(caseModel.getCaseId(), thirdHop.getCases().getCaseId());

        // Fourth(last) Case sla hop
        CaseSlaHopModel fourthHop = caseSlas.getLast();
        assertNull(fourthHop.getCaseSlaHopId());
        assertNull(fourthHop.getSlaTarget());
        assertNull(fourthHop.getSlaTargetDate());
        assertNull(fourthHop.getTotalDuration());
        assertNull(fourthHop.getOwnerId());
        assertNull(fourthHop.getOwnerName());
        assertEquals(teamFourthHop.getNameTh(), fourthHop.getTeamName());
        assertEquals(teamFourthHop.getTeamId(), fourthHop.getTeamId());
        assertEquals(userThirdHop.getUserId(), fourthHop.getCreatedById());
        assertEquals(userThirdHop.getUserId(), fourthHop.getModifiedById());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getLast().getStartDateTimeC()), fourthHop.getStartDatetime());
        assertNull(fourthHop.getEndDatetime());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getLast().getStartDateTimeC()), fourthHop.getCreatedOn());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getLast().getStartDateTimeC()), fourthHop.getModifiedOn());
        assertEquals(4, fourthHop.getHopNumber());
        assertEquals(true, fourthHop.getCloseByBu());
        assertEquals(caseModel.getCaseId(), fourthHop.getCases().getCaseId());

        // Case sla activities
        List<CaseSlaActivity> caseSlaActivities = transaction.getCaseTransaction().getCaseSlaActivities();
        assertNotNull(caseSlaActivities);
        assertEquals(11, caseSlaActivities.size());

        // First sla activity
        CaseSlaActivity firstActivity = caseSlaActivities.getFirst();
        assertNull(firstActivity.getCaseSlaActivityId());
        assertEquals(createByUser.getUserId(), firstActivity.getOwnerId());
        assertEquals(createByUser.getFullNameTH(), firstActivity.getOwnerName());
        assertEquals(createdByTeam.getTeamId(), firstActivity.getTeamId());
        assertEquals(createdByTeam.getNameTh(), firstActivity.getTeamNameTh());
        assertEquals(createByUser.getFullNameTH(), firstActivity.getCreatedByName());
        assertEquals(createByUser.getFullNameTH(), firstActivity.getModifiedByName());
        assertEquals(createByUser.getUserId(), firstActivity.getCreatedById());
        assertEquals(createByUser.getUserId(), firstActivity.getModifiedById());
        assertEquals(createDate, firstActivity.getStartDate());
        assertEquals(createDate, firstActivity.getEndDate());
        assertEquals(createDate, firstActivity.getCreatedOn());
        assertEquals(createDate, firstActivity.getModifiedOn());
        assertEquals(caseModel.getCaseNumber(), firstActivity.getCases().getCaseNumber());
        assertEquals(1, firstActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.CREATE, firstActivity.getAction());
        assertEquals(0.0, firstActivity.getActualDuration(), 0.0000001);

        // Second sla activity
        CaseSlaActivity secondActivity = caseSlaActivities.get(1);
        assertNull(secondActivity.getCaseSlaActivityId());
        assertEquals(createByUser.getUserId(), secondActivity.getOwnerId());
        assertEquals(createByUser.getFullNameTH(), secondActivity.getOwnerName());
        assertEquals(createdByTeam.getTeamId(), secondActivity.getTeamId());
        assertEquals(createdByTeam.getNameTh(), secondActivity.getTeamNameTh());
        assertEquals(createByUser.getFullNameTH(), secondActivity.getCreatedByName());
        assertEquals(createByUser.getFullNameTH(), secondActivity.getModifiedByName());
        assertEquals(createByUser.getUserId(), secondActivity.getCreatedById());
        assertEquals(createByUser.getUserId(), secondActivity.getModifiedById());
        assertEquals(createDate.plusNanos(1_000_000L), secondActivity.getStartDate());
        assertEquals(createDate.plusNanos(1_000_000L), secondActivity.getEndDate());
        assertEquals(createDate.plusNanos(1_000_000L), secondActivity.getCreatedOn());
        assertEquals(createDate.plusNanos(1_000_000L), secondActivity.getModifiedOn());
        assertNull(secondActivity.getActualDuration());
        assertEquals(caseModel.getCaseNumber(), secondActivity.getCases().getCaseNumber());
        assertEquals(1, secondActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.NEXT_HOP, secondActivity.getAction());

        // Third sla activity
        CaseSlaActivity thirdActivity = caseSlaActivities.get(2);
        assertNull(thirdActivity.getCaseSlaActivityId());
        assertNull(thirdActivity.getOwnerId());
        assertNull(thirdActivity.getOwnerName());
        assertEquals(teamSecondHop.getTeamId(), thirdActivity.getTeamId());
        assertEquals(teamSecondHop.getNameTh(), thirdActivity.getTeamNameTh());
        assertEquals(createByUser.getFullNameTH(), thirdActivity.getCreatedByName());
        assertEquals(userSecondHop.getFullNameTH(), thirdActivity.getModifiedByName());
        assertEquals(createByUser.getUserId(), thirdActivity.getCreatedById());
        assertEquals(userSecondHop.getUserId(), thirdActivity.getModifiedById());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getStartDateTimeC()).plusNanos(2_000_000L), thirdActivity.getStartDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()).plusNanos(2_000_000L), thirdActivity.getEndDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getStartDateTimeC()).plusNanos(2_000_000L), thirdActivity.getCreatedOn());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()).plusNanos(2_000_000L), thirdActivity.getModifiedOn());
        assertNotNull(thirdActivity.getActualDuration());
        assertEquals(caseModel.getCaseNumber(), thirdActivity.getCases().getCaseNumber());
        assertEquals(2, thirdActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.ASSIGN_TEAM, thirdActivity.getAction());

        // Fourth sla activity
        CaseSlaActivity fourthActivity = caseSlaActivities.get(3);
        assertNull(fourthActivity.getCaseSlaActivityId());
        assertEquals(userSecondHop.getUserId(), fourthActivity.getOwnerId());
        assertEquals(userSecondHop.getFullNameTH(), fourthActivity.getOwnerName());
        assertEquals(teamSecondHop.getTeamId(), fourthActivity.getTeamId());
        assertEquals(teamSecondHop.getNameTh(), fourthActivity.getTeamNameTh());
        assertEquals(userSecondHop.getFullNameTH(), fourthActivity.getCreatedByName());
        assertEquals(userSecondHop.getFullNameTH(), fourthActivity.getModifiedByName());
        assertEquals(userSecondHop.getUserId(), fourthActivity.getCreatedById());
        assertEquals(userSecondHop.getUserId(), fourthActivity.getModifiedById());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getStartDateTimeC()).plusNanos(3_000_000L), fourthActivity.getStartDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()).plusNanos(3_000_000L), fourthActivity.getEndDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getStartDateTimeC()).plusNanos(3_000_000L), fourthActivity.getCreatedOn());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()).plusNanos(3_000_000L), fourthActivity.getModifiedOn());
        assertNotNull(fourthActivity.getActualDuration());
        assertEquals(caseModel.getCaseNumber(), fourthActivity.getCases().getCaseNumber());
        assertEquals(2, fourthActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.ASSIGN_OWNER, fourthActivity.getAction());

        // Fifth sla activity
        CaseSlaActivity fifthActivity = caseSlaActivities.get(4);
        assertNull(fifthActivity.getCaseSlaActivityId());
        assertEquals(userSecondHop.getUserId(), fifthActivity.getOwnerId());
        assertEquals(userSecondHop.getFullNameTH(), fifthActivity.getOwnerName());
        assertEquals(teamSecondHop.getTeamId(), fifthActivity.getTeamId());
        assertEquals(teamSecondHop.getNameTh(), fifthActivity.getTeamNameTh());
        assertEquals(userSecondHop.getFullNameTH(), fifthActivity.getCreatedByName());
        assertEquals(userSecondHop.getFullNameTH(), fifthActivity.getModifiedByName());
        assertEquals(userSecondHop.getUserId(), fifthActivity.getCreatedById());
        assertEquals(userSecondHop.getUserId(), fifthActivity.getModifiedById());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()).plusNanos(4_000_000L), fifthActivity.getStartDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()).plusNanos(4_000_000L), fifthActivity.getEndDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()).plusNanos(4_000_000L), fifthActivity.getCreatedOn());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()).plusNanos(4_000_000L), fifthActivity.getModifiedOn());
        assertNull(fifthActivity.getActualDuration());
        assertEquals(caseModel.getCaseNumber(), fifthActivity.getCases().getCaseNumber());
        assertEquals(2, fifthActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.RESOLVED, fifthActivity.getAction());

        // Sixth sla activity
        CaseSlaActivity sixthActivity = caseSlaActivities.get(5);
        assertNull(sixthActivity.getCaseSlaActivityId());
        assertEquals(userSecondHop.getUserId(), sixthActivity.getOwnerId());
        assertEquals(userSecondHop.getFullNameTH(), sixthActivity.getOwnerName());
        assertEquals(teamSecondHop.getTeamId(), sixthActivity.getTeamId());
        assertEquals(teamSecondHop.getNameTh(), sixthActivity.getTeamNameTh());
        assertEquals(userSecondHop.getFullNameTH(), sixthActivity.getCreatedByName());
        assertEquals(userSecondHop.getFullNameTH(), sixthActivity.getModifiedByName());
        assertEquals(userSecondHop.getUserId(), sixthActivity.getCreatedById());
        assertEquals(userSecondHop.getUserId(), sixthActivity.getModifiedById());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()).plusNanos(5_000_000L), sixthActivity.getStartDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()).plusNanos(5_000_000L), sixthActivity.getEndDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()).plusNanos(5_000_000L), sixthActivity.getCreatedOn());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()).plusNanos(5_000_000L), sixthActivity.getModifiedOn());
        assertNull(sixthActivity.getActualDuration());
        assertEquals(caseModel.getCaseNumber(), sixthActivity.getCases().getCaseNumber());
        assertEquals(2, sixthActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.NEXT_HOP, sixthActivity.getAction());

        // Seventh sla activity
        CaseSlaActivity seventhActivity = caseSlaActivities.get(6);
        assertNull(seventhActivity.getCaseSlaActivityId());
        assertNull(seventhActivity.getOwnerId());
        assertNull(seventhActivity.getOwnerName());
        assertEquals(teamThirdHop.getTeamId(), seventhActivity.getTeamId());
        assertEquals(teamThirdHop.getNameTh(), seventhActivity.getTeamNameTh());
        assertEquals(userSecondHop.getFullNameTH(), seventhActivity.getCreatedByName());
        assertEquals(userThirdHop.getFullNameTH(), seventhActivity.getModifiedByName());
        assertEquals(userSecondHop.getUserId(), seventhActivity.getCreatedById());
        assertEquals(userThirdHop.getUserId(), seventhActivity.getModifiedById());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getStartDateTimeC()).plusNanos(6_000_000L), seventhActivity.getStartDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()).plusNanos(6_000_000L), seventhActivity.getEndDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getStartDateTimeC()).plusNanos(6_000_000L), seventhActivity.getCreatedOn());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()).plusNanos(6_000_000L), seventhActivity.getModifiedOn());
        assertEquals(0.625, seventhActivity.getActualDuration(), 0.0000001);
        assertEquals(caseModel.getCaseNumber(), seventhActivity.getCases().getCaseNumber());
        assertEquals(3, seventhActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.ASSIGN_TEAM, seventhActivity.getAction());

        // Eight sla activity
        CaseSlaActivity eightActivity = caseSlaActivities.get(7);
        assertNull(eightActivity.getCaseSlaActivityId());
        assertEquals(userThirdHop.getUserId(), eightActivity.getOwnerId());
        assertEquals(userThirdHop.getFullNameTH(), eightActivity.getOwnerName());
        assertEquals(teamThirdHop.getTeamId(), eightActivity.getTeamId());
        assertEquals(teamThirdHop.getNameTh(), eightActivity.getTeamNameTh());
        assertEquals(userThirdHop.getFullNameTH(), eightActivity.getCreatedByName());
        assertEquals(userThirdHop.getFullNameTH(), eightActivity.getModifiedByName());
        assertEquals(userThirdHop.getUserId(), eightActivity.getCreatedById());
        assertEquals(userThirdHop.getUserId(), eightActivity.getModifiedById());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getStartDateTimeC()).plusNanos(7_000_000L), eightActivity.getStartDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()).plusNanos(7_000_000L), eightActivity.getEndDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getStartDateTimeC()).plusNanos(7_000_000L), eightActivity.getCreatedOn());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()).plusNanos(7_000_000L), eightActivity.getModifiedOn());
        assertEquals(0.625, eightActivity.getActualDuration(), 0.0000001);
        assertEquals(caseModel.getCaseNumber(), eightActivity.getCases().getCaseNumber());
        assertEquals(3, eightActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.ASSIGN_OWNER, eightActivity.getAction());

        // Ninth sla activity
        CaseSlaActivity ninthActivity = caseSlaActivities.get(8);
        assertNull(ninthActivity.getCaseSlaActivityId());
        assertEquals(userThirdHop.getUserId(), ninthActivity.getOwnerId());
        assertEquals(userThirdHop.getFullNameTH(), ninthActivity.getOwnerName());
        assertEquals(teamThirdHop.getTeamId(), ninthActivity.getTeamId());
        assertEquals(teamThirdHop.getNameTh(), ninthActivity.getTeamNameTh());
        assertEquals(userThirdHop.getFullNameTH(), ninthActivity.getCreatedByName());
        assertEquals(userThirdHop.getFullNameTH(), ninthActivity.getModifiedByName());
        assertEquals(userThirdHop.getUserId(), ninthActivity.getCreatedById());
        assertEquals(userThirdHop.getUserId(), ninthActivity.getModifiedById());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()).plusNanos(8_000_000L), ninthActivity.getStartDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()).plusNanos(8_000_000L), ninthActivity.getEndDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()).plusNanos(8_000_000L), ninthActivity.getCreatedOn());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()).plusNanos(8_000_000L), ninthActivity.getModifiedOn());
        assertNull(ninthActivity.getActualDuration());
        assertEquals(caseModel.getCaseNumber(), ninthActivity.getCases().getCaseNumber());
        assertEquals(3, ninthActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.RESOLVED, ninthActivity.getAction());

        // Ninth sla activity
        CaseSlaActivity tenthActivity = caseSlaActivities.get(9);
        assertNull(tenthActivity.getCaseSlaActivityId());
        assertEquals(userThirdHop.getUserId(), tenthActivity.getOwnerId());
        assertEquals(userThirdHop.getFullNameTH(), tenthActivity.getOwnerName());
        assertEquals(teamThirdHop.getTeamId(), tenthActivity.getTeamId());
        assertEquals(teamThirdHop.getNameTh(), tenthActivity.getTeamNameTh());
        assertEquals(userThirdHop.getFullNameTH(), tenthActivity.getCreatedByName());
        assertEquals(userThirdHop.getFullNameTH(), tenthActivity.getModifiedByName());
        assertEquals(userThirdHop.getUserId(), tenthActivity.getCreatedById());
        assertEquals(userThirdHop.getUserId(), tenthActivity.getModifiedById());
        assertEquals(DateTimeUtils.getLocalDateTime().plusNanos(9_000_000L), tenthActivity.getStartDate());
        assertEquals(DateTimeUtils.getLocalDateTime().plusNanos(9_000_000L), tenthActivity.getEndDate());
        assertEquals(DateTimeUtils.getLocalDateTime().plusNanos(9_000_000L), tenthActivity.getCreatedOn());
        assertEquals(DateTimeUtils.getLocalDateTime().plusNanos(9_000_000L), tenthActivity.getModifiedOn());
        assertNull(tenthActivity.getActualDuration());
        assertEquals(caseModel.getCaseNumber(), tenthActivity.getCases().getCaseNumber());
        assertEquals(3, tenthActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.NEXT_HOP, tenthActivity.getAction());

        // EleventhActivity sla activity
        CaseSlaActivity eleventhActivity = caseSlaActivities.get(10);
        assertNull(eleventhActivity.getCaseSlaActivityId());
        assertEquals(userThirdHop.getUserId(), ninthActivity.getOwnerId());
        assertEquals(userThirdHop.getFullNameTH(), ninthActivity.getOwnerName());
        assertEquals(teamFourthHop.getTeamId(), eleventhActivity.getTeamId());
        assertEquals(teamFourthHop.getNameTh(), eleventhActivity.getTeamNameTh());
        assertEquals(userThirdHop.getFullNameTH(), eleventhActivity.getCreatedByName());
        assertEquals(userThirdHop.getFullNameTH(), eleventhActivity.getModifiedByName());
        assertEquals(userThirdHop.getUserId(), eleventhActivity.getCreatedById());
        assertEquals(userThirdHop.getUserId(), eleventhActivity.getModifiedById());
        assertEquals(DateTimeUtils.getLocalDateTime().plusNanos(10_000_000L), eleventhActivity.getStartDate());
        assertNull(eleventhActivity.getEndDate());
        assertEquals(DateTimeUtils.getLocalDateTime().plusNanos(10_000_000L), eleventhActivity.getCreatedOn());
        assertEquals(DateTimeUtils.getLocalDateTime().plusNanos(10_000_000L), eleventhActivity.getModifiedOn());
        assertNull(eleventhActivity.getActualDuration());
        assertEquals(caseModel.getCaseNumber(), eleventhActivity.getCases().getCaseNumber());
        assertEquals(4, eleventhActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.ASSIGN_TEAM, eleventhActivity.getAction());

        // last modify data is same in case and last activity
        assertEquals(eleventhActivity.getModifiedOn(), caseModel.getModifiedOn());
        assertEquals(eleventhActivity.getModifiedById(), caseModel.getModifiedById());
    }

    @Test
    void caseResolvedTtbWebAutoClosedAfterResolvedCanMigrationCase() {
        mockTempSla.mockTempStgSlaPerOwnerResolvedBu1NotHoveOwner(stgSlaPerOwnerModels);
        ServiceTypeMatrixModel serviceTypeMatrixModel = mockSTM.mockSTMFixAutoClosedAfterResolved();
        TeamReadonlyModel createdByTeam = mockTeam.mockTeam(tempCaseModel.getCreatedByTeamNew());
        EmployeeUserModel createByUser = mockEmployeeUser.mockEmployeeUser(tempCaseModel.getCreatedByEmployeeIdC(), tempCaseModel.getCreatedNameC());
        EmployeeUserModel naEmployee = mockEmployeeUser.mockEmployeeUser(Constant.NA_EMPLOYEE_ID, Constant.NA_EMPLOYEE_FULL_NAME);

        EmployeeUserModel userThirdHop = mockEmployeeUser.mockEmployeeUser(stgSlaPerOwnerModels.get(1).getEmployeeIdC(), stgSlaPerOwnerModels.get(1).getName());

        TeamReadonlyModel teamSecondHop = mockTeam.mockTeam(stgSlaPerOwnerModels.getFirst().getOwnerTeamNew());
        TeamReadonlyModel teamThirdHop = mockTeam.mockTeam(stgSlaPerOwnerModels.get(1).getOwnerTeamNew());

       when(serviceTypeMatrixRepository.findByServiceTypeMatrixCode(Mockito.anyString()))
                .thenReturn(serviceTypeMatrixModel);

        when(teamReadonlyRepository.findByNameTh(Mockito.anyString()))
                .thenReturn(
                        Optional.ofNullable(createdByTeam),
                        Optional.ofNullable(teamSecondHop),
                        Optional.ofNullable(teamThirdHop)
                );

        when(employeeUserRepository.findByEmployeeId(Mockito.anyString()))
                .thenReturn(
                        Optional.ofNullable(createByUser),
                        Optional.ofNullable(naEmployee),
                        Optional.ofNullable(userThirdHop)
                );

        when(employeeUserRepository.findTeamIdByEmployeeId(Mockito.anyString()))
                .thenReturn(
                        List.of(teamSecondHop.getTeamId().toString()),
                        List.of(teamThirdHop.getTeamId().toString()));

        when(teamReadonlyRepository.findAllByTeamIdInAndStatusCode(Mockito.any(List.class), Mockito.anyInt()))
                .thenReturn(List.of(teamSecondHop))
                .thenReturn(List.of(teamThirdHop));

        when(stgSlaPerOwnerRepository.findAllByCaseCOrderByStartDateTimeCAsc(tempCaseModel.getSfId())).thenReturn(stgSlaPerOwnerModels);

        when(caseRepository.save(Mockito.any(CaseTransactionModel.class))).thenAnswer(invocation -> {
            CaseTransactionModel input = invocation.getArgument(0);

            // simulate JPA @GeneratedValue
            input.setCaseId(UUID.randomUUID());

            return input;
        });

        when(caseSlaHopRepository.saveAll(Mockito.anyList()))
                .thenAnswer(invocation -> {
                    List<CaseSlaHopModel> inputList = invocation.getArgument(0);
                    for (CaseSlaHopModel item : inputList) {
                        item.setCaseSlaHopId(UUID.randomUUID());
                    }

                    return inputList;
                });

        when(caseSlaActivityRepository.saveAll(Mockito.anyList()))
                .thenAnswer(invocation -> {
                    List<CaseSlaActivity> inputList = invocation.getArgument(0);
                    for (CaseSlaActivity item : inputList) {
                        item.setCaseSlaActivityId(UUID.randomUUID());
                    }

                    return inputList;
                });

        when(kafkaTemplate.send(Mockito.any(Message.class)))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        StgToCaseWriterDTO transaction = createCaseService.createCase(tempCaseModel);

        CaseTransactionModel caseModel = transaction.getCaseTransaction();

        // Case
        assertEquals(tempCaseModel.getCaseNumber(), caseModel.getCaseNumber());
        assertEquals(createdByTeam.getTeamId(), caseModel.getCreatedByTeamId());
        assertEquals(createByUser.getUserId(), caseModel.getCreatedById());
        assertNotNull(caseModel.getModifiedById());
        ZonedDateTime createDate = DateTimeUtils.parseToZoneDateTime(tempCaseModel.getCreatedDate());
        assertEquals(createDate, caseModel.getCreatedOn());
        assertNotNull(caseModel.getModifiedOn());
        assertEquals(Constant.CASE_STATUS_COMPLETED, caseModel.getCaseStatusCode());
        assertEquals(Constant.TEP, caseModel.getIntegrationSystem());
        assertEquals(3, caseModel.getActiveHopNumber());
        assertEquals(1.0, caseModel.getTotalDuration(), 0.0000001);

        // Case Resolved Info
        assertEquals(stgSlaPerOwnerModels.get(1).getName(), caseModel.getResolvedBy());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()), caseModel.getResolvedDate());
        assertEquals(userThirdHop.getUserId(), caseModel.getResolvedById());
        assertEquals(teamThirdHop.getTeamId(), caseModel.getResolvedByTeamId());

        // Case Closed Info
        assertEquals(stgSlaPerOwnerModels.get(1).getName(), caseModel.getClosedBy());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()).plusNanos(8_000_000L), caseModel.getClosedDate());
        assertEquals(userThirdHop.getUserId(), caseModel.getClosedById());
        assertEquals(teamThirdHop.getTeamId(), caseModel.getClosedByTeamId());

        // Service type matrix
        assertNotNull(serviceTypeMatrixModel);
        assertEquals(serviceTypeMatrixModel.getServiceTypeMatrixCode(), caseModel.getServiceTypeMatrixCode());
        assertEquals(serviceTypeMatrixModel.getServiceTypeMatrixType(), caseModel.getServiceTypeMatrixType());
        assertEquals(serviceTypeMatrixModel.getSla(), caseModel.getSla());
        assertEquals(serviceTypeMatrixModel.getFcr(), caseModel.getFcr());
        assertEquals(ServiceTypeMatrixTypeEnum.FIX, caseModel.getServiceTypeMatrixType());

        // Case Sla hop
        List<CaseSlaHopModel> caseSlas = caseModel.getSlaHop();
        assertNotNull(caseSlas);
        assertEquals(3, caseSlas.size());

        // First case sla hop
        CaseSlaHopModel firstHop = caseSlas.getFirst();
        assertNull(firstHop.getCaseSlaHopId());
        assertEquals(createDate, firstHop.getStartDatetime());
        assertEquals(createDate, firstHop.getEndDatetime());
        assertEquals(createByUser.getUserId(), firstHop.getOwnerId());
        assertEquals(createByUser.getFullNameTH(), firstHop.getOwnerName());
        assertEquals(createdByTeam.getTeamId(), firstHop.getTeamId());
        assertEquals(createdByTeam.getNameTh(), firstHop.getTeamName());
        assertEquals(createDate, firstHop.getCreatedOn());
        assertEquals(createDate, firstHop.getModifiedOn());
        assertEquals(createByUser.getUserId(), firstHop.getCreatedById());
        assertEquals(createByUser.getUserId(), firstHop.getModifiedById());
        assertEquals(1, firstHop.getHopNumber());
        assertEquals(0, firstHop.getTotalDuration());
        assertEquals(false, firstHop.getCloseByBu());
        assertNull(firstHop.getSlaTarget());
        assertNull(firstHop.getSlaTargetDate());
        assertEquals(0.0, firstHop.getTotalDuration(), 0.0000001);
        assertEquals(createdByTeam.getNameTh(), firstHop.getTeamName());
        assertEquals(createdByTeam.getTeamId(), firstHop.getTeamId());
        assertEquals(caseModel.getCaseId(), firstHop.getCases().getCaseId());

        // Second Case Sla hop
        CaseSlaHopModel secondHop = caseSlas.get(1);
        assertNull(secondHop.getCaseSlaHopId());
        assertNull(secondHop.getTotalDuration());
        assertNull(secondHop.getSlaTarget());
        assertNull(secondHop.getSlaTargetDate());
        assertNull(secondHop.getTotalDuration());
        assertNull(secondHop.getOwnerId());
        assertNull(secondHop.getOwnerName());
        assertEquals(teamSecondHop.getNameTh(), secondHop.getTeamName());
        assertEquals(teamSecondHop.getTeamId(), secondHop.getTeamId());
        assertEquals(createByUser.getUserId(), secondHop.getCreatedById());
        assertEquals(naEmployee.getUserId(), secondHop.getModifiedById());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getStartDateTimeC()), secondHop.getStartDatetime());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()), secondHop.getEndDatetime());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getStartDateTimeC()), secondHop.getCreatedOn());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()), secondHop.getModifiedOn());
        assertEquals(2, secondHop.getHopNumber());
        assertEquals(false, secondHop.getCloseByBu());
        assertEquals(caseModel.getCaseId(), secondHop.getCases().getCaseId());

        // Third(last) Case sla hop
        CaseSlaHopModel thirdHop = caseSlas.get(2);
        assertNull(thirdHop.getCaseSlaHopId());
        assertNull(thirdHop.getSlaTarget());
        assertNull(thirdHop.getSlaTargetDate());
        assertNull(thirdHop.getTotalDuration());
        assertNotNull(thirdHop.getOwnerId());
        assertEquals(stgSlaPerOwnerModels.get(1).getName(), thirdHop.getOwnerName());
        assertEquals(teamThirdHop.getNameTh(), thirdHop.getTeamName());
        assertEquals(teamThirdHop.getTeamId(), thirdHop.getTeamId());
        assertEquals(naEmployee.getUserId(), thirdHop.getCreatedById());
        assertEquals(userThirdHop.getUserId(), thirdHop.getModifiedById());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getStartDateTimeC()), thirdHop.getStartDatetime());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()), thirdHop.getEndDatetime());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getStartDateTimeC()), thirdHop.getCreatedOn());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()), thirdHop.getModifiedOn());
        assertEquals(3, thirdHop.getHopNumber());
        assertEquals(false, thirdHop.getCloseByBu());
        assertEquals(caseModel.getCaseId(), thirdHop.getCases().getCaseId());


        // Case sla activities
        List<CaseSlaActivity> caseSlaActivities = transaction.getCaseTransaction().getCaseSlaActivities();
        assertNotNull(caseSlaActivities);
        assertEquals(9, caseSlaActivities.size());

        // First sla activity
        CaseSlaActivity firstActivity = caseSlaActivities.getFirst();
        assertNull(firstActivity.getCaseSlaActivityId());
        assertEquals(createByUser.getUserId(), firstActivity.getOwnerId());
        assertEquals(createByUser.getFullNameTH(), firstActivity.getOwnerName());
        assertEquals(createdByTeam.getTeamId(), firstActivity.getTeamId());
        assertEquals(createdByTeam.getNameTh(), firstActivity.getTeamNameTh());
        assertEquals(createByUser.getFullNameTH(), firstActivity.getCreatedByName());
        assertEquals(createByUser.getFullNameTH(), firstActivity.getModifiedByName());
        assertEquals(createByUser.getUserId(), firstActivity.getCreatedById());
        assertEquals(createByUser.getUserId(), firstActivity.getModifiedById());
        assertEquals(createDate, firstActivity.getStartDate());
        assertEquals(createDate, firstActivity.getEndDate());
        assertEquals(createDate, firstActivity.getCreatedOn());
        assertEquals(createDate, firstActivity.getModifiedOn());
        assertEquals(caseModel.getCaseNumber(), firstActivity.getCases().getCaseNumber());
        assertEquals(1, firstActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.CREATE, firstActivity.getAction());
        assertEquals(0.0, firstActivity.getActualDuration(), 0.0000001);

        // Second sla activity
        CaseSlaActivity secondActivity = caseSlaActivities.get(1);
        assertNull(secondActivity.getCaseSlaActivityId());
        assertEquals(createByUser.getUserId(), secondActivity.getOwnerId());
        assertEquals(createByUser.getFullNameTH(), secondActivity.getOwnerName());
        assertEquals(createdByTeam.getTeamId(), secondActivity.getTeamId());
        assertEquals(createdByTeam.getNameTh(), secondActivity.getTeamNameTh());
        assertEquals(createByUser.getFullNameTH(), secondActivity.getCreatedByName());
        assertEquals(createByUser.getFullNameTH(), secondActivity.getModifiedByName());
        assertEquals(createByUser.getUserId(), secondActivity.getCreatedById());
        assertEquals(createByUser.getUserId(), secondActivity.getModifiedById());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(0).getStartDateTimeC()).plusNanos(1_000_000L), secondActivity.getStartDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(0).getStartDateTimeC()).plusNanos(1_000_000L), secondActivity.getEndDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(0).getStartDateTimeC()).plusNanos(1_000_000L), secondActivity.getCreatedOn());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(0).getStartDateTimeC()).plusNanos(1_000_000L), secondActivity.getModifiedOn());
        assertNull(secondActivity.getActualDuration());
        assertEquals(caseModel.getCaseNumber(), secondActivity.getCases().getCaseNumber());
        assertEquals(1, secondActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.NEXT_HOP, secondActivity.getAction());

        // Third sla activity
        CaseSlaActivity thirdActivity = caseSlaActivities.get(2);
        assertNull(thirdActivity.getCaseSlaActivityId());
        assertNull(thirdActivity.getOwnerId());
        assertNull(thirdActivity.getOwnerName());
        assertEquals(teamSecondHop.getTeamId(), thirdActivity.getTeamId());
        assertEquals(teamSecondHop.getNameTh(), thirdActivity.getTeamNameTh());
        assertEquals(createByUser.getFullNameTH(), thirdActivity.getCreatedByName());
        assertEquals(naEmployee.getFullNameTH(), thirdActivity.getModifiedByName());
        assertEquals(createByUser.getUserId(), thirdActivity.getCreatedById());
        assertEquals(naEmployee.getUserId(), thirdActivity.getModifiedById());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getStartDateTimeC()).plusNanos(2_000_000L), thirdActivity.getStartDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()).plusNanos(2_000_000L), thirdActivity.getEndDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getStartDateTimeC()).plusNanos(2_000_000L), thirdActivity.getCreatedOn());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()).plusNanos(2_000_000L), thirdActivity.getModifiedOn());
        assertNotNull(thirdActivity.getActualDuration());
        assertEquals(caseModel.getCaseNumber(), thirdActivity.getCases().getCaseNumber());
        assertEquals(2, thirdActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.ASSIGN_TEAM, thirdActivity.getAction());

        // Fourth sla activity
        CaseSlaActivity fourthActivity = caseSlaActivities.get(3);
        assertNull(fourthActivity.getCaseSlaActivityId());
        assertNull(fourthActivity.getOwnerId());
        assertNull(fourthActivity.getOwnerName());
        assertEquals(teamSecondHop.getTeamId(), fourthActivity.getTeamId());
        assertEquals(teamSecondHop.getNameTh(), fourthActivity.getTeamNameTh());
        assertEquals(naEmployee.getFullNameTH(), fourthActivity.getCreatedByName());
        assertEquals(naEmployee.getFullNameTH(), fourthActivity.getModifiedByName());
        assertEquals(naEmployee.getUserId(), fourthActivity.getCreatedById());
        assertEquals(naEmployee.getUserId(), fourthActivity.getModifiedById());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()).plusNanos(3_000_000L), fourthActivity.getStartDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()).plusNanos(3_000_000L), fourthActivity.getEndDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()).plusNanos(3_000_000L), fourthActivity.getCreatedOn());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()).plusNanos(3_000_000L), fourthActivity.getModifiedOn());
        assertNull(fourthActivity.getActualDuration());
        assertEquals(caseModel.getCaseNumber(), fourthActivity.getCases().getCaseNumber());
        assertEquals(2, fourthActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.RESOLVED, fourthActivity.getAction());

        // Fifth sla activity
        CaseSlaActivity fifthActivity = caseSlaActivities.get(4);
        assertNull(fifthActivity.getCaseSlaActivityId());
        assertNull(fifthActivity.getOwnerId());
        assertNull(fifthActivity.getOwnerName());
        assertEquals(teamSecondHop.getTeamId(), fifthActivity.getTeamId());
        assertEquals(teamSecondHop.getNameTh(), fifthActivity.getTeamNameTh());
        assertEquals(naEmployee.getFullNameTH(), fifthActivity.getCreatedByName());
        assertEquals(naEmployee.getFullNameTH(), fifthActivity.getModifiedByName());
        assertEquals(naEmployee.getUserId(), fifthActivity.getCreatedById());
        assertEquals(naEmployee.getUserId(), fifthActivity.getModifiedById());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()).plusNanos(4_000_000L), fifthActivity.getStartDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()).plusNanos(4_000_000L), fifthActivity.getEndDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()).plusNanos(4_000_000L), fifthActivity.getCreatedOn());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()).plusNanos(4_000_000L), fifthActivity.getModifiedOn());
        assertNull(fifthActivity.getActualDuration());
        assertEquals(caseModel.getCaseNumber(), fifthActivity.getCases().getCaseNumber());
        assertEquals(2, fifthActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.NEXT_HOP, fifthActivity.getAction());

        // Sixth sla activity
        CaseSlaActivity sixthActivity = caseSlaActivities.get(5);
        assertNull(sixthActivity.getCaseSlaActivityId());
        assertNull(sixthActivity.getOwnerId());
        assertNull(sixthActivity.getOwnerName());
        assertEquals(teamThirdHop.getTeamId(), sixthActivity.getTeamId());
        assertEquals(teamThirdHop.getNameTh(), sixthActivity.getTeamNameTh());
        assertEquals(naEmployee.getFullNameTH(), sixthActivity.getCreatedByName());
        assertEquals(userThirdHop.getFullNameTH(), sixthActivity.getModifiedByName());
        assertEquals(naEmployee.getUserId(), sixthActivity.getCreatedById());
        assertEquals(userThirdHop.getUserId(), sixthActivity.getModifiedById());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getStartDateTimeC()).plusNanos(5_000_000L), sixthActivity.getStartDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()).plusNanos(5_000_000L), sixthActivity.getEndDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getStartDateTimeC()).plusNanos(5_000_000L), sixthActivity.getCreatedOn());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()).plusNanos(5_000_000L), sixthActivity.getModifiedOn());
        assertEquals(0.625, sixthActivity.getActualDuration(), 0.0000001);
        assertEquals(caseModel.getCaseNumber(), sixthActivity.getCases().getCaseNumber());
        assertEquals(3, sixthActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.ASSIGN_TEAM, sixthActivity.getAction());

        // Seventh sla activity
        CaseSlaActivity seventhActivity = caseSlaActivities.get(6);
        assertNull(seventhActivity.getCaseSlaActivityId());
        assertEquals(userThirdHop.getUserId(), seventhActivity.getOwnerId());
        assertEquals(userThirdHop.getFullNameTH(), seventhActivity.getOwnerName());
        assertEquals(teamThirdHop.getTeamId(), seventhActivity.getTeamId());
        assertEquals(teamThirdHop.getNameTh(), seventhActivity.getTeamNameTh());
        assertEquals(userThirdHop.getFullNameTH(), seventhActivity.getCreatedByName());
        assertEquals(userThirdHop.getFullNameTH(), seventhActivity.getModifiedByName());
        assertEquals(userThirdHop.getUserId(), seventhActivity.getCreatedById());
        assertEquals(userThirdHop.getUserId(), seventhActivity.getModifiedById());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getStartDateTimeC()).plusNanos(6_000_000L), seventhActivity.getStartDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()).plusNanos(6_000_000L), seventhActivity.getEndDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getStartDateTimeC()).plusNanos(6_000_000L), seventhActivity.getCreatedOn());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()).plusNanos(6_000_000L), seventhActivity.getModifiedOn());
        assertEquals(0.625, seventhActivity.getActualDuration(), 0.0000001);
        assertEquals(caseModel.getCaseNumber(), seventhActivity.getCases().getCaseNumber());
        assertEquals(3, seventhActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.ASSIGN_OWNER, seventhActivity.getAction());

        // Eight sla activity
        CaseSlaActivity eightActivity = caseSlaActivities.get(7);
        assertNull(eightActivity.getCaseSlaActivityId());
        assertEquals(userThirdHop.getUserId(), eightActivity.getOwnerId());
        assertEquals(userThirdHop.getFullNameTH(), eightActivity.getOwnerName());
        assertEquals(teamThirdHop.getTeamId(), eightActivity.getTeamId());
        assertEquals(teamThirdHop.getNameTh(), eightActivity.getTeamNameTh());
        assertEquals(userThirdHop.getFullNameTH(), eightActivity.getCreatedByName());
        assertEquals(userThirdHop.getFullNameTH(), eightActivity.getModifiedByName());
        assertEquals(userThirdHop.getUserId(), eightActivity.getCreatedById());
        assertEquals(userThirdHop.getUserId(), eightActivity.getModifiedById());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()).plusNanos(7_000_000L), eightActivity.getStartDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()).plusNanos(7_000_000L), eightActivity.getEndDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()).plusNanos(7_000_000L), eightActivity.getCreatedOn());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()).plusNanos(7_000_000L), eightActivity.getModifiedOn());
        assertNull(eightActivity.getActualDuration());
        assertEquals(caseModel.getCaseNumber(), eightActivity.getCases().getCaseNumber());
        assertEquals(3, eightActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.RESOLVED, eightActivity.getAction());

        // Ninth sla activity
        CaseSlaActivity ninthActivity = caseSlaActivities.get(8);
        assertNull(ninthActivity.getCaseSlaActivityId());
        assertEquals(userThirdHop.getUserId(), ninthActivity.getOwnerId());
        assertEquals(userThirdHop.getFullNameTH(), ninthActivity.getOwnerName());
        assertEquals(teamThirdHop.getTeamId(), ninthActivity.getTeamId());
        assertEquals(teamThirdHop.getNameTh(), ninthActivity.getTeamNameTh());
        assertEquals(userThirdHop.getFullNameTH(), ninthActivity.getCreatedByName());
        assertEquals(userThirdHop.getFullNameTH(), ninthActivity.getModifiedByName());
        assertEquals(userThirdHop.getUserId(), ninthActivity.getCreatedById());
        assertEquals(userThirdHop.getUserId(), ninthActivity.getModifiedById());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()).plusNanos(8_000_000L), ninthActivity.getStartDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()).plusNanos(8_000_000L), ninthActivity.getEndDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()).plusNanos(8_000_000L), ninthActivity.getCreatedOn());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()).plusNanos(8_000_000L), ninthActivity.getModifiedOn());
        assertNull(ninthActivity.getActualDuration());
        assertEquals(caseModel.getCaseNumber(), ninthActivity.getCases().getCaseNumber());
        assertEquals(3, ninthActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.AUTO_COMPLETED, ninthActivity.getAction());

        // last modify data is same in case and last activity
        assertEquals(ninthActivity.getModifiedOn(), caseModel.getModifiedOn());
        assertEquals(ninthActivity.getModifiedById(), caseModel.getModifiedById());
    }

    @Test
    void caseResolvedOwnerNotInTeamBu1CanMigrationCase() {
        mockTempSla.mockTempStgSlaPerOwnerResolvedNextHistoryNotIsClosedByBu(stgSlaPerOwnerModels);
        ServiceTypeMatrixModel serviceTypeMatrixModel = mockSTM.mockSTMFixWithoutFCR();
        TeamReadonlyModel createdByTeam = mockTeam.mockTeam(tempCaseModel.getCreatedByTeamNew());
        EmployeeUserModel createByUser = mockEmployeeUser.mockEmployeeUser(tempCaseModel.getCreatedByEmployeeIdC(), tempCaseModel.getCreatedNameC());
        EmployeeUserModel naEmployee = mockEmployeeUser.mockEmployeeUser(Constant.NA_EMPLOYEE_ID, Constant.NA_EMPLOYEE_FULL_NAME);
        EmployeeUserModel systemEmployee = mockEmployeeUser.mockEmployeeUser(Constant.SYSTEM_EMPLOYEE_ID);

        EmployeeUserModel userSecondHop = mockEmployeeUser.mockEmployeeUser(stgSlaPerOwnerModels.getFirst().getEmployeeIdC(), stgSlaPerOwnerModels.getFirst().getName());
        EmployeeUserModel userThirdHop = mockEmployeeUser.mockEmployeeUser(stgSlaPerOwnerModels.get(1).getEmployeeIdC(), stgSlaPerOwnerModels.get(1).getName());
        EmployeeUserModel userFourthHop = mockEmployeeUser.mockEmployeeUser(stgSlaPerOwnerModels.getLast().getEmployeeIdC(), stgSlaPerOwnerModels.getLast().getName());

        TeamReadonlyModel teamSecondHop = mockTeam.mockTeam(stgSlaPerOwnerModels.getFirst().getOwnerTeamNew());
        TeamReadonlyModel teamThirdHop = mockTeam.mockTeam(stgSlaPerOwnerModels.get(1).getOwnerTeamNew());
        TeamReadonlyModel teamFourthHop = mockTeam.mockTeam(stgSlaPerOwnerModels.getLast().getOwnerTeamNew());

       when(serviceTypeMatrixRepository.findByServiceTypeMatrixCode(Mockito.anyString()))
                .thenReturn(serviceTypeMatrixModel);

        when(teamReadonlyRepository.findByNameTh(Mockito.anyString()))
                .thenReturn(
                        Optional.ofNullable(createdByTeam),
                        Optional.ofNullable(teamSecondHop),
                        Optional.ofNullable(teamThirdHop),
                        Optional.ofNullable(teamFourthHop));

        when(employeeUserRepository.findByEmployeeId(Mockito.anyString()))
                .thenReturn(
                        Optional.ofNullable(createByUser),
                        Optional.ofNullable(naEmployee),
                        Optional.ofNullable(systemEmployee),
                        Optional.ofNullable(userThirdHop),
                        Optional.ofNullable(userFourthHop));

        when(employeeUserRepository.findTeamIdByEmployeeId(Mockito.anyString()))
                .thenReturn(
                        List.of(teamThirdHop.getTeamId().toString()),
                        List.of(teamThirdHop.getTeamId().toString()),
                        List.of(teamFourthHop.getTeamId().toString()));

        when(teamReadonlyRepository.findAllByTeamIdInAndStatusCode(Mockito.any(List.class), Mockito.anyInt()))
                .thenReturn(List.of(teamThirdHop))
                .thenReturn(List.of(teamThirdHop))
                .thenReturn(List.of(teamFourthHop));

        when(stgSlaPerOwnerRepository.findAllByCaseCOrderByStartDateTimeCAsc(tempCaseModel.getSfId())).thenReturn(stgSlaPerOwnerModels);

        when(caseRepository.save(Mockito.any(CaseTransactionModel.class))).thenAnswer(invocation -> {
            CaseTransactionModel input = invocation.getArgument(0);

            // simulate JPA @GeneratedValue
            input.setCaseId(UUID.randomUUID());

            return input;
        });

        when(caseSlaHopRepository.saveAll(Mockito.anyList()))
                .thenAnswer(invocation -> {
                    List<CaseSlaHopModel> inputList = invocation.getArgument(0);
                    for (CaseSlaHopModel item : inputList) {
                        item.setCaseSlaHopId(UUID.randomUUID());
                    }

                    return inputList;
                });

        when(caseSlaActivityRepository.saveAll(Mockito.anyList()))
                .thenAnswer(invocation -> {
                    List<CaseSlaActivity> inputList = invocation.getArgument(0);
                    for (CaseSlaActivity item : inputList) {
                        item.setCaseSlaActivityId(UUID.randomUUID());
                    }

                    return inputList;
                });

        when(kafkaTemplate.send(Mockito.any(Message.class)))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        StgToCaseWriterDTO transaction = createCaseService.createCase(tempCaseModel);

        CaseTransactionModel caseModel = transaction.getCaseTransaction();

        // Case
        assertEquals(tempCaseModel.getCaseNumber(), caseModel.getCaseNumber());
        assertEquals(createdByTeam.getTeamId(), caseModel.getCreatedByTeamId());
        assertEquals(createByUser.getUserId(), caseModel.getCreatedById());
        assertNotNull(caseModel.getModifiedById());
        ZonedDateTime createDate = DateTimeUtils.parseToZoneDateTime(tempCaseModel.getCreatedDate());
        assertEquals(createDate, caseModel.getCreatedOn());
        assertNotNull(caseModel.getModifiedOn());
        assertEquals(Constant.CASE_STATUS_RESOLVED, caseModel.getCaseStatusCode());
        assertEquals(Constant.TEP, caseModel.getIntegrationSystem());
        assertEquals(4, caseModel.getActiveHopNumber());

        // Case Resolved Info
        assertEquals(userThirdHop.getFullNameTH(), caseModel.getResolvedBy());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()), caseModel.getResolvedDate());
        assertEquals(userThirdHop.getUserId(), caseModel.getResolvedById());
        assertEquals(teamThirdHop.getTeamId(), caseModel.getResolvedByTeamId());

        // Case Closed Info
        assertNull(caseModel.getClosedBy());
        assertNull(caseModel.getClosedDate());
        assertNull(caseModel.getClosedById());
        assertNull(caseModel.getClosedByTeamId());

        // Service type matrix
        assertNotNull(serviceTypeMatrixModel);
        assertEquals(serviceTypeMatrixModel.getServiceTypeMatrixCode(), caseModel.getServiceTypeMatrixCode());
        assertEquals(serviceTypeMatrixModel.getServiceTypeMatrixType(), caseModel.getServiceTypeMatrixType());
        assertEquals(serviceTypeMatrixModel.getSla(), caseModel.getSla());
        assertEquals(serviceTypeMatrixModel.getFcr(), caseModel.getFcr());
        assertEquals(ServiceTypeMatrixTypeEnum.FIX, caseModel.getServiceTypeMatrixType());

        // Case Sla hop
        List<CaseSlaHopModel> caseSlas = caseModel.getSlaHop();
        assertNotNull(caseSlas);
        assertEquals(4, caseSlas.size());

        // First case sla hop
        CaseSlaHopModel firstHop = caseSlas.getFirst();
        assertNull(firstHop.getCaseSlaHopId());
        assertEquals(createDate, firstHop.getStartDatetime());
        assertEquals(createDate, firstHop.getEndDatetime());
        assertEquals(createByUser.getUserId(), firstHop.getOwnerId());
        assertEquals(createByUser.getFullNameTH(), firstHop.getOwnerName());
        assertEquals(createdByTeam.getTeamId(), firstHop.getTeamId());
        assertEquals(createdByTeam.getNameTh(), firstHop.getTeamName());
        assertEquals(createDate, firstHop.getCreatedOn());
        assertEquals(createDate, firstHop.getModifiedOn());
        assertEquals(createByUser.getUserId(), firstHop.getCreatedById());
        assertEquals(createByUser.getUserId(), firstHop.getModifiedById());
        assertEquals(1, firstHop.getHopNumber());
        assertEquals(0, firstHop.getTotalDuration());
        assertEquals(false, firstHop.getCloseByBu());
        assertNull(firstHop.getSlaTarget());
        assertNull(firstHop.getSlaTargetDate());
        assertEquals(0.0, firstHop.getTotalDuration(), 0.0000001);
        assertEquals(createdByTeam.getNameTh(), firstHop.getTeamName());
        assertEquals(createdByTeam.getTeamId(), firstHop.getTeamId());
        assertEquals(caseModel.getCaseId(), firstHop.getCases().getCaseId());

        // Second Case Sla hop
        CaseSlaHopModel secondHop = caseSlas.get(1);
        assertNull(secondHop.getCaseSlaHopId());
        assertNull(secondHop.getTotalDuration());
        assertNull(secondHop.getSlaTarget());
        assertNull(secondHop.getSlaTargetDate());
        assertNull(secondHop.getTotalDuration());
        assertNull(secondHop.getOwnerId());
        assertNull(secondHop.getOwnerName());
        assertEquals(teamSecondHop.getNameTh(), secondHop.getTeamName());
        assertEquals(teamSecondHop.getTeamId(), secondHop.getTeamId());
        assertEquals(2, secondHop.getHopNumber());
        assertEquals(false, secondHop.getCloseByBu());
        assertEquals(createByUser.getUserId(), secondHop.getCreatedById());
        assertEquals(naEmployee.getUserId(), secondHop.getModifiedById());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getStartDateTimeC()), secondHop.getStartDatetime());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()), secondHop.getEndDatetime());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getStartDateTimeC()), secondHop.getCreatedOn());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()), secondHop.getModifiedOn());
        assertEquals(caseModel.getCaseId(), secondHop.getCases().getCaseId());

        // Third(last) Case sla hop
        CaseSlaHopModel thirdHop = caseSlas.get(2);
        assertNull(thirdHop.getCaseSlaHopId());
        assertNull(thirdHop.getSlaTarget());
        assertNull(thirdHop.getSlaTargetDate());
        assertNull(thirdHop.getTotalDuration());
        assertNotNull(thirdHop.getOwnerId());
        assertEquals(stgSlaPerOwnerModels.get(1).getName(), thirdHop.getOwnerName());
        assertEquals(teamThirdHop.getNameTh(), thirdHop.getTeamName());
        assertEquals(teamThirdHop.getTeamId(), thirdHop.getTeamId());
        assertEquals(naEmployee.getUserId(), thirdHop.getCreatedById());
        assertEquals(userThirdHop.getUserId(), thirdHop.getModifiedById());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getStartDateTimeC()), thirdHop.getStartDatetime());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()), thirdHop.getEndDatetime());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getStartDateTimeC()), thirdHop.getCreatedOn());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()), thirdHop.getModifiedOn());
        assertEquals(3, thirdHop.getHopNumber());
        assertEquals(false, thirdHop.getCloseByBu());
        assertEquals(caseModel.getCaseId(), thirdHop.getCases().getCaseId());

        // Fourth(last) Case sla hop
        CaseSlaHopModel fourthHop = caseSlas.getLast();
        assertNull(fourthHop.getCaseSlaHopId());
        assertNull(fourthHop.getSlaTarget());
        assertNull(fourthHop.getSlaTargetDate());
        assertNull(fourthHop.getTotalDuration());
        assertNull(fourthHop.getOwnerId());
        assertNull(fourthHop.getOwnerName());
        assertEquals(teamFourthHop.getNameTh(), fourthHop.getTeamName());
        assertEquals(teamFourthHop.getTeamId(), fourthHop.getTeamId());
        assertEquals(userThirdHop.getUserId(), fourthHop.getCreatedById());
        assertEquals(userThirdHop.getUserId(), fourthHop.getModifiedById());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getLast().getStartDateTimeC()), fourthHop.getStartDatetime());
        assertNull(fourthHop.getEndDatetime());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getLast().getStartDateTimeC()), fourthHop.getCreatedOn());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getLast().getStartDateTimeC()), fourthHop.getModifiedOn());
        assertEquals(4, fourthHop.getHopNumber());
        assertEquals(true, fourthHop.getCloseByBu());
        assertEquals(caseModel.getCaseId(), fourthHop.getCases().getCaseId());

        // Case sla activities
        List<CaseSlaActivity> caseSlaActivities = transaction.getCaseTransaction().getCaseSlaActivities();
        assertNotNull(caseSlaActivities);
        assertEquals(10, caseSlaActivities.size());

        // First sla activity
        CaseSlaActivity firstActivity = caseSlaActivities.getFirst();
        assertNull(firstActivity.getCaseSlaActivityId());
        assertEquals(createByUser.getUserId(), firstActivity.getOwnerId());
        assertEquals(createByUser.getFullNameTH(), firstActivity.getOwnerName());
        assertEquals(createdByTeam.getTeamId(), firstActivity.getTeamId());
        assertEquals(createdByTeam.getNameTh(), firstActivity.getTeamNameTh());
        assertEquals(createByUser.getFullNameTH(), firstActivity.getCreatedByName());
        assertEquals(createByUser.getFullNameTH(), firstActivity.getModifiedByName());
        assertEquals(createByUser.getUserId(), firstActivity.getCreatedById());
        assertEquals(createByUser.getUserId(), firstActivity.getModifiedById());
        assertEquals(createDate, firstActivity.getStartDate());
        assertEquals(createDate, firstActivity.getEndDate());
        assertEquals(createDate, firstActivity.getCreatedOn());
        assertEquals(createDate, firstActivity.getModifiedOn());
        assertEquals(caseModel.getCaseNumber(), firstActivity.getCases().getCaseNumber());
        assertEquals(1, firstActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.CREATE, firstActivity.getAction());
        assertEquals(0.0, firstActivity.getActualDuration(), 0.0000001);

        // Second sla activity
        CaseSlaActivity secondActivity = caseSlaActivities.get(1);
        assertNull(secondActivity.getCaseSlaActivityId());
        assertEquals(createByUser.getUserId(), secondActivity.getOwnerId());
        assertEquals(createByUser.getFullNameTH(), secondActivity.getOwnerName());
        assertEquals(createdByTeam.getTeamId(), secondActivity.getTeamId());
        assertEquals(createdByTeam.getNameTh(), secondActivity.getTeamNameTh());
        assertEquals(createByUser.getFullNameTH(), secondActivity.getCreatedByName());
        assertEquals(createByUser.getFullNameTH(), secondActivity.getModifiedByName());
        assertEquals(createByUser.getUserId(), secondActivity.getCreatedById());
        assertEquals(createByUser.getUserId(), secondActivity.getModifiedById());
        assertEquals(createDate.plusNanos(1_000_000L), secondActivity.getStartDate());
        assertEquals(createDate.plusNanos(1_000_000L), secondActivity.getEndDate());
        assertEquals(createDate.plusNanos(1_000_000L), secondActivity.getCreatedOn());
        assertEquals(createDate.plusNanos(1_000_000L), secondActivity.getModifiedOn());
        assertNull(secondActivity.getActualDuration());
        assertEquals(caseModel.getCaseNumber(), secondActivity.getCases().getCaseNumber());
        assertEquals(1, secondActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.NEXT_HOP, secondActivity.getAction());

        // Third sla activity
        CaseSlaActivity thirdActivity = caseSlaActivities.get(2);
        assertNull(thirdActivity.getCaseSlaActivityId());
        assertNull(thirdActivity.getOwnerId());
        assertNull(thirdActivity.getOwnerName());
        assertEquals(teamSecondHop.getTeamId(), thirdActivity.getTeamId());
        assertEquals(teamSecondHop.getNameTh(), thirdActivity.getTeamNameTh());
        assertEquals(createByUser.getFullNameTH(), thirdActivity.getCreatedByName());
        assertEquals(naEmployee.getFullNameTH(), thirdActivity.getModifiedByName());
        assertEquals(createByUser.getUserId(), thirdActivity.getCreatedById());
        assertEquals(naEmployee.getUserId(), thirdActivity.getModifiedById());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getStartDateTimeC()).plusNanos(2_000_000L), thirdActivity.getStartDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()).plusNanos(2_000_000L), thirdActivity.getEndDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getStartDateTimeC()).plusNanos(2_000_000L), thirdActivity.getCreatedOn());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()).plusNanos(2_000_000L), thirdActivity.getModifiedOn());
        assertNotNull(thirdActivity.getActualDuration());
        assertEquals(caseModel.getCaseNumber(), thirdActivity.getCases().getCaseNumber());
        assertEquals(2, thirdActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.ASSIGN_TEAM, thirdActivity.getAction());

        // Fourth sla activity
        CaseSlaActivity fourthActivity = caseSlaActivities.get(3);
        assertNull(fourthActivity.getCaseSlaActivityId());
        assertNull(fourthActivity.getOwnerId());
        assertNull(fourthActivity.getOwnerName());
        assertEquals(teamSecondHop.getTeamId(), fourthActivity.getTeamId());
        assertEquals(teamSecondHop.getNameTh(), fourthActivity.getTeamNameTh());
        assertEquals(naEmployee.getFullNameTH(), fourthActivity.getCreatedByName());
        assertEquals(naEmployee.getFullNameTH(), fourthActivity.getModifiedByName());
        assertEquals(naEmployee.getUserId(), fourthActivity.getCreatedById());
        assertEquals(naEmployee.getUserId(), fourthActivity.getModifiedById());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()).plusNanos(3_000_000L), fourthActivity.getStartDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()).plusNanos(3_000_000L), fourthActivity.getEndDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()).plusNanos(3_000_000L), fourthActivity.getCreatedOn());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()).plusNanos(3_000_000L), fourthActivity.getModifiedOn());
        assertNull(fourthActivity.getActualDuration());
        assertEquals(caseModel.getCaseNumber(), fourthActivity.getCases().getCaseNumber());
        assertEquals(2, fourthActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.RESOLVED, fourthActivity.getAction());

        // Fifth sla activity
        CaseSlaActivity fifthActivity = caseSlaActivities.get(4);
        assertNull(fifthActivity.getCaseSlaActivityId());
        assertNull(fifthActivity.getOwnerId());
        assertNull(fifthActivity.getOwnerName());
        assertEquals(teamSecondHop.getTeamId(), fifthActivity.getTeamId());
        assertEquals(teamSecondHop.getNameTh(), fifthActivity.getTeamNameTh());
        assertEquals(naEmployee.getFullNameTH(), fifthActivity.getCreatedByName());
        assertEquals(naEmployee.getFullNameTH(), fifthActivity.getModifiedByName());
        assertEquals(naEmployee.getUserId(), fifthActivity.getCreatedById());
        assertEquals(naEmployee.getUserId(), fifthActivity.getModifiedById());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()).plusNanos(4_000_000L), fifthActivity.getStartDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()).plusNanos(4_000_000L), fifthActivity.getEndDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()).plusNanos(4_000_000L), fifthActivity.getCreatedOn());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()).plusNanos(4_000_000L), fifthActivity.getModifiedOn());
        assertNull(fifthActivity.getActualDuration());
        assertEquals(caseModel.getCaseNumber(), fifthActivity.getCases().getCaseNumber());
        assertEquals(2, fifthActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.NEXT_HOP, fifthActivity.getAction());

        // Sixth sla activity
        CaseSlaActivity sixthActivity = caseSlaActivities.get(5);
        assertNull(sixthActivity.getCaseSlaActivityId());
        assertNull(sixthActivity.getOwnerId());
        assertNull(sixthActivity.getOwnerName());
        assertEquals(teamThirdHop.getTeamId(), sixthActivity.getTeamId());
        assertEquals(teamThirdHop.getNameTh(), sixthActivity.getTeamNameTh());
        assertEquals(naEmployee.getFullNameTH(), sixthActivity.getCreatedByName());
        assertEquals(userThirdHop.getFullNameTH(), sixthActivity.getModifiedByName());
        assertEquals(naEmployee.getUserId(), sixthActivity.getCreatedById());
        assertEquals(userThirdHop.getUserId(), sixthActivity.getModifiedById());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getStartDateTimeC()).plusNanos(5_000_000L), sixthActivity.getStartDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()).plusNanos(5_000_000L), sixthActivity.getEndDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getStartDateTimeC()).plusNanos(5_000_000L), sixthActivity.getCreatedOn());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()).plusNanos(5_000_000L), sixthActivity.getModifiedOn());
        assertEquals(0.625, sixthActivity.getActualDuration(), 0.0000001);
        assertEquals(caseModel.getCaseNumber(), sixthActivity.getCases().getCaseNumber());
        assertEquals(3, sixthActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.ASSIGN_TEAM, sixthActivity.getAction());

        // Seventh sla activity
        CaseSlaActivity seventhActivity = caseSlaActivities.get(6);
        assertNull(seventhActivity.getCaseSlaActivityId());
        assertEquals(userThirdHop.getUserId(), seventhActivity.getOwnerId());
        assertEquals(userThirdHop.getFullNameTH(), seventhActivity.getOwnerName());
        assertEquals(teamThirdHop.getTeamId(), seventhActivity.getTeamId());
        assertEquals(teamThirdHop.getNameTh(), seventhActivity.getTeamNameTh());
        assertEquals(userThirdHop.getFullNameTH(), seventhActivity.getCreatedByName());
        assertEquals(userThirdHop.getFullNameTH(), seventhActivity.getModifiedByName());
        assertEquals(userThirdHop.getUserId(), seventhActivity.getCreatedById());
        assertEquals(userThirdHop.getUserId(), seventhActivity.getModifiedById());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getStartDateTimeC()).plusNanos(6_000_000L), seventhActivity.getStartDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()).plusNanos(6_000_000L), seventhActivity.getEndDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getStartDateTimeC()).plusNanos(6_000_000L), seventhActivity.getCreatedOn());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()).plusNanos(6_000_000L), seventhActivity.getModifiedOn());
        assertEquals(0.625, seventhActivity.getActualDuration(), 0.0000001);
        assertEquals(caseModel.getCaseNumber(), seventhActivity.getCases().getCaseNumber());
        assertEquals(3, seventhActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.ASSIGN_OWNER, seventhActivity.getAction());

        // Eight sla activity
        CaseSlaActivity eigthActivity = caseSlaActivities.get(7);
        assertNull(eigthActivity.getCaseSlaActivityId());
        assertEquals(userThirdHop.getUserId(), eigthActivity.getOwnerId());
        assertEquals(userThirdHop.getFullNameTH(), eigthActivity.getOwnerName());
        assertEquals(teamThirdHop.getTeamId(), eigthActivity.getTeamId());
        assertEquals(teamThirdHop.getNameTh(), eigthActivity.getTeamNameTh());
        assertEquals(userThirdHop.getFullNameTH(), eigthActivity.getCreatedByName());
        assertEquals(userThirdHop.getFullNameTH(), eigthActivity.getModifiedByName());
        assertEquals(userThirdHop.getUserId(), eigthActivity.getCreatedById());
        assertEquals(userThirdHop.getUserId(), eigthActivity.getModifiedById());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()).plusNanos(7_000_000L), eigthActivity.getStartDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()).plusNanos(7_000_000L), eigthActivity.getEndDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()).plusNanos(7_000_000L), eigthActivity.getCreatedOn());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()).plusNanos(7_000_000L), eigthActivity.getModifiedOn());
        assertNull(eigthActivity.getActualDuration());
        assertEquals(caseModel.getCaseNumber(), eigthActivity.getCases().getCaseNumber());
        assertEquals(3, eigthActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.RESOLVED, eigthActivity.getAction());

        // Ninth sla activity
        CaseSlaActivity ninthActivity = caseSlaActivities.get(8);
        assertNull(ninthActivity.getCaseSlaActivityId());
        assertEquals(userThirdHop.getUserId(), ninthActivity.getOwnerId());
        assertEquals(userThirdHop.getFullNameTH(), ninthActivity.getOwnerName());
        assertEquals(teamThirdHop.getTeamId(), ninthActivity.getTeamId());
        assertEquals(teamThirdHop.getNameTh(), ninthActivity.getTeamNameTh());
        assertEquals(userThirdHop.getFullNameTH(), ninthActivity.getCreatedByName());
        assertEquals(userThirdHop.getFullNameTH(), ninthActivity.getModifiedByName());
        assertEquals(userThirdHop.getUserId(), ninthActivity.getCreatedById());
        assertEquals(userThirdHop.getUserId(), ninthActivity.getModifiedById());
        assertEquals(DateTimeUtils.getLocalDateTime().plusNanos(8_000_000L), ninthActivity.getStartDate());
        assertEquals(DateTimeUtils.getLocalDateTime().plusNanos(8_000_000L), ninthActivity.getEndDate());
        assertEquals(DateTimeUtils.getLocalDateTime().plusNanos(8_000_000L), ninthActivity.getCreatedOn());
        assertEquals(DateTimeUtils.getLocalDateTime().plusNanos(8_000_000L), ninthActivity.getModifiedOn());
        assertNull(ninthActivity.getActualDuration());
        assertEquals(caseModel.getCaseNumber(), ninthActivity.getCases().getCaseNumber());
        assertEquals(3, ninthActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.NEXT_HOP, ninthActivity.getAction());

        // Tenth sla activity
        CaseSlaActivity tenthActivity = caseSlaActivities.get(9);
        assertNull(tenthActivity.getCaseSlaActivityId());
        assertNull(tenthActivity.getOwnerId());
        assertNull(tenthActivity.getOwnerName());
        assertEquals(teamFourthHop.getTeamId(), tenthActivity.getTeamId());
        assertEquals(teamFourthHop.getNameTh(), tenthActivity.getTeamNameTh());
        assertEquals(userThirdHop.getFullNameTH(), tenthActivity.getCreatedByName());
        assertEquals(userThirdHop.getFullNameTH(), tenthActivity.getModifiedByName());
        assertEquals(userThirdHop.getUserId(), tenthActivity.getCreatedById());
        assertEquals(userThirdHop.getUserId(), tenthActivity.getModifiedById());
        assertEquals(DateTimeUtils.getLocalDateTime().plusNanos(9_000_000L), tenthActivity.getStartDate());
        assertNull(tenthActivity.getEndDate());
        assertEquals(DateTimeUtils.getLocalDateTime().plusNanos(9_000_000L), tenthActivity.getCreatedOn());
        assertEquals(DateTimeUtils.getLocalDateTime().plusNanos(9_000_000L), tenthActivity.getModifiedOn());
        assertNull(tenthActivity.getActualDuration());
        assertEquals(caseModel.getCaseNumber(), tenthActivity.getCases().getCaseNumber());
        assertEquals(4, tenthActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.ASSIGN_TEAM, tenthActivity.getAction());

        // last modify data is same in case and last activity
        assertEquals(tenthActivity.getModifiedOn(), caseModel.getModifiedOn());
        assertEquals(tenthActivity.getModifiedById(), caseModel.getModifiedById());
    }

    @Test
    void caseResolvedClosedByCreatorCase() {
        mockTempSla.mockTempStgSlaPerOwnerResolvedNextHistoryNotIsClosedByBu(stgSlaPerOwnerModels);
        ServiceTypeMatrixModel serviceTypeMatrixModel = mockSTM.mockSTMDynamicWithOutFCRClosedByCreator();
        TeamReadonlyModel createdByTeam = mockTeam.mockTeam(tempCaseModel.getCreatedByTeamNew());
        EmployeeUserModel createByUser = mockEmployeeUser.mockEmployeeUser(tempCaseModel.getCreatedByEmployeeIdC(), tempCaseModel.getCreatedNameC());
        EmployeeUserModel naEmployee = mockEmployeeUser.mockEmployeeUser(Constant.NA_EMPLOYEE_ID, Constant.NA_EMPLOYEE_FULL_NAME);
        EmployeeUserModel systemEmployee = mockEmployeeUser.mockEmployeeUser(Constant.SYSTEM_EMPLOYEE_ID);

        EmployeeUserModel userSecondHop = mockEmployeeUser.mockEmployeeUser(stgSlaPerOwnerModels.getFirst().getEmployeeIdC(), stgSlaPerOwnerModels.getFirst().getName());
        EmployeeUserModel userThirdHop = mockEmployeeUser.mockEmployeeUser(stgSlaPerOwnerModels.get(1).getEmployeeIdC(), stgSlaPerOwnerModels.get(1).getName());
        EmployeeUserModel userFourthHop = mockEmployeeUser.mockEmployeeUser(stgSlaPerOwnerModels.getLast().getEmployeeIdC(), stgSlaPerOwnerModels.getLast().getName());

        TeamReadonlyModel teamSecondHop = mockTeam.mockTeam(stgSlaPerOwnerModels.getFirst().getOwnerTeamNew());
        TeamReadonlyModel teamThirdHop = mockTeam.mockTeam(stgSlaPerOwnerModels.get(1).getOwnerTeamNew());

        createdByTeam.setTeamId(UUID.fromString("ac1b9644-d165-411b-be8b-a646d8d00e6c"));
        teamSecondHop.setTeamId(UUID.fromString("ac1b9644-d165-411b-be8b-a646d8d00e62"));
        teamThirdHop.setTeamId(UUID.fromString("ac1b9644-d165-411b-be8b-a646d8d00e63"));

        createByUser.setUserId(UUID.fromString("ac1b9644-d165-411b-be8b-a646d8d00ee1"));
        naEmployee.setUserId(UUID.fromString("ac1b9644-d165-411b-be8b-a646d8d00eec"));
        userSecondHop.setUserId(UUID.fromString("ac1b9644-d165-411b-be8b-a646d8d00ee2"));
        userThirdHop.setUserId(UUID.fromString("ac1b9644-d165-411b-be8b-a646d8d00ee3"));
        userFourthHop.setUserId(UUID.fromString("ac1b9644-d165-411b-be8b-a646d8d00ee4"));

       when(serviceTypeMatrixRepository.findByServiceTypeMatrixCode(Mockito.anyString()))
                .thenReturn(serviceTypeMatrixModel);

        when(teamReadonlyRepository.findByNameTh(Mockito.anyString()))
                .thenReturn(
                        Optional.ofNullable(createdByTeam),
                        Optional.ofNullable(teamSecondHop),
                        Optional.ofNullable(teamThirdHop),
                        Optional.ofNullable(createdByTeam));

        when(employeeUserRepository.findByEmployeeId(Mockito.anyString()))
                .thenReturn(
                        Optional.ofNullable(createByUser),
                        Optional.ofNullable(naEmployee),
                        Optional.ofNullable(systemEmployee),
                        Optional.ofNullable(userSecondHop),
                        Optional.ofNullable(userThirdHop),
                        Optional.ofNullable(userFourthHop));

        when(employeeUserRepository.findTeamIdByEmployeeId(Mockito.anyString()))
                .thenReturn(
                        List.of(teamSecondHop.getTeamId().toString()),
                        List.of(teamThirdHop.getTeamId().toString()),
                        List.of(createdByTeam.getTeamId().toString()));

        when(teamReadonlyRepository.findAllByTeamIdInAndStatusCode(Mockito.any(List.class), Mockito.anyInt()))
                .thenReturn(List.of(teamSecondHop))
                .thenReturn(List.of(teamThirdHop))
                .thenReturn(List.of(createdByTeam));

        when(stgSlaPerOwnerRepository.findAllByCaseCOrderByStartDateTimeCAsc(tempCaseModel.getSfId())).thenReturn(stgSlaPerOwnerModels);

        when(caseRepository.save(Mockito.any(CaseTransactionModel.class))).thenAnswer(invocation -> {
            CaseTransactionModel input = invocation.getArgument(0);

            // simulate JPA @GeneratedValue
            input.setCaseId(UUID.randomUUID());

            return input;
        });

        when(caseSlaHopRepository.saveAll(Mockito.anyList()))
                .thenAnswer(invocation -> {
                    List<CaseSlaHopModel> inputList = invocation.getArgument(0);
                    for (CaseSlaHopModel item : inputList) {
                        item.setCaseSlaHopId(UUID.randomUUID());
                    }

                    return inputList;
                });

        when(caseSlaActivityRepository.saveAll(Mockito.anyList()))
                .thenAnswer(invocation -> {
                    List<CaseSlaActivity> inputList = invocation.getArgument(0);
                    for (CaseSlaActivity item : inputList) {
                        item.setCaseSlaActivityId(UUID.randomUUID());
                    }

                    return inputList;
                });

        when(kafkaTemplate.send(Mockito.any(Message.class)))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        StgToCaseWriterDTO transaction = createCaseService.createCase(tempCaseModel);

        CaseTransactionModel caseModel = transaction.getCaseTransaction();

        // Case
        assertEquals(tempCaseModel.getCaseNumber(), caseModel.getCaseNumber());
        assertEquals(createdByTeam.getTeamId(), caseModel.getCreatedByTeamId());
        assertEquals(createByUser.getUserId(), caseModel.getCreatedById());
        assertNotNull(caseModel.getModifiedById());
        ZonedDateTime createDate = DateTimeUtils.parseToZoneDateTime(tempCaseModel.getCreatedDate());
        assertEquals(createDate, caseModel.getCreatedOn());
        assertNotNull(caseModel.getModifiedOn());
        assertEquals(Constant.CASE_STATUS_RESOLVED, caseModel.getCaseStatusCode());
        assertEquals(Constant.TEP, caseModel.getIntegrationSystem());
        assertEquals(4, caseModel.getActiveHopNumber());

        // Case Resolved Info
        assertEquals(userThirdHop.getFullNameTH(), caseModel.getResolvedBy());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()), caseModel.getResolvedDate());
        assertEquals(userThirdHop.getUserId(), caseModel.getResolvedById());
        assertEquals(teamThirdHop.getTeamId(), caseModel.getResolvedByTeamId());

        // Case Closed Info
        assertNull(caseModel.getClosedBy());
        assertNull(caseModel.getClosedDate());
        assertNull(caseModel.getClosedById());
        assertNull(caseModel.getClosedByTeamId());

        // Service type matrix
        assertNotNull(serviceTypeMatrixModel);
        assertEquals(serviceTypeMatrixModel.getServiceTypeMatrixCode(), caseModel.getServiceTypeMatrixCode());
        assertEquals(serviceTypeMatrixModel.getServiceTypeMatrixType(), caseModel.getServiceTypeMatrixType());
        assertEquals(serviceTypeMatrixModel.getSla(), caseModel.getSla());
        assertEquals(serviceTypeMatrixModel.getFcr(), caseModel.getFcr());
        assertEquals(ServiceTypeMatrixTypeEnum.DYNAMIC, caseModel.getServiceTypeMatrixType());

        // Case Sla hop
        List<CaseSlaHopModel> caseSlas = caseModel.getSlaHop();
        assertNotNull(caseSlas);
        assertEquals(4, caseSlas.size());

        // First case sla hop
        CaseSlaHopModel firstHop = caseSlas.getFirst();
        assertNull(firstHop.getCaseSlaHopId());
        assertEquals(createDate, firstHop.getStartDatetime());
        assertEquals(createDate, firstHop.getEndDatetime());
        assertEquals(createByUser.getUserId(), firstHop.getOwnerId());
        assertEquals(createByUser.getFullNameTH(), firstHop.getOwnerName());
        assertEquals(createdByTeam.getTeamId(), firstHop.getTeamId());
        assertEquals(createdByTeam.getNameTh(), firstHop.getTeamName());
        assertEquals(createDate, firstHop.getCreatedOn());
        assertEquals(createDate, firstHop.getModifiedOn());
        assertEquals(createByUser.getUserId(), firstHop.getCreatedById());
        assertEquals(createByUser.getUserId(), firstHop.getModifiedById());
        assertEquals(1, firstHop.getHopNumber());
        assertEquals(0, firstHop.getTotalDuration());
        assertEquals(false, firstHop.getCloseByBu());
        assertNull(firstHop.getSlaTarget());
        assertNull(firstHop.getSlaTargetDate());
        assertEquals(0.0, firstHop.getTotalDuration(), 0.0000001);
        assertEquals(createdByTeam.getNameTh(), firstHop.getTeamName());
        assertEquals(createdByTeam.getTeamId(), firstHop.getTeamId());
        assertEquals(caseModel.getCaseId(), firstHop.getCases().getCaseId());

        // Second Case Sla hop
        CaseSlaHopModel secondHop = caseSlas.get(1);
        assertNull(secondHop.getCaseSlaHopId());
        assertNull(secondHop.getTotalDuration());
        assertNull(secondHop.getSlaTarget());
        assertNull(secondHop.getSlaTargetDate());
        assertNull(secondHop.getTotalDuration());
        assertNotNull(secondHop.getOwnerId());
        assertEquals(stgSlaPerOwnerModels.getFirst().getName(), secondHop.getOwnerName());
        assertEquals(teamSecondHop.getNameTh(), secondHop.getTeamName());
        assertEquals(teamSecondHop.getTeamId(), secondHop.getTeamId());
        assertEquals(2, secondHop.getHopNumber());
        assertEquals(false, secondHop.getCloseByBu());
        assertEquals(createByUser.getUserId(), secondHop.getCreatedById());
        assertEquals(userSecondHop.getUserId(), secondHop.getModifiedById());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getStartDateTimeC()), secondHop.getStartDatetime());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()), secondHop.getEndDatetime());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getStartDateTimeC()), secondHop.getCreatedOn());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()), secondHop.getModifiedOn());
        assertEquals(caseModel.getCaseId(), secondHop.getCases().getCaseId());

        // Third(last) Case sla hop
        CaseSlaHopModel thirdHop = caseSlas.get(2);
        assertNull(thirdHop.getCaseSlaHopId());
        assertNull(thirdHop.getSlaTarget());
        assertNull(thirdHop.getSlaTargetDate());
        assertNull(thirdHop.getTotalDuration());
        assertNotNull(thirdHop.getOwnerId());
        assertEquals(stgSlaPerOwnerModels.get(1).getName(), thirdHop.getOwnerName());
        assertEquals(teamThirdHop.getNameTh(), thirdHop.getTeamName());
        assertEquals(teamThirdHop.getTeamId(), thirdHop.getTeamId());
        assertEquals(userSecondHop.getUserId(), thirdHop.getCreatedById());
        assertEquals(userThirdHop.getUserId(), thirdHop.getModifiedById());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getStartDateTimeC()), thirdHop.getStartDatetime());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()), thirdHop.getEndDatetime());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getStartDateTimeC()), thirdHop.getCreatedOn());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()), thirdHop.getModifiedOn());
        assertEquals(3, thirdHop.getHopNumber());
        assertEquals(false, thirdHop.getCloseByBu());
        assertEquals(caseModel.getCaseId(), thirdHop.getCases().getCaseId());

        // Fourth(last) Case sla hop
        CaseSlaHopModel fourthHop = caseSlas.getLast();
        assertNull(fourthHop.getCaseSlaHopId());
        assertNull(fourthHop.getSlaTarget());
        assertNull(fourthHop.getSlaTargetDate());
        assertNull(fourthHop.getTotalDuration());
        assertNull(fourthHop.getOwnerId());
        assertNull(fourthHop.getOwnerName());
        assertEquals(createdByTeam.getNameTh(), fourthHop.getTeamName());
        assertEquals(createdByTeam.getTeamId(), fourthHop.getTeamId());
        assertEquals(userThirdHop.getUserId(), fourthHop.getCreatedById());
        assertEquals(userThirdHop.getUserId(), fourthHop.getModifiedById());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getLast().getStartDateTimeC()), fourthHop.getStartDatetime());
        assertNull(fourthHop.getEndDatetime());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getLast().getStartDateTimeC()), fourthHop.getCreatedOn());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getLast().getStartDateTimeC()), fourthHop.getModifiedOn());
        assertEquals(4, fourthHop.getHopNumber());
        assertEquals(true, fourthHop.getCloseByBu());
        assertEquals(caseModel.getCaseId(), fourthHop.getCases().getCaseId());

        // Case sla activities
        List<CaseSlaActivity> caseSlaActivities = transaction.getCaseTransaction().getCaseSlaActivities();
        assertNotNull(caseSlaActivities);
        assertEquals(11, caseSlaActivities.size());

        // First sla activity
        CaseSlaActivity firstActivity = caseSlaActivities.getFirst();
        assertNull(firstActivity.getCaseSlaActivityId());
        assertEquals(createByUser.getUserId(), firstActivity.getOwnerId());
        assertEquals(createByUser.getFullNameTH(), firstActivity.getOwnerName());
        assertEquals(createdByTeam.getTeamId(), firstActivity.getTeamId());
        assertEquals(createdByTeam.getNameTh(), firstActivity.getTeamNameTh());
        assertEquals(createByUser.getFullNameTH(), firstActivity.getCreatedByName());
        assertEquals(createByUser.getFullNameTH(), firstActivity.getModifiedByName());
        assertEquals(createByUser.getUserId(), firstActivity.getCreatedById());
        assertEquals(createByUser.getUserId(), firstActivity.getModifiedById());
        assertEquals(createDate, firstActivity.getStartDate());
        assertEquals(createDate, firstActivity.getEndDate());
        assertEquals(createDate, firstActivity.getCreatedOn());
        assertEquals(createDate, firstActivity.getModifiedOn());
        assertEquals(caseModel.getCaseNumber(), firstActivity.getCases().getCaseNumber());
        assertEquals(1, firstActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.CREATE, firstActivity.getAction());
        assertEquals(0.0, firstActivity.getActualDuration(), 0.0000001);

        // Second sla activity
        CaseSlaActivity secondActivity = caseSlaActivities.get(1);
        assertNull(secondActivity.getCaseSlaActivityId());
        assertEquals(createByUser.getUserId(), secondActivity.getOwnerId());
        assertEquals(createByUser.getFullNameTH(), secondActivity.getOwnerName());
        assertEquals(createdByTeam.getTeamId(), secondActivity.getTeamId());
        assertEquals(createdByTeam.getNameTh(), secondActivity.getTeamNameTh());
        assertEquals(createByUser.getFullNameTH(), secondActivity.getCreatedByName());
        assertEquals(createByUser.getFullNameTH(), secondActivity.getModifiedByName());
        assertEquals(createByUser.getUserId(), secondActivity.getCreatedById());
        assertEquals(createByUser.getUserId(), secondActivity.getModifiedById());
        assertEquals(createDate.plusNanos(1_000_000L), secondActivity.getStartDate());
        assertEquals(createDate.plusNanos(1_000_000L), secondActivity.getEndDate());
        assertEquals(createDate.plusNanos(1_000_000L), secondActivity.getCreatedOn());
        assertEquals(createDate.plusNanos(1_000_000L), secondActivity.getModifiedOn());
        assertNull(secondActivity.getActualDuration());
        assertEquals(caseModel.getCaseNumber(), secondActivity.getCases().getCaseNumber());
        assertEquals(1, secondActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.NEXT_HOP, secondActivity.getAction());

        // Third sla activity
        CaseSlaActivity thirdActivity = caseSlaActivities.get(2);
        assertNull(thirdActivity.getCaseSlaActivityId());
        assertNull(thirdActivity.getOwnerId());
        assertNull(thirdActivity.getOwnerName());
        assertEquals(teamSecondHop.getTeamId(), thirdActivity.getTeamId());
        assertEquals(teamSecondHop.getNameTh(), thirdActivity.getTeamNameTh());
        assertEquals(createByUser.getFullNameTH(), thirdActivity.getCreatedByName());
        assertEquals(userSecondHop.getFullNameTH(), thirdActivity.getModifiedByName());
        assertEquals(createByUser.getUserId(), thirdActivity.getCreatedById());
        assertEquals(userSecondHop.getUserId(), thirdActivity.getModifiedById());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getStartDateTimeC()).plusNanos(2_000_000L), thirdActivity.getStartDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()).plusNanos(2_000_000L), thirdActivity.getEndDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getStartDateTimeC()).plusNanos(2_000_000L), thirdActivity.getCreatedOn());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()).plusNanos(2_000_000L), thirdActivity.getModifiedOn());
        assertNotNull(thirdActivity.getActualDuration());
        assertEquals(caseModel.getCaseNumber(), thirdActivity.getCases().getCaseNumber());
        assertEquals(2, thirdActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.ASSIGN_TEAM, thirdActivity.getAction());

        // Fourth sla activity
        CaseSlaActivity fourthActivity = caseSlaActivities.get(3);
        assertNull(fourthActivity.getCaseSlaActivityId());
        assertEquals(userSecondHop.getUserId(), fourthActivity.getOwnerId());
        assertEquals(userSecondHop.getFullNameTH(), fourthActivity.getOwnerName());
        assertEquals(teamSecondHop.getTeamId(), fourthActivity.getTeamId());
        assertEquals(teamSecondHop.getNameTh(), fourthActivity.getTeamNameTh());
        assertEquals(userSecondHop.getFullNameTH(), fourthActivity.getCreatedByName());
        assertEquals(userSecondHop.getFullNameTH(), fourthActivity.getModifiedByName());
        assertEquals(userSecondHop.getUserId(), fourthActivity.getCreatedById());
        assertEquals(userSecondHop.getUserId(), fourthActivity.getModifiedById());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getStartDateTimeC()).plusNanos(3_000_000L), fourthActivity.getStartDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()).plusNanos(3_000_000L), fourthActivity.getEndDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getStartDateTimeC()).plusNanos(3_000_000L), fourthActivity.getCreatedOn());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()).plusNanos(3_000_000L), fourthActivity.getModifiedOn());
        assertNotNull(fourthActivity.getActualDuration());
        assertEquals(caseModel.getCaseNumber(), fourthActivity.getCases().getCaseNumber());
        assertEquals(2, fourthActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.ASSIGN_OWNER, fourthActivity.getAction());

        // Fifth sla activity
        CaseSlaActivity fifthActivity = caseSlaActivities.get(4);
        assertNull(fifthActivity.getCaseSlaActivityId());
        assertEquals(userSecondHop.getUserId(), fifthActivity.getOwnerId());
        assertEquals(userSecondHop.getFullNameTH(), fifthActivity.getOwnerName());
        assertEquals(teamSecondHop.getTeamId(), fifthActivity.getTeamId());
        assertEquals(teamSecondHop.getNameTh(), fifthActivity.getTeamNameTh());
        assertEquals(userSecondHop.getFullNameTH(), fifthActivity.getCreatedByName());
        assertEquals(userSecondHop.getFullNameTH(), fifthActivity.getModifiedByName());
        assertEquals(userSecondHop.getUserId(), fifthActivity.getCreatedById());
        assertEquals(userSecondHop.getUserId(), fifthActivity.getModifiedById());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()).plusNanos(4_000_000L), fifthActivity.getStartDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()).plusNanos(4_000_000L), fifthActivity.getEndDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()).plusNanos(4_000_000L), fifthActivity.getCreatedOn());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()).plusNanos(4_000_000L), fifthActivity.getModifiedOn());
        assertNull(fifthActivity.getActualDuration());
        assertEquals(caseModel.getCaseNumber(), fifthActivity.getCases().getCaseNumber());
        assertEquals(2, fifthActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.RESOLVED, fifthActivity.getAction());

        // Sixth sla activity
        CaseSlaActivity sixthActivity = caseSlaActivities.get(5);
        assertNull(sixthActivity.getCaseSlaActivityId());
        assertEquals(userSecondHop.getUserId(), sixthActivity.getOwnerId());
        assertEquals(userSecondHop.getFullNameTH(), sixthActivity.getOwnerName());
        assertEquals(teamSecondHop.getTeamId(), sixthActivity.getTeamId());
        assertEquals(teamSecondHop.getNameTh(), sixthActivity.getTeamNameTh());
        assertEquals(userSecondHop.getFullNameTH(), sixthActivity.getCreatedByName());
        assertEquals(userSecondHop.getFullNameTH(), sixthActivity.getModifiedByName());
        assertEquals(userSecondHop.getUserId(), sixthActivity.getCreatedById());
        assertEquals(userSecondHop.getUserId(), sixthActivity.getModifiedById());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()).plusNanos(5_000_000L), sixthActivity.getStartDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()).plusNanos(5_000_000L), sixthActivity.getEndDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()).plusNanos(5_000_000L), sixthActivity.getCreatedOn());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.getFirst().getEndDateTimeC()).plusNanos(5_000_000L), sixthActivity.getModifiedOn());
        assertNull(sixthActivity.getActualDuration());
        assertEquals(caseModel.getCaseNumber(), sixthActivity.getCases().getCaseNumber());
        assertEquals(2, sixthActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.NEXT_HOP, sixthActivity.getAction());

        // Seventh sla activity
        CaseSlaActivity seventhActivity = caseSlaActivities.get(6);
        assertNull(seventhActivity.getCaseSlaActivityId());
        assertNull(seventhActivity.getOwnerId());
        assertNull(seventhActivity.getOwnerName());
        assertEquals(teamThirdHop.getTeamId(), seventhActivity.getTeamId());
        assertEquals(teamThirdHop.getNameTh(), seventhActivity.getTeamNameTh());
        assertEquals(userSecondHop.getFullNameTH(), seventhActivity.getCreatedByName());
        assertEquals(userThirdHop.getFullNameTH(), seventhActivity.getModifiedByName());
        assertEquals(userSecondHop.getUserId(), seventhActivity.getCreatedById());
        assertEquals(userThirdHop.getUserId(), seventhActivity.getModifiedById());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getStartDateTimeC()).plusNanos(6_000_000L), seventhActivity.getStartDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()).plusNanos(6_000_000L), seventhActivity.getEndDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getStartDateTimeC()).plusNanos(6_000_000L), seventhActivity.getCreatedOn());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()).plusNanos(6_000_000L), seventhActivity.getModifiedOn());
        assertEquals(0.625, seventhActivity.getActualDuration(), 0.0000001);
        assertEquals(caseModel.getCaseNumber(), seventhActivity.getCases().getCaseNumber());
        assertEquals(3, seventhActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.ASSIGN_TEAM, seventhActivity.getAction());

        // Eight sla activity
        CaseSlaActivity eightActivity = caseSlaActivities.get(7);
        assertNull(eightActivity.getCaseSlaActivityId());
        assertEquals(userThirdHop.getUserId(), eightActivity.getOwnerId());
        assertEquals(userThirdHop.getFullNameTH(), eightActivity.getOwnerName());
        assertEquals(teamThirdHop.getTeamId(), eightActivity.getTeamId());
        assertEquals(teamThirdHop.getNameTh(), eightActivity.getTeamNameTh());
        assertEquals(userThirdHop.getFullNameTH(), eightActivity.getCreatedByName());
        assertEquals(userThirdHop.getFullNameTH(), eightActivity.getModifiedByName());
        assertEquals(userThirdHop.getUserId(), eightActivity.getCreatedById());
        assertEquals(userThirdHop.getUserId(), eightActivity.getModifiedById());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getStartDateTimeC()).plusNanos(7_000_000L), eightActivity.getStartDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()).plusNanos(7_000_000L), eightActivity.getEndDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getStartDateTimeC()).plusNanos(7_000_000L), eightActivity.getCreatedOn());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()).plusNanos(7_000_000L), eightActivity.getModifiedOn());
        assertEquals(0.625, eightActivity.getActualDuration(), 0.0000001);
        assertEquals(caseModel.getCaseNumber(), eightActivity.getCases().getCaseNumber());
        assertEquals(3, eightActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.ASSIGN_OWNER, eightActivity.getAction());

        // Ninth sla activity
        CaseSlaActivity ninthActivity = caseSlaActivities.get(8);
        assertNull(ninthActivity.getCaseSlaActivityId());
        assertEquals(userThirdHop.getUserId(), ninthActivity.getOwnerId());
        assertEquals(userThirdHop.getFullNameTH(), ninthActivity.getOwnerName());
        assertEquals(teamThirdHop.getTeamId(), ninthActivity.getTeamId());
        assertEquals(teamThirdHop.getNameTh(), ninthActivity.getTeamNameTh());
        assertEquals(userThirdHop.getFullNameTH(), ninthActivity.getCreatedByName());
        assertEquals(userThirdHop.getFullNameTH(), ninthActivity.getModifiedByName());
        assertEquals(userThirdHop.getUserId(), ninthActivity.getCreatedById());
        assertEquals(userThirdHop.getUserId(), ninthActivity.getModifiedById());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()).plusNanos(8_000_000L), ninthActivity.getStartDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()).plusNanos(8_000_000L), ninthActivity.getEndDate());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()).plusNanos(8_000_000L), ninthActivity.getCreatedOn());
        assertEquals(DateTimeUtils.parseToZoneDateTime(stgSlaPerOwnerModels.get(1).getEndDateTimeC()).plusNanos(8_000_000L), ninthActivity.getModifiedOn());
        assertNull(ninthActivity.getActualDuration());
        assertEquals(caseModel.getCaseNumber(), ninthActivity.getCases().getCaseNumber());
        assertEquals(3, ninthActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.RESOLVED, ninthActivity.getAction());

        // Ninth sla activity
        CaseSlaActivity tenthActivity = caseSlaActivities.get(9);
        assertNull(tenthActivity.getCaseSlaActivityId());
        assertEquals(userThirdHop.getUserId(), tenthActivity.getOwnerId());
        assertEquals(userThirdHop.getFullNameTH(), tenthActivity.getOwnerName());
        assertEquals(teamThirdHop.getTeamId(), tenthActivity.getTeamId());
        assertEquals(teamThirdHop.getNameTh(), tenthActivity.getTeamNameTh());
        assertEquals(userThirdHop.getFullNameTH(), tenthActivity.getCreatedByName());
        assertEquals(userThirdHop.getFullNameTH(), tenthActivity.getModifiedByName());
        assertEquals(userThirdHop.getUserId(), tenthActivity.getCreatedById());
        assertEquals(userThirdHop.getUserId(), tenthActivity.getModifiedById());
        assertEquals(DateTimeUtils.getLocalDateTime().plusNanos(9_000_000L), tenthActivity.getStartDate());
        assertEquals(DateTimeUtils.getLocalDateTime().plusNanos(9_000_000L), tenthActivity.getEndDate());
        assertEquals(DateTimeUtils.getLocalDateTime().plusNanos(9_000_000L), tenthActivity.getCreatedOn());
        assertEquals(DateTimeUtils.getLocalDateTime().plusNanos(9_000_000L), tenthActivity.getModifiedOn());
        assertNull(tenthActivity.getActualDuration());
        assertEquals(caseModel.getCaseNumber(), tenthActivity.getCases().getCaseNumber());
        assertEquals(3, tenthActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.NEXT_HOP, tenthActivity.getAction());

        // EleventhActivity sla activity
        CaseSlaActivity eleventhActivity = caseSlaActivities.get(10);
        assertNull(eleventhActivity.getCaseSlaActivityId());
        assertEquals(userThirdHop.getUserId(), ninthActivity.getOwnerId());
        assertEquals(userThirdHop.getFullNameTH(), ninthActivity.getOwnerName());
        assertEquals(createdByTeam.getTeamId(), eleventhActivity.getTeamId());
        assertEquals(createdByTeam.getNameTh(), eleventhActivity.getTeamNameTh());
        assertEquals(userThirdHop.getFullNameTH(), eleventhActivity.getCreatedByName());
        assertEquals(userThirdHop.getFullNameTH(), eleventhActivity.getModifiedByName());
        assertEquals(userThirdHop.getUserId(), eleventhActivity.getCreatedById());
        assertEquals(userThirdHop.getUserId(), eleventhActivity.getModifiedById());
        assertEquals(DateTimeUtils.getLocalDateTime().plusNanos(10_000_000L), eleventhActivity.getStartDate());
        assertNull(eleventhActivity.getEndDate());
        assertEquals(DateTimeUtils.getLocalDateTime().plusNanos(10_000_000L), eleventhActivity.getCreatedOn());
        assertEquals(DateTimeUtils.getLocalDateTime().plusNanos(10_000_000L), eleventhActivity.getModifiedOn());
        assertNull(eleventhActivity.getActualDuration());
        assertEquals(caseModel.getCaseNumber(), eleventhActivity.getCases().getCaseNumber());
        assertEquals(4, eleventhActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.ASSIGN_TEAM, eleventhActivity.getAction());

        // last modify data is same in case and last activity
        assertEquals(eleventhActivity.getModifiedOn(), caseModel.getModifiedOn());
        assertEquals(eleventhActivity.getModifiedById(), caseModel.getModifiedById());
    }

    @Test
    @Disabled("can have sla per owner team not matched any of the employee's teams")
    void caseResolvedOwnerNotInTeamAllBuCanNotMigrationCase() {
        mockTempSla.mockTempStgSlaPerOwnerResolvedNextHistoryNotIsClosedByBu(stgSlaPerOwnerModels);
        ServiceTypeMatrixModel serviceTypeMatrixModel = mockSTM.mockSTMFixWithoutFCR();
        TeamReadonlyModel createdByTeam = mockTeam.mockTeam(tempCaseModel.getCreatedByTeamNew());
        EmployeeUserModel createByUser = mockEmployeeUser.mockEmployeeUser(tempCaseModel.getCreatedByEmployeeIdC(), tempCaseModel.getCreatedNameC());
        EmployeeUserModel naEmployee = mockEmployeeUser.mockEmployeeUser(Constant.NA_EMPLOYEE_ID, Constant.NA_EMPLOYEE_FULL_NAME);

        EmployeeUserModel userSecondHop = mockEmployeeUser.mockEmployeeUser(stgSlaPerOwnerModels.getFirst().getEmployeeIdC(), stgSlaPerOwnerModels.getFirst().getName());
        EmployeeUserModel userThirdHop = mockEmployeeUser.mockEmployeeUser(stgSlaPerOwnerModels.get(1).getEmployeeIdC(), stgSlaPerOwnerModels.get(1).getName());
        EmployeeUserModel userFourthHop = mockEmployeeUser.mockEmployeeUser(stgSlaPerOwnerModels.getLast().getEmployeeIdC(), stgSlaPerOwnerModels.getLast().getName());

        TeamReadonlyModel teamSecondHop = mockTeam.mockTeam(stgSlaPerOwnerModels.getFirst().getOwnerTeamNew());
        TeamReadonlyModel teamThirdHop = mockTeam.mockTeam(stgSlaPerOwnerModels.get(1).getOwnerTeamNew());
        TeamReadonlyModel teamFourthHop = mockTeam.mockTeam(stgSlaPerOwnerModels.getLast().getOwnerTeamNew());

       when(serviceTypeMatrixRepository.findByServiceTypeMatrixCode(Mockito.anyString()))
                .thenReturn(serviceTypeMatrixModel);

        when(teamReadonlyRepository.findByNameTh(Mockito.anyString()))
                .thenReturn(
                        Optional.ofNullable(createdByTeam),
                        Optional.ofNullable(teamSecondHop),
                        Optional.ofNullable(teamThirdHop),
                        Optional.ofNullable(teamFourthHop));

        when(employeeUserRepository.findByEmployeeId(Mockito.anyString()))
                .thenReturn(
                        Optional.ofNullable(createByUser),
                        Optional.ofNullable(naEmployee),
                        Optional.ofNullable(userThirdHop),
                        Optional.ofNullable(userFourthHop));

        when(employeeUserRepository.findTeamIdByEmployeeId(Mockito.anyString()))
                .thenReturn(
                        List.of(createdByTeam.getTeamId().toString()),
                        List.of(createdByTeam.getTeamId().toString()),
                        List.of(createdByTeam.getTeamId().toString()));

        when(teamReadonlyRepository.findAllByTeamIdInAndStatusCode(Mockito.any(List.class), Mockito.anyInt()))
                .thenReturn(List.of(createdByTeam))
                .thenReturn(List.of(createdByTeam))
                .thenReturn(List.of(createdByTeam));

        when(stgSlaPerOwnerRepository.findAllByCaseCOrderByStartDateTimeCAsc(tempCaseModel.getSfId())).thenReturn(stgSlaPerOwnerModels);

        when(caseRepository.save(Mockito.any(CaseTransactionModel.class))).thenAnswer(invocation -> {
            CaseTransactionModel input = invocation.getArgument(0);

            // simulate JPA @GeneratedValue
            input.setCaseId(UUID.randomUUID());

            return input;
        });

        when(caseSlaHopRepository.saveAll(Mockito.anyList()))
                .thenAnswer(invocation -> {
                    List<CaseSlaHopModel> inputList = invocation.getArgument(0);
                    for (CaseSlaHopModel item : inputList) {
                        item.setCaseSlaHopId(UUID.randomUUID());
                    }

                    return inputList;
                });

        when(caseSlaActivityRepository.saveAll(Mockito.anyList()))
                .thenAnswer(invocation -> {
                    List<CaseSlaActivity> inputList = invocation.getArgument(0);
                    for (CaseSlaActivity item : inputList) {
                        item.setCaseSlaActivityId(UUID.randomUUID());
                    }

                    return inputList;
                });

        when(kafkaTemplate.send(Mockito.any(Message.class)))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            createCaseService.createCase(tempCaseModel);
        });

        assertEquals("No resolved SLA owner team matched any of the employee's teams", exception.getMessage());
    }

    @Test
    void caseResolvedInvalidStartDate() {
        mockTempSla.mockTempStgSlaPerOwnerResolvedInvalidStartDate(stgSlaPerOwnerModels);
        ServiceTypeMatrixModel serviceTypeMatrixModel = mockSTM.mockSTMFixWithoutFCR();
        TeamReadonlyModel createdByTeam = mockTeam.mockTeam(tempCaseModel.getCreatedByTeamNew());
        EmployeeUserModel createByUser = mockEmployeeUser.mockEmployeeUser(tempCaseModel.getCreatedByEmployeeIdC(), tempCaseModel.getCreatedNameC());
        EmployeeUserModel naEmployee = mockEmployeeUser.mockEmployeeUser(Constant.NA_EMPLOYEE_ID, Constant.NA_EMPLOYEE_FULL_NAME);

        EmployeeUserModel userSecondHop = mockEmployeeUser.mockEmployeeUser(stgSlaPerOwnerModels.getFirst().getEmployeeIdC(), stgSlaPerOwnerModels.getFirst().getName());
        EmployeeUserModel userThirdHop = mockEmployeeUser.mockEmployeeUser(stgSlaPerOwnerModels.get(1).getEmployeeIdC(), stgSlaPerOwnerModels.get(1).getName());
        EmployeeUserModel userFourthHop = mockEmployeeUser.mockEmployeeUser(stgSlaPerOwnerModels.getLast().getEmployeeIdC(), stgSlaPerOwnerModels.getLast().getName());

        TeamReadonlyModel teamSecondHop = mockTeam.mockTeam(stgSlaPerOwnerModels.getFirst().getOwnerTeamNew());
        TeamReadonlyModel teamThirdHop = mockTeam.mockTeam(stgSlaPerOwnerModels.get(1).getOwnerTeamNew());
        TeamReadonlyModel teamFourthHop = mockTeam.mockTeam(stgSlaPerOwnerModels.getLast().getOwnerTeamNew());

       when(serviceTypeMatrixRepository.findByServiceTypeMatrixCode(Mockito.anyString()))
                .thenReturn(serviceTypeMatrixModel);

        when(teamReadonlyRepository.findByNameTh(Mockito.anyString()))
                .thenReturn(
                        Optional.ofNullable(createdByTeam),
                        Optional.ofNullable(teamSecondHop),
                        Optional.ofNullable(teamThirdHop),
                        Optional.ofNullable(teamFourthHop));

        when(employeeUserRepository.findByEmployeeId(Mockito.anyString()))
                .thenReturn(
                        Optional.ofNullable(createByUser),
                        Optional.ofNullable(naEmployee),
                        Optional.ofNullable(userThirdHop),
                        Optional.ofNullable(userFourthHop));

        when(employeeUserRepository.findTeamIdByEmployeeId(Mockito.anyString()))
                .thenReturn(
                        List.of(teamSecondHop.getTeamId().toString()),
                        List.of(teamThirdHop.getTeamId().toString()),
                        List.of(teamFourthHop.getTeamId().toString()));

        when(teamReadonlyRepository.findAllByTeamIdInAndStatusCode(Mockito.any(List.class), Mockito.anyInt()))
                .thenReturn(List.of(teamSecondHop))
                .thenReturn(List.of(teamThirdHop))
                .thenReturn(List.of(teamFourthHop));

        when(stgSlaPerOwnerRepository.findAllByCaseCOrderByStartDateTimeCAsc(tempCaseModel.getSfId())).thenReturn(stgSlaPerOwnerModels);

        when(caseRepository.save(Mockito.any(CaseTransactionModel.class))).thenAnswer(invocation -> {
            CaseTransactionModel input = invocation.getArgument(0);

            // simulate JPA @GeneratedValue
            input.setCaseId(UUID.randomUUID());

            return input;
        });

        when(caseSlaHopRepository.saveAll(Mockito.anyList()))
                .thenAnswer(invocation -> {
                    List<CaseSlaHopModel> inputList = invocation.getArgument(0);
                    for (CaseSlaHopModel item : inputList) {
                        item.setCaseSlaHopId(UUID.randomUUID());
                    }

                    return inputList;
                });

        when(caseSlaActivityRepository.saveAll(Mockito.anyList()))
                .thenAnswer(invocation -> {
                    List<CaseSlaActivity> inputList = invocation.getArgument(0);
                    for (CaseSlaActivity item : inputList) {
                        item.setCaseSlaActivityId(UUID.randomUUID());
                    }

                    return inputList;
                });

        when(kafkaTemplate.send(Mockito.any(Message.class)))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            createCaseService.createCase(tempCaseModel);
        });

        assertEquals("Invalid start date:  at SLA per owner record: 2", exception.getMessage());
    }

    @Test
    void caseResolvedCreatedByEmployeeSameClosedEmployeeAndNotHaveSlaPerOwnerCanMigrationCase() {
        tempCaseModel = mockTempCase.mockTempStgCaseInProgressResolvedCreatedByEmployeeSameClosedEmployee();
        stgSlaPerOwnerModels = null;
        ServiceTypeMatrixModel serviceTypeMatrixModel = mockSTM.mockSTMFixWithoutFCR();
        TeamReadonlyModel createdByTeam = mockTeam.mockTeam(tempCaseModel.getCreatedByTeamNew());
        EmployeeUserModel createByUser = mockEmployeeUser.mockEmployeeUser(tempCaseModel.getCreatedByEmployeeIdC());
        EmployeeUserModel naEmployee = mockEmployeeUser.mockEmployeeUser(Constant.NA_EMPLOYEE_ID);

        when(serviceTypeMatrixRepository.findByServiceTypeMatrixCode(Mockito.anyString()))
                .thenReturn(serviceTypeMatrixModel);

        when(teamReadonlyRepository.findByNameTh(Mockito.anyString()))
                .thenReturn(
                        Optional.ofNullable(createdByTeam));

        when(employeeUserRepository.findByEmployeeId(Mockito.anyString()))
                .thenReturn(
                        Optional.ofNullable(createByUser),
                        Optional.ofNullable(naEmployee));

        when(stgSlaPerOwnerRepository.findAllByCaseCOrderByStartDateTimeCAsc(tempCaseModel.getSfId())).thenReturn(stgSlaPerOwnerModels);

        when(caseRepository.save(Mockito.any(CaseTransactionModel.class))).thenAnswer(invocation -> {
            CaseTransactionModel input = invocation.getArgument(0);

            // simulate JPA @GeneratedValue
            input.setCaseId(UUID.randomUUID());

            return input;
        });

        when(caseSlaHopRepository.saveAll(Mockito.anyList()))
                .thenAnswer(invocation -> {
                    List<CaseSlaHopModel> inputList = invocation.getArgument(0);
                    for (CaseSlaHopModel item : inputList) {
                        item.setCaseSlaHopId(UUID.randomUUID());
                    }

                    return inputList;
                });

        when(caseSlaActivityRepository.saveAll(Mockito.anyList()))
                .thenAnswer(invocation -> {
                    List<CaseSlaActivity> inputList = invocation.getArgument(0);
                    for (CaseSlaActivity item : inputList) {
                        item.setCaseSlaActivityId(UUID.randomUUID());
                    }

                    return inputList;
                });

        when(kafkaTemplate.send(Mockito.any(Message.class)))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        StgToCaseWriterDTO transaction = createCaseService.createCase(tempCaseModel);

        CaseTransactionModel caseModel = transaction.getCaseTransaction();

        // Case
        assertEquals(tempCaseModel.getCaseNumber(), caseModel.getCaseNumber());
        assertEquals(createdByTeam.getTeamId(), caseModel.getCreatedByTeamId());
        assertEquals(createByUser.getUserId(), caseModel.getCreatedById());
        assertNull(caseModel.getTeamId());
        assertNull(caseModel.getTeamName());
        assertNotNull(caseModel.getModifiedById());
        ZonedDateTime createDate = DateTimeUtils.parseToZoneDateTime(tempCaseModel.getCreatedDate());
        ZonedDateTime closedDate = DateTimeUtils.parseToZoneDateTime(tempCaseModel.getResolvedDateTimeC());
        assertEquals(createDate, caseModel.getCreatedOn());
        assertNotNull(caseModel.getModifiedOn());
        assertEquals(Constant.CASE_STATUS_COMPLETED, caseModel.getCaseStatusCode());
        assertEquals(Constant.TEP, caseModel.getIntegrationSystem());
        assertEquals(1, caseModel.getActiveHopNumber());
        assertEquals(1.75, caseModel.getTotalDuration(), 0.0000001);

        // Case Resolved Info
        assertNull(caseModel.getResolvedBy());
        assertNull(caseModel.getResolvedDate());
        assertNull(caseModel.getResolvedById());
        assertNull(caseModel.getResolvedByTeamId());

        // Case Closed Info
        assertEquals(closedDate.plusNanos(1_000_000L), caseModel.getClosedDate());
        assertEquals(createByUser.getUserId(), caseModel.getClosedById());
        assertEquals(createByUser.getFullNameTH(), caseModel.getClosedBy());
        assertEquals(createdByTeam.getTeamId(), caseModel.getClosedByTeamId());

        // Service type matrix
        assertNotNull(serviceTypeMatrixModel);
        assertEquals(serviceTypeMatrixModel.getServiceTypeMatrixCode(), caseModel.getServiceTypeMatrixCode());
        assertEquals(serviceTypeMatrixModel.getServiceTypeMatrixType(), caseModel.getServiceTypeMatrixType());
        assertEquals(serviceTypeMatrixModel.getSla(), caseModel.getSla());
        assertEquals(serviceTypeMatrixModel.getFcr(), caseModel.getFcr());
        assertEquals(ServiceTypeMatrixTypeEnum.FIX, caseModel.getServiceTypeMatrixType());

        // Case Sla hop
        List<CaseSlaHopModel> caseSlas = caseModel.getSlaHop();
        assertNotNull(caseSlas);
        assertEquals(1, caseSlas.size());

        // First case sla hop
        CaseSlaHopModel firstHop = caseSlas.getFirst();
        assertNull(firstHop.getCaseSlaHopId());
        assertEquals(createDate, firstHop.getStartDatetime());
        assertEquals(closedDate, firstHop.getEndDatetime());
        assertEquals(createByUser.getUserId(), firstHop.getOwnerId());
        assertEquals(createByUser.getFullNameTH(), firstHop.getOwnerName());
        assertEquals(createdByTeam.getTeamId(), firstHop.getTeamId());
        assertEquals(createdByTeam.getNameTh(), firstHop.getTeamName());
        assertEquals(createDate, firstHop.getCreatedOn());
        assertEquals(closedDate, firstHop.getModifiedOn());
        assertEquals(createByUser.getUserId(), firstHop.getCreatedById());
        assertEquals(createByUser.getUserId(), firstHop.getModifiedById());
        assertEquals(1, firstHop.getHopNumber());
        assertEquals(0, firstHop.getTotalDuration());
        assertEquals(true, firstHop.getCloseByBu());
        assertNull(firstHop.getSlaTarget());
        assertNull(firstHop.getSlaTargetDate());
        assertEquals(0.0, firstHop.getTotalDuration(), 0.0000001);
        assertEquals(createdByTeam.getNameTh(), firstHop.getTeamName());
        assertEquals(createdByTeam.getTeamId(), firstHop.getTeamId());
        assertEquals(caseModel.getCaseId(), firstHop.getCases().getCaseId());

        // Case sla activities
        List<CaseSlaActivity> caseSlaActivities = transaction.getCaseTransaction().getCaseSlaActivities();
        assertNotNull(caseSlaActivities);
        assertEquals(2, caseSlaActivities.size());

        // First sla activity
        CaseSlaActivity firstActivity = caseSlaActivities.getFirst();
        assertNull(firstActivity.getCaseSlaActivityId());
        assertEquals(createByUser.getUserId(), firstActivity.getOwnerId());
        assertEquals(createByUser.getFullNameTH(), firstActivity.getOwnerName());
        assertEquals(createdByTeam.getTeamId(), firstActivity.getTeamId());
        assertEquals(createdByTeam.getNameTh(), firstActivity.getTeamNameTh());
        assertEquals(createByUser.getFullNameTH(), firstActivity.getCreatedByName());
        assertEquals(createByUser.getFullNameTH(), firstActivity.getModifiedByName());
        assertEquals(createByUser.getUserId(), firstActivity.getCreatedById());
        assertEquals(createByUser.getUserId(), firstActivity.getModifiedById());
        assertEquals(createDate, firstActivity.getStartDate());
        assertEquals(createDate, firstActivity.getEndDate());
        assertEquals(createDate, firstActivity.getCreatedOn());
        assertEquals(createDate, firstActivity.getModifiedOn());
        assertEquals(caseModel.getCaseNumber(), firstActivity.getCases().getCaseNumber());
        assertEquals(1, firstActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.CREATE, firstActivity.getAction());
        assertEquals(1.75, firstActivity.getActualDuration(), 0.0000001);

        // Second sla activity
        CaseSlaActivity secondActivity = caseSlaActivities.get(1);
        assertNull(secondActivity.getCaseSlaActivityId());
        assertEquals(createByUser.getUserId(), secondActivity.getOwnerId());
        assertEquals(createByUser.getFullNameTH(), secondActivity.getOwnerName());
        assertEquals(createdByTeam.getTeamId(), secondActivity.getTeamId());
        assertEquals(createdByTeam.getNameTh(), secondActivity.getTeamNameTh());
        assertEquals(createByUser.getFullNameTH(), secondActivity.getCreatedByName());
        assertEquals(createByUser.getFullNameTH(), secondActivity.getModifiedByName());
        assertEquals(createByUser.getUserId(), secondActivity.getCreatedById());
        assertEquals(createByUser.getUserId(), secondActivity.getModifiedById());
        assertEquals(closedDate.plusNanos(1_000_000L), secondActivity.getStartDate());
        assertEquals(closedDate.plusNanos(1_000_000L), secondActivity.getEndDate());
        assertEquals(closedDate.plusNanos(1_000_000L), secondActivity.getCreatedOn());
        assertEquals(closedDate.plusNanos(1_000_000L), secondActivity.getModifiedOn());
        assertEquals(1.75, secondActivity.getActualDuration(), 0.0000001);
        assertEquals(caseModel.getCaseNumber(), secondActivity.getCases().getCaseNumber());
        assertEquals(1, secondActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.COMPLETED, secondActivity.getAction());

        // last modify data is same in case and last activity
        assertEquals(secondActivity.getModifiedOn(), caseModel.getModifiedOn());
        assertEquals(secondActivity.getModifiedById(), caseModel.getModifiedById());
    }
}
