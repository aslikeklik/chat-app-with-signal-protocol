package com.example.chat_app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    private ListView listView;
    private FirebaseAuth fAuth;
    private ArrayList<String> subjectLists = new ArrayList<>();
    private FirebaseDatabase db;
    private DatabaseReference dbRef,msgDbRef;
    private ArrayAdapter<String> adapter;
    String receiverUid;

    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        fAuth = FirebaseAuth.getInstance();

        listView = (ListView) findViewById(R.id.listViewSubjects);

        db = FirebaseDatabase.getInstance();
        dbRef = db.getReference("Users");
        msgDbRef=FirebaseDatabase.getInstance().getReference("Messages");

        adapter = new ArrayAdapter<String>(HomeActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, subjectLists);
        listView.setAdapter(adapter);

        dbRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String value=snapshot.getValue(String.class).toString();
                if(!value.equals(fAuth.getCurrentUser().getEmail().toString())) {
                    subjectLists.add(value);
                    adapter.notifyDataSetChanged();

                }
                }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String currentUserUid=fAuth.getCurrentUser().getUid();
                String receiverEmail=subjectLists.get(position);
                Intent intent=new Intent(HomeActivity.this,ChatActivity.class);
                dbRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                            if (dataSnapshot.getValue().equals(receiverEmail)){
                                receiverUid=dataSnapshot.getKey();
                                intent.putExtra("RECEIVER_UID",receiverUid);
                                startActivity(intent);
                            }
                        }
                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                intent.putExtra("RECEIVER_EMAIL",receiverEmail);


            }
        });


    }

}
