package com.project.restaurant.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.project.restaurant.FirebaseUtil;
import com.project.restaurant.R;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private EditText etHotelID, etRoom;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private ProgressDialog progressDialog,pd;
    private Button btnLogin,btnRegister;
    private TextView tvRoom,tvHotelId;
    private String hID,secureId;

    @Override
    @SuppressLint("HardwareIds")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if(!isConnected(LoginActivity.this))
        {
            if(!LoginActivity.this.isDestroyed() && !LoginActivity.this.isFinishing())
                builderDialog(LoginActivity.this).show();
        } else {
            //Assign views
            etHotelID   = findViewById(R.id.etHotelID);
            etRoom      = findViewById(R.id.etRoom);
            btnLogin    = findViewById(R.id.btnLogin);
            btnRegister = findViewById(R.id.btnRegister);
            tvHotelId   = findViewById(R.id.tvHotelId);
            tvRoom      = findViewById(R.id.tvRoom);

            btnLogin.setEnabled(false);
            btnLogin.setVisibility(View.VISIBLE);
            btnRegister.setEnabled(false);
            btnRegister.setVisibility(View.GONE);

            progressDialog=new ProgressDialog(this);
            pd=new ProgressDialog(this);
            pd.setMessage("Hang on...");
            progressDialog.setMessage("Hang on...");

            secureId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            FirebaseUser user = mAuth.getCurrentUser();

            if(user != null) {
                finish();
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
            }
            else {
                progressDialog.show();
                verifyTabDetails(secureId);
            }

            btnLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pd.show();
                    btnLogin.setEnabled(false);
                    validate();
                }
            });

            btnRegister.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(@NonNull View view) {
                    final String hotelId= etHotelID.getText().toString().trim();
                    if(hotelId.isEmpty())
                        Toast.makeText(view.getContext(),"Enter the Hotel Id",Toast.LENGTH_LONG).show();
                    else{
                        DatabaseReference ref= FirebaseUtil.getDatabase().getReference(hotelId);
                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()){
                                    DatabaseReference reference=FirebaseUtil.getDatabase().getReference("Tabs").child(secureId);
                                    reference.setValue(hotelId);
                                }
                                else{
                                    Toast.makeText(LoginActivity.this, "Contact service provider to register your Hotel.", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }
            });
        }
    }

    private void verifyTabDetails(@NonNull final String macAddress){

        FirebaseUtil.getDatabase().getReference("Tabs").keepSynced(true);

        DatabaseReference reference = FirebaseUtil.getDatabase().getReference("Tabs").child(macAddress);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //If tab is not registered
                if(!dataSnapshot.exists()){
                    progressDialog.dismiss();
                  
                    if(!LoginActivity.this.isDestroyed() && !LoginActivity.this.isFinishing())
                    {
                        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                        builder.setMessage("The tab has not been registered to the hotel yet. Click on Register to join the Hotel");
                        builder.setPositiveButton("OK",null);
                        builder.show();
                    }

                    tvHotelId.setVisibility(View.VISIBLE);
                    etHotelID.setVisibility(View.VISIBLE);

                    tvRoom.setVisibility(View.GONE);
                    etRoom.setVisibility(View.GONE);

                    btnLogin.setEnabled(false);
                    btnLogin.setVisibility(View.INVISIBLE);

                    btnRegister.setEnabled(true);
                    btnRegister.setVisibility(View.VISIBLE);
                }

                //If registration is successful but room not allotted
                else if(!dataSnapshot.getValue().toString().contains("@")){
                    progressDialog.dismiss();

                    hID = dataSnapshot.getValue().toString();

                    tvHotelId.setVisibility(View.VISIBLE);
                    etHotelID.setVisibility(View.VISIBLE);
                    etHotelID.setText(hID);
                    etHotelID.setEnabled(false);

                    tvRoom.setVisibility(View.VISIBLE);
                    etRoom.setVisibility(View.VISIBLE);

                    btnLogin.setEnabled(true);
                    btnLogin.setVisibility(View.VISIBLE);

                    btnRegister.setEnabled(false);
                    btnRegister.setVisibility(View.GONE);
                }

                //If registration is successful and room is allotted
                else if(dataSnapshot.getValue().toString().contains("@")){
                    String HotelRoom = dataSnapshot.getValue().toString();
                    String[] parts=HotelRoom.split("@");
                    final String hotel=parts[0];
                    final String room=parts[1];
                    validateForDirectSignIn(hotel,room);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    private void validate() {

        final String room = etRoom.getText().toString().trim();
        if(room.isEmpty()) {
            pd.dismiss();
            btnLogin.setEnabled(true);
            Toast.makeText(this, "Please fill all details", Toast.LENGTH_SHORT).show();
        }
        else{
            final DatabaseReference reference = FirebaseUtil.getDatabase().getReference(hID);
            reference.child("Users").keepSynced(true);

            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(!dataSnapshot.exists()){
                        Toast.makeText(LoginActivity.this, "Hotel not registered yet.", Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                        btnLogin.setEnabled(true);
                    }
                    else if(!dataSnapshot.child("Users").child(room).exists()){
                        pd.dismiss();
                        btnLogin.setEnabled(true);
                        Toast.makeText(LoginActivity.this, "Room number not registered yet.\nContact admin.", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        String status = Objects.requireNonNull(dataSnapshot.child("Users").child(room).getValue()).toString();
                        switch (status) {
                            case "inUse":
                                Toast.makeText(LoginActivity.this, "Room already in use.\n Contact admin to logout.", Toast.LENGTH_SHORT).show();
                                pd.dismiss();
                                btnLogin.setEnabled(true);
                                break;
                            case "inactive":
                                Toast.makeText(LoginActivity.this, "Please check-in first.", Toast.LENGTH_SHORT).show();
                                pd.dismiss();
                                btnLogin.setEnabled(true);
                                break;
                            case "active":
                                FirebaseUtil.getDatabase().getReference("Tabs").child(secureId).setValue(hID+"@"+room);
                                break;
                        }

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }
    private void signIN(@NonNull final String hotelID, @NonNull final String room){
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            final String displayName = hotelID+"@"+room;

                            FirebaseUser user = mAuth.getCurrentUser();

                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(displayName).build();
                            assert user != null;
                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                FirebaseUtil.getDatabase().getReference(hotelID).child("Users").child(room).setValue("inUse");
                                                finish();
                                                startActivity(new Intent(LoginActivity.this,MainActivity.class));
                                                progressDialog.dismiss();
                                                pd.dismiss();
                                                btnLogin.setEnabled(true);
                                            }
                                            else {
                                                progressDialog.dismiss();
                                                pd.dismiss();
                                                btnLogin.setEnabled(true);
                                                Toast.makeText(LoginActivity.this, "Error! Try again.", Toast.LENGTH_SHORT).show();
                                            }

                                        }
                                    });



                        }
                        else {
                            Toast.makeText(LoginActivity.this, "Sign-in failed. Try Again.", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            pd.dismiss();
                            btnLogin.setEnabled(true);
                        }
                    }
                });

    }
    private void validateForDirectSignIn(@NonNull final String hotel, @NonNull final String room){

        final DatabaseReference reference = FirebaseUtil.getDatabase().getReference(hotel);
        reference.child("Users").keepSynced(true);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String status = Objects.requireNonNull(dataSnapshot.child("Users").child(room).getValue()).toString();
                switch (status) {
                    case "inUse":
                        signIN(hotel,room);
                        break;
                    case "inactive":
                        Toast.makeText(LoginActivity.this, "Please check-in first.", Toast.LENGTH_SHORT).show();
                        btnLogin.setEnabled(true);
                        progressDialog.dismiss();
                        pd.dismiss();
                        break;
                    case "active":
                        signIN(hotel,room);
                        break;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                pd.dismiss();
                progressDialog.dismiss();
                Toast.makeText(LoginActivity.this, "Something went wrong.", Toast.LENGTH_SHORT).show();
            }
        });

    }
    private boolean isConnected(Context context){
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        if(networkInfo != null && networkInfo.isConnectedOrConnecting()){
            android.net.NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            android.net.NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            return (mobile != null && mobile.isConnectedOrConnecting()) || (wifi != null && wifi.isConnectedOrConnecting());
        }
        else
            return false;
    }
    private AlertDialog.Builder builderDialog(Context c){
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("No Internet Connection");
        builder.setMessage("Make sure you have an active internet connection to continue.");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        return builder;
    }
}
