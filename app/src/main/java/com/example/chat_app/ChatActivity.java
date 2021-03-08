package com.example.chat_app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;

public class ChatActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private EditText messageEditText;
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private ListView listView;
    private ArrayList<String> messageList=new ArrayList<>();
    private ArrayList<String> userList=new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messageEditText=findViewById(R.id.messageEditText);
        databaseReference= FirebaseDatabase.getInstance().getReference("Messages");
        mAuth=FirebaseAuth.getInstance();
        listView=findViewById(R.id.messageListView);



        String receiverEmail=getIntent().getStringExtra("RECEIVER_EMAIL");
        String receiverUid=getIntent().getStringExtra("RECEIVER_UID");
        String senderUid=mAuth.getUid().toString();
        String sortUid=sortUid(receiverUid,senderUid);

        adapter  = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,messageList){
            public View getView(int position, View convertView, ViewGroup parent){
                // Cast the current view as a TextView
                TextView tv = (TextView) super.getView(position,convertView,parent);

                /*
                    setGravity
                        void setGravity (int gravity)

                        Sets the horizontal alignment of the text and the vertical gravity that
                        will be used when there is extra space in the TextView beyond what
                        is required for the text itself.
                */
                /*
                    Gravity
                        END
                            Push object to x-axis position at the end of its container, not changing its size.

                        RIGHT
                            Push object to the right of its container, not changing its size.

                        CENTER_VERTICAL
                            Place object in the vertical center of its container, not changing its size.
                */
                // Set the item text gravity to right/end and vertical center
               String currentUser= mAuth.getCurrentUser().getEmail();
                if(userList.get(position).equals(currentUser))
                    tv.setGravity(Gravity.RIGHT);
                else tv.setGravity(Gravity.LEFT);

                // Return the view
                return tv;
            }
        };


        FirebaseDatabase.getInstance().getReference("Messages").child(sortUid).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
              for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                  String a=dataSnapshot.getKey();
                  dataSnapshot.getValue(Message.class);
                    Message message=dataSnapshot.getValue(Message.class);
                    messageList.add(message.getMessage());
                    userList.add(message.getSender());
                  //adapter = new ArrayAdapter<String>(ChatActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, messageList);
                  adapter.notifyDataSetChanged();
                  listView.setAdapter(adapter);
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
        String messageText=messageEditText.getText().toString();
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

}