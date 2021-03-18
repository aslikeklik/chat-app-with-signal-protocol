package com.example.chat_app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.io.UnsupportedEncodingException;
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
    private static String receiverPrivateKey,receiverPublicKey,senderPrivateKey;


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

        //KEY DEFINING
        receiverPrivateKey=sharedPref.getString(receiverEmail,"");
        senderPrivateKey=sharedPref.getString(senderEmail,"");



        FirebaseDatabase.getInstance().getReference("Users").child(receiverUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                receiverPublicKey=snapshot.child("publicKey").getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


            String sortUid=sortUid(receiverUid,senderUid);





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
                    messageList.add(message.getMessage());
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void sendMessageButton(View view) throws IllegalBlockSizeException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException {
        String receiverUid=getIntent().getStringExtra("RECEIVER_UID");
        String receiverEmail=getIntent().getStringExtra("RECEIVER_EMAIL");

        String sortedUid=sortUid(receiverUid,mAuth.getUid().toString());
        String timestamp=Long.toString(new Date().getTime()); //mesajın zamanı için

        String messageText=messageEditText.getText().toString();
        String cipherMessage = Base64.getEncoder().encodeToString(encrypt(messageText, receiverPublicKey));

        Message message=Message.builder()
                .message(cipherMessage)
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



}