package com.project.restaurant.list_items;

public class MenuListItem {

    private String dish_name;
    private String dish_desc;
    private int dish_price;
    private Integer dish_amount;
    private String dish_url;
    private String dish_type; //Starters OR Main course OR Dessert



    public MenuListItem(String dish_name, String dish_desc, int dish_price, String dish_type, Integer dish_amount, String dish_url) {
        this.dish_name = dish_name;
        this.dish_desc = dish_desc;
        this.dish_price= dish_price;
        this.dish_type = dish_type;
        this.dish_amount=dish_amount;
        this.dish_url=dish_url;
    }

    public String getDish_name() {
        return dish_name;
    }

    public String getDish_desc() {
        return dish_desc;
    }

    public int getDish_price(){ return dish_price;}

    public Integer getDish_amount(){ return dish_amount; }

    public String getDish_type() {
        return dish_type;
    }

    public String getDish_url() { return dish_url; }
}
