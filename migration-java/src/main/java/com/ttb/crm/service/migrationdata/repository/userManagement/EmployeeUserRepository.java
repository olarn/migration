package com.ttb.crm.service.migrationdata.repository.userManagement;

import com.ttb.crm.service.migrationdata.model.userManagement.EmployeeUserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmployeeUserRepository extends JpaRepository<EmployeeUserModel, UUID> {
    Optional<EmployeeUserModel> findByEmployeeId(String employeeId);
    Optional<EmployeeUserModel> findByEmployeeIdAndStatusCode(String employeeId, Integer statusCode);
    Optional<EmployeeUserModel> findByUserId(UUID userId);

    @Query(value = """
             SELECT DISTINCT team_id FROM (
                SELECT rt.team_id AS team_id FROM role_team rt JOIN "employee_user" su ON rt.role_id = (select su3.role_id from "employee_user" su3 where su3.employee_id = :employeeId)
                UNION
                SELECT ut.team_id AS team_id from user_team ut join "employee_user" su on ut.user_id = (select su3.user_id from "employee_user" su3 where su3.employee_id = :employeeId)
            ) AS queue_id_results
            """, nativeQuery = true)
    List<String> findTeamIdByEmployeeId(@Param("employeeId") String employeeId);
}
