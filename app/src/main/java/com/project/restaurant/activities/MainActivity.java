package com.project.restaurant.activities;

import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import android.view.MenuItem;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OneSignal;
import com.project.restaurant.BottomMenuHelper;
import com.project.restaurant.FirebaseUtil;
import com.project.restaurant.fragments.CartFragment;
import com.project.restaurant.fragments.MenuFragment;
import com.project.restaurant.R;
import com.project.restaurant.fragments.StatusFragment;

import org.json.JSONObject;

import java.util.Objects;

import static com.onesignal.OneSignal.OSInFocusDisplayOption.None;

public class MainActivity extends AppCompatActivity
        implements BottomNavigationView.OnNavigationItemSelectedListener{

    @Nullable
    Fragment fragment = null;
    String currentFragment;
    private BottomNavigationView navView;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    @Nullable
    private FirebaseUser user=mAuth.getCurrentUser();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isInactive();


        if(savedInstanceState == null){
            loadFragment(new MenuFragment());
            getSupportActionBar().setTitle("Menu");
        }

        navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(this);

        String tabID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getDisplayName();
        assert tabID != null;
        String[] parts= tabID.split("@");
        String hotel = parts[0];
        String room = parts[1];

        DatabaseReference reference = FirebaseUtil.getDatabase().getReference(hotel)
                .child("Current Orders").child(room);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    String i = String.valueOf(dataSnapshot.getChildrenCount() - 1);
                    if(!i.equals("0"))
                        BottomMenuHelper.showBadge(MainActivity.this,navView,R.id.navigation_cart,i);
                    else
                        BottomMenuHelper.removeBadge(navView,R.id.navigation_cart);
                }
                else
                    BottomMenuHelper.removeBadge(navView,R.id.navigation_cart);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void isInactive(){
        String temp = user.getDisplayName();
        assert temp != null;
        String[] parts = temp.split("@");
        String hotelID = parts[0];
        String room = parts[1];

        DatabaseReference reference = FirebaseUtil.getDatabase().getReference(hotelID).child("Users").child(room);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String status = dataSnapshot.getValue().toString();
                if(status.equals("inactive")){
                    mAuth.signOut();
                    user.delete();
                    Toast.makeText(MainActivity.this, "Sign-out successful by admin", Toast.LENGTH_SHORT).show();
                    finish();
                    startActivity(new Intent(MainActivity.this,LoginActivity.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private boolean loadFragment(@Nullable Fragment fragment){
        if(fragment != null){
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        currentFragment = Objects.requireNonNull(getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container))
                .getClass().getSimpleName();

        switch (menuItem.getItemId()){
            case R.id.navigation_menu: {
                if(currentFragment.equals("MenuFragment")) break;
                else{
                    currentFragment ="MenuFragment";
                    fragment = new MenuFragment();
                    getSupportActionBar().setTitle("Menu");
                    break;
                }
            }
            case R.id.navigation_cart: {
                if(currentFragment.equals("CartFragment")) break;
                else{
                    currentFragment ="CartFragment";
                    fragment = new CartFragment();
                    getSupportActionBar().setTitle("Your Cart");
                    break;
                }
            }
            case R.id.navigation_status: {
                if(currentFragment.equals("StatusFragment")) break;
                else{
                    currentFragment ="StatusFragment";
                    fragment = new StatusFragment();
                    getSupportActionBar().setTitle("Order Status");
                    break;
                }
            }
        }

        return loadFragment(fragment);
    }
}
