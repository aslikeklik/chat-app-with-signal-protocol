package com.example.chat_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chat_app.adapters.CustomAdapter;
import com.example.chat_app.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    private ListView listView;
    private FirebaseAuth fAuth;
    private ArrayList<String> subjectLists = new ArrayList<>();
    ArrayList<String> userNameList=new ArrayList<>();
    ArrayList<String> onlineList=new ArrayList<>();
    private FirebaseDatabase db;
    private DatabaseReference dbRef;
    private ArrayAdapter<String> adapter;

    String receiverUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        fAuth = FirebaseAuth.getInstance();

        listView = (ListView) findViewById(R.id.listViewSubjects);

        db = FirebaseDatabase.getInstance();
        dbRef = db.getReference("Users");





        adapter=new CustomAdapter(HomeActivity.this,userNameList,onlineList);
        adapter.notifyDataSetChanged();
        listView.setAdapter(adapter);
        dbRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                User user=snapshot.getValue(User.class);

                String name=user.getName();
                String surname=user.getSurname();
                String value=user.getEmail();
                Boolean isOnline=user.getOnline();

                if(!value.equals(fAuth.getCurrentUser().getEmail())) {
                    subjectLists.add(value);
                    userNameList.add(name+" "+surname);
                    onlineList.add(isOnline.toString());
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

                dbRef.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot,@Nullable String previousChildName) {
                        for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                            if (dataSnapshot.getValue().equals(receiverEmail)){
                                receiverUid=snapshot.getKey();
                                intent.putExtra("RECEIVER_UID",receiverUid);
                                startActivity(intent);
                            }
                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot,@Nullable String previousChildName) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot,@Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                intent.putExtra("RECEIVER_EMAIL",receiverEmail);


            }
        });

        adapter.notifyDataSetChanged();
    }

     @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.exit:
                FirebaseDatabase.getInstance().getReference("Users").child(fAuth.getUid()).child("online").setValue(false);
                fAuth.signOut();
                startActivity(new Intent(this,MainActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
