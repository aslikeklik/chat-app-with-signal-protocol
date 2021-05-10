package com.example.chat_app.rsa;


import android.database.sqlite.SQLiteDatabase;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.chat_app.model.KeyPairsMaker;
import com.example.chat_app.util.ByteConverter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import org.whispersystems.libsignal.IdentityKey;
import org.whispersystems.libsignal.IdentityKeyPair;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.ecc.Curve;
import org.whispersystems.libsignal.ecc.ECKeyPair;
import org.whispersystems.libsignal.ecc.ECPublicKey;
import org.whispersystems.libsignal.state.PreKeyBundle;
import org.whispersystems.libsignal.state.PreKeyRecord;
import org.whispersystems.libsignal.state.SignalProtocolStore;
import org.whispersystems.libsignal.state.SignedPreKeyRecord;
import org.whispersystems.libsignal.state.impl.InMemorySignalProtocolStore;
import org.whispersystems.libsignal.util.KeyHelper;

import java.io.IOException;
import java.util.Base64;
import java.util.Random;

import lombok.SneakyThrows;

public class Entity {
    private final SignalProtocolStore store;
    private final PreKeyBundle preKey;
    private final SignalProtocolAddress address;
    private SQLiteDatabase database;
    public static String keyPairMakerString;

    @SneakyThrows
    @RequiresApi(api=Build.VERSION_CODES.O)
    public Entity(int preKeyId,int signedPreKeyId,String address)
            throws InvalidKeyException
    {
        this.address = new SignalProtocolAddress(address, 1);
        this.store = new InMemorySignalProtocolStore(
                KeyHelper.generateIdentityKeyPair(),
                KeyHelper.generateRegistrationId(false));
        IdentityKeyPair identityKeyPair = store.getIdentityKeyPair();
        int registrationId = store.getLocalRegistrationId();

        ECKeyPair preKeyPair = Curve.generateKeyPair();
        ECKeyPair signedPreKeyPair = Curve.generateKeyPair();
        int deviceId =1;
        long timestamp = System.currentTimeMillis();

        byte[] signedPreKeySignature = Curve.calculateSignature(
                identityKeyPair.getPrivateKey(),
                signedPreKeyPair.getPublicKey().serialize());

        IdentityKey identityKey = identityKeyPair.getPublicKey();
        ECPublicKey preKeyPublic = preKeyPair.getPublicKey();
        String preKeyPairPrivateKey=Base64.getEncoder().encodeToString(preKeyPair.getPrivateKey().serialize());
        String signedPrivateKey=Base64.getEncoder().encodeToString(signedPreKeyPair.getPrivateKey().serialize());

        KeyPairsMaker keyPairsMaker=KeyPairsMaker.builder()
                .preKeyPairPrivateKey(preKeyPairPrivateKey)
                .signedPreKeySignaturePrivateKey(signedPrivateKey)
                .timestamp(timestamp).build();

        keyPairMakerString=Base64.getEncoder().encodeToString(ByteConverter.makeByteKeyPairs(keyPairsMaker));

       // FirebaseDatabase.getInstance().getReference("privates").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(keyPairsMaker);
        ECPublicKey signedPreKeyPublic = signedPreKeyPair.getPublicKey();

        this.preKey = new PreKeyBundle(
                registrationId,
                deviceId,
                preKeyId,
                preKeyPublic,
                signedPreKeyId,
                signedPreKeyPublic,
                signedPreKeySignature,
                identityKey);

        PreKeyRecord preKeyRecord = new PreKeyRecord(preKey.getPreKeyId(), preKeyPair);
        SignedPreKeyRecord signedPreKeyRecord = new SignedPreKeyRecord(
                signedPreKeyId, timestamp, signedPreKeyPair, signedPreKeySignature);

        store.storePreKey(preKeyId, preKeyRecord);
        store.storeSignedPreKey(signedPreKeyId, signedPreKeyRecord);
    }

    public SignalProtocolStore getStore() {
        return store;
    }

    public PreKeyBundle getPreKey() {
        return preKey;
    }

    public SignalProtocolAddress getAddress() {
        return address;
    }
}
