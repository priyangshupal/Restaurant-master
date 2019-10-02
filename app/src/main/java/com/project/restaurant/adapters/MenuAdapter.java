package com.project.restaurant.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.project.restaurant.BottomMenuHelper;
import com.project.restaurant.FirebaseUtil;
import com.project.restaurant.list_items.MenuListItem;
import com.project.restaurant.R;

import java.util.List;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.ViewHolder> {

    private List<MenuListItem> menuListItems;
    private Context context;
    String url="";

    public MenuAdapter(List<MenuListItem> menuListItems, Context context) {
        this.menuListItems = menuListItems;
        this.context = context;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int position) {
        final MenuListItem listItem = menuListItems.get(position);

        viewHolder.tvDishName.setText(listItem.getDish_name());
        viewHolder.tvDishDesc.setText(listItem.getDish_desc());
        String dishPrice = "Rs."+listItem.getDish_price();
        viewHolder.tvDishPrice.setText(dishPrice);
        String dishAmount = listItem.getDish_amount().toString();
        viewHolder.tvDishAmount.setText(dishAmount);

        url=listItem.getDish_url();
        if(url.equals(""))
            url = "https://firebasestorage.googleapis.com/v0/b/restaurant-9ba9d.appspot.com/o/img_gbread.jpeg?alt=media&token=109fa596-53a5-46c9-9c76-a7dc80f5a776";
        Glide.with(viewHolder.itemView.getContext()).load(url).into(viewHolder.ivDishImage);

        viewHolder.increase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Integer amount = Integer.parseInt(viewHolder.tvDishAmount.getText().toString()) + 1;
                viewHolder.tvDishAmount.setText(amount.toString());
                addToCart(listItem.getDish_name(),listItem.getDish_price());
            }
        });

        viewHolder.reduce.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Integer amount=Integer.parseInt(viewHolder.tvDishAmount.getText().toString());
                if(amount>0){
                    amount -= 1;
                    viewHolder.tvDishAmount.setText(amount.toString());
                    subFromCart(listItem.getDish_name(),listItem.getDish_price());
                }
            }
        });
    }
    @Override
    public int getItemCount() {
        return menuListItems.size();
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.menu_list_item,viewGroup,false);
        return new ViewHolder(v);
    }
    class ViewHolder extends RecyclerView.ViewHolder{
        TextView tvDishName;
        TextView tvDishDesc;
        TextView tvDishAmount;
        TextView tvDishPrice;
        ImageView ivDishImage;
        ImageButton reduce,increase;
        BottomNavigationView navView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDishName   = itemView.findViewById(R.id.dish_name);
            tvDishDesc   = itemView.findViewById(R.id.dish_description);
            tvDishAmount = itemView.findViewById(R.id.dish_amount);
            tvDishPrice  = itemView.findViewById(R.id.dish_price);
            ivDishImage  = itemView.findViewById(R.id.dish_image);
            reduce       = itemView.findViewById(R.id.dish_subtract_button);
            increase     = itemView.findViewById(R.id.dish_add_button);
            navView      = itemView.findViewById(R.id.nav_view);
        }
    }

    private void addToCart(@NonNull final String name, final Integer price){

        final String tabID =FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        assert tabID != null;
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
                //progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //progressDialog.dismiss();
            }
        });
    }
    private void subFromCart(@NonNull final String name, final Integer price){

        final String tabID = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        assert tabID != null;
        String[] parts=tabID.split("@");
        String hotel=parts[0];
        final String room=parts[1];

        final DatabaseReference databaseReference= FirebaseUtil.getDatabase().getReference(hotel);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Integer amount = Integer.parseInt(dataSnapshot.child("Current Orders").child(room).child(name).getValue().toString()) - 1;
                Integer value = Integer.parseInt(dataSnapshot.child("Current Orders").child(room).child("TotalPrice").getValue().toString()) - price;

                if(amount==0 && value==0)
                    databaseReference.child("Current Orders").child(room).removeValue();

                else if(amount==0 && value!=0){
                    databaseReference.child("Current Orders").child(room).child(name).removeValue();
                    databaseReference.child("Current Orders").child(room).child("TotalPrice").setValue(value);
                }

                else{
                    databaseReference.child("Current Orders").child(room).child(name).setValue(amount);
                    databaseReference.child("Current Orders").child(room).child("TotalPrice").setValue(value);
                }

                //progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //progressDialog.dismiss();
            }
        });
    }
}