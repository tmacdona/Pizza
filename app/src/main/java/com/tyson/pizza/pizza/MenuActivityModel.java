package com.tyson.pizza.pizza;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Tyson on 2/22/2017.
 *
 * Model for the menu activity using Android DataBinding.
 *
 * This is where we keep all the instructions for modifying the UI based on
 * activity state changes for an improved MVP structure.
 */

public class MenuActivityModel extends BaseObservable {

    private ArrayList<PizzaMenuItem> pizzaList = null;
    private String address;
    private String dollarTotal;
    private String pizzaOrderSummary;
    private boolean visibleDollarTotal;
    private Drawable pizzaOrderSummaryImage;
    private Context context;


    public MenuActivityModel(Context context){
        this.context = context;
        setVisibleDollarTotal(false);
    }

    // set the pizza list for later reference
    void setPizzaList(ArrayList<PizzaMenuItem> list){
        pizzaList = list;
    }

    // primary method for updating the item counts as the user interacts with the recycler view
    void updateOrderItem(int listPosition, PizzaMenuItem item){

        ArrayList<String> orderImageList = new ArrayList<>(pizzaList.size());

        // update the quantity in the list
        pizzaList.get(listPosition).quantity = item.quantity;

        float dollarTotalValue = 0;

        StringBuilder builder = new StringBuilder();

        int i = 0;

        // count all the pizzas that are ordered, keeping track of cost
        // as well as which image assets are used
        for(PizzaMenuItem pizza : pizzaList){

            if(pizza.quantity > 0){

                dollarTotalValue += pizza.price * pizza.quantity;

                if(i >0){
                    builder.append("\n");
                }

                builder.append(String.format(Locale.US, "%d X %s",
                        pizza.quantity, pizza.title));

                i++;

                orderImageList.add(pizza.imageResource);
            }
        }


        // set the order summary text
        setPizzaOrderSummary(builder.toString());


        // set the dollar amount string
        dollarTotal = String.format(Locale.US, "$%.2f", dollarTotalValue);
        notifyPropertyChanged(BR.dollarTotal);

        // set the visibility of the dollar total
        setVisibleDollarTotal(dollarTotalValue > 0);


        // set the combined bitmap for the pizza images
        Bitmap bitmap = BitmapCollage.getMergedImageFromAssetList(context, orderImageList,
                context.getResources().getDimensionPixelSize(R.dimen.image_thumb_size));

        setPizzaOrderSummaryImage(new BitmapDrawable(context.getResources(), bitmap));
    }




    // handle the setting of the image resource for the order summary
    public void setPizzaOrderSummaryImage(Drawable pizzaOrderSummaryImage){
        this.pizzaOrderSummaryImage = pizzaOrderSummaryImage;
        notifyPropertyChanged(BR.pizzaOrderSummaryImage);
    }

    @Bindable
    public Drawable getPizzaOrderSummaryImage(){
        return pizzaOrderSummaryImage;
    }



    // handle the setting of the textview for the total cost
    // don't display if zero
    public void setVisibleDollarTotal(boolean visibleDollarTotal){
        this.visibleDollarTotal = visibleDollarTotal;
        notifyPropertyChanged(BR.visibleDollarTotal);
    }

    @Bindable
    public boolean getVisibleDollarTotal(){
        return visibleDollarTotal;
    }


    public void setLocation(Address location){

        setAddress(StringUtils.getSingleLineAddress(location));
    }


    // handle the setting of the address TextView
    public void setAddress(String address){
        this.address = address;
        notifyPropertyChanged(BR.address);
    }

    @Bindable
    public String getAddress(){
        return address;
    }


    // handle the setting of the dollar total TextView
    public void setDollarTotal(String dollarTotal){
        this.dollarTotal = dollarTotal;
        notifyPropertyChanged(BR.dollarTotal);
    }

    @Bindable
    public String getDollarTotal(){
        return dollarTotal;
    }


    // handle the setting of the dollar total TextView
    public void setPizzaOrderSummary(String pizzaOrderSummary){
        this.pizzaOrderSummary = pizzaOrderSummary;
        notifyPropertyChanged(BR.pizzaOrderSummary);
    }

    @Bindable
    public String getPizzaOrderSummary(){
        return pizzaOrderSummary;
    }
}
