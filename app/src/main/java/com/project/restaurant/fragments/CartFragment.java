package com.project.restaurant.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.project.restaurant.FirebaseUtil;
import com.project.restaurant.list_items.CartListItem;
import com.project.restaurant.R;
import com.project.restaurant.adapters.CartAdapter;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class CartFragment extends Fragment {

    private String room;
    private String hotel;

    @SuppressLint("InflateParams")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart,null);
    }

    private RecyclerView cartRecyclerView;
    @Nullable
    private RecyclerView.Adapter cartAdapter;
    private List<CartListItem> cartListItems;
    private RelativeLayout emptyCart, placedOrder;
    private TextView totalPrice;
    private Button btnCheckout;
    private HashMap<String,String> priceList;
    private List<String> menuItems;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cartRecyclerView    = view.findViewById(R.id.cart_recycler_view);
        totalPrice          = view.findViewById(R.id.TotalPrice);
        emptyCart           = view.findViewById(R.id.empty_cart);
        placedOrder         = view.findViewById(R.id.order_placed);
        btnCheckout         = view.findViewById(R.id.cart_checkout);
        cartRecyclerView.setHasFixedSize(true);
        cartRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        cartListItems = new ArrayList<>();
        menuItems     = new ArrayList<>();
        priceList     = new HashMap<>();

        placedOrder.setVisibility(View.GONE);
        getPriceList();
        loadCartRecyclerView();
        updatePrice();

        final String tabID =FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        String[] parts=tabID.split("@");
        hotel=parts[0];
        room=parts[1];

        DatabaseReference databaseReference= FirebaseUtil.getDatabase().getReference(hotel).child("Current Orders").child(room);
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //skip
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                getPriceList();
//                loadCartRecyclerView();
                //skip
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                loadCartRecyclerView();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //skip
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //skip
            }
        });

        btnCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updatePrice();
                String price = totalPrice.getText().toString();
                if(!price.equals("Rs. 0")){
                    AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                    builder.setMessage("Confirm? The order price will be added to your bill")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    final String tabId= FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
                                    String[] parts=tabId.split("@");
                                    String hotel=parts[0];
                                    final String room=parts[1];

                                    Long timestamp=System.currentTimeMillis()/1000;
                                    final String Timestamp= timestamp.toString();
                                    final DatabaseReference databaseReference= FirebaseUtil.getDatabase().getReference(hotel);
                                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.child("Current Orders").child(room).exists()){

                                                for(DataSnapshot orders: dataSnapshot.child("Current Orders").child(room).getChildren()){
                                                    String key=orders.getKey();
                                                    String value=orders.getValue().toString();
                                                    if(key.equals("TotalPrice")){
                                                        databaseReference.child("Orders").child(room).child(Timestamp).child(key).setValue(value);
                                                    }
                                                    else {
                                                        databaseReference.child("Orders").child(room).child(Timestamp).child("Items").child(key).setValue(value);
                                                    }
                                                }
                                                Calendar c= Calendar.getInstance();
                                                @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                                String datetime=dateFormat.format(c.getTime());
                                                databaseReference.child("Orders").child(room).child(Timestamp).child("Time").setValue(datetime);
                                                databaseReference.child("Orders").child(room).child(Timestamp).child("Status").setValue("Order Placed");

                                                databaseReference.child("Current Orders").child(room).removeValue();
                                                databaseReference.child("Current Orders").child(room).child("TotalPrice").setValue(0);
                                                btnCheckout.setEnabled(false);

                                            }

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                    placedOrder.setVisibility(View.VISIBLE);
                                    sendNotification();

                                    if(!isConnected(getContext()))
                                        builderDialog(getContext()).show();
                                }
                            })
                            .setNegativeButton("No",null);
                    AlertDialog alert= builder.create();
                    alert.show();

                }
                else
                    Toast.makeText(getContext(), "Your cart is empty", Toast.LENGTH_SHORT).show();

            }
        });
    }


    private void getPriceList(){
        final String tabID =FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        String[] parts=tabID.split("@");
        final String hotel=parts[0];
        final String room=parts[1];

        DatabaseReference databaseReference1=FirebaseUtil.getDatabase().getReference(hotel).child("Menu");
        databaseReference1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot item: dataSnapshot.getChildren())
                {
                    String Price=item.child("Price").getValue().toString();
                    priceList.put(item.getKey(),Price);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadCartRecyclerView(){
        cartListItems.clear();
        final String tabID =FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        String[] parts=tabID.split("@");
        final String hotel=parts[0];
        final String room=parts[1];


        final DatabaseReference databaseReference = FirebaseUtil.getDatabase().getReference(hotel);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("Current Orders").child(room).exists()){

                    for(DataSnapshot item: dataSnapshot.child("Current Orders").child(room).getChildren()){

                        final String name = item.getKey();
                        assert name != null;
                        if(name.equals("TotalPrice")){
                            String p = "Rs. "+ item.getValue().toString();
                            totalPrice.setText(p);
                        }
                        else
                        {
                            final String amount = item.getValue().toString();
                            //String price = getDishPrice(name); TODO
                            //final DatabaseReference databaseReference1 = FirebaseUtil.getDatabase().getReference(hotel).child("Menu");
                            String price=priceList.get(name);
                            CartListItem cartListItem= new CartListItem(name,price,amount);
                            cartListItems.add(cartListItem);
                        }
                    }
                    cartAdapter = new CartAdapter(cartListItems,getContext());
                    cartRecyclerView.setAdapter(cartAdapter);
                }
                else
                {
                    emptyCart.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void updatePrice(){

        final String tabID =FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        String[] parts=tabID.split("@");
        String hotel=parts[0];
        final String room=parts[1];

        final DatabaseReference db = FirebaseUtil.getDatabase().getReference(hotel).child("Current Orders").child(room).child("TotalPrice");
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String p = "Rs. " + dataSnapshot.getValue().toString();
                    totalPrice.setText(p);
                    if(dataSnapshot.getValue().toString().equals("0")){
                        emptyCart.setVisibility(View.VISIBLE);
                        cartRecyclerView.setVisibility(View.GONE);
                    }
                    else{
                        emptyCart.setVisibility(View.GONE);
                        cartRecyclerView.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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
        builder.setMessage("Your orders will be updated once internet connection is back.");
        builder.setPositiveButton("Ok",null);
        return builder;
    }

    private void sendNotification() {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                int SDK_INT = android.os.Build.VERSION.SDK_INT;
                if (SDK_INT > 8) {
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                            .permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                    String receiverTag = hotel+"@admin";

                    try {
                        String jsonResponse;

                        URL url = new URL("https://onesignal.com/api/v1/notifications");
                        HttpURLConnection con = (HttpURLConnection) url.openConnection();
                        con.setUseCaches(false);
                        con.setDoOutput(true);
                        con.setDoInput(true);

                        con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                        con.setRequestProperty("Authorization", "Basic MzRmZjJiYmMtZDQxNS00NTgzLTk1ZTAtMDlmNTE3MWRkY2U0");
                        con.setRequestMethod("POST");

                        String strJsonBody = "{"
                                + "\"app_id\": \"1eb05efd-4236-47d4-b460-3eebd46878ed\","

                                + "\"filters\": [{\"field\": \"tag\", \"key\": \"Admin_ID\", \"relation\": \"=\", \"value\": \"" + receiverTag + "\"}],"

                                + "\"data\": {\"foo\": \"bar\"},"
                                + "\"headings\": {\"en\": \" "+"New order received"+" \"},"
                                + "\"contents\": {\"en\": \" "+"From room no " +room+" \"}"
                                + "}";


                        System.out.println("strJsonBody:\n" + strJsonBody);

                        byte[] sendBytes = strJsonBody.getBytes(StandardCharsets.UTF_8);
                        con.setFixedLengthStreamingMode(sendBytes.length);

                        OutputStream outputStream = con.getOutputStream();
                        outputStream.write(sendBytes);

                        int httpResponse = con.getResponseCode();
                        System.out.println("httpResponse: " + httpResponse);

                        if (httpResponse >= HttpURLConnection.HTTP_OK
                                && httpResponse < HttpURLConnection.HTTP_BAD_REQUEST) {
                            Scanner scanner = new Scanner(con.getInputStream(), "UTF-8");
                            jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                            scanner.close();
                        }
                        else {
                            Scanner scanner = new Scanner(con.getErrorStream(), "UTF-8");
                            jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                            scanner.close();
                        }
                        System.out.println("jsonResponse:\n" + jsonResponse);

                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }
        });
    }



}
