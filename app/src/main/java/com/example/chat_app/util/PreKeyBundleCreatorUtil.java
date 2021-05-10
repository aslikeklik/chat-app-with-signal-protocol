package com.example.chat_app.util;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.chat_app.model.PreKeyBundleMaker;

import org.whispersystems.libsignal.IdentityKey;
import org.whispersystems.libsignal.ecc.Curve;
import org.whispersystems.libsignal.ecc.ECPublicKey;
import org.whispersystems.libsignal.state.PreKeyBundle;

import java.util.Base64;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PreKeyBundleCreatorUtil {
    @SneakyThrows
    @RequiresApi(api=Build.VERSION_CODES.O)
    public PreKeyBundle createPreKeyBundle(PreKeyBundleMaker preKeyBundleMaker){
        byte [] decodedPreKeyPublic=Base64.getDecoder().decode(preKeyBundleMaker.getPreKeyPublic());
        ECPublicKey preKeyPublic=Curve.decodePoint(decodedPreKeyPublic,0);

        byte [] decodedSignedPreKey=Base64.getDecoder().decode(preKeyBundleMaker.getSignedPreKeyPublic());
        ECPublicKey signedPreKey=Curve.decodePoint(decodedSignedPreKey,0);

        byte [] decodedSignedPreKeySignature= Base64.getDecoder().decode(preKeyBundleMaker.getIdentityPreKeySignature());


        byte [] decodedIdentityKeysPublicKey= Base64.getDecoder().decode(preKeyBundleMaker.getIdentityKey());
        ECPublicKey identityPublicKey=Curve.decodePoint(decodedIdentityKeysPublicKey,0);
        IdentityKey identityKey=new IdentityKey(identityPublicKey);






        return new PreKeyBundle(preKeyBundleMaker.getRegistrationId()
                ,preKeyBundleMaker.getDeviceId()
        ,preKeyBundleMaker.getPreKeyId()
        ,preKeyPublic
        ,preKeyBundleMaker.getSignedPreKeyId()
        ,signedPreKey
        ,decodedSignedPreKeySignature
        ,identityKey);

                /*
                  public PreKeyBundle(int registrationId, int deviceId, int preKeyId, ECPublicKey preKeyPublic,
                      int signedPreKeyId, ECPublicKey signedPreKeyPublic, byte[] signedPreKeySignature,
                      IdentityKey identityKey)
                 */
    }
}
