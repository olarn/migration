package com.ttb.crm.service.migrationdata.bean;

import com.ttb.crm.lib.crmssp_common_utils_lib.bean.BaseModelBean;
import com.ttb.crm.service.migrationdata.enums.ServiceTypeMatrixTypeEnum;
import com.ttb.crm.service.migrationdata.model.caseManagement.CaseCommentModel;
import com.ttb.crm.service.migrationdata.model.caseManagement.CaseDocumentReferenceModel;
import com.ttb.crm.service.migrationdata.model.caseManagement.CaseSlaHopModel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class CreateCaseDTO extends BaseModelBean {
    private UUID caseId;

    private String caseNumber; //

    private String parentCaseNumber; //

    private String ecId;

    private String rmId; //

    private String customerName; //

    private String contactPersonName; //

    private String contactPersonEmail; //

    private String contactPersonPhone; //

    private String contactPersonPhone2; //

    private String toExt2; //

    private String contactPersonChannelCode; //

    private String contactPersonChannelValue; //

    private String priorityCode; //

    private String priorityValue; //

    private String description; //

    private String originalProblemChannelCode; //

    private String originalProblemChannelValue; //

    private String dataSourceCode; //

    private String dataSourceValue; //

    private String productTypeCode1; //

    private String productTypeValue1; //

    private String subProductTypeCode1;

    private String subProductTypeValue1;

    private String productNumberFull1; //

    private String productNumberMarking1; //

    private String suffix1; //

    private String fundCode1; //

    private String productTypeCode2; //

    private String productTypeValue2; //

    private String subProductTypeCode2;

    private String subProductTypeValue2;

    private String productNumberFull2; //

    private String productNumberMarking2; //

    private String suffix2; //

    private String fundCode2; //

    private String productTypeCode3; //

    private String productTypeValue3; //

    private String subProductTypeCode3;

    private String subProductTypeValue3;

    private String productNumberFull3; //

    private String productNumberMarking3; //

    private String suffix3; //

    private String fundCode3; //

    private ZonedDateTime transactionDate; //

    private Float fundTransferBillPaymentAmount; //

    private Float amountDepositWithdrawal; //

    private String billerProviderName; //

    private String ref1; //

    private String ref2; //

    private String ref3;

    private String groupingServicePointCode;

    private String groupingServicePointValue;

    private String atmBankOwnerCode; // it set after

    private String atmBankOwnerValue; // it set after

    private String branchAtmShopLocationTransactionCode; // X have only branchAtmShopLocationTransaction

    private String branchAtmShopLocationTransaction; //

    private String branchAtmShopAddressThai; // X

    private String branchAtmShopNumber; // X

    private Float transferAmount; //

    private String interBankRecipientAccountNo; //

    private String wrongTransferAccount; //

    private String correctRecipientBankCode; //

    private String correctRecipientBankValue; //

    private String correctBankRecipientAccountNo; //

    private String correctTargetAccount; //

    private String transactionTypeCode; //

    private String transactionTypeValue; //

    private Float amountWithdrawalDeposit; //

    private Float amountReceivedDepositToAccount; //

    private String transferTypeCode;

    private String transferTypeValue;

    private String recipientBankCode; //

    private String recipientBankValue; //

    private String promptPay; //

    private String promptPayTransferAccount;

    private Float depositAmount; //

    private String wrongTransferTypeCode;

    private String wrongTransferTypeValue;

    private String wrongPromptPay; //

    private String wrongPromptPayTransferAccount;

    private String caseStatusCode; //

    private String caseStatusValue; //

    private Integer activeHopNumber;

    private String ownerEmployeeId; // for retrieve

    private UUID ownerId; //

    private String ownerName;

    private ZonedDateTime assignOwnerDate; // from stg

    private ZonedDateTime startTeamDate; // from stg

    private UUID teamId; //

    private String teamName; // get and set

    private UUID createdByTeamId; // get and set

    private ZonedDateTime slaStartDate;

    private ZonedDateTime slaTargetDate; //

    private String slaStatus; //

    private String callNumber; //

    private String participantId; //

    private String conversationId; //

    private String startCall;

    private String endCall;

    private String callDuration;

    private String callChannel;

    private String integrationSystem; //

    private Boolean subIntegrationSystem;

    private String externalId; //

    private String receiverName; //

    private String documentId; //

    private String objectId; //

    private String repositoryId; //

    private String documentType; //

    private Boolean readyToPrint;

    private String emsTracking; //

    private String address; //

    private String serviceTypeMatrixCode; // new

    private String serviceTypeMatrixCodeOld;

    private ServiceTypeMatrixTypeEnum serviceTypeMatrixType;

    private String serviceCategoryCode; //

    private String serviceCategoryValue; //

    private String serviceTabCode;

    private String serviceTabValue;

    private String subServiceTabCode;

    private String subServiceTabValue;

    private String serviceTemplateTypeCode;

    private String serviceTemplateTypeValue;

    private String serviceTemplateCode; //

    private String serviceTemplateValue; //

    private String supportedChannelCode;

    private String supportedChannelValue;

    private String productServiceCode; //

    private String productServiceValueTh; //

    private String productServiceValueEn;

    private Boolean ntbEligible;

    private String issueNameTtbTouchTh; //

    private String issueNameTtbTouchEn; //

    private String issueTh; //

    private String issueEn;

    private String issueLine2Th;

    private String issueLine2En;

    private Boolean autoCloseCaseAfterResolved;

    private Float sla; //

    private String severityCode; //

    private String severityValue;

    private String smsCodeNew; //

    private String smsCodeInProgress; //

    private String smsCodeResolved; //

    private String smsCodeCompleted;

    private String smsCodeResolution1; //

    private String smsCodeResolution2; //

    private String resolution1Code;

    private String resolution1Value;

    private String resolution2Code;

    private String resolution2Value;

    private String messageId;

    private String urlLink;

    private Boolean frRequired;

    private Boolean fcr; //

    private Boolean visibleOnTouch; //

    private String carouselServiceTabCode;

    private String carouselServiceTabValue;

    private String ptaSegmentCode;

    private String ptaSegmentValue;

    private String cancelReasonCode;

    private String cancelReasonValue;

    private String cancelReasonComment;

    private String changeServiceTypeMatrixReasonCode;

    private String changeServiceTypeMatrixReasonValue;

    private String changeServiceTypeMatrixReasonComment;

    private String previousReasonCode; //

    private String previousReasonValue; //

    private String previousReasonComment;

    private String rootCauseListCode; //

    private String rootCauseListValue; //

    private String rootCauseListComment; //

    private String resolutionListCode; //

    private String resolutionListValue; //

    private String resolutionListComment; //

    private String changeOwnerReason;

    private Float approvedAmount1; //

    private Float approvedAmount2;

    private Float approvedAmount3;

    private String branchName; //

    private String branchCode; //

    private String staffName; //

    private String staffId; //

    private String resolvedBy; //

    private String resolvedByEmployeeId; // from stg

    private String resolvedByTeam; //

    private String resolvedByTeamId; //

    private ZonedDateTime resolvedDate; //

    private String closedBy; //

    private String closedByEmployeeId; // from stg

    private String closedByTeam; //

    private String closedByTeamId; //

    private ZonedDateTime closedDate; //

    private ZonedDateTime closedStartDate; // from stg

    private String issue2En;

    private String previousCaseStatusCode;

    private Boolean isMigration;

    private String migrationLot;

    private List<CaseSlaHopModel> slaHop;

    private List<CaseDocumentReferenceModel> caseDocumentReferences;

    private List<CaseCommentModel> caseComments;

    private String createByEmployeeID; // for retrieve

    private String createByName; // from stg

    private String createByTeamName; // from stg

    private ZonedDateTime createdCloseDateTeam; // from stg

    private String modifiedByEmployeeID; // for retrieve

    private String responsibleBuOld;

    private String responsibleBu; // from stg

    private ZonedDateTime responsibleBuDatetime; // from stg

//    private String responsibleOwner; // from stg

    private String responsibleOwnerName; // from stg

    private String responsibleOwnerEmployeeID; // from stg

    private ZonedDateTime responsibleOwnerDatetime; // from stg

    private ZonedDateTime responsibleCloseDate; // from stg

    private ZonedDateTime ownerStartDate; // from stg

    private String ownerTeamName; // from stg

    private ZonedDateTime ownerTeamDatetime; // from stg

    private Boolean isSTMLocked = Boolean.FALSE;

    private Boolean isHopLocked = Boolean.FALSE;

    private Boolean isOwnerLocked = Boolean.FALSE;
}
