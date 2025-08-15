package com.ttb.crm.service.migrationdata.service;

import com.ttb.crm.lib.crmssp_encrypt_decrypt_lib.AESGCMUtil;
import com.ttb.crm.lib.crmssp_encrypt_decrypt_lib.CrmEncryptKeyService;
import com.ttb.crm.lib.crmssp_encrypt_decrypt_lib.bean.RetrieveEncryptKeyResponse;
import com.ttb.crm.service.migrationdata.bean.response.CaseCaptureEventDTO;
import com.ttb.crm.service.migrationdata.bean.response.MovementDataDto;
import com.ttb.crm.service.migrationdata.model.caseManagement.CaseTransactionModel;
import org.apache.kafka.test.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CaseMovementDataServiceTest {
    @Mock
    private KafkaProducerService kafkaProducerService;

    @Mock
    private CrmEncryptKeyService crmEncryptKeyService;

    @InjectMocks
    private CaseMovementDataService caseMovementDataService;

    @Captor
    private ArgumentCaptor<String> payloadCaptor;

    @BeforeEach
    void setup() {
        caseMovementDataService = new CaseMovementDataService(kafkaProducerService, crmEncryptKeyService);
        ReflectionTestUtils.setField(caseMovementDataService, "topicName", "test-topic");
    }

    @Test
    void pushAllCaseToEventHub_ShouldNotPublish_WhenCaseNumberNotMatch() {
        // Arrange
        CaseTransactionModel caseTx = new CaseTransactionModel();
        caseTx.setCaseNumber("C999");

        Map<String, CaseCaptureEventDTO> items = Map.of("C123", new CaseCaptureEventDTO());

        // Act
        caseMovementDataService.pushAllCaseToEventHub(List.of(caseTx), items);

        // Assert
        verify(kafkaProducerService, never()).sendMessageWithAsync(any(), any());
    }

    @Test
    void encryptPayload_ShouldReturnEncryptedString() throws Exception {
        // Arrange
        MovementDataDto mockData = new MovementDataDto();
        mockData.setEventName("testValue");

        RetrieveEncryptKeyResponse mockKeyResponse = new RetrieveEncryptKeyResponse();
        mockKeyResponse.setEncryptKey("mockKeyString");

        SecretKey mockSecretKey = Mockito.mock(SecretKey.class);

        try (MockedStatic<AESGCMUtil> utilities = Mockito.mockStatic(AESGCMUtil.class)) {
            // Mock service method
            Mockito.when(crmEncryptKeyService.getEncryptionKey("TEP"))
                    .thenReturn(mockKeyResponse);

            // Mock static method
            utilities.when(() -> AESGCMUtil.getSecretKeyByString("mockKeyString"))
                    .thenReturn(mockSecretKey);

            byte[] fakeIv = "fakeInitVector".getBytes();
            utilities.when(AESGCMUtil::generateRandomInitialVector)
                    .thenReturn(fakeIv);

            utilities.when(() -> AESGCMUtil.encrypt(Mockito.anyString(), Mockito.eq(mockSecretKey), Mockito.eq(fakeIv)))
                    .thenReturn("encryptedValue");

            utilities.when(() -> AESGCMUtil.bytesToString(fakeIv))
                    .thenReturn("fakeIvString");

            // Act
            String encryptedResult = ReflectionTestUtils.invokeMethod(
                    caseMovementDataService, "encryptPayload", mockData);

            // Assert
            assertNotNull(encryptedResult);
            Assertions.assertTrue(encryptedResult.contains("encryptedValue"));
            Assertions.assertTrue(encryptedResult.contains("fakeIvString"));
        }
    }
}
