package com.ttb.crm.service.migrationdata.helper;
import com.ttb.crm.service.migrationdata.model.userManagement.EmployeeUserModel;
import jakarta.persistence.Column;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;

@Slf4j
public class Utils {
    public static EmployeeUserModel prepareUser(EmployeeUserModel user, String fullName) {
        String[] result = splitFullName(fullName);
        user.setFirstNameTh(result[0]);
        user.setLastNameTh(result[1]);
        return user;
    }

    public static String[] splitFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return new String[]{"", ""};
        }

        String[] parts = fullName.trim().split("\\s+");
        String firstName = parts[0];
        String lastName = parts.length > 1 ? parts[parts.length - 1] : "";

        return new String[]{firstName, lastName};
    }

    public static String truncate(String input, int maxLength) {
        if (input == null) return null;
        return input.length() > maxLength ? input.substring(0, maxLength) : input;
    }

    public static int getMaxLength(Class<?> clazz, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            Column column = field.getAnnotation(Column.class);
            return column != null ? column.length() : Integer.MAX_VALUE;
        } catch (NoSuchFieldException e) {
            log.warn("Field '{}' not found in class '{}'. Returning Integer.MAX_VALUE.", fieldName, clazz.getSimpleName());
            return Integer.MAX_VALUE;
        }
    }
}