package com.ttb.crm.service.migrationdata.service;

import com.ttb.crm.service.migrationdata.model.userManagement.TeamReadonlyModel;
import com.ttb.crm.service.migrationdata.repository.userManagement.TeamReadonlyRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TeamServiceTest {
    @InjectMocks
    private TeamService teamService;

    @Mock
    private TeamReadonlyRepository teamReadonlyRepository;

    @Test
    void WhenFetchByNameEnShouldReturnTeam() {
        TeamReadonlyModel teamReadonlyModel = new TeamReadonlyModel();
        teamReadonlyModel.setNameEn("team");

        when(teamReadonlyRepository.findByNameEn(Mockito.anyString()))
                .thenReturn(Optional.of(teamReadonlyModel));

        TeamReadonlyModel result = teamService.fetchByNameEn("team");

        assertNotNull(result);
        assertEquals("team", result.getNameEn());
    }

    @Test
    void WhenFetchByNameEnNotHaveTeamNotShouldReturnError() {
        when(teamReadonlyRepository.findByNameEn(Mockito.anyString()))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            teamService.fetchByNameEn("test");
        });

        assertEquals("Team test not found", exception.getMessage());
    }
}
