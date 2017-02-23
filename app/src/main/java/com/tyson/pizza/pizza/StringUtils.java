package com.tyson.pizza.pizza;

import android.location.Address;
import android.text.TextUtils;

import java.util.ArrayList;

/**
 * Created by Tyson Macdonald on 2/22/2017.
 *
 * Static utils for dealing with address strings
 */

public class StringUtils {

    private StringUtils(){}

    public static String getMultiLineAddress(Address address){

        if(address != null) {
            ArrayList<String> addressFragments = new ArrayList<>();

            // Fetch the address lines using getAddressLine,
            // join them, and send them to the thread.
            for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
            }

            return TextUtils.join(System.getProperty("line.separator"), addressFragments);
        }

        return "";
    }

    public static String getSingleLineAddress(Address address){

        if(address != null) {
            ArrayList<String> addressFragments = new ArrayList<>();

            // Fetch the address lines using getAddressLine,
            // join them, and send them to the thread.
            for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
            }

            return TextUtils.join(",", addressFragments);
        }

        return "";
    }
}
