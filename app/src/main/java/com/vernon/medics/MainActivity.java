package com.vernon.medics;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity{
    private Button mdoctor, mpatient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mdoctor = (Button) findViewById(R.id.doctor);
        mpatient = (Button) findViewById(R.id.patient);

        mdoctor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent login = new Intent(MainActivity.this, doctorloginactivity.class);
                startActivity(login);
                finish();
                return;
            }
        });
        mpatient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent login = new Intent(MainActivity.this, patientloginactivity.class);
                startActivity(login);
                finish();
                return;
            }
        });


    }
}
