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

import com.example.chat_app.model.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class ChatActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private EditText messageEditText;
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private ListView listView;
    private ArrayList<String> messageList=new ArrayList<>();
    private ArrayList<String> userList=new ArrayList<>();
    private ArrayAdapter<String> adapter;

    private byte encryptionKey[] = {9, 115, 51, 86, 105, 4, -31, -23, -68, 88, 17, 20, 3, -105, 119, -53};

    private Cipher cipher, decipher;
    private SecretKeySpec secretKeySpec;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messageEditText=findViewById(R.id.messageEditText);
        databaseReference= FirebaseDatabase.getInstance().getReference("Messages");
        mAuth=FirebaseAuth.getInstance();
        listView=findViewById(R.id.messageListView);

        String senderEmail=mAuth.getCurrentUser().getEmail();
        String senderUid=mAuth.getCurrentUser().getUid();
        SharedPreferences sharedPref = this.getSharedPreferences("sharedPref",Context.MODE_PRIVATE);
        // String privateKey= sharedPref.getString("123@123.com","yok falan");


        String receiverEmail=getIntent().getStringExtra("RECEIVER_EMAIL");
        String receiverUid=getIntent().getStringExtra("RECEIVER_UID");

        String sortUid=sortUid(receiverUid,senderUid);


        try {
            cipher = Cipher.getInstance("AES");
            decipher = Cipher.getInstance("AES");
            secretKeySpec = new SecretKeySpec(encryptionKey, "AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }



        adapter  = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,messageList){
            public View getView(int position, View convertView, ViewGroup parent){
                // Cast the current view as a TextView
                TextView tv = (TextView) super.getView(position,convertView,parent);

                if(userList.get(position).equals(receiverEmail))
                    tv.setGravity(Gravity.RIGHT);
                else tv.setGravity(Gravity.LEFT);

                return tv;
            }
        };


        FirebaseDatabase.getInstance().getReference("Messages").child(sortUid).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                userList.clear();
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    Message message=dataSnapshot.getValue(Message.class);
                    try {
                        messageList.add(AESDecryptionMethod(message.getMessage()));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    Log.i("user list",userList.toString());
                    //adapter = new ArrayAdapter<String>(ChatActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, messageList);
                    //     adapter.notifyDataSetChanged();
                    listView.setAdapter(adapter);
                    userList.add(message.getReceiver());

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void sendMessageButton(View view) {
        String receiverUid=getIntent().getStringExtra("RECEIVER_UID");
        String receiverEmail=getIntent().getStringExtra("RECEIVER_EMAIL");
        String messageText=AESEncryptionMethod(messageEditText.getText().toString());
        String sortedUid=sortUid(receiverUid,mAuth.getUid().toString());
        String timestamp=Long.toString(new Date().getTime()); //mesajın zamanı için

        Message message=Message.builder()
                .message(messageText)
                .receiver(receiverEmail)
                .sender(mAuth.getCurrentUser().getEmail())
                .msgTimeStamp(timestamp)
                .build();
        FirebaseDatabase.getInstance().getReference("Messages").child(sortedUid).child(timestamp).setValue(message);

    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    public String sortUid(String sender, String receiver){
        String result=sender+receiver;
        String sorted = result.chars()
                .sorted()
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        return sorted;
    }

    private String AESEncryptionMethod(String string){

        byte[] stringByte = string.getBytes();
        byte[] encryptedByte = new byte[stringByte.length];

        try {
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);//secret key spec is an array contains keys
            encryptedByte = cipher.doFinal(stringByte);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        String returnString = null;

        try {
            returnString = new String(encryptedByte, "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return returnString;
    }

    private String AESDecryptionMethod(String string) throws UnsupportedEncodingException {
        byte[] EncryptedByte = string.getBytes("ISO-8859-1");
        String decryptedString = string;

        byte[] decryption;

        try {
            decipher.init(cipher.DECRYPT_MODE, secretKeySpec);
            decryption = decipher.doFinal(EncryptedByte);
            decryptedString = new String(decryption);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return decryptedString;
    }

}