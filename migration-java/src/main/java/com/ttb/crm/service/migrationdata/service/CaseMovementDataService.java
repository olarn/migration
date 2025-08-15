package com.ttb.crm.service.migrationdata.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ttb.crm.lib.crmssp_encrypt_decrypt_lib.AESGCMUtil;
import com.ttb.crm.lib.crmssp_encrypt_decrypt_lib.CrmEncryptKeyService;
import com.ttb.crm.lib.crmssp_encrypt_decrypt_lib.bean.RetrieveEncryptKeyResponse;
import com.ttb.crm.service.migrationdata.bean.MovementDataEncryptDto;
import com.ttb.crm.service.migrationdata.bean.response.CaseCaptureEventDTO;
import com.ttb.crm.service.migrationdata.bean.response.MovementDataDto;
import com.ttb.crm.service.migrationdata.model.caseManagement.CaseTransactionModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;


@Slf4j
@Service
@RequiredArgsConstructor
public class CaseMovementDataService {

    private final KafkaProducerService kafkaProducerService;
    private final CrmEncryptKeyService crmEncryptKeyService;

    @Value("${spring.eventhub.topic-case-movement.name}")
    private String topicName;

    @Async("batchTaskExecutor")
    public void pushAllCaseToEventHub(List<CaseTransactionModel> caseTransactionModels, Map<String, CaseCaptureEventDTO> items) {
        caseTransactionModels.forEach(publishToEventHub(items));
    }

    private Consumer<CaseTransactionModel> publishToEventHub(Map<String, CaseCaptureEventDTO> items) {
        return caseTransactionModel -> {
            CaseCaptureEventDTO matchItem = items.get(caseTransactionModel.getCaseNumber());
            if (matchItem != null) {
                matchItem.getAfterChange().setCaseId(caseTransactionModel.getCaseId());
                publishKafkaEventWithAsync(matchItem);
            }
        };
    }

    public void publishKafkaEventWithAsync(CaseCaptureEventDTO dto) {
        Optional.of(dto)
                .map(this::buildMovementData)
                .map(this::encryptPayload)
                .ifPresent(payload -> kafkaProducerService.sendMessageWithAsync(topicName, payload));
    }

    private MovementDataDto buildMovementData(CaseCaptureEventDTO dto) {
        MovementDataDto data = new MovementDataDto();
        BeanUtils.copyProperties(dto.getAfterChange(), data);

        data.setEventName(dto.getEventAction());

        return data;
    }

    private String encryptPayload(MovementDataDto data) {
        MovementDataEncryptDto movementDataEncryptDto = new MovementDataEncryptDto();

        ObjectMapper jsonMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        try {
            String payload = jsonMapper.writeValueAsString(data);

            RetrieveEncryptKeyResponse crmEncryptKey = crmEncryptKeyService.getEncryptionKey("TEP");
            SecretKey secretKey = AESGCMUtil.getSecretKeyByString(crmEncryptKey.getEncryptKey());
            byte[] initVector = AESGCMUtil.generateRandomInitialVector();

            Object encryptedData = AESGCMUtil.encrypt(payload, secretKey, initVector);

            movementDataEncryptDto.setEncryptedData(encryptedData.toString());
            movementDataEncryptDto.setInitialVector(AESGCMUtil.bytesToString(initVector));

            return movementDataEncryptDto.toString();

        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }
}
