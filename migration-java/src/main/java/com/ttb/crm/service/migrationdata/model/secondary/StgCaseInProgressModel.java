package com.ttb.crm.service.migrationdata.model.secondary;

import com.ttb.crm.service.migrationdata.model.batch.TempStgCaseInProgressLogModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.time.ZonedDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "stg_case_inprogress")
public class StgCaseInProgressModel {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "Case_Issue__c")
    private String caseIssueC;

    @Column(name = "ParentCaseNumber")
    private String parentCaseNumber;

    @Column(name = "Product_Type_1__c")
    private String productType1C;

    @Column(name = "Product_Type_1__Code")
    private String productType1Code;

    @Column(name = "Product_Type_1__Value")
    private String productType1Value;

    @Column(name = "Product_Number_1__c")
    private String productNumber1C;

    @Column(name = "Product_Number_Full_1__c")
    private String productNumberFull1C;

    @Column(name = "Suffix_1__c")
    private String suffix1C;

    @Column(name = "FundCode_1__c")
    private String fundCode1C;

    @Column(name = "Product_Type_2__c")
    private String productType2C;

    @Column(name = "Product_Type_2__Code")
    private String productType2Code;

    @Column(name = "Product_Type_2__Value")
    private String productType2Value;

    @Column(name = "Product_Number_2__c")
    private String productNumber2C;

    @Column(name = "Product_Number_Full_2__c")
    private String productNumberFull2C;

    @Column(name = "Suffix_2__c")
    private String suffix2C;

    @Column(name = "FundCode_2__c")
    private String fundCode2C;

    @Column(name = "Product_Type_3__c")
    private String productType3C;

    @Column(name = "Product_Type_3__Code")
    private String productType3Code;

    @Column(name = "Product_Type_3__Value")
    private String productType3Value;

    @Column(name = "Product_Number_3__c")
    private String productNumber3C;

    @Column(name = "Product_Number_Full_3__c")
    private String productNumberFull3C;

    @Column(name = "Suffix_3__c")
    private String suffix3C;

    @Column(name = "FundCode_3__c")
    private String fundCode3C;

    @Column(name = "Transaction_Date__c")
    private String transactionDateC;

    @Column(name = "Fund_Transfer_Bill_Payment_Amount__c")
    private String fundTransferBillPaymentAmountC;

    @Column(name = "Amount_Deposit_Withdrawal__c")
    private String amountDepositWithdrawalC;

    @Column(name = "Biller_Provider_Name__c")
    private String billerProviderNameC;

    @Column(name = "Ref_1__c")
    private String ref1C;

    @Column(name = "Ref_2__c")
    private String ref2C;

    @Column(name = "Transfer_Amount__c")
    private String transferAmountC;

    @Column(name = "Deposit_Amount__c")
    private String depositAmountC;

    @Column(name = "Recipient_Bank__c")
    private String recipientBankC;

    @Column(name = "Recipient_Bank__Code")
    private String recipientBankCode;

    @Column(name = "Recipient_Bank__Value")
    private String recipientBankValue;

    @Column(name = "Inter_Bank_Recipient_Account_No__c")
    private String interBankRecipientAccountNoC;

    @Column(name = "Wrong_Transfer_Account__c")
    private String wrongTransferAccountC;

    @Column(name = "Correct_Recipient_Bank__c")
    private String correctRecipientBankC;

    @Column(name = "Correct_Recipient_Bank__Code")
    private String correctRecipientBankCode;

    @Column(name = "Correct_Recipient_Bank__Value")
    private String correctRecipientBankValue;

    @Column(name = "Correct_Bank_Recipient_Account_No__c")
    private String correctBankRecipientAccountNoC;

    @Column(name = "Correct_Target_Account__c")
    private String correctTargetAccountC;

    @Column(name = "Prompt_Pay__c")
    private String promptPayC;

    @Column(name = "Prompt_Pay")
    private String promptPay;

    @Column(name = "Wrong_Prompt_Pay")
    private String wrongPromptPay;

    @Column(name = "Receiver_Name__c")
    private String receiverNameC;

    @Column(name = "transaction_type__c")
    private String transactionTypeC;

    @Column(name = "Transaction_Type__Code")
    private String transactionTypeCode;

    @Column(name = "Transaction_Type__Value")
    private String transactionTypeValue;

    @Column(name = "Amount_Withdrawal_Deposit__c")
    private String amountWithdrawalDepositC;

    @Column(name = "Amount_Received_Deposit_to_Account__c")
    private String amountReceivedDepositToAccountC;

    @Column(name = "Branch_ATM_Shop_Location_Transaction__c")
    private String branchAtmShopLocationTransactionC;

    @Column(name = "ATM_Bank_Owner__c")
    private String atmBankOwnerC;

    @Column(name = "ATM_Bank_Owner__Code")
    private String atmBankOwnerCode;

    @Column(name = "ATM_Bank_Owner__Value")
    private String atmBankOwnerValue;

    @Column(name = "Description__c")
    private String descriptionC;

    @Column(name = "Description")
    private String description;

    @Column(name = "Guideline__c")
    private String guidelineC;

//    @Column(name = "origin")
//    private String origin;

    @Column(name = "original_problem_channel_code")
    private String originalProblemChannelCode;

    @Column(name = "original_problem_channel_value")
    private String originalProblemChannelValue;

    @Column(name = "data_source_code")
    private String dataSourceCode;

    @Column(name = "data_source_value")
    private String dataSourceValue;

    @Column(name = "Priority")
    private String priority;

    @Column(name = "Priority_Code")
    private String priorityCode;

    @Column(name = "Priority_Value")
    private String priorityValue;

    @Column(name = "Participant_ID__c")
    private String participantIdC;

    @Column(name = "Call_Log_ID__c")
    private String callLogIdC;

    @Column(name = "Contact_Person_Name__c")
    private String contactPersonNameC;

    @Column(name = "Contact_Person_Phone__c")
    private String contactPersonPhoneC;

    @Column(name = "Contact_Person_Phone_2__c")
    private String contactPersonPhone2C;

    @Column(name = "To_Ext_2__c")
    private String toExt2C;

    @Column(name = "Contact_Person_Email__c")
    private String contactPersonEmailC;

    @Column(name = "Contact_Person_Channel__c")
    private String contactPersonChannelC;

    @Column(name = "Contact_Person_Channel__Code")
    private String contactPersonChannelCode;

    @Column(name = "Contact_Person_Channel__Value")
    private String contactPersonChannelValue;

    @Column(name = "FCR__c")
    private String fcrC;

    @Column(name = "Staff_Employee_ID__c")
    private String staffIdREmployeeIdC;

    @Column(name = "Branch_Code__c")
    private String branchCodeC;

    @Column(name = "Branch_Name__c")
    private String branchNameC;

    @Column(name = "Call_Number__c")
    private String callNumberC;

    @Column(name = "Approved_Amount__c")
    private String approvedAmountC;

    @Column(name = "CaseNumber")
    private String caseNumber;

    @Column(name = "Status")
    private String status;

    @Column(name = "Status_Code")
    private String statusCode;

    @Column(name = "Status_Value")
    private String statusValue;

    @Column(name = "SLA_Day__c")
    private String slaDayC;

    @Column(name = "Display_on_OneApp__c")
    private String displayOnOneAppC;

    @Column(name = "Is_Over_SLA__c")
    private String isOverSlaC;

    @Column(name = "sla_status")
    private String slaStatus;

    @Column(name = "Category__c")
    private String categoryC;

    @Column(name = "Category__Code")
    private String categoryCode;

    @Column(name = "Category__Value")
    private String categoryValue;

    @Column(name = "Product_Category__c")
    private String productCategoryC;

    @Column(name = "Product_Category__Code")
    private String productCategoryCode;

    @Column(name = "Product_Category__Value")
    private String productCategoryValue;

    @Column(name = "Issue_New_for_OneApp__c")
    private String issueNewForOneappC;

    @Column(name = "Issue_New_for_OneApp_EN__c")
    private String issueNewForOneappEnC;

    @Column(name = "Resolution_List__c")
    private String resolutionListC;

    @Column(name = "Resolution_List__Code")
    private String resolutionListCode;

    @Column(name = "Resolution_List__Value")
    private String resolutionListValue;

    @Column(name = "Resolution__c")
    private String resolutionC;

    @Column(name = "Root_Cause_List__c")
    private String rootCauseListC;

    @Column(name = "Root_Cause_List__Code")
    private String rootCauseListCode;

    @Column(name = "Root_Cause_List__Value")
    private String rootCauseListValue;

    @Column(name = "Root_Cause__c")
    private String rootCauseC;

    @Column(name = "Return_Reason__c")
    private String returnReasonC;

    @Column(name = "Return_Reason__Code")
    private String returnReasonCode;

    @Column(name = "Return_Reason__Value")
    private String returnReasonValue;

    @Column(name = "Current_Service_Template__c")
    private String currentServiceTemplateC;

    @Column(name = "Current_Service_Template__Code")
    private String currentServiceTemplateCode;

    @Column(name = "Current_Service_Template__Value")
    private String currentServiceTemplateValue;

    @Column(name = "Owner_Employee_ID__c")
    private String ownerEmployeeIdC;

    @Column(name = "Owner_Name__c") //new
    private String ownerNameC;

    @Column(name = "Owner_Start_Datetime") //new
    private String ownerStartDatetimeC;

    @Column(name = "Owner_Team_Datetime__c") //new
    private String ownerTeamDatetimeC;

    @Column(name = "Owner_Team__c")
    private String ownerTeamC;

    @Column(name = "Owner_Team_New")
    private String ownerTeamNew;

    @Column(name = "Service_Type_Matrix_Code__c")
    private String serviceTypeMatrixCodeC;

    @Column(name = "Service_Type_Matrix_Code_New")
    private String serviceTypeMatrixCodeNew;

    @Column(name = "Account_Name")
    private String accountName;

    @Column(name = "TMB_Customer_ID_PE__c")
    private String tmbCustomerIdPeC;

    @Column(name = "SMS_Code_In_progress__c")
    private String smsCodeInProgressC;

    @Column(name = "SMS_Code_New__c")
    private String smsCodeNewC;

    @Column(name = "SMS_Code_Resolution_1__c")
    private String smsCodeResolution1C;

    @Column(name = "SMS_Code_Resolution_2__c")
    private String smsCodeResolution2C;

    @Column(name = "SMS_Code_Resolved__c")
    private String smsCodeResolvedC;

    @Column(name = "PTA_Segment__c")
    private String ptaSegmentC;

    @Column(name = "PTA_Segment__Code")
    private String ptaSegmentCode;

    @Column(name = "PTA_Segment__Value")
    private String ptaSegmentValue;

    @Column(name = "Card_No__c")
    private String cardNoC;

    @Column(name = "Card_No2__c")
    private String cardNo2C;

    @Column(name = "Closed_date")
    private String closedDate;

    @Column(name = "Case_Severity__c")
    private String caseSeverityC;

    @Column(name = "Case_Severity__Code")
    private String caseSeverityCode;

    @Column(name = "Case_Severity__Value")
    private String caseSeverityValue;

    @Column(name = "Address__c")
    private String addressC;

    @Column(name = "Document_Id__c")
    private String documentIdC;

    @Column(name = "Document_Type__c")
    private String documentTypeC;

    @Column(name = "EMS_Tracking_No__c")
    private String emsTrackingNoC;

    @Column(name = "Object_Id__c")
    private String objectIdC;

    @Column(name = "Repository_Id__c")
    private String repositoryIdC;

    @Column(name = "Staff_ID__c")
    private String staffIdC;

    @Column(name = "Resolved_By__c")
    private String resolvedByC;

    @Column(name = "Resolved_Employee_ID")
    private String resolvedEmployeeId; // new

    @Column(name = "Resolved_Name")
    private String resolvedName; // new

    @Column(name = "Resolved_Team")
    private String resolvedTeam;

    @Column(name = "Resolved_Team_New")
    private String resolvedTeamNew; // new

    @Column(name = "Resolved_Date_Time__c")
    private String resolvedDateTimeC;

    @Column(name = "Closed_Start_Datetime")
    private String closedStartDatetime; // new

    @Column(name = "Closed_by__c")
    private String closedByC;

    @Column(name = "Closed_Employee_ID")
    private String closedEmployeeId; //new

    @Column(name = "Closed_Name")
    private String closedNameC; //new

    @Column(name = "Closed_By_BU__c")
    private String closedByBuC;

    @Column(name = "Closed_By_BU_New")
    private String closedByBuNew;

    @Column(name = "SF_ID")
    private String sfId;

    @Column(name = "CreatedDate")
    private String createdDate;

    @Column(name = "CreatedBy_Employee_ID__c")
    private String createdByEmployeeIdC;

    @Column(name = "Created_Name__c")
    private String createdNameC; //new

    @Column(name = "Created_By_Team__c")
    private String createdByTeamC;

    @Column(name = "Created_By_Team_New")
    private String createdByTeamNew; //new

    @Column(name = "Created_CloseDate_Team")
    private String createdCloseDateTeam; //new

    @Column(name = "LastModifiedDate")
    private String lastModifiedDate;

    @Column(name = "LastModifiedBy_Employee_ID__c")
    private String lastModifiedByEmployeeIdC;

    @Column(name = "Commit_Date__c")
    private String commitDateC;

    @Column(name = "is_migration")
    private Boolean isMigration;

    @Column(name = "migration_lot", length = 50)
    private String migrationLot;

    @Column(name = "mid")
    private Long mid;

    @Column(name = "record_status", length = 50)
    private String recordStatus;

    @Column(name = "record_remark")
    private String recordRemark;

    @Column(name = "record_lastdatetime")
    private ZonedDateTime recordLastDatetime;

    @Column(name = "load_status", length = 50)
    private String loadStatus;

    @Column(name = "load_remark")
    private String loadRemark;

    @Column(name = "load_lastdatetime")
    private ZonedDateTime loadLastDatetime;

    @Column(name = "Responsible_BU__c")
    private String responsibleBuC;

    @Column(name = "Responsible_BU_New")
    private String responsibleBuNew; // new

    @Column(name = "Responsible_BU_Datetime__c")
    private String responsibleBuDatetimeC; // new

    @Column(name = "Responsible_Owner__c")
    private String responsibleOwnerC; // new

    @Column(name = "Responsible_Owner_Name")
    private String responsibleOwnerName; // new

    @Column(name = "Responsible_Owner_Datetime__c")
    private String responsibleOwnerDatetimeC; // new

    @Column(name = "Responsible_CloseDate")
    private String responsibleCloseDate; // new

    @Column(name = "Integration_System")
    private String integrationSystem; // new

    @Column(name = "is_newcase")
    private Boolean isNewcase;

    @Column(name = "Auto_Close_After_Resolved")
    private String autoCloseAfterResolved;

    @Column(name = "Created_Channel__c")
    private String createdChannelC;

    @Column(name = "Previous_Owner__c")
    private String previousOwnerC;

    public StgCaseInProgressModel(TempStgCaseInProgressLogModel tempStgCaseInProgressLogModel) {
        BeanUtils.copyProperties(tempStgCaseInProgressLogModel, this);
    }

}