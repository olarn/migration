public class CaseTransactionMapper {

    public CreateCaseDTO map(StgCaseInProgressModel temp) {
        CreateCaseDTO caseTransaction = new CreateCaseDTO();
        setCaseInfo(caseTransaction, temp);
        setMasterData(caseTransaction, temp);
        setTeamAndUserInfo(caseTransaction, temp);
        setResolvedInfo(caseTransaction, temp);
        setClosedByInfo(caseTransaction, temp);
        setSmsCode(caseTransaction, temp);
        setProductInfo(caseTransaction, temp);
        setContactPerson(caseTransaction, temp);

        return caseTransaction;
    }

    private void setCaseInfo(CreateCaseDTO dto, StgCaseInProgressModel temp) {
        dto.setCaseNumber(Utils.truncate(temp.getCaseNumber(), Utils.getMaxLength(CaseTransactionModel.class, "caseNumber")));
        dto.setCaseStatusCode(Utils.truncate(temp.getStatusCode(), Utils.getMaxLength(CaseTransactionModel.class, "caseStatusCode")));
        dto.setCaseStatusValue(Utils.truncate(temp.getStatusValue(), Utils.getMaxLength(CaseTransactionModel.class, "caseStatusValue")));
        dto.setParentCaseNumber(Utils.truncate(temp.getParentCaseNumber(), Utils.getMaxLength(CaseTransactionModel.class, "parentCaseNumber")));
        dto.setIssueTh(Utils.truncate(temp.getCaseIssueC(), Utils.getMaxLength(CaseTransactionModel.class, "issueTh")));
        dto.setSuffix1(Utils.truncate(temp.getSuffix1C(), Utils.getMaxLength(CaseTransactionModel.class, "suffix1")));
        dto.setSuffix2(Utils.truncate(temp.getSuffix2C(), Utils.getMaxLength(CaseTransactionModel.class, "suffix2")));
        dto.setSuffix3(Utils.truncate(temp.getSuffix3C(), Utils.getMaxLength(CaseTransactionModel.class, "suffix3")));
        dto.setFundCode1(Utils.truncate(temp.getFundCode1C(), Utils.getMaxLength(CaseTransactionModel.class, "fundCode1")));
        dto.setFundCode2(Utils.truncate(temp.getFundCode2C(), Utils.getMaxLength(CaseTransactionModel.class, "fundCode2")));
        dto.setFundCode3(Utils.truncate(temp.getFundCode3C(), Utils.getMaxLength(CaseTransactionModel.class, "fundCode3")));
        dto.setIntegrationSystem(Utils.truncate(temp.getIntegrationSystem(), Utils.getMaxLength(CaseTransactionModel.class, "integrationSystem"))); // new can truncate
        dto.setParticipantId(Utils.truncate(temp.getParticipantIdC(), Utils.getMaxLength(CaseTransactionModel.class, "participantId")));
        dto.setConversationId(Utils.truncate(temp.getCallLogIdC(), Utils.getMaxLength(CaseTransactionModel.class, "conversationId")));
        dto.setStaffId(Utils.truncate(temp.getStaffIdREmployeeIdC(), Utils.getMaxLength(CaseTransactionModel.class, "staffId")));
        dto.setSlaStatus(Utils.truncate(temp.getSlaStatus(), Utils.getMaxLength(CaseTransactionModel.class, "slaStatus")));
        dto.setModifiedByEmployeeID(Utils.truncate(temp.getLastModifiedByEmployeeIdC(), Utils.getMaxLength(CaseTransactionModel.class, "modifiedById")));
        dto.setPreviousReasonCode(Utils.truncate(temp.getReturnReasonCode(), Utils.getMaxLength(CaseTransactionModel.class, "previousReasonCode")));
        dto.setPreviousReasonValue(Utils.truncate(temp.getReturnReasonValue(), Utils.getMaxLength(CaseTransactionModel.class, "previousReasonValue")));
        dto.setCustomerName(Utils.truncate(temp.getAccountName(), Utils.getMaxLength(CaseTransactionModel.class, "customerName")));
        dto.setRmId(Utils.truncate(temp.getTmbCustomerIdPeC(), Utils.getMaxLength(CaseTransactionModel.class, "rmId")));
        dto.setStaffName(Utils.truncate(temp.getStaffIdC(), Utils.getMaxLength(CaseTransactionModel.class, "staffName")));
        dto.setExternalId(Utils.truncate(temp.getSfId(), Utils.getMaxLength(CaseTransactionModel.class, "externalId")));

        if (temp.getCommitDateC() != null && !temp.getCommitDateC().isEmpty()) {
            dto.setSlaTargetDate(parseToZoneDateTime(temp.getCommitDateC()));
        }

        dto.setIsMigration(Optional.ofNullable(temp.getIsMigration()).orElse(false));
        dto.setMigrationLot(Utils.truncate(temp.getMigrationLot(), Utils.getMaxLength(CaseTransactionModel.class, "migrationLot")));


        if (temp.getOwnerStartDatetimeC() != null && !temp.getOwnerStartDatetimeC().isEmpty()) {
            dto.setAssignOwnerDate(parseToZoneDateTime(temp.getOwnerStartDatetimeC()));
        }

        if (temp.getOwnerTeamDatetimeC() != null && !temp.getOwnerTeamDatetimeC().isEmpty()) {
            dto.setStartTeamDate(parseToZoneDateTime(temp.getOwnerTeamDatetimeC()));
        }

        if (temp.getLastModifiedDate() != null && !temp.getLastModifiedDate().isEmpty()) {
            dto.setModifiedOn(parseToZoneDateTime(temp.getLastModifiedDate()));
        }
    }

    private void setMasterData(CreateCaseDTO dto, StgCaseInProgressModel temp) {
        dto.setFcr("true".equalsIgnoreCase(
                Optional.ofNullable(temp.getFcrC()).orElse("false")
        ));
        dto.setServiceTypeMatrixCode(Utils.truncate(temp.getServiceTypeMatrixCodeNew(), Utils.getMaxLength(CaseTransactionModel.class, "serviceTypeMatrixCode")));
        dto.setServiceTypeMatrixCodeOld(Utils.truncate(temp.getServiceTypeMatrixCodeC(), Utils.getMaxLength(CaseTransactionModel.class, "serviceTypeMatrixCode")));
        dto.setToExt2(Utils.truncate(temp.getToExt2C(), Utils.getMaxLength(CaseTransactionModel.class, "toExt2")));

        if (temp.getTransactionDateC() != null && !temp.getTransactionDateC().isEmpty()) {
            dto.setTransactionDate(parseToZoneDateTime(temp.getTransactionDateC()));
        }

        dto.setBillerProviderName(Utils.truncate(temp.getBillerProviderNameC(), Utils.getMaxLength(CaseTransactionModel.class, "billerProviderName")));
        dto.setRef1(Utils.truncate(temp.getRef1C(), Utils.getMaxLength(CaseTransactionModel.class, "ref1")));
        dto.setRef2(Utils.truncate(temp.getRef2C(), Utils.getMaxLength(CaseTransactionModel.class, "ref2")));
        dto.setRecipientBankCode(Utils.truncate(temp.getRecipientBankCode(), Utils.getMaxLength(CaseTransactionModel.class, "recipientBankCode")));
        dto.setRecipientBankValue(Utils.truncate(temp.getRecipientBankValue(), Utils.getMaxLength(CaseTransactionModel.class, "recipientBankValue")));
        dto.setInterBankRecipientAccountNo(Utils.truncate(temp.getInterBankRecipientAccountNoC(), Utils.getMaxLength(CaseTransactionModel.class, "interBankRecipientAccountNo")));
        dto.setWrongTransferAccount(Utils.truncate(temp.getWrongTransferAccountC(), Utils.getMaxLength(CaseTransactionModel.class, "wrongTransferAccount")));
        dto.setPromptPay(Utils.truncate(temp.getPromptPayC(), Utils.getMaxLength(CaseTransactionModel.class, "promptPay")));
        dto.setWrongPromptPay(Utils.truncate(temp.getWrongPromptPay(), Utils.getMaxLength(CaseTransactionModel.class, "wrongPromptPay")));
        dto.setReceiverName(Utils.truncate(temp.getReceiverNameC(), Utils.getMaxLength(CaseTransactionModel.class, "receiverName")));
        dto.setTransactionTypeCode(Utils.truncate(temp.getTransactionTypeCode(), Utils.getMaxLength(CaseTransactionModel.class, "transactionTypeCode")));
        dto.setTransactionTypeValue(Utils.truncate(temp.getTransactionTypeValue(), Utils.getMaxLength(CaseTransactionModel.class, "transactionTypeValue")));

        dto.setBranchAtmShopLocationTransaction(Utils.truncate(temp.getBranchAtmShopLocationTransactionC(), Utils.getMaxLength(CaseTransactionModel.class, "branchAtmShopLocationTransaction")));
        dto.setAtmBankOwnerCode(Utils.truncate(temp.getAtmBankOwnerCode(), Utils.getMaxLength(CaseTransactionModel.class, "atmBankOwnerCode")));
        dto.setAtmBankOwnerValue(Utils.truncate(temp.getAtmBankOwnerValue(), Utils.getMaxLength(CaseTransactionModel.class, "atmBankOwnerValue")));

        dto.setDescription(Utils.truncate(temp.getDescriptionC(), Utils.getMaxLength(CaseTransactionModel.class, "description")));

        dto.setOriginalProblemChannelCode(Utils.truncate(temp.getOriginalProblemChannelCode(), Utils.getMaxLength(CaseTransactionModel.class, "originalProblemChannelCode")));
        dto.setOriginalProblemChannelValue(Utils.truncate(temp.getOriginalProblemChannelValue(), Utils.getMaxLength(CaseTransactionModel.class, "originalProblemChannelValue")));
        dto.setDataSourceCode(Utils.truncate(temp.getDataSourceCode(), Utils.getMaxLength(CaseTransactionModel.class, "dataSourceCode")));
        dto.setDataSourceValue(Utils.truncate(temp.getDataSourceValue(), Utils.getMaxLength(CaseTransactionModel.class, "dataSourceValue")));

        dto.setPriorityCode(Utils.truncate(temp.getPriorityCode(), Utils.getMaxLength(CaseTransactionModel.class, "priorityCode")));
        dto.setPriorityValue(Utils.truncate(temp.getPriorityValue(), Utils.getMaxLength(CaseTransactionModel.class, "priorityValue")));
        dto.setBranchCode(Utils.truncate(temp.getBranchCodeC(), Utils.getMaxLength(CaseTransactionModel.class, "branchCode")));
        dto.setBranchName(Utils.truncate(temp.getBranchNameC(), Utils.getMaxLength(CaseTransactionModel.class, "branchName")));

        dto.setCallNumber(Utils.truncate(temp.getCallNumberC(), Utils.getMaxLength(CaseTransactionModel.class, "callNumber")));

        dto.setSla(
                (temp.getSlaDayC() != null && isNumeric(temp.getSlaDayC()))
                        ? Float.parseFloat(temp.getSlaDayC())
                        : 0F
        );

        dto.setVisibleOnTouch("true".equalsIgnoreCase(
                Optional.ofNullable(temp.getDisplayOnOneAppC()).orElse("false")
        ));

        dto.setServiceCategoryCode(Utils.truncate(temp.getCategoryCode(), Utils.getMaxLength(CaseTransactionModel.class, "serviceCategoryCode")));
        dto.setServiceCategoryValue(Utils.truncate(temp.getCategoryValue(), Utils.getMaxLength(CaseTransactionModel.class, "serviceCategoryValue")));

        dto.setServiceTemplateCode(Utils.truncate(temp.getCurrentServiceTemplateCode(), Utils.getMaxLength(CaseTransactionModel.class, "serviceTemplateCode")));
        dto.setServiceTemplateValue(Utils.truncate(temp.getCurrentServiceTemplateValue(), Utils.getMaxLength(CaseTransactionModel.class, "serviceTemplateValue")));
        dto.setPtaSegmentCode(Utils.truncate(temp.getPtaSegmentCode(), Utils.getMaxLength(CaseTransactionModel.class, "ptaSegmentCode")));
        dto.setPtaSegmentValue(Utils.truncate(temp.getPtaSegmentValue(), Utils.getMaxLength(CaseTransactionModel.class, "ptaSegmentValue")));

        dto.setAddress(Utils.truncate(temp.getAddressC(), Utils.getMaxLength(CaseTransactionModel.class, "address")));
        dto.setDocumentId(Utils.truncate(temp.getDocumentIdC(), Utils.getMaxLength(CaseTransactionModel.class, "documentId")));
        dto.setDocumentType(Utils.truncate(temp.getDocumentTypeC(), Utils.getMaxLength(CaseTransactionModel.class, "documentType")));
        dto.setEmsTracking(Utils.truncate(temp.getEmsTrackingNoC(), Utils.getMaxLength(CaseTransactionModel.class, "emsTracking")));
        dto.setObjectId(Utils.truncate(temp.getObjectIdC(), Utils.getMaxLength(CaseTransactionModel.class, "objectId")));
        dto.setRepositoryId(Utils.truncate(temp.getRepositoryIdC(), Utils.getMaxLength(CaseTransactionModel.class, "repositoryId")));
        dto.setResolutionListCode(Utils.truncate(temp.getResolutionListCode(), Utils.getMaxLength(CaseTransactionModel.class, "resolutionListCode")));
        dto.setResolutionListValue(Utils.truncate(temp.getResolutionListValue(), Utils.getMaxLength(CaseTransactionModel.class, "resolutionListValue")));

        setAmountInfo(dto, temp);
        setIssueInfo(dto, temp);
        setSeverityInfo(dto, temp);
        setCorrectInfo(dto, temp);
    }

    private void setAmountInfo(CreateCaseDTO dto, StgCaseInProgressModel temp) {
        setDepositInfo(dto, temp);
        setTransferAmountInfo(dto, temp);
        if (temp.getAmountDepositWithdrawalC() != null && isNumeric(temp.getAmountDepositWithdrawalC())) {
            dto.setAmountDepositWithdrawal(Float.parseFloat(temp.getAmountDepositWithdrawalC()));
        }
        if (temp.getApprovedAmountC() != null && isNumeric(temp.getApprovedAmountC())) {
            dto.setAmountWithdrawalDeposit(Float.parseFloat(temp.getApprovedAmountC())); // This might be a typo; usually ApprovedAmount goes to approvedAmount1/2/3, not AmountWithdrawalDeposit
        }
    }

    private void setDepositInfo(CreateCaseDTO dto, StgCaseInProgressModel temp) {
        if (temp.getAmountWithdrawalDepositC() != null && isNumeric(temp.getAmountWithdrawalDepositC())) {
            dto.setAmountWithdrawalDeposit(Float.parseFloat(temp.getAmountWithdrawalDepositC()));
        }

        if (temp.getAmountReceivedDepositToAccountC() != null && isNumeric(temp.getAmountReceivedDepositToAccountC())) {
            dto.setAmountReceivedDepositToAccount(Float.parseFloat(temp.getAmountReceivedDepositToAccountC()));
        }
    }

    private void setTransferAmountInfo(CreateCaseDTO dto, StgCaseInProgressModel temp) {
        if (temp.getFundTransferBillPaymentAmountC() != null && isNumeric(temp.getFundTransferBillPaymentAmountC())) {
            dto.setFundTransferBillPaymentAmount(Float.parseFloat(temp.getFundTransferBillPaymentAmountC()));
        }

        if (temp.getTransferAmountC() != null && isNumeric(temp.getTransferAmountC())) {
            dto.setTransferAmount(Float.parseFloat(temp.getTransferAmountC()));
        }
    }

    private void setIssueInfo(CreateCaseDTO dto, StgCaseInProgressModel temp) {
        dto.setIssueNameTtbTouchTh(Utils.truncate(temp.getIssueNewForOneappC(), Utils.getMaxLength(CaseTransactionModel.class, "issueNameTtbTouchTh")));
        dto.setIssueNameTtbTouchEn(Utils.truncate(temp.getIssueNewForOneappEnC(), Utils.getMaxLength(CaseTransactionModel.class, "issueNameTtbTouchEn")));
    }

    private void setSeverityInfo(CreateCaseDTO dto, StgCaseInProgressModel temp) {
        dto.setSeverityCode(Utils.truncate(temp.getCaseSeverityCode(), Utils.getMaxLength(CaseTransactionModel.class, "severityCode")));
        dto.setSeverityValue(Utils.truncate(temp.getCaseSeverityValue(), Utils.getMaxLength(CaseTransactionModel.class, "severityValue")));
    }

    private void setCorrectInfo(CreateCaseDTO dto, StgCaseInProgressModel temp) {
        dto.setCorrectRecipientBankCode(Utils.truncate(temp.getCorrectRecipientBankCode(), Utils.getMaxLength(CaseTransactionModel.class, "correctRecipientBankCode")));
        dto.setCorrectRecipientBankValue(Utils.truncate(temp.getCorrectRecipientBankValue(), Utils.getMaxLength(CaseTransactionModel.class, "correctRecipientBankValue")));
        dto.setCorrectBankRecipientAccountNo(Utils.truncate(temp.getCorrectBankRecipientAccountNoC(), Utils.getMaxLength(CaseTransactionModel.class, "correctBankRecipientAccountNo")));
        dto.setCorrectTargetAccount(Utils.truncate(temp.getCorrectTargetAccountC(), Utils.getMaxLength(CaseTransactionModel.class, "correctTargetAccount")));
    }

    private void setTeamAndUserInfo(CreateCaseDTO dto, StgCaseInProgressModel temp) {
        dto.setTeamName(Utils.truncate(temp.getOwnerTeamNew(), Utils.getMaxLength(CaseTransactionModel.class, "teamName")));
        setCreatedInfo(dto, temp);
        setResponsibleBUInfo(dto, temp);
        setOwnerInfo(dto, temp);
    }

    private void setCreatedInfo(CreateCaseDTO dto, StgCaseInProgressModel temp) {
        dto.setCreateByTeamName(temp.getCreatedByTeamNew());
        dto.setCreateByEmployeeID(Utils.truncate(temp.getCreatedByEmployeeIdC(), Utils.getMaxLength(CaseTransactionModel.class, "createdById")));
        dto.setCreateByName(temp.getCreatedNameC());

        if (temp.getCreatedCloseDateTeam() != null && !temp.getCreatedCloseDateTeam().isEmpty()) {
            dto.setCreatedCloseDateTeam(parseToZoneDateTime(temp.getCreatedCloseDateTeam()));
        }

        if (temp.getCreatedDate() != null && !temp.getCreatedDate().isEmpty()) {
            dto.setCreatedOn(parseToZoneDateTime(temp.getCreatedDate()));
        }
    }

    private void setResponsibleBUInfo(CreateCaseDTO dto, StgCaseInProgressModel temp) {
        dto.setResponsibleBuOld(temp.getResponsibleBuC());
        dto.setResponsibleBu(temp.getResponsibleBuNew());
        dto.setResponsibleOwnerName(temp.getResponsibleOwnerName());
        if (temp.getResponsibleBuDatetimeC() != null && !temp.getResponsibleBuDatetimeC().isEmpty()) {
            dto.setResponsibleBuDatetime(parseToZoneDateTime(temp.getResponsibleBuDatetimeC()));
        }

        dto.setResponsibleOwnerEmployeeID(temp.getResponsibleOwnerC());
        if (temp.getResponsibleOwnerDatetimeC() != null && !temp.getResponsibleOwnerDatetimeC().isEmpty()) {
            dto.setResponsibleOwnerDatetime(parseToZoneDateTime(temp.getResponsibleOwnerDatetimeC()));
        }

        if (temp.getResponsibleCloseDate() != null && !temp.getResponsibleCloseDate().isEmpty()) {
            dto.setResponsibleCloseDate(parseToZoneDateTime(temp.getResponsibleCloseDate()));
        }
    }

    private void setOwnerInfo(CreateCaseDTO dto, StgCaseInProgressModel temp) {
        dto.setOwnerTeamName(Utils.truncate(temp.getOwnerTeamNew(), Utils.getMaxLength(CaseTransactionModel.class, "teamName")));
        dto.setOwnerEmployeeId(Utils.truncate(temp.getOwnerEmployeeIdC(), Utils.getMaxLength(CaseTransactionModel.class, "ownerId")));
        dto.setOwnerName(Utils.truncate(temp.getOwnerNameC(), Utils.getMaxLength(CaseTransactionModel.class, "ownerName")));
    }

    private void setResolvedInfo(CreateCaseDTO dto, StgCaseInProgressModel temp) {
        dto.setResolvedBy(Utils.truncate(temp.getResolvedName(), Utils.getMaxLength(CaseTransactionModel.class, "resolvedBy")));
        dto.setResolvedByEmployeeId(temp.getResolvedEmployeeId());
        dto.setResolvedByTeam(temp.getResolvedTeamNew());
        if (temp.getResolvedDateTimeC() != null && !temp.getResolvedDateTimeC().isEmpty()) {
            dto.setResolvedDate(parseToZoneDateTime(temp.getResolvedDateTimeC()));
        }

        if (Constant.OTHER_RESOLUTION_CODE.equals(temp.getResolutionListCode()) || Constant.OTHER_RESOLUTION_VALUE.equals(temp.getResolutionListValue())) {
            dto.setResolutionListComment(Utils.truncate(StringUtils.isBlank(temp.getResolutionC()) ? "-" : temp.getResolutionC(), Utils.getMaxLength(CaseTransactionModel.class, "resolutionListComment")));
        } else {
            dto.setResolutionListComment(null);
        }
        dto.setRootCauseListCode(Utils.truncate(temp.getRootCauseListCode(), Utils.getMaxLength(CaseTransactionModel.class, "rootCauseListCode")));
        dto.setRootCauseListValue(Utils.truncate(temp.getRootCauseListValue(), Utils.getMaxLength(CaseTransactionModel.class, "rootCauseListValue")));
        if (Constant.OTHER_ROOT_CAUSE_CODE.equals(temp.getRootCauseListCode()) || Constant.OTHER_ROOT_CAUSE_VALUE.equals(temp.getRootCauseListValue())) {
            dto.setRootCauseListComment(Utils.truncate(StringUtils.isBlank(temp.getRootCauseC()) ? "-" : temp.getRootCauseListC(), Utils.getMaxLength(CaseTransactionModel.class, "rootCauseListComment")));
        } else {
            dto.setRootCauseListComment(null);
        }
    }

    private void setClosedByInfo(CreateCaseDTO dto, StgCaseInProgressModel temp) {
        dto.setClosedBy(Utils.truncate(temp.getClosedNameC(), Utils.getMaxLength(CaseTransactionModel.class, "closedBy")));
        dto.setClosedByEmployeeId(temp.getClosedEmployeeId());
        dto.setClosedByTeam(Utils.truncate(temp.getClosedByBuNew(), Utils.getMaxLength(CaseTransactionModel.class, "closedByTeam")));

        if (temp.getClosedStartDatetime() != null && !temp.getClosedStartDatetime().isEmpty()) {
            dto.setClosedStartDate(parseToZoneDateTime(temp.getClosedStartDatetime()));
        }

        if (temp.getClosedDate() != null && !temp.getClosedDate().isEmpty()) {
            dto.setClosedDate(parseToZoneDateTime(temp.getClosedDate()));
        }
    }

    private void setSmsCode(CreateCaseDTO dto, StgCaseInProgressModel temp) {
        dto.setSmsCodeInProgress(Utils.truncate(temp.getSmsCodeInProgressC(), Utils.getMaxLength(CaseTransactionModel.class, "smsCodeInProgress")));
        dto.setSmsCodeNew(Utils.truncate(temp.getSmsCodeNewC(), Utils.getMaxLength(CaseTransactionModel.class, "smsCodeNew")));
        dto.setSmsCodeResolution1(Utils.truncate(temp.getSmsCodeResolution1C(), Utils.getMaxLength(CaseTransactionModel.class, "smsCodeResolution1")));
        dto.setSmsCodeResolution2(Utils.truncate(temp.getSmsCodeResolution2C(), Utils.getMaxLength(CaseTransactionModel.class, "smsCodeResolution2")));
        dto.setSmsCodeResolved(Utils.truncate(temp.getSmsCodeResolvedC(), Utils.getMaxLength(CaseTransactionModel.class, "smsCodeResolved")));
    }

    private void setProductInfo(CreateCaseDTO dto, StgCaseInProgressModel temp) {
        dto.setProductTypeCode1(Utils.truncate(temp.getProductType1Code(), Utils.getMaxLength(CaseTransactionModel.class, "productTypeCode1")));
        dto.setProductTypeValue1(Utils.truncate(temp.getProductType1Value(), Utils.getMaxLength(CaseTransactionModel.class, "productTypeValue1")));
        dto.setProductNumberMarking1(Utils.truncate(temp.getProductNumber1C(), Utils.getMaxLength(CaseTransactionModel.class, "productNumberMarking1")));
        dto.setProductNumberFull1(Utils.truncate(temp.getProductNumberFull1C(), Utils.getMaxLength(CaseTransactionModel.class, "productNumberFull1")));
        dto.setProductTypeCode2(Utils.truncate(temp.getProductType2Code(), Utils.getMaxLength(CaseTransactionModel.class, "productTypeCode2")));
        dto.setProductTypeValue2(Utils.truncate(temp.getProductType2Value(), Utils.getMaxLength(CaseTransactionModel.class, "productTypeValue2")));
        dto.setProductNumberMarking2(Utils.truncate(temp.getProductNumber2C(), Utils.getMaxLength(CaseTransactionModel.class, "productNumberMarking2")));
        dto.setProductNumberFull2(Utils.truncate(temp.getProductNumberFull2C(), Utils.getMaxLength(CaseTransactionModel.class, "productNumberFull2")));
        dto.setProductTypeCode3(Utils.truncate(temp.getProductType3Code(), Utils.getMaxLength(CaseTransactionModel.class, "productTypeCode3")));
        dto.setProductTypeValue3(Utils.truncate(temp.getProductType3Value(), Utils.getMaxLength(CaseTransactionModel.class, "productTypeValue3")));
        dto.setProductNumberMarking3(Utils.truncate(temp.getProductNumber3C(), Utils.getMaxLength(CaseTransactionModel.class, "productNumberMarking3")));
        dto.setProductNumberFull3(Utils.truncate(temp.getProductNumberFull3C(), Utils.getMaxLength(CaseTransactionModel.class, "productNumberFull3")));
        dto.setProductServiceCode(Utils.truncate(temp.getProductCategoryCode(), Utils.getMaxLength(CaseTransactionModel.class, "productServiceCode")));
        dto.setProductServiceValueTh(Utils.truncate(temp.getProductCategoryValue(), Utils.getMaxLength(CaseTransactionModel.class, "productServiceValueTh")));

    }

    private void setContactPerson(CreateCaseDTO dto, StgCaseInProgressModel temp) {
        dto.setContactPersonName(Utils.truncate(temp.getContactPersonNameC(), Utils.getMaxLength(CaseTransactionModel.class, "contactPersonName")));
        dto.setContactPersonPhone(Utils.truncate(temp.getContactPersonPhoneC(), Utils.getMaxLength(CaseTransactionModel.class, "contactPersonPhone")));
        dto.setContactPersonPhone2(Utils.truncate(temp.getContactPersonPhone2C(), Utils.getMaxLength(CaseTransactionModel.class, "contactPersonPhone2")));
        dto.setContactPersonEmail(Utils.truncate(temp.getContactPersonEmailC(), Utils.getMaxLength(CaseTransactionModel.class, "contactPersonEmail")));
        dto.setContactPersonChannelCode(Utils.truncate(temp.getContactPersonChannelCode(), Utils.getMaxLength(CaseTransactionModel.class, "contactPersonChannelCode")));
        dto.setContactPersonChannelValue(Utils.truncate(temp.getContactPersonChannelValue(), Utils.getMaxLength(CaseTransactionModel.class, "contactPersonChannelValue")));
    }

    public static boolean isNumeric(String str) {
        if (str == null) return false;
        try {
            Float.parseFloat(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}