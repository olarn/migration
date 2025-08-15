package com.ttb.crm.service.migrationdata.helper;

import com.ttb.crm.service.migrationdata.model.masterManagement.HolidayModel;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

@Component
public class MockHoliday {

    public HolidayModel mockHoliday(String holidayName) {
        HolidayModel holidayModel = new HolidayModel();
        holidayModel.setHolidayName(holidayName)
                .setHolidayId(UUID.randomUUID())
                .setStatusCode(0)
                .setHolidayDate(LocalDate.now());
        return holidayModel;
    }
}
