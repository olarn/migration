package com.ttb.crm.service.migrationdata.service;

import com.ttb.crm.service.migrationdata.bean.TempLogData;
import com.ttb.crm.service.migrationdata.model.batch.TempStgCaseDocumentReferenceLogModel;
import com.ttb.crm.service.migrationdata.model.batch.TempStgCaseInProgressLogModel;
import com.ttb.crm.service.migrationdata.model.batch.TempStgSlaPerOwnerLogModel;
import com.ttb.crm.service.migrationdata.repository.batch.TempStgCaseDocumentReferenceLogRepository;
import com.ttb.crm.service.migrationdata.repository.batch.TempStgCaseInProgressRepository;
import com.ttb.crm.service.migrationdata.repository.batch.TempStgSlaPerOwnerLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CleanTempLogService {
    private final TempStgCaseInProgressRepository tempStgCaseInProgressRepository;
    private final TempStgSlaPerOwnerLogRepository tempStgSlaPerOwnerLogRepository;
    private final TempStgCaseDocumentReferenceLogRepository tempStgCaseDocumentReferenceLogRepository;

    public void deleteAll(List<? extends TempLogData> items) throws Exception {
        // ดึง TempStgCaseInProgressLogModel ทั้งหมดจาก items
        List<TempStgCaseInProgressLogModel> caseLogs = items.stream()
                .map(TempLogData::getTempStgCaseInProgressLogModel)
                .toList();

        // ดึง TempStgSlaPerOwnerLogModel ทั้งหมดจาก items (flatten)
        List<TempStgSlaPerOwnerLogModel> slaLogs = items.stream()
                .flatMap(item -> item.getTempStgSlaPerOwnerLogModel().stream())
                .toList();

        List<TempStgCaseDocumentReferenceLogModel> documentLogList = items.stream()
                .flatMap(item -> item.getTempStgCaseDocumentReferenceLogModel().stream())
                .toList();

        // ลบข้อมูล
        tempStgCaseInProgressRepository.deleteAll(caseLogs);
        tempStgSlaPerOwnerLogRepository.deleteAll(slaLogs);
        tempStgCaseDocumentReferenceLogRepository.deleteAll(documentLogList);
    }
}
