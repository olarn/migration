package com.ttb.crm.service.migrationdata.model.caseManagement;

import com.ttb.crm.service.migrationdata.bean.CreateCaseDTO;
import com.ttb.crm.service.migrationdata.bean.response.CreateCaseData;
import com.ttb.crm.service.migrationdata.enums.ServiceTypeMatrixTypeEnum;
import com.ttb.crm.service.migrationdata.model.masterManagement.ServiceTypeMatrixModel;
import com.ttb.crm.service.migrationdata.model.userManagement.EmployeeUserModel;
import com.ttb.crm.service.migrationdata.model.userManagement.TeamReadonlyModel;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Immutable;
import org.springframework.beans.BeanUtils;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "case_transaction")
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@Immutable
public class CaseTransactionModel {

    @Id
    @GeneratedValue
    @Column(name = "case_id", nullable = false, updatable = false)
    private UUID caseId;

    @Version
    private Long version;

    @Column(name = "case_number", nullable = false)
    private String caseNumber;

    @Column(name = "parent_case_number")
    private String parentCaseNumber;

    @Column(name = "ec_id")
    private String ecId;

    @Column(name = "rm_id", length = 30)
    private String rmId;

    @Column(name = "customer_name", length = 250)
    private String customerName;

    @Column(name = "contact_person_name", length = 100)
    private String contactPersonName;

    @Column(name = "contact_person_email", length = 250)
    private String contactPersonEmail;

    @Column(name = "contact_person_phone", length = 10)
    private String contactPersonPhone;

    @Column(name = "contact_person_phone_2", length = 10)
    private String contactPersonPhone2;

    @Column(name = "to_ext_2", length = 40)
    private String toExt2;

    @Column(name = "contact_person_channel_code", length = 50)
    private String contactPersonChannelCode;

    @Column(name = "contact_person_channel_value", length = 300)
    private String contactPersonChannelValue;

    @Column(name = "priority_code", length = 50)
    private String priorityCode;

    @Column(name = "priority_value", length = 300)
    private String priorityValue;

    @Column(name = "description", length = 3200)
    private String description;

    @Column(name = "original_problem_channel_code", length = 50)
    private String originalProblemChannelCode;

    @Column(name = "original_problem_channel_value", length = 300)
    private String originalProblemChannelValue;

    @Column(name = "data_source_code", length = 50)
    private String dataSourceCode;

    @Column(name = "data_source_value", length = 300)
    private String dataSourceValue;

    @Column(name = "product_type_code_1")
    private String productTypeCode1;

    @Column(name = "product_type_value_1")
    private String productTypeValue1;

    @Column(name = "sub_product_type_code_1")
    private String subProductTypeCode1;

    @Column(name = "sub_product_type_value_1")
    private String subProductTypeValue1;

    @Column(name = "product_number_full_1")
    private String productNumberFull1;

    @Column(name = "product_number_marking_1")
    private String productNumberMarking1;

    @Column(name = "suffix_1", length = 5)
    private String suffix1;

    @Column(name = "fund_code_1", length = 25)
    private String fundCode1;

    @Column(name = "product_type_code_2")
    private String productTypeCode2;

    @Column(name = "product_type_value_2")
    private String productTypeValue2;

    @Column(name = "sub_product_type_code_2")
    private String subProductTypeCode2;

    @Column(name = "sub_product_type_value_2")
    private String subProductTypeValue2;

    @Column(name = "product_number_full_2")
    private String productNumberFull2;

    @Column(name = "product_number_marking_2")
    private String productNumberMarking2;

    @Column(name = "suffix_2", length = 5)
    private String suffix2;

    @Column(name = "fund_code_2", length = 25)
    private String fundCode2;

    @Column(name = "product_type_code_3")
    private String productTypeCode3;

    @Column(name = "product_type_value_3")
    private String productTypeValue3;

    @Column(name = "sub_product_type_code_3")
    private String subProductTypeCode3;

    @Column(name = "sub_product_type_value_3")
    private String subProductTypeValue3;

    @Column(name = "product_number_full_3")
    private String productNumberFull3;

    @Column(name = "product_number_marking_3")
    private String productNumberMarking3;

    @Column(name = "suffix_3", length = 5)
    private String suffix3;

    @Column(name = "fund_code_3", length = 25)
    private String fundCode3;

    @Column(name = "transaction_date")
    private ZonedDateTime transactionDate;

    @Column(name = "fund_transfer_bill_payment_amount")
    private Float fundTransferBillPaymentAmount;

    @Column(name = "amount_deposit_withdrawal")
    private Float amountDepositWithdrawal;

    @Column(name = "biller_provider_name")
    private String billerProviderName;

    @Column(name = "ref_1")
    private String ref1;

    @Column(name = "ref_2")
    private String ref2;

    @Column(name = "ref_3")
    private String ref3;

    @Column(name = "grouping_service_point_code", length = 50)
    private String groupingServicePointCode;

    @Column(name = "grouping_service_point_value", length = 300)
    private String groupingServicePointValue;

    @Column(name = "atm_bank_owner_code", length = 50)
    private String atmBankOwnerCode;

    @Column(name = "atm_bank_owner_value", length = 300)
    private String atmBankOwnerValue;

    @Column(name = "branch_atm_shop_location_transaction_code")
    private String branchAtmShopLocationTransactionCode;

    @Column(name = "branch_atm_shop_location_transaction")
    private String branchAtmShopLocationTransaction;

    @Column(name = "branch_atm_shop_address_thai")
    private String branchAtmShopAddressThai;

    @Column(name = "branch_atm_shop_number")
    private String branchAtmShopNumber;

    @Column(name = "transfer_amount")
    private Float transferAmount;

    @Column(name = "inter_bank_recipient_account_no", length = 175)
    private String interBankRecipientAccountNo;

    @Column(name = "wrong_transfer_account")
    private String wrongTransferAccount;

    @Column(name = "correct_recipient_bank_code", length = 50)
    private String correctRecipientBankCode;

    @Column(name = "correct_recipient_bank_value", length = 300)
    private String correctRecipientBankValue;

    @Column(name = "correct_bank_recipient_account_no", length = 175)
    private String correctBankRecipientAccountNo;

    @Column(name = "correct_target_account")
    private String correctTargetAccount;

    @Column(name = "transaction_type_code", length = 50)
    private String transactionTypeCode;

    @Column(name = "transaction_type_value", length = 300)
    private String transactionTypeValue;

    @Column(name = "amount_withdrawal_deposit")
    private Float amountWithdrawalDeposit;

    @Column(name = "amount_received_deposit_to_account")
    private Float amountReceivedDepositToAccount;

    @Column(name = "transfer_type_code", length = 50)
    private String transferTypeCode;

    @Column(name = "transfer_type_value", length = 300)
    private String transferTypeValue;

    @Column(name = "recipient_bank_code", length = 50)
    private String recipientBankCode;

    @Column(name = "recipient_bank_value", length = 300)
    private String recipientBankValue;

    @Column(name = "prompt_pay", length = 20)
    private String promptPay;

    @Column(name = "prompt_pay_transfer_account")
    private String promptPayTransferAccount;

    @Column(name = "deposit_amount")
    private Float depositAmount;

    @Column(name = "wrong_transfer_type_code", length = 50)
    private String wrongTransferTypeCode;

    @Column(name = "wrong_transfer_type_value", length = 300)
    private String wrongTransferTypeValue;

    @Column(name = "wrong_prompt_pay")
    private String wrongPromptPay;

    @Column(name = "wrong_prompt_pay_transfer_account")
    private String wrongPromptPayTransferAccount;

    @Column(name = "case_status_code", nullable = false, length = 50)
    private String caseStatusCode;

    @Column(name = "case_status_value", length = 300)
    private String caseStatusValue;

    @Column(name = "active_hop_number")
    private Integer activeHopNumber;

    @Column(name = "owner_id")
    private UUID ownerId;

    @Column(name = "owner_name")
    private String ownerName;

    @Column(name = "team_id")
    private UUID teamId;

    @Column(name = "team_name")
    private String teamName;

    @Column(name = "created_by_team_id")
    private UUID createdByTeamId;

    @Column(name = "sla_start_date")
    private ZonedDateTime slaStartDate;

    @Column(name = "sla_target_date")
    private ZonedDateTime slaTargetDate;

    @Column(name = "sla_status")
    private String slaStatus;

    @Column(name = "call_number", length = 10)
    private String callNumber;

    @Column(name = "participant_id", length = 50)
    private String participantId;

    @Column(name = "conversation_id", length = 50)
    private String conversationId;

    @Column(name = "start_call")
    private String startCall;

    @Column(name = "end_call")
    private String endCall;

    @Column(name = "call_duration")
    private String callDuration;

    @Column(name = "call_channel")
    private String callChannel;

    @Column(name = "integration_system")
    private String integrationSystem;

    @Column(name = "sub_integration_system", length = 50)
    private String subIntegrationSystem;

    @Column(name = "external_id")
    private String externalId;

    @Column(name = "receiver_name")
    private String receiverName;

    @Column(name = "document_id")
    private String documentId;

    @Column(name = "object_id")
    private String objectId;

    @Column(name = "repository_id")
    private String repositoryId;

    @Column(name = "document_type")
    private String documentType;

    @Column(name = "ready_to_print")
    private Boolean readyToPrint;

    @Column(name = "ems_tracking")
    private String emsTracking;

    @Column(name = "address")
    private String address;

    @Column(name = "service_type_matrix_code", nullable = false)
    private String serviceTypeMatrixCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type_matrix_type")
    private ServiceTypeMatrixTypeEnum serviceTypeMatrixType;

    @Column(name = "service_category_code", length = 50)
    private String serviceCategoryCode;

    @Column(name = "service_category_value", length = 300)
    private String serviceCategoryValue;

    @Column(name = "service_tab_code", length = 50)
    private String serviceTabCode;

    @Column(name = "service_tab_value", length = 300)
    private String serviceTabValue;

    @Column(name = "sub_service_tab_code", length = 50)
    private String subServiceTabCode;

    @Column(name = "sub_service_tab_value", length = 300)
    private String subServiceTabValue;

    @Column(name = "service_template_type_code", length = 50)
    private String serviceTemplateTypeCode;

    @Column(name = "service_template_type_value", length = 300)
    private String serviceTemplateTypeValue;

    @Column(name = "service_template_code", length = 50)
    private String serviceTemplateCode;

    @Column(name = "service_template_value", length = 300)
    private String serviceTemplateValue;

    @Column(name = "supported_channel_code", length = 50)
    private String supportedChannelCode;

    @Column(name = "supported_channel_value", length = 300)
    private String supportedChannelValue;

    @Column(name = "product_service_code", length = 50)
    private String productServiceCode;

    @Column(name = "product_service_value_th", length = 300)
    private String productServiceValueTh;

    @Column(name = "product_service_value_en", length = 300)
    private String productServiceValueEn;

    @Column(name = "ntb_eligible")
    private Boolean ntbEligible;

    @Column(name = "issue_name_ttb_touch_th", length = 250)
    private String issueNameTtbTouchTh;

    @Column(name = "issue_name_ttb_touch_en", length = 250)
    private String issueNameTtbTouchEn;

    @Column(name = "issue_th", nullable = false, length = 250)
    private String issueTh;

    @Column(name = "issue_en", length = 250)
    private String issueEn;

    @Column(name = "issue_line_2_th", length = 250)
    private String issueLine2Th;

    @Column(name = "issue_line_2_en", length = 250)
    private String issueLine2En;

    @Column(name = "auto_close_case_after_resolved")
    private Boolean autoCloseCaseAfterResolved;

    @Column(name = "sla", nullable = false)
    private Float sla;

    @Column(name = "severity_code", nullable = false, length = 50)
    private String severityCode;

    @Column(name = "severity_value", nullable = false, length = 300)
    private String severityValue;

    @Column(name = "sms_code_new", length = 50)
    private String smsCodeNew;

    @Column(name = "sms_code_in_progress", length = 50)
    private String smsCodeInProgress;

    @Column(name = "sms_code_resolved", length = 50)
    private String smsCodeResolved;

    @Column(name = "sms_code_completed", length = 50)
    private String smsCodeCompleted;

    @Column(name = "sms_code_resolution_1", length = 50)
    private String smsCodeResolution1;

    @Column(name = "sms_code_resolution_2", length = 50)
    private String smsCodeResolution2;

    @Column(name = "resolution_1_code", length = 50)
    private String resolution1Code;

    @Column(name = "resolution_1_value", length = 300)
    private String resolution1Value;

    @Column(name = "resolution_2_code", length = 50)
    private String resolution2Code;

    @Column(name = "resolution_2_value", length = 300)
    private String resolution2Value;

    @Column(name = "message_id", length = 100)
    private String messageId;

    @Column(name = "url_link", length = 500)
    private String urlLink;

    @Column(name = "fr_required")
    private Boolean frRequired;

    @Column(name = "fcr")
    private Boolean fcr;

    @Column(name = "visible_on_touch")
    private Boolean visibleOnTouch;

    @Column(name = "carousel_service_tab_code", length = 50)
    private String carouselServiceTabCode;

    @Column(name = "carousel_service_tab_value", length = 300)
    private String carouselServiceTabValue;

    @Column(name = "pta_segment_code", length = 50)
    private String ptaSegmentCode;

    @Column(name = "pta_segment_value", length = 300)
    private String ptaSegmentValue;

    @Column(name = "cancel_reason_code", length = 50)
    private String cancelReasonCode;

    @Column(name = "cancel_reason_value", length = 300)
    private String cancelReasonValue;

    @Column(name = "cancel_reason_comment", length = 500)
    private String cancelReasonComment;

    @Column(name = "change_service_type_matrix_reason_code", length = 50)
    private String changeServiceTypeMatrixReasonCode;

    @Column(name = "change_service_type_matrix_reason_value", length = 300)
    private String changeServiceTypeMatrixReasonValue;

    @Column(name = "change_service_type_matrix_reason_comment")
    private String changeServiceTypeMatrixReasonComment;

    @Column(name = "previous_reason_code", length = 50)
    private String previousReasonCode;

    @Column(name = "previous_reason_value", length = 300)
    private String previousReasonValue;

    @Column(name = "previous_reason_comment", length = 500)
    private String previousReasonComment;

    @Column(name = "root_cause_list_code", length = 50)
    private String rootCauseListCode;

    @Column(name = "root_cause_list_value", length = 300)
    private String rootCauseListValue;

    @Column(name = "root_cause_list_comment", length = 500)
    private String rootCauseListComment;

    @Column(name = "resolution_list_code", length = 50)
    private String resolutionListCode;

    @Column(name = "resolution_list_value", length = 300)
    private String resolutionListValue;

    @Column(name = "resolution_list_comment", length = 500)
    private String resolutionListComment;

    @Column(name = "change_owner_reason", length = 500)
    private String changeOwnerReason;

    @Column(name = "approved_amount_1")
    private Float approvedAmount1;

    @Column(name = "approved_amount_2")
    private Float approvedAmount2;

    @Column(name = "approved_amount_3")
    private Float approvedAmount3;

    @Column(name = "branch_name")
    private String branchName;

    @Column(name = "branch_code")
    private String branchCode;

    @Column(name = "staff_name")
    private String staffName;

    @Column(name = "staff_id")
    private String staffId;

    @Column(name = "resolved_by")
    private String resolvedBy;

    @Column(name = "resolved_by_team")
    private String resolvedByTeam;

    @Column(name = "resolved_date")
    private ZonedDateTime resolvedDate;

    @Column(name = "closed_by_id")
    private UUID closedById;

    @Column(name = "closed_by")
    private String closedBy;

    @Column(name = "closed_by_team")
    private String closedByTeam;

    @Column(name = "closed_by_team_id")
    private UUID closedByTeamId;

    @Column(name = "closed_date")
    private ZonedDateTime closedDate;

    @Column(name = "issue_2_en")
    private String issue2En;

    @Transient
    private String previousCaseStatusCode;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "case_id")
    @OrderBy("hopNumber ASC")
    private List<CaseSlaHopModel> slaHop;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "case_id")
    private List<CaseDocumentReferenceModel> caseDocumentReferences;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "case_id")
    private List<CaseCommentModel> caseComments;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "case_id")
    private List<CaseSlaActivity> caseSlaActivities;

    @Column(name = "status_change_date")
    private ZonedDateTime statusChangeDate;
    @Column(name = "status_change_by_id")
    private UUID statusChangeById;
    @Column(name = "status_change_by_name")
    private String statusChangeByName;
    @Column(name = "status_change_by_team_id")
    private UUID statusChangeByTeamId;
    @Column(name = "status_change_by_team_name")
    private String statusChangeByTeamName;
    @Column(name = "resolved_by_id")
    private UUID resolvedById;
    @Column(name = "resolved_by_team_id")
    private UUID resolvedByTeamId;

    @Column(name = "total_duration")
    private Float totalDuration;

    @Column(name = "is_stm_locked", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isSTMLocked = Boolean.FALSE;

    @Column(name = "is_hop_locked", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isHopLocked = Boolean.FALSE;

    @Column(name = "is_owner_locked", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isOwnerLocked = Boolean.FALSE;

    @Column(name = "is_migration")
    private Boolean isMigration;

    @Column(name = "migration_lot", length = 50)
    private String migrationLot;

    @Column(name = "created_on")
    private ZonedDateTime createdOn;

    @Column(name = "created_by_id")
    private UUID createdById;

    @Column(name = "modified_on")
    private ZonedDateTime modifiedOn;

    @Column(name = "modified_by_id")
    private UUID modifiedById;

    @Column(name = "status_code")
    private Integer statusCode;

    public CaseTransactionModel(CreateCaseData data) {
        this.setDataFromRequest(data.request());
        this.setServiceTypeMatrixData(data.stm());
        this.setDataCreateBy(data);
        this.setDataMigrate();
        this.setDataTotalDuration(data.toTolDuration());
    }

    private void setDataFromRequest(CreateCaseDTO request) {
        BeanUtils.copyProperties(request, this);
        if (request.getTransactionDate() != null) {
            this.setTransactionDate(request.getTransactionDate());
        }
    }

    private void setServiceTypeMatrixData(ServiceTypeMatrixModel serviceTypeMatrixDto) {
        BeanUtils.copyProperties(serviceTypeMatrixDto, this);
    }

    private void setDataCreateBy(CreateCaseData caseData) {
        this.setCreatedByTeamId(caseData.createTeam().getTeamId())
                .setCreatedById(caseData.createUser().getUserId())
                .setCreatedOn(caseData.request().getCreatedOn());
    }

    public void setDataModifiedBy(CaseSlaActivity activity) {
        this.setModifiedById(activity.getModifiedById())
                .setModifiedOn(activity.getModifiedOn());
    }

    private void setDataMigrate() {
        this.setIsMigration(Boolean.TRUE);
    }

    private void setDataTotalDuration(Float duration) {
        this.setTotalDuration(duration);
    }

    public void setStatusChange(EmployeeUserModel user, TeamReadonlyModel team) {
        if (user != null && user.getUserId() != null && !user.getUserId().equals(new UUID(0L, 0L))) {
            this.setStatusChangeDate(ZonedDateTime.now(ZoneId.systemDefault()));
            this.setStatusChangeById(user.getUserId());
            this.setStatusChangeByName(user.getFullNameTH());
        }
        if (team != null && team.getTeamId() != null && !team.getTeamId().equals(new UUID(0L, 0L))) {
            this.setStatusChangeByTeamId(team.getTeamId());
            this.setStatusChangeByTeamName(team.getNameTh());
        }
    }

    public void cleanClosedInfo() {
        this.setClosedByTeam(null)
                .setClosedByTeamId(null)
                .setClosedById(null)
                .setClosedBy(null)
                .setClosedDate(null);
    }

    public void cleanResolvedInfo() {
        this.setResolvedBy(null)
                .setResolvedByTeam(null)
                .setResolvedByTeamId(null)
                .setResolvedById(null)
                .setResolvedDate(null)
                .setResolutionListCode(null)
                .setResolutionListValue(null)
                .setResolutionListComment(null)
                .setRootCauseListCode(null)
                .setRootCauseListValue(null)
                .setRootCauseListComment(null);

    }
}
