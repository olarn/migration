package com.ttb.crm.service.migrationdata.service.preparecaesdataservice;


import com.ttb.crm.lib.crmssp_encrypt_decrypt_lib.AESGCMUtil;
import com.ttb.crm.lib.crmssp_encrypt_decrypt_lib.CrmEncryptKeyService;
import com.ttb.crm.service.migrationdata.bean.Secret;
import com.ttb.crm.service.migrationdata.config.redis.CachingGroup;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.function.Function;
@CachingGroup(value="encrypt-field")
@Service
@RequiredArgsConstructor
public class EncryptFieldService {

    private final CrmEncryptKeyService crmEncryptKeyService;

    public static byte[] hexToBase64(String hex) {
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length(); i += 2) {
            bytes[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return bytes;

    }

    @Cacheable(value = "encryptFieldCache", key = "#key")
    public Secret getKey(String key) {
        return Optional.of(crmEncryptKeyService.getEncryptionKey(key))
                .map(response -> new Secret(
                        AESGCMUtil.getSecretKeyByString(response.getEncryptFieldKey()),
                        hexToBase64(response.getEncryptFieldInitialVector()))
                )
                .orElseThrow();
    }

    public Function<String, String> encryptField(Secret key) {
        return value -> Optional.of(key)
                .map(secret -> AESGCMUtil.encrypt(value, secret.getKey(), secret.getIv()))
                .orElseThrow(() -> new IllegalArgumentException("Invalid key provided"));
    }
}
