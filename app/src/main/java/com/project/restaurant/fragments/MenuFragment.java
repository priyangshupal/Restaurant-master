package com.project.restaurant.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.project.restaurant.FirebaseUtil;
import com.project.restaurant.list_items.MenuListItem;
import com.project.restaurant.R;
import com.project.restaurant.adapters.MenuAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class MenuFragment extends Fragment {

    @SuppressLint("InflateParams")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_menu,null);
    }

    private RecyclerView menuRecyclerView;
    @Nullable
    private RecyclerView.Adapter menuAdapter;
    private List<MenuListItem> menuListItems;
    private HashMap<String,Integer> orderAmount;
    private DatabaseReference databaseReference;
    private String hotel;
    private String room;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        menuRecyclerView = view.findViewById(R.id.menu_recycler_view);
        menuRecyclerView.setHasFixedSize(true);
        menuRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        menuListItems =new ArrayList<>();
        orderAmount = new HashMap<>();

        String tabID = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        assert tabID != null;
        String[] parts= tabID.split("@");
        hotel=parts[0];
        room=parts[1];

        if(!menuListItems.isEmpty())
            menuListItems.clear();
        loadRecyclerView();

        //Spinner for filtering menu list
        Spinner filterSpinner = view.findViewById(R.id.filter_menu_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(Objects.requireNonNull(getContext()),
                R.array.filter_choice, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(adapter);
        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(@NonNull AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                switch (item) {
                    case "All":
                        menuListItems.clear();
                        loadRecyclerView();
                        break;
                    case "Veg Only":
                        menuListItems.clear();
                        loadVegOnly();
                        break;
                    case "Breakfast":
                        menuListItems.clear();
                        loadBreakfast();
                        break;
                    case "Dal":
                        menuListItems.clear();
                        loadDals();
                        break;
                    case "Curry":
                        menuListItems.clear();
                        loadCurrys();
                        break;
                    case "Beverages":
                        menuListItems.clear();
                        loadBeverages();
                        break;
                    case "Hot Drinks":
                        menuListItems.clear();
                        loadHotDrinks();
                        break;
                    case "Indian Breakfast":
                        menuListItems.clear();
                        loadIndianBreakfast();
                        break;
                    case "Naan":
                        menuListItems.clear();
                        loadNaans();
                        break;
                    case "Raita":
                        menuListItems.clear();
                        loadRaitas();
                        break;
                    case "Rice":
                        menuListItems.clear();
                        loadRice();
                        break;
                    case "Salad":
                        menuListItems.clear();
                        loadSalad();
                        break;
                    case "Snacks":
                        menuListItems.clear();
                        loadSnacks();
                        break;
                    case "Soups":
                        menuListItems.clear();
                        loadSoups();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void getHashMap(){
        orderAmount.clear();
        databaseReference = FirebaseUtil.getDatabase().getReference(hotel);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("Current Orders").child(room).exists())
                {
                    for(DataSnapshot item: dataSnapshot.child("Current Orders").child(room).getChildren())
                    {
                        String name= item.getKey();
                        Integer amount=Integer.parseInt(item.getValue().toString());
                        orderAmount.put(name,amount);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadRecyclerView(){
        getHashMap();
        databaseReference = FirebaseUtil.getDatabase().getReference(hotel).child("Menu");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                menuListItems.clear();
                for(DataSnapshot item: dataSnapshot.getChildren()){

                    String name = item.getKey();
                    assert name != null;
                    int price = Integer.parseInt(dataSnapshot.child(name).child("Price").getValue().toString());
                    String desc= dataSnapshot.child(name).child("Type").getValue().toString();
                    String type= dataSnapshot.child(name).child("Category").getValue().toString();
                    Integer amount=0;
                    if(orderAmount.containsKey(name))
                        amount= orderAmount.get(name);
                    String url="";
                    if(dataSnapshot.child(name).child("Url").exists())
                        url=dataSnapshot.child(name).child("Url").getValue().toString();
                    MenuListItem menuListItem = new MenuListItem(name,desc,price,type,amount,url);
                    menuListItems.add(menuListItem);
                }
                menuAdapter = new MenuAdapter(menuListItems,getContext());
                menuRecyclerView.setAdapter(menuAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(),"Can't get",Toast.LENGTH_LONG).show();
            }
        });
    }

    //for filter spinner
    private void loadBeverages() {
        getHashMap();
        databaseReference = FirebaseUtil.getDatabase().getReference(hotel).child("Menu");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                menuListItems.clear();
                for(DataSnapshot item: dataSnapshot.getChildren()){
                    String name = item.getKey();
                    int price = Integer.parseInt(dataSnapshot.child(name).child("Price").getValue().toString());
                    String desc= dataSnapshot.child(name).child("Type").getValue().toString();
                    String type= dataSnapshot.child(name).child("Category").getValue().toString();
                    Integer amount=0;
                    if(orderAmount.containsKey(name))
                        amount= orderAmount.get(name);
                    String url="";
                    if(dataSnapshot.child(name).child("Url").exists())
                        url=dataSnapshot.child(name).child("Url").getValue().toString();
                    if(type.equals("Beverages")) {
                        MenuListItem menuListItem = new MenuListItem(name,desc,price,type,amount,url);
                        menuListItems.add(menuListItem);
                    }
                }
                menuAdapter = new MenuAdapter(menuListItems,getContext());
                menuRecyclerView.setAdapter(menuAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError){
                Toast.makeText(getContext(),"Can't get",Toast.LENGTH_LONG).show();
            }
        });
    }
    private void loadRaitas() {
        getHashMap();
        databaseReference = FirebaseUtil.getDatabase().getReference(hotel).child("Menu");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                menuListItems.clear();
                for(DataSnapshot item: dataSnapshot.getChildren()){
                    String name = item.getKey();
                    int price = Integer.parseInt(dataSnapshot.child(name).child("Price").getValue().toString());
                    String desc= dataSnapshot.child(name).child("Type").getValue().toString();
                    String type= dataSnapshot.child(name).child("Category").getValue().toString();
                    Integer amount=0;
                    if(orderAmount.containsKey(name))
                        amount= orderAmount.get(name);
                    String url="";
                    if(dataSnapshot.child(name).child("Url").exists())
                        url=dataSnapshot.child(name).child("Url").getValue().toString();
                    if(type.equals("Raita")) {
                        MenuListItem menuListItem = new MenuListItem(name,desc,price,type,amount,url);
                        menuListItems.add(menuListItem);
                    }
                }
                menuAdapter = new MenuAdapter(menuListItems,getContext());
                menuRecyclerView.setAdapter(menuAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError){
                Toast.makeText(getContext(),"Can't get",Toast.LENGTH_LONG).show();
            }
        });
    }
    private void loadIndianBreakfast() {
        getHashMap();
        databaseReference = FirebaseUtil.getDatabase().getReference(hotel).child("Menu");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                menuListItems.clear();
                for(DataSnapshot item: dataSnapshot.getChildren()){
                    String name = item.getKey();
                    int price = Integer.parseInt(dataSnapshot.child(name).child("Price").getValue().toString());
                    String desc= dataSnapshot.child(name).child("Type").getValue().toString();
                    String type= dataSnapshot.child(name).child("Category").getValue().toString();
                    Integer amount=0;
                    if(orderAmount.containsKey(name))
                        amount= orderAmount.get(name);
                    String url="";
                    if(dataSnapshot.child(name).child("Url").exists())
                        url=dataSnapshot.child(name).child("Url").getValue().toString();
                    if(type.equals("Indian Breakfast")) {
                        MenuListItem menuListItem = new MenuListItem(name,desc,price,type,amount,url);
                        menuListItems.add(menuListItem);
                    }
                }
                menuAdapter = new MenuAdapter(menuListItems,getContext());
                menuRecyclerView.setAdapter(menuAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError){
                Toast.makeText(getContext(),"Can't get",Toast.LENGTH_LONG).show();
            }
        });
    }
    private void loadCurrys() {
        getHashMap();
        databaseReference = FirebaseUtil.getDatabase().getReference(hotel).child("Menu");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                menuListItems.clear();
                for(DataSnapshot item: dataSnapshot.getChildren()){
                    String name = item.getKey();
                    int price = Integer.parseInt(dataSnapshot.child(name).child("Price").getValue().toString());
                    String desc= dataSnapshot.child(name).child("Type").getValue().toString();
                    String type= dataSnapshot.child(name).child("Category").getValue().toString();
                    Integer amount=0;
                    if(orderAmount.containsKey(name))
                        amount= orderAmount.get(name);
                    String url="";
                    if(dataSnapshot.child(name).child("Url").exists())
                        url=dataSnapshot.child(name).child("Url").getValue().toString();
                    if(type.equals("Curry")) {
                        MenuListItem menuListItem = new MenuListItem(name,desc,price,type,amount,url);
                        menuListItems.add(menuListItem);
                    }
                }
                menuAdapter = new MenuAdapter(menuListItems,getContext());
                menuRecyclerView.setAdapter(menuAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError){
                Toast.makeText(getContext(),"Can't get",Toast.LENGTH_LONG).show();
            }
        });
    }
    private void loadRice() {
        getHashMap();
        databaseReference = FirebaseUtil.getDatabase().getReference(hotel).child("Menu");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                menuListItems.clear();
                for(DataSnapshot item: dataSnapshot.getChildren()){
                    String name = item.getKey();
                    int price = Integer.parseInt(dataSnapshot.child(name).child("Price").getValue().toString());
                    String desc= dataSnapshot.child(name).child("Type").getValue().toString();
                    String type= dataSnapshot.child(name).child("Category").getValue().toString();
                    Integer amount=0;
                    if(orderAmount.containsKey(name))
                        amount= orderAmount.get(name);
                    String url="";
                    if(dataSnapshot.child(name).child("Url").exists())
                        url=dataSnapshot.child(name).child("Url").getValue().toString();
                    if(type.equals("Rice")) {
                        MenuListItem menuListItem = new MenuListItem(name,desc,price,type,amount,url);
                        menuListItems.add(menuListItem);
                    }
                }
                menuAdapter = new MenuAdapter(menuListItems,getContext());
                menuRecyclerView.setAdapter(menuAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError){
                Toast.makeText(getContext(),"Can't get",Toast.LENGTH_LONG).show();
            }
        });
    }
    private void loadNaans() {
        getHashMap();
        databaseReference = FirebaseUtil.getDatabase().getReference(hotel).child("Menu");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                menuListItems.clear();
                for(DataSnapshot item: dataSnapshot.getChildren()){
                    String name = item.getKey();
                    int price = Integer.parseInt(dataSnapshot.child(name).child("Price").getValue().toString());
                    String desc= dataSnapshot.child(name).child("Type").getValue().toString();
                    String type= dataSnapshot.child(name).child("Category").getValue().toString();
                    Integer amount=0;
                    if(orderAmount.containsKey(name))
                        amount= orderAmount.get(name);
                    String url="";
                    if(dataSnapshot.child(name).child("Url").exists())
                        url=dataSnapshot.child(name).child("Url").getValue().toString();
                    if(type.equals("Naan")) {
                        MenuListItem menuListItem = new MenuListItem(name,desc,price,type,amount,url);
                        menuListItems.add(menuListItem);
                    }
                }
                menuAdapter = new MenuAdapter(menuListItems,getContext());
                menuRecyclerView.setAdapter(menuAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError){
                Toast.makeText(getContext(),"Can't get",Toast.LENGTH_LONG).show();
            }
        });
    }
    private void loadSalad() {
        getHashMap();
        databaseReference = FirebaseUtil.getDatabase().getReference(hotel).child("Menu");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                menuListItems.clear();
                for(DataSnapshot item: dataSnapshot.getChildren()){
                    String name = item.getKey();
                    int price = Integer.parseInt(dataSnapshot.child(name).child("Price").getValue().toString());
                    String desc= dataSnapshot.child(name).child("Type").getValue().toString();
                    String type= dataSnapshot.child(name).child("Category").getValue().toString();
                    Integer amount=0;
                    if(orderAmount.containsKey(name))
                        amount= orderAmount.get(name);
                    String url="";
                    if(dataSnapshot.child(name).child("Url").exists())
                        url=dataSnapshot.child(name).child("Url").getValue().toString();
                    if(type.equals("Salad")) {
                        MenuListItem menuListItem = new MenuListItem(name,desc,price,type,amount,url);
                        menuListItems.add(menuListItem);
                    }
                }
                menuAdapter = new MenuAdapter(menuListItems,getContext());
                menuRecyclerView.setAdapter(menuAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError){
                Toast.makeText(getContext(),"Can't get",Toast.LENGTH_LONG).show();
            }
        });
    }
    private void loadSnacks() {
        getHashMap();
        databaseReference = FirebaseUtil.getDatabase().getReference(hotel).child("Menu");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                menuListItems.clear();
                for(DataSnapshot item: dataSnapshot.getChildren()){
                    String name = item.getKey();
                    int price = Integer.parseInt(dataSnapshot.child(name).child("Price").getValue().toString());
                    String desc= dataSnapshot.child(name).child("Type").getValue().toString();
                    String type= dataSnapshot.child(name).child("Category").getValue().toString();
                    Integer amount=0;
                    if(orderAmount.containsKey(name))
                        amount= orderAmount.get(name);
                    String url="";
                    if(dataSnapshot.child(name).child("Url").exists())
                        url=dataSnapshot.child(name).child("Url").getValue().toString();
                    if(type.equals("Snacks")) {
                        MenuListItem menuListItem = new MenuListItem(name,desc,price,type,amount,url);
                        menuListItems.add(menuListItem);
                    }
                }
                menuAdapter = new MenuAdapter(menuListItems,getContext());
                menuRecyclerView.setAdapter(menuAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError){
                Toast.makeText(getContext(),"Can't get",Toast.LENGTH_LONG).show();
            }
        });
    }
    private void loadSoups() {
        getHashMap();
        databaseReference = FirebaseUtil.getDatabase().getReference(hotel).child("Menu");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                menuListItems.clear();
                for(DataSnapshot item: dataSnapshot.getChildren()){
                    String name = item.getKey();
                    int price = Integer.parseInt(dataSnapshot.child(name).child("Price").getValue().toString());
                    String desc= dataSnapshot.child(name).child("Type").getValue().toString();
                    String type= dataSnapshot.child(name).child("Category").getValue().toString();
                    Integer amount=0;
                    if(orderAmount.containsKey(name))
                        amount= orderAmount.get(name);
                    String url="";
                    if(dataSnapshot.child(name).child("Url").exists())
                        url=dataSnapshot.child(name).child("Url").getValue().toString();
                    if(type.equals("Soups")) {
                        MenuListItem menuListItem = new MenuListItem(name,desc,price,type,amount,url);
                        menuListItems.add(menuListItem);
                    }
                }
                menuAdapter = new MenuAdapter(menuListItems,getContext());
                menuRecyclerView.setAdapter(menuAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError){
                Toast.makeText(getContext(),"Can't get",Toast.LENGTH_LONG).show();
            }
        });
    }
    private void loadHotDrinks() {
        getHashMap();
        databaseReference = FirebaseUtil.getDatabase().getReference(hotel).child("Menu");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                menuListItems.clear();
                for(DataSnapshot item: dataSnapshot.getChildren()){
                    String name = item.getKey();
                    int price = Integer.parseInt(dataSnapshot.child(name).child("Price").getValue().toString());
                    String desc= dataSnapshot.child(name).child("Type").getValue().toString();
                    String type= dataSnapshot.child(name).child("Category").getValue().toString();
                    Integer amount=0;
                    if(orderAmount.containsKey(name))
                        amount= orderAmount.get(name);
                    String url="";
                    if(dataSnapshot.child(name).child("Url").exists())
                        url=dataSnapshot.child(name).child("Url").getValue().toString();
                    if(type.equals("Hot Drinks")) {
                        MenuListItem menuListItem = new MenuListItem(name,desc,price,type,amount,url);
                        menuListItems.add(menuListItem);
                    }
                }
                menuAdapter = new MenuAdapter(menuListItems,getContext());
                menuRecyclerView.setAdapter(menuAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError){
                Toast.makeText(getContext(),"Can't get",Toast.LENGTH_LONG).show();
            }
        });
    }
    private void loadDals() {
        getHashMap();
        databaseReference = FirebaseUtil.getDatabase().getReference(hotel).child("Menu");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                menuListItems.clear();
                for(DataSnapshot item: dataSnapshot.getChildren()){
                    String name = item.getKey();
                    int price = Integer.parseInt(dataSnapshot.child(name).child("Price").getValue().toString());
                    String desc= dataSnapshot.child(name).child("Type").getValue().toString();
                    String type= dataSnapshot.child(name).child("Category").getValue().toString();
                    Integer amount=0;
                    if(orderAmount.containsKey(name))
                        amount= orderAmount.get(name);
                    String url="";
                    if(dataSnapshot.child(name).child("Url").exists())
                        url=dataSnapshot.child(name).child("Url").getValue().toString();
                    if(type.equals("Dal")) {
                        MenuListItem menuListItem = new MenuListItem(name,desc,price,type,amount,url);
                        menuListItems.add(menuListItem);
                    }
                }
                menuAdapter = new MenuAdapter(menuListItems,getContext());
                menuRecyclerView.setAdapter(menuAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError){
                Toast.makeText(getContext(),"Can't get",Toast.LENGTH_LONG).show();
            }
        });
    }
    private void loadBreakfast() {
        getHashMap();
        databaseReference = FirebaseUtil.getDatabase().getReference(hotel).child("Menu");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                menuListItems.clear();
                for(DataSnapshot item: dataSnapshot.getChildren()){
                    String name = item.getKey();
                    int price = Integer.parseInt(dataSnapshot.child(name).child("Price").getValue().toString());
                    String desc= dataSnapshot.child(name).child("Type").getValue().toString();
                    String type= dataSnapshot.child(name).child("Category").getValue().toString();
                    Integer amount=0;
                    if(orderAmount.containsKey(name))
                        amount= orderAmount.get(name);
                    String url="";
                    if(dataSnapshot.child(name).child("Url").exists())
                        url=dataSnapshot.child(name).child("Url").getValue().toString();
                    if(type.equals("Breakfast")) {
                        MenuListItem menuListItem = new MenuListItem(name, desc, price, type,amount,url);
                        menuListItems.add(menuListItem);
                    }
                }
                menuAdapter = new MenuAdapter(menuListItems,getContext());
                menuRecyclerView.setAdapter(menuAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError){
                Toast.makeText(getContext(),"Can't get",Toast.LENGTH_LONG).show();
            }
        });
    }
    private void loadVegOnly(){
        getHashMap();
        databaseReference = FirebaseUtil.getDatabase().getReference(hotel).child("Menu");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                menuListItems.clear();
                for(DataSnapshot item: dataSnapshot.getChildren()){
                    String name = item.getKey();
                    int price = Integer.parseInt(dataSnapshot.child(name).child("Price").getValue().toString());
                    String desc= dataSnapshot.child(name).child("Type").getValue().toString();
                    String type= dataSnapshot.child(name).child("Category").getValue().toString();
                    Integer amount=0;
                    if(orderAmount.containsKey(name))
                        amount= orderAmount.get(name);
                    String url="";
                    if(dataSnapshot.child(name).child("Url").exists())
                        url=dataSnapshot.child(name).child("Url").getValue().toString();
                    if(desc.equals("Veg")) {
                        MenuListItem menuListItem = new MenuListItem(name, desc, price, type,amount,url);
                        menuListItems.add(menuListItem);
                    }
                }
                menuAdapter = new MenuAdapter(menuListItems,getContext());
                menuRecyclerView.setAdapter(menuAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(),"Can't get",Toast.LENGTH_LONG).show();
            }
        });
    }
}
