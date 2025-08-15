package com.ttb.crm.service.migrationdata.repository.masterManagement;

import com.ttb.crm.service.migrationdata.model.masterManagement.ServiceTypeMatrixModel;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface ServiceTypeMatrixRepository extends JpaRepository<ServiceTypeMatrixModel, UUID> {
    Optional<ServiceTypeMatrixModel> findByServiceTypeMatrixCodeAndStatusCode(String serviceTypeMatrixCode, Integer statusCode);

//    Optional<ServiceTypeMatrixModel> findByServiceTypeMatrixCode(String serviceTypeMatrixCode);

    @EntityGraph(attributePaths = "serviceTypeMatrixSlas")
    List<ServiceTypeMatrixModel> findAllByServiceTypeMatrixCodeInAndStatusCode(Set<String> serviceTypeMatrixCodes, Integer statusCode);

    ServiceTypeMatrixModel findByServiceTypeMatrixCode(String serviceTypeMatrixCode);
}
