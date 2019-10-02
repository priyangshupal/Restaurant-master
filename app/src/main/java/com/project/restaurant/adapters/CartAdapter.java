package com.project.restaurant.adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.project.restaurant.FirebaseUtil;
import com.project.restaurant.list_items.CartListItem;
import com.project.restaurant.R;

import java.util.List;
import java.util.Objects;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    //List constructor to hold contents of recycler view
    private List<CartListItem> cartListItems;
    private Context context;

    public CartAdapter(List<CartListItem> cartListItems, Context context) {
        this.cartListItems = cartListItems;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.cart_item_list,viewGroup,false);
        return new ViewHolder(v);
    }
    //To declare and initialize all views
    class ViewHolder extends RecyclerView.ViewHolder{
        TextView tvCartDishName,tvCartDishPrice,tvCartDishAmount,tvRemove;
        ImageButton tvCartDishReduce, tvCartDishIncrease;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRemove = itemView.findViewById(R.id.cart_remove_dish);
            tvCartDishName = itemView.findViewById(R.id.cart_dish_name);
            tvCartDishPrice = itemView.findViewById(R.id.cart_dish_price);
            tvCartDishAmount = itemView.findViewById(R.id.cart_dish_amount);
            tvCartDishReduce = itemView.findViewById(R.id.cart_dish_subtract_button);
            tvCartDishIncrease = itemView.findViewById(R.id.cart_dish_add_button);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int position) {

        final CartListItem listItem = cartListItems.get(position);

        //Get all variables from list_items class & set to different views
        viewHolder.tvCartDishName.setText(listItem.getCart_dish_name());
        String temp = "Rs. " + listItem.getCart_dish_price();
        viewHolder.tvCartDishPrice.setText(temp);
        viewHolder.tvCartDishAmount.setText(listItem.getCart_dish_amount());


        viewHolder.tvCartDishIncrease.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                //get dish amount
                Integer amount = Integer.parseInt(viewHolder.tvCartDishAmount.getText().toString()) + 1;
                viewHolder.tvCartDishAmount.setText(amount.toString());

                //update dish amount
                addToCart(listItem.getCart_dish_name(),Integer.parseInt(listItem.getCart_dish_price()));
            }
        });

        viewHolder.tvCartDishReduce.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                Integer amount = Integer.parseInt(viewHolder.tvCartDishAmount.getText().toString());
                if(amount>=0){
                    subtractFromCart(listItem.getCart_dish_name(),Integer.parseInt(listItem.getCart_dish_price()));
                    amount -= 1;
                    viewHolder.tvCartDishAmount.setText(amount.toString());
                }
                if(amount == 0){
                    cartListItems.remove(viewHolder.getAdapterPosition());
                    notifyDataSetChanged();
                }
            }
        });

        viewHolder.tvRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Are you sure want to remove this item from cart?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                removeFromCart(viewHolder.getAdapterPosition(),
                                        Integer.parseInt(viewHolder.tvCartDishAmount.getText().toString()));
                            }
                        })
                        .setNegativeButton("No",null);
                AlertDialog alert= builder.create();
                alert.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartListItems.size();
    }

    private void addToCart(@NonNull final String name, final Integer price){
        final String tabID = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        String[] parts=tabID.split("@");
        String hotel=parts[0];
        final String room=parts[1];

        final DatabaseReference databaseReference= FirebaseUtil.getDatabase().getReference(hotel);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.child("Current Orders").exists()){
                    databaseReference.child("Current Orders").child(room).child(name).setValue(1);
                    databaseReference.child("Current Orders").child(room).child("TotalPrice").setValue(price);
                }
                else if(!dataSnapshot.child("Current Orders").child(room).exists()){
                    databaseReference.child("Current Orders").child(room).child(name).setValue(1);
                    databaseReference.child("Current Orders").child(room).child("TotalPrice").setValue(price);
                }
                else if(!dataSnapshot.child("Current Orders").child(room).child(name).exists()){
                    databaseReference.child("Current Orders").child(room).child(name).setValue(1);
                    Integer value = Integer.parseInt(dataSnapshot.child("Current Orders").child(room).child("TotalPrice").getValue().toString()) + price;
                    databaseReference.child("Current Orders").child(room).child("TotalPrice").setValue(value);
                }
                else if(dataSnapshot.child("Current Orders").child(room).child(name).exists()){
                    Integer amount = Integer.parseInt(dataSnapshot.child("Current Orders").child(room).child(name).getValue().toString()) + 1;
                    Integer value = Integer.parseInt(dataSnapshot.child("Current Orders").child(room).child("TotalPrice").getValue().toString()) + price;
                    databaseReference.child("Current Orders").child(room).child(name).setValue(amount);
                    databaseReference.child("Current Orders").child(room).child("TotalPrice").setValue(value);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //progressDialog.dismiss();
            }
        });

    }
    private void subtractFromCart(@NonNull final String name, final Integer price){
        final String tabID =FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        String[] parts=tabID.split("@");
        String hotel=parts[0];
        final String room=parts[1];

        final DatabaseReference databaseReference=FirebaseUtil.getDatabase().getReference(hotel);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                
                Integer amount = Integer.parseInt(dataSnapshot.child("Current Orders").child(room).child(name).getValue().toString()) - 1;
                Integer value = Integer.parseInt(dataSnapshot.child("Current Orders").child(room).child("TotalPrice").getValue().toString()) - price;

                if(amount==0 && value==0){
                    databaseReference.child("Current Orders").child(room).child(name).removeValue();
                    databaseReference.child("Current Orders").child(room).child("TotalPrice").setValue(value);
                }

                else if(amount==0){
                    databaseReference.child("Current Orders").child(room).child(name).removeValue();
                    databaseReference.child("Current Orders").child(room).child("TotalPrice").setValue(value);
                }

                else{
                    databaseReference.child("Current Orders").child(room).child(name).setValue(amount);
                    databaseReference.child("Current Orders").child(room).child("TotalPrice").setValue(value);
                }

               // progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //progressDialog.dismiss();
            }
        });
    }
    private void removeFromCart(int position, final int amount){
        CartListItem listItem = cartListItems.get(position);
        final String name = listItem.getCart_dish_name();
        final Integer p = Integer.parseInt(listItem.getCart_dish_price());
        cartListItems.remove(position);

        final String tabID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getDisplayName();
        String[] parts=tabID.split("@");
        String hotel=parts[0];
        final String room=parts[1];

        final DatabaseReference databaseReference = FirebaseUtil.getDatabase().getReference(hotel).child("Current Orders").child(room);
        databaseReference.child(name).removeValue();

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Integer value = Integer.parseInt(dataSnapshot.child("TotalPrice").getValue().toString());
                value -= (p*amount);
                databaseReference.child("TotalPrice").setValue(value);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        //update recycler view
        notifyItemRemoved(position);

    }

}
