package com.ttb.crm.service.migrationdata.bean;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.crypto.SecretKey;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Secret {
    private SecretKey key;
    private byte[] iv;
}