package com.ttb.crm.service.migrationdata.service;

import com.ttb.crm.lib.crmssp_common_utils_lib.exception.NotFoundException;
import com.ttb.crm.service.migrationdata.config.redis.CachingGroup;
import com.ttb.crm.service.migrationdata.model.userManagement.EmployeeUserModel;
import com.ttb.crm.service.migrationdata.repository.userManagement.EmployeeUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@CachingGroup("user-data")
public class UserService {
    private final EmployeeUserRepository employeeUserRepository;

    @Cacheable(value = "employeeUserByEmployeeId", key = "#employeeId")
    public EmployeeUserModel retrieveUserByEmployeeIdIncludeInactive(String employeeId) {
        return employeeUserRepository.findByEmployeeId(employeeId).orElseThrow(() -> new NotFoundException("User " + employeeId + " not found"));
    }

    @Cacheable(value = "employeeUserByEmployeeIdWithActiveStatus", key = "#employeeId")
    public Optional<EmployeeUserModel> fetchUserByEmployeeId(String employeeId) {
        return employeeUserRepository.findByEmployeeIdAndStatusCode(employeeId, 0);
    }

    @Cacheable(value = "employeeUserByUserId", key = "#userId")
    public Optional<EmployeeUserModel> fetchUserByUserId(UUID userId) {
        return employeeUserRepository.findByUserId(userId);
    }

    @Cacheable(value = "teamIdListByEmployeeId", key = "#employeeId")
    public List<UUID> getTeamsByEmployeeId(String employeeId) {
        return Optional
                .of(employeeUserRepository.findTeamIdByEmployeeId(employeeId))
                .filter(list -> !list.isEmpty())
                .map(list -> list.stream().map(UUID::fromString).toList())
                .orElse(List.of());
    }

    public EmployeeUserModel getUserById(UUID userId) {
        return Optional.ofNullable(userId)
                .flatMap(this::fetchUserByUserId)
                .orElse(null);
    }
}
