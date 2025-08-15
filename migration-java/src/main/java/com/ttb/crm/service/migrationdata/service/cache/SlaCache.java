package com.ttb.crm.service.migrationdata.service.cache;

import com.ttb.crm.service.migrationdata.model.masterManagement.HolidayModel;
import com.ttb.crm.service.migrationdata.repository.masterManagement.HolidayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class SlaCache {
    private final HolidayRepository holidayRepository;

    @Cacheable(value = "getHolidays")
    public List<HolidayModel> getHolidays() {
        return holidayRepository.findByStatusCode(0).orElseThrow();
    }
}
