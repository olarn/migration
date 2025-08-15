package com.ttb.crm.service.migrationdata.repository.masterManagement;

import com.ttb.crm.service.migrationdata.model.masterManagement.HolidayModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HolidayRepository extends JpaRepository<HolidayModel, UUID> {
    Optional<List<HolidayModel>> findByStatusCode(int statusCode);
}
