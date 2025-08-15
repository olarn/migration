package com.ttb.crm.service.migrationdata.service;

import com.ttb.crm.service.migrationdata.model.userManagement.EmployeeUserModel;
import com.ttb.crm.service.migrationdata.repository.userManagement.EmployeeUserRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @InjectMocks
    private UserService userService;

    @Mock
    private EmployeeUserRepository employeeUserRepository;

    @Test
    void WhenFindUserByUserIdShouldReturnUser() {
        EmployeeUserModel user = new EmployeeUserModel();
        UUID userId = UUID.randomUUID();
        user.setUserId(userId);

        when(employeeUserRepository.findByUserId(Mockito.any(UUID.class)))
                .thenReturn(Optional.of(user));

        Optional<EmployeeUserModel> optionalUser = userService.fetchUserByUserId(userId);
        assertTrue(optionalUser.isPresent());
    }

    @Test
    void WhenFindUserByUserIdNotFoundShouldReturnEmptyUser() {
        EmployeeUserModel user = new EmployeeUserModel();
        UUID userId = UUID.randomUUID();
        user.setUserId(userId);

        when(employeeUserRepository.findByUserId(Mockito.any(UUID.class)))
                .thenReturn(Optional.empty());

        Optional<EmployeeUserModel> optionalUser = userService.fetchUserByUserId(userId);
        assertFalse(optionalUser.isPresent());
    }

    @Test
    void WhenFetchUserByEmployeeIdShouldReturnUser() {
        EmployeeUserModel user = new EmployeeUserModel();
        UUID userId = UUID.randomUUID();
        String employeeId = "EMP1234";
        user.setUserId(userId);

        when(employeeUserRepository.findByEmployeeIdAndStatusCode(Mockito.anyString(), Mockito.anyInt()))
                .thenReturn(Optional.of(user));

        Optional<EmployeeUserModel> optionalUser = userService.fetchUserByEmployeeId(employeeId);
        assertTrue(optionalUser.isPresent());
    }

    @Test
    void WhenFetchUserByEmployeeIdNotFoundShouldReturnEmptyUser() {
        EmployeeUserModel user = new EmployeeUserModel();
        UUID userId = UUID.randomUUID();
        String employeeId = "EMP1234";
        user.setUserId(userId);

        when(employeeUserRepository.findByEmployeeIdAndStatusCode(Mockito.anyString(), Mockito.anyInt()))
                .thenReturn(Optional.empty());

        Optional<EmployeeUserModel> optionalUser = userService.fetchUserByEmployeeId(employeeId);
        assertFalse(optionalUser.isPresent());
    }

}
