package com.ttb.crm.service.migrationdata.helper;

import com.ttb.crm.service.migrationdata.model.secondary.StgCaseInProgressModel;
import org.springframework.stereotype.Component;

import static com.ttb.crm.service.migrationdata.helper.DateTimeUtils.parseToZoneDateTime;

@Component
public class MockTempCase {

    public StgCaseInProgressModel mockTempStgCaseInProgress() {
        StgCaseInProgressModel caseModel = new StgCaseInProgressModel();
        caseModel.setId(380980L);
        caseModel.setCaseIssueC("ขอใช้ / เปลี่ยนแปลง / ยกเลิก บริการหักบัญชีอัตโนมัติ ลูกค้า CYB");
        caseModel.setDescriptionC("ขอเปลี่ยนวันชำระ เป็นทุกวันที่ 20 มีผล 20/6/68");
        caseModel.setOriginalProblemChannelCode("orpc00019");
        caseModel.setOriginalProblemChannelValue("ลูกค้าสัมพันธ์");
        caseModel.setPriority("Medium");
        caseModel.setPriorityCode("prty00002");
        caseModel.setPriorityValue("Medium");
        caseModel.setContactPersonPhoneC("******2626");
        caseModel.setContactPersonChannelC("Phone");
        caseModel.setContactPersonChannelCode("ctpc00001");
        caseModel.setContactPersonChannelValue("Phone");
        caseModel.setFcrC("FALSE");
        caseModel.setCaseNumber("5025120015115021");
        caseModel.setStatus("In progress");
        caseModel.setStatusCode("IN_PROGRESS");
        caseModel.setStatusValue("In progress");
        caseModel.setSlaDayC("2.0");
        caseModel.setDisplayOnOneAppC("TRUE");
        caseModel.setIsOverSlaC("1.0");
        caseModel.setSlaStatus("OVER_SLA");
        caseModel.setCategoryC("Request");
        caseModel.setCategoryCode("REQUEST");
        caseModel.setCategoryValue("Request");
        caseModel.setProductCategoryC("ttb DRIVE cash your car");
        caseModel.setProductCategoryCode("pser00006");
        caseModel.setProductCategoryValue("ttb DRIVE cash your car");
        caseModel.setIssueNewForOneappC("ขอเปลี่ยนแปลงการหักบัญชีอัตโนมัติ");
        caseModel.setIssueNewForOneappEnC("Change auto direct debit");
        caseModel.setResolutionListC("Other");
        caseModel.setResolutionListCode("reso00016");
        caseModel.setResolutionListValue("Other");
        caseModel.setResolutionC("สาขาดำเนินการส่งเรื่องใหม่ เนื่องจากแจ้งผิดหน่วยงาน รบกวนปิดเคสด้วยค่ะ");
        caseModel.setRootCauseListC("Other");
        caseModel.setRootCauseListCode("issc00034");
        caseModel.setRootCauseListValue("Other");
        caseModel.setRootCauseC("-");
        caseModel.setCurrentServiceTemplateC("Product");
        caseModel.setCurrentServiceTemplateCode("stem00002");
        caseModel.setCurrentServiceTemplateValue("Product");
        caseModel.setServiceTypeMatrixCodeC("26149");
        caseModel.setAccountName("น***** ม******");
        caseModel.setTmbCustomerIdPeC("**************************4193");
        caseModel.setPtaSegmentC("RBG");
        caseModel.setPtaSegmentCode("segm00001");
        caseModel.setPtaSegmentValue("RBG");
        caseModel.setCaseSeverityC("3");
        caseModel.setCaseSeverityCode("prty00003");
        caseModel.setCaseSeverityValue("Low");
        caseModel.setCommitDateC("2025-06-06 00:00:00");
        caseModel.setSfId("500RC00000hs5UiSEJ");
        caseModel.setIsNewcase(false);
        caseModel.setCreatedDate("2025-06-03 17:18:13");
        caseModel.setCreatedByEmployeeIdC("create");
        caseModel.setCreatedNameC("Thanakorn Chaiyasit");
        caseModel.setCreatedByTeamC("Creator Team");
        caseModel.setCreatedCloseDateTeam("2025-06-03 17:18:13");
        caseModel.setResponsibleBuDatetimeC("2025-06-03 17:18:14");
        caseModel.setResponsibleBuC("Inbound Voice Team 8");
        caseModel.setResponsibleOwnerDatetimeC("2025-06-03 17:18:14");
        caseModel.setResponsibleOwnerName("Jakrapan Sangkharat");
        caseModel.setOwnerTeamDatetimeC("2025-06-07T04:51:11.000Z");
        caseModel.setOwnerTeamC("Investment Line");
        caseModel.setOwnerStartDatetimeC("2025-06-02 17:18:14");
        caseModel.setOwnerEmployeeIdC("owner");
        caseModel.setOwnerNameC("Sukjai Sandee");
        caseModel.setIntegrationSystem("TEP");
        caseModel.setLastModifiedDate("2025-06-02 17:18:14");
        caseModel.setLastModifiedByEmployeeIdC("owner");
        caseModel.setRecordStatus("Success");
        caseModel.setRecordRemark("Recipient_Bank__c : Success | Correct_Recipient_Bank__c : Success | Transaction_Type__c : Success | ATM_Bank_Owner__c : Success | Origin : Success | Priority : Success | Contact_Person_Channel__c : Success | Status : Success | Category__c : Success | Product_Category__c : Success | Resolution_List__c : Success | Root_Cause_List__c : Success | Return_Reason__c : Success | Current_Service_Template__c : Success | PTA_Segment__c : Success | ");
        caseModel.setRecordLastDatetime(parseToZoneDateTime("2025-07-07 17:12:28.853 +0000"));
        caseModel.setServiceTypeMatrixCodeNew("STM002837");
        caseModel.setCreatedByTeamNew("Inbound Voice Team 1");
        caseModel.setResponsibleBuNew("Inbound Voice Team 8");
        caseModel.setOwnerTeamNew("Investment Line");

        return caseModel;

    }

    public StgCaseInProgressModel mockTempStgCaseInProgressCompleted() {
        StgCaseInProgressModel caseModel = new StgCaseInProgressModel();
        caseModel.setId(380980L);
        caseModel.setCaseIssueC("ขอใช้ / เปลี่ยนแปลง / ยกเลิก บริการหักบัญชีอัตโนมัติ ลูกค้า CYB");
        caseModel.setDescriptionC("ขอเปลี่ยนวันชำระ เป็นทุกวันที่ 20 มีผล 20/6/68");
        caseModel.setOriginalProblemChannelCode("orpc00019");
        caseModel.setOriginalProblemChannelValue("ลูกค้าสัมพันธ์");
        caseModel.setPriority("Medium");
        caseModel.setPriorityCode("prty00002");
        caseModel.setPriorityValue("Medium");
        caseModel.setContactPersonPhoneC("******2626");
        caseModel.setContactPersonChannelC("Phone");
        caseModel.setContactPersonChannelCode("ctpc00001");
        caseModel.setContactPersonChannelValue("Phone");
        caseModel.setFcrC("FALSE");
        caseModel.setCaseNumber("5025120015115021");
        caseModel.setStatus("Completed");
        caseModel.setStatusCode("COMPLETED");
        caseModel.setStatusValue("Completed");
        caseModel.setSlaDayC("2.0");
        caseModel.setDisplayOnOneAppC("TRUE");
        caseModel.setIsOverSlaC("1.0");
        caseModel.setSlaStatus("OVER_SLA");
        caseModel.setCategoryC("Request");
        caseModel.setCategoryCode("REQUEST");
        caseModel.setCategoryValue("Request");
        caseModel.setProductCategoryC("ttb DRIVE cash your car");
        caseModel.setProductCategoryCode("pser00006");
        caseModel.setProductCategoryValue("ttb DRIVE cash your car");
        caseModel.setIssueNewForOneappC("ขอเปลี่ยนแปลงการหักบัญชีอัตโนมัติ");
        caseModel.setIssueNewForOneappEnC("Change auto direct debit");
        caseModel.setResolutionListC("Other");
        caseModel.setResolutionListCode("reso00016");
        caseModel.setResolutionListValue("Other");
        caseModel.setResolutionC("สาขาดำเนินการส่งเรื่องใหม่ เนื่องจากแจ้งผิดหน่วยงาน รบกวนปิดเคสด้วยค่ะ");
        caseModel.setRootCauseListC("Other");
        caseModel.setRootCauseListCode("issc00034");
        caseModel.setRootCauseListValue("Other");
        caseModel.setRootCauseC("-");
        caseModel.setCurrentServiceTemplateC("Product");
        caseModel.setCurrentServiceTemplateCode("stem00002");
        caseModel.setCurrentServiceTemplateValue("Product");
        caseModel.setServiceTypeMatrixCodeC("26149");
        caseModel.setAccountName("น***** ม******");
        caseModel.setTmbCustomerIdPeC("**************************4193");
        caseModel.setPtaSegmentC("RBG");
        caseModel.setPtaSegmentCode("segm00001");
        caseModel.setPtaSegmentValue("RBG");
        caseModel.setCaseSeverityC("3");
        caseModel.setCaseSeverityCode("prty00003");
        caseModel.setCaseSeverityValue("Low");
        caseModel.setCommitDateC("2025-06-11 00:00:00");
        caseModel.setSfId("500RC00000hs5UiSEJ");
        caseModel.setIsNewcase(false);
        caseModel.setCreatedDate("2025-06-09 01:00:00");
        caseModel.setCreatedByEmployeeIdC("55501");
        caseModel.setCreatedNameC("Somporn Pandee");
        caseModel.setCreatedByTeamC("Inbound Voice Team 1");
        caseModel.setIntegrationSystem("TEP");
        caseModel.setLastModifiedDate("2025-06-11 10:00:00");
        caseModel.setLastModifiedByEmployeeIdC("99902");
        caseModel.setRecordStatus("Success");
        caseModel.setRecordRemark("Recipient_Bank__c : Success | Correct_Recipient_Bank__c : Success | Transaction_Type__c : Success | ATM_Bank_Owner__c : Success | Origin : Success | Priority : Success | Contact_Person_Channel__c : Success | Status : Success | Category__c : Success | Product_Category__c : Success | Resolution_List__c : Success | Root_Cause_List__c : Success | Return_Reason__c : Success | Current_Service_Template__c : Success | PTA_Segment__c : Success | ");
        caseModel.setRecordLastDatetime(parseToZoneDateTime("2025-07-07 17:12:28.853 +0000"));
        caseModel.setServiceTypeMatrixCodeNew("STM002837");
        caseModel.setCreatedByTeamNew("Inbound Voice Team 1");
        caseModel.setResponsibleBuNew("Inbound Voice Team 2");
        caseModel.setOwnerTeamDatetimeC("2025-06-07T04:51:11.000Z");
        caseModel.setOwnerTeamC("Investment Line");
        caseModel.setOwnerStartDatetimeC("2025-06-02 17:18:14");
        caseModel.setOwnerEmployeeIdC("99902");
        caseModel.setOwnerNameC("Sukjai Sandee");
        caseModel.setClosedByBuC("Investment Line");
        caseModel.setClosedByC("99902 Sukjai Sandee");
        caseModel.setClosedDate("2025-06-11 10:00:00");
        caseModel.setClosedNameC("Sukjai Sandee");
        caseModel.setClosedEmployeeId("99902");
        caseModel.setClosedByBuNew("Investment Line");
        caseModel.setClosedStartDatetime("2025-06-11 10:00:00");
        caseModel.setResolvedByC("55515 Inbound Voice Team 8");
        caseModel.setResolvedTeam("Inbound Voice Team 8");
        caseModel.setResolvedTeamNew("Inbound Voice Team 8");
        caseModel.setResolvedName("Jakrapan Sangkharat");
        caseModel.setResolvedEmployeeId("55515");
        caseModel.setResolvedDateTimeC("2025-06-10 08:00:00");
        return caseModel;
    }

    public StgCaseInProgressModel mockTempStgCaseInProgressCompletedResolutionIsLongOverLengthInCaseModel() {
        StgCaseInProgressModel caseModel = mockTempStgCaseInProgressCompleted();
        caseModel.setResolutionC("ผลการตรวจสอบรายการโอนเงินของท่านสำเร็จ กรุณาตรวจสอบบัญชีปลายทางอีกครั้ง ถ้าเงินไม่เข้าบัญชี ให้ตรวจสอบ ดูยอดเงินวันถัดไปทีธนาคารปลายทางผู้รับจะปรับปรุงคืนเงินเข้าบัญชีไม่เกิน 19.00 น. หากท่านมีข้อสงสัยเกี่ยวกับรายการดังกล่าว กรุณาติดต่อทีเอ็มบีธนชาต คอนแทค เซ็นเตอร์ 1428 ผลการตรวจสอบรายการโอนเงินของท่านสำเร็จ กรุณาตรวจสอบบัญชีปลายทางอีกครั้ง ถ้าเงินไม่เข้าบัญชี ให้ตรวจสอบ ดูยอดเงินวันถัดไปทีธนาคารปลายทางผู้รับจะปรับปรุงคืนเงินเข้าบัญชีไม่เกิน 19.00 น. หากท่านมีข้อสงสัยเกี่ยวกับรายการดังกล่าว กรุณาติดต่อทีเอ็มบีธนชาต คอนแทค เซ็นเตอร์ 1428 ผลการตรวจสอบรายการโอนเงินของท่านสำเร็จ กรุณาตรวจสอบบัญชีปลายทางอีกครั้ง ถ้าเงินไม่เข้าบัญชี ให้ตรวจสอบ ดูยอดเงินวันถัดไปทีธนาคารปลายทางผู้รับจะปรับปรุงคืนเงินเข้าบัญชีไม่เกิน 19.00 น. หากท่านมีข้อสงสัยเกี่ยวกับรายการดังกล่าว กรุณาติดต่อทีเอ็มบีธนชาต คอนแทค เซ็นเตอร์ 1428");
        return caseModel;
    }

    public StgCaseInProgressModel mockTempStgCaseInProgressCompletedOneApp() {
        StgCaseInProgressModel caseModel = mockTempStgCaseInProgressCompleted();
        caseModel.setIntegrationSystem(Constant.ONE_APP);
        return caseModel;
    }

    public StgCaseInProgressModel mockTempStgCaseInProgressCompletedOneAppMyAdvisorStmO0011() {
        StgCaseInProgressModel caseModel = mockTempStgCaseInProgressCompleted();
        caseModel.setIntegrationSystem(Constant.ONE_APP);
        caseModel.setOwnerTeamNew(null);
        caseModel.setOwnerTeamC(null);
        caseModel.setCreatedNameC(Constant.SF_EX_API_FULL_NAME);
        caseModel.setCreatedByEmployeeIdC(Constant.SF_EX_API_ID);
        caseModel.setOwnerNameC("Jakrapan Sangkharat");
        caseModel.setOwnerEmployeeIdC("55515");
        caseModel.setCreatedDate("2025-06-09 01:00:00");
        caseModel.setClosedDate("2025-06-11 10:00:00");
        caseModel.setServiceTypeMatrixCodeC("O0011");
        return caseModel;
    }

    public StgCaseInProgressModel mockTempStgCaseInProgressCompletedCreatedByEmployeeSameClosedEmployee() {
        StgCaseInProgressModel caseModel = mockTempStgCaseInProgressCompleted();
        caseModel.setClosedEmployeeId("55501");
        caseModel.setClosedNameC("Somporn Pandee");
        return caseModel;
    }

    public StgCaseInProgressModel mockTempStgCaseInProgressCompletedOneAppMyAdvisorStmO0012() {
        StgCaseInProgressModel caseModel = mockTempStgCaseInProgressCompletedOneAppMyAdvisorStmO0011();
        caseModel.setServiceTypeMatrixCodeC("O0012");
        return caseModel;
    }

    public StgCaseInProgressModel mockTempStgCaseInProgressCompletedOneAppMyAdvisorStmO0013() {
        StgCaseInProgressModel caseModel = mockTempStgCaseInProgressCompletedOneAppMyAdvisorStmO0011();
        caseModel.setServiceTypeMatrixCodeC("O0013");
        caseModel.setOwnerTeamNew("xxx");
        return caseModel;
    }

    public StgCaseInProgressModel mockTempStgCaseInProgressCompletedTtbWeb() {
        StgCaseInProgressModel caseModel = mockTempStgCaseInProgressCompleted();
        caseModel.setIntegrationSystem(Constant.TTB_WEB);
        caseModel.setOriginalProblemChannelCode(Constant.TTB_WEB);
        caseModel.setOriginalProblemChannelValue(Constant.TTB_WEB);
        return caseModel;
    }

    public StgCaseInProgressModel mockTempStgCaseInProgressCompletedNoneStmCode() {
        StgCaseInProgressModel caseModel = mockTempStgCaseInProgressCompleted();
        caseModel.setServiceTypeMatrixCodeNew(null);
        return caseModel;
    }

    public StgCaseInProgressModel mockTempStgCaseInProgressCompletedTtbWebNoneStmCode() {
        StgCaseInProgressModel caseModel = mockTempStgCaseInProgressCompletedNoneStmCode();
        caseModel.setIntegrationSystem(Constant.TTB_WEB);
        caseModel.setOriginalProblemChannelCode(Constant.TTB_WEB);
        caseModel.setOriginalProblemChannelValue(Constant.TTB_WEB);
        return caseModel;
    }

    public StgCaseInProgressModel mockTempStgCaseInProgressResolved() {
        StgCaseInProgressModel caseModel = new StgCaseInProgressModel();
        caseModel.setId(380980L);
        caseModel.setCaseIssueC("ขอใช้ / เปลี่ยนแปลง / ยกเลิก บริการหักบัญชีอัตโนมัติ ลูกค้า CYB");
        caseModel.setDescriptionC("ขอเปลี่ยนวันชำระ เป็นทุกวันที่ 20 มีผล 20/6/68");
        caseModel.setOriginalProblemChannelCode("orpc00019");
        caseModel.setOriginalProblemChannelValue("ลูกค้าสัมพันธ์");
        caseModel.setPriority("Medium");
        caseModel.setPriorityCode("prty00002");
        caseModel.setPriorityValue("Medium");
        caseModel.setContactPersonPhoneC("******2626");
        caseModel.setContactPersonChannelC("Phone");
        caseModel.setContactPersonChannelCode("ctpc00001");
        caseModel.setContactPersonChannelValue("Phone");
        caseModel.setFcrC("FALSE");
        caseModel.setCaseNumber("5025120015115021");
        caseModel.setStatus("RESOLVED");
        caseModel.setStatusCode("RESOLVED");
        caseModel.setStatusValue("Resolved");
        caseModel.setSlaDayC("2.0");
        caseModel.setDisplayOnOneAppC("TRUE");
        caseModel.setIsOverSlaC("1.0");
        caseModel.setSlaStatus("OVER_SLA");
        caseModel.setCategoryC("Request");
        caseModel.setCategoryCode("REQUEST");
        caseModel.setCategoryValue("Request");
        caseModel.setProductCategoryC("ttb DRIVE cash your car");
        caseModel.setProductCategoryCode("pser00006");
        caseModel.setProductCategoryValue("ttb DRIVE cash your car");
        caseModel.setIssueNewForOneappC("ขอเปลี่ยนแปลงการหักบัญชีอัตโนมัติ");
        caseModel.setIssueNewForOneappEnC("Change auto direct debit");
        caseModel.setResolutionListC("Other");
        caseModel.setResolutionListCode("reso00016");
        caseModel.setResolutionListValue("Other");
        caseModel.setResolutionC("สาขาดำเนินการส่งเรื่องใหม่ เนื่องจากแจ้งผิดหน่วยงาน รบกวนปิดเคสด้วยค่ะ");
        caseModel.setRootCauseListC("Other");
        caseModel.setRootCauseListCode("issc00034");
        caseModel.setRootCauseListValue("Other");
        caseModel.setRootCauseC("test");
        caseModel.setCurrentServiceTemplateC("Product");
        caseModel.setCurrentServiceTemplateCode("stem00002");
        caseModel.setCurrentServiceTemplateValue("Product");
        caseModel.setServiceTypeMatrixCodeC("26149");
        caseModel.setAccountName("น***** ม******");
        caseModel.setTmbCustomerIdPeC("**************************4193");
        caseModel.setPtaSegmentC("RBG");
        caseModel.setPtaSegmentCode("segm00001");
        caseModel.setPtaSegmentValue("RBG");
        caseModel.setCaseSeverityC("3");
        caseModel.setCaseSeverityCode("prty00003");
        caseModel.setCaseSeverityValue("Low");
        caseModel.setCommitDateC("2025-06-11 00:00:00");
        caseModel.setSfId("500RC00000hs5UiSEJ");
        caseModel.setIsNewcase(false);
        caseModel.setCreatedDate("2025-06-09 01:00:00");
        caseModel.setCreatedByEmployeeIdC("55501");
        caseModel.setCreatedNameC("Somporn Pandee");
        caseModel.setCreatedByTeamC("Inbound Voice Team 1");
        caseModel.setIntegrationSystem("TEP");
        caseModel.setLastModifiedDate("2025-06-11 10:00:00");
        caseModel.setLastModifiedByEmployeeIdC("99902");
        caseModel.setRecordStatus("Success");
        caseModel.setRecordRemark("Recipient_Bank__c : Success | Correct_Recipient_Bank__c : Success | Transaction_Type__c : Success | ATM_Bank_Owner__c : Success | Origin : Success | Priority : Success | Contact_Person_Channel__c : Success | Status : Success | Category__c : Success | Product_Category__c : Success | Resolution_List__c : Success | Root_Cause_List__c : Success | Return_Reason__c : Success | Current_Service_Template__c : Success | PTA_Segment__c : Success | ");
        caseModel.setRecordLastDatetime(parseToZoneDateTime("2025-07-07 17:12:28.853 +0000"));
        caseModel.setServiceTypeMatrixCodeNew("STM002837");
        caseModel.setCreatedByTeamNew("Inbound Voice Team 1");
        caseModel.setResponsibleBuNew("Inbound Voice Team 2");
        caseModel.setOwnerTeamDatetimeC("2025-06-07T04:51:11.000Z");
        caseModel.setOwnerTeamC("Investment Line");
        caseModel.setOwnerStartDatetimeC("2025-06-02 17:18:14");
        caseModel.setOwnerEmployeeIdC("99902");
        caseModel.setOwnerNameC("Sukjai Sandee");
        caseModel.setClosedByBuC(null);
        caseModel.setClosedByC(null);
        caseModel.setClosedDate(null);
        caseModel.setClosedNameC(null);
        caseModel.setClosedEmployeeId(null);
        caseModel.setClosedByBuNew(null);
        caseModel.setClosedStartDatetime(null);
        caseModel.setResolvedByC("55515 Inbound Voice Team 8");
        caseModel.setResolvedTeam("Inbound Voice Team 8");
        caseModel.setResolvedTeamNew("Inbound Voice Team 8");
        caseModel.setResolvedName("Jakrapan Sangkharat");
        caseModel.setResolvedEmployeeId("55515");
        caseModel.setResolvedDateTimeC("2025-06-10 08:00:00");
        return caseModel;
    }

    public StgCaseInProgressModel mockTempStgCaseInProgressResolvedOneApp() {
        StgCaseInProgressModel caseModel = mockTempStgCaseInProgressResolved();
        caseModel.setIntegrationSystem(Constant.ONE_APP);
        return caseModel;
    }

    public StgCaseInProgressModel mockTempStgCaseInProgressResolvedCreatedByEmployeeSameClosedEmployee() {
        StgCaseInProgressModel caseModel = mockTempStgCaseInProgressResolved();
        caseModel.setClosedEmployeeId("55501");
        caseModel.setClosedNameC("Somporn Pandee");
        return caseModel;
    }

    public StgCaseInProgressModel mockTempStgCaseInProgressResolvedOneAppMyAdvisorStmO0011() {
        StgCaseInProgressModel caseModel = mockTempStgCaseInProgressResolved();
        caseModel.setIntegrationSystem(Constant.ONE_APP);
        caseModel.setOwnerTeamNew(null);
        caseModel.setOwnerTeamC(null);
        caseModel.setCreatedNameC(Constant.SF_EX_API_FULL_NAME);
        caseModel.setCreatedByEmployeeIdC(Constant.SF_EX_API_ID);
        caseModel.setOwnerNameC("Jakrapan Sangkharat");
        caseModel.setOwnerEmployeeIdC("55515");
        caseModel.setCreatedDate("2025-06-09 01:00:00");
        caseModel.setResolvedDateTimeC("2025-06-11 10:00:00");
        caseModel.setServiceTypeMatrixCodeC("O0011");
        return caseModel;
    }

    public StgCaseInProgressModel mockTempStgCaseInProgressResolvedOneAppMyAdvisorStmO0012() {
        StgCaseInProgressModel caseModel = mockTempStgCaseInProgressResolvedOneAppMyAdvisorStmO0011();
        caseModel.setServiceTypeMatrixCodeC("O0012");
        return caseModel;
    }

    public StgCaseInProgressModel mockTempStgCaseInProgressResolvedOneAppMyAdvisorStmO0013() {
        StgCaseInProgressModel caseModel = mockTempStgCaseInProgressResolvedOneAppMyAdvisorStmO0011();
        caseModel.setServiceTypeMatrixCodeC("O0013");
        return caseModel;
    }

    public StgCaseInProgressModel mockTempStgCaseInProgressResolvedTtbWeb() {
        StgCaseInProgressModel caseModel = mockTempStgCaseInProgressResolved();
        caseModel.setIntegrationSystem(Constant.TTB_WEB);
        caseModel.setOriginalProblemChannelCode(Constant.TTB_WEB);
        caseModel.setOriginalProblemChannelValue(Constant.TTB_WEB);
        return caseModel;
    }

    public StgCaseInProgressModel mockTempStgCaseInProgressResolvedTtbWebNoneStmCode() {
        StgCaseInProgressModel caseModel = mockTempStgCaseInProgressResolvedTtbWeb();
        caseModel.setServiceTypeMatrixCodeNew(null);
        return caseModel;
    }
}
