package com.ttb.crm.service.migrationdata.repository.userManagement;

import com.ttb.crm.service.migrationdata.model.userManagement.TeamReadonlyModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeamReadonlyRepository extends JpaRepository<TeamReadonlyModel, UUID> {
    Optional<TeamReadonlyModel> findByNameEn(String nameEn);

    Optional<TeamReadonlyModel> findByNameTh(String nameTh);

    List<TeamReadonlyModel> findAllByTeamIdInAndStatusCode(List<UUID> teamId, int statusCode);
}
