package com.ttb.crm.service.migrationdata;

import com.ttb.crm.lib.crmssp_common_utils_lib.excel.ExportExcelServiceUtil;
import com.ttb.crm.lib.crmssp_common_utils_lib.notification.EmailServiceUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ComponentScan(
        basePackages = "com.ttb.crm",
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = ExportExcelServiceUtil.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = EmailServiceUtils.class)
        }
)
class BatchApplicationTest {
    @Mock
    private BatchApplication batchApplication;

    @Test
    void contextLoads() {
        assertTrue(true);
    }
}