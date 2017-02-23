package com.tyson.pizza.pizza;

import java.util.Locale;

/**
 * Created by Tyson on 2/22/2017.
 * Data structure for pizza menu items
 */

class PizzaMenuItem {
    String title = "";
    String desctription = "The Best Pizza";
    float price = 10.0f;
    String imageResource = "";

    int quantity = 0;

    PizzaMenuItem(String title, String imageResource){
        this.title = title;
        this.imageResource = imageResource;
    }

    String getPrice(){

        return String.format(Locale.US, "$%.2f", price);
    }

}
