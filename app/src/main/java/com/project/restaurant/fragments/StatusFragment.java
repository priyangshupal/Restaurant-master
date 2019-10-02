package com.project.restaurant.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.project.restaurant.FirebaseUtil;
import com.project.restaurant.R;
import com.project.restaurant.adapters.StatusAdapter;
import com.project.restaurant.list_items.StatusListItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class StatusFragment extends Fragment {

    @SuppressLint("InflateParams")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_status,null);
    }

    private RecyclerView statusRecyclerView;
    private RecyclerView.Adapter statusAdapter;
    private List<StatusListItem> statusListItems;
    private ImageView imgEmptyStatus;
    private TextView tvEmptyStatus;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imgEmptyStatus = view.findViewById(R.id.img_no_status);
        tvEmptyStatus = view.findViewById(R.id.tv_noStatus);
        statusRecyclerView = view.findViewById(R.id.status_recycler_view);
        statusRecyclerView.setHasFixedSize(true);
        statusRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        statusListItems = new ArrayList<>();

        getOrders();

        final SwipeRefreshLayout pullToRefresh = view.findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getOrders();
                pullToRefresh.setRefreshing(false);
            }
        });

    }

    private void getOrders(){
        final String tabID= FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        String[] parts=tabID.split("@");
        String hotel=parts[0];
        final String room=parts[1];

        DatabaseReference databaseReference= FirebaseUtil.getDatabase().getReference(hotel);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                statusListItems.clear();
                if(!dataSnapshot.child("Orders").exists()) {
                    statusRecyclerView.setVisibility(View.GONE);
                    imgEmptyStatus.setVisibility(View.VISIBLE);
                    tvEmptyStatus.setVisibility(View.VISIBLE);
                }
                else if(!dataSnapshot.child("Orders").child(room).exists()){
                    statusRecyclerView.setVisibility(View.GONE);
                    imgEmptyStatus.setVisibility(View.VISIBLE);
                    tvEmptyStatus.setVisibility(View.VISIBLE);
                }

                else{
                    for(DataSnapshot orders: dataSnapshot.child("Orders").child(room).getChildren()){
                        String status="";
                        if(orders.child("Status").exists())
                            status = orders.child("Status").getValue().toString();
                        if(!status.equals("Order Delivered")){
                            String orderId= orders.getKey();
                            String orderAmount="Fetching...";
                            if(orders.child("TotalPrice").exists()){
                                orderAmount = orders.child("TotalPrice").getValue().toString();
                            }


                            StatusListItem listItem;
                            switch (status) {
                                case "Order Placed":
                                    listItem = new StatusListItem(orderId,orderAmount,"To be updated", true, false, false);
                                    break;

                                case "Order Confirmed":
                                    String time = "";
                                    if (orders.child("Time").exists())
                                        time = orders.child("Time").getValue().toString();

                                    @SuppressLint("SimpleDateFormat")
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                    Integer estimatedMinute = 0;

                                    if (orders.child("Estimated Minute").exists())
                                        estimatedMinute = Integer.parseInt(orders.child("Estimated Minute").getValue().toString());
                                    String estimated;
                                    try {
                                        Date timeOfOrder = dateFormat.parse(time);
                                        Calendar calendar = Calendar.getInstance();
                                        calendar.setTime(timeOfOrder);
                                        calendar.add(Calendar.MINUTE, estimatedMinute);
                                        Date timeOfDelivery = calendar.getTime();
                                        calendar = Calendar.getInstance();
                                        Date currentTime = calendar.getTime();
                                        long diff = timeOfDelivery.getTime() - currentTime.getTime();
                                        long sec = diff / 1000;
                                        long min = sec / 60;
                                        if (min <= 10) min = 10;
                                        estimated = min + " Minutes";

                                    } catch (ParseException e) {
                                        estimated = estimatedMinute + " min from order time";
                                    }

                                    listItem = new StatusListItem(orderId, orderAmount,estimated, true, true, false);
                                    break;

                                case "Order Ready":
                                    listItem = new StatusListItem(orderId,orderAmount, "5 Minutes", true, true, true);
                                    break;

                                default:
                                    listItem = new StatusListItem(orderId,orderAmount, "Order Denied! Sorry.", false, false, false);
                                    break;
                            }
                            statusListItems.add(listItem);
                        }
                    }

                    statusAdapter = new StatusAdapter(statusListItems);
                    statusRecyclerView.setAdapter(statusAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
