package com.ttb.crm.service.migrationdata.helper;

import com.ttb.crm.service.migrationdata.model.userManagement.TeamReadonlyModel;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MockTeam {

    public TeamReadonlyModel mockTeam(
            String nameTh
    ) {
        TeamReadonlyModel teamModel = new TeamReadonlyModel();
        teamModel.setNameTh(nameTh);
        teamModel.setTeamId(UUID.randomUUID());
        teamModel.setStatusCode(0);
        return teamModel;
    }
}
