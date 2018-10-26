package com.example.stampe;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

public class StampActivity extends AppCompatActivity {

    int benefit;
    int stamp;
    FirebaseAuth auth;
    TextView stampNum;
    TextView benefitNum;
    private DatabaseReference mFirebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stamp);

        auth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance().getReference("users");
        updateBenefit();
        updateStamp();

        System.out.println("setting stamp and benefit");
        System.out.println("stamp = "+stamp);
        System.out.println("benefit = "+benefit);

        stampNum = findViewById(R.id.stampNum);
        benefitNum = findViewById(R.id.benefitNum);
        Button redeem = (Button) findViewById(R.id.redeem);




        redeem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("this is your current benefit : "+ benefit);
                if(benefit >=1) {
                    benefit--;
                    Toast.makeText(getBaseContext(), "Your benefit redeemed!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getBaseContext(), "You got no benefit!", Toast.LENGTH_SHORT).show();
                }
                mFirebaseDatabase.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("benefit").setValue(benefit);
                MainActivity.stamp = stamp;
                MainActivity.benefit = benefit;
            }
        });

        //int benefit = intent.getIntExtra("benefit", 0);
        System.out.println("called on stamp activity " +stamp);



    }

    private synchronized void updateBenefit() {
        final String userKey = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mFirebaseDatabase.child(userKey).child("benefit").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                benefit = dataSnapshot.getValue(Integer.class);
                System.out.println("benefit is : "+benefit);
                benefitNum.setText(""+benefit);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private synchronized void updateStamp() {
        final String userKey = FirebaseAuth.getInstance().getCurrentUser().getUid();
        System.out.println("method updateStamp() : userKey: "+userKey);
        mFirebaseDatabase.child(userKey).child("stamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(LoginActivity.clearTemp==false) return;
                System.out.println("acquiring stamp");
                stamp = dataSnapshot.getValue(Integer.class);
                System.out.println("stamp is : "+stamp);
                stampNum.setText(""+stamp);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



}
