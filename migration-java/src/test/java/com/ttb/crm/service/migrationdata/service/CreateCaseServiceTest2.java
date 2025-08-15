package com.ttb.crm.service.migrationdata.service;

import com.ttb.crm.lib.crmssp_common_utils_lib.excel.ExportExcelServiceUtil;
import com.ttb.crm.lib.crmssp_common_utils_lib.exception.ValidationException;
import com.ttb.crm.lib.crmssp_common_utils_lib.notification.EmailServiceUtils;
import com.ttb.crm.lib.crmssp_encrypt_decrypt_lib.CrmEncryptKeyService;
import com.ttb.crm.lib.crmssp_encrypt_decrypt_lib.bean.RetrieveEncryptKeyResponse;
import com.ttb.crm.service.migrationdata.bean.CreateCaseDTO;
import com.ttb.crm.service.migrationdata.bean.StgToCaseWriterDTO;
import com.ttb.crm.service.migrationdata.enums.CaseSlaActivityAction;
import com.ttb.crm.service.migrationdata.helper.Constant;
import com.ttb.crm.service.migrationdata.helper.MockEmployeeUser;
import com.ttb.crm.service.migrationdata.helper.MockSTM;
import com.ttb.crm.service.migrationdata.helper.MockTeam;
import com.ttb.crm.service.migrationdata.helper.MockTempCase;
import com.ttb.crm.service.migrationdata.helper.MockTempDocumentReference;
import com.ttb.crm.service.migrationdata.helper.MockTempSla;
import com.ttb.crm.service.migrationdata.model.batch.TempStgCaseDocumentReferenceLogModel;
import com.ttb.crm.service.migrationdata.model.caseManagement.CaseDocumentReferenceModel;
import com.ttb.crm.service.migrationdata.model.caseManagement.CaseSlaActivity;
import com.ttb.crm.service.migrationdata.model.caseManagement.CaseSlaHopModel;
import com.ttb.crm.service.migrationdata.model.caseManagement.CaseTransactionModel;
import com.ttb.crm.service.migrationdata.model.masterManagement.MasterDataModel;
import com.ttb.crm.service.migrationdata.model.masterManagement.MasterGroupModel;
import com.ttb.crm.service.migrationdata.model.masterManagement.ServiceTypeMatrixModel;
import com.ttb.crm.service.migrationdata.model.masterManagement.ServiceTypeMatrixSla;
import com.ttb.crm.service.migrationdata.model.secondary.StgCaseInProgressModel;
import com.ttb.crm.service.migrationdata.model.secondary.StgSlaPerOwnerModel;
import com.ttb.crm.service.migrationdata.model.userManagement.EmployeeUserModel;
import com.ttb.crm.service.migrationdata.model.userManagement.TeamReadonlyModel;
import com.ttb.crm.service.migrationdata.repository.batch.TempStgCaseDocumentReferenceLogRepository;
import com.ttb.crm.service.migrationdata.repository.caseManagement.CaseDocumentReferenceRepository;
import com.ttb.crm.service.migrationdata.repository.caseManagement.CaseSlaActivityRepository;
import com.ttb.crm.service.migrationdata.repository.caseManagement.CaseSlaHopRepository;
import com.ttb.crm.service.migrationdata.repository.caseManagement.CaseTransactionRepository;
import com.ttb.crm.service.migrationdata.repository.masterManagement.HolidayRepository;
import com.ttb.crm.service.migrationdata.repository.masterManagement.MasterDataRepository;
import com.ttb.crm.service.migrationdata.repository.masterManagement.MasterGroupRepository;
import com.ttb.crm.service.migrationdata.repository.masterManagement.ServiceTypeMatrixRepository;
import com.ttb.crm.service.migrationdata.repository.secondary.StgSlaPerOwnerRepository;
import com.ttb.crm.service.migrationdata.repository.userManagement.EmployeeUserRepository;
import com.ttb.crm.service.migrationdata.repository.userManagement.TeamReadonlyRepository;
import com.ttb.crm.service.migrationdata.service.preparecaesdataservice.CreateCaseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.ttb.crm.service.migrationdata.helper.DateTimeUtils.parseToZoneDateTime;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@ComponentScan(
        basePackages = "com.ttb.crm",
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = ExportExcelServiceUtil.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = EmailServiceUtils.class)
        }
)
class CreateCaseServiceTest2 {

    @MockitoBean
    private CacheManager cacheManager;

    @Autowired
    private CreateCaseService createCaseService;

    @Autowired
    private MockTempCase mockTempCase;

    @Autowired
    private MockSTM mockSTM;

    @Autowired
    private MockTeam mockTeam;

    @Autowired
    private MockTempSla mockTempSla;

    @Autowired
    private MockEmployeeUser mockEmployeeUser;

    @Autowired
    private MockTempDocumentReference mockTempDocumentReference;

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
    private CaseDocumentReferenceRepository caseDocumentReferenceRepository;

    @MockitoBean
    private HolidayRepository holidayRepository;

    @MockitoBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @MockitoBean
    private MasterDataRepository masterDataRepository;

    @MockitoBean
    private MasterGroupRepository masterGroupRepository;

    @MockitoBean
    private TempStgCaseDocumentReferenceLogRepository tempStgCaseDocumentReferenceLogRepository;

    private StgCaseInProgressModel tempCaseModel;
    private List<StgSlaPerOwnerModel> stgSlaPerOwnerModel;
    private EmployeeUserModel naUser;
    private TeamReadonlyModel systemTeam;

    private void assertDefaultCase(CaseTransactionModel caseModel) {
        assertNull(caseModel.getCaseId());
        assertNotNull(caseModel.getCaseNumber());
        assertNotNull(caseModel.getCreatedByTeamId());
        assertNotNull(caseModel.getCreatedById());
        assertNotNull(caseModel.getModifiedById());
        assertNotNull(caseModel.getCreatedOn());
        assertNotNull(caseModel.getModifiedOn());
    }

    private void assertDefaultCaseSlaActivity(CaseSlaActivity activity) {
        assertNull(activity.getCaseSlaActivityId());
        assertNotNull(activity.getCreatedOn());
        assertNotNull(activity.getModifiedOn());
        assertNotNull(activity.getCreatedById());
        assertNotNull(activity.getModifiedById());
        assertNotNull(activity.getTeamId());
        assertNotNull(activity.getTeamNameTh());
        assertNotNull(activity.getStartDate());
    }

    private void assertCaseAlreadyInProgressWithClosedInfo(CaseTransactionModel caseModel) {
        // Case Closed Info
        assertNull(caseModel.getClosedBy());
        assertNull(caseModel.getClosedDate());
        assertNull(caseModel.getClosedById());
        assertNull(caseModel.getClosedByTeamId());
    }

    private void assertCaseAlreadyInProgressWithResolveInfo(CaseTransactionModel caseModel) {
        // Case Resolved Info
        assertNull(caseModel.getResolvedBy());
        assertNull(caseModel.getResolvedDate());
        assertNull(caseModel.getResolvedById());
        assertNull(caseModel.getResolvedByTeamId());
    }

    private void assertServiceTypeMatrix(ServiceTypeMatrixModel stm, CaseTransactionModel caseModel) {
        // Service type matrix
        assertEquals(stm.getServiceTypeMatrixCode(), caseModel.getServiceTypeMatrixCode());
        assertEquals(stm.getServiceTypeMatrixType(), caseModel.getServiceTypeMatrixType());
        assertEquals(stm.getSla(), caseModel.getSla());
        assertEquals(stm.getFcr(), caseModel.getFcr());
        assertEquals(stm.getServiceTypeMatrixType(), caseModel.getServiceTypeMatrixType());
    }

    @BeforeEach
    void setUp() {
        stgSlaPerOwnerModel = new ArrayList<>();
        tempCaseModel = mockTempCase.mockTempStgCaseInProgress();

        stgSlaPerOwnerModel.add(mockTempSla.mockTempStgSlaPerOwnerCompleted(
                "500RC00000hs5UiSEJ", "Inbound Voice Team 1", "Thanakorn Chaiyasit", "bu", "2025-06-04 17:18:13", "2025-06-04 17:18:14", "New"
        ));
        stgSlaPerOwnerModel.add(mockTempSla.mockTempStgSlaPerOwnerCompleted(
                "500RC00000hs5UiSEJ", "Inbound Voice Team 8", "Jakrapan Sangkharat", "middle", "2025-06-04 17:18:14", "2025-06-07 04:51:11", "In progress"
        ));
        stgSlaPerOwnerModel.add(mockTempSla.mockTempStgSlaPerOwnerCompleted(
                "500RC00000hs5UiSEJ", "Investment Line", "Sukjai Sandee", "owner", "2025-06-07 04:51:11", "", "In progress"
        ));

        RetrieveEncryptKeyResponse response = new RetrieveEncryptKeyResponse();
        response.setOwnerName("TEST");
        response.setEncryptKey("WG3+YMfeP1pdDYGuJfxzlOUhsYInK1WV2m+MMB/KcfI=");
        response.setEncryptFieldInitialVector("BW1+YMfeP1pdDYGuJfxzlOUhsYInK1WV2m+MMB/Kcrm=");
        response.setEncryptFieldKey("4e5f6d7c8b9a0d1e2f3c4b5a6d7e8f9a");

        when(crmEncryptKeyService.getEncryptionKey(anyString())).thenReturn(response);

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

        when(caseDocumentReferenceRepository.saveAll(Mockito.anyList()))
                .thenAnswer(inv -> {
                    List<CaseDocumentReferenceModel> inputList = inv.getArgument(0);
                    for (CaseDocumentReferenceModel item : inputList) {
                        item.setDocumentReferenceId(UUID.randomUUID());
                    }
                    return inputList;
                });

        when(kafkaTemplate.send(Mockito.any(Message.class)))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        when(holidayRepository.findByStatusCode(0)).thenReturn(Optional.of(List.of()));

        naUser = mockEmployeeUser.mockEmployeeUser(Constant.NA_EMPLOYEE_ID);
        naUser.setUserId(UUID.fromString("98548809-b693-49b5-9ccd-b1c74b128f23"));
        when(employeeUserRepository.findByEmployeeId(Constant.NA_EMPLOYEE_ID))
                .thenReturn(Optional.of(naUser));
        systemTeam = mockTeam.mockTeam(Constant.SYSTEM_TEAM);
        when(teamReadonlyRepository.findByNameTh(Constant.SYSTEM_TEAM))
                .thenReturn(Optional.of(systemTeam));
        EmployeeUserModel systemUser = mockEmployeeUser.mockEmployeeUser(Constant.SYSTEM_EMPLOYEE_ID);
        systemUser.setUserId(UUID.fromString("c358ef57-c45b-40ab-9893-975df0acb513"));
        when(employeeUserRepository.findByEmployeeId(Constant.SYSTEM_EMPLOYEE_ID))
                .thenReturn(Optional.of(systemUser));
        when(employeeUserRepository.findByUserId(Objects.requireNonNull(systemUser.getUserId())))
                .thenReturn(Optional.of(systemUser));

        when(cacheManager.getCache(anyString())).thenReturn(mock(Cache.class));
    }

    @Test
    void caseInProgressWithSTMTypeFixAndCanMigrationWithoutDocumentReference() {

        ServiceTypeMatrixModel serviceTypeMatrixModel = mockSTM.mockSTMFixWithoutFCR();
        when(serviceTypeMatrixRepository.findByServiceTypeMatrixCode(Mockito.anyString()))
                .thenReturn(serviceTypeMatrixModel);

        TeamReadonlyModel createdByTeam = mockTeam.mockTeam(tempCaseModel.getCreatedByTeamNew());
        when(teamReadonlyRepository.findByNameTh(anyString()))
                .thenAnswer(inv -> {
                    String teamName = inv.getArgument(0);
                    if (teamName.equals(tempCaseModel.getCreatedByTeamNew())) {
                        return Optional.of(createdByTeam);
                    }
                    return Optional.of(mockTeam.mockTeam(teamName));
                });

        EmployeeUserModel createdByUser = mockEmployeeUser.mockEmployeeUser(tempCaseModel.getCreatedByEmployeeIdC());
        when(employeeUserRepository.findByEmployeeId(anyString()))
                .thenAnswer(inv -> {
                    String employeeId = inv.getArgument(0);
                    if (employeeId.equals(tempCaseModel.getCreatedByEmployeeIdC())) {
                        return Optional.of(createdByUser);
                    }
                    return Optional.of(mockEmployeeUser.mockEmployeeUser(employeeId));
                });

        when(stgSlaPerOwnerRepository.findAllByCaseCOrderByStartDateTimeCAsc(anyString()))
                .thenReturn(stgSlaPerOwnerModel);

        when(employeeUserRepository.findTeamIdByEmployeeId(stgSlaPerOwnerModel.get(1).getEmployeeIdC()))
                .thenReturn(List.of(serviceTypeMatrixModel.getServiceTypeMatrixSlas().get(1).getResponsibleBuId().toString()));

        when(employeeUserRepository.findTeamIdByEmployeeId(stgSlaPerOwnerModel.get(2).getEmployeeIdC()))
                .thenReturn(List.of(serviceTypeMatrixModel.getServiceTypeMatrixSlas().get(2).getResponsibleBuId().toString()));

        when(holidayRepository.findByStatusCode(0))
                .thenReturn(Optional.of(List.of()));

        String ownerSecondHop = stgSlaPerOwnerModel.get(1).getEmployeeIdC();
        String ownerThirdHop = stgSlaPerOwnerModel.get(2).getEmployeeIdC();

        EmployeeUserModel user55515 = mockEmployeeUser.mockEmployeeUser(ownerSecondHop);
        EmployeeUserModel user99902 = mockEmployeeUser.mockEmployeeUser(ownerThirdHop);

        when(employeeUserRepository.findByEmployeeIdAndStatusCode(ownerSecondHop, Constant.ACTIVE_STATUS_CODE))
                .thenReturn(Optional.of(user55515));

        when(employeeUserRepository.findByEmployeeIdAndStatusCode(ownerThirdHop, Constant.ACTIVE_STATUS_CODE))
                .thenReturn(Optional.of(user99902));

        when(employeeUserRepository.findByUserId(Objects.requireNonNull(createdByUser.getUserId())))
                .thenReturn(Optional.of(createdByUser));

        when(employeeUserRepository.findByUserId(Objects.requireNonNull(user55515.getUserId())))
                .thenReturn(Optional.of(user55515));

        when(employeeUserRepository.findByUserId(Objects.requireNonNull(user99902.getUserId())))
                .thenReturn(Optional.of(user99902));

        when(tempStgCaseDocumentReferenceLogRepository.findAllByCaseC(anyString()))
                .thenReturn(List.of());

        StgToCaseWriterDTO transaction = createCaseService.createCase(tempCaseModel);

        CaseTransactionModel caseModel = transaction.getCaseTransaction();

        // Validate data mock not null
        assertNotNull(createdByTeam);
        assertNotNull(createdByUser);
        // Case
        assertDefaultCase(caseModel);
        assertEquals(tempCaseModel.getCaseNumber(), caseModel.getCaseNumber());
        assertEquals(createdByTeam.getTeamId(), caseModel.getCreatedByTeamId());
        assertEquals(createdByUser.getUserId(), caseModel.getCreatedById());
        assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), caseModel.getCreatedOn());
        assertEquals(Constant.CASE_STATUS_IN_PROGRESS, caseModel.getCaseStatusCode());
        assertEquals(Constant.TEP, caseModel.getIntegrationSystem());

        assertCaseAlreadyInProgressWithResolveInfo(caseModel);
        assertCaseAlreadyInProgressWithClosedInfo(caseModel);
        assertNotNull(serviceTypeMatrixModel);
        assertServiceTypeMatrix(serviceTypeMatrixModel, caseModel);

        // Case Sla hop
        List<ServiceTypeMatrixSla> slas = serviceTypeMatrixModel.getServiceTypeMatrixSlas();
        List<CaseSlaHopModel> caseSlas = caseModel.getSlaHop();
        assertNotNull(caseSlas);
        assertEquals(slas.size(), caseSlas.size());

        // First case sla hop
        CaseSlaHopModel firstHop = caseSlas.getFirst();
        assertNull(firstHop.getCaseSlaHopId());
        assertNotNull(firstHop.getTeamId());
        assertNotNull(firstHop.getTeamName());
        assertNotNull(firstHop.getSlaTarget());
        assertNotNull(firstHop.getSlaTargetDate());
        assertNotNull(firstHop.getCloseByBu());
        assertNotNull(firstHop.getHopNumber());
        assertNotNull(firstHop.getStartDatetime());
        assertNotNull(firstHop.getEndDatetime());
        assertNotNull(firstHop.getTotalDuration());
        assertNotNull(firstHop.getOwnerId());
        assertNotNull(firstHop.getOwnerName());
        assertNotNull(firstHop.getCreatedOn());
        assertNotNull(firstHop.getCreatedById());
        assertNotNull(firstHop.getModifiedOn());
        assertNotNull(firstHop.getModifiedById());
        assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), firstHop.getStartDatetime());
        assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), firstHop.getEndDatetime());
        assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), firstHop.getCreatedOn());
        assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), firstHop.getModifiedOn());
        assertEquals(slas.getFirst().getHopNumber(), firstHop.getHopNumber());
        assertEquals(0, firstHop.getTotalDuration());
        assertEquals(slas.getFirst().getCloseByBu(), firstHop.getCloseByBu());
        assertEquals(slas.getFirst().getSlaTarget(), firstHop.getSlaTarget());
        assertEquals(createdByTeam.getNameTh(), firstHop.getTeamName());
        assertEquals(createdByTeam.getTeamId(), firstHop.getTeamId());
        assertEquals(caseModel.getCaseId(), firstHop.getCases().getCaseId());

        // Second Case Sla hop
        CaseSlaHopModel secondHop = caseSlas.get(1);
        assertNull(secondHop.getCaseSlaHopId());
        assertNotNull(secondHop.getTeamId());
        assertNotNull(secondHop.getTeamName());
        assertNotNull(secondHop.getSlaTarget());
        assertNotNull(secondHop.getSlaTargetDate());
        assertNotNull(secondHop.getCloseByBu());
        assertNotNull(secondHop.getHopNumber());
        assertNotNull(secondHop.getStartDatetime());
        assertNotNull(secondHop.getEndDatetime());
        assertNotNull(secondHop.getTotalDuration());
        assertNotNull(secondHop.getOwnerId());
        assertNotNull(secondHop.getOwnerName());
        assertNotNull(secondHop.getCreatedOn());
        assertNotNull(secondHop.getCreatedById());
        assertNotNull(secondHop.getModifiedOn());
        assertNotNull(secondHop.getModifiedById());
        assertEquals(firstHop.getStartDatetime(), secondHop.getStartDatetime()
        );
        assertEquals(parseToZoneDateTime(
                        stgSlaPerOwnerModel.get(1).getEndDateTimeC()
                ),
                secondHop.getEndDatetime()
        );
        assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), secondHop.getCreatedOn());
        assertEquals(parseToZoneDateTime(stgSlaPerOwnerModel.get(1).getEndDateTimeC()), secondHop.getModifiedOn());
        assertEquals(slas.get(1).getHopNumber(), secondHop.getHopNumber());
        assertEquals(slas.get(1).getCloseByBu(), secondHop.getCloseByBu());
        assertEquals(slas.get(1).getSlaTarget(), secondHop.getSlaTarget());
        assertEquals(slas.get(1).getResponsibleBu(), secondHop.getTeamName());
        assertEquals(slas.get(1).getResponsibleBuId(), secondHop.getTeamId());
        assertEquals(caseModel.getCaseId(), secondHop.getCases().getCaseId());

        // Third(last) Case sla hop
        CaseSlaHopModel thirdHop = caseSlas.getLast();
        assertNull(thirdHop.getCaseSlaHopId());
        assertNotNull(thirdHop.getTeamId());
        assertNotNull(thirdHop.getTeamName());
        assertNotNull(thirdHop.getSlaTarget());
        assertNotNull(thirdHop.getSlaTargetDate());
        assertNotNull(thirdHop.getCloseByBu());
        assertNotNull(thirdHop.getHopNumber());
        assertNotNull(thirdHop.getStartDatetime());
        assertNotNull(thirdHop.getOwnerId());
        assertNotNull(thirdHop.getOwnerName());
        assertNotNull(thirdHop.getCreatedOn());
        assertNotNull(thirdHop.getCreatedById());
        assertNotNull(thirdHop.getModifiedOn());
        assertNotNull(thirdHop.getModifiedById());
        assertNull(thirdHop.getTotalDuration());
        assertNull(thirdHop.getEndDatetime());
        assertEquals(parseToZoneDateTime(stgSlaPerOwnerModel.getLast().getStartDateTimeC()), thirdHop.getStartDatetime());
        assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), thirdHop.getCreatedOn());
        assertEquals(parseToZoneDateTime(stgSlaPerOwnerModel.getLast().getStartDateTimeC()), thirdHop.getModifiedOn());
        assertEquals(slas.getLast().getHopNumber(), thirdHop.getHopNumber());
        assertEquals(slas.getLast().getCloseByBu(), thirdHop.getCloseByBu());
        assertEquals(slas.getLast().getSlaTarget(), thirdHop.getSlaTarget());
        assertEquals(slas.getLast().getResponsibleBu(), thirdHop.getTeamName());
        assertEquals(slas.getLast().getResponsibleBuId(), thirdHop.getTeamId());
        assertEquals(caseModel.getCaseId(), thirdHop.getCases().getCaseId());

        // Case sla activities
        List<CaseSlaActivity> caseSlaActivities = transaction.getCaseTransaction().getCaseSlaActivities();
        assertNotNull(caseSlaActivities);
        assertEquals(7, caseSlaActivities.size());

        // First sla activity
        CaseSlaActivity firstActivity = caseSlaActivities.getFirst();
        assertDefaultCaseSlaActivity(firstActivity);
        assertNotNull(firstActivity.getOwnerId());
        assertNotNull(firstActivity.getOwnerName());
        assertNotNull(firstActivity.getEndDate());
        assertNotNull(firstActivity.getCreatedByName());
        assertNotNull(firstActivity.getModifiedByName());
        assertNotNull(firstActivity.getActualDuration());
        assertEquals(firstHop.getCreatedById(), firstActivity.getCreatedById());
        assertEquals(firstHop.getModifiedById(), firstActivity.getModifiedById());
        assertEquals(firstHop.getOwnerId(), firstActivity.getOwnerId());
        assertEquals(firstHop.getOwnerName(), firstActivity.getOwnerName());
        assertEquals(firstHop.getTeamId(), firstActivity.getTeamId());
        assertEquals(firstHop.getTeamName(), firstActivity.getTeamNameTh());
        assertEquals(firstHop.getHopNumber(), firstActivity.getHopNumberRef());
        assertEquals(caseModel.getCaseNumber(), firstActivity.getCases().getCaseNumber());
        assertEquals(0.0, firstActivity.getActualDuration(), 0.0000001);
        assertEquals(CaseSlaActivityAction.CREATE, firstActivity.getAction());

        // Second sla activity
        CaseSlaActivity secondActivity = caseSlaActivities.get(1);
        assertDefaultCaseSlaActivity(secondActivity);
        assertNotNull(secondActivity.getOwnerId());
        assertNotNull(secondActivity.getOwnerName());
        assertNotNull(secondActivity.getEndDate());
        assertNull(secondActivity.getActualDuration());
        assertEquals(firstHop.getCreatedById(), secondActivity.getCreatedById());
        assertEquals(firstHop.getModifiedById(), secondActivity.getModifiedById());
        assertEquals(firstHop.getOwnerId(), secondActivity.getOwnerId());
        assertEquals(firstHop.getOwnerName(), secondActivity.getOwnerName());
        assertEquals(firstHop.getTeamId(), secondActivity.getTeamId());
        assertEquals(firstHop.getTeamName(), secondActivity.getTeamNameTh());
        assertEquals(firstHop.getHopNumber(), secondActivity.getHopNumberRef());
        assertEquals(caseModel.getCaseNumber(), secondActivity.getCases().getCaseNumber());
        assertEquals(CaseSlaActivityAction.NEXT_HOP, secondActivity.getAction());

        // Third sla activity
        CaseSlaActivity thirdActivity = caseSlaActivities.get(2);
        assertDefaultCaseSlaActivity(thirdActivity);
        assertNotNull(thirdActivity.getEndDate());
        assertNotNull(thirdActivity.getActualDuration());
        assertNull(thirdActivity.getOwnerId());
        assertNull(thirdActivity.getOwnerName());
        assertEquals(firstHop.getOwnerId(), thirdActivity.getCreatedById());
        assertEquals(secondHop.getOwnerId(), thirdActivity.getModifiedById());
        assertEquals(secondHop.getTeamId(), thirdActivity.getTeamId());
        assertEquals(secondHop.getTeamName(), thirdActivity.getTeamNameTh());
        assertEquals(secondHop.getHopNumber(), thirdActivity.getHopNumberRef());
        assertEquals(caseModel.getCaseNumber(), thirdActivity.getCases().getCaseNumber());
        assertEquals(CaseSlaActivityAction.ASSIGN_TEAM, thirdActivity.getAction());

        // Fourth sla activity
        CaseSlaActivity fourthActivity = caseSlaActivities.get(3);
        assertDefaultCaseSlaActivity(fourthActivity);
        assertNotNull(fourthActivity.getEndDate());
        assertNotNull(fourthActivity.getActualDuration());
        assertNotNull(fourthActivity.getOwnerId());
        assertNotNull(fourthActivity.getOwnerName());
        assertEquals(secondHop.getOwnerId(), fourthActivity.getCreatedById());
        assertEquals(secondHop.getOwnerId(), fourthActivity.getModifiedById());
        assertEquals(secondHop.getTeamId(), fourthActivity.getTeamId());
        assertEquals(secondHop.getTeamName(), fourthActivity.getTeamNameTh());
        assertEquals(secondHop.getHopNumber(), fourthActivity.getHopNumberRef());
        assertEquals(caseModel.getCaseNumber(), fourthActivity.getCases().getCaseNumber());
        assertEquals(secondHop.getHopNumber(), fourthActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.ASSIGN_OWNER, fourthActivity.getAction());

        // Fifth sla activity
        CaseSlaActivity fifthActivity = caseSlaActivities.get(4);
        assertDefaultCaseSlaActivity(fifthActivity);
        assertNotNull(fifthActivity.getEndDate());
        assertNotNull(fifthActivity.getOwnerId());
        assertNotNull(fifthActivity.getOwnerName());
        assertNull(fifthActivity.getActualDuration());
        assertEquals(secondHop.getOwnerId(), fifthActivity.getCreatedById());
        assertEquals(secondHop.getOwnerId(), fifthActivity.getModifiedById());
        assertEquals(secondHop.getTeamId(), fifthActivity.getTeamId());
        assertEquals(secondHop.getTeamName(), fifthActivity.getTeamNameTh());
        assertEquals(secondHop.getHopNumber(), fifthActivity.getHopNumberRef());
        assertEquals(secondHop.getHopNumber(), fifthActivity.getHopNumberRef());
        assertEquals(caseModel.getCaseNumber(), fifthActivity.getCases().getCaseNumber());
        assertEquals(CaseSlaActivityAction.NEXT_HOP, fifthActivity.getAction());

        // Sixth sla activity
        CaseSlaActivity sixthActivity = caseSlaActivities.get(5);
        assertDefaultCaseSlaActivity(sixthActivity);
        assertNull(sixthActivity.getEndDate());
        assertNull(sixthActivity.getActualDuration());
        assertNull(sixthActivity.getOwnerId());
        assertNull(sixthActivity.getOwnerName());
        assertEquals(secondHop.getOwnerId(), sixthActivity.getCreatedById());
        assertEquals(secondHop.getOwnerId(), sixthActivity.getModifiedById());
        assertEquals(thirdHop.getTeamId(), sixthActivity.getTeamId());
        assertEquals(thirdHop.getTeamName(), sixthActivity.getTeamNameTh());
        assertEquals(thirdHop.getHopNumber(), sixthActivity.getHopNumberRef());
        assertEquals(caseModel.getCaseNumber(), sixthActivity.getCases().getCaseNumber());
        assertEquals(CaseSlaActivityAction.ASSIGN_TEAM, sixthActivity.getAction());

        // Seventh sla activity
        CaseSlaActivity seventhActivity = caseSlaActivities.get(6);
        assertDefaultCaseSlaActivity(sixthActivity);
        assertNotNull(seventhActivity.getOwnerId());
        assertNotNull(seventhActivity.getOwnerName());
        assertNull(seventhActivity.getActualDuration());
        assertNull(seventhActivity.getEndDate());
        assertEquals(thirdHop.getOwnerId(), seventhActivity.getCreatedById());
        assertEquals(thirdHop.getOwnerId(), seventhActivity.getModifiedById());
        assertEquals(thirdHop.getTeamId(), seventhActivity.getTeamId());
        assertEquals(thirdHop.getTeamName(), seventhActivity.getTeamNameTh());
        assertEquals(thirdHop.getHopNumber(), seventhActivity.getHopNumberRef());
        assertEquals(caseModel.getCaseNumber(), seventhActivity.getCases().getCaseNumber());
        assertEquals(CaseSlaActivityAction.ASSIGN_OWNER, seventhActivity.getAction());
    }

    @Test
    void caseInProgressWithSTMTypeFixAndCanMigrationWithDocumentReference() {

        ServiceTypeMatrixModel serviceTypeMatrixModel = mockSTM.mockSTMFixWithoutFCR();
        when(serviceTypeMatrixRepository.findByServiceTypeMatrixCode(Mockito.anyString()))
                .thenReturn(serviceTypeMatrixModel);

        TeamReadonlyModel createdByTeam = mockTeam.mockTeam(tempCaseModel.getCreatedByTeamNew());
        when(teamReadonlyRepository.findByNameTh(anyString()))
                .thenAnswer(inv -> {
                    String teamName = inv.getArgument(0);
                    if (teamName.equals(tempCaseModel.getCreatedByTeamNew())) {
                        return Optional.of(createdByTeam);
                    }
                    return Optional.of(mockTeam.mockTeam(teamName));
                });

        EmployeeUserModel createdByUser = mockEmployeeUser.mockEmployeeUser(tempCaseModel.getCreatedByEmployeeIdC());
        when(employeeUserRepository.findByEmployeeId(anyString()))
                .thenAnswer(inv -> {
                    String employeeId = inv.getArgument(0);
                    if (employeeId.equals(tempCaseModel.getCreatedByEmployeeIdC())) {
                        return Optional.of(createdByUser);
                    }
                    return Optional.of(mockEmployeeUser.mockEmployeeUser(employeeId));
                });

        when(stgSlaPerOwnerRepository.findAllByCaseCOrderByStartDateTimeCAsc(anyString()))
                .thenReturn(stgSlaPerOwnerModel);

        when(employeeUserRepository.findTeamIdByEmployeeId(stgSlaPerOwnerModel.get(1).getEmployeeIdC()))
                .thenReturn(List.of(serviceTypeMatrixModel.getServiceTypeMatrixSlas().get(1).getResponsibleBuId().toString()));

        when(employeeUserRepository.findTeamIdByEmployeeId(stgSlaPerOwnerModel.get(2).getEmployeeIdC()))
                .thenReturn(List.of(serviceTypeMatrixModel.getServiceTypeMatrixSlas().get(2).getResponsibleBuId().toString()));

        String ownerSecondHop = stgSlaPerOwnerModel.get(1).getEmployeeIdC();
        String ownerThirdHop = stgSlaPerOwnerModel.get(2).getEmployeeIdC();

        EmployeeUserModel user55515 = mockEmployeeUser.mockEmployeeUser(ownerSecondHop);
        EmployeeUserModel user99902 = mockEmployeeUser.mockEmployeeUser(ownerThirdHop);

        when(employeeUserRepository.findByEmployeeIdAndStatusCode(ownerSecondHop, Constant.ACTIVE_STATUS_CODE))
                .thenReturn(Optional.of(user55515));

        when(employeeUserRepository.findByEmployeeIdAndStatusCode(ownerThirdHop, Constant.ACTIVE_STATUS_CODE))
                .thenReturn(Optional.of(user99902));

        when(employeeUserRepository.findByUserId(Objects.requireNonNull(createdByUser.getUserId())))
                .thenReturn(Optional.of(createdByUser));

        when(employeeUserRepository.findByUserId(Objects.requireNonNull(user55515.getUserId())))
                .thenReturn(Optional.of(user55515));

        when(employeeUserRepository.findByUserId(Objects.requireNonNull(user99902.getUserId())))
                .thenReturn(Optional.of(user99902));

        List<TempStgCaseDocumentReferenceLogModel> docRef = mockTempDocumentReference.mockTempDocumentReference(tempCaseModel.getSfId());

        when(tempStgCaseDocumentReferenceLogRepository.findAllByCaseC(anyString()))
                .thenReturn(docRef);

        when(holidayRepository.findByStatusCode(0))
                .thenReturn(Optional.of(List.of()));

        StgToCaseWriterDTO transaction = createCaseService.createCase(tempCaseModel);

        CaseTransactionModel caseModel = transaction.getCaseTransaction();

        // Validate data mock not null
        assertNotNull(createdByTeam);
        assertNotNull(createdByUser);
        // Case
        assertDefaultCase(caseModel);
        assertEquals(tempCaseModel.getCaseNumber(), caseModel.getCaseNumber());
        assertEquals(createdByTeam.getTeamId(), caseModel.getCreatedByTeamId());
        assertEquals(createdByUser.getUserId(), caseModel.getCreatedById());
        assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), caseModel.getCreatedOn());
        assertEquals(Constant.CASE_STATUS_IN_PROGRESS, caseModel.getCaseStatusCode());
        assertEquals(Constant.TEP, caseModel.getIntegrationSystem());

        assertCaseAlreadyInProgressWithResolveInfo(caseModel);
        assertCaseAlreadyInProgressWithClosedInfo(caseModel);
        assertNotNull(serviceTypeMatrixModel);
        assertServiceTypeMatrix(serviceTypeMatrixModel, caseModel);

        // Case Sla hop
        List<ServiceTypeMatrixSla> slas = serviceTypeMatrixModel.getServiceTypeMatrixSlas();
        List<CaseSlaHopModel> caseSlas = caseModel.getSlaHop();
        assertNotNull(caseSlas);
        assertEquals(slas.size(), caseSlas.size());

        // First case sla hop
        CaseSlaHopModel firstHop = caseSlas.getFirst();
        assertNull(firstHop.getCaseSlaHopId());
        assertNotNull(firstHop.getTeamId());
        assertNotNull(firstHop.getTeamName());
        assertNotNull(firstHop.getSlaTarget());
        assertNotNull(firstHop.getSlaTargetDate());
        assertNotNull(firstHop.getCloseByBu());
        assertNotNull(firstHop.getHopNumber());
        assertNotNull(firstHop.getStartDatetime());
        assertNotNull(firstHop.getEndDatetime());
        assertNotNull(firstHop.getTotalDuration());
        assertNotNull(firstHop.getOwnerId());
        assertNotNull(firstHop.getOwnerName());
        assertNotNull(firstHop.getCreatedOn());
        assertNotNull(firstHop.getCreatedById());
        assertNotNull(firstHop.getModifiedOn());
        assertNotNull(firstHop.getModifiedById());
        assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), firstHop.getStartDatetime());
        assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), firstHop.getEndDatetime());
        assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), firstHop.getCreatedOn());
        assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), firstHop.getModifiedOn());
        assertEquals(slas.getFirst().getHopNumber(), firstHop.getHopNumber());
        assertEquals(0, firstHop.getTotalDuration());
        assertEquals(slas.getFirst().getCloseByBu(), firstHop.getCloseByBu());
        assertEquals(slas.getFirst().getSlaTarget(), firstHop.getSlaTarget());
        assertEquals(createdByTeam.getNameTh(), firstHop.getTeamName());
        assertEquals(createdByTeam.getTeamId(), firstHop.getTeamId());
        assertEquals(caseModel.getCaseId(), firstHop.getCases().getCaseId());

        // Second Case Sla hop
        CaseSlaHopModel secondHop = caseSlas.get(1);
        assertNull(secondHop.getCaseSlaHopId());
        assertNotNull(secondHop.getTeamId());
        assertNotNull(secondHop.getTeamName());
        assertNotNull(secondHop.getSlaTarget());
        assertNotNull(secondHop.getSlaTargetDate());
        assertNotNull(secondHop.getCloseByBu());
        assertNotNull(secondHop.getHopNumber());
        assertNotNull(secondHop.getStartDatetime());
        assertNotNull(secondHop.getEndDatetime());
        assertNotNull(secondHop.getTotalDuration());
        assertNotNull(secondHop.getOwnerId());
        assertNotNull(secondHop.getOwnerName());
        assertNotNull(secondHop.getCreatedOn());
        assertNotNull(secondHop.getCreatedById());
        assertNotNull(secondHop.getModifiedOn());
        assertNotNull(secondHop.getModifiedById());
        assertEquals(firstHop.getStartDatetime(), secondHop.getStartDatetime()
        );
        assertEquals(parseToZoneDateTime(
                        stgSlaPerOwnerModel.get(1).getEndDateTimeC()
                ),
                secondHop.getEndDatetime()
        );
        assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), secondHop.getCreatedOn());
        assertEquals(parseToZoneDateTime(stgSlaPerOwnerModel.get(1).getEndDateTimeC()), secondHop.getModifiedOn());
        assertEquals(slas.get(1).getHopNumber(), secondHop.getHopNumber());
        assertEquals(slas.get(1).getCloseByBu(), secondHop.getCloseByBu());
        assertEquals(slas.get(1).getSlaTarget(), secondHop.getSlaTarget());
        assertEquals(slas.get(1).getResponsibleBu(), secondHop.getTeamName());
        assertEquals(slas.get(1).getResponsibleBuId(), secondHop.getTeamId());
        assertEquals(caseModel.getCaseId(), secondHop.getCases().getCaseId());

        // Third(last) Case sla hop
        CaseSlaHopModel thirdHop = caseSlas.getLast();
        assertNull(thirdHop.getCaseSlaHopId());
        assertNotNull(thirdHop.getTeamId());
        assertNotNull(thirdHop.getTeamName());
        assertNotNull(thirdHop.getSlaTarget());
        assertNotNull(thirdHop.getSlaTargetDate());
        assertNotNull(thirdHop.getCloseByBu());
        assertNotNull(thirdHop.getHopNumber());
        assertNotNull(thirdHop.getStartDatetime());
        assertNotNull(thirdHop.getOwnerId());
        assertNotNull(thirdHop.getOwnerName());
        assertNotNull(thirdHop.getCreatedOn());
        assertNotNull(thirdHop.getCreatedById());
        assertNotNull(thirdHop.getModifiedOn());
        assertNotNull(thirdHop.getModifiedById());
        assertNull(thirdHop.getTotalDuration());
        assertNull(thirdHop.getEndDatetime());
        assertEquals(parseToZoneDateTime(stgSlaPerOwnerModel.getLast().getStartDateTimeC()), thirdHop.getStartDatetime());
        assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), thirdHop.getCreatedOn());
        assertEquals(parseToZoneDateTime(stgSlaPerOwnerModel.getLast().getStartDateTimeC()), thirdHop.getModifiedOn());
        assertEquals(slas.getLast().getHopNumber(), thirdHop.getHopNumber());
        assertEquals(slas.getLast().getCloseByBu(), thirdHop.getCloseByBu());
        assertEquals(slas.getLast().getSlaTarget(), thirdHop.getSlaTarget());
        assertEquals(slas.getLast().getResponsibleBu(), thirdHop.getTeamName());
        assertEquals(slas.getLast().getResponsibleBuId(), thirdHop.getTeamId());
        assertEquals(caseModel.getCaseId(), thirdHop.getCases().getCaseId());

        // Case sla activities
        List<CaseSlaActivity> caseSlaActivities = transaction.getCaseTransaction().getCaseSlaActivities();
        assertNotNull(caseSlaActivities);
        assertEquals(7, caseSlaActivities.size());

        // First sla activity
        CaseSlaActivity firstActivity = caseSlaActivities.getFirst();
        assertDefaultCaseSlaActivity(firstActivity);
        assertNotNull(firstActivity.getOwnerId());
        assertNotNull(firstActivity.getOwnerName());
        assertNotNull(firstActivity.getEndDate());
        assertNotNull(firstActivity.getCreatedByName());
        assertNotNull(firstActivity.getModifiedByName());
        assertNotNull(firstActivity.getActualDuration());
        assertEquals(firstHop.getCreatedById(), firstActivity.getCreatedById());
        assertEquals(firstHop.getModifiedById(), firstActivity.getModifiedById());
        assertEquals(firstHop.getOwnerId(), firstActivity.getOwnerId());
        assertEquals(firstHop.getOwnerName(), firstActivity.getOwnerName());
        assertEquals(firstHop.getTeamId(), firstActivity.getTeamId());
        assertEquals(firstHop.getTeamName(), firstActivity.getTeamNameTh());
        assertEquals(firstHop.getHopNumber(), firstActivity.getHopNumberRef());
        assertEquals(caseModel.getCaseNumber(), firstActivity.getCases().getCaseNumber());
        assertEquals(0.0, firstActivity.getActualDuration(), 0.0000001);
        assertEquals(CaseSlaActivityAction.CREATE, firstActivity.getAction());

        // Second sla activity
        CaseSlaActivity secondActivity = caseSlaActivities.get(1);
        assertDefaultCaseSlaActivity(secondActivity);
        assertNotNull(secondActivity.getOwnerId());
        assertNotNull(secondActivity.getOwnerName());
        assertNotNull(secondActivity.getEndDate());
        assertNull(secondActivity.getActualDuration());
        assertEquals(firstHop.getCreatedById(), secondActivity.getCreatedById());
        assertEquals(firstHop.getModifiedById(), secondActivity.getModifiedById());
        assertEquals(firstHop.getOwnerId(), secondActivity.getOwnerId());
        assertEquals(firstHop.getOwnerName(), secondActivity.getOwnerName());
        assertEquals(firstHop.getTeamId(), secondActivity.getTeamId());
        assertEquals(firstHop.getTeamName(), secondActivity.getTeamNameTh());
        assertEquals(firstHop.getHopNumber(), secondActivity.getHopNumberRef());
        assertEquals(caseModel.getCaseNumber(), secondActivity.getCases().getCaseNumber());
        assertEquals(CaseSlaActivityAction.NEXT_HOP, secondActivity.getAction());

        // Third sla activity
        CaseSlaActivity thirdActivity = caseSlaActivities.get(2);
        assertDefaultCaseSlaActivity(thirdActivity);
        assertNotNull(thirdActivity.getEndDate());
        assertNotNull(thirdActivity.getActualDuration());
        assertNull(thirdActivity.getOwnerId());
        assertNull(thirdActivity.getOwnerName());
        assertEquals(firstHop.getOwnerId(), thirdActivity.getCreatedById());
        assertEquals(secondHop.getOwnerId(), thirdActivity.getModifiedById());
        assertEquals(secondHop.getTeamId(), thirdActivity.getTeamId());
        assertEquals(secondHop.getTeamName(), thirdActivity.getTeamNameTh());
        assertEquals(secondHop.getHopNumber(), thirdActivity.getHopNumberRef());
        assertEquals(caseModel.getCaseNumber(), thirdActivity.getCases().getCaseNumber());
        assertEquals(CaseSlaActivityAction.ASSIGN_TEAM, thirdActivity.getAction());

        // Fourth sla activity
        CaseSlaActivity fourthActivity = caseSlaActivities.get(3);
        assertDefaultCaseSlaActivity(fourthActivity);
        assertNotNull(fourthActivity.getEndDate());
        assertNotNull(fourthActivity.getActualDuration());
        assertNotNull(fourthActivity.getOwnerId());
        assertNotNull(fourthActivity.getOwnerName());
        assertEquals(secondHop.getOwnerId(), fourthActivity.getCreatedById());
        assertEquals(secondHop.getOwnerId(), fourthActivity.getModifiedById());
        assertEquals(secondHop.getTeamId(), fourthActivity.getTeamId());
        assertEquals(secondHop.getTeamName(), fourthActivity.getTeamNameTh());
        assertEquals(secondHop.getHopNumber(), fourthActivity.getHopNumberRef());
        assertEquals(caseModel.getCaseNumber(), fourthActivity.getCases().getCaseNumber());
        assertEquals(secondHop.getHopNumber(), fourthActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.ASSIGN_OWNER, fourthActivity.getAction());

        // Fifth sla activity
        CaseSlaActivity fifthActivity = caseSlaActivities.get(4);
        assertDefaultCaseSlaActivity(fifthActivity);
        assertNotNull(fifthActivity.getEndDate());
        assertNotNull(fifthActivity.getOwnerId());
        assertNotNull(fifthActivity.getOwnerName());
        assertNull(fifthActivity.getActualDuration());
        assertEquals(secondHop.getOwnerId(), fifthActivity.getCreatedById());
        assertEquals(secondHop.getOwnerId(), fifthActivity.getModifiedById());
        assertEquals(secondHop.getTeamId(), fifthActivity.getTeamId());
        assertEquals(secondHop.getTeamName(), fifthActivity.getTeamNameTh());
        assertEquals(secondHop.getHopNumber(), fifthActivity.getHopNumberRef());
        assertEquals(secondHop.getHopNumber(), fifthActivity.getHopNumberRef());
        assertEquals(caseModel.getCaseNumber(), fifthActivity.getCases().getCaseNumber());
        assertEquals(CaseSlaActivityAction.NEXT_HOP, fifthActivity.getAction());

        // Sixth sla activity
        CaseSlaActivity sixthActivity = caseSlaActivities.get(5);
        assertDefaultCaseSlaActivity(sixthActivity);
        assertNull(sixthActivity.getEndDate());
        assertNull(sixthActivity.getActualDuration());
        assertNull(sixthActivity.getOwnerId());
        assertNull(sixthActivity.getOwnerName());
        assertEquals(secondHop.getOwnerId(), sixthActivity.getCreatedById());
        assertEquals(secondHop.getOwnerId(), sixthActivity.getModifiedById());
        assertEquals(thirdHop.getTeamId(), sixthActivity.getTeamId());
        assertEquals(thirdHop.getTeamName(), sixthActivity.getTeamNameTh());
        assertEquals(thirdHop.getHopNumber(), sixthActivity.getHopNumberRef());
        assertEquals(caseModel.getCaseNumber(), sixthActivity.getCases().getCaseNumber());
        assertEquals(CaseSlaActivityAction.ASSIGN_TEAM, sixthActivity.getAction());

        // Seventh sla activity
        CaseSlaActivity seventhActivity = caseSlaActivities.get(6);
        assertDefaultCaseSlaActivity(sixthActivity);
        assertNotNull(seventhActivity.getOwnerId());
        assertNotNull(seventhActivity.getOwnerName());
        assertNull(seventhActivity.getActualDuration());
        assertNull(seventhActivity.getEndDate());
        assertEquals(thirdHop.getOwnerId(), seventhActivity.getCreatedById());
        assertEquals(thirdHop.getOwnerId(), seventhActivity.getModifiedById());
        assertEquals(thirdHop.getTeamId(), seventhActivity.getTeamId());
        assertEquals(thirdHop.getTeamName(), seventhActivity.getTeamNameTh());
        assertEquals(thirdHop.getHopNumber(), seventhActivity.getHopNumberRef());
        assertEquals(caseModel.getCaseNumber(), seventhActivity.getCases().getCaseNumber());
        assertEquals(CaseSlaActivityAction.ASSIGN_OWNER, seventhActivity.getAction());
    }

    @Nested
    class caseServiceTypeMatrixTypeDynamic {
        @Test
        @DisplayName("When stm type is Dynamic, and match sale force histories with new stm sla, expect can migration " +
                "and save in crm service")
        void caseMatchHistoriesWithNewStm() {
            ServiceTypeMatrixModel serviceTypeMatrixModel = mockSTM.mockSTMDynamicWithOutFCR();
            when(serviceTypeMatrixRepository.findByServiceTypeMatrixCode(Mockito.anyString()))
                .thenReturn(serviceTypeMatrixModel);

            TeamReadonlyModel createdByTeam = mockTeam.mockTeam(tempCaseModel.getCreatedByTeamNew());
            when(teamReadonlyRepository.findByNameTh(tempCaseModel.getCreatedByTeamNew()))
                    .thenReturn(Optional.of(createdByTeam));

            EmployeeUserModel createdByUser = mockEmployeeUser.mockEmployeeUser(tempCaseModel.getCreatedByEmployeeIdC());
            when(employeeUserRepository.findByEmployeeId(tempCaseModel.getCreatedByEmployeeIdC()))
                    .thenReturn(Optional.of(createdByUser));

            when(stgSlaPerOwnerRepository.findAllByCaseCOrderByStartDateTimeCAsc(anyString()))
                    .thenReturn(stgSlaPerOwnerModel);

            when(employeeUserRepository.findTeamIdByEmployeeId(stgSlaPerOwnerModel.getFirst().getEmployeeIdC()))
                    .thenReturn(List.of(serviceTypeMatrixModel.getServiceTypeMatrixSlas().get(1).getResponsibleBuId().toString()));

            when(employeeUserRepository.findByEmployeeIdAndStatusCode(stgSlaPerOwnerModel.getFirst().getEmployeeIdC(), 0))
                    .thenReturn(Optional.of(createdByUser));

            TeamReadonlyModel teamInBoundVoiceTeam8 = mockTeam.mockTeam(stgSlaPerOwnerModel.get(1).getOwnerTeamNew());
            when(teamReadonlyRepository.findByNameTh(stgSlaPerOwnerModel.get(1).getOwnerTeamNew()))
                    .thenReturn(Optional.of(teamInBoundVoiceTeam8));

            when(employeeUserRepository.findTeamIdByEmployeeId(stgSlaPerOwnerModel.get(1).getEmployeeIdC()))
                    .thenReturn(List.of(teamInBoundVoiceTeam8.getTeamId().toString()));

            EmployeeUserModel middleSlaUser = mockEmployeeUser.mockEmployeeUser(stgSlaPerOwnerModel.get(1).getEmployeeIdC());
            when(employeeUserRepository.findByEmployeeIdAndStatusCode(stgSlaPerOwnerModel.get(1).getEmployeeIdC(), 0))
                    .thenReturn(Optional.of(middleSlaUser));

            TeamReadonlyModel teamInvestmentLine = mockTeam.mockTeam(stgSlaPerOwnerModel.get(2).getOwnerTeamNew());
            when(teamReadonlyRepository.findByNameTh(stgSlaPerOwnerModel.get(2).getOwnerTeamNew()))
                    .thenReturn(Optional.of(teamInvestmentLine));

            when(employeeUserRepository.findTeamIdByEmployeeId(stgSlaPerOwnerModel.get(2).getEmployeeIdC()))
                    .thenReturn(List.of(serviceTypeMatrixModel.getServiceTypeMatrixSlas().get(2).getResponsibleBuId().toString()));

            EmployeeUserModel ownerSlaUser = mockEmployeeUser.mockEmployeeUser(stgSlaPerOwnerModel.get(2).getEmployeeIdC());
            when(employeeUserRepository.findByEmployeeIdAndStatusCode(stgSlaPerOwnerModel.get(2).getEmployeeIdC(), 0))
                    .thenReturn(Optional.of(ownerSlaUser));

            when(employeeUserRepository.findByUserId(Objects.requireNonNull(createdByUser.getUserId())))
                    .thenReturn(Optional.of(createdByUser));
            when(employeeUserRepository.findByUserId(Objects.requireNonNull(middleSlaUser.getUserId())))
                    .thenReturn(Optional.of(middleSlaUser));
            when(employeeUserRepository.findByUserId(Objects.requireNonNull(ownerSlaUser.getUserId())))
                    .thenReturn(Optional.of(ownerSlaUser));

            StgToCaseWriterDTO transaction = createCaseService.createCase(tempCaseModel);

            CaseTransactionModel caseModel = transaction.getCaseTransaction();

            assertNotNull(createdByTeam);
            assertNotNull(createdByUser);

            // Case
            assertDefaultCase(caseModel);
            assertEquals(tempCaseModel.getCaseNumber(), caseModel.getCaseNumber());
            assertEquals(createdByTeam.getTeamId(), caseModel.getCreatedByTeamId());
            assertEquals(createdByUser.getUserId(), caseModel.getCreatedById());
            assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), caseModel.getCreatedOn());
            assertEquals(Constant.CASE_STATUS_IN_PROGRESS, caseModel.getCaseStatusCode());
            assertEquals(Constant.TEP, caseModel.getIntegrationSystem());

            assertCaseAlreadyInProgressWithResolveInfo(caseModel);
            assertCaseAlreadyInProgressWithClosedInfo(caseModel);
            assertNotNull(serviceTypeMatrixModel);
            assertServiceTypeMatrix(serviceTypeMatrixModel, caseModel);

            // Case Sla hop
            List<ServiceTypeMatrixSla> slas = serviceTypeMatrixModel.getServiceTypeMatrixSlas();
            List<CaseSlaHopModel> caseSlas = caseModel.getSlaHop();
            assertNotNull(caseSlas);
            assertEquals(4, caseSlas.size());

            // First case sla hop
            CaseSlaHopModel firstHop = caseSlas.getFirst();
            assertNull(firstHop.getCaseSlaHopId());
            assertNotNull(firstHop.getTeamId());
            assertNotNull(firstHop.getTeamName());
            assertNotNull(firstHop.getCloseByBu());
            assertNotNull(firstHop.getHopNumber());
            assertNotNull(firstHop.getStartDatetime());
            assertNotNull(firstHop.getEndDatetime());
            assertNotNull(firstHop.getOwnerId());
            assertNotNull(firstHop.getOwnerName());
            assertNotNull(firstHop.getCreatedOn());
            assertNotNull(firstHop.getCreatedById());
            assertNotNull(firstHop.getModifiedOn());
            assertNotNull(firstHop.getModifiedById());
            assertNotNull(firstHop.getTotalDuration());
            assertNull(firstHop.getSlaTarget());
            assertNull(firstHop.getSlaTargetDate());
            assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), firstHop.getStartDatetime());
            assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), firstHop.getEndDatetime());
            assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), firstHop.getCreatedOn());
            assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), firstHop.getModifiedOn());
            assertEquals(slas.getFirst().getHopNumber(), firstHop.getHopNumber());
            assertEquals(slas.getFirst().getCloseByBu(), firstHop.getCloseByBu());
            assertEquals(slas.getFirst().getSlaTarget(), firstHop.getSlaTarget());
            assertEquals(createdByTeam.getNameTh(), firstHop.getTeamName());
            assertEquals(createdByTeam.getTeamId(), firstHop.getTeamId());
            assertEquals(0f, firstHop.getTotalDuration(), 0.0000001);
            assertEquals(caseModel.getCaseId(), firstHop.getCases().getCaseId());

            // Second Case Sla hop
            CaseSlaHopModel secondHop = caseSlas.get(1);
            assertNull(secondHop.getCaseSlaHopId());
            assertNotNull(secondHop.getTeamId());
            assertNotNull(secondHop.getTeamName());
            assertNotNull(secondHop.getCloseByBu());
            assertNotNull(secondHop.getHopNumber());
            assertNotNull(secondHop.getStartDatetime());
            assertNotNull(secondHop.getEndDatetime());
            assertNotNull(secondHop.getOwnerId());
            assertNotNull(secondHop.getOwnerName());
            assertNotNull(secondHop.getCreatedOn());
            assertNotNull(secondHop.getCreatedById());
            assertNotNull(secondHop.getModifiedOn());
            assertNotNull(secondHop.getModifiedById());
            assertNull(secondHop.getSlaTarget());
            assertNull(secondHop.getSlaTargetDate());
            assertNull(secondHop.getTotalDuration());
            assertEquals(firstHop.getEndDatetime(), secondHop.getStartDatetime()
            );
            assertEquals(parseToZoneDateTime(
                            stgSlaPerOwnerModel.getFirst().getEndDateTimeC()
                    ),
                    secondHop.getEndDatetime()
            );
            assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), secondHop.getCreatedOn());
            assertEquals(parseToZoneDateTime(stgSlaPerOwnerModel.getFirst().getEndDateTimeC()), secondHop.getModifiedOn());
            assertEquals(slas.get(1).getHopNumber(), secondHop.getHopNumber());
            assertEquals(slas.get(1).getCloseByBu(), secondHop.getCloseByBu());
            assertEquals(slas.get(1).getResponsibleBu(), secondHop.getTeamName());
            assertEquals(slas.get(1).getResponsibleBuId(), secondHop.getTeamId());
            assertEquals(caseModel.getCaseId(), secondHop.getCases().getCaseId());

            // Third Case sla hop
            CaseSlaHopModel thirdHop = caseSlas.get(2);
            assertNull(thirdHop.getCaseSlaHopId());
            assertNotNull(thirdHop.getTeamId());
            assertNotNull(thirdHop.getTeamName());
            assertNotNull(thirdHop.getCloseByBu());
            assertNotNull(thirdHop.getHopNumber());
            assertNotNull(thirdHop.getStartDatetime());
            assertNotNull(thirdHop.getEndDatetime());
            assertNotNull(thirdHop.getOwnerId());
            assertNotNull(thirdHop.getOwnerName());
            assertNotNull(thirdHop.getCreatedOn());
            assertNotNull(thirdHop.getCreatedById());
            assertNotNull(thirdHop.getModifiedOn());
            assertNotNull(thirdHop.getModifiedById());
            assertNull(thirdHop.getSlaTarget());
            assertNull(thirdHop.getSlaTargetDate());
            assertNull(thirdHop.getTotalDuration());
            assertEquals(parseToZoneDateTime(stgSlaPerOwnerModel.get(1).getStartDateTimeC()), thirdHop.getStartDatetime());
            assertEquals(parseToZoneDateTime(
                            stgSlaPerOwnerModel.get(1).getEndDateTimeC()
                    ),
                    thirdHop.getEndDatetime()
            );
            assertEquals(secondHop.getEndDatetime(), thirdHop.getCreatedOn());
            assertEquals(parseToZoneDateTime(stgSlaPerOwnerModel.get(1).getEndDateTimeC()), thirdHop.getModifiedOn());
            assertEquals(3, thirdHop.getHopNumber());
            assertEquals(false, thirdHop.getCloseByBu());
            assertEquals(caseModel.getCaseId(), thirdHop.getCases().getCaseId());

            // Fourth(last) Case sla hop
            CaseSlaHopModel fourthHop = caseSlas.getLast();
            assertNull(fourthHop.getCaseSlaHopId());
            assertNotNull(fourthHop.getTeamId());
            assertNotNull(fourthHop.getTeamName());
            assertNotNull(fourthHop.getCloseByBu());
            assertNotNull(fourthHop.getHopNumber());
            assertNotNull(fourthHop.getStartDatetime());
            assertNotNull(fourthHop.getOwnerId());
            assertNotNull(fourthHop.getOwnerName());
            assertNotNull(fourthHop.getCreatedOn());
            assertNotNull(fourthHop.getCreatedById());
            assertNotNull(fourthHop.getModifiedOn());
            assertNotNull(fourthHop.getModifiedById());
            assertNull(fourthHop.getSlaTarget());
            assertNull(fourthHop.getSlaTargetDate());
            assertNull(fourthHop.getTotalDuration());
            assertNull(fourthHop.getEndDatetime());
            assertEquals(parseToZoneDateTime(stgSlaPerOwnerModel.getLast().getStartDateTimeC()), fourthHop.getStartDatetime());
            assertEquals(thirdHop.getEndDatetime(), fourthHop.getCreatedOn());
            assertEquals(parseToZoneDateTime(stgSlaPerOwnerModel.getLast().getStartDateTimeC()), fourthHop.getModifiedOn());
            assertEquals(4, fourthHop.getHopNumber());
            assertEquals(slas.getLast().getCloseByBu(), fourthHop.getCloseByBu());
            assertEquals(caseModel.getCaseId(), fourthHop.getCases().getCaseId());

            // Case sla activities
            List<CaseSlaActivity> caseSlaActivities = transaction.getCaseTransaction().getCaseSlaActivities();
            assertNotNull(caseSlaActivities);
            assertEquals(10, caseSlaActivities.size());

            // First sla activity
            CaseSlaActivity firstActivity = caseSlaActivities.getFirst();
            assertDefaultCaseSlaActivity(firstActivity);
            assertNotNull(firstActivity.getOwnerId());
            assertNotNull(firstActivity.getOwnerName());
            assertNotNull(firstActivity.getEndDate());
            assertNotNull(firstActivity.getCreatedByName());
            assertNotNull(firstActivity.getModifiedByName());
            assertNotNull(firstActivity.getActualDuration());
            assertEquals(firstHop.getCreatedById(), firstActivity.getCreatedById());
            assertEquals(firstHop.getModifiedById(), firstActivity.getModifiedById());
            assertEquals(firstHop.getOwnerId(), firstActivity.getOwnerId());
            assertEquals(firstHop.getOwnerName(), firstActivity.getOwnerName());
            assertEquals(firstHop.getTeamId(), firstActivity.getTeamId());
            assertEquals(firstHop.getTeamName(), firstActivity.getTeamNameTh());
            assertEquals(firstHop.getHopNumber(), firstActivity.getHopNumberRef());
            assertEquals(caseModel.getCaseNumber(), firstActivity.getCases().getCaseNumber());
            assertEquals(0.0, firstActivity.getActualDuration(), 0.0000001);
            assertEquals(CaseSlaActivityAction.CREATE, firstActivity.getAction());

            // Second sla activity
            CaseSlaActivity secondActivity = caseSlaActivities.get(1);
            assertDefaultCaseSlaActivity(secondActivity);
            assertNotNull(secondActivity.getOwnerId());
            assertNotNull(secondActivity.getOwnerName());
            assertNotNull(secondActivity.getEndDate());
            assertNull(secondActivity.getActualDuration());
            assertEquals(firstHop.getCreatedById(), secondActivity.getCreatedById());
            assertEquals(firstHop.getModifiedById(), secondActivity.getModifiedById());
            assertEquals(firstHop.getOwnerId(), secondActivity.getOwnerId());
            assertEquals(firstHop.getOwnerName(), secondActivity.getOwnerName());
            assertEquals(firstHop.getTeamId(), secondActivity.getTeamId());
            assertEquals(firstHop.getTeamName(), secondActivity.getTeamNameTh());
            assertEquals(firstHop.getHopNumber(), secondActivity.getHopNumberRef());
            assertEquals(caseModel.getCaseNumber(), secondActivity.getCases().getCaseNumber());
            assertEquals(CaseSlaActivityAction.NEXT_HOP, secondActivity.getAction());

            // Third sla activity
            CaseSlaActivity thirdActivity = caseSlaActivities.get(2);
            assertDefaultCaseSlaActivity(thirdActivity);
            assertNotNull(thirdActivity.getEndDate());
            assertNotNull(thirdActivity.getActualDuration());
            assertNull(thirdActivity.getOwnerId());
            assertNull(thirdActivity.getOwnerName());
            assertEquals(firstHop.getOwnerId(), thirdActivity.getCreatedById());
            assertEquals(secondHop.getOwnerId(), thirdActivity.getModifiedById());
            assertEquals(secondHop.getTeamId(), thirdActivity.getTeamId());
            assertEquals(secondHop.getTeamName(), thirdActivity.getTeamNameTh());
            assertEquals(secondHop.getHopNumber(), thirdActivity.getHopNumberRef());
            assertEquals(caseModel.getCaseNumber(), thirdActivity.getCases().getCaseNumber());
            assertEquals(CaseSlaActivityAction.ASSIGN_TEAM, thirdActivity.getAction());

            // Fourth sla activity
            CaseSlaActivity fourthActivity = caseSlaActivities.get(3);
            assertDefaultCaseSlaActivity(fourthActivity);
            assertNotNull(fourthActivity.getEndDate());
            assertNotNull(fourthActivity.getActualDuration());
            assertNotNull(fourthActivity.getOwnerId());
            assertNotNull(fourthActivity.getOwnerName());
            assertEquals(secondHop.getOwnerId(), fourthActivity.getCreatedById());
            assertEquals(secondHop.getOwnerId(), fourthActivity.getModifiedById());
            assertEquals(secondHop.getTeamId(), fourthActivity.getTeamId());
            assertEquals(secondHop.getTeamName(), fourthActivity.getTeamNameTh());
            assertEquals(caseModel.getCaseNumber(), fourthActivity.getCases().getCaseNumber());
            assertEquals(secondHop.getHopNumber(), fourthActivity.getHopNumberRef());
            assertEquals(CaseSlaActivityAction.ASSIGN_OWNER, fourthActivity.getAction());


            // Fifth sla activity
            CaseSlaActivity fifthActivity = caseSlaActivities.get(4);
            assertDefaultCaseSlaActivity(fifthActivity);
            assertNotNull(fifthActivity.getEndDate());
            assertNotNull(fifthActivity.getOwnerId());
            assertNotNull(fifthActivity.getOwnerName());
            assertNull(fifthActivity.getActualDuration());
            assertEquals(secondHop.getOwnerId(), fifthActivity.getCreatedById());
            assertEquals(secondHop.getOwnerId(), fifthActivity.getModifiedById());
            assertEquals(secondHop.getTeamId(), fifthActivity.getTeamId());
            assertEquals(secondHop.getTeamName(), fifthActivity.getTeamNameTh());
            assertEquals(secondHop.getHopNumber(), fifthActivity.getHopNumberRef());
            assertEquals(secondHop.getHopNumber(), fifthActivity.getHopNumberRef());
            assertEquals(caseModel.getCaseNumber(), fifthActivity.getCases().getCaseNumber());
            assertEquals(CaseSlaActivityAction.NEXT_HOP, fifthActivity.getAction());

            // Sixth sla activity
            CaseSlaActivity sixthActivity = caseSlaActivities.get(5);
            assertDefaultCaseSlaActivity(sixthActivity);
            assertNotNull(sixthActivity.getEndDate());
            assertNotNull(sixthActivity.getActualDuration());
            assertNull(sixthActivity.getOwnerId());
            assertNull(sixthActivity.getOwnerName());
            assertEquals(secondHop.getOwnerId(), sixthActivity.getCreatedById());
            assertEquals(thirdHop.getOwnerId(), sixthActivity.getModifiedById());
            assertEquals(thirdHop.getTeamId(), sixthActivity.getTeamId());
            assertEquals(thirdHop.getTeamName(), sixthActivity.getTeamNameTh());
            assertEquals(thirdHop.getHopNumber(), sixthActivity.getHopNumberRef());
            assertEquals(caseModel.getCaseNumber(), sixthActivity.getCases().getCaseNumber());
            assertEquals(CaseSlaActivityAction.ASSIGN_TEAM, sixthActivity.getAction());

            // Seventh sla activity
            CaseSlaActivity seventhActivity = caseSlaActivities.get(6);
            assertDefaultCaseSlaActivity(seventhActivity);
            assertNotNull(seventhActivity.getOwnerId());
            assertNotNull(seventhActivity.getOwnerName());
            assertNotNull(seventhActivity.getActualDuration());
            assertNotNull(seventhActivity.getEndDate());
            assertEquals(thirdHop.getOwnerId(), seventhActivity.getCreatedById());
            assertEquals(thirdHop.getOwnerId(), seventhActivity.getModifiedById());
            assertEquals(thirdHop.getTeamId(), seventhActivity.getTeamId());
            assertEquals(thirdHop.getTeamName(), seventhActivity.getTeamNameTh());
            assertEquals(thirdHop.getHopNumber(), seventhActivity.getHopNumberRef());
            assertEquals(caseModel.getCaseNumber(), seventhActivity.getCases().getCaseNumber());
            assertEquals(CaseSlaActivityAction.ASSIGN_OWNER, seventhActivity.getAction());

            // Eighth sla activity
            CaseSlaActivity eighthActivity = caseSlaActivities.get(7);
            assertDefaultCaseSlaActivity(eighthActivity);
            assertNotNull(eighthActivity.getEndDate());
            assertNotNull(eighthActivity.getOwnerId());
            assertNotNull(eighthActivity.getOwnerName());
            assertNull(eighthActivity.getActualDuration());
            assertEquals(thirdHop.getOwnerId(), eighthActivity.getCreatedById());
            assertEquals(thirdHop.getOwnerId(), eighthActivity.getModifiedById());
            assertEquals(thirdHop.getTeamId(), eighthActivity.getTeamId());
            assertEquals(thirdHop.getTeamName(), eighthActivity.getTeamNameTh());
            assertEquals(thirdHop.getHopNumber(), eighthActivity.getHopNumberRef());
            assertEquals(thirdHop.getHopNumber(), eighthActivity.getHopNumberRef());
            assertEquals(caseModel.getCaseNumber(), eighthActivity.getCases().getCaseNumber());
            assertEquals(CaseSlaActivityAction.NEXT_HOP, eighthActivity.getAction());

            // Ninth sla activity
            CaseSlaActivity ninthActivity = caseSlaActivities.get(8);
            assertDefaultCaseSlaActivity(ninthActivity);
            assertNull(ninthActivity.getActualDuration());
            assertNull(ninthActivity.getEndDate());
            assertNull(ninthActivity.getOwnerId());
            assertNull(ninthActivity.getOwnerName());
            assertEquals(thirdHop.getOwnerId(), ninthActivity.getCreatedById());
            assertEquals(thirdHop.getOwnerId(), ninthActivity.getModifiedById());
            assertEquals(fourthHop.getTeamId(), ninthActivity.getTeamId());
            assertEquals(fourthHop.getTeamName(), ninthActivity.getTeamNameTh());
            assertEquals(fourthHop.getHopNumber(), ninthActivity.getHopNumberRef());
            assertEquals(caseModel.getCaseNumber(), ninthActivity.getCases().getCaseNumber());
            assertEquals(CaseSlaActivityAction.ASSIGN_TEAM, ninthActivity.getAction());

            // Tenth sla activity
            CaseSlaActivity tenthActivity = caseSlaActivities.getLast();
            assertDefaultCaseSlaActivity(tenthActivity);
            assertNotNull(tenthActivity.getOwnerId());
            assertNotNull(tenthActivity.getOwnerName());
            assertNull(tenthActivity.getActualDuration());
            assertNull(tenthActivity.getEndDate());
            assertEquals(fourthHop.getOwnerId(), tenthActivity.getCreatedById());
            assertEquals(fourthHop.getOwnerId(), tenthActivity.getModifiedById());
            assertEquals(fourthHop.getTeamId(), tenthActivity.getTeamId());
            assertEquals(fourthHop.getTeamName(), tenthActivity.getTeamNameTh());
            assertEquals(fourthHop.getHopNumber(), tenthActivity.getHopNumberRef());
            assertEquals(caseModel.getCaseNumber(), tenthActivity.getCases().getCaseNumber());
            assertEquals(CaseSlaActivityAction.ASSIGN_OWNER, tenthActivity.getAction());
        }

        @Test
        @DisplayName("When stm type is Dynamic, no histories from sale force and create team same team with owner team " +
                "team, expect create new case follow stm sla and save in crm service")
        void caseNoHistoriesFromSaleForce() {
            stgSlaPerOwnerModel.clear();
            String createTeamName = tempCaseModel.getCreatedByTeamNew();
            tempCaseModel.setOwnerTeamNew(createTeamName);

            ServiceTypeMatrixModel serviceTypeMatrixModel = mockSTM.mockSTMDynamicWithOutFCR();
          when(serviceTypeMatrixRepository.findByServiceTypeMatrixCode(Mockito.anyString()))
                .thenReturn(serviceTypeMatrixModel);

            TeamReadonlyModel createdTeam = mockTeam.mockTeam(createTeamName);
            when(teamReadonlyRepository.findByNameTh(createTeamName))
                    .thenReturn(Optional.of(createdTeam));

            EmployeeUserModel createdUser = mockEmployeeUser.mockEmployeeUser(tempCaseModel.getCreatedByEmployeeIdC());
            when(employeeUserRepository.findByEmployeeId(createdUser.getEmployeeId()))
                    .thenReturn(Optional.of(createdUser));

            when(stgSlaPerOwnerRepository.findAllByCaseCOrderByStartDateTimeCAsc(Mockito.anyString()))
                    .thenReturn(Collections.emptyList());

            when(employeeUserRepository.findByUserId(Objects.requireNonNull(createdUser.getUserId())))
                    .thenReturn(Optional.of(createdUser));


            StgToCaseWriterDTO response = createCaseService.createCase(tempCaseModel);
            CaseTransactionModel caseModel = response.getCaseTransaction();

            // Case
            assertDefaultCase(caseModel);
            assertEquals(tempCaseModel.getCaseNumber(), caseModel.getCaseNumber());
            assertEquals(createdTeam.getTeamId(), caseModel.getCreatedByTeamId());
            assertEquals(createdUser.getUserId(), caseModel.getCreatedById());
            assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), caseModel.getCreatedOn());
            assertEquals(Constant.CASE_STATUS_IN_PROGRESS, caseModel.getCaseStatusCode());
            assertEquals(Constant.TEP, caseModel.getIntegrationSystem());

            assertCaseAlreadyInProgressWithResolveInfo(caseModel);
            assertCaseAlreadyInProgressWithClosedInfo(caseModel);
            assertNotNull(serviceTypeMatrixModel);
            assertServiceTypeMatrix(serviceTypeMatrixModel, caseModel);

            // Case Sla hop
            List<ServiceTypeMatrixSla> slas = serviceTypeMatrixModel.getServiceTypeMatrixSlas();
            List<CaseSlaHopModel> caseSlas = caseModel.getSlaHop();
            assertNotNull(caseSlas);
            assertEquals(2, caseSlas.size());

            // First case sla hop
            CaseSlaHopModel firstHop = caseSlas.getFirst();
            assertNull(firstHop.getCaseSlaHopId());
            assertNotNull(firstHop.getTeamId());
            assertNotNull(firstHop.getTeamName());
            assertNotNull(firstHop.getCloseByBu());
            assertNotNull(firstHop.getHopNumber());
            assertNotNull(firstHop.getStartDatetime());
            assertNotNull(firstHop.getEndDatetime());
            assertNotNull(firstHop.getOwnerId());
            assertNotNull(firstHop.getOwnerName());
            assertNotNull(firstHop.getCreatedOn());
            assertNotNull(firstHop.getCreatedById());
            assertNotNull(firstHop.getModifiedOn());
            assertNotNull(firstHop.getModifiedById());
            assertNotNull(firstHop.getTotalDuration());
            assertNull(firstHop.getSlaTarget());
            assertNull(firstHop.getSlaTargetDate());
            assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), firstHop.getStartDatetime());
            assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), firstHop.getEndDatetime());
            assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), firstHop.getCreatedOn());
            assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), firstHop.getModifiedOn());
            assertEquals(slas.getFirst().getHopNumber(), firstHop.getHopNumber());
            assertEquals(slas.getFirst().getCloseByBu(), firstHop.getCloseByBu());
            assertEquals(slas.getFirst().getSlaTarget(), firstHop.getSlaTarget());
            assertEquals(createdTeam.getNameTh(), firstHop.getTeamName());
            assertEquals(createdTeam.getTeamId(), firstHop.getTeamId());
            assertEquals(0f, firstHop.getTotalDuration(), 0.0000001);
            assertEquals(caseModel.getCaseId(), firstHop.getCases().getCaseId());

            // Second Case Sla hop
            CaseSlaHopModel secondHop = caseSlas.getLast();
            assertNull(secondHop.getCaseSlaHopId());
            assertNotNull(secondHop.getTeamId());
            assertNotNull(secondHop.getTeamName());
            assertNotNull(secondHop.getCloseByBu());
            assertNotNull(secondHop.getHopNumber());
            assertNotNull(secondHop.getStartDatetime());
            assertNotNull(secondHop.getCreatedOn());
            assertNotNull(secondHop.getCreatedById());
            assertNotNull(secondHop.getModifiedOn());
            assertNotNull(secondHop.getModifiedById());
            assertNull(secondHop.getOwnerId());
            assertNull(secondHop.getOwnerName());
            assertNull(secondHop.getEndDatetime());
            assertNull(secondHop.getSlaTarget());
            assertNull(secondHop.getSlaTargetDate());
            assertNull(secondHop.getTotalDuration());
            assertEquals(parseToZoneDateTime(
                            tempCaseModel.getCreatedDate()
                    ),
                    secondHop.getStartDatetime()
            );
            assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), secondHop.getCreatedOn());
            assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), secondHop.getModifiedOn());
            assertEquals(slas.get(1).getHopNumber(), secondHop.getHopNumber());
            assertEquals(slas.get(1).getCloseByBu(), secondHop.getCloseByBu());
            assertEquals(slas.get(1).getResponsibleBu(), secondHop.getTeamName());
            assertEquals(slas.get(1).getResponsibleBuId(), secondHop.getTeamId());
            assertEquals(caseModel.getCaseId(), secondHop.getCases().getCaseId());

            // Case sla activities
            List<CaseSlaActivity> caseSlaActivities = response.getCaseTransaction().getCaseSlaActivities();
            assertNotNull(caseSlaActivities);
            assertEquals(3, caseSlaActivities.size());

            // First sla activity
            CaseSlaActivity firstActivity = caseSlaActivities.getFirst();
            assertDefaultCaseSlaActivity(firstActivity);
            assertNotNull(firstActivity.getOwnerId());
            assertNotNull(firstActivity.getOwnerName());
            assertNotNull(firstActivity.getEndDate());
            assertNotNull(firstActivity.getCreatedByName());
            assertNotNull(firstActivity.getModifiedByName());
            assertNotNull(firstActivity.getActualDuration());
            assertEquals(firstHop.getCreatedById(), firstActivity.getCreatedById());
            assertEquals(firstHop.getModifiedById(), firstActivity.getModifiedById());
            assertEquals(firstHop.getOwnerId(), firstActivity.getOwnerId());
            assertEquals(firstHop.getOwnerName(), firstActivity.getOwnerName());
            assertEquals(firstHop.getTeamId(), firstActivity.getTeamId());
            assertEquals(firstHop.getTeamName(), firstActivity.getTeamNameTh());
            assertEquals(firstHop.getHopNumber(), firstActivity.getHopNumberRef());
            assertEquals(caseModel.getCaseNumber(), firstActivity.getCases().getCaseNumber());
            assertEquals(0.0, firstActivity.getActualDuration(), 0.0000001);
            assertEquals(CaseSlaActivityAction.CREATE, firstActivity.getAction());

            // Second sla activity
            CaseSlaActivity secondActivity = caseSlaActivities.get(1);
            assertDefaultCaseSlaActivity(secondActivity);
            assertNotNull(secondActivity.getOwnerId());
            assertNotNull(secondActivity.getOwnerName());
            assertNotNull(secondActivity.getEndDate());
            assertNull(secondActivity.getActualDuration());
            assertEquals(firstHop.getCreatedById(), secondActivity.getCreatedById());
            assertEquals(firstHop.getModifiedById(), secondActivity.getModifiedById());
            assertEquals(firstHop.getOwnerId(), secondActivity.getOwnerId());
            assertEquals(firstHop.getOwnerName(), secondActivity.getOwnerName());
            assertEquals(firstHop.getTeamId(), secondActivity.getTeamId());
            assertEquals(firstHop.getTeamName(), secondActivity.getTeamNameTh());
            assertEquals(firstHop.getHopNumber(), secondActivity.getHopNumberRef());
            assertEquals(caseModel.getCaseNumber(), secondActivity.getCases().getCaseNumber());
            assertEquals(CaseSlaActivityAction.NEXT_HOP, secondActivity.getAction());

            // Third sla activity
            CaseSlaActivity thirdActivity = caseSlaActivities.getLast();
            assertDefaultCaseSlaActivity(thirdActivity);
            assertNull(thirdActivity.getEndDate());
            assertNull(thirdActivity.getActualDuration());
            assertNull(thirdActivity.getOwnerId());
            assertNull(thirdActivity.getOwnerName());
            assertEquals(firstHop.getOwnerId(), thirdActivity.getCreatedById());
            assertEquals(firstHop.getOwnerId(), thirdActivity.getModifiedById());
            assertEquals(secondHop.getTeamId(), thirdActivity.getTeamId());
            assertEquals(secondHop.getTeamName(), thirdActivity.getTeamNameTh());
            assertEquals(secondHop.getHopNumber(), thirdActivity.getHopNumberRef());
            assertEquals(caseModel.getCaseNumber(), thirdActivity.getCases().getCaseNumber());
            assertEquals(CaseSlaActivityAction.ASSIGN_TEAM, thirdActivity.getAction());
        }

        @Test
        @DisplayName("When stm type is Dynamic, and not match sale force histories with new stm sla, expect can migration " +
                "with line up hop 2 and save in crm service")
        void caseNotMatchHistoriesWithNewStm() {
            ServiceTypeMatrixModel serviceTypeMatrixModel = mockSTM.mockSTMDynamicNotMatchWithoutFcr();

          when(serviceTypeMatrixRepository.findByServiceTypeMatrixCode(Mockito.anyString()))
                .thenReturn(serviceTypeMatrixModel);

            TeamReadonlyModel createdTeam = mockTeam.mockTeam(tempCaseModel.getCreatedByTeamNew());
            when(teamReadonlyRepository.findByNameTh(tempCaseModel.getCreatedByTeamNew()))
                    .thenReturn(Optional.of(createdTeam));

            EmployeeUserModel createdUser = mockEmployeeUser.mockEmployeeUser(tempCaseModel.getCreatedByEmployeeIdC());
            when(employeeUserRepository.findByEmployeeId(createdUser.getEmployeeId()))
                    .thenReturn(Optional.of(createdUser));

            when(stgSlaPerOwnerRepository.findAllByCaseCOrderByStartDateTimeCAsc(Mockito.anyString()))
                    .thenReturn(stgSlaPerOwnerModel);

            String buEmployeeId = stgSlaPerOwnerModel.getFirst().getEmployeeIdC();

            when(employeeUserRepository.findTeamIdByEmployeeId(buEmployeeId))
                    .thenReturn(List.of(serviceTypeMatrixModel.getServiceTypeMatrixSlas().getLast().getResponsibleBuId().toString()));

            EmployeeUserModel buUser = mockEmployeeUser.mockEmployeeUser(buEmployeeId);
            when(employeeUserRepository.findByEmployeeIdAndStatusCode(buEmployeeId, 0))
                    .thenReturn(Optional.of(buUser));

            String middleEmployeeId = stgSlaPerOwnerModel.get(1).getEmployeeIdC();
            TeamReadonlyModel teamInboundVoice8 = mockTeam.mockTeam(stgSlaPerOwnerModel.get(1).getOwnerTeamNew());
            when(teamReadonlyRepository.findByNameTh(stgSlaPerOwnerModel.get(1).getOwnerTeamNew()))
                    .thenReturn(Optional.of(teamInboundVoice8));

            when(employeeUserRepository.findTeamIdByEmployeeId(middleEmployeeId))
                    .thenReturn(List.of(teamInboundVoice8.getTeamId().toString()));

            EmployeeUserModel middleUser = mockEmployeeUser.mockEmployeeUser(middleEmployeeId);
            when(employeeUserRepository.findByEmployeeIdAndStatusCode(middleEmployeeId, 0))
                    .thenReturn(Optional.of(middleUser));

            String ownerEmployeeId = stgSlaPerOwnerModel.getLast().getEmployeeIdC();

            when(employeeUserRepository.findTeamIdByEmployeeId(ownerEmployeeId))
                    .thenReturn(List.of(serviceTypeMatrixModel.getServiceTypeMatrixSlas().get(1).getResponsibleBuId().toString()));

            EmployeeUserModel ownerUser = mockEmployeeUser.mockEmployeeUser(ownerEmployeeId);
            when(employeeUserRepository.findByEmployeeIdAndStatusCode(ownerEmployeeId, 0))
                    .thenReturn(Optional.of(ownerUser));

            when(employeeUserRepository.findByUserId(Objects.requireNonNull(createdUser.getUserId())))
                    .thenReturn(Optional.of(createdUser));
            when(employeeUserRepository.findByUserId(Objects.requireNonNull(buUser.getUserId())))
                    .thenReturn(Optional.of(buUser));
            when(employeeUserRepository.findByUserId(Objects.requireNonNull(middleUser.getUserId())))
                    .thenReturn(Optional.of(middleUser));
            when(employeeUserRepository.findByUserId(Objects.requireNonNull(ownerUser.getUserId())))
                    .thenReturn(Optional.of(ownerUser));

            StgToCaseWriterDTO response = createCaseService.createCase(tempCaseModel);
            CaseTransactionModel caseModel = response.getCaseTransaction();

            // Case
            assertDefaultCase(caseModel);
            assertEquals(tempCaseModel.getCaseNumber(), caseModel.getCaseNumber());
            assertEquals(createdTeam.getTeamId(), caseModel.getCreatedByTeamId());
            assertEquals(createdUser.getUserId(), caseModel.getCreatedById());
            assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), caseModel.getCreatedOn());
            assertEquals(Constant.CASE_STATUS_IN_PROGRESS, caseModel.getCaseStatusCode());
            assertEquals(Constant.TEP, caseModel.getIntegrationSystem());

            assertCaseAlreadyInProgressWithResolveInfo(caseModel);
            assertCaseAlreadyInProgressWithClosedInfo(caseModel);
            assertNotNull(serviceTypeMatrixModel);
            assertServiceTypeMatrix(serviceTypeMatrixModel, caseModel);

            // Case Sla hop
            List<ServiceTypeMatrixSla> slas = serviceTypeMatrixModel.getServiceTypeMatrixSlas();
            List<CaseSlaHopModel> caseSlas = caseModel.getSlaHop();
            assertNotNull(caseSlas);
            assertEquals(5, caseSlas.size());

            // First case sla hop
            CaseSlaHopModel firstHop = caseSlas.getFirst();
            assertNull(firstHop.getCaseSlaHopId());
            assertNotNull(firstHop.getTeamId());
            assertNotNull(firstHop.getTeamName());
            assertNotNull(firstHop.getCloseByBu());
            assertNotNull(firstHop.getHopNumber());
            assertNotNull(firstHop.getStartDatetime());
            assertNotNull(firstHop.getEndDatetime());
            assertNotNull(firstHop.getOwnerId());
            assertNotNull(firstHop.getOwnerName());
            assertNotNull(firstHop.getCreatedOn());
            assertNotNull(firstHop.getCreatedById());
            assertNotNull(firstHop.getModifiedOn());
            assertNotNull(firstHop.getModifiedById());
            assertNotNull(firstHop.getTotalDuration());
            assertNull(firstHop.getSlaTarget());
            assertNull(firstHop.getSlaTargetDate());
            assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), firstHop.getStartDatetime());
            assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), firstHop.getEndDatetime());
            assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), firstHop.getCreatedOn());
            assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), firstHop.getModifiedOn());
            assertEquals(slas.getFirst().getHopNumber(), firstHop.getHopNumber());
            assertEquals(slas.getFirst().getCloseByBu(), firstHop.getCloseByBu());
            assertEquals(slas.getFirst().getSlaTarget(), firstHop.getSlaTarget());
            assertEquals(createdTeam.getNameTh(), firstHop.getTeamName());
            assertEquals(createdTeam.getTeamId(), firstHop.getTeamId());
            assertEquals(0f, firstHop.getTotalDuration(), 0.0000001);
            assertEquals(caseModel.getCaseId(), firstHop.getCases().getCaseId());

            // Second Case Sla hop
            CaseSlaHopModel secondHop = caseSlas.get(1);
            assertNull(secondHop.getCaseSlaHopId());
            assertNotNull(secondHop.getTeamId());
            assertNotNull(secondHop.getTeamName());
            assertNotNull(secondHop.getCloseByBu());
            assertNotNull(secondHop.getHopNumber());
            assertNotNull(secondHop.getStartDatetime());
            assertNotNull(secondHop.getEndDatetime());
            assertNotNull(secondHop.getCreatedOn());
            assertNotNull(secondHop.getCreatedById());
            assertNotNull(secondHop.getModifiedOn());
            assertNotNull(secondHop.getModifiedById());
            assertNull(secondHop.getOwnerId());
            assertNull(secondHop.getOwnerName());
            assertNull(secondHop.getSlaTarget());
            assertNull(secondHop.getSlaTargetDate());
            assertNull(secondHop.getTotalDuration());
            ZonedDateTime caseCreatedOn = parseToZoneDateTime(tempCaseModel.getCreatedDate());
            assertEquals(caseCreatedOn, secondHop.getStartDatetime());
            assertEquals(parseToZoneDateTime(stgSlaPerOwnerModel.getFirst().getStartDateTimeC()), secondHop.getEndDatetime());
            assertEquals(caseCreatedOn, secondHop.getModifiedOn());
            assertEquals(caseCreatedOn, secondHop.getCreatedOn());
            assertEquals(slas.get(1).getHopNumber(), secondHop.getHopNumber());
            assertEquals(slas.get(1).getCloseByBu(), secondHop.getCloseByBu());
            assertEquals(slas.get(1).getResponsibleBu(), secondHop.getTeamName());
            assertEquals(slas.get(1).getResponsibleBuId(), secondHop.getTeamId());
            assertEquals(caseModel.getCaseId(), secondHop.getCases().getCaseId());

            // Third Case sla hop
            CaseSlaHopModel thirdHop = caseSlas.get(2);
            assertNull(thirdHop.getCaseSlaHopId());
            assertNotNull(thirdHop.getTeamId());
            assertNotNull(thirdHop.getTeamName());
            assertNotNull(thirdHop.getCloseByBu());
            assertNotNull(thirdHop.getHopNumber());
            assertNotNull(thirdHop.getStartDatetime());
            assertNotNull(thirdHop.getEndDatetime());
            assertNotNull(thirdHop.getOwnerId());
            assertNotNull(thirdHop.getOwnerName());
            assertNotNull(thirdHop.getCreatedOn());
            assertNotNull(thirdHop.getCreatedById());
            assertNotNull(thirdHop.getModifiedOn());
            assertNotNull(thirdHop.getModifiedById());
            assertNull(thirdHop.getSlaTarget());
            assertNull(thirdHop.getSlaTargetDate());
            assertNull(thirdHop.getTotalDuration());
            assertEquals(parseToZoneDateTime(stgSlaPerOwnerModel.getFirst().getStartDateTimeC()), thirdHop.getStartDatetime());
            assertEquals(parseToZoneDateTime(
                            stgSlaPerOwnerModel.getFirst().getEndDateTimeC()
                    ),
                    thirdHop.getEndDatetime()
            );
            assertEquals(parseToZoneDateTime(stgSlaPerOwnerModel.getFirst().getStartDateTimeC()), thirdHop.getCreatedOn());
            assertEquals(parseToZoneDateTime(stgSlaPerOwnerModel.getFirst().getEndDateTimeC()), thirdHop.getModifiedOn());
            assertEquals(slas.getLast().getHopNumber(), thirdHop.getHopNumber());
            assertEquals(slas.getLast().getCloseByBu(), thirdHop.getCloseByBu());
            assertEquals(slas.getLast().getResponsibleBu(), thirdHop.getTeamName());
            assertEquals(slas.getLast().getResponsibleBuId(), thirdHop.getTeamId());
            assertEquals(caseModel.getCaseId(), thirdHop.getCases().getCaseId());

            // Fourth Case sla hop
            CaseSlaHopModel fourthHop = caseSlas.get(3);
            assertNull(fourthHop.getCaseSlaHopId());
            assertNotNull(fourthHop.getTeamId());
            assertNotNull(fourthHop.getTeamName());
            assertNotNull(fourthHop.getCloseByBu());
            assertNotNull(fourthHop.getHopNumber());
            assertNotNull(fourthHop.getStartDatetime());
            assertNotNull(fourthHop.getEndDatetime());
            assertNotNull(fourthHop.getOwnerId());
            assertNotNull(fourthHop.getOwnerName());
            assertNotNull(fourthHop.getCreatedOn());
            assertNotNull(fourthHop.getCreatedById());
            assertNotNull(fourthHop.getModifiedOn());
            assertNotNull(fourthHop.getModifiedById());
            assertNull(fourthHop.getSlaTarget());
            assertNull(fourthHop.getSlaTargetDate());
            assertNull(fourthHop.getTotalDuration());
            assertEquals(parseToZoneDateTime(stgSlaPerOwnerModel.get(1).getStartDateTimeC()), fourthHop.getStartDatetime());
            assertEquals(parseToZoneDateTime(stgSlaPerOwnerModel.get(1).getStartDateTimeC()), fourthHop.getCreatedOn());
            assertEquals(parseToZoneDateTime(stgSlaPerOwnerModel.get(1).getEndDateTimeC()), fourthHop.getModifiedOn());
            assertEquals(4, fourthHop.getHopNumber());
            assertEquals(false, fourthHop.getCloseByBu());
            assertEquals(caseModel.getCaseId(), fourthHop.getCases().getCaseId());

            // Fifth(last) Case sla hop
            CaseSlaHopModel fifthHop = caseSlas.getLast();
            assertNull(fifthHop.getCaseSlaHopId());
            assertNotNull(fifthHop.getTeamId());
            assertNotNull(fifthHop.getTeamName());
            assertNotNull(fifthHop.getCloseByBu());
            assertNotNull(fifthHop.getHopNumber());
            assertNotNull(fifthHop.getStartDatetime());
            assertNotNull(fifthHop.getOwnerId());
            assertNotNull(fifthHop.getOwnerName());
            assertNotNull(fifthHop.getCreatedOn());
            assertNotNull(fifthHop.getCreatedById());
            assertNotNull(fifthHop.getModifiedOn());
            assertNotNull(fifthHop.getModifiedById());
            assertNull(fifthHop.getSlaTarget());
            assertNull(fifthHop.getSlaTargetDate());
            assertNull(fifthHop.getTotalDuration());
            assertNull(fifthHop.getEndDatetime());
            assertEquals(parseToZoneDateTime(stgSlaPerOwnerModel.getLast().getStartDateTimeC()), fifthHop.getStartDatetime());
            assertEquals(parseToZoneDateTime(stgSlaPerOwnerModel.getLast().getStartDateTimeC()), fifthHop.getCreatedOn());
            assertEquals(parseToZoneDateTime(stgSlaPerOwnerModel.getLast().getStartDateTimeC()), fifthHop.getModifiedOn());
            assertEquals(5, fifthHop.getHopNumber());
            assertEquals(false, fifthHop.getCloseByBu());
            assertEquals(caseModel.getCaseId(), fifthHop.getCases().getCaseId());

            // Case sla activities
            List<CaseSlaActivity> caseSlaActivities = response.getCaseTransaction().getCaseSlaActivities();
            assertNotNull(caseSlaActivities);
            assertEquals(12, caseSlaActivities.size());

            // First sla activity
            CaseSlaActivity firstActivity = caseSlaActivities.getFirst();
            assertDefaultCaseSlaActivity(firstActivity);
            assertNotNull(firstActivity.getOwnerId());
            assertNotNull(firstActivity.getOwnerName());
            assertNotNull(firstActivity.getEndDate());
            assertNotNull(firstActivity.getCreatedByName());
            assertNotNull(firstActivity.getModifiedByName());
            assertNotNull(firstActivity.getActualDuration());
            assertEquals(firstHop.getCreatedById(), firstActivity.getCreatedById());
            assertEquals(firstHop.getModifiedById(), firstActivity.getModifiedById());
            assertEquals(firstHop.getOwnerId(), firstActivity.getOwnerId());
            assertEquals(firstHop.getOwnerName(), firstActivity.getOwnerName());
            assertEquals(firstHop.getTeamId(), firstActivity.getTeamId());
            assertEquals(firstHop.getTeamName(), firstActivity.getTeamNameTh());
            assertEquals(firstHop.getHopNumber(), firstActivity.getHopNumberRef());
            assertEquals(caseModel.getCaseNumber(), firstActivity.getCases().getCaseNumber());
            assertEquals(0.0, firstActivity.getActualDuration(), 0.0000001);
            assertEquals(CaseSlaActivityAction.CREATE, firstActivity.getAction());

            // Second sla activity
            CaseSlaActivity secondActivity = caseSlaActivities.get(1);
            assertDefaultCaseSlaActivity(secondActivity);
            assertNotNull(secondActivity.getOwnerId());
            assertNotNull(secondActivity.getOwnerName());
            assertNotNull(secondActivity.getEndDate());
            assertNull(secondActivity.getActualDuration());
            assertEquals(firstHop.getCreatedById(), secondActivity.getCreatedById());
            assertEquals(firstHop.getModifiedById(), secondActivity.getModifiedById());
            assertEquals(firstHop.getOwnerId(), secondActivity.getOwnerId());
            assertEquals(firstHop.getOwnerName(), secondActivity.getOwnerName());
            assertEquals(firstHop.getTeamId(), secondActivity.getTeamId());
            assertEquals(firstHop.getTeamName(), secondActivity.getTeamNameTh());
            assertEquals(firstHop.getHopNumber(), secondActivity.getHopNumberRef());
            assertEquals(caseModel.getCaseNumber(), secondActivity.getCases().getCaseNumber());
            assertEquals(CaseSlaActivityAction.NEXT_HOP, secondActivity.getAction());

            // Third sla activity
            CaseSlaActivity thirdActivity = caseSlaActivities.get(2);
            assertDefaultCaseSlaActivity(thirdActivity);
            assertNotNull(thirdActivity.getEndDate());
            assertNotNull(thirdActivity.getActualDuration());
            assertNull(thirdActivity.getOwnerId());
            assertNull(thirdActivity.getOwnerName());
            assertEquals(firstHop.getOwnerId(), thirdActivity.getCreatedById());
            assertEquals(naUser.getUserId(), thirdActivity.getModifiedById());
            assertEquals(secondHop.getTeamId(), thirdActivity.getTeamId());
            assertEquals(secondHop.getTeamName(), thirdActivity.getTeamNameTh());
            assertEquals(secondHop.getHopNumber(), thirdActivity.getHopNumberRef());
            assertEquals(caseModel.getCaseNumber(), thirdActivity.getCases().getCaseNumber());
            assertEquals(CaseSlaActivityAction.ASSIGN_TEAM, thirdActivity.getAction());

            // Fourth sla activity
            CaseSlaActivity fourthActivity = caseSlaActivities.get(3);
            assertDefaultCaseSlaActivity(fourthActivity);
            assertNotNull(fourthActivity.getEndDate());
            assertNotNull(fourthActivity.getOwnerId());
            assertNotNull(fourthActivity.getOwnerName());
            assertNull(fourthActivity.getActualDuration());
            assertEquals(naUser.getUserId(), fourthActivity.getCreatedById());
            assertEquals(naUser.getUserId(), fourthActivity.getModifiedById());
            assertEquals(secondHop.getTeamId(), fourthActivity.getTeamId());
            assertEquals(secondHop.getTeamName(), fourthActivity.getTeamNameTh());
            assertEquals(secondHop.getHopNumber(), fourthActivity.getHopNumberRef());
            assertEquals(secondHop.getHopNumber(), fourthActivity.getHopNumberRef());
            assertEquals(caseModel.getCaseNumber(), fourthActivity.getCases().getCaseNumber());
            assertEquals(CaseSlaActivityAction.NEXT_HOP, fourthActivity.getAction());

            // Fifth sla activity
            CaseSlaActivity fifthActivity = caseSlaActivities.get(4);
            assertDefaultCaseSlaActivity(fifthActivity);
            assertNotNull(fifthActivity.getEndDate());
            assertNotNull(fifthActivity.getActualDuration());
            assertNull(fifthActivity.getOwnerId());
            assertNull(fifthActivity.getOwnerName());
            assertEquals(naUser.getUserId(), fifthActivity.getCreatedById());
            assertEquals(thirdHop.getOwnerId(), fifthActivity.getModifiedById());
            assertEquals(thirdHop.getTeamId(), fifthActivity.getTeamId());
            assertEquals(thirdHop.getTeamName(), fifthActivity.getTeamNameTh());
            assertEquals(thirdHop.getHopNumber(), fifthActivity.getHopNumberRef());
            assertEquals(caseModel.getCaseNumber(), fifthActivity.getCases().getCaseNumber());
            assertEquals(CaseSlaActivityAction.ASSIGN_TEAM, fifthActivity.getAction());

            // Sixth sla activity
            CaseSlaActivity sixthActivity = caseSlaActivities.get(5);
            assertDefaultCaseSlaActivity(sixthActivity);
            assertNotNull(sixthActivity.getOwnerId());
            assertNotNull(sixthActivity.getOwnerName());
            assertNotNull(sixthActivity.getActualDuration());
            assertNotNull(sixthActivity.getEndDate());
            assertEquals(thirdHop.getOwnerId(), sixthActivity.getCreatedById());
            assertEquals(thirdHop.getOwnerId(), sixthActivity.getModifiedById());
            assertEquals(thirdHop.getTeamId(), sixthActivity.getTeamId());
            assertEquals(thirdHop.getTeamName(), sixthActivity.getTeamNameTh());
            assertEquals(thirdHop.getHopNumber(), sixthActivity.getHopNumberRef());
            assertEquals(caseModel.getCaseNumber(), sixthActivity.getCases().getCaseNumber());
            assertEquals(CaseSlaActivityAction.ASSIGN_OWNER, sixthActivity.getAction());

            // Seventh sla activity
            CaseSlaActivity seventhActivity = caseSlaActivities.get(6);
            assertDefaultCaseSlaActivity(seventhActivity);
            assertNotNull(seventhActivity.getEndDate());
            assertNotNull(seventhActivity.getOwnerId());
            assertNotNull(seventhActivity.getOwnerName());
            assertNull(seventhActivity.getActualDuration());
            assertEquals(thirdHop.getOwnerId(), seventhActivity.getCreatedById());
            assertEquals(thirdHop.getOwnerId(), seventhActivity.getModifiedById());
            assertEquals(thirdHop.getTeamId(), seventhActivity.getTeamId());
            assertEquals(thirdHop.getTeamName(), seventhActivity.getTeamNameTh());
            assertEquals(thirdHop.getHopNumber(), seventhActivity.getHopNumberRef());
            assertEquals(thirdHop.getHopNumber(), seventhActivity.getHopNumberRef());
            assertEquals(caseModel.getCaseNumber(), seventhActivity.getCases().getCaseNumber());
            assertEquals(CaseSlaActivityAction.NEXT_HOP, seventhActivity.getAction());

            // Eighth sla activity
            CaseSlaActivity eighthActivity = caseSlaActivities.get(7);
            assertDefaultCaseSlaActivity(eighthActivity);
            assertNotNull(eighthActivity.getActualDuration());
            assertNotNull(eighthActivity.getEndDate());
            assertNull(eighthActivity.getOwnerId());
            assertNull(eighthActivity.getOwnerName());
            assertEquals(thirdHop.getOwnerId(), eighthActivity.getCreatedById());
            assertEquals(fourthHop.getOwnerId(), eighthActivity.getModifiedById());
            assertEquals(fourthHop.getTeamId(), eighthActivity.getTeamId());
            assertEquals(fourthHop.getTeamName(), eighthActivity.getTeamNameTh());
            assertEquals(fourthHop.getHopNumber(), eighthActivity.getHopNumberRef());
            assertEquals(caseModel.getCaseNumber(), eighthActivity.getCases().getCaseNumber());
            assertEquals(CaseSlaActivityAction.ASSIGN_TEAM, eighthActivity.getAction());

            // Ninth sla activity
            CaseSlaActivity ninthActivity = caseSlaActivities.get(8);
            assertDefaultCaseSlaActivity(ninthActivity);
            assertNotNull(ninthActivity.getOwnerId());
            assertNotNull(ninthActivity.getOwnerName());
            assertNotNull(ninthActivity.getActualDuration());
            assertNotNull(ninthActivity.getEndDate());
            assertEquals(fourthHop.getOwnerId(), ninthActivity.getCreatedById());
            assertEquals(fourthHop.getOwnerId(), ninthActivity.getModifiedById());
            assertEquals(fourthHop.getTeamId(), ninthActivity.getTeamId());
            assertEquals(fourthHop.getTeamName(), ninthActivity.getTeamNameTh());
            assertEquals(fourthHop.getHopNumber(), ninthActivity.getHopNumberRef());
            assertEquals(caseModel.getCaseNumber(), ninthActivity.getCases().getCaseNumber());
            assertEquals(CaseSlaActivityAction.ASSIGN_OWNER, ninthActivity.getAction());

            // Tenth sla activity
            CaseSlaActivity tenthActivity = caseSlaActivities.get(9);
            assertDefaultCaseSlaActivity(tenthActivity);
            assertNotNull(tenthActivity.getEndDate());
            assertNotNull(tenthActivity.getOwnerId());
            assertNotNull(tenthActivity.getOwnerName());
            assertNull(tenthActivity.getActualDuration());
            assertEquals(fourthHop.getOwnerId(), tenthActivity.getCreatedById());
            assertEquals(fourthHop.getOwnerId(), tenthActivity.getModifiedById());
            assertEquals(fourthHop.getTeamId(), tenthActivity.getTeamId());
            assertEquals(fourthHop.getTeamName(), tenthActivity.getTeamNameTh());
            assertEquals(fourthHop.getHopNumber(), tenthActivity.getHopNumberRef());
            assertEquals(fourthHop.getHopNumber(), tenthActivity.getHopNumberRef());
            assertEquals(caseModel.getCaseNumber(), tenthActivity.getCases().getCaseNumber());
            assertEquals(CaseSlaActivityAction.NEXT_HOP, tenthActivity.getAction());

            // Eleventh sla activity
            CaseSlaActivity eleventhActivity = caseSlaActivities.get(10);
            assertDefaultCaseSlaActivity(eleventhActivity);
            assertNull(eleventhActivity.getActualDuration());
            assertNull(eleventhActivity.getEndDate());
            assertNull(eleventhActivity.getOwnerId());
            assertNull(eleventhActivity.getOwnerName());
            assertEquals(fourthHop.getOwnerId(), eleventhActivity.getCreatedById());
            assertEquals(fourthHop.getOwnerId(), eleventhActivity.getModifiedById());
            assertEquals(fifthHop.getTeamId(), eleventhActivity.getTeamId());
            assertEquals(fifthHop.getTeamName(), eleventhActivity.getTeamNameTh());
            assertEquals(fifthHop.getHopNumber(), eleventhActivity.getHopNumberRef());
            assertEquals(caseModel.getCaseNumber(), eleventhActivity.getCases().getCaseNumber());
            assertEquals(CaseSlaActivityAction.ASSIGN_TEAM, eleventhActivity.getAction());

            // Twelfth sla activity
            CaseSlaActivity TwelfthActivity = caseSlaActivities.getLast();
            assertDefaultCaseSlaActivity(TwelfthActivity);
            assertNotNull(TwelfthActivity.getOwnerId());
            assertNotNull(TwelfthActivity.getOwnerName());
            assertNull(TwelfthActivity.getActualDuration());
            assertNull(TwelfthActivity.getEndDate());
            assertEquals(fifthHop.getOwnerId(), TwelfthActivity.getCreatedById());
            assertEquals(fifthHop.getOwnerId(), TwelfthActivity.getModifiedById());
            assertEquals(fifthHop.getTeamId(), TwelfthActivity.getTeamId());
            assertEquals(fifthHop.getTeamName(), TwelfthActivity.getTeamNameTh());
            assertEquals(fifthHop.getHopNumber(), TwelfthActivity.getHopNumberRef());
            assertEquals(caseModel.getCaseNumber(), TwelfthActivity.getCases().getCaseNumber());
            assertEquals(CaseSlaActivityAction.ASSIGN_OWNER, TwelfthActivity.getAction());

        }
    }

    @Nested
    class caseServiceTypeMatrixIsFCR {
        @Test
        void caseInProgressWithSTMIsFCR() {
            ServiceTypeMatrixModel serviceTypeMatrixModel = mockSTM.mockSTMFixFCR();

            when(serviceTypeMatrixRepository.findByServiceTypeMatrixCode(Mockito.anyString()))
                .thenReturn(serviceTypeMatrixModel);

            TeamReadonlyModel createdByTeam = mockTeam.mockTeam(tempCaseModel.getCreatedByTeamNew());
            when(teamReadonlyRepository.findByNameTh(anyString())).thenReturn(Optional.of(createdByTeam));

            EmployeeUserModel createdByUser = mockEmployeeUser.mockEmployeeUser(tempCaseModel.getCreatedByEmployeeIdC());
            when(employeeUserRepository.findByEmployeeId(tempCaseModel.getCreatedByEmployeeIdC()))
                    .thenReturn(Optional.ofNullable(createdByUser));

            StgToCaseWriterDTO transaction = createCaseService.createCase(tempCaseModel);

            CaseTransactionModel caseModel = transaction.getCaseTransaction();

            assertNotNull(createdByUser);
            // Case
            assertDefaultCase(caseModel);
            assertNull(caseModel.getOwnerId());
            assertNull(caseModel.getOwnerName());
            assertNull(caseModel.getTeamId());
            assertNull(caseModel.getTeamName());
            assertEquals(tempCaseModel.getCaseNumber(), caseModel.getCaseNumber());
            assertEquals(createdByTeam.getTeamId(), caseModel.getCreatedByTeamId());
            assertEquals(createdByUser.getUserId(), caseModel.getCreatedById());
            assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), caseModel.getCreatedOn());
            assertEquals(Constant.CASE_STATUS_COMPLETED, caseModel.getCaseStatusCode());
            assertEquals(Constant.TEP, caseModel.getIntegrationSystem());

            // Case Closed Info
            assertNotNull(caseModel.getClosedBy());
            assertNotNull(caseModel.getClosedDate());
            assertNotNull(caseModel.getClosedById());
            assertNotNull(caseModel.getClosedByTeamId());

            assertNotNull(serviceTypeMatrixModel);
            assertServiceTypeMatrix(serviceTypeMatrixModel, caseModel);

            // Case Sla hop
            List<CaseSlaHopModel> caseSlas = caseModel.getSlaHop();
            assertEquals(0,caseSlas.size());

            // Case sla activities
            List<CaseSlaActivity> caseSlaActivities = transaction.getCaseTransaction().getCaseSlaActivities();
            assertNotNull(caseSlaActivities);
            assertEquals(2, caseSlaActivities.size());

            // First sla activity
            CaseSlaActivity firstActivity = caseSlaActivities.getFirst();
            assertDefaultCaseSlaActivity(firstActivity);
            assertNotNull(firstActivity.getOwnerId());
            assertNotNull(firstActivity.getOwnerName());
            assertNotNull(firstActivity.getEndDate());
            assertNotNull(firstActivity.getCreatedByName());
            assertNotNull(firstActivity.getModifiedByName());
            assertNotNull(firstActivity.getActualDuration());
            assertEquals(1, firstActivity.getHopNumberRef());
            assertEquals(caseModel.getCaseNumber(), firstActivity.getCases().getCaseNumber());
            assertEquals(0.0, firstActivity.getActualDuration(), 0.0000001);
            assertEquals(CaseSlaActivityAction.CREATE, firstActivity.getAction());

            // First sla activity
            CaseSlaActivity secondActivity = caseSlaActivities.getLast();
            assertDefaultCaseSlaActivity(secondActivity);
            assertNotNull(secondActivity.getOwnerId());
            assertNotNull(secondActivity.getOwnerName());
            assertNotNull(secondActivity.getEndDate());
            assertNotNull(secondActivity.getCreatedByName());
            assertNotNull(secondActivity.getModifiedByName());
            assertNull(secondActivity.getActualDuration());
            assertEquals(1, secondActivity.getHopNumberRef());
            assertEquals(caseModel.getCaseNumber(), secondActivity.getCases().getCaseNumber());
            assertEquals(CaseSlaActivityAction.AUTO_COMPLETED, secondActivity.getAction());

        }
    }

    @Nested
    class caseAutoCreate {
        @Test
        void caseInProgressWithAutoCreateCaseWithSpecialSTM() {

            ServiceTypeMatrixModel serviceTypeMatrixModel = mockSTM.mockSTMAutoCreateCase();
            TeamReadonlyModel createdByTeam = mockTeam.mockTeam(tempCaseModel.getCreatedByTeamNew());
            EmployeeUserModel createByUser = mockEmployeeUser.mockEmployeeUser(tempCaseModel.getCreatedByEmployeeIdC());
            EmployeeUserModel modifiedByEmployeeId = mockEmployeeUser.mockEmployeeUser(tempCaseModel.getLastModifiedByEmployeeIdC());
            EmployeeUserModel user55515 = mockEmployeeUser.mockEmployeeUser("55515");
            tempCaseModel.setServiceTypeMatrixCodeC("18050");
            tempCaseModel.setCreatedByTeamNew("Inbound Voice Team 1");

            stgSlaPerOwnerModel.clear();
            stgSlaPerOwnerModel.add(mockTempSla.mockTempStgSlaPerOwnerCompleted(
                    "500RC00000hs5UiSEJ", "Inbound Voice Team 1", "Thanakorn Chaiyasit", "55501",
                    "2025-06-02 17:18:13", "2025-06-02 17:18:13", "New"
            ));

            when(serviceTypeMatrixRepository.findByServiceTypeMatrixCode(Mockito.anyString()))
                .thenReturn(serviceTypeMatrixModel);

            when(teamReadonlyRepository.findByNameTh(anyString()))
                    .thenReturn(Optional.ofNullable(createdByTeam));
            when(employeeUserRepository.findByEmployeeId(tempCaseModel.getCreatedByEmployeeIdC()))
                    .thenReturn(Optional.ofNullable(createByUser));

            when(employeeUserRepository.findByEmployeeId(tempCaseModel.getLastModifiedByEmployeeIdC()))
                    .thenReturn(Optional.ofNullable(modifiedByEmployeeId));
            when(employeeUserRepository.findByEmployeeIdAndStatusCode(tempCaseModel.getLastModifiedByEmployeeIdC(), 0))
                    .thenReturn(Optional.ofNullable(modifiedByEmployeeId));
            when(employeeUserRepository.findByUserId(Objects.requireNonNull(modifiedByEmployeeId).getUserId())).thenReturn(Optional.of(modifiedByEmployeeId));

            when(employeeUserRepository.findByEmployeeId("55515")).thenReturn(Optional.ofNullable(user55515));
            when(employeeUserRepository.findByEmployeeIdAndStatusCode("55515", 0)).thenReturn(Optional.ofNullable(user55515));
            when(employeeUserRepository.findByUserId(Objects.requireNonNull(user55515).getUserId())).thenReturn(Optional.of(user55515));

            when(stgSlaPerOwnerRepository.findAllByCaseCOrderByStartDateTimeCAsc(tempCaseModel.getSfId())).thenReturn(stgSlaPerOwnerModel);

            when(serviceTypeMatrixRepository.findByServiceTypeMatrixCode("27447"))
                    .thenReturn(serviceTypeMatrixModel);

            StgToCaseWriterDTO transaction = createCaseService.createCase(tempCaseModel);

            CaseTransactionModel caseModel = transaction.getCaseTransaction();

            assertDefaultCase(caseModel);
            assertEquals(Constant.CASE_STATUS_IN_PROGRESS, caseModel.getCaseStatusCode());
            assertEquals(Constant.TEP, caseModel.getIntegrationSystem());

            assertCaseAlreadyInProgressWithResolveInfo(caseModel);
            assertCaseAlreadyInProgressWithClosedInfo(caseModel);
            assertNotNull(serviceTypeMatrixModel);
            assertServiceTypeMatrix(serviceTypeMatrixModel, caseModel);

            // Case sla activities
            List<CaseSlaActivity> caseSlaActivities = transaction.getCaseTransaction().getCaseSlaActivities();
            assertNotNull(caseSlaActivities);
            assertEquals(3, caseSlaActivities.size());

            // First sla activity
            CaseSlaActivity firstActivity = caseSlaActivities.getFirst();
            assertNull(firstActivity.getCaseSlaActivityId());
            assertNotNull(firstActivity.getCreatedOn());
            assertNotNull(firstActivity.getModifiedOn());
            assertNotNull(firstActivity.getCreatedById());
            assertNotNull(firstActivity.getModifiedById());
            assertNotNull(firstActivity.getOwnerId());
            assertNotNull(firstActivity.getOwnerName());
            assertNotNull(firstActivity.getTeamId());
            assertNotNull(firstActivity.getTeamNameTh());
            assertNotNull(firstActivity.getStartDate());
            assertNotNull(firstActivity.getEndDate());
            assertNotNull(firstActivity.getCreatedByName());
            assertNotNull(firstActivity.getModifiedByName());
            assertEquals(caseModel.getCaseNumber(), firstActivity.getCases().getCaseNumber());
            assertEquals(1, firstActivity.getHopNumberRef());
            assertEquals(CaseSlaActivityAction.CREATE, firstActivity.getAction());
            assertEquals(0.0, firstActivity.getActualDuration(), 0.0000001);

            // Second sla activity
            CaseSlaActivity secondActivity = caseSlaActivities.get(1);
            assertNull(secondActivity.getCaseSlaActivityId());
            assertNotNull(secondActivity.getCreatedOn());
            assertNotNull(secondActivity.getModifiedOn());
            assertNotNull(secondActivity.getCreatedById());
            assertNotNull(secondActivity.getModifiedById());
            assertNotNull(secondActivity.getOwnerId());
            assertNotNull(secondActivity.getOwnerName());
            assertNotNull(secondActivity.getTeamId());
            assertNotNull(secondActivity.getTeamNameTh());
            assertNotNull(secondActivity.getStartDate());
            assertNotNull(secondActivity.getEndDate());
            assertNull(secondActivity.getActualDuration());
            assertEquals(caseModel.getCaseNumber(), secondActivity.getCases().getCaseNumber());
            assertEquals(1, secondActivity.getHopNumberRef());
            assertEquals(CaseSlaActivityAction.NEXT_HOP, secondActivity.getAction());

            // Third sla activity
            CaseSlaActivity thirdActivity = caseSlaActivities.get(2);
            assertNull(thirdActivity.getCaseSlaActivityId());
            assertNotNull(thirdActivity.getCreatedOn());
            assertNotNull(thirdActivity.getModifiedOn());
            assertNotNull(thirdActivity.getCreatedById());
            assertNotNull(thirdActivity.getModifiedById());
            assertNotNull(thirdActivity.getTeamId());
            assertNotNull(thirdActivity.getTeamNameTh());
            assertNotNull(thirdActivity.getStartDate());
            assertNull(thirdActivity.getOwnerId());
            assertNull(thirdActivity.getOwnerName());
            assertEquals(caseModel.getCaseNumber(), thirdActivity.getCases().getCaseNumber());
            assertEquals(2, thirdActivity.getHopNumberRef());
            assertEquals(CaseSlaActivityAction.ASSIGN_TEAM, thirdActivity.getAction());
        }
    }

    @Test
    void caseInProgressWithOneAppMyAdvisorAssignOwner() {
        stgSlaPerOwnerModel.clear();
        tempCaseModel.setServiceTypeMatrixCodeC("O0011");
        tempCaseModel.setIntegrationSystem(Constant.ONE_APP);
        tempCaseModel.setCreatedByEmployeeIdC(Constant.SF_EX_API_ID);
        tempCaseModel.setCreatedByTeamNew(null);
        ServiceTypeMatrixModel serviceTypeMatrixModel = mockSTM.mockSTMOneAppMyAdvisor();

        when(serviceTypeMatrixRepository.findByServiceTypeMatrixCode(Mockito.anyString()))
                .thenReturn(serviceTypeMatrixModel);

        TeamReadonlyModel createdTeam = mockTeam.mockTeam(tempCaseModel.getCreatedByTeamNew());
        when(teamReadonlyRepository.findByNameTh(tempCaseModel.getCreatedByTeamNew()))
                .thenReturn(Optional.ofNullable(createdTeam));

        EmployeeUserModel createdByUser = mockEmployeeUser.mockEmployeeUser(tempCaseModel.getCreatedByEmployeeIdC());
        when(employeeUserRepository.findByEmployeeId(tempCaseModel.getCreatedByEmployeeIdC()))
                .thenReturn(Optional.ofNullable(createdByUser));

        when(stgSlaPerOwnerRepository.findAllByCaseCOrderByStartDateTimeCAsc(tempCaseModel.getSfId()))
                .thenReturn(stgSlaPerOwnerModel);

        MasterGroupModel masterGroupModel = new MasterGroupModel();
        masterGroupModel.setId(UUID.randomUUID())
                .setCode(tempCaseModel.getServiceTypeMatrixCodeNew())
                .setStatusCode(0);

        MasterDataModel masterDataModel = new MasterDataModel();
        masterDataModel.setNameTh("My Advisor")
                .setId(UUID.randomUUID())
                .setStatusCode(0)
                .setMasterGroup(masterGroupModel);

        masterGroupModel.setMasterData(Collections.singletonList(masterDataModel));

        when(masterDataRepository.findByCodeAndStatusCodeAndMasterGroup_CodeAndMasterGroup_StatusCode(anyString(), Mockito.anyInt(), anyString(), Mockito.anyInt()))
                .thenReturn(Optional.of(masterDataModel));

        when(masterGroupRepository.findByCode(anyString()))
                .thenReturn(Optional.of(masterGroupModel));

        EmployeeUserModel ownerEmployee = mockEmployeeUser.mockEmployeeUser(tempCaseModel.getOwnerEmployeeIdC());
        when(employeeUserRepository.findByEmployeeId(tempCaseModel.getOwnerEmployeeIdC())).thenReturn(Optional.of(ownerEmployee));
        // Mock Sla Activity
        when(employeeUserRepository.findByUserId(Objects.requireNonNull(ownerEmployee.getUserId())))
                .thenReturn(Optional.of(ownerEmployee));

        StgToCaseWriterDTO transaction = createCaseService.createCase(tempCaseModel);
        CaseTransactionModel caseModel = transaction.getCaseTransaction();

        // Case transaction
        assertDefaultCase(caseModel);
        assertNotNull(caseModel.getOwnerId());
        assertNotNull(caseModel.getOwnerName());
        assertNull(caseModel.getTeamId());
        assertNull(caseModel.getTeamName());
        assertEquals(tempCaseModel.getCaseNumber(), caseModel.getCaseNumber());
        assertEquals(Constant.CASE_STATUS_IN_PROGRESS, caseModel.getCaseStatusCode());
        assertEquals(Constant.ONE_APP, caseModel.getIntegrationSystem());

        assertCaseAlreadyInProgressWithResolveInfo(caseModel);
        assertCaseAlreadyInProgressWithClosedInfo(caseModel);
        assertNotNull(serviceTypeMatrixModel);
        assertServiceTypeMatrix(serviceTypeMatrixModel, caseModel);

        List<ServiceTypeMatrixSla> slas = serviceTypeMatrixModel.getServiceTypeMatrixSlas();
        List<CaseSlaHopModel> caseSlas = caseModel.getSlaHop();
        assertNotNull(slas);
        assertNotNull(caseSlas);
        assertEquals(slas.size(), caseSlas.size());

        // First case sla hop
        CaseSlaHopModel firstHop = caseSlas.getFirst();
        assertNotNull(firstHop);
        assertNull(firstHop.getCaseSlaHopId());
        assertNotNull(firstHop.getTeamId());
        assertNotNull(firstHop.getTeamName());
        assertNotNull(firstHop.getCloseByBu());
        assertNotNull(firstHop.getHopNumber());
        assertNotNull(firstHop.getStartDatetime());
        assertNotNull(firstHop.getEndDatetime());
        assertNotNull(firstHop.getOwnerId());
        assertNotNull(firstHop.getOwnerName());
        assertNotNull(firstHop.getCreatedOn());
        assertNotNull(firstHop.getCreatedById());
        assertNotNull(firstHop.getModifiedOn());
        assertNotNull(firstHop.getModifiedById());
        assertNull(firstHop.getSlaTarget());
        assertNull(firstHop.getSlaTargetDate());
        assertNotNull(firstHop.getTotalDuration());
        assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), firstHop.getStartDatetime());
        assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), firstHop.getEndDatetime());
        assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), firstHop.getCreatedOn());
        assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), firstHop.getModifiedOn());
        assertEquals(slas.getFirst().getHopNumber(), firstHop.getHopNumber());
        assertEquals(slas.getFirst().getCloseByBu(), firstHop.getCloseByBu());
        assertEquals(slas.getFirst().getSlaTarget(), firstHop.getSlaTarget());
        assertEquals(systemTeam.getNameTh(), firstHop.getTeamName());
        assertEquals(systemTeam.getTeamId(), firstHop.getTeamId());
        assertEquals(0f, firstHop.getTotalDuration(), 0.0000001);
        assertEquals(caseModel.getCaseId(), firstHop.getCases().getCaseId());

        // Second Case Sla hop
        CaseSlaHopModel secondHop = caseSlas.getLast();
        assertNull(secondHop.getCaseSlaHopId());
        assertNotNull(secondHop.getCloseByBu());
        assertNotNull(secondHop.getHopNumber());
        assertNotNull(secondHop.getStartDatetime());
        assertNotNull(secondHop.getOwnerId());
        assertNotNull(secondHop.getOwnerName());
        assertNotNull(secondHop.getCreatedOn());
        assertNotNull(secondHop.getCreatedById());
        assertNotNull(secondHop.getModifiedOn());
        assertNotNull(secondHop.getModifiedById());
        assertNull(secondHop.getSlaTarget());
        assertNull(secondHop.getSlaTargetDate());
        assertNull(secondHop.getTotalDuration());
        assertNull(secondHop.getTeamId());
        assertNull(secondHop.getTeamName());
        assertNull(secondHop.getEndDatetime());
        assertEquals(firstHop.getEndDatetime(), secondHop.getStartDatetime());
        assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), secondHop.getCreatedOn());
        assertEquals(firstHop.getEndDatetime(), secondHop.getModifiedOn());
        assertEquals(slas.get(1).getHopNumber(), secondHop.getHopNumber());
        assertEquals(slas.get(1).getCloseByBu(), secondHop.getCloseByBu());
        assertEquals(caseModel.getCaseId(), secondHop.getCases().getCaseId());

        // Case sla activities
        List<CaseSlaActivity> caseSlaActivities = transaction.getCaseTransaction().getCaseSlaActivities();
        assertNotNull(caseSlaActivities);
        assertEquals(3, caseSlaActivities.size());

        // First sla activity
        CaseSlaActivity firstActivity = caseSlaActivities.getFirst();
        assertDefaultCaseSlaActivity(firstActivity);
        assertNotNull(firstActivity.getOwnerId());
        assertNotNull(firstActivity.getOwnerName());
        assertNotNull(firstActivity.getEndDate());
        assertNotNull(firstActivity.getCreatedByName());
        assertNotNull(firstActivity.getModifiedByName());
        assertNotNull(firstActivity.getActualDuration());
        assertEquals(firstHop.getCreatedById(), firstActivity.getCreatedById());
        assertEquals(firstHop.getModifiedById(), firstActivity.getModifiedById());
        assertEquals(firstHop.getOwnerId(), firstActivity.getOwnerId());
        assertEquals(firstHop.getOwnerName(), firstActivity.getOwnerName());
        assertEquals(firstHop.getTeamId(), firstActivity.getTeamId());
        assertEquals(firstHop.getTeamName(), firstActivity.getTeamNameTh());
        assertEquals(firstHop.getHopNumber(), firstActivity.getHopNumberRef());
        assertEquals(caseModel.getCaseNumber(), firstActivity.getCases().getCaseNumber());
        assertEquals(0.0, firstActivity.getActualDuration(), 0.0000001);
        assertEquals(CaseSlaActivityAction.CREATE, firstActivity.getAction());

        // Second sla activity
        CaseSlaActivity secondActivity = caseSlaActivities.get(1);
        assertDefaultCaseSlaActivity(secondActivity);
        assertNotNull(secondActivity.getOwnerId());
        assertNotNull(secondActivity.getOwnerName());
        assertNotNull(secondActivity.getEndDate());
        assertNull(secondActivity.getActualDuration());
        assertEquals(firstHop.getCreatedById(), secondActivity.getCreatedById());
        assertEquals(firstHop.getModifiedById(), secondActivity.getModifiedById());
        assertEquals(firstHop.getOwnerId(), secondActivity.getOwnerId());
        assertEquals(firstHop.getOwnerName(), secondActivity.getOwnerName());
        assertEquals(firstHop.getTeamId(), secondActivity.getTeamId());
        assertEquals(firstHop.getTeamName(), secondActivity.getTeamNameTh());
        assertEquals(firstHop.getHopNumber(), secondActivity.getHopNumberRef());
        assertEquals(caseModel.getCaseNumber(), secondActivity.getCases().getCaseNumber());
        assertEquals(CaseSlaActivityAction.NEXT_HOP, secondActivity.getAction());

        // Third sla activity
        CaseSlaActivity thirdActivity = caseSlaActivities.getLast();
        assertNull(thirdActivity.getCaseSlaActivityId());
        assertNotNull(thirdActivity.getCreatedOn());
        assertNotNull(thirdActivity.getModifiedOn());
        assertNotNull(thirdActivity.getCreatedById());
        assertNotNull(thirdActivity.getModifiedById());
        assertNotNull(thirdActivity.getStartDate());
        assertNotNull(thirdActivity.getOwnerId());
        assertNotNull(thirdActivity.getOwnerName());
        assertNull(thirdActivity.getActualDuration());
        assertNull(thirdActivity.getEndDate());
        assertEquals(firstHop.getCreatedById(), thirdActivity.getCreatedById());
        assertEquals(secondHop.getOwnerId(), thirdActivity.getModifiedById());
        assertEquals(caseModel.getCaseNumber(), thirdActivity.getCases().getCaseNumber());
        assertEquals(secondHop.getHopNumber(), thirdActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.ASSIGN_OWNER, thirdActivity.getAction());
    }

    @Test
    @Disabled
    void caseInProgressWithSfex() {
        ServiceTypeMatrixModel serviceTypeMatrixModel = mockSTM.mockSTMOneAppSFEX();
        EmployeeUserModel createdByUser = mockEmployeeUser.mockEmployeeUser(tempCaseModel.getCreatedByEmployeeIdC());
        TeamReadonlyModel createdByTeam = mockTeam.mockTeam(tempCaseModel.getCreatedByTeamNew());

        tempCaseModel.setServiceTypeMatrixCodeNew("O00011");
        tempCaseModel.setCreatedByEmployeeIdC(Constant.SF_EX_API_ID);
        tempCaseModel.setCreatedNameC(Constant.SF_EX_API_FULL_NAME);

        stgSlaPerOwnerModel.clear();
        stgSlaPerOwnerModel.add(mockTempSla.mockTempStgSlaPerOwnerCompleted(
                "500RC00000hs5UiSEJ", "Inbound Voice Team 8", "User B", "55515",
                "2025-06-02 17:18:14", "2025-06-07 04:51:11", "In progress"
        ));
        stgSlaPerOwnerModel.add(mockTempSla.mockTempStgSlaPerOwnerCompleted(
                "500RC00000hs5UiSEJ", "Investment Line", "User C", "99902",
                "2025-06-07 04:51:11", "", "In progress"
        ));

        when(serviceTypeMatrixRepository.findByServiceTypeMatrixCode("O00011"))
                .thenReturn(serviceTypeMatrixModel);

        when(stgSlaPerOwnerRepository.findAllByCaseCOrderByStartDateTimeCAsc(tempCaseModel.getSfId()))
                .thenReturn(stgSlaPerOwnerModel);

        when(teamReadonlyRepository.findByNameTh(anyString()))
                .thenAnswer(inv -> Optional.of(mockTeam.mockTeam(inv.getArgument(0))));

        when(teamReadonlyRepository.findByNameTh(tempCaseModel.getCreatedByTeamNew()))
                .thenReturn(Optional.of(createdByTeam));
        when(employeeUserRepository.findByEmployeeId(tempCaseModel.getCreatedByEmployeeIdC()))
                .thenReturn(Optional.of(createdByUser));
        when(employeeUserRepository.findByUserId(Objects.requireNonNull(createdByUser.getUserId())))
                .thenReturn(Optional.of(createdByUser));

        EmployeeUserModel systemUser = mockEmployeeUser.mockEmployeeUser(Constant.SYSTEM_EMPLOYEE_ID);
        when(employeeUserRepository.findByEmployeeIdAndStatusCode(Constant.SYSTEM_EMPLOYEE_ID, 0))
                .thenReturn(Optional.of(systemUser));
        when(employeeUserRepository.findByUserId(systemUser.getUserId()))
                .thenReturn(Optional.of(systemUser));

        EmployeeUserModel user55515 = mockEmployeeUser.mockEmployeeUser("55515");
        when(employeeUserRepository.findByEmployeeIdAndStatusCode("55515", 0))
                .thenReturn(Optional.of(user55515));
        when(employeeUserRepository.findByUserId(user55515.getUserId()))
                .thenReturn(Optional.of(user55515));

        EmployeeUserModel user99902 = mockEmployeeUser.mockEmployeeUser("99902");
        when(employeeUserRepository.findByEmployeeIdAndStatusCode("99902", 0))
                .thenReturn(Optional.of(user99902));
        when(employeeUserRepository.findByUserId(user99902.getUserId()))
                .thenReturn(Optional.of(user99902));

        when(stgSlaPerOwnerRepository.findAllByCaseCOrderByStartDateTimeCAsc(tempCaseModel.getSfId()))
                .thenReturn(stgSlaPerOwnerModel);

        StgToCaseWriterDTO transaction = createCaseService.createCase(tempCaseModel);

        CaseTransactionModel caseModel = transaction.getCaseTransaction();

        assertNotNull(createdByUser);
        // Case
        assertNotNull(caseModel.getCaseId());
        assertNotNull(caseModel.getCaseNumber());
        assertNotNull(caseModel.getCreatedByTeamId());
        assertNotNull(caseModel.getCreatedById());
        assertNotNull(caseModel.getModifiedById());
        assertNotNull(caseModel.getCreatedOn());
        assertNotNull(caseModel.getModifiedOn());
        assertNotNull(caseModel.getOwnerName());
        assertNotNull(caseModel.getTeamName());
        assertNull(caseModel.getTeamId());
        assertNull(caseModel.getOwnerId());
        assertEquals(tempCaseModel.getCaseNumber(), caseModel.getCaseNumber());
        assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), caseModel.getCreatedOn());
        assertEquals(Constant.CASE_STATUS_IN_PROGRESS, caseModel.getCaseStatusCode());
        assertEquals(Constant.TEP, caseModel.getIntegrationSystem());

        assertCaseAlreadyInProgressWithClosedInfo(caseModel);
        assertNotNull(serviceTypeMatrixModel);
        assertServiceTypeMatrix(serviceTypeMatrixModel, caseModel);

        assertNotNull(serviceTypeMatrixModel);
        assertServiceTypeMatrix(serviceTypeMatrixModel, caseModel);

        // Case Sla hop
        List<CaseSlaHopModel> slas = caseModel.getSlaHop();
        assertEquals(3, slas.size());

        // Case sla activities
        List<CaseSlaActivity> caseSlaActivities = transaction.getCaseTransaction().getCaseSlaActivities();
        assertNotNull(caseSlaActivities);
        assertEquals(7, caseSlaActivities.size());

        // First case sla hop
        CaseSlaHopModel firstHop = slas.getFirst();
        assertNull(firstHop.getCaseSlaHopId());
        assertNotNull(firstHop.getTeamId());
        assertNotNull(firstHop.getTeamName());
        assertNotNull(firstHop.getCloseByBu());
        assertNotNull(firstHop.getHopNumber());
        assertNotNull(firstHop.getStartDatetime());
        assertNotNull(firstHop.getEndDatetime());
        assertNotNull(firstHop.getOwnerId());
        assertNotNull(firstHop.getOwnerName());
        assertNotNull(firstHop.getCreatedOn());
        assertNotNull(firstHop.getCreatedById());
        assertNotNull(firstHop.getModifiedOn());
        assertNotNull(firstHop.getModifiedById());
        assertNull(firstHop.getSlaTarget());
        assertNull(firstHop.getSlaTargetDate());
        assertNotNull(firstHop.getTotalDuration());
        assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), firstHop.getStartDatetime());
        assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), firstHop.getEndDatetime());
        assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), firstHop.getCreatedOn());
        assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), firstHop.getModifiedOn());
        assertEquals(slas.getFirst().getHopNumber(), firstHop.getHopNumber());
//        assertEquals(0, firstHop.getTotalDuration());
        assertEquals(slas.getFirst().getCloseByBu(), firstHop.getCloseByBu());
        assertEquals(slas.getFirst().getSlaTarget(), firstHop.getSlaTarget());
        assertEquals(caseModel.getCaseId(), firstHop.getCases().getCaseId());

        // Second Case Sla hop
        CaseSlaHopModel secondHop = slas.get(1);
        assertNull(secondHop.getCaseSlaHopId());
        assertNotNull(secondHop.getTeamId());
        assertNotNull(secondHop.getTeamName());
        assertNotNull(secondHop.getCloseByBu());
        assertNotNull(secondHop.getHopNumber());
        assertNotNull(secondHop.getStartDatetime());
        assertNotNull(secondHop.getEndDatetime());
        assertNotNull(secondHop.getOwnerId());
        assertNotNull(secondHop.getOwnerName());
        assertNotNull(secondHop.getCreatedOn());
        assertNotNull(secondHop.getCreatedById());
        assertNotNull(secondHop.getModifiedOn());
        assertNotNull(secondHop.getModifiedById());
        assertNull(secondHop.getSlaTarget());
        assertNull(secondHop.getSlaTargetDate());
        assertNull(secondHop.getTotalDuration());
        assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), secondHop.getCreatedOn());
        assertEquals(slas.get(1).getHopNumber(), secondHop.getHopNumber());
        assertEquals(slas.get(1).getCloseByBu(), secondHop.getCloseByBu());
        assertEquals(slas.get(1).getSlaTarget(), secondHop.getSlaTarget());
        assertEquals(caseModel.getCaseId(), secondHop.getCases().getCaseId());

        // Third(last) Case sla hop
        CaseSlaHopModel thirdHop = slas.getLast();
        assertNull(thirdHop.getCaseSlaHopId());
        assertNotNull(thirdHop.getTeamId());
        assertNotNull(thirdHop.getTeamName());
        assertNotNull(thirdHop.getCloseByBu());
        assertNotNull(thirdHop.getHopNumber());
        assertNotNull(thirdHop.getStartDatetime());
        assertNotNull(thirdHop.getOwnerId());
        assertNotNull(thirdHop.getOwnerName());
        assertNotNull(thirdHop.getCreatedOn());
        assertNotNull(thirdHop.getCreatedById());
        assertNotNull(thirdHop.getModifiedOn());
        assertNotNull(thirdHop.getModifiedById());
        assertNull(thirdHop.getSlaTarget());
        assertNull(thirdHop.getSlaTargetDate());
        assertNull(thirdHop.getTotalDuration());
        assertNull(thirdHop.getEndDatetime());
        assertEquals(parseToZoneDateTime(stgSlaPerOwnerModel.getLast().getStartDateTimeC()), thirdHop.getStartDatetime());
        assertEquals(secondHop.getEndDatetime(), thirdHop.getCreatedOn());
        assertEquals(parseToZoneDateTime(stgSlaPerOwnerModel.getLast().getStartDateTimeC()), thirdHop.getModifiedOn());
        assertEquals(slas.getLast().getHopNumber(), thirdHop.getHopNumber());
        assertEquals(slas.getLast().getCloseByBu(), thirdHop.getCloseByBu());
        assertEquals(slas.getLast().getSlaTarget(), thirdHop.getSlaTarget());
        assertEquals(caseModel.getCaseId(), thirdHop.getCases().getCaseId());

        // First sla activity
        CaseSlaActivity firstActivity = caseSlaActivities.getFirst();
        assertDefaultCaseSlaActivity(firstActivity);
        assertNotNull(firstActivity.getOwnerId());
        assertNotNull(firstActivity.getOwnerName());
        assertNotNull(firstActivity.getEndDate());
        assertNotNull(firstActivity.getCreatedByName());
        assertNotNull(firstActivity.getModifiedByName());
        assertNotNull(firstActivity.getActualDuration());
        assertEquals(firstHop.getCreatedById(), firstActivity.getCreatedById());
        assertEquals(firstHop.getModifiedById(), firstActivity.getModifiedById());
        assertEquals(firstHop.getOwnerId(), firstActivity.getOwnerId());
        assertEquals(firstHop.getOwnerName(), firstActivity.getOwnerName());
        assertEquals(firstHop.getTeamId(), firstActivity.getTeamId());
        assertEquals(firstHop.getTeamName(), firstActivity.getTeamNameTh());
        assertEquals(firstHop.getHopNumber(), firstActivity.getHopNumberRef());
        assertEquals(caseModel.getCaseNumber(), firstActivity.getCases().getCaseNumber());
        assertEquals(0.0, firstActivity.getActualDuration(), 0.0000001);
        assertEquals(CaseSlaActivityAction.CREATE, firstActivity.getAction());

        // Second sla activity
        CaseSlaActivity secondActivity = caseSlaActivities.get(1);
        assertDefaultCaseSlaActivity(secondActivity);
        assertNotNull(secondActivity.getOwnerId());
        assertNotNull(secondActivity.getOwnerName());
        assertNotNull(secondActivity.getEndDate());
        assertNull(secondActivity.getActualDuration());
        assertEquals(firstHop.getCreatedById(), secondActivity.getCreatedById());
        assertEquals(firstHop.getModifiedById(), secondActivity.getModifiedById());
        assertEquals(firstHop.getOwnerId(), secondActivity.getOwnerId());
        assertEquals(firstHop.getOwnerName(), secondActivity.getOwnerName());
        assertEquals(firstHop.getTeamId(), secondActivity.getTeamId());
        assertEquals(firstHop.getTeamName(), secondActivity.getTeamNameTh());
        assertEquals(firstHop.getHopNumber(), secondActivity.getHopNumberRef());
        assertEquals(caseModel.getCaseNumber(), secondActivity.getCases().getCaseNumber());
        assertEquals(CaseSlaActivityAction.NEXT_HOP, secondActivity.getAction());

        // Third sla activity
        CaseSlaActivity thirdActivity = caseSlaActivities.get(2);
        assertDefaultCaseSlaActivity(thirdActivity);
        assertNotNull(thirdActivity.getEndDate());
        assertNotNull(thirdActivity.getActualDuration());
        assertNull(thirdActivity.getOwnerId());
        assertNull(thirdActivity.getOwnerName());
        assertEquals(firstHop.getOwnerId(), thirdActivity.getCreatedById());
        assertEquals(secondHop.getOwnerId(), thirdActivity.getModifiedById());
        assertEquals(secondHop.getTeamId(), thirdActivity.getTeamId());
        assertEquals(secondHop.getTeamName(), thirdActivity.getTeamNameTh());
        assertEquals(secondHop.getHopNumber(), thirdActivity.getHopNumberRef());
        assertEquals(caseModel.getCaseNumber(), thirdActivity.getCases().getCaseNumber());
        assertEquals(CaseSlaActivityAction.ASSIGN_TEAM, thirdActivity.getAction());

        // Fourth sla activity
        CaseSlaActivity fourthActivity = caseSlaActivities.get(3);
        assertDefaultCaseSlaActivity(fourthActivity);
        assertNotNull(fourthActivity.getEndDate());
        assertNotNull(fourthActivity.getActualDuration());
        assertNotNull(fourthActivity.getOwnerId());
        assertNotNull(fourthActivity.getOwnerName());
        assertEquals(secondHop.getOwnerId(), fourthActivity.getCreatedById());
        assertEquals(secondHop.getOwnerId(), fourthActivity.getModifiedById());
        assertEquals(secondHop.getTeamId(), fourthActivity.getTeamId());
        assertEquals(secondHop.getTeamName(), fourthActivity.getTeamNameTh());
        assertEquals(secondHop.getHopNumber(), fourthActivity.getHopNumberRef());
        assertEquals(caseModel.getCaseNumber(), fourthActivity.getCases().getCaseNumber());
        assertEquals(secondHop.getHopNumber(), fourthActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.ASSIGN_OWNER, fourthActivity.getAction());

        // Fifth sla activity
        CaseSlaActivity fifthActivity = caseSlaActivities.get(4);
        assertDefaultCaseSlaActivity(fifthActivity);
        assertNotNull(fifthActivity.getEndDate());
        assertNotNull(fifthActivity.getOwnerId());
        assertNotNull(fifthActivity.getOwnerName());
        assertNull(fifthActivity.getActualDuration());
        assertEquals(secondHop.getOwnerId(), fifthActivity.getCreatedById());
        assertEquals(secondHop.getOwnerId(), fifthActivity.getModifiedById());
        assertEquals(secondHop.getTeamId(), fifthActivity.getTeamId());
        assertEquals(secondHop.getTeamName(), fifthActivity.getTeamNameTh());
        assertEquals(secondHop.getHopNumber(), fifthActivity.getHopNumberRef());
        assertEquals(secondHop.getHopNumber(), fifthActivity.getHopNumberRef());
        assertEquals(caseModel.getCaseNumber(), fifthActivity.getCases().getCaseNumber());
        assertEquals(CaseSlaActivityAction.NEXT_HOP, fifthActivity.getAction());

        // Sixth sla activity
        CaseSlaActivity sixthActivity = caseSlaActivities.get(5);
        assertDefaultCaseSlaActivity(sixthActivity);
        assertNull(sixthActivity.getEndDate());
        assertNull(sixthActivity.getActualDuration());
        assertNull(sixthActivity.getOwnerId());
        assertNull(sixthActivity.getOwnerName());
        assertEquals(secondHop.getOwnerId(), sixthActivity.getCreatedById());
        assertEquals(secondHop.getOwnerId(), sixthActivity.getModifiedById());
        assertEquals(thirdHop.getTeamId(), sixthActivity.getTeamId());
        assertEquals(thirdHop.getTeamName(), sixthActivity.getTeamNameTh());
        assertEquals(thirdHop.getHopNumber(), sixthActivity.getHopNumberRef());
        assertEquals(caseModel.getCaseNumber(), sixthActivity.getCases().getCaseNumber());
        assertEquals(CaseSlaActivityAction.ASSIGN_TEAM, sixthActivity.getAction());

        // Seventh sla activity
        CaseSlaActivity seventhActivity = caseSlaActivities.get(6);
        assertDefaultCaseSlaActivity(sixthActivity);
        assertNotNull(seventhActivity.getOwnerId());
        assertNotNull(seventhActivity.getOwnerName());
        assertNull(seventhActivity.getActualDuration());
        assertNull(seventhActivity.getEndDate());
        assertEquals(thirdHop.getOwnerId(), seventhActivity.getCreatedById());
        assertEquals(thirdHop.getOwnerId(), seventhActivity.getModifiedById());
        assertEquals(thirdHop.getTeamId(), seventhActivity.getTeamId());
        assertEquals(thirdHop.getTeamName(), seventhActivity.getTeamNameTh());
        assertEquals(thirdHop.getHopNumber(), seventhActivity.getHopNumberRef());
        assertEquals(caseModel.getCaseNumber(), seventhActivity.getCases().getCaseNumber());
        assertEquals(CaseSlaActivityAction.ASSIGN_OWNER, seventhActivity.getAction());
    }

    @Test
    @Disabled
    void caseInProgressWithIntegrationSystemEqualOneApp() {
        ServiceTypeMatrixModel serviceTypeMatrixModel = mockSTM.mockSTMOneAppSFEX();
        stgSlaPerOwnerModel.clear();
        tempCaseModel.setIntegrationSystem(Constant.ONE_APP);
        tempCaseModel.setCreatedByEmployeeIdC("55501");
        tempCaseModel.setCreatedNameC("Thanakorn Chaiyasit");
        tempCaseModel.setCreatedByTeamNew("Inbound Voice Team 1");

        TeamReadonlyModel createdByTeam = mockTeam.mockTeam(tempCaseModel.getCreatedByTeamNew());
        EmployeeUserModel createByUser = mockEmployeeUser.mockEmployeeUser(tempCaseModel.getCreatedByEmployeeIdC());
        when(employeeUserRepository.findByEmployeeId(tempCaseModel.getCreatedByEmployeeIdC()))
                .thenReturn(Optional.of(createByUser));
        when(employeeUserRepository.findByUserId(Objects.requireNonNull(createByUser.getUserId())))
                .thenReturn(Optional.of(createByUser));

        stgSlaPerOwnerModel.clear();
        stgSlaPerOwnerModel.add(mockTempSla.mockTempStgSlaPerOwnerCompleted(
                "500RC00000hs5UiSEJ", "Inbound Voice Team 8", "User B", "55515",
                "2025-06-03 17:18:14", "2025-06-07 04:51:11", "In progress"
        ));
        stgSlaPerOwnerModel.add(mockTempSla.mockTempStgSlaPerOwnerCompleted(
                "500RC00000hs5UiSEJ", "Investment Line", "User C", "99902",
                "2025-06-07 04:51:12", "", "In progress"
        ));

        MasterGroupModel masterGroupModel = new MasterGroupModel();
        masterGroupModel.setId(UUID.randomUUID())
                .setCode(Constant.ORIGINAL_PROBLEM_CHANNEL_GROUP_CODE)
                .setStatusCode(0);

        MasterDataModel masterDataModel = new MasterDataModel();
        masterDataModel.setNameTh("ลูกค้าสัมพันธ์")
                .setId(UUID.randomUUID())
                .setStatusCode(0)
                .setMasterGroup(masterGroupModel);

        masterGroupModel.setMasterData(Collections.singletonList(masterDataModel));

        when(masterDataRepository.findByCodeAndStatusCodeAndMasterGroup_CodeAndMasterGroup_StatusCode(anyString(), Mockito.anyInt(), anyString(), Mockito.anyInt()))
                .thenReturn(Optional.of(masterDataModel));

        when(masterGroupRepository.findByCode(anyString()))
                .thenReturn(Optional.of(masterGroupModel));

        when(serviceTypeMatrixRepository.findByServiceTypeMatrixCode(any()))
                .thenReturn(serviceTypeMatrixModel);

        when(stgSlaPerOwnerRepository.findAllByCaseCOrderByStartDateTimeCAsc(tempCaseModel.getSfId()))
                .thenReturn(stgSlaPerOwnerModel);


        when(teamReadonlyRepository.findByNameTh(anyString()))
                .thenAnswer(inv -> {
                    String teamName = inv.getArgument(0); // ดึง Argument ที่ส่งเข้ามา
                    if (tempCaseModel.getCreatedByTeamNew().equals(teamName)) {
                        // ถ้าเป็นชื่อทีมพิเศษ ให้คืนค่า Optional.of(specialTeam)
                        return Optional.of(createdByTeam);
                    } else {
                        // สำหรับชื่ออื่นๆ ให้ทำตาม mock ปกติ (เช่น mockTeam จาก inv.getArgument)
                        return Optional.of(mockTeam.mockTeam(teamName));
                    }
                });

        EmployeeUserModel normalUser = mockEmployeeUser.mockEmployeeUser("55501");
        when(employeeUserRepository.findByEmployeeId("55501"))
                .thenReturn(Optional.of(normalUser));
        when(employeeUserRepository.findByUserId(normalUser.getUserId()))
                .thenReturn(Optional.of(normalUser));

        EmployeeUserModel user55515 = mockEmployeeUser.mockEmployeeUser("55515");
        when(employeeUserRepository.findByEmployeeIdAndStatusCode("55515", 0))
                .thenReturn(Optional.of(user55515));
        when(employeeUserRepository.findByUserId(user55515.getUserId()))
                .thenReturn(Optional.of(user55515));

        EmployeeUserModel user99902 = mockEmployeeUser.mockEmployeeUser("99902");
        when(employeeUserRepository.findByEmployeeIdAndStatusCode("99902", 0))
                .thenReturn(Optional.of(user99902));
        when(employeeUserRepository.findByUserId(user99902.getUserId()))
                .thenReturn(Optional.of(user99902));

        StgToCaseWriterDTO transaction = createCaseService.createCase(tempCaseModel);
        CaseTransactionModel caseModel = transaction.getCaseTransaction();

        // Validate data mock not null
        assertNotNull(createdByTeam);
        assertNotNull(createByUser);
        // Case
        assertDefaultCase(caseModel);
        assertEquals(tempCaseModel.getCaseNumber(), caseModel.getCaseNumber());
//        assertEquals(createdByTeam.getTeamId(), caseModel.getCreatedByTeamId());
//        assertEquals(createByUser.getUserId(), caseModel.getCreatedById());
        assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), caseModel.getCreatedOn());
        assertEquals(Constant.CASE_STATUS_IN_PROGRESS, caseModel.getCaseStatusCode());
        assertEquals(Constant.ONE_APP, caseModel.getIntegrationSystem());

        assertCaseAlreadyInProgressWithResolveInfo(caseModel);
        assertCaseAlreadyInProgressWithClosedInfo(caseModel);
        assertNotNull(serviceTypeMatrixModel);
        assertServiceTypeMatrix(serviceTypeMatrixModel, caseModel);

        List<ServiceTypeMatrixSla> slas = serviceTypeMatrixModel.getServiceTypeMatrixSlas();
        List<CaseSlaHopModel> caseSlas = caseModel.getSlaHop();
        assertNotNull(slas);
        assertNotNull(caseSlas);
        assertEquals(slas.size(), caseSlas.size());

        // First case sla hop
        CaseSlaHopModel firstHop = caseSlas.getFirst();
        assertNotNull(firstHop);
        assertNull(firstHop.getCaseSlaHopId());
        assertNotNull(firstHop.getTeamId());
        assertNotNull(firstHop.getTeamName());
        assertNotNull(firstHop.getCloseByBu());
        assertNotNull(firstHop.getHopNumber());
        assertNotNull(firstHop.getStartDatetime());
        assertNotNull(firstHop.getEndDatetime());
        assertNotNull(firstHop.getOwnerId());
        assertNotNull(firstHop.getOwnerName());
        assertNotNull(firstHop.getCreatedOn());
        assertNotNull(firstHop.getCreatedById());
        assertNotNull(firstHop.getModifiedOn());
        assertNotNull(firstHop.getModifiedById());
        assertNotNull(firstHop.getTotalDuration());
        assertNull(firstHop.getSlaTarget());
        assertNull(firstHop.getSlaTargetDate());
        assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), firstHop.getStartDatetime());
        assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), firstHop.getEndDatetime());
        assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), firstHop.getCreatedOn());
        assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), firstHop.getModifiedOn());
        assertEquals(slas.getFirst().getHopNumber(), firstHop.getHopNumber());
        assertEquals(slas.getFirst().getCloseByBu(), firstHop.getCloseByBu());
        assertEquals(slas.getFirst().getSlaTarget(), firstHop.getSlaTarget());
        assertEquals(createdByTeam.getNameTh(), firstHop.getTeamName());
        assertEquals(createdByTeam.getTeamId(), firstHop.getTeamId());
        assertEquals(0f, firstHop.getTotalDuration(), 0.0000001);
        assertEquals(caseModel.getCaseId(), firstHop.getCases().getCaseId());

        // Second Case Sla hop
        CaseSlaHopModel secondHop = caseSlas.get(1);
        assertNull(secondHop.getCaseSlaHopId());
        assertNotNull(secondHop.getCloseByBu());
        assertNotNull(secondHop.getHopNumber());
        assertNotNull(secondHop.getStartDatetime());
        assertNotNull(secondHop.getOwnerId());
        assertNotNull(secondHop.getOwnerName());
        assertNotNull(secondHop.getCreatedOn());
        assertNotNull(secondHop.getCreatedById());
        assertNotNull(secondHop.getModifiedOn());
        assertNotNull(secondHop.getModifiedById());
        assertNull(secondHop.getSlaTarget());
        assertNull(secondHop.getSlaTargetDate());
        assertNull(secondHop.getTotalDuration());
        assertNotNull(secondHop.getTeamId());
        assertNotNull(secondHop.getTeamName());
        assertNotNull(secondHop.getEndDatetime());
//        assertEquals(firstHop.getEndDatetime(), secondHop.getStartDatetime());
        assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), secondHop.getCreatedOn());
//        assertEquals(firstHop.getEndDatetime(), secondHop.getModifiedOn());
        assertEquals(slas.get(1).getHopNumber(), secondHop.getHopNumber());
        assertEquals(slas.get(1).getCloseByBu(), secondHop.getCloseByBu());
        assertEquals(caseModel.getCaseId(), secondHop.getCases().getCaseId());

        // Third(last) Case sla hop
        CaseSlaHopModel thirdHop = caseSlas.getLast();
        assertNull(thirdHop.getCaseSlaHopId());
        assertNotNull(thirdHop.getTeamId());
        assertNotNull(thirdHop.getTeamName());
        assertNotNull(thirdHop.getCloseByBu());
        assertNotNull(thirdHop.getHopNumber());
        assertNotNull(thirdHop.getStartDatetime());
        assertNotNull(thirdHop.getOwnerId());
        assertNotNull(thirdHop.getOwnerName());
        assertNotNull(thirdHop.getCreatedOn());
        assertNotNull(thirdHop.getCreatedById());
        assertNotNull(thirdHop.getModifiedOn());
        assertNotNull(thirdHop.getModifiedById());
        assertNull(thirdHop.getSlaTarget());
        assertNull(thirdHop.getSlaTargetDate());
        assertNull(thirdHop.getTotalDuration());
        assertNull(thirdHop.getEndDatetime());
        assertEquals(parseToZoneDateTime(stgSlaPerOwnerModel.getLast().getStartDateTimeC()), thirdHop.getStartDatetime());
        assertEquals(thirdHop.getStartDatetime(), thirdHop.getCreatedOn());
        assertEquals(parseToZoneDateTime(stgSlaPerOwnerModel.getLast().getStartDateTimeC()), thirdHop.getModifiedOn());
        assertEquals(slas.getLast().getHopNumber(), thirdHop.getHopNumber());
        assertEquals(slas.getLast().getCloseByBu(), thirdHop.getCloseByBu());
        assertEquals(slas.getLast().getSlaTarget(), thirdHop.getSlaTarget());
        assertEquals(caseModel.getCaseId(), thirdHop.getCases().getCaseId());

        // Case sla activities
        List<CaseSlaActivity> caseSlaActivities = transaction.getCaseTransaction().getCaseSlaActivities();
        assertNotNull(caseSlaActivities);
        assertEquals(7, caseSlaActivities.size());

        // First sla activity
        CaseSlaActivity firstActivity = caseSlaActivities.getFirst();
        assertDefaultCaseSlaActivity(firstActivity);
        assertNotNull(firstActivity.getOwnerId());
        assertNotNull(firstActivity.getOwnerName());
        assertNotNull(firstActivity.getEndDate());
        assertNotNull(firstActivity.getCreatedByName());
        assertNotNull(firstActivity.getModifiedByName());
        assertNotNull(firstActivity.getActualDuration());
        assertEquals(firstHop.getCreatedById(), firstActivity.getCreatedById());
        assertEquals(firstHop.getModifiedById(), firstActivity.getModifiedById());
        assertEquals(firstHop.getOwnerId(), firstActivity.getOwnerId());
        assertEquals(firstHop.getOwnerName(), firstActivity.getOwnerName());
        assertEquals(firstHop.getTeamId(), firstActivity.getTeamId());
        assertEquals(firstHop.getTeamName(), firstActivity.getTeamNameTh());
        assertEquals(firstHop.getHopNumber(), firstActivity.getHopNumberRef());
        assertEquals(caseModel.getCaseNumber(), firstActivity.getCases().getCaseNumber());
        assertEquals(0.0, firstActivity.getActualDuration(), 0.0000001);
        assertEquals(CaseSlaActivityAction.CREATE, firstActivity.getAction());

        // Second sla activity
        CaseSlaActivity secondActivity = caseSlaActivities.get(1);
        assertDefaultCaseSlaActivity(secondActivity);
        assertNotNull(secondActivity.getOwnerId());
        assertNotNull(secondActivity.getOwnerName());
        assertNotNull(secondActivity.getEndDate());
        assertNull(secondActivity.getActualDuration());
        assertEquals(firstHop.getCreatedById(), secondActivity.getCreatedById());
        assertEquals(firstHop.getModifiedById(), secondActivity.getModifiedById());
        assertEquals(firstHop.getOwnerId(), secondActivity.getOwnerId());
        assertEquals(firstHop.getOwnerName(), secondActivity.getOwnerName());
        assertEquals(firstHop.getTeamId(), secondActivity.getTeamId());
        assertEquals(firstHop.getTeamName(), secondActivity.getTeamNameTh());
        assertEquals(firstHop.getHopNumber(), secondActivity.getHopNumberRef());
        assertEquals(caseModel.getCaseNumber(), secondActivity.getCases().getCaseNumber());
        assertEquals(CaseSlaActivityAction.NEXT_HOP, secondActivity.getAction());

        // Third sla activity
        CaseSlaActivity thirdActivity = caseSlaActivities.get(2);
        assertDefaultCaseSlaActivity(thirdActivity);
        assertNotNull(thirdActivity.getEndDate());
        assertNotNull(thirdActivity.getActualDuration());
        assertNull(thirdActivity.getOwnerId());
        assertNull(thirdActivity.getOwnerName());
        assertEquals(firstHop.getOwnerId(), thirdActivity.getCreatedById());
//        assertEquals(secondHop.getOwnerId(), thirdActivity.getModifiedById());
//        assertEquals(secondHop.getTeamId(), thirdActivity.getTeamId());
//        assertEquals(secondHop.getTeamName(), thirdActivity.getTeamNameTh());
//        assertEquals(secondHop.getHopNumber(), thirdActivity.getHopNumberRef());
//        assertEquals(caseModegetCaseNumber()(), thirdActivity.getCases().getCaseNumber());
        assertEquals(CaseSlaActivityAction.ASSIGN_TEAM, thirdActivity.getAction());

        // Fourth sla activity
        CaseSlaActivity fourthActivity = caseSlaActivities.get(3);
        assertDefaultCaseSlaActivity(fourthActivity);
        assertNotNull(fourthActivity.getEndDate());
        assertNotNull(fourthActivity.getActualDuration());
        assertNotNull(fourthActivity.getOwnerId());
        assertNotNull(fourthActivity.getOwnerName());
        assertEquals(secondHop.getOwnerId(), fourthActivity.getCreatedById());
        assertEquals(secondHop.getOwnerId(), fourthActivity.getModifiedById());
        assertEquals(secondHop.getTeamId(), fourthActivity.getTeamId());
        assertEquals(secondHop.getTeamName(), fourthActivity.getTeamNameTh());
        assertEquals(secondHop.getHopNumber(), fourthActivity.getHopNumberRef());
        assertEquals(caseModel.getCaseNumber(), fourthActivity.getCases().getCaseNumber());
        assertEquals(secondHop.getHopNumber(), fourthActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.ASSIGN_OWNER, fourthActivity.getAction());

        // Fifth sla activity
        CaseSlaActivity fifthActivity = caseSlaActivities.get(4);
        assertDefaultCaseSlaActivity(fifthActivity);
        assertNotNull(fifthActivity.getEndDate());
        assertNotNull(fifthActivity.getOwnerId());
        assertNotNull(fifthActivity.getOwnerName());
        assertNull(fifthActivity.getActualDuration());
        assertEquals(secondHop.getOwnerId(), fifthActivity.getCreatedById());
        assertEquals(secondHop.getOwnerId(), fifthActivity.getModifiedById());
        assertEquals(secondHop.getTeamId(), fifthActivity.getTeamId());
        assertEquals(secondHop.getTeamName(), fifthActivity.getTeamNameTh());
        assertEquals(secondHop.getHopNumber(), fifthActivity.getHopNumberRef());
        assertEquals(secondHop.getHopNumber(), fifthActivity.getHopNumberRef());
        assertEquals(caseModel.getCaseNumber(), fifthActivity.getCases().getCaseNumber());
        assertEquals(CaseSlaActivityAction.NEXT_HOP, fifthActivity.getAction());

        // Sixth sla activity
        CaseSlaActivity sixthActivity = caseSlaActivities.get(5);
        assertDefaultCaseSlaActivity(sixthActivity);
        assertNull(sixthActivity.getEndDate());
        assertNull(sixthActivity.getActualDuration());
        assertNull(sixthActivity.getOwnerId());
        assertNull(sixthActivity.getOwnerName());
        assertEquals(secondHop.getOwnerId(), sixthActivity.getCreatedById());
        assertEquals(secondHop.getOwnerId(), sixthActivity.getModifiedById());
        assertEquals(thirdHop.getTeamId(), sixthActivity.getTeamId());
        assertEquals(thirdHop.getTeamName(), sixthActivity.getTeamNameTh());
        assertEquals(thirdHop.getHopNumber(), sixthActivity.getHopNumberRef());
        assertEquals(caseModel.getCaseNumber(), sixthActivity.getCases().getCaseNumber());
        assertEquals(CaseSlaActivityAction.ASSIGN_TEAM, sixthActivity.getAction());

        // Seventh sla activity
        CaseSlaActivity seventhActivity = caseSlaActivities.get(6);
        assertDefaultCaseSlaActivity(sixthActivity);
        assertNotNull(seventhActivity.getOwnerId());
        assertNotNull(seventhActivity.getOwnerName());
        assertNull(seventhActivity.getActualDuration());
        assertNull(seventhActivity.getEndDate());
        assertEquals(thirdHop.getOwnerId(), seventhActivity.getCreatedById());
        assertEquals(thirdHop.getOwnerId(), seventhActivity.getModifiedById());
        assertEquals(thirdHop.getTeamId(), seventhActivity.getTeamId());
        assertEquals(thirdHop.getTeamName(), seventhActivity.getTeamNameTh());
        assertEquals(thirdHop.getHopNumber(), seventhActivity.getHopNumberRef());
        assertEquals(caseModel.getCaseNumber(), seventhActivity.getCases().getCaseNumber());
        assertEquals(CaseSlaActivityAction.ASSIGN_OWNER, seventhActivity.getAction());
    }

    @Test
    @Disabled
    void caseInProgressWithFixAndStatusNew() {
        ServiceTypeMatrixModel serviceTypeMatrixModel = mockSTM.mockSTMFixWithoutFCR();
        TeamReadonlyModel createdByTeam = mockTeam.mockTeam(tempCaseModel.getCreatedByTeamNew());
        EmployeeUserModel createByUser = mockEmployeeUser.mockEmployeeUser(tempCaseModel.getCreatedByEmployeeIdC());

        tempCaseModel.setStatus(Constant.CASE_STATUS_NEW);
        tempCaseModel.setCreatedByEmployeeIdC("55501");

        stgSlaPerOwnerModel.clear();
        stgSlaPerOwnerModel.add(mockTempSla.mockTempStgSlaPerOwnerCompleted(
                "500RC00000hs5UiSEJ", "Create", "Thanakorn Chaiyasit", "55501",
                "2025-06-03 17:18:13", "2025-06-03 17:18:13", "New"
        ));
        stgSlaPerOwnerModel.add(mockTempSla.mockTempStgSlaPerOwnerCompleted(
                "500RC00000hs5UiSEJ", "Inbound Voice Team 8", "User B", "55515",
                "2025-06-02 17:18:14", "2025-06-07 04:51:11", "In progress"
        ));
        stgSlaPerOwnerModel.add(mockTempSla.mockTempStgSlaPerOwnerCompleted(
                "500RC00000hs5UiSEJ", "Investment Line", "User C", "99902",
                "2025-06-07 04:51:12", "", "In progress"
        ));

        MasterGroupModel masterGroupModel = new MasterGroupModel();
        masterGroupModel.setId(UUID.randomUUID())
                .setCode(Constant.ORIGINAL_PROBLEM_CHANNEL_GROUP_CODE)
                .setStatusCode(0);

        MasterDataModel masterDataModel = new MasterDataModel();
        masterDataModel.setNameTh("ลูกค้าสัมพันธ์")
                .setId(UUID.randomUUID())
                .setStatusCode(0)
                .setMasterGroup(masterGroupModel);

        masterGroupModel.setMasterData(Collections.singletonList(masterDataModel));

         when(serviceTypeMatrixRepository.findByServiceTypeMatrixCode(any()))
                .thenReturn(serviceTypeMatrixModel);

        when(teamReadonlyRepository.findByNameTh(tempCaseModel.getCreatedByTeamNew()))
                .thenReturn(Optional.of(createdByTeam));
        when(employeeUserRepository.findByEmployeeId(tempCaseModel.getCreatedByEmployeeIdC()))
                .thenReturn(Optional.of(createByUser));
        when(employeeUserRepository.findByUserId(Objects.requireNonNull(createByUser.getUserId())))
                .thenReturn(Optional.of(createByUser));

        when(masterDataRepository.findByCodeAndStatusCodeAndMasterGroup_CodeAndMasterGroup_StatusCode(anyString(), Mockito.anyInt(), anyString(), Mockito.anyInt()))
                .thenReturn(Optional.of(masterDataModel));

        when(masterGroupRepository.findByCode(anyString()))
                .thenReturn(Optional.of(masterGroupModel));

        when(stgSlaPerOwnerRepository.findAllByCaseCOrderByStartDateTimeCAsc(tempCaseModel.getSfId()))
                .thenReturn(stgSlaPerOwnerModel);

        EmployeeUserModel user55515 = mockEmployeeUser.mockEmployeeUser("55515");
        when(employeeUserRepository.findByEmployeeIdAndStatusCode("55515", 0))
                .thenReturn(Optional.of(user55515));
        when(employeeUserRepository.findByUserId(user55515.getUserId()))
                .thenReturn(Optional.of(user55515));

        EmployeeUserModel user99902 = mockEmployeeUser.mockEmployeeUser("99902");
        when(employeeUserRepository.findByEmployeeIdAndStatusCode("99902", 0))
                .thenReturn(Optional.of(user99902));
        when(employeeUserRepository.findByUserId(user99902.getUserId()))
                .thenReturn(Optional.of(user99902));

        StgToCaseWriterDTO transaction = createCaseService.createCase(tempCaseModel);
        CaseTransactionModel caseModel = transaction.getCaseTransaction();

        // Validate data mock not null
        assertNotNull(createdByTeam);
        assertNotNull(createByUser);
        // Case
        assertDefaultCase(caseModel);
        assertEquals(tempCaseModel.getCaseNumber(), caseModel.getCaseNumber());
        assertEquals(createdByTeam.getTeamId(), caseModel.getCreatedByTeamId());
        assertEquals(createByUser.getUserId(), caseModel.getCreatedById());
        assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), caseModel.getCreatedOn());
        assertEquals(Constant.CASE_STATUS_IN_PROGRESS, caseModel.getCaseStatusCode());
        assertEquals(Constant.TEP, caseModel.getIntegrationSystem());

        assertCaseAlreadyInProgressWithResolveInfo(caseModel);
        assertCaseAlreadyInProgressWithClosedInfo(caseModel);
        assertNotNull(serviceTypeMatrixModel);
        assertServiceTypeMatrix(serviceTypeMatrixModel, caseModel);

        List<ServiceTypeMatrixSla> slas = serviceTypeMatrixModel.getServiceTypeMatrixSlas();
        List<CaseSlaHopModel> caseSlas = caseModel.getSlaHop();
        assertNotNull(slas);
        assertNotNull(caseSlas);
        assertEquals(slas.size(), caseSlas.size());

        // First case sla hop
        CaseSlaHopModel firstHop = caseSlas.getFirst();
        assertNotNull(firstHop);
        assertNull(firstHop.getCaseSlaHopId());
        assertNotNull(firstHop.getTeamId());
        assertNotNull(firstHop.getTeamName());
        assertNotNull(firstHop.getCloseByBu());
        assertNotNull(firstHop.getHopNumber());
        assertNotNull(firstHop.getStartDatetime());
        assertNotNull(firstHop.getEndDatetime());
        assertNotNull(firstHop.getOwnerId());
        assertNotNull(firstHop.getOwnerName());
        assertNotNull(firstHop.getCreatedOn());
        assertNotNull(firstHop.getCreatedById());
        assertNotNull(firstHop.getModifiedOn());
        assertNotNull(firstHop.getModifiedById());
        assertNotNull(firstHop.getSlaTarget());
        assertNotNull(firstHop.getSlaTargetDate());
        assertNotNull(firstHop.getTotalDuration());
        assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), firstHop.getStartDatetime());
        assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), firstHop.getEndDatetime());
        assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), firstHop.getCreatedOn());
        assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), firstHop.getModifiedOn());
        assertEquals(slas.getFirst().getHopNumber(), firstHop.getHopNumber());
        assertEquals(slas.getFirst().getCloseByBu(), firstHop.getCloseByBu());
        assertEquals(slas.getFirst().getSlaTarget(), firstHop.getSlaTarget());
        assertEquals(createdByTeam.getNameTh(), firstHop.getTeamName());
        assertEquals(createdByTeam.getTeamId(), firstHop.getTeamId());
        assertEquals(caseModel.getCaseId(), firstHop.getCases().getCaseId());

        // Second Case Sla hop
        CaseSlaHopModel secondHop = caseSlas.get(1);
        assertNull(secondHop.getCaseSlaHopId());
        assertNotNull(secondHop.getCloseByBu());
        assertNotNull(secondHop.getHopNumber());
        assertNotNull(secondHop.getStartDatetime());
        assertNotNull(secondHop.getOwnerId());
        assertNotNull(secondHop.getOwnerName());
        assertNotNull(secondHop.getCreatedOn());
        assertNotNull(secondHop.getCreatedById());
        assertNotNull(secondHop.getModifiedOn());
        assertNotNull(secondHop.getModifiedById());
        assertNotNull(secondHop.getSlaTarget());
        assertNotNull(secondHop.getSlaTargetDate());
        assertNotNull(secondHop.getTotalDuration());
        assertNotNull(secondHop.getTeamId());
        assertNotNull(secondHop.getTeamName());
        assertNotNull(secondHop.getEndDatetime());
//        assertEquals(firstHop.getEndDatetime(), secondHop.getStartDatetime());
        assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), secondHop.getCreatedOn());
//        assertEquals(firstHop.getEndDatetime(), secondHop.getModifiedOn());
        assertEquals(slas.get(1).getHopNumber(), secondHop.getHopNumber());
        assertEquals(slas.get(1).getCloseByBu(), secondHop.getCloseByBu());
        assertEquals(caseModel.getCaseId(), secondHop.getCases().getCaseId());

        // Third(last) Case sla hop
        CaseSlaHopModel thirdHop = caseSlas.getLast();
        assertNull(thirdHop.getCaseSlaHopId());
        assertNotNull(thirdHop.getTeamId());
        assertNotNull(thirdHop.getTeamName());
        assertNotNull(thirdHop.getCloseByBu());
        assertNotNull(thirdHop.getHopNumber());
        assertNotNull(thirdHop.getStartDatetime());
        assertNotNull(thirdHop.getOwnerId());
        assertNotNull(thirdHop.getOwnerName());
        assertNotNull(thirdHop.getCreatedOn());
        assertNotNull(thirdHop.getCreatedById());
        assertNotNull(thirdHop.getModifiedOn());
        assertNotNull(thirdHop.getModifiedById());
        assertNotNull(thirdHop.getSlaTarget());
        assertNotNull(thirdHop.getSlaTargetDate());
        assertNull(thirdHop.getTotalDuration());
        assertNull(thirdHop.getEndDatetime());
        assertEquals(parseToZoneDateTime(stgSlaPerOwnerModel.getLast().getStartDateTimeC()), thirdHop.getStartDatetime());
        assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), thirdHop.getCreatedOn());
        assertEquals(parseToZoneDateTime(stgSlaPerOwnerModel.getLast().getStartDateTimeC()), thirdHop.getModifiedOn());
        assertEquals(slas.getLast().getHopNumber(), thirdHop.getHopNumber());
        assertEquals(slas.getLast().getCloseByBu(), thirdHop.getCloseByBu());
        assertEquals(slas.getLast().getSlaTarget(), thirdHop.getSlaTarget());
        assertEquals(caseModel.getCaseId(), thirdHop.getCases().getCaseId());

        // Case sla activities
        List<CaseSlaActivity> caseSlaActivities = transaction.getCaseTransaction().getCaseSlaActivities();
        assertNotNull(caseSlaActivities);
        assertEquals(7, caseSlaActivities.size());

        // First sla activity
        CaseSlaActivity firstActivity = caseSlaActivities.getFirst();
        assertDefaultCaseSlaActivity(firstActivity);
        assertNotNull(firstActivity.getOwnerId());
        assertNotNull(firstActivity.getOwnerName());
        assertNotNull(firstActivity.getEndDate());
        assertNotNull(firstActivity.getCreatedByName());
        assertNotNull(firstActivity.getModifiedByName());
        assertNotNull(firstActivity.getActualDuration());
        assertEquals(firstHop.getCreatedById(), firstActivity.getCreatedById());
        assertEquals(firstHop.getModifiedById(), firstActivity.getModifiedById());
        assertEquals(firstHop.getOwnerId(), firstActivity.getOwnerId());
        assertEquals(firstHop.getOwnerName(), firstActivity.getOwnerName());
        assertEquals(firstHop.getTeamId(), firstActivity.getTeamId());
        assertEquals(firstHop.getTeamName(), firstActivity.getTeamNameTh());
        assertEquals(firstHop.getHopNumber(), firstActivity.getHopNumberRef());
        assertEquals(caseModel.getCaseNumber(), firstActivity.getCases().getCaseNumber());
        assertEquals(0.0, firstActivity.getActualDuration(), 0.0000001);
        assertEquals(CaseSlaActivityAction.CREATE, firstActivity.getAction());

        // Second sla activity
        CaseSlaActivity secondActivity = caseSlaActivities.get(1);
        assertDefaultCaseSlaActivity(secondActivity);
        assertNotNull(secondActivity.getOwnerId());
        assertNotNull(secondActivity.getOwnerName());
        assertNotNull(secondActivity.getEndDate());
        assertNull(secondActivity.getActualDuration());
        assertEquals(firstHop.getCreatedById(), secondActivity.getCreatedById());
        assertEquals(firstHop.getModifiedById(), secondActivity.getModifiedById());
        assertEquals(firstHop.getOwnerId(), secondActivity.getOwnerId());
        assertEquals(firstHop.getOwnerName(), secondActivity.getOwnerName());
        assertEquals(firstHop.getTeamId(), secondActivity.getTeamId());
        assertEquals(firstHop.getTeamName(), secondActivity.getTeamNameTh());
        assertEquals(firstHop.getHopNumber(), secondActivity.getHopNumberRef());
        assertEquals(caseModel.getCaseNumber(), secondActivity.getCases().getCaseNumber());
        assertEquals(CaseSlaActivityAction.NEXT_HOP, secondActivity.getAction());

        // Third sla activity
        CaseSlaActivity thirdActivity = caseSlaActivities.get(2);
        assertDefaultCaseSlaActivity(thirdActivity);
        assertNotNull(thirdActivity.getEndDate());
        assertNotNull(thirdActivity.getActualDuration());
        assertNull(thirdActivity.getOwnerId());
        assertNull(thirdActivity.getOwnerName());
        assertEquals(firstHop.getOwnerId(), thirdActivity.getCreatedById());

        assertEquals(secondHop.getOwnerId(), thirdActivity.getModifiedById());
        assertEquals(secondHop.getTeamId(), thirdActivity.getTeamId());
        assertEquals(secondHop.getTeamName(), thirdActivity.getTeamNameTh());
        assertEquals(secondHop.getHopNumber(), thirdActivity.getHopNumberRef());
        assertEquals(caseModel.getCaseNumber(), thirdActivity.getCases().getCaseNumber());

        assertEquals(CaseSlaActivityAction.ASSIGN_TEAM, thirdActivity.getAction());

        // Fourth sla activity
        CaseSlaActivity fourthActivity = caseSlaActivities.get(3);
        assertDefaultCaseSlaActivity(fourthActivity);
        assertNotNull(fourthActivity.getEndDate());
        assertNotNull(fourthActivity.getActualDuration());
        assertNotNull(fourthActivity.getOwnerId());
        assertNotNull(fourthActivity.getOwnerName());
        assertEquals(secondHop.getOwnerId(), fourthActivity.getCreatedById());
        assertEquals(secondHop.getOwnerId(), fourthActivity.getModifiedById());
        assertEquals(secondHop.getTeamId(), fourthActivity.getTeamId());
        assertEquals(secondHop.getTeamName(), fourthActivity.getTeamNameTh());
        assertEquals(secondHop.getHopNumber(), fourthActivity.getHopNumberRef());
        assertEquals(caseModel.getCaseNumber(), fourthActivity.getCases().getCaseNumber());
        assertEquals(secondHop.getHopNumber(), fourthActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.ASSIGN_OWNER, fourthActivity.getAction());

        // Fifth sla activity
        CaseSlaActivity fifthActivity = caseSlaActivities.get(4);
        assertDefaultCaseSlaActivity(fifthActivity);
        assertNotNull(fifthActivity.getEndDate());
        assertNotNull(fifthActivity.getOwnerId());
        assertNotNull(fifthActivity.getOwnerName());
        assertNull(fifthActivity.getActualDuration());
        assertEquals(secondHop.getOwnerId(), fifthActivity.getCreatedById());
        assertEquals(secondHop.getOwnerId(), fifthActivity.getModifiedById());
        assertEquals(secondHop.getTeamId(), fifthActivity.getTeamId());
        assertEquals(secondHop.getTeamName(), fifthActivity.getTeamNameTh());
        assertEquals(secondHop.getHopNumber(), fifthActivity.getHopNumberRef());
        assertEquals(secondHop.getHopNumber(), fifthActivity.getHopNumberRef());
        assertEquals(caseModel.getCaseNumber(), fifthActivity.getCases().getCaseNumber());
        assertEquals(CaseSlaActivityAction.NEXT_HOP, fifthActivity.getAction());

        // Sixth sla activity
        CaseSlaActivity sixthActivity = caseSlaActivities.get(5);
        assertDefaultCaseSlaActivity(sixthActivity);
        assertNull(sixthActivity.getEndDate());
        assertNull(sixthActivity.getActualDuration());
        assertNull(sixthActivity.getOwnerId());
        assertNull(sixthActivity.getOwnerName());
        assertEquals(secondHop.getOwnerId(), sixthActivity.getCreatedById());
        assertEquals(secondHop.getOwnerId(), sixthActivity.getModifiedById());
        assertEquals(thirdHop.getTeamId(), sixthActivity.getTeamId());
        assertEquals(thirdHop.getTeamName(), sixthActivity.getTeamNameTh());
        assertEquals(thirdHop.getHopNumber(), sixthActivity.getHopNumberRef());
        assertEquals(caseModel.getCaseNumber(), sixthActivity.getCases().getCaseNumber());
        assertEquals(CaseSlaActivityAction.ASSIGN_TEAM, sixthActivity.getAction());

        // Seventh sla activity
        CaseSlaActivity seventhActivity = caseSlaActivities.get(6);
        assertDefaultCaseSlaActivity(sixthActivity);
        assertNotNull(seventhActivity.getOwnerId());
        assertNotNull(seventhActivity.getOwnerName());
        assertNull(seventhActivity.getActualDuration());
        assertNull(seventhActivity.getEndDate());
        assertEquals(thirdHop.getOwnerId(), seventhActivity.getCreatedById());
        assertEquals(thirdHop.getOwnerId(), seventhActivity.getModifiedById());
        assertEquals(thirdHop.getTeamId(), seventhActivity.getTeamId());
        assertEquals(thirdHop.getTeamName(), seventhActivity.getTeamNameTh());
        assertEquals(thirdHop.getHopNumber(), seventhActivity.getHopNumberRef());
        assertEquals(caseModel.getCaseNumber(), seventhActivity.getCases().getCaseNumber());
        assertEquals(CaseSlaActivityAction.ASSIGN_OWNER, seventhActivity.getAction());
    }

    @Test
    @Disabled
    void caseInProgressWithDynamicAndStatusNewWithSlaPerOwner() {
        ServiceTypeMatrixModel serviceTypeMatrixModel = mockSTM.mockSTMDynamicWithOutFCR();
        TeamReadonlyModel createdByTeam = mockTeam.mockTeam(tempCaseModel.getCreatedByTeamNew());
        EmployeeUserModel createByUser = mockEmployeeUser.mockEmployeeUser(tempCaseModel.getCreatedByEmployeeIdC());

        tempCaseModel.setStatus("New");
        tempCaseModel.setCreatedByEmployeeIdC("90021");

        stgSlaPerOwnerModel.clear();
//        tempSlaModel.add(mockTempSla.mockTempStgSlaPerOwnerCompleted(
//                "500Ij0000024F53IAE", "Create", "User A", "90021",
//                "2025-06-14 09:32:36", "2025-06-14 10:00:00", "New"
//        ));
        stgSlaPerOwnerModel.add(mockTempSla.mockTempStgSlaPerOwnerCompleted(
                "500Ij0000024F53IAE", "Inbound Voice Team 1", "User B", "55515",
                "2025-06-14 10:00:00", "2025-06-14 11:00:00", "In progress"
        ));
        stgSlaPerOwnerModel.add(mockTempSla.mockTempStgSlaPerOwnerCompleted(
                "500Ij0000024F53IAE", "Investment Line", "User C", "99902",
                "2025-06-14 11:00:00", "", "In progress"
        ));

        MasterGroupModel masterGroupModel = new MasterGroupModel();
        masterGroupModel.setId(UUID.randomUUID())
                .setCode(Constant.ORIGINAL_PROBLEM_CHANNEL_GROUP_CODE)
                .setStatusCode(0);

        MasterDataModel masterDataModel = new MasterDataModel();
        masterDataModel.setNameTh("ลูกค้าสัมพันธ์")
                .setId(UUID.randomUUID())
                .setStatusCode(0)
                .setMasterGroup(masterGroupModel);

        masterGroupModel.setMasterData(Collections.singletonList(masterDataModel));

         when(serviceTypeMatrixRepository.findByServiceTypeMatrixCode(any()))
                .thenReturn(serviceTypeMatrixModel);

//        when(teamReadonlyRepository.findByNameTh(tempCaseModel.getCreatedByTeamNew()))
//                .thenReturn(Optional.of(createdByTeam));
        when(teamReadonlyRepository.findByNameTh(anyString()))
                .thenAnswer(inv -> {
                    String teamName = inv.getArgument(0);
                    if (tempCaseModel.getCreatedByTeamNew().equals(teamName)) {
                        return Optional.of(createdByTeam);
                    } else {
                        return Optional.of(mockTeam.mockTeam(teamName));
                    }
                });
//        when(employeeUserRepository.findByEmployeeId(tempCaseModel.getCreatedByEmployeeIdC()))
//                .thenReturn(Optional.of(createByUser));
        when(employeeUserRepository.findByEmployeeId(anyString()))
                .thenAnswer(inv -> {
                    String employeeId = inv.getArgument(0);
                    if (tempCaseModel.getCreatedByEmployeeIdC().equals(employeeId)) {
                        return Optional.of(createByUser);
                    } else {
                        return Optional.of(mockEmployeeUser.mockEmployeeUser((String) inv.getArgument(0)));
                    }
                });
        when(employeeUserRepository.findByUserId(Objects.requireNonNull(createByUser.getUserId())))
                .thenReturn(Optional.of(createByUser));
        when(employeeUserRepository.findByEmployeeIdAndStatusCode(anyString(), anyInt()))
                .thenAnswer(inv -> Optional.of(mockEmployeeUser.mockEmployeeUser((String) inv.getArgument(0))));
        when(employeeUserRepository.findByUserId(any(UUID.class)))
                .thenAnswer(invocation -> {
                    UUID uuid = invocation.getArgument(0);
                    return Optional.of(mockEmployeeUser.mockEmployeeUser(uuid.toString()));
                });

        when(masterDataRepository.findByCodeAndStatusCodeAndMasterGroup_CodeAndMasterGroup_StatusCode(anyString(), Mockito.anyInt(), anyString(), Mockito.anyInt()))
                .thenReturn(Optional.of(masterDataModel));

        when(masterGroupRepository.findByCode(anyString()))
                .thenReturn(Optional.of(masterGroupModel));

        when(stgSlaPerOwnerRepository.findAllByCaseCOrderByStartDateTimeCAsc(tempCaseModel.getSfId()))
                .thenReturn(stgSlaPerOwnerModel);

        EmployeeUserModel user55515 = mockEmployeeUser.mockEmployeeUser("55515");
        when(employeeUserRepository.findByEmployeeIdAndStatusCode("55515", 0))
                .thenReturn(Optional.of(user55515));
//        when(employeeUserRepository.findByUserId(Objects.requireNonNull(user55515.getUserId())))
//                .thenReturn(Optional.of(user55515));

        EmployeeUserModel user99902 = mockEmployeeUser.mockEmployeeUser("99902");
        when(employeeUserRepository.findByEmployeeIdAndStatusCode("99902", 0))
                .thenReturn(Optional.of(user99902));
//        when(employeeUserRepository.findByUserId(user99902.getUserId()))
//                .thenReturn(Optional.of(user99902));

        StgToCaseWriterDTO transaction = createCaseService.createCase(tempCaseModel);
        CaseTransactionModel caseModel = transaction.getCaseTransaction();

        // Validate data mock not null
        assertNotNull(createdByTeam);
        assertNotNull(createByUser);
        // Case
        assertDefaultCase(caseModel);
        assertEquals(tempCaseModel.getCaseNumber(), caseModel.getCaseNumber());
        assertEquals(createByUser.getUserId(), caseModel.getCreatedById());
        assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), caseModel.getCreatedOn());
        assertEquals(Constant.CASE_STATUS_IN_PROGRESS, caseModel.getCaseStatusCode());
        assertEquals(Constant.TEP, caseModel.getIntegrationSystem());

        assertCaseAlreadyInProgressWithResolveInfo(caseModel);
        assertCaseAlreadyInProgressWithClosedInfo(caseModel);
        assertNotNull(serviceTypeMatrixModel);
        assertServiceTypeMatrix(serviceTypeMatrixModel, caseModel);

        List<ServiceTypeMatrixSla> slas = serviceTypeMatrixModel.getServiceTypeMatrixSlas();
        List<CaseSlaHopModel> caseSlas = caseModel.getSlaHop();

        assertNotNull(slas);
        assertNotNull(caseSlas);
        assertEquals(3, caseSlas.size());

        // First case sla hop
        CaseSlaHopModel firstHop = caseSlas.getFirst();
        assertNotNull(firstHop);
        assertNull(firstHop.getCaseSlaHopId());
        assertNotNull(firstHop.getTeamId());
        assertNotNull(firstHop.getTeamName());
        assertNotNull(firstHop.getCloseByBu());
        assertNotNull(firstHop.getHopNumber());
        assertNotNull(firstHop.getStartDatetime());
        assertNotNull(firstHop.getEndDatetime());
        assertNotNull(firstHop.getOwnerId());
        assertNotNull(firstHop.getOwnerName());
        assertNotNull(firstHop.getCreatedOn());
        assertNotNull(firstHop.getCreatedById());
        assertNotNull(firstHop.getModifiedOn());
        assertNotNull(firstHop.getModifiedById());
        assertNotNull(firstHop.getTotalDuration());
        assertNull(firstHop.getSlaTarget());
        assertNull(firstHop.getSlaTargetDate());
//        assertEquals(DateTimeUtils.parseToZoneDateTime(tempSlaModel.getFirst().getStartDateTimeC()), firstHop.getStartDatetime());
//        assertEquals(DateTimeUtils.parseToZoneDateTime(tempCaseModel.getCreatedDate()), firstHop.getStartDatetime());
//        assertEquals(DateTimeUtils.parseToZoneDateTime(tempCaseModel.getCreatedDate()), firstHop.getEndDatetime());
        assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), firstHop.getCreatedOn());
//        assertEquals(DateTimeUtils.parseToZoneDateTime(tempCaseModel.getCreatedDate()), firstHop.getModifiedOn());
        assertEquals(slas.getFirst().getHopNumber(), firstHop.getHopNumber());
        assertEquals(slas.getFirst().getCloseByBu(), firstHop.getCloseByBu());
        assertEquals(slas.getFirst().getSlaTarget(), firstHop.getSlaTarget());
        assertEquals(createdByTeam.getNameTh(), firstHop.getTeamName());
        assertEquals(createdByTeam.getTeamId(), firstHop.getTeamId());
        assertEquals(0f, firstHop.getTotalDuration(), 0.0000001);
        assertEquals(caseModel.getCaseId(), firstHop.getCases().getCaseId());

        // Third(last) Case sla hop
        CaseSlaHopModel secondHop = caseSlas.get(1);
        assertNull(secondHop.getCaseSlaHopId());
        assertNotNull(secondHop.getTeamId());
        assertNotNull(secondHop.getTeamName());
        assertNotNull(secondHop.getCloseByBu());
        assertNotNull(secondHop.getHopNumber());
        assertNotNull(secondHop.getStartDatetime());
        assertNotNull(secondHop.getOwnerId());
        assertNotNull(secondHop.getOwnerName());
        assertNotNull(secondHop.getCreatedOn());
        assertNotNull(secondHop.getCreatedById());
        assertNotNull(secondHop.getModifiedOn());
        assertNotNull(secondHop.getModifiedById());
        assertNull(secondHop.getSlaTarget());
        assertNull(secondHop.getSlaTargetDate());
        assertNull(secondHop.getTotalDuration());
        assertNotNull(secondHop.getEndDatetime());
//        assertEquals(DateTimeUtils.parseToZoneDateTime(tempSlaModel.getLast().getStartDateTimeC()), secondHop.getStartDatetime());
//        assertEquals(DateTimeUtils.parseToZoneDateTime(tempCaseModel.getCreatedDate()), secondHop.getCreatedOn());
        assertEquals(parseToZoneDateTime(stgSlaPerOwnerModel.getLast().getStartDateTimeC()), secondHop.getModifiedOn());
//        assertEquals(slas.getLast().getHopNumber(), secondHop.getHopNumber());
        assertEquals(slas.get(1).getCloseByBu(), secondHop.getCloseByBu());
        assertEquals(slas.get(1).getSlaTarget(), secondHop.getSlaTarget());
        assertEquals(caseModel.getCaseId(), secondHop.getCases().getCaseId());

        // Fourth Case sla hop
        CaseSlaHopModel thirdHop = caseSlas.get(2);
        assertNull(thirdHop.getCaseSlaHopId());
        assertNotNull(thirdHop.getTeamId());
        assertNotNull(thirdHop.getTeamName());
        assertNotNull(thirdHop.getCloseByBu());
        assertNotNull(thirdHop.getHopNumber());
        assertNotNull(thirdHop.getStartDatetime());
        assertNotNull(thirdHop.getOwnerId());
        assertNotNull(thirdHop.getOwnerName());
        assertNotNull(thirdHop.getCreatedOn());
        assertNotNull(thirdHop.getCreatedById());
        assertNotNull(thirdHop.getModifiedOn());
        assertNotNull(thirdHop.getModifiedById());
        assertNull(thirdHop.getEndDatetime());
        assertNull(thirdHop.getSlaTarget());
        assertNull(thirdHop.getSlaTargetDate());
        assertNull(thirdHop.getTotalDuration());
        assertEquals(parseToZoneDateTime(stgSlaPerOwnerModel.get(1).getStartDateTimeC()), thirdHop.getStartDatetime());
        assertEquals(parseToZoneDateTime(stgSlaPerOwnerModel.get(1).getStartDateTimeC()), thirdHop.getCreatedOn());
//        assertEquals(DateTimeUtils.parseToZoneDateTime(tempSlaModel.get(1).getEndDateTimeC()), thirdHop.getModifiedOn());
        assertEquals(3, thirdHop.getHopNumber());
        assertEquals(true, thirdHop.getCloseByBu());
        assertEquals(caseModel.getCaseId(), thirdHop.getCases().getCaseId());

        // Case sla activities
        List<CaseSlaActivity> caseSlaActivities = transaction.getCaseTransaction().getCaseSlaActivities();
        assertNotNull(caseSlaActivities);
        assertEquals(7, caseSlaActivities.size());

        // First sla activity
        CaseSlaActivity firstActivity = caseSlaActivities.getFirst();
        assertDefaultCaseSlaActivity(firstActivity);
        assertNotNull(firstActivity.getOwnerId());
        assertNotNull(firstActivity.getOwnerName());
        assertNotNull(firstActivity.getEndDate());
        assertNotNull(firstActivity.getCreatedByName());
        assertNotNull(firstActivity.getModifiedByName());
        assertNotNull(firstActivity.getActualDuration());
        assertEquals(firstHop.getCreatedById(), firstActivity.getCreatedById());
        assertEquals(firstHop.getModifiedById(), firstActivity.getModifiedById());
        assertEquals(firstHop.getOwnerId(), firstActivity.getOwnerId());
        assertEquals(firstHop.getOwnerName(), firstActivity.getOwnerName());
        assertEquals(firstHop.getTeamId(), firstActivity.getTeamId());
        assertEquals(firstHop.getTeamName(), firstActivity.getTeamNameTh());
        assertEquals(firstHop.getHopNumber(), firstActivity.getHopNumberRef());
        assertEquals(caseModel.getCaseNumber(), firstActivity.getCases().getCaseNumber());
        assertEquals(0.0, firstActivity.getActualDuration(), 0.0000001);
        assertEquals(CaseSlaActivityAction.CREATE, firstActivity.getAction());

        // Second sla activity
        CaseSlaActivity secondActivity = caseSlaActivities.get(1);
        assertDefaultCaseSlaActivity(secondActivity);
        assertNotNull(secondActivity.getOwnerId());
        assertNotNull(secondActivity.getOwnerName());
        assertNotNull(secondActivity.getEndDate());
        assertNull(secondActivity.getActualDuration());
        assertEquals(firstHop.getCreatedById(), secondActivity.getCreatedById());
//        assertEquals(firstHop.getModifiedById(), secondActivity.getModifiedById());
        assertEquals(firstHop.getOwnerId(), secondActivity.getOwnerId());
        assertEquals(firstHop.getOwnerName(), secondActivity.getOwnerName());
        assertEquals(firstHop.getTeamId(), secondActivity.getTeamId());
        assertEquals(firstHop.getTeamName(), secondActivity.getTeamNameTh());
        assertEquals(firstHop.getHopNumber(), secondActivity.getHopNumberRef());
        assertEquals(caseModel.getCaseNumber(), secondActivity.getCases().getCaseNumber());
        assertEquals(CaseSlaActivityAction.NEXT_HOP, secondActivity.getAction());

        // Third sla activity
        CaseSlaActivity thirdActivity = caseSlaActivities.get(2);
        assertDefaultCaseSlaActivity(thirdActivity);
        assertNotNull(thirdActivity.getEndDate());
        assertNotNull(thirdActivity.getActualDuration());
        assertNull(thirdActivity.getOwnerId());
        assertNull(thirdActivity.getOwnerName());
//        assertEquals(firstHop.getOwnerId(), thirdActivity.getCreatedById());

//        assertEquals(secondHop.getOwnerId(), thirdActivity.getModifiedById());
        assertEquals(secondHop.getTeamId(), thirdActivity.getTeamId());
        assertEquals(secondHop.getTeamName(), thirdActivity.getTeamNameTh());
        assertEquals(secondHop.getHopNumber(), thirdActivity.getHopNumberRef());
        assertEquals(caseModel.getCaseNumber(), thirdActivity.getCases().getCaseNumber());

        assertEquals(CaseSlaActivityAction.ASSIGN_TEAM, thirdActivity.getAction());

        // Fourth sla activity
        CaseSlaActivity fourthActivity = caseSlaActivities.get(3);
        assertDefaultCaseSlaActivity(fourthActivity);
        assertNotNull(fourthActivity.getEndDate());
        assertNotNull(fourthActivity.getActualDuration());
        assertNotNull(fourthActivity.getOwnerId());
        assertNotNull(fourthActivity.getOwnerName());
//        assertEquals(secondHop.getOwnerId(), fourthActivity.getCreatedById());
//        assertEquals(secondHop.getOwnerId(), fourthActivity.getModifiedById());
        assertEquals(secondHop.getTeamId(), fourthActivity.getTeamId());
        assertEquals(secondHop.getTeamName(), fourthActivity.getTeamNameTh());
        assertEquals(secondHop.getHopNumber(), fourthActivity.getHopNumberRef());
        assertEquals(caseModel.getCaseNumber(), fourthActivity.getCases().getCaseNumber());
        assertEquals(secondHop.getHopNumber(), fourthActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.ASSIGN_OWNER, fourthActivity.getAction());

        // Fifth sla activity
        CaseSlaActivity fifthActivity = caseSlaActivities.get(4);
        assertDefaultCaseSlaActivity(fifthActivity);
        assertNotNull(fifthActivity.getEndDate());
        assertNotNull(fifthActivity.getOwnerId());
        assertNotNull(fifthActivity.getOwnerName());
        assertNull(fifthActivity.getActualDuration());
        assertEquals(caseModel.getCaseNumber(), fifthActivity.getCases().getCaseNumber());
        assertEquals(CaseSlaActivityAction.NEXT_HOP, fifthActivity.getAction());

        // Sixth sla activity
        CaseSlaActivity sixthActivity = caseSlaActivities.get(5);
        assertDefaultCaseSlaActivity(sixthActivity);
        assertNull(sixthActivity.getEndDate());
        assertNull(sixthActivity.getActualDuration());
        assertNull(sixthActivity.getOwnerId());
        assertNull(sixthActivity.getOwnerName());
        assertEquals(caseModel.getCaseNumber(), sixthActivity.getCases().getCaseNumber());
        assertEquals(CaseSlaActivityAction.ASSIGN_TEAM, sixthActivity.getAction());

        // Seventh sla activity
        CaseSlaActivity seventhActivity = caseSlaActivities.get(6);
        assertDefaultCaseSlaActivity(sixthActivity);
        assertNotNull(seventhActivity.getOwnerId());
        assertNotNull(seventhActivity.getOwnerName());
        assertNull(seventhActivity.getActualDuration());
        assertNull(seventhActivity.getEndDate());
        assertEquals(caseModel.getCaseNumber(), seventhActivity.getCases().getCaseNumber());
        assertEquals(CaseSlaActivityAction.ASSIGN_OWNER, seventhActivity.getAction());
    }

    @Test
    void caseInProgressWithDynamicAndStatusNewWithOutSlaPerOwner() {
        ServiceTypeMatrixModel serviceTypeMatrixModel = mockSTM.mockSTMDynamicWithOutFCR();
        TeamReadonlyModel createdByTeam = mockTeam.mockTeam(tempCaseModel.getCreatedByTeamNew());
        EmployeeUserModel createByUser = mockEmployeeUser.mockEmployeeUser(tempCaseModel.getCreatedByEmployeeIdC());

        tempCaseModel.setStatus("New");
        tempCaseModel.setCreatedByEmployeeIdC("90021");
        stgSlaPerOwnerModel.clear();

        MasterGroupModel masterGroupModel = new MasterGroupModel();
        masterGroupModel.setId(UUID.randomUUID())
                .setCode(Constant.ORIGINAL_PROBLEM_CHANNEL_GROUP_CODE)
                .setStatusCode(0);

        MasterDataModel masterDataModel = new MasterDataModel();
        masterDataModel.setNameTh("ลูกค้าสัมพันธ์")
                .setId(UUID.randomUUID())
                .setStatusCode(0)
                .setMasterGroup(masterGroupModel);

        masterGroupModel.setMasterData(Collections.singletonList(masterDataModel));

         when(serviceTypeMatrixRepository.findByServiceTypeMatrixCode(any()))
                .thenReturn(serviceTypeMatrixModel);

        when(teamReadonlyRepository.findByNameTh(anyString()))
                .thenAnswer(inv -> {
                    String teamName = inv.getArgument(0);
                    if (tempCaseModel.getCreatedByTeamNew().equals(teamName)) {
                        return Optional.of(createdByTeam);
                    } else {
                        return Optional.of(mockTeam.mockTeam(teamName));
                    }
                });
        when(employeeUserRepository.findByEmployeeId(anyString()))
                .thenAnswer(inv -> {
                    String employeeId = inv.getArgument(0);
                    if (tempCaseModel.getCreatedByEmployeeIdC().equals(employeeId)) {
                        return Optional.of(createByUser);
                    } else {
                        return Optional.of(mockEmployeeUser.mockEmployeeUser((String) inv.getArgument(0)));
                    }
                });
        when(employeeUserRepository.findByUserId(Objects.requireNonNull(createByUser.getUserId())))
                .thenReturn(Optional.of(createByUser));
        when(employeeUserRepository.findByEmployeeIdAndStatusCode(anyString(), anyInt()))
                .thenAnswer(inv -> Optional.of(mockEmployeeUser.mockEmployeeUser((String) inv.getArgument(0))));
        when(employeeUserRepository.findByUserId(any(UUID.class)))
                .thenAnswer(invocation -> {
                    UUID uuid = invocation.getArgument(0);
                    return Optional.of(mockEmployeeUser.mockEmployeeUser(uuid.toString()));
                });

        when(masterDataRepository.findByCodeAndStatusCodeAndMasterGroup_CodeAndMasterGroup_StatusCode(anyString(), Mockito.anyInt(), anyString(), Mockito.anyInt()))
                .thenReturn(Optional.of(masterDataModel));

        when(masterGroupRepository.findByCode(anyString()))
                .thenReturn(Optional.of(masterGroupModel));

        when(stgSlaPerOwnerRepository.findAllByCaseCOrderByStartDateTimeCAsc(tempCaseModel.getSfId()))
                .thenReturn(stgSlaPerOwnerModel);

        EmployeeUserModel user55515 = mockEmployeeUser.mockEmployeeUser("55515");
        when(employeeUserRepository.findByEmployeeIdAndStatusCode("55515", 0))
                .thenReturn(Optional.of(user55515));

        EmployeeUserModel user99902 = mockEmployeeUser.mockEmployeeUser("99902");
        when(employeeUserRepository.findByEmployeeIdAndStatusCode("99902", 0))
                .thenReturn(Optional.of(user99902));

        StgToCaseWriterDTO transaction = createCaseService.createCase(tempCaseModel);
        CaseTransactionModel caseModel = transaction.getCaseTransaction();

        // Validate data mock not null
        assertNotNull(createdByTeam);
        assertNotNull(createByUser);
        // Case
        assertDefaultCase(caseModel);
        assertEquals(tempCaseModel.getCaseNumber(), caseModel.getCaseNumber());
        assertEquals(createByUser.getUserId(), caseModel.getCreatedById());
        assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), caseModel.getCreatedOn());
        assertEquals(Constant.CASE_STATUS_IN_PROGRESS, caseModel.getCaseStatusCode());
        assertEquals(Constant.TEP, caseModel.getIntegrationSystem());

        assertCaseAlreadyInProgressWithResolveInfo(caseModel);
        assertCaseAlreadyInProgressWithClosedInfo(caseModel);
        assertNotNull(serviceTypeMatrixModel);
        assertServiceTypeMatrix(serviceTypeMatrixModel, caseModel);

        List<ServiceTypeMatrixSla> slas = serviceTypeMatrixModel.getServiceTypeMatrixSlas();
        List<CaseSlaHopModel> caseSlas = caseModel.getSlaHop();

        assertNotNull(slas);
        assertNotNull(caseSlas);
        assertEquals(2, caseSlas.size());

        // First case sla hop
        CaseSlaHopModel firstHop = caseSlas.getFirst();
        assertNotNull(firstHop);
        assertNull(firstHop.getCaseSlaHopId());
        assertNotNull(firstHop.getTeamId());
        assertNotNull(firstHop.getTeamName());
        assertNotNull(firstHop.getCloseByBu());
        assertNotNull(firstHop.getHopNumber());
        assertNotNull(firstHop.getStartDatetime());
        assertNotNull(firstHop.getEndDatetime());
        assertNotNull(firstHop.getOwnerId());
        assertNotNull(firstHop.getOwnerName());
        assertNotNull(firstHop.getCreatedOn());
        assertNotNull(firstHop.getCreatedById());
        assertNotNull(firstHop.getModifiedOn());
        assertNotNull(firstHop.getModifiedById());
        assertNull(firstHop.getSlaTarget());
        assertNull(firstHop.getSlaTargetDate());
        assertNotNull(firstHop.getTotalDuration());
        assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), firstHop.getCreatedOn());
//        assertEquals(DateTimeUtils.parseToZoneDateTime(tempCaseModel.getCreatedDate()), firstHop.getModifiedOn());
        assertEquals(slas.getFirst().getHopNumber(), firstHop.getHopNumber());
        assertEquals(slas.getFirst().getCloseByBu(), firstHop.getCloseByBu());
        assertEquals(slas.getFirst().getSlaTarget(), firstHop.getSlaTarget());
        assertEquals(createdByTeam.getNameTh(), firstHop.getTeamName());
        assertEquals(createdByTeam.getTeamId(), firstHop.getTeamId());
        assertEquals(0f, firstHop.getTotalDuration(), 0.0000001);
        assertEquals(caseModel.getCaseId(), firstHop.getCases().getCaseId());

        // Second Case Sla hop
        CaseSlaHopModel secondHop = caseSlas.get(1);
        assertNull(secondHop.getCaseSlaHopId());
        assertNotNull(secondHop.getCloseByBu());
        assertNotNull(secondHop.getHopNumber());
        assertNotNull(secondHop.getStartDatetime());
        assertNotNull(secondHop.getModifiedById());
        assertNotNull(secondHop.getModifiedOn());
        assertNotNull(secondHop.getCreatedOn());
        assertNotNull(secondHop.getCreatedById());
        assertNotNull(secondHop.getTeamId());
        assertNotNull(secondHop.getTeamName());
        assertNull(secondHop.getOwnerId());
        assertNull(secondHop.getOwnerName());
        assertNull(secondHop.getSlaTarget());
        assertNull(secondHop.getSlaTargetDate());
        assertNull(secondHop.getTotalDuration());
        assertNull(secondHop.getEndDatetime());
//        assertEquals(firstHop.getEndDatetime(), secondHop.getStartDatetime());
        assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), secondHop.getCreatedOn());
//        assertEquals(firstHop.getEndDatetime(), secondHop.getModifiedOn());
        assertEquals(slas.get(1).getHopNumber(), secondHop.getHopNumber());
        assertEquals(slas.get(1).getCloseByBu(), secondHop.getCloseByBu());
        assertEquals(caseModel.getCaseId(), secondHop.getCases().getCaseId());

        // Case sla activities
        List<CaseSlaActivity> caseSlaActivities = transaction.getCaseTransaction().getCaseSlaActivities();
        assertNotNull(caseSlaActivities);
        assertEquals(3, caseSlaActivities.size());

        // First sla activity
        CaseSlaActivity firstActivity = caseSlaActivities.getFirst();
        assertDefaultCaseSlaActivity(firstActivity);
        assertNotNull(firstActivity.getOwnerId());
        assertNotNull(firstActivity.getOwnerName());
        assertNotNull(firstActivity.getEndDate());
        assertNotNull(firstActivity.getCreatedByName());
        assertNotNull(firstActivity.getModifiedByName());
        assertNotNull(firstActivity.getActualDuration());
        assertEquals(firstHop.getCreatedById(), firstActivity.getCreatedById());
//        assertEquals(firstHop.getModifiedById(), firstActivity.getModifiedById());
        assertEquals(firstHop.getOwnerId(), firstActivity.getOwnerId());
        assertEquals(firstHop.getOwnerName(), firstActivity.getOwnerName());
        assertEquals(firstHop.getTeamId(), firstActivity.getTeamId());
        assertEquals(firstHop.getTeamName(), firstActivity.getTeamNameTh());
        assertEquals(firstHop.getHopNumber(), firstActivity.getHopNumberRef());
        assertEquals(caseModel.getCaseNumber(), firstActivity.getCases().getCaseNumber());
        assertEquals(0.0, firstActivity.getActualDuration(), 0.0000001);
        assertEquals(CaseSlaActivityAction.CREATE, firstActivity.getAction());

        // Second sla activity
        CaseSlaActivity secondActivity = caseSlaActivities.get(1);
        assertDefaultCaseSlaActivity(secondActivity);
        assertNotNull(secondActivity.getOwnerId());
        assertNotNull(secondActivity.getOwnerName());
        assertNotNull(secondActivity.getEndDate());
        assertNull(secondActivity.getActualDuration());
        assertEquals(firstHop.getCreatedById(), secondActivity.getCreatedById());
        assertEquals(firstHop.getModifiedById(), secondActivity.getModifiedById());
        assertEquals(firstHop.getOwnerId(), secondActivity.getOwnerId());
        assertEquals(firstHop.getOwnerName(), secondActivity.getOwnerName());
        assertEquals(firstHop.getTeamId(), secondActivity.getTeamId());
        assertEquals(firstHop.getTeamName(), secondActivity.getTeamNameTh());
        assertEquals(firstHop.getHopNumber(), secondActivity.getHopNumberRef());
        assertEquals(caseModel.getCaseNumber(), secondActivity.getCases().getCaseNumber());
        assertEquals(CaseSlaActivityAction.NEXT_HOP, secondActivity.getAction());

        // Third sla activity
        CaseSlaActivity thirdActivity = caseSlaActivities.get(2);
        assertDefaultCaseSlaActivity(thirdActivity);
        assertNull(thirdActivity.getEndDate());
        assertNull(thirdActivity.getActualDuration());
        assertNull(thirdActivity.getOwnerId());
        assertNull(thirdActivity.getOwnerName());
//        assertEquals(firstHop.getOwnerId(), thirdActivity.getCreatedById());

//        assertEquals(secondHop.getOwnerId(), thirdActivity.getModifiedById());
        assertEquals(secondHop.getTeamId(), thirdActivity.getTeamId());
        assertEquals(secondHop.getTeamName(), thirdActivity.getTeamNameTh());
        assertEquals(secondHop.getHopNumber(), thirdActivity.getHopNumberRef());
        assertEquals(caseModel.getCaseNumber(), thirdActivity.getCases().getCaseNumber());

        assertEquals(CaseSlaActivityAction.ASSIGN_TEAM, thirdActivity.getAction());

    }

    @Test
    @Disabled
    void caseInProgressWithOwnerEqualEmp() {
        ServiceTypeMatrixModel serviceTypeMatrixModel = mockSTM.mockSTMDynamicWithOutFCR();
//        TeamReadonlyModel createdByTeam = mockTeam.mockTeam(tempCaseModel.getCreatedByTeamNew());
        EmployeeUserModel createByUser = mockEmployeeUser.mockEmployeeUser(tempCaseModel.getCreatedByEmployeeIdC());
        EmployeeUserModel creator = mockEmployeeUser.mockEmployeeUser("55501", "User Creator");

        tempCaseModel.setCreatedByEmployeeIdC("90021");
        tempCaseModel.setOwnerEmployeeIdC("90021");
        tempCaseModel.setOwnerNameC("Noppadon Sahakulrat");
        tempCaseModel.setStatus(Constant.CASE_STATUS_IN_PROGRESS);

        stgSlaPerOwnerModel.clear();

        when(employeeUserRepository.findByEmployeeId(any()))
                .thenReturn(Optional.of(creator));
        when(employeeUserRepository.findByUserId(creator.getUserId()))
                .thenReturn(Optional.of(creator));
        when(employeeUserRepository.findByEmployeeIdAndStatusCode(any(), eq(0)))
                .thenReturn(Optional.of(creator));

        EmployeeUserModel user55515 = mockEmployeeUser.mockEmployeeUser("55515", "User B");
        when(employeeUserRepository.findByEmployeeId("55515"))
                .thenReturn(Optional.of(user55515));
        when(employeeUserRepository.findByUserId(user55515.getUserId()))
                .thenReturn(Optional.of(user55515));
        when(employeeUserRepository.findByEmployeeIdAndStatusCode("55515", 0))
                .thenReturn(Optional.of(user55515));

        EmployeeUserModel user99902 = mockEmployeeUser.mockEmployeeUser("99902", "User C");
        when(employeeUserRepository.findByEmployeeId("99902"))
                .thenReturn(Optional.of(user99902));
        when(employeeUserRepository.findByUserId(user99902.getUserId()))
                .thenReturn(Optional.of(user99902));
        when(employeeUserRepository.findByEmployeeIdAndStatusCode("99902", 0))
                .thenReturn(Optional.of(user99902));

        // Mock STM repository
        when(serviceTypeMatrixRepository.findByServiceTypeMatrixCode(Mockito.anyString()))
                .thenReturn(serviceTypeMatrixModel);

        // Mock team repository
        when(teamReadonlyRepository.findByNameTh(anyString()))
                .thenAnswer(inv -> Optional.of(mockTeam.mockTeam(inv.getArgument(0))));

        // Mock master data
        MasterGroupModel masterGroupModel = new MasterGroupModel();
        masterGroupModel.setId(UUID.randomUUID())
                .setCode(Constant.ORIGINAL_PROBLEM_CHANNEL_GROUP_CODE)
                .setStatusCode(0);

        MasterDataModel masterDataModel = new MasterDataModel();
        masterDataModel.setNameTh("ลูกค้าสัมพันธ์")
                .setId(UUID.randomUUID())
                .setStatusCode(0)
                .setMasterGroup(masterGroupModel);

        when(masterDataRepository.findByCodeAndStatusCodeAndMasterGroup_CodeAndMasterGroup_StatusCode(
                anyString(), anyInt(), anyString(), anyInt()))
                .thenReturn(Optional.of(masterDataModel));

        when(masterGroupRepository.findByCode(anyString()))
                .thenReturn(Optional.of(masterGroupModel));

        // Mock temp SLA repository
        when(stgSlaPerOwnerRepository.findAllByCaseCOrderByStartDateTimeCAsc(tempCaseModel.getSfId()))
                .thenReturn(stgSlaPerOwnerModel);

        StgToCaseWriterDTO transaction = createCaseService.createCase(tempCaseModel);

        CaseTransactionModel caseModel = transaction.getCaseTransaction();

        assertNotNull(createByUser);
        // Case
        assertNotNull(caseModel.getCaseId());
        assertNotNull(caseModel.getCaseNumber());
        assertNotNull(caseModel.getCreatedByTeamId());
        assertNotNull(caseModel.getCreatedById());
        assertNotNull(caseModel.getModifiedById());
        assertNotNull(caseModel.getCreatedOn());
        assertNotNull(caseModel.getModifiedOn());
        assertNull(caseModel.getOwnerId());
        assertNotNull(caseModel.getOwnerName());
        assertNull(caseModel.getTeamId());
        assertNotNull(caseModel.getTeamName());
        assertEquals(tempCaseModel.getCaseNumber(), caseModel.getCaseNumber());
        assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), caseModel.getCreatedOn());
        assertEquals(Constant.CASE_STATUS_IN_PROGRESS, caseModel.getCaseStatusCode());
        assertEquals(Constant.TEP, caseModel.getIntegrationSystem());

        assertCaseAlreadyInProgressWithClosedInfo(caseModel);
        assertNotNull(serviceTypeMatrixModel);
        assertServiceTypeMatrix(serviceTypeMatrixModel, caseModel);


        assertNotNull(serviceTypeMatrixModel);
        assertServiceTypeMatrix(serviceTypeMatrixModel, caseModel);

        // Case Sla hop
        List<CaseSlaHopModel> slas = caseModel.getSlaHop();
        assertEquals(2, slas.size());

        // First case sla hop
        CaseSlaHopModel firstHop = slas.getFirst();
        assertNull(firstHop.getCaseSlaHopId());
        assertNotNull(firstHop.getTeamId());
        assertNotNull(firstHop.getTeamName());
        assertNotNull(firstHop.getCloseByBu());
        assertNotNull(firstHop.getHopNumber());
        assertNotNull(firstHop.getStartDatetime());
        assertNotNull(firstHop.getEndDatetime());
        assertNotNull(firstHop.getOwnerId());
        assertNotNull(firstHop.getOwnerName());
        assertNotNull(firstHop.getCreatedOn());
        assertNotNull(firstHop.getCreatedById());
        assertNotNull(firstHop.getModifiedOn());
        assertNotNull(firstHop.getModifiedById());
        assertNull(firstHop.getSlaTarget());
        assertNull(firstHop.getSlaTargetDate());
        assertNotNull(firstHop.getTotalDuration());
        assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), firstHop.getStartDatetime());
        assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), firstHop.getEndDatetime());
        assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), firstHop.getCreatedOn());
        assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), firstHop.getModifiedOn());
        assertEquals(slas.getFirst().getHopNumber(), firstHop.getHopNumber());
        assertEquals(slas.getFirst().getCloseByBu(), firstHop.getCloseByBu());
        assertEquals(slas.getFirst().getSlaTarget(), firstHop.getSlaTarget());
        assertEquals(caseModel.getCaseId(), firstHop.getCases().getCaseId());

        // Second Case Sla hop
        CaseSlaHopModel secondHop = slas.get(1);
        assertNull(secondHop.getCaseSlaHopId());
        assertNotNull(secondHop.getTeamId());
        assertNotNull(secondHop.getTeamName());
        assertNotNull(secondHop.getCloseByBu());
        assertNotNull(secondHop.getHopNumber());
        assertNotNull(secondHop.getStartDatetime());
        assertNotNull(secondHop.getCreatedById());
        assertNotNull(secondHop.getModifiedOn());
        assertNotNull(secondHop.getModifiedById());
        assertNotNull(secondHop.getCreatedOn());
        assertNull(secondHop.getOwnerName());
        assertNull(secondHop.getOwnerId());
        assertNull(secondHop.getEndDatetime());
        assertNull(secondHop.getSlaTarget());
        assertNull(secondHop.getSlaTargetDate());
        assertNull(secondHop.getTotalDuration());
        assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), secondHop.getCreatedOn());
        assertEquals(slas.get(1).getHopNumber(), secondHop.getHopNumber());
        assertEquals(slas.get(1).getCloseByBu(), secondHop.getCloseByBu());
        assertEquals(slas.get(1).getSlaTarget(), secondHop.getSlaTarget());
        assertEquals(caseModel.getCaseId(), secondHop.getCases().getCaseId());

        // Case sla activities
        List<CaseSlaActivity> caseSlaActivities = transaction.getCaseTransaction().getCaseSlaActivities();
        assertNotNull(caseSlaActivities);
        assertEquals(3, caseSlaActivities.size());

        // First sla activity
        CaseSlaActivity firstActivity = caseSlaActivities.getFirst();
        assertDefaultCaseSlaActivity(firstActivity);
        assertNotNull(firstActivity.getOwnerId());
        assertNotNull(firstActivity.getOwnerName());
        assertNotNull(firstActivity.getEndDate());
        assertNotNull(firstActivity.getCreatedByName());
        assertNotNull(firstActivity.getModifiedByName());
        assertNotNull(firstActivity.getActualDuration());
        assertEquals(firstHop.getCreatedById(), firstActivity.getCreatedById());
        assertEquals(firstHop.getModifiedById(), firstActivity.getModifiedById());
        assertEquals(firstHop.getOwnerId(), firstActivity.getOwnerId());
        assertEquals(firstHop.getOwnerName(), firstActivity.getOwnerName());
        assertEquals(firstHop.getTeamId(), firstActivity.getTeamId());
        assertEquals(firstHop.getTeamName(), firstActivity.getTeamNameTh());
        assertEquals(firstHop.getHopNumber(), firstActivity.getHopNumberRef());
        assertEquals(caseModel.getCaseNumber(), firstActivity.getCases().getCaseNumber());
        assertEquals(0.0, firstActivity.getActualDuration(), 0.0000001);
        assertEquals(CaseSlaActivityAction.CREATE, firstActivity.getAction());

        // Second sla activity
        CaseSlaActivity secondActivity = caseSlaActivities.get(1);
        assertDefaultCaseSlaActivity(secondActivity);
        assertNotNull(secondActivity.getOwnerId());
        assertNotNull(secondActivity.getOwnerName());
        assertNotNull(secondActivity.getEndDate());
        assertNull(secondActivity.getActualDuration());
        assertEquals(firstHop.getCreatedById(), secondActivity.getCreatedById());
        assertEquals(firstHop.getModifiedById(), secondActivity.getModifiedById());
        assertEquals(firstHop.getOwnerId(), secondActivity.getOwnerId());
        assertEquals(firstHop.getOwnerName(), secondActivity.getOwnerName());
        assertEquals(firstHop.getTeamId(), secondActivity.getTeamId());
        assertEquals(firstHop.getTeamName(), secondActivity.getTeamNameTh());
        assertEquals(firstHop.getHopNumber(), secondActivity.getHopNumberRef());
        assertEquals(caseModel.getCaseNumber(), secondActivity.getCases().getCaseNumber());
        assertEquals(CaseSlaActivityAction.NEXT_HOP, secondActivity.getAction());

        // Third sla activity
        CaseSlaActivity thirdActivity = caseSlaActivities.getLast();
        assertNull(thirdActivity.getCaseSlaActivityId());
        assertNotNull(thirdActivity.getCreatedOn());
        assertNotNull(thirdActivity.getModifiedOn());
        assertNotNull(thirdActivity.getCreatedById());
        assertNotNull(thirdActivity.getModifiedById());
        assertNotNull(thirdActivity.getStartDate());
        assertNull(thirdActivity.getOwnerName());
        assertNull(thirdActivity.getOwnerId());
        assertNull(thirdActivity.getActualDuration());
        assertNull(thirdActivity.getEndDate());
        assertEquals(caseModel.getCaseNumber(), thirdActivity.getCases().getCaseNumber());
        assertEquals(secondHop.getHopNumber(), thirdActivity.getHopNumberRef());
        assertEquals(CaseSlaActivityAction.ASSIGN_TEAM, thirdActivity.getAction());
    }

    @Test
    @Disabled
    void caseInProgressNotLatestHistoryEmpInactiveOrNotMemberInTeam() {
        ServiceTypeMatrixModel serviceTypeMatrixModel = mockSTM.mockSTMDynamicWithOutFCR();
        EmployeeUserModel createByUser = mockEmployeeUser.mockEmployeeUser(tempCaseModel.getCreatedByEmployeeIdC());
        EmployeeUserModel creator = mockEmployeeUser.mockEmployeeUser("90021", "Noppadon Sahakulrat");
        EmployeeUserModel owner = mockEmployeeUser.mockEmployeeUser("83798");

        tempCaseModel.setStatus(Constant.CASE_STATUS_IN_PROGRESS);
        tempCaseModel.setCreatedByEmployeeIdC("90021");
        tempCaseModel.setOwnerEmployeeIdC("83798");
        tempCaseModel.setOwnerNameC("Yanyong Charoensawan");
        tempCaseModel.setOwnerTeamNew("");

        stgSlaPerOwnerModel.clear();

        // ควรเพิ่ม SF Activities ให้ครบ workflow:
        stgSlaPerOwnerModel.add(mockTempSla.mockTempStgSlaPerOwnerCompleted(
                tempCaseModel.getSfId(), "Create", "Creator", "90021",
                "2025-06-14 09:00:00", "2025-06-14 10:00:00", "New"
        ));

        stgSlaPerOwnerModel.add(mockTempSla.mockTempStgSlaPerOwnerCompleted(
                tempCaseModel.getSfId(), "Inbound Voice Team 1", "", "",
                "2025-06-14 10:00:00", "2025-06-14 11:00:00", "In progress"
        ));

        stgSlaPerOwnerModel.add(mockTempSla.mockTempStgSlaPerOwnerCompleted(
                tempCaseModel.getSfId(), "Investment Line", "Final Owner", "99902",
                "2025-06-14 11:00:00", null, "In progress"
        ));

        // ✅ Mock NA User (เพราะ PrepareTeamUserData ต้องการ)
        EmployeeUserModel naUser = mockEmployeeUser.mockEmployeeUser(Constant.NA_EMPLOYEE_ID);
        when(employeeUserRepository.findByEmployeeId(Constant.NA_EMPLOYEE_ID))
                .thenReturn(Optional.of(naUser));

        // ✅ Mock System Team & User
        TeamReadonlyModel systemTeam = mockTeam.mockTeam(Constant.SYSTEM_TEAM);
        when(teamReadonlyRepository.findByNameTh(Constant.SYSTEM_TEAM))
                .thenReturn(Optional.of(systemTeam));

        // ✅ Mock case creator (90021) - ต้องมี!
        when(employeeUserRepository.findByEmployeeId("90021"))
                .thenReturn(Optional.of(creator));
        when(employeeUserRepository.findByUserId(creator.getUserId()))
                .thenReturn(Optional.of(creator));
        when(employeeUserRepository.findByEmployeeIdAndStatusCode("90021", 0))
                .thenReturn(Optional.of(creator));

        // ✅ Mock user 99902
        EmployeeUserModel user99902 = mockEmployeeUser.mockEmployeeUser("99902", "User C");
        when(employeeUserRepository.findByEmployeeId("99902"))
                .thenReturn(Optional.of(user99902));
        when(employeeUserRepository.findByUserId(user99902.getUserId()))
                .thenReturn(Optional.of(user99902));
        when(employeeUserRepository.findByEmployeeIdAndStatusCode("99902", 0))
                .thenReturn(Optional.of(user99902));

        // Mock empty employees (inactive scenario)
        when(employeeUserRepository.findByEmployeeId(""))
                .thenReturn(Optional.empty());
        when(employeeUserRepository.findByEmployeeIdAndStatusCode("", 0))
                .thenReturn(Optional.empty());

        when(serviceTypeMatrixRepository.findByServiceTypeMatrixCode(Mockito.anyString()))
                .thenReturn(serviceTypeMatrixModel);

        when(teamReadonlyRepository.findByNameTh(anyString()))
                .thenAnswer(inv -> Optional.of(mockTeam.mockTeam(inv.getArgument(0))));

        when(holidayRepository.findByStatusCode(0))
                .thenReturn(Optional.of(List.of()));

        MasterGroupModel masterGroupModel = new MasterGroupModel();
        masterGroupModel.setId(UUID.randomUUID())
                .setCode(Constant.ORIGINAL_PROBLEM_CHANNEL_GROUP_CODE)
                .setStatusCode(0);

        MasterDataModel masterDataModel = new MasterDataModel();
        masterDataModel.setNameTh("ลูกค้าสัมพันธ์")
                .setId(UUID.randomUUID())
                .setStatusCode(0)
                .setMasterGroup(masterGroupModel);

        when(masterDataRepository.findByCodeAndStatusCodeAndMasterGroup_CodeAndMasterGroup_StatusCode(
                anyString(), anyInt(), anyString(), anyInt()))
                .thenReturn(Optional.of(masterDataModel));

        when(masterGroupRepository.findByCode(anyString()))
                .thenReturn(Optional.of(masterGroupModel));

        // Mock temp SLA repository
        when(stgSlaPerOwnerRepository.findAllByCaseCOrderByStartDateTimeCAsc(tempCaseModel.getSfId()))
                .thenReturn(stgSlaPerOwnerModel);

        StgToCaseWriterDTO transaction = createCaseService.createCase(tempCaseModel);

        CaseTransactionModel caseModel = transaction.getCaseTransaction();

        assertNotNull(createByUser);
        // Case
        assertNotNull(caseModel.getCaseId());
        assertNotNull(caseModel.getCaseNumber());
        assertNotNull(caseModel.getCreatedByTeamId());
        assertNotNull(caseModel.getCreatedById());
        assertNotNull(caseModel.getModifiedById());
        assertNotNull(caseModel.getCreatedOn());
        assertNotNull(caseModel.getModifiedOn());
        assertNull(caseModel.getOwnerId());
        assertNotNull(caseModel.getOwnerName());
        assertNull(caseModel.getTeamId());
        assertNotNull(caseModel.getTeamName());
        assertEquals(tempCaseModel.getCaseNumber(), caseModel.getCaseNumber());
        assertEquals(parseToZoneDateTime(tempCaseModel.getCreatedDate()), caseModel.getCreatedOn());
        assertEquals(Constant.CASE_STATUS_IN_PROGRESS, caseModel.getCaseStatusCode());
        assertEquals(Constant.TEP, caseModel.getIntegrationSystem());

        assertCaseAlreadyInProgressWithClosedInfo(caseModel);
        assertNotNull(serviceTypeMatrixModel);
        assertServiceTypeMatrix(serviceTypeMatrixModel, caseModel);

        assertNotNull(serviceTypeMatrixModel);
        assertServiceTypeMatrix(serviceTypeMatrixModel, caseModel);
    }

    @Test
    void validateFieldsForFinCer_ShouldSetReadyToPrint_WhenAllFieldsPresent() {
        // Arrange
        CaseTransactionModel caseTx = new CaseTransactionModel();
        CreateCaseDTO body = new CreateCaseDTO();
        body.setObjectId("obj-1");
        body.setDocumentId("doc-1");
        body.setRepositoryId("repo-1");

        // Act
        createCaseService.validateFieldsForFinCer(caseTx, body);

        // Assert
        assertTrue(caseTx.getReadyToPrint());
    }

    @Test
    void validateFieldsForFinCer_ShouldThrow_WhenObjectIdIsNull() {
        // Arrange
        CaseTransactionModel caseTx = new CaseTransactionModel();
        CreateCaseDTO body = new CreateCaseDTO();
        body.setObjectId(null);
        body.setDocumentId("doc-1");
        body.setRepositoryId("repo-1");

        // Act & Assert
        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> createCaseService.validateFieldsForFinCer(caseTx, body)
        );
        assertEquals("ObjectId is required", ex.getMessage());
    }

    @Test
    void validateFieldsForFinCer_ShouldThrow_WhenDocumentIdIsEmpty() {
        // Arrange
        CaseTransactionModel caseTx = new CaseTransactionModel();
        CreateCaseDTO body = new CreateCaseDTO();
        body.setObjectId("obj-1");
        body.setDocumentId("");
        body.setRepositoryId("repo-1");

        // Act & Assert
        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> createCaseService.validateFieldsForFinCer(caseTx, body)
        );
        assertEquals("DocumentId is required", ex.getMessage());
    }


    @Test
    void validateFieldsForFinCer_ShouldThrow_WhenRepositoryIdIsBlank() {
        // Arrange
        CaseTransactionModel caseTx = new CaseTransactionModel();
        CreateCaseDTO body = new CreateCaseDTO();
        body.setObjectId("obj-1");
        body.setDocumentId("doc-1");
        body.setRepositoryId("   ");

        // Act & Assert
        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> createCaseService.validateFieldsForFinCer(caseTx, body)
        );
        assertEquals("RepositoryId is required", ex.getMessage());
    }
}
