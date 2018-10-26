package com.example.stampe;

        import android.app.Activity;
        import android.content.Context;
        import android.content.Intent;
        import android.location.LocationListener;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.util.Log;
        import android.view.View;
        import android.widget.Button;
        import android.widget.ImageButton;
        import android.widget.TextView;
        import android.widget.Toast;

        import com.google.firebase.auth.FirebaseAuth;
        import com.google.firebase.auth.FirebaseUser;
        import com.google.firebase.database.DataSnapshot;
        import com.google.firebase.database.DatabaseError;
        import com.google.firebase.database.DatabaseReference;
        import com.google.firebase.database.FirebaseDatabase;
        import com.google.firebase.database.ValueEventListener;

        import org.w3c.dom.Text;

        import java.io.File;
        import java.io.FileNotFoundException;
        import java.io.PrintStream;
        import java.sql.SQLOutput;
        import java.util.Map;
        import java.util.Scanner;

public class MainActivity extends Activity {

    private String secretText;
    private final int redeemCiteria = 10;
    public static int stamp;
    public static int benefit;

    Button btnScan;
    Button btnStamp;
    Button btnProfile;
    TextView greeting;

    String result;

    FirebaseAuth auth;
    private DatabaseReference mFirebaseDatabase;

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        System.out.println("created");

        auth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance().getReference("users");
        if(auth.getCurrentUser() == null) {
            System.out.println("current user is null");
            finish();
        }
        userId = auth.getCurrentUser().getUid();
        System.out.println("i dont want this sentence to be called");
        updateStamp();
        //System.out.println("MainActivity: uid = "+userId);



        greeting = (TextView) findViewById(R.id.greeting);
        getUsername();


        btnScan = (Button) findViewById(R.id.btnScan);
        btnStamp = (Button) findViewById(R.id.btnStamp);
        btnProfile = (Button) findViewById(R.id.btnProfile);


        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                    intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
                    setSecretText();
                    System.out.println("after set secret text is : "+secretText);
                    startActivityForResult(intent, 0);

                } catch (Exception e) {
                    Toast.makeText(getBaseContext(), "Please Install Barcode Scanner", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnStamp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("current stamp is "+stamp);
                Intent intent = new Intent(MainActivity.this,StampActivity.class);
                intent.putExtra("name", stamp);
                startActivity(intent);
            }
        });

        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            }
        });

    }

    @Override
    public synchronized void onActivityResult(int requestCode, int resultCode, Intent intent) {
        System.out.println("on activity result secret text is: " +secretText);
        if (requestCode==0) {
            System.out.println("on activity result");
            if(resultCode == RESULT_OK) {
                System.out.println("about to scan");
                result = intent.getStringExtra("SCAN_RESULT");
                System.out.println("result is : "+result);
                System.out.println("secret text is: "+secretText);
                System.out.println("checking...");
                if(result.equals(secretText)) {
                    System.out.println("matched");
                    Toast.makeText(getBaseContext(), "stamp added", Toast.LENGTH_SHORT).show();
                    System.out.println("updating stamp");
                    updateStamp();
                    if(stamp+1>=redeemCiteria) {
                        stamp = 0;
                        updateBenefit();
                        benefit++;
                        Toast.makeText(getBaseContext(), "Congratulation! You have earn a benefit!", Toast.LENGTH_SHORT).show();
                    } else stamp++;
                    mFirebaseDatabase.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("stamp").setValue(stamp);
                    mFirebaseDatabase.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("benefit").setValue(benefit);
                    System.out.println("result is : "+result);
                    Intent toStamp = new Intent(MainActivity.this, StampActivity.class);
                    toStamp.putExtra("stamp", stamp);
                    toStamp.putExtra("benefit", benefit);
                    startActivity(toStamp);
                } else {
                    System.out.println("not matched");
                    Toast.makeText(getBaseContext(), "QR not matched", Toast.LENGTH_SHORT).show();
                }
                result = "";
                secretText = "";
            }

        }
    }

    public void onMap(View view) {
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }

    private void updateStamp() {
        final String userKey = FirebaseAuth.getInstance().getCurrentUser().getUid();
        System.out.println("method updateStamp() : userKey: "+userKey);
        mFirebaseDatabase.child(userKey).child("stamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(LoginActivity.clearTemp==false) return;
                System.out.println("acquiring stamp");
                stamp = dataSnapshot.getValue(Integer.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void updateBenefit() {
        final String userKey = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mFirebaseDatabase.child(userKey).child("benefit").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                benefit = dataSnapshot.getValue(Integer.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getUsername() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userKey = user.getUid();
        System.out.println("method getUsername() : userKey: "+userKey);
        mFirebaseDatabase.child(userKey).child("id").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String u = dataSnapshot.getValue(String.class);
                setTextToGreeting(u);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setTextToGreeting(String s) {
        greeting.setText("Hi "+s+" !");
    }

    private void setTexttoSecretText(String s) {
        secretText = s;
    }

    private synchronized void setSecretText() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseDatabase.getInstance().getReference("generatedQRText").child("text").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String t = dataSnapshot.getValue(String.class);
                System.out.println("on setSecretText acquired text is: "+t);
                setTexttoSecretText(t);
                System.out.println("On setSecretText : "+secretText);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
