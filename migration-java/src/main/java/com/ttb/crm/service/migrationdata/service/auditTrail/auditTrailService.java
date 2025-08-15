package com.ttb.crm.service.migrationdata.service.auditTrail;

import com.ttb.crm.service.migrationdata.bean.response.CaseCaptureEventDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;

//@Service
//@AllArgsConstructor
//public class AuditTrailService {
//
//    public void publishLog(CaseCaptureEventDTO caseCaptureEventDTO, HttpServletRequest request) {
//        AuditTrailPayload payload = AuditTrailPayload.builder()
//                .action(caseCaptureEventDTO.getEventAction())
//                .actor(caseCaptureEventDTO.getEmployeeId())
//                .oldData(caseCaptureEventDTO.getBeforeChange())
//                .newData(caseCaptureEventDTO.getAfterChange())
//
//                .endpoint("/case-management%s".formatted(request.getHeader("")))
//                .ipAddress(request.getHeader(""))
//
//                .createdOn(ZonedDateTime.now())
//                .method("POST")
//                .serviceName("crmssp-case-management-service")
//                .build();
//
//        AuditTrailLogger.log(payload);
//    }
//}