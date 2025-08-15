package com.ttb.crm.service.migrationdata.service;

import com.ttb.crm.lib.crmssp_common_utils_lib.exception.NotFoundException;
import com.ttb.crm.service.migrationdata.model.userManagement.TeamReadonlyModel;
import com.ttb.crm.service.migrationdata.repository.userManagement.TeamReadonlyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeamService {
    private final TeamReadonlyRepository teamReadonlyRepository;

    @Cacheable(value = "teams", key = "#teamName")
    public TeamReadonlyModel retrieveTeamByName(String teamName) {
        return teamReadonlyRepository.findByNameTh(teamName)
                .orElseThrow(() -> new NotFoundException("Team " + teamName + " not found"));
    }

    @Cacheable(value = "teamsByNameEn", key = "#teamNameEn")
    public TeamReadonlyModel fetchByNameEn(String teamNameEn) {
        return teamReadonlyRepository.findByNameEn(teamNameEn)
                .orElseThrow(() -> new NotFoundException("Team " + teamNameEn + " not found"));
    }

    @Cacheable(value = "teamsByIds", key = "#teamIds.toString()")
    public List<TeamReadonlyModel> findAllTeamsByTeamId(List<UUID> teamIds) {
        List<TeamReadonlyModel> teamList = teamReadonlyRepository.findAllByTeamIdInAndStatusCode(teamIds, 0);
        return teamList;
    }

}
