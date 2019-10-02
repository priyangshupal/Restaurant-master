package com.project.restaurant.adapters;

import android.app.AlertDialog;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.project.restaurant.FirebaseUtil;
import com.project.restaurant.R;
import com.project.restaurant.list_items.StatusListItem;

import java.util.List;

public class StatusAdapter extends RecyclerView.Adapter<StatusAdapter.ViewHolder> {

    StringBuilder message;

    private List<StatusListItem> statusListItems;
    public StatusAdapter(List<StatusListItem> statusListItems) {
        this.statusListItems = statusListItems;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.status_list_item,viewGroup,false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        final StatusListItem listItem = statusListItems.get(position);
        String amount = "Rs. "+ listItem.getOrderAmount();
        viewHolder.tvOrderAmount.setText(amount);
        viewHolder.tvEstimatedTime.setText(listItem.getEstimatedTime());
        viewHolder.rbPlaced.setChecked(listItem.isoPlaced());
        viewHolder.rbConfirmed.setChecked(listItem.isoConfirmed());
        viewHolder.rbReady.setChecked(listItem.isoReady());

        viewHolder.cvStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(@NonNull final View view) {
                message = new StringBuilder("Your Orders are:-\n");
                String orderId= listItem.getOrderId();
                final String tabId= FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
                String[] parts=tabId.split("@");
                String hotel=parts[0];
                final String room=parts[1];

                DatabaseReference reference= FirebaseUtil.getDatabase().getReference(hotel)
                        .child("Orders").child(room).child(orderId);
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot item:dataSnapshot.child("Items").getChildren()) {
                            message.append(item.getKey()).append(" X ").append(item.getValue().toString()).append("\n");
                        }
                        message.append("\nTime of orders: ").append(dataSnapshot.child("Time").getValue().toString());
                        message.append("\nOrder ID: ").append(listItem.getOrderId());
                        AlertDialog.Builder builder=new AlertDialog.Builder(view.getContext());
                        builder.setMessage(message);
                        builder.setPositiveButton("OK",null);
                        builder.show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        message=new StringBuilder("Sorry some error occurred, couldn't find the order.");
                        AlertDialog.Builder builder=new AlertDialog.Builder(view.getContext());
                        builder.setMessage(message);
                        builder.setPositiveButton("OK",null);
                        builder.show();
                    }
                });

            }
        });
    }



    @Override
    public int getItemCount() {
        return statusListItems.size();
    }

    class  ViewHolder extends RecyclerView.ViewHolder{

        TextView tvOrderAmount,tvEstimatedTime;
        RadioButton rbPlaced,rbConfirmed,rbReady;
        CardView cvStatus;

        ViewHolder(@NonNull final View itemView) {
            super(itemView);
            tvOrderAmount = itemView.findViewById(R.id.status_OrderAmount);
            tvEstimatedTime = itemView.findViewById(R.id.status_estimatedTime);
            rbPlaced = itemView.findViewById(R.id.rb_Placed);
            rbReady = itemView.findViewById(R.id.rb_Ready);
            rbConfirmed = itemView.findViewById(R.id.rb_Confirmed);
            cvStatus = itemView.findViewById(R.id.cvStatusItem);
        }

    }

}
