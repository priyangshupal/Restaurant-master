package com.project.restaurant.list_items;

public class CartListItem {

    private String cart_dish_name;
    private String cart_dish_price;
    private String cart_dish_amount;

    public CartListItem(String cart_dish_name, String cart_dish_price, String cart_dish_amount) {
        this.cart_dish_name = cart_dish_name;
        this.cart_dish_price = cart_dish_price;
        this.cart_dish_amount = cart_dish_amount;
    }

    public String getCart_dish_name() {
        return cart_dish_name;
    }

    public String getCart_dish_price() {
        return cart_dish_price;
    }

    public String getCart_dish_amount() {return cart_dish_amount;}
}
