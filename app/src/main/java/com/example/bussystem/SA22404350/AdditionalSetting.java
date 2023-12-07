package com.example.bussystem.SA22404350;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.bussystem.R;
import com.example.bussystem.SA22403810.BusDriverDetails;


public class AdditionalSetting extends AppCompatActivity {

    CardView deleteAcount,update;
    String type;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_additional_setting);

        deleteAcount = findViewById(R.id.deleteAccount);
        update = findViewById(R.id.update);

        Intent intent = getIntent();
        type = intent.getStringExtra("type");

        deleteAcount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), Confirmation_Dialog_Delete.class));
//                Animatoo.INSTANCE.animateFade(AdditionalSetting.this);
            }
        });
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(type.equals("Passenger")) {
                    Intent intent = new Intent(getApplicationContext(),NameChange.class);
                    intent.putExtra("Type","Passenger");
                    startActivity(intent);
                }
                else{
                    startActivity(new Intent(getApplicationContext(), BusDriverDetails.class));


                }
            }
        });
    }

}