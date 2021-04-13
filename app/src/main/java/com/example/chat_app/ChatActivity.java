package com.example.chat_app;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.chat_app.model.KeyPairsMaker;
import com.example.chat_app.model.Message;
import com.example.chat_app.model.PreKeyBundleMaker;
import com.example.chat_app.model.StoreMaker;
import com.example.chat_app.rsa.Session;
import com.example.chat_app.util.InMemorySignalProtocolStoreCreatorUtil;
import com.example.chat_app.util.PreKeyBundleCreatorUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.ecc.Curve;
import org.whispersystems.libsignal.ecc.ECKeyPair;
import org.whispersystems.libsignal.ecc.ECPrivateKey;
import org.whispersystems.libsignal.protocol.PreKeySignalMessage;
import org.whispersystems.libsignal.protocol.SignalMessage;
import org.whispersystems.libsignal.state.PreKeyBundle;
import org.whispersystems.libsignal.state.PreKeyRecord;
import org.whispersystems.libsignal.state.SignalProtocolStore;
import org.whispersystems.libsignal.state.SignedPreKeyRecord;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import lombok.SneakyThrows;

import static com.example.chat_app.rsa.RSAUtils.decrypt;
import static com.example.chat_app.rsa.RSAUtils.encrypt;

public class ChatActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private EditText messageEditText;
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private ListView listView;
    private ArrayList<String> messageList=new ArrayList<>();
    private ArrayList<String> userList=new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private static String receiverPrivateKey, receiverPublicKey, senderPrivateKey;
    private static Session aliceToBobSession, bobToAliceSession;
    private static PreKeyBundle bobPreKeyBundle,alicePreKeyBundle;
    private static SignalProtocolStore signalProtocolStore;
    private static  SignalProtocolAddress signalProtocolAddress;

    private Cipher cipher, decipher;
    private SecretKeySpec secretKeySpec;

    @RequiresApi(api=Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messageEditText=findViewById(R.id.messageEditText);
        databaseReference=FirebaseDatabase.getInstance().getReference("Messages");
        mAuth=FirebaseAuth.getInstance();
        listView=findViewById(R.id.messageListView);

        String senderEmail=mAuth.getCurrentUser().getEmail();
        String senderUid=mAuth.getCurrentUser().getUid();
        SharedPreferences sharedPref=this.getSharedPreferences("sharedPref",Context.MODE_PRIVATE);
        // String privateKey= sharedPref.getString("123@123.com","yok falan");


        String receiverEmail=getIntent().getStringExtra("RECEIVER_EMAIL");
        String receiverUid=getIntent().getStringExtra("RECEIVER_UID");

        //KEY DEFINING
        receiverPrivateKey=sharedPref.getString(receiverEmail,"");
        senderPrivateKey=sharedPref.getString(senderEmail,"");



        if(aliceToBobSession==null) {

            FirebaseDatabase.getInstance().getReference("Users").child(receiverUid).addValueEventListener(new ValueEventListener() {
                @RequiresApi(api=Build.VERSION_CODES.O)
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    receiverPublicKey=snapshot.child("publicKey").getValue(String.class);
                    PreKeyBundleMaker preKeyBundleMaker=snapshot.child("preKeyBundleMaker").getValue(PreKeyBundleMaker.class);
                    bobPreKeyBundle=PreKeyBundleCreatorUtil.createPreKeyBundle(preKeyBundleMaker);

                    Log.d("TAG","onDataChange: ");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            FirebaseDatabase.getInstance().getReference("Users").child(senderUid).addValueEventListener(new ValueEventListener() {
                @RequiresApi(api=Build.VERSION_CODES.O)
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    PreKeyBundleMaker preKeyBundleMaker=snapshot.child("preKeyBundleMaker").getValue(PreKeyBundleMaker.class);
                    alicePreKeyBundle=PreKeyBundleCreatorUtil.createPreKeyBundle(preKeyBundleMaker);

                    Log.d("TAG","onDataChange: ");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            FirebaseDatabase.getInstance().getReference("Users").child(senderUid).addValueEventListener(new ValueEventListener() {
                @RequiresApi(api=Build.VERSION_CODES.O)
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    StoreMaker storeMaker=snapshot.child("storeMaker").getValue(StoreMaker.class);
                    signalProtocolStore=InMemorySignalProtocolStoreCreatorUtil.createStore(storeMaker);
                    Log.d("TAG","onDataChange: ");
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            FirebaseDatabase.getInstance().getReference("privates").child(senderUid).addValueEventListener(new ValueEventListener() {
                @RequiresApi(api=Build.VERSION_CODES.O)
                @SneakyThrows
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    KeyPairsMaker keyPairsMaker=snapshot.getValue(KeyPairsMaker.class);
                    byte[] decodedPrivateKey=Base64.getDecoder().decode(keyPairsMaker.getPreKeyPairPrivateKey());
                    ECPrivateKey ecPrivateKey=Curve.decodePrivatePoint(decodedPrivateKey);
                    ECKeyPair ecKeyPair=new ECKeyPair(alicePreKeyBundle.getPreKey(),ecPrivateKey);
                    PreKeyRecord preKeyRecord=new PreKeyRecord(alicePreKeyBundle.getPreKeyId(),ecKeyPair);

                    byte[] decodedSignedPrivateKey=Base64.getDecoder().decode(keyPairsMaker.getSignedPreKeySignaturePrivateKey());
                    ECPrivateKey signedPrivateKey=Curve.decodePrivatePoint(decodedSignedPrivateKey);
                    ECKeyPair signedPreKeyPair=new ECKeyPair(alicePreKeyBundle.getSignedPreKey(),signedPrivateKey);

                    SignedPreKeyRecord signedPreKeyRecord=new SignedPreKeyRecord(
                            alicePreKeyBundle.getSignedPreKeyId(),keyPairsMaker.getTimestamp(),signedPreKeyPair,alicePreKeyBundle.getSignedPreKeySignature());

                    signalProtocolStore.storePreKey(alicePreKeyBundle.getPreKeyId(),preKeyRecord);
                    signalProtocolStore.storeSignedPreKey(alicePreKeyBundle.getSignedPreKeyId(),signedPreKeyRecord);

                    signalProtocolAddress=new SignalProtocolAddress(receiverUid,1);

                    aliceToBobSession=new Session(signalProtocolStore,bobPreKeyBundle,signalProtocolAddress);

                    Log.d("TAG","onDataChange: ");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }

            String sortUid=sortUid(receiverUid,senderUid);


            adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,messageList) {
                public View getView(int position,View convertView,ViewGroup parent) {
                    // Cast the current view as a TextView
                    TextView tv=(TextView) super.getView(position,convertView,parent);

                    if (userList.get(position).equals(receiverEmail))
                        tv.setGravity(Gravity.RIGHT);
                    else tv.setGravity(Gravity.LEFT);

                    return tv;
                }
            };



        FirebaseDatabase.getInstance().getReference("Messages").child(sortUid).addValueEventListener(new ValueEventListener() {

            ArrayList<String> previousCipherText=new ArrayList<>();

            @SneakyThrows
            @RequiresApi(api=Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                userList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Message message=dataSnapshot.getValue(Message.class);

                    String plainText=message.getMessage(); //Şifreli şu anda decryption etmemiz gerek.

                    if (message.getReceiver().equals(mAuth.getCurrentUser().getEmail())&& !message.getDecrypted() && !previousCipherText.contains(plainText)) {
                        byte[] ds=Base64.getDecoder().decode(plainText);
                        PreKeySignalMessage toBobMessageDecrypt = new PreKeySignalMessage(ds);
                        previousCipherText.add(plainText);
                        plainText=aliceToBobSession.decrypt(toBobMessageDecrypt);
                        message.setMessage(plainText);
                        message.setDecrypted(true);
                        FirebaseDatabase.getInstance().getReference("Messages").child(sortUid).child(message.getMsgTimeStamp()).setValue(message);

                    }


                 //   adapter = new ArrayAdapter<String>(ChatActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, messageList);
                  //  adapter.notifyDataSetChanged();
                    messageList.add(plainText);
                    listView.setAdapter(adapter);
                    userList.add(message.getReceiver());
                }

                }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    @RequiresApi(api=Build.VERSION_CODES.O)
    public void sendMessageButton(View view) throws IllegalBlockSizeException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException {
        String receiverUid=getIntent().getStringExtra("RECEIVER_UID");
        String receiverEmail=getIntent().getStringExtra("RECEIVER_EMAIL");

        String sortedUid=sortUid(receiverUid,mAuth.getUid().toString());
        String timestamp=Long.toString(new Date().getTime()); //mesajın zamanı için

        String messageText=messageEditText.getText().toString();
     //   String cipherMessage=Base64.getEncoder().encodeToString(encrypt(messageText,receiverPublicKey));
        PreKeySignalMessage toBobMessage = aliceToBobSession.encrypt(messageText);
        String signalCipherText=Base64.getEncoder().encodeToString(toBobMessage.serialize());


        Message message=Message.builder()
                .message(signalCipherText)
                .receiver(receiverEmail)
                .sender(mAuth.getCurrentUser().getEmail())
                .msgTimeStamp(timestamp)
                .decrypted(false)
                .build();
        FirebaseDatabase.getInstance().getReference("Messages").child(sortedUid).child(timestamp).setValue(message);

    }

    @RequiresApi(api=Build.VERSION_CODES.N)
    public String sortUid(String sender,String receiver) {
        String result=sender + receiver;
        String sorted=result.chars()
                .sorted()
                .collect(StringBuilder::new,StringBuilder::appendCodePoint,StringBuilder::append)
                .toString();
        return sorted;
    }


}