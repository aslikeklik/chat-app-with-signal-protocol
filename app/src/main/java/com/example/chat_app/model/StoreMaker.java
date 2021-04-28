package com.example.chat_app.model;

import com.google.firebase.database.IgnoreExtraProperties;

import org.whispersystems.libsignal.ecc.ECPublicKey;

import java.io.Serializable;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@IgnoreExtraProperties
public class StoreMaker implements Serializable {
    //InMemorySignalProtocolStore(IdentityKeyPair identityKeyPair, int registrationId)
    //IdentityKeyPair(IdentityKey publicKey, ECPrivateKey privateKey)
    //public IdentityKey(ECPublicKey publicKey)
    String storeIdentityKey;
    String storePrivateKey;
    int registrationId;
}
