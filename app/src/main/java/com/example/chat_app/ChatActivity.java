package com.example.chat_app;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

public class ChatActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private EditText messageEditText;
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messageEditText=findViewById(R.id.messageEditText);
        databaseReference= FirebaseDatabase.getInstance().getReference("Messages");
        mAuth=FirebaseAuth.getInstance();
      //  Log.d("email",receiverEmail);

        String receiverEmail=getIntent().getStringExtra("RECEIVER_EMAIL");



    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void sendMessageButton(View view) {
        String receiverUid=getIntent().getStringExtra("RECEIVER_UID");
        String message=messageEditText.getText().toString();
        String sortedUid=sortUid(receiverUid,mAuth.getUid().toString());
        String timestamp=Long.toString(new Date().getTime()); //mesajın zamanı için
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