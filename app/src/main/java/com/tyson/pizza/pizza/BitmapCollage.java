package com.tyson.pizza.pizza;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by Tyson on 2/22/2017.
 *
 * Static utility for stitching several bitmaps into one image.
 * A section of each image (half or quarter) is cut from each bitmap and added to the
 * corresponding section of the source canvas.
 *
 * The quadrants are defined in clockwise order from the top-left
 *
 *  1 | 2
 *  -----
 *  4 | 3
 *
 *  When there are only 2 images, the top and bottom quadrants are combined
 */

public class BitmapCollage {
    
    
    
    public static Bitmap getMergedImageFromAssetList(Context context, ArrayList<String> imageList,
                                                     int size){

        ArrayList<Bitmap> bitmaps = new ArrayList<>(imageList.size());

        for(String asset : imageList) {

            // load image
            try {
                // get input stream
                InputStream inputStream = context.getAssets().open(asset);

                // load bitmap into array list
                bitmaps.add(BitmapFactory.decodeStream(inputStream));
            }
            catch(IOException ex) {
                Log.e("BitmapCollage", "Could not load image from assets");
            }
        }

        return getMergedImage(size, bitmaps);
    }

    public static Bitmap getMergedImage(int size, ArrayList<Bitmap> imageList){


        if(imageList == null || imageList.isEmpty()){
            return null;
        }

        // return the first element if the list only has one element
        if(imageList.size() == 1){
            return imageList.get(0);
        }

        Bitmap result = Bitmap.createBitmap(size, size, imageList.get(0).getConfig());

        Canvas canvas = new Canvas(result);

        int i = 0;
        Rect sourceRect = null;
        Rect destinationRect = null;
        int listSize = imageList.size();


        // if more than 2 images, we divide our lower images by half again
        int divider = listSize > 2 ? 2 : 1;


        // cycle through each of the bitmaps, crop them, and add them to the canvas
        for(Bitmap bitmap : imageList){

            // define the source and destination cuts for each bitmap (according to index) in the list
            switch (i){
                case 0:
                    // define the segments of the original bitmaps to stitch together
                    // rectangles are defined by the coordinates left, top, right, bottom
                    sourceRect = new Rect(0,0,
                            imageList.get(i).getWidth()/2, imageList.get(i).getHeight());

                    destinationRect = new Rect(0,0,result.getWidth()/2, result.getHeight());
                    break;

                case 1:
                    // variable depending on the number of bitmaps
                    sourceRect = new Rect(imageList.get(i).getWidth()/2, 0,
                            imageList.get(i).getWidth(), imageList.get(i).getHeight()/divider);

                    destinationRect = new Rect(result.getWidth()/2, 0,
                            result.getWidth(), result.getHeight()/divider);
                    break;

                case 2:
                    sourceRect = new Rect(imageList.get(i).getWidth()/2, imageList.get(i).getHeight()/2,
                            imageList.get(i).getWidth(), imageList.get(i).getHeight());

                    destinationRect = new Rect(result.getWidth()/2, result.getHeight()/2,
                            result.getWidth(), result.getHeight());
                    break;

                case 3:
                    sourceRect = new Rect(0, imageList.get(i).getHeight()/2,
                            imageList.get(i).getWidth()/2, imageList.get(i).getHeight());

                    destinationRect = new Rect(0, result.getHeight()/2,
                            result.getWidth()/2, result.getHeight());
                    break;

                // we're just not going to show more than 4 images
            }

            // apply the bitmap to the canvas
            canvas.drawBitmap(bitmap, sourceRect, destinationRect, null);

            // limit the number of bitmaps to 4
            if(i++ > 4)
                break;
        }

        return result;
    }
}
