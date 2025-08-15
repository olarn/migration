package com.ttb.crm.service.migrationdata.helper;


import com.ttb.crm.service.migrationdata.model.secondary.StgSlaPerOwnerModel;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;

@Component
public class MockTempSla {

    public StgSlaPerOwnerModel mockTempStgSlaPerOwnerCompleted(
            String sfId,
            String ownerTeam,
            String name,
            String employeeId,
            String start,
            String end,
            String status
    ) {
        StgSlaPerOwnerModel model = new StgSlaPerOwnerModel();
        model.setCaseC(sfId);
        model.setOwnerTeamC(ownerTeam);
        model.setOwnerTeamNew(ownerTeam);
        model.setName(name);
        model.setEmployeeIdC(employeeId);
        model.setStartDateTimeC(start);
        model.setEndDateTimeC(end);
        model.setCaseStatusC(status);
        model.setRecordStatus("Success");
        model.setRecordRemark("Recipient_Bank__c : Success | Correct_Recipient_Bank__c : Success | Transaction_Type__c : Success | ATM_Bank_Owner__c : Success | Origin : Success | Priority : Success | Contact_Person_Channel__c : Success | Status : Success | Category__c : Success | Product_Category__c : Success | Resolution_List__c : Success | Root_Cause_List__c : Success | Return_Reason__c : Success | Current_Service_Template__c : Success | PTA_Segment__c : Success | ");
        model.setRecordLastDatetime(ZonedDateTime.now());
        return model;
    }

    public void mockTempStgSlaPerOwnerCompleted(List<StgSlaPerOwnerModel> tempSlaModel) {
        tempSlaModel.add(mockTempStgSlaPerOwnerCompleted(
                "500RC00000hs5UiSEJ", "Inbound Voice Team 2", "Thanakorn Chaiyasit", "55501", "2025-06-09 01:00:00", "2025-06-09 04:00:00", "In progress"
        ));
        tempSlaModel.add(mockTempStgSlaPerOwnerCompleted(
                "500RC00000hs5UiSEJ", "Inbound Voice Team 8", "Jakrapan Sangkharat", "55515", "2025-06-09 04:00:00", "2025-06-10 01:00:00", "Resolved"
        ));
        tempSlaModel.add(mockTempStgSlaPerOwnerCompleted(
                "500RC00000hs5UiSEJ", "Investment Line", "Sukjai Sandee", "99902", "2025-06-10 01:00:00", "2025-06-11 10:00:00", "Completed"
        ));
    }

    public void mockTempStgSlaPerOwnerCompletedInvalidEndDate(List<StgSlaPerOwnerModel> tempSlaModel) {
        tempSlaModel.add(mockTempStgSlaPerOwnerCompleted(
                "500RC00000hs5UiSEJ", "Inbound Voice Team 2", "Thanakorn Chaiyasit", "55501", "2025-06-09 01:00:00", "2025-06-09 04:00:00", "In progress"
        ));
        tempSlaModel.add(mockTempStgSlaPerOwnerCompleted(
                "500RC00000hs5UiSEJ", "Inbound Voice Team 8", "Jakrapan Sangkharat", "55515", "2025-06-09 04:00:00", "2025-06-10 01:00:00", "Resolved"
        ));
        tempSlaModel.add(mockTempStgSlaPerOwnerCompleted(
                "500RC00000hs5UiSEJ", "Investment Line", "Sukjai Sandee", "99902", "2025-06-10 01:00:00", "", "Completed"
        ));
    }

    public void mockTempStgSlaPerOwnerCompletedInvalidStartDate(List<StgSlaPerOwnerModel> tempSlaModel) {
        tempSlaModel.add(mockTempStgSlaPerOwnerCompleted(
                "500RC00000hs5UiSEJ", "Inbound Voice Team 2", "Thanakorn Chaiyasit", "55501", "2025-06-09 01:00:00", "2025-06-09 04:00:00", "In progress"
        ));
        tempSlaModel.add(mockTempStgSlaPerOwnerCompleted(
                "500RC00000hs5UiSEJ", "Inbound Voice Team 8", "Jakrapan Sangkharat", "55515", "2025-06-09 04:00:00", "2025-06-10 01:00:00", "Resolved"
        ));
        tempSlaModel.add(mockTempStgSlaPerOwnerCompleted(
                "500RC00000hs5UiSEJ", "Investment Line", "Sukjai Sandee", "99902", "", "2025-06-10 01:00:00", "Completed"
        ));
    }

    public void mockTempStgSlaPerOwnerCompletedInvalidTeam(List<StgSlaPerOwnerModel> tempSlaModel) {
        tempSlaModel.add(mockTempStgSlaPerOwnerCompleted(
                "500RC00000hs5UiSEJ", "Inbound Voice Team 2", "Thanakorn Chaiyasit", "55501", "2025-06-09 01:00:00", "2025-06-09 04:00:00", "In progress"
        ));
        tempSlaModel.add(mockTempStgSlaPerOwnerCompleted(
                "500RC00000hs5UiSEJ", "", "Jakrapan Sangkharat", "55515", "2025-06-09 04:00:00", "2025-06-10 01:00:00", "Resolved"
        ));
        tempSlaModel.add(mockTempStgSlaPerOwnerCompleted(
                "500RC00000hs5UiSEJ", "Investment Line", "Sukjai Sandee", "99902", "2025-06-10 01:00:00", "2025-06-10 01:00:00", "Completed"
        ));
    }

    public void mockTempStgSlaPerOwnerCompletedBu1NotHoveOwner(List<StgSlaPerOwnerModel> tempSlaModel) {
        tempSlaModel.add(mockTempStgSlaPerOwnerCompleted(
                "500RC00000hs5UiSEJ", "Inbound Voice Team 2", "", "", "2025-06-09 01:00:00", "2025-06-09 04:00:00", "Resolved"
        ));
        tempSlaModel.add(mockTempStgSlaPerOwnerCompleted(
                "500RC00000hs5UiSEJ", "Inbound Voice Team 8", "Jakrapan Sangkharat", "55515", "2025-06-09 04:00:00", "2025-06-10 01:00:00", "Resolved"
        ));
        tempSlaModel.add(mockTempStgSlaPerOwnerCompleted(
                "500RC00000hs5UiSEJ", "Investment Line", "Sukjai Sandee", "99902", "2025-06-10 01:00:00", "2025-06-11 10:00:00", "Completed"
        ));
    }

    public void mockTempStgSlaPerOwnerCompletedWithOutResolved(List<StgSlaPerOwnerModel> tempSlaModel) {
        tempSlaModel.add(mockTempStgSlaPerOwnerCompleted(
                "500RC00000hs5UiSEJ", "Inbound Voice Team 8", "Jakrapan Sangkharat", "55515", "2025-06-09 04:00:00", "2025-06-11 10:00:00", "Completed"
        ));
    }

    public void mockTempStgSlaPerOwnerResolvedNextHistoryIsClosedByBuAndHaveOwner(List<StgSlaPerOwnerModel> tempSlaModel) {
        tempSlaModel.add(mockTempStgSlaPerOwnerCompleted(
                "500RC00000hs5UiSEJ", "Inbound Voice Team 2", "Thanakorn Chaiyasit", "55501", "2025-06-09 01:00:00", "2025-06-09 04:00:00", "In progress"
        ));
        tempSlaModel.add(mockTempStgSlaPerOwnerCompleted(
                "500RC00000hs5UiSEJ", "Inbound Voice Team 8", "Jakrapan Sangkharat", "55515", "2025-06-09 04:00:00", "2025-06-10 01:00:00", "Resolved"
        ));
        tempSlaModel.add(mockTempStgSlaPerOwnerCompleted(
                "500RC00000hs5UiSEJ", "Investment Line", "Sukjai Sandee", "99902", "2025-06-10 01:00:00", "", ""
        ));
    }

    public void mockTempStgSlaPerOwnerResolvedNextHistoryIsClosedByBuAndNotHaveOwner(List<StgSlaPerOwnerModel> tempSlaModel) {
        tempSlaModel.add(mockTempStgSlaPerOwnerCompleted(
                "500RC00000hs5UiSEJ", "Inbound Voice Team 2", "Thanakorn Chaiyasit", "55501", "2025-06-09 01:00:00", "2025-06-09 04:00:00", "In progress"
        ));
        tempSlaModel.add(mockTempStgSlaPerOwnerCompleted(
                "500RC00000hs5UiSEJ", "Inbound Voice Team 8", "Jakrapan Sangkharat", "55515", "2025-06-09 04:00:00", "2025-06-10 01:00:00", "Resolved"
        ));
        tempSlaModel.add(mockTempStgSlaPerOwnerCompleted(
                "500RC00000hs5UiSEJ", "Investment Line", "", "", "2025-06-10 01:00:00", "", ""
        ));
    }

    public void mockTempStgSlaPerOwnerResolvedNextHistoryNotIsClosedByBu(List<StgSlaPerOwnerModel> tempSlaModel) {
        tempSlaModel.add(mockTempStgSlaPerOwnerCompleted(
                "500RC00000hs5UiSEJ", "Inbound Voice Team 2", "Thanakorn Chaiyasit", "55501", "2025-06-09 01:00:00", "2025-06-09 04:00:00", "Resolved"
        ));
        tempSlaModel.add(mockTempStgSlaPerOwnerCompleted(
                "500RC00000hs5UiSEJ", "Inbound Voice Team 8", "Jakrapan Sangkharat", "55515", "2025-06-09 04:00:00", "2025-06-10 01:00:00", "Resolved"
        ));
        tempSlaModel.add(mockTempStgSlaPerOwnerCompleted(
                "500RC00000hs5UiSEJ", "Investment Line 2", "Sukjai Sandee", "99902", "2025-06-10 01:00:00", "", ""
        ));
    }

    public void mockTempStgSlaPerOwnerResolvedInvalidStartDate(List<StgSlaPerOwnerModel> tempSlaModel) {
        tempSlaModel.add(mockTempStgSlaPerOwnerCompleted(
                "500RC00000hs5UiSEJ", "Inbound Voice Team 2", "Thanakorn Chaiyasit", "55501", "2025-06-09 01:00:00", "2025-06-09 04:00:00", "Resolved"
        ));
        tempSlaModel.add(mockTempStgSlaPerOwnerCompleted(
                "500RC00000hs5UiSEJ", "Inbound Voice Team 8", "Jakrapan Sangkharat", "55515", "2025-06-09 04:00:00", "2025-06-10 01:00:00", "Resolved"
        ));
        tempSlaModel.add(mockTempStgSlaPerOwnerCompleted(
                "500RC00000hs5UiSEJ", "Investment Line 2", "Sukjai Sandee", "99902", "", "2025-06-10 01:00:00", ""
        ));
    }

    public void mockTempStgSlaPerOwnerResolvedBu1NotHoveOwner(List<StgSlaPerOwnerModel> tempSlaModel) {
        tempSlaModel.add(mockTempStgSlaPerOwnerCompleted(
                "500RC00000hs5UiSEJ", "Inbound Voice Team 2", "", "", "2025-06-09 01:00:00", "2025-06-09 04:00:00", "Resolved"
        ));
        tempSlaModel.add(mockTempStgSlaPerOwnerCompleted(
                "500RC00000hs5UiSEJ", "Inbound Voice Team 8", "Jakrapan Sangkharat", "55515", "2025-06-09 04:00:00", "2025-06-10 01:00:00", "Resolved"
        ));
        tempSlaModel.add(mockTempStgSlaPerOwnerCompleted(
                "500RC00000hs5UiSEJ", "Investment Line", "Sukjai Sandee", "99902", "2025-06-10 01:00:00", "2025-06-11 10:00:00", "Completed"
        ));
    }
}
