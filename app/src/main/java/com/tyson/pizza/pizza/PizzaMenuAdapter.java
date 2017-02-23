package com.tyson.pizza.pizza;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Tyson on 2/22/2017.
 * Adapter to inflate pizza menu items in to the menu and manage touch events with the list.
 *
 * This adapter takes in a list of PizzaMenuItem
 */

class PizzaMenuAdapter extends RecyclerView.Adapter<PizzaMenuAdapter.PizzaItemViewHolder> {

    private ArrayList<PizzaMenuItem> mList;
    private PizzaMenuItemClickListener mClickListener = null;
    private AssetManager mAssetManager;

    // define the view holder, complete with click listeners
    class PizzaItemViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener{

        TextView title;
        TextView description;
        TextView price;
        TextView quantity;
        ImageView image;

        PizzaItemViewHolder(View itemView) {
            super(itemView);

            title = (TextView) itemView.findViewById(R.id.title);
            description = (TextView) itemView.findViewById(R.id.description);
            price = (TextView) itemView.findViewById(R.id.price);
            quantity = (TextView) itemView.findViewById(R.id.quantity_text);
            image = (ImageView) itemView.findViewById(R.id.menu_item_image);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }


        // using click events to add items to the order
        @Override
        public void onClick(View v) {

            Log.d("Adapter", "onClick");

            // add item to order total
            mList.get(getAdapterPosition()).quantity++;
            notifyItemChanged(getAdapterPosition());

            if (mClickListener != null) {
                mClickListener.onItemClick(getAdapterPosition(),
                        mList.get(getAdapterPosition()));
            }
        }

        // using longClick events to remove items from the order
        @Override
        public boolean onLongClick(View v) {

            // remove item to order total
            if(mList.get(getAdapterPosition()).quantity > 0) {
                mList.get(getAdapterPosition()).quantity--;
            }

            notifyItemChanged(getAdapterPosition());

            if (mClickListener != null) {
                mClickListener.onItemLongClick(getAdapterPosition(),
                        mList.get(getAdapterPosition()));
            }
            return true;
        }
    }


    // Constructor for the adapter
    PizzaMenuAdapter(Context context, ArrayList<PizzaMenuItem> list){
        mAssetManager = context.getAssets();
        mList = list;
    }


    // method to set a listener from the activity
    void setListener(PizzaMenuItemClickListener listener){
        mClickListener = listener;
    }



    @Override
    public PizzaItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.menu_list_item , parent, false);

        return new PizzaItemViewHolder(view);
    }


    // bind data to the view holder
    // adding a couple other touch events: the pizza image and the order count.
    // Clicking the pizza image should open a detail view.
    // Clicking the order number will decrement the order
    @Override
    public void onBindViewHolder(PizzaItemViewHolder holder, final int position) {

        holder.title.setText(mList.get(position).title);
        holder.description.setText(mList.get(position).desctription);
        holder.price.setText(mList.get(position).getPrice());

        holder.quantity.setText(String.format(Locale.US, "%d",mList.get(position).quantity));

        int textColor;
        if(mList.get(position).quantity > 0){
            textColor = android.R.color.primary_text_light;
        }else {
            textColor = android.R.color.darker_gray;

        }
        holder.quantity.setTextColor(ContextCompat
                .getColor(holder.quantity.getContext(), textColor));

        // load image
        try {
            // get input stream
            InputStream ims = mAssetManager.open(mList.get(position).imageResource);
            // load image as Drawable
            Drawable d = Drawable.createFromStream(ims, null);
            // set image to ImageView
            holder.image.setImageDrawable(d);
        }
        catch(IOException ex) {
            Log.e("Pizza Adapter", "Could not load image from assets");
        }


        // decrement the order when the quantity is touched
        holder.quantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // add item to order total
                if(mList.get(position).quantity > 0) {
                    mList.get(position).quantity--;
                }

                notifyItemChanged(position);

                if (mClickListener != null) {
                    mClickListener.onItemLongClick(position, mList.get(position));
                }

            }
        });


        // open the detail view for the pizza
        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mClickListener != null) {
                    mClickListener.onItemImageClick(position, mList.get(position), v);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }


    // interface for the click listener
    interface PizzaMenuItemClickListener{
        void onItemClick(int position, PizzaMenuItem item);
        void onItemLongClick(int position, PizzaMenuItem item);
        void onItemImageClick(int position, PizzaMenuItem item, View v);
    }
}
