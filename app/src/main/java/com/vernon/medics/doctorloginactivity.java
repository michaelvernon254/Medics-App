package com.vernon.medics;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class doctorloginactivity extends AppCompatActivity {
    private EditText memail, mpassword;
    private Button mlogin, mregistration;

    private FirebaseAuth mauth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    private DatabaseReference current_user_db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctorloginactivity);

        mauth = FirebaseAuth.getInstance();
        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user!=null){
                    Intent login = new Intent(doctorloginactivity.this, doctormapactivity.class);
                    startActivity(login);
                    finish();
                    return;
                }
            }
        };

        memail = (EditText) findViewById(R.id.email);
        mpassword = (EditText) findViewById(R.id.password);

        mlogin = (Button) findViewById(R.id.login);

        mlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = memail.getText().toString();
                final String password = mpassword.getText().toString();
                mauth.signInWithEmailAndPassword(email, password).addOnCompleteListener(doctorloginactivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()){
                            Toast.makeText(doctorloginactivity.this, "Sign up went wrong please try again.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        mregistration = findViewById(R.id.register);
        mregistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = memail.getText().toString();
                final String password = mpassword.getText().toString();
                mauth.createUserWithEmailAndPassword(email, password).addOnCompleteListener
                        (doctorloginactivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                long time = System.currentTimeMillis();
                                current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Doctors/"+time);
                                current_user_db.setValue(email,password).addOnCompleteListener
                                        ( new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (!task.isSuccessful()){
                                                    Toast.makeText(doctorloginactivity.this, "Something went wrong. Please try again later.", Toast.LENGTH_SHORT).show();
                                                }else {
                                                    Toast.makeText(doctorloginactivity.this, "Succeeded", Toast.LENGTH_SHORT).show();

                                                }
                                            }
                                        });
                            }
                        });
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        mauth.addAuthStateListener(firebaseAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mauth.removeAuthStateListener(firebaseAuthListener);
    }
}
