package com.ttb.crm.service.migrationdata.service.preparecaesdataservice;

import com.ttb.crm.lib.crmssp_common_utils_lib.exception.NotFoundException;
import com.ttb.crm.service.migrationdata.bean.CreateCaseDTO;
import com.ttb.crm.service.migrationdata.enums.CaseStatus;
import com.ttb.crm.service.migrationdata.helper.Constant;
import com.ttb.crm.service.migrationdata.model.secondary.StgSlaPerOwnerModel;
import com.ttb.crm.service.migrationdata.repository.caseManagement.CaseDocumentReferenceRepository;
import com.ttb.crm.service.migrationdata.repository.caseManagement.CaseSlaActivityRepository;
import com.ttb.crm.service.migrationdata.repository.caseManagement.CaseSlaHopRepository;
import com.ttb.crm.service.migrationdata.repository.caseManagement.CaseTransactionRepository;
import com.ttb.crm.service.migrationdata.service.TeamServiceTest;
import com.ttb.crm.service.migrationdata.service.UserService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import static com.ttb.crm.service.migrationdata.helper.DateTimeUtils.parseToZoneDateTime;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EnhancedCaseValidationServiceTest {

    @InjectMocks
    private EnhancedCaseValidationService service;

    @Mock
    private CaseTransactionRepository caseRepository;
    @Mock
    private CaseSlaHopRepository caseSlaHopRepository;
    @Mock
    private CaseSlaActivityRepository caseSlaActivityRepository;
    @Mock
    private UserService userService;
    @Mock
    private TeamServiceTest teamService;
    @Mock
    private EntityManager entityManager;
    @Mock
    private CheckCaseType checkCaseType;
    @Mock
    private CaseDocumentReferenceRepository caseDocumentReferenceRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void validateIsOnGoingStatus_shouldThrowIllegalArgumentException() {
        CreateCaseDTO dto = new CreateCaseDTO().setCaseStatusCode(CaseStatus.CANCEL.toString());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> service.validateIsOnGoingStatus(dto));

        assertTrue(exception.getMessage().contains("Case status is CANCEL. Please check if case status is NEW or IN_PROGRESS"));
    }

    @Test
    public void validateIsOnGoingStatus_shouldNotThrowIllegalArgumentException() {
        CreateCaseDTO dto = new CreateCaseDTO().setCaseStatusCode(CaseStatus.IN_PROGRESS.toString());

        assertDoesNotThrow(() -> service.validateIsOnGoingStatus(dto));
    }

    @Test
    public void validateRequired_shouldThrowIllegalArgumentException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> service.validateRequired(null, "Object is null"));

        assertTrue(exception.getMessage().contains("Object is null"));
    }

    @Test
    public void validateRequired_shouldThrowIllegalArgumentExceptionInCaseOfEmptyString() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> service.validateRequired("", "Object is null or empty"));

        assertTrue(exception.getMessage().contains("Object is null or empty"));
    }

    @Test
    public void validateRequired_shouldNotThrowIllegalArgumentException() {
        assertDoesNotThrow(() -> service.validateRequired("Test", "Object is null"));
    }

    @Test
    public void validateMandatoryData_shouldThrowIllegalArgumentException() {
        CreateCaseDTO dto = new CreateCaseDTO().setCaseNumber("");
        Exception exception = assertThrows(IllegalArgumentException.class, () -> service.validateMandatoryData(dto));

        assertTrue(exception.getMessage().contains("Case number is required"));
    }

    @Test
    public void validateMandatoryData_shouldThrowIllegalArgumentExceptionServiceTypeMatrixNewIsMissing() {
        CreateCaseDTO dto = new CreateCaseDTO().setCaseNumber("2025000001").setCaseStatusCode(CaseStatus.IN_PROGRESS.toString());
        when(checkCaseType.isCompletedStatus(dto)).thenReturn(false);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> service.validateMandatoryData(dto));

        assertTrue(exception.getMessage().contains("Service type matrix code new is required"));
    }

    @Test
    public void validateMandatoryData_shouldThrowIllegalArgumentExceptionServiceTypeMatrixIsMissing() {
        CreateCaseDTO dto = new CreateCaseDTO().setCaseNumber("2025000001").setCaseStatusCode(CaseStatus.IN_PROGRESS.toString())
                .setServiceTypeMatrixCode("STM002516");
        when(checkCaseType.isCompletedStatus(dto)).thenReturn(false);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> service.validateMandatoryData(dto));

        assertTrue(exception.getMessage().contains("Service type matrix code is required"));
    }

    @Test
    public void validateMandatoryData_shouldThrowIllegalArgumentExceptionCaseStatusCodeIsMissing() {
        CreateCaseDTO dto = new CreateCaseDTO().setCaseNumber("2025000001")
                .setServiceTypeMatrixCode("STM002516")
                .setServiceTypeMatrixCodeOld("S000");
        when(checkCaseType.isCompletedStatus(dto)).thenReturn(true);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> service.validateMandatoryData(dto));

        assertTrue(exception.getMessage().contains("Case status code is required"));
    }

    @Test
    public void validateMandatoryData_shouldThrowIllegalArgumentExceptionNoOriginalProblemChannelAndDataSource() {
        CreateCaseDTO dto = new CreateCaseDTO().setCaseNumber("2025000001")
                .setServiceTypeMatrixCode("STM002516")
                .setServiceTypeMatrixCodeOld("S000")
                .setCaseStatusCode(CaseStatus.IN_PROGRESS.toString());
        when(checkCaseType.isCompletedStatus(dto)).thenReturn(true);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> service.validateMandatoryData(dto));

        assertTrue(exception.getMessage().contains("Original problem channel code and DataSource are required"));
    }

    @Test
    public void validateMandatoryData_shouldThrowIllegalArgumentExceptionNoDataSource() {
        CreateCaseDTO dto = new CreateCaseDTO().setCaseNumber("2025000001")
                .setServiceTypeMatrixCode("STM002516")
                .setServiceTypeMatrixCodeOld("S000")
                .setCaseStatusCode(CaseStatus.IN_PROGRESS.toString())
                .setOriginalProblemChannelCode("Online");
        when(checkCaseType.isCompletedStatus(dto)).thenReturn(true);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> service.validateMandatoryData(dto));

        assertTrue(exception.getMessage().contains("Original problem channel value is required"));
    }

    @Test
    public void validateMandatoryData_shouldThrowIllegalArgumentExceptionNoDataSourceValue() {
        CreateCaseDTO dto = new CreateCaseDTO().setCaseNumber("2025000001")
                .setServiceTypeMatrixCode("STM002516")
                .setServiceTypeMatrixCodeOld("S000")
                .setCaseStatusCode(CaseStatus.IN_PROGRESS.toString())
                .setDataSourceCode("Test")
                .setOriginalProblemChannelValue("Test");
        when(checkCaseType.isCompletedStatus(dto)).thenReturn(true);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> service.validateMandatoryData(dto));

        assertTrue(exception.getMessage().contains("DataSource value is required"));
    }

    @Test
    public void validateMandatoryData_shouldNotThrowIllegalArgumentException() {
        CreateCaseDTO dto = new CreateCaseDTO().setCaseNumber("2025000001")
                .setServiceTypeMatrixCode("STM002516")
                .setServiceTypeMatrixCodeOld("S000")
                .setCaseStatusCode(CaseStatus.IN_PROGRESS.toString())
                .setOriginalProblemChannelCode("Online")
                .setOriginalProblemChannelValue("Test")
                .setDataSourceCode("Test")
                .setDataSourceValue("Test");
        when(checkCaseType.isCompletedStatus(dto)).thenReturn(true);

        assertDoesNotThrow(() -> service.validateMandatoryData(dto));
    }

    @Test
    public void validateCaseOnGoingData_shouldNotThrowException() {
        CreateCaseDTO dto = new CreateCaseDTO().setCaseStatusCode(CaseStatus.IN_PROGRESS.toString());

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                service.validateCaseOnGoingData(dto));

        assertTrue(exception.getMessage().contains("Case number is required"));
    }

    @Test
    public void validateCaseOnGoingData_shouldThrowExceptionForInvalidStatus() {
        CreateCaseDTO dto = new CreateCaseDTO().setCaseStatusCode(CaseStatus.CANCEL.toString());

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                service.validateCaseOnGoingData(dto));

        assertTrue(exception.getMessage().contains("Please check if case status is NEW or IN_PROGRESS"));
    }

    @Test
    public void validateByIntegrationSystem_shouldThrowExceptionNoIntegrationSystem() {
        CreateCaseDTO dto = new CreateCaseDTO();

        Exception exception = assertThrows(IllegalArgumentException.class, () -> service.validateByIntegrationSystem(dto));

        assertTrue(exception.getMessage().contains("Integration system is required"));
    }

    @Test
    public void validateByIntegrationSystem_shouldThrowExceptionInvalidIntegrationSystem() {
        CreateCaseDTO dto = new CreateCaseDTO().setIntegrationSystem("Test");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> service.validateByIntegrationSystem(dto));

        assertTrue(exception.getMessage().contains("Unsupported integration system: Test"));
    }

    @Test
    public void validateByIntegrationSystem_shouldNotThrowException() {
        CreateCaseDTO dto = new CreateCaseDTO().setIntegrationSystem(Constant.TEP);

        assertDoesNotThrow(() -> service.validateByIntegrationSystem(dto));
    }

    @Test
    public void validateCreatedCaseData_shouldThrowExceptionNoCreateByTeamName() {
        CreateCaseDTO dto = new CreateCaseDTO().setIntegrationSystem(Constant.ONE_APP)
                .setCaseStatusCode(Constant.CASE_STATUS_RESOLVED);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> service.validateCreatedCaseData(dto));

        assertTrue(exception.getMessage().contains("Create by team name is required"));
    }

//    @Test
//    public void validateCreatedCaseData_shouldThrowExceptionNoOwnerEmployeeId() {
//        CreateCaseDTO dto = new CreateCaseDTO().setIntegrationSystem(Constant.ONE_APP)
//                .setCaseStatusCode(Constant.CASE_STATUS_COMPLETED)
//                .setCreateByEmployeeID(Constant.SF_EX_API_ID);
//        when(checkCaseType.isOneAppOrTtbWeb(anyString())).thenReturn(true);
//        when(checkCaseType.isCompletedStatus(any(CreateCaseDTO.class))).thenReturn(true);
//
//        Exception exception = assertThrows(IllegalArgumentException.class, () -> service.validateCreatedCaseData(dto));
//
//        assertTrue(exception.getMessage().contains("Owner employee ID is required"));
//        verify((checkCaseType), times(1)).isOneAppOrTtbWeb(anyString());
//        verify((checkCaseType), times(1)).isCompletedStatus(any(CreateCaseDTO.class));
//    }

//    @Test
//    public void validateCreatedCaseData_shouldThrowExceptionNoOwnerAndTeam() {
//        CreateCaseDTO dto = new CreateCaseDTO().setIntegrationSystem(Constant.ONE_APP)
//                .setCreateByEmployeeID("55555")
//                .setOwnerTeamName("Test")
//                .setServiceTypeMatrixCodeOld("STMOLD");
//
//        when(checkCaseType.isOneAppOrTtbWeb(anyString())).thenReturn(false);
//        when(checkCaseType.isOneApp(anyString())).thenReturn(true);
//        when(checkCaseType.isCreateOneAppAndPayrollOrPWACase(anyString())).thenReturn(true);
//
//        Exception exception = assertThrows(IllegalArgumentException.class, () -> service.validateCreatedCaseData(dto));
//
//        System.out.println(exception.getMessage());
//        assertTrue(exception.getMessage().contains("Create by name is required"));
//        verify((checkCaseType), times(1)).isOneAppOrTtbWeb(anyString());
//        verify((checkCaseType), times(1)).isOneApp(anyString());
//        verify((checkCaseType), times(1)).isCreateOneAppAndPayrollOrPWACase(anyString());
//    }

    @Test
    public void validateCreatedCaseData_shouldNotThrowException() {
        ZonedDateTime zonedDateTime = parseToZoneDateTime("2025-07-07 17:12:28.853 +0000");
        CreateCaseDTO dto = new CreateCaseDTO().setIntegrationSystem(Constant.ONE_APP)
                .setCaseStatusCode(Constant.CASE_STATUS_RESOLVED)
                .setCreateByTeamName("Inbound Voice Team 4")
                .setCreateByEmployeeID("55555")
                .setCreateByName("Test");
        dto.setCreatedOn(zonedDateTime);
        dto.setModifiedOn(zonedDateTime);

        assertDoesNotThrow(() -> service.validateCreatedCaseData(dto));
    }

//    @Test
//    public void isCompletedOrResolvedOneAppOrTtbWebCase_shouldReturnTrue() {
//        CreateCaseDTO dto = new CreateCaseDTO().setIntegrationSystem(Constant.ONE_APP)
//                .setCreateByEmployeeID(Constant.SF_EX_API_ID);
//
//        when(checkCaseType.isOneAppOrTtbWeb(anyString())).thenReturn(true);
//        when(checkCaseType.isCompletedStatus(any(CreateCaseDTO.class))).thenReturn(true);
//
//        assertTrue(() -> service.isCompletedOrResolvedOneAppOrTtbWebCase(dto));
//        verify((checkCaseType), times(1)).isOneAppOrTtbWeb(anyString());
//        verify((checkCaseType), times(1)).isCompletedStatus(any(CreateCaseDTO.class));
//    }

//    @Test
//    public void isCompletedOrResolvedOneAppOrTtbWebCase_shouldReturnFalse() {
//        CreateCaseDTO dto = new CreateCaseDTO().setIntegrationSystem(Constant.ONE_APP)
//                .setCreateByEmployeeID(Constant.SF_EX_API_ID);
//
//        when(checkCaseType.isOneAppOrTtbWeb(anyString())).thenReturn(true);
//        when(checkCaseType.isCompletedStatus(any(CreateCaseDTO.class))).thenReturn(false);
//        when(checkCaseType.isResolvedStatus(any(CreateCaseDTO.class))).thenReturn(false);
//
//        assertFalse(() -> service.isCompletedOrResolvedOneAppOrTtbWebCase(dto));
//        verify((checkCaseType), times(1)).isOneAppOrTtbWeb(anyString());
//        verify((checkCaseType), times(1)).isCompletedStatus(any(CreateCaseDTO.class));
//        verify((checkCaseType), times(1)).isResolvedStatus(any(CreateCaseDTO.class));
//    }

//    @Test
//    public void isCompletedOrResolvedOneAppOrTtbWebCase_shouldReturnFalse2() {
//        CreateCaseDTO dto = new CreateCaseDTO().setIntegrationSystem(Constant.ONE_APP)
//                .setOwnerEmployeeId(Constant.SF_EX_API_ID);
//
//        when(checkCaseType.isCompletedStatus(any(CreateCaseDTO.class))).thenReturn(false);
//        when(checkCaseType.isResolvedStatus(any(CreateCaseDTO.class))).thenReturn(true);
//        when(checkCaseType.isOneAppOrTtbWeb(anyString())).thenReturn(true);
//        when(checkCaseType.isCreateOneAppAndPayrollOrPWACase(anyString())).thenReturn(true);
//
//        assertTrue(() -> service.isCompletedOrResolvedOneAppOrTtbWebCase(dto));
//        verify((checkCaseType), times(1)).isCompletedStatus(any(CreateCaseDTO.class));
//        verify((checkCaseType), times(1)).isResolvedStatus(any(CreateCaseDTO.class));
//        verify((checkCaseType), times(1)).isOneAppOrTtbWeb(anyString());
//        verify((checkCaseType), times(1)).isCreateOneAppAndPayrollOrPWACase(any(String.class));
//    }

//    @Test
//    public void isCompletedOrResolvedOneAppOrTtbWebCase_shouldReturnFalse3() {
//        CreateCaseDTO dto = new CreateCaseDTO().setIntegrationSystem(Constant.ONE_APP)
//                .setCreateByEmployeeID("55555");
//
//        when(checkCaseType.isOneAppOrTtbWeb(anyString())).thenReturn(true);
//        when(checkCaseType.isCompletedStatus(any(CreateCaseDTO.class))).thenReturn(false);
//        when(checkCaseType.isResolvedStatus(any(CreateCaseDTO.class))).thenReturn(true);
//
//        assertFalse(() -> service.isCompletedOrResolvedOneAppOrTtbWebCase(dto));
//        verify((checkCaseType), times(1)).isOneAppOrTtbWeb(anyString());
//        verify((checkCaseType), times(1)).isCompletedStatus(any(CreateCaseDTO.class));
//        verify((checkCaseType), times(1)).isResolvedStatus(any(CreateCaseDTO.class));
//    }

    @Test
    public void isOneAppPayrollAndPwaCase_false() {
        CreateCaseDTO dto = new CreateCaseDTO().setIntegrationSystem(Constant.ONE_APP)
                .setServiceTypeMatrixCodeOld("STMOLD");

        when(checkCaseType.isOneApp(anyString())).thenReturn(true);
        when(checkCaseType.isCreateOneAppAndPayrollOrPWACase(anyString())).thenReturn(false);

        assertFalse(() -> service.isOneAppPayrollAndPwaCase(dto));
        verify((checkCaseType), times(1)).isOneApp(anyString());
        verify((checkCaseType), times(1)).isCreateOneAppAndPayrollOrPWACase(anyString());
    }

    @Test
    public void isOneAppPayrollAndPwaCase_false2() {
        CreateCaseDTO dto = new CreateCaseDTO().setIntegrationSystem(Constant.ONE_APP)
                .setServiceTypeMatrixCodeOld("STMOLD");

        when(checkCaseType.isOneApp(anyString())).thenReturn(false);

        assertFalse(() -> service.isOneAppPayrollAndPwaCase(dto));
        verify((checkCaseType), times(1)).isOneApp(anyString());
    }

    @Test
    public void isOneAppPayrollAndPwaCase_true() {
        CreateCaseDTO dto = new CreateCaseDTO().setIntegrationSystem(Constant.ONE_APP)
                .setServiceTypeMatrixCodeOld("STMOLD");

        when(checkCaseType.isOneApp(anyString())).thenReturn(true);
        when(checkCaseType.isCreateOneAppAndPayrollOrPWACase(anyString())).thenReturn(true);

        assertTrue(() -> service.isOneAppPayrollAndPwaCase(dto));
        verify((checkCaseType), times(1)).isOneApp(anyString());
        verify((checkCaseType), times(1)).isCreateOneAppAndPayrollOrPWACase(anyString());
    }

    @Test
    public void validateOwnerRequired_shouldThrowExceptionNoOwnerName() {
        CreateCaseDTO dto = new CreateCaseDTO().setOwnerEmployeeId(Constant.SF_EX_API_ID);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> service.validateOwnerRequired(dto));

        assertTrue(exception.getMessage().contains("Owner name is required"));
    }

    @Test
    public void validateCreatedCaseCompletedData_shouldThrowExceptionNoClosedDate() {
        CreateCaseDTO dto = new CreateCaseDTO().setServiceTypeMatrixCodeOld(Constant.SF_EX_API_ID);

//        when(checkCaseType.isCreateOneAppAndPayrollOrPWACase(anyString())).thenReturn(true);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> service.validateClosedDateData(dto));
        assertTrue(exception.getMessage().contains("Closed date is required"));
//        verify((checkCaseType), times(1)).isCreateOneAppAndPayrollOrPWACase(anyString());
    }

    @Test
    public void validateCreatedCaseCompletedData_shouldNotThrowException() {
        ZonedDateTime zonedDateTime = parseToZoneDateTime("2025-07-07 17:12:28.853 +0000");
        CreateCaseDTO dto = new CreateCaseDTO().setServiceTypeMatrixCodeOld(Constant.SF_EX_API_ID)
                .setClosedDate(zonedDateTime);

//        when(checkCaseType.isCreateOneAppAndPayrollOrPWACase(anyString())).thenReturn(true);

        assertDoesNotThrow(() -> service.validateClosedDateData(dto));
//        verify((checkCaseType), times(1)).isCreateOneAppAndPayrollOrPWACase(anyString());
    }

    @Test
    public void validateResolveDate_shouldThrowExceptionNoClosedDate() {
        CreateCaseDTO dto = new CreateCaseDTO().setServiceTypeMatrixCodeOld("STMOLD");

        when(checkCaseType.isCreateOneAppAndPayrollOrPWACase(anyString())).thenReturn(true);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> service.validateResolveDate(dto));
        assertTrue(exception.getMessage().contains("Resolved date is required"));
        verify((checkCaseType), times(1)).isCreateOneAppAndPayrollOrPWACase(anyString());
    }

    @Test
    public void validateResolveDate_shouldNotThrowException() {
        ZonedDateTime zonedDateTime = parseToZoneDateTime("2025-07-07 17:12:28.853 +0000");
        CreateCaseDTO dto = new CreateCaseDTO().setServiceTypeMatrixCodeOld("STMOLD")
                .setResolvedDate(zonedDateTime);

        when(checkCaseType.isCreateOneAppAndPayrollOrPWACase(anyString())).thenReturn(true);

        assertDoesNotThrow(() -> service.validateResolveDate(dto));
        verify((checkCaseType), times(1)).isCreateOneAppAndPayrollOrPWACase(anyString());
    }

    @Test
    public void validateResolveData_shouldThrowExceptionNoClosedDate() {
        CreateCaseDTO dto = new CreateCaseDTO().setFcr(true);

        when(checkCaseType.isFcr(anyBoolean())).thenReturn(false);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> service.validateResolveData(dto));
        assertTrue(exception.getMessage().contains("Resolution code is required"));
        verify((checkCaseType), times(1)).isFcr(anyBoolean());
    }

    @Test
    public void validateResolveData_shouldNotThrowException() {
        CreateCaseDTO dto = new CreateCaseDTO().setFcr(true);

        when(checkCaseType.isFcr(anyBoolean())).thenReturn(true);

        assertDoesNotThrow(() -> service.validateResolveData(dto));
        verify((checkCaseType), times(1)).isFcr(anyBoolean());
    }

    @Test
    public void validateResolveData_shouldNotThrowException2() {
        CreateCaseDTO dto = new CreateCaseDTO().setResolutionListCode("reso00003")
                .setResolutionListValue("ดำเนินการตามที่ลูกค้าร้องขอ")
                .setRootCauseListCode("issc00018")
                .setRootCauseListValue("Customer_ลูกค้าร้องขอบริการ")
                .setFcr(false);

        when(checkCaseType.isFcr(anyBoolean())).thenReturn(false);

        assertDoesNotThrow(() -> service.validateResolveData(dto));
        verify((checkCaseType), times(1)).isFcr(anyBoolean());
    }

//    @Test
//    public void validateModifiedDateCaseData_shouldNotThrowException() {
//        CreateCaseDTO dto = new CreateCaseDTO().setCaseNumber("2025000001");
//
//        when(caseRepository.findByCaseNumber(dto.getCaseNumber())).thenReturn(Optional.empty());
//
//        assertDoesNotThrow(() -> service.validateModifiedDateCaseData(dto));
//        verify((caseRepository), times(1)).findByCaseNumber(any());
//    }

//    @Test
//    public void validateModifiedDateCaseData_shouldNotThrowException2() {
//        ZonedDateTime zonedDateTime = parseToZoneDateTime("2025-07-07 17:12:28.853 +0000");
//        CreateCaseDTO dto = new CreateCaseDTO().setCaseNumber("2025000001");
//        CaseIdAndModifiedOnOnDTO caseTransactionModel = new CaseIdAndModifiedOnOnDTO();
//        caseTransactionModel.setModifiedOn(zonedDateTime);
//
//        when(caseRepository.findByCaseNumber(dto.getCaseNumber())).thenReturn(Optional.ofNullable(caseTransactionModel));
//
//        assertDoesNotThrow(() -> service.validateModifiedDateCaseData(dto));
//        verify((caseRepository), times(1)).findByCaseNumber(any());
//    }

//    @Test
//    public void validateModifiedDateCaseData_shouldNotThrowException3() {
//        ZonedDateTime zonedDateTime = parseToZoneDateTime("2025-07-07 17:12:28.853 +0000");
//        CreateCaseDTO dto = new CreateCaseDTO().setCaseNumber("2025000001");
//        dto.setModifiedOn(zonedDateTime);
//        CaseIdAndModifiedOnOnDTO caseTransactionModel = new CaseIdAndModifiedOnOnDTO();
//        caseTransactionModel.setModifiedOn(zonedDateTime);
//
//        when(caseRepository.findByCaseNumber(dto.getCaseNumber())).thenReturn(Optional.ofNullable(caseTransactionModel));
//
//        Exception exception = assertThrows(IllegalArgumentException.class, () -> service.validateModifiedDateCaseData(dto));
//        assertTrue(exception.getMessage().contains("Case has already been modified"));
//        verify((caseRepository), times(1)).findByCaseNumber(any());
//    }

//    @Test
//    public void validateModifiedDateCaseData_shouldNotThrowException4() {
//        UUID caseId = UUID.randomUUID();
//        ZonedDateTime zonedDateTime = parseToZoneDateTime("2025-07-07 17:12:28.853 +0000");
//        ZonedDateTime zonedDateTime2 = parseToZoneDateTime("2025-07-09 17:12:28.853 +0000");
//        CreateCaseDTO dto = new CreateCaseDTO().setCaseNumber("2025000001");
//        dto.setModifiedOn(zonedDateTime2);
//        CaseIdAndModifiedOnOnDTO caseTransactionModel = new CaseIdAndModifiedOnOnDTO();
//        caseTransactionModel.setCaseId(caseId);
//
//        when(caseRepository.findByCaseNumber(dto.getCaseNumber())).thenReturn(Optional.ofNullable(caseTransactionModel));
//        doNothing().when(entityManager).flush();
//        doNothing().when(entityManager).flush();
//        doNothing().when(entityManager).flush();
//
//        assertThrows(NullPointerException.class, () -> service.validateModifiedDateCaseData(dto));
    /// /        assertDoesNotThrow(() -> service.validateModifiedDateCaseData(dto));
//        verify((caseRepository), times(1)).findByCaseNumber(any());
//    }

    CreateCaseDTO dto = new CreateCaseDTO()
            .setCaseNumber("2025000001")
            .setIntegrationSystem(Constant.TTB_WEB)
            .setServiceTypeMatrixCodeOld(Constant.ONE_APP);

    @Test
    public void testValidateSfActivitiesCompleted_emptyActivities_shouldNotThrow() {
        List<StgSlaPerOwnerModel> sfActivities = Collections.emptyList();

        when(checkCaseType.isTtbWebOrCreateOneAppAndPayrollOrPWACase(anyString(), anyString())).thenReturn(true);
        when(checkCaseType.isCompletedStatus(dto)).thenReturn(true);

        assertDoesNotThrow(() -> service.validateSfActivitiesCompleted(sfActivities, dto));
    }

    @Test
    public void testValidateSfActivitiesResolved_noResolved_shouldThrow() {
        List<StgSlaPerOwnerModel> sfActivities = List.of(createUnresolvedActivity());

        when(checkCaseType.isTtbWebOrCreateOneAppAndPayrollOrPWACase(anyString(), anyString())).thenReturn(true);
        when(checkCaseType.isCompletedStatus(dto)).thenReturn(true);

        assertThrows(NotFoundException.class, () -> service.validateSfActivitiesResolved(sfActivities, dto));
    }

    private StgSlaPerOwnerModel createUnresolvedActivity() {
        StgSlaPerOwnerModel model = new StgSlaPerOwnerModel();
        model.setCaseStatusC("IN_PROGRESS"); // Adjust based on your actual status field
        return model;
    }


    @Test
    public void testPrepareSlaPerOwnerActivities_noResolvedWithMatchingTeam_shouldThrow() {
        StgSlaPerOwnerModel activity = new StgSlaPerOwnerModel();
        activity.setOwnerTeamNew("TeamA");

        List<StgSlaPerOwnerModel> sfActivities = List.of(activity);

        when(checkCaseType.isResolvedStatus(activity)).thenReturn(false);

        assertThrows(NullPointerException.class, () -> service.prepareSlaPerOwnerActivities(sfActivities));
    }

    @Test
    public void testPrepareSlaPerOwnerActivities_teamMismatch_shouldClearFields() {
        StgSlaPerOwnerModel activity = new StgSlaPerOwnerModel();
        activity.setOwnerTeamNew("TeamA");
        activity.setEmployeeIdC("E123");
        activity.setName("John Doe");

        List<StgSlaPerOwnerModel> sfActivities = List.of(activity);

        when(checkCaseType.isResolvedStatus(activity)).thenReturn(false);

        assertThrows(NullPointerException.class, () -> service.prepareSlaPerOwnerActivities(sfActivities));
    }

//    @Test
//    public void testCreateValidatorWithTeamCheck_invokesAllValidations() {
//        int totalSize = 5;
//        boolean isCompleted = true;
//        int index = 2;
//
//        StgSlaPerOwnerModel model = new StgSlaPerOwnerModel();
//
//        assertDoesNotThrow(() -> service.createValidatorWithTeamCheck(totalSize, isCompleted));
//    }

}
