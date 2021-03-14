package com.example.chat_app;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private EditText editTextUserName;
    private EditText editTextUserPassword;
    private Button buttonLogin, buttonRegister;
    private TextView txtRegister;
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private String userName;
    private String userPassword;
    private DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextUserName=(EditText) findViewById(R.id.editTextUserName);
        editTextUserPassword=(EditText) findViewById(R.id.editTextUserPassword);
        buttonLogin=(Button) findViewById(R.id.btnLogin);
        buttonRegister=(Button) findViewById(R.id.btnRegister);
        databaseReference=FirebaseDatabase.getInstance().getReference("Users");

        BottomNavigationView bottomNav=findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        //I added this if statement to keep the selected fragment when rotating the device
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new GirisFragment()).commit();
        }


        mAuth=FirebaseAuth.getInstance();
        firebaseUser=mAuth.getCurrentUser(); // authenticate olan kullaniciyi aliyoruz eger var ise
        if (firebaseUser != null) {

        }


    }


    public void createUser(String username,String password) {
        mAuth.createUserWithEmailAndPassword(username,password)
                .addOnCompleteListener(this,new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG","createUserWithEmail:success");
                            //   FirebaseUser user = mAuth.getCurrentUser();
                            String email=mAuth.getCurrentUser().getEmail();
                            String uid=mAuth.getCurrentUser().getUid();
                            User user=User.builder()
                                    .email(email)
                                    .UID(uid)
                                    .build();
                            databaseReference=FirebaseDatabase.getInstance().getReference("Users");
                            databaseReference.child(uid).setValue(user);

                            startActivity(new Intent(MainActivity.this,HomeActivity.class));

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG","createUserWithEmail:failure",task.getException());
                            Toast.makeText(MainActivity.this,"Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }

                        // ...
                    }
                });
    }

    public void signUser(String username,String password) {
        mAuth.signInWithEmailAndPassword(username,password)
                .addOnCompleteListener(this,new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG","signUserWithEmail:success");
                            //   FirebaseUser user = mAuth.getCurrentUser();

                            startActivity(new Intent(MainActivity.this,HomeActivity.class));
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG","signUserWithEmail:failure",task.getException());
                            Toast.makeText(MainActivity.this,"Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }

                        // ...
                    }
                });
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener=new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment selectedFragment=null;
            switch (item.getItemId()) {
                case R.id.nav_giris:
                    selectedFragment=new GirisFragment();
                    break;
                case R.id.nav_kaydol:
                    selectedFragment=new KaydolFragment();
                    break;
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    selectedFragment).commit();
            return true;
        }
    };

    public void login(View view) {
        editTextUserName=findViewById(R.id.editTextUserName);
        editTextUserPassword=findViewById(R.id.editTextUserPassword);

        String userName=editTextUserName.getText().toString();
        String password=editTextUserPassword.getText().toString();
        signUser(userName,password);
    }

    public void signup(View view) {
        editTextUserName=findViewById(R.id.editTextUserName);
        editTextUserPassword=findViewById(R.id.editTextUserPassword);

        String userName=editTextUserName.getText().toString();
        String password=editTextUserPassword.getText().toString();
        createUser(userName,password);
    }
}



