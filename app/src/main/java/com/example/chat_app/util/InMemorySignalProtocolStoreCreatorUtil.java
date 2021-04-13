package com.example.chat_app.util;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.chat_app.model.StoreMaker;

import org.whispersystems.libsignal.IdentityKey;
import org.whispersystems.libsignal.IdentityKeyPair;
import org.whispersystems.libsignal.ecc.Curve;
import org.whispersystems.libsignal.ecc.ECPrivateKey;
import org.whispersystems.libsignal.ecc.ECPublicKey;
import org.whispersystems.libsignal.state.SignalProtocolStore;
import org.whispersystems.libsignal.state.impl.InMemorySignalProtocolStore;

import java.util.Base64;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class InMemorySignalProtocolStoreCreatorUtil {
    @SneakyThrows
    @RequiresApi(api=Build.VERSION_CODES.O)
    public SignalProtocolStore createStore(StoreMaker storeMaker){
        byte[] decodedIdentityKey=Base64.getDecoder().decode(storeMaker.getStoreIdentityKey());
        ECPublicKey publicKey=Curve.decodePoint(decodedIdentityKey,0);
        IdentityKey identityKey=new IdentityKey(publicKey);

        byte[] decodedPrivateKey=Base64.getDecoder().decode(storeMaker.getStorePrivateKey());
        ECPrivateKey ecPrivateKey=Curve.decodePrivatePoint(decodedPrivateKey);

        IdentityKeyPair identityKeyPair=new IdentityKeyPair(identityKey,ecPrivateKey);

        return new InMemorySignalProtocolStore(identityKeyPair,storeMaker.getRegistrationId());
    }
}
