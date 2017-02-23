package com.tyson.pizza.pizza;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by Tyson Macdonald on 3/22/2016.
 * Class for getting system permission for Marshmallow and higher
 * and performing the requested action.
 *
 * The method setRequestPermissionsResult must be called from the Overided method
 * onRequestPermissionsResult in the ACTIVITY.
 */



public abstract class SystemPermissions {

    public interface Permission_Request_Type {
        int READ_CONTACTS = 0;
        int WRITE_CONTACTS = 1;
        int READ_CALL_LOG = 2;
        int READ_SMS = 3;
        int GET_ACCOUNTS = 4;
        int READ_EXTERNAL_STORAGE = 5;
        int WRITE_EXTERNAL_STORAGE = 6;
        int FINE_LOCATION = 7;
    }

    private Activity mActivity;
    private String mPermissionDialogTitle;
    private String mPermissionDialogBody;
    private String[] mPermissions;
    private int[] mRequestTypes;
    private int mRequestCode;


    public SystemPermissions(Activity activity, int requestType){

        mActivity = activity;
        mRequestTypes = new int[1];
        mRequestTypes[0] = requestType;
        mPermissions = new String[1];
        mRequestCode = requestType;


        switch (requestType){
            case Permission_Request_Type.READ_CONTACTS:
                mPermissions[0] = Manifest.permission.READ_CONTACTS;
                break;

            case Permission_Request_Type.WRITE_CONTACTS:
                mPermissions[0] = Manifest.permission.WRITE_CONTACTS;
                break;

            case Permission_Request_Type.READ_CALL_LOG:
                mPermissions[0] = Manifest.permission.READ_CALL_LOG;
                break;

            case Permission_Request_Type.READ_SMS:
                mPermissions[0] = Manifest.permission.READ_SMS;
                break;

            case Permission_Request_Type.GET_ACCOUNTS:
                mPermissions[0] = Manifest.permission.GET_ACCOUNTS;
                break;

            case Permission_Request_Type.READ_EXTERNAL_STORAGE:
                mPermissions[0] = Manifest.permission.READ_EXTERNAL_STORAGE;
                break;

            case Permission_Request_Type.WRITE_EXTERNAL_STORAGE:
                mPermissions[0] = Manifest.permission.WRITE_EXTERNAL_STORAGE;
                break;

            case Permission_Request_Type.FINE_LOCATION:
                mPermissions[0] = Manifest.permission.ACCESS_FINE_LOCATION;
                break;

            default:
        }
    }

    public SystemPermissions(Activity activity, int[] requestTypes){

        mActivity = activity;
        mRequestTypes = requestTypes;

        // create a request code from the list of requestTypes
        String temp = "";
        for(int type : requestTypes){
            temp = String.format("%s%d", temp, type);
        }

        // the request code is the concatenated list of request types
        mRequestCode = Integer.parseInt(temp);

        mPermissions = setPermissionsStringArray(requestTypes);
    }

    private static String[] setPermissionsStringArray(int[] requestTypes) {

        String[] permissions = new String[requestTypes.length];

        int i = 0;

        for(int requestType : requestTypes) {

            switch (requestType) {
                case Permission_Request_Type.READ_CONTACTS:
                    permissions[i] = Manifest.permission.READ_CONTACTS;
                    break;

                case Permission_Request_Type.WRITE_CONTACTS:
                    permissions[i] = Manifest.permission.WRITE_CONTACTS;
                    break;

                case Permission_Request_Type.READ_CALL_LOG:
                    permissions[i] = Manifest.permission.READ_CALL_LOG;
                    break;

                case Permission_Request_Type.READ_SMS:
                    permissions[i] = Manifest.permission.READ_SMS;
                    break;

                case Permission_Request_Type.GET_ACCOUNTS:
                    permissions[i] = Manifest.permission.GET_ACCOUNTS;
                    break;

                case Permission_Request_Type.READ_EXTERNAL_STORAGE:
                    permissions[i] = Manifest.permission.READ_EXTERNAL_STORAGE;
                    break;

                case Permission_Request_Type.WRITE_EXTERNAL_STORAGE:
                    permissions[i] = Manifest.permission.WRITE_EXTERNAL_STORAGE;
                    break;


                case Permission_Request_Type.FINE_LOCATION:
                    permissions[i] = Manifest.permission.ACCESS_FINE_LOCATION;
                    break;
                default:
            }

            i++;
        }

        return permissions;
    }

    /**
     * Main class method for requesting permission from the system.
     * Performs the action immediately if all permissions are already granted.
     * @param permissionDialogTitle String title for the permission dialog
     * @param permissionDialogBody String body for the permission dialog
     */
    public void requestPermissionFromUserAndPerformAction(String permissionDialogTitle,
                                          String permissionDialogBody){

        if(hasMarshmallow()) {

            mPermissionDialogTitle = permissionDialogTitle;
            mPermissionDialogBody = permissionDialogBody;

            boolean permissionIsGranted = true;
            boolean requestPermission = false;


            // cycle through the full list of requested permissions to set the user dialogs
            for(String manifest_permission : mPermissions) {

                // if the permission is already granted, perform the action
                if (ContextCompat.checkSelfPermission(mActivity, manifest_permission)
                        == PackageManager.PERMISSION_GRANTED) {

                    // do nothing
                    // persisting the state of permissionIsGranted

                } else {
                    // if not granted, request permission

                    permissionIsGranted = false;

                    // Should we show an explanation?
                    //TODO make the dialog strings more representative of multiple permissions
                    if (mPermissionDialogBody != null && !mPermissionDialogBody.isEmpty() &&
                            ActivityCompat.shouldShowRequestPermissionRationale(mActivity,
                            manifest_permission)) {

                        contactPermissionExplanation();

                        // Show an explanation to the user *asynchronously* -- don't block
                        // this thread waiting for the user's response! After the user
                        // sees the explanation, try again to request the permission.

                    } else {
                        // we should submit a permission request at the end.
                        requestPermission = true;
                    }
                }
            }

            // if ALL permission are granted, try running the action
            if(permissionIsGranted) {
                // perform the action
                action();
            }

            if(requestPermission){
                // No explanation needed, we can request the permission.
                // Its probably OK if we request permissions of the system that are already granted
                requestContactsReadPermission();
            }
        }else {

            // if we don't have Marshmallow permissions, go ahead and perform the action
            action();
        }
    }

    private static boolean hasMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    /**
     * Performs the given action, but only if the system has at least one specified permission granted
     */
    public void performWithSomePermission(){


        boolean aPermissionIsGranted = false;

        // check if we need to get permission
        if (hasMarshmallow()) {

            // cycle through the full list of requested permissions to set the user dialogs
            for (String manifest_permission : mPermissions) {

                // if the permission is granted, set the flag
                if (ContextCompat.checkSelfPermission(mActivity, manifest_permission)
                                == PackageManager.PERMISSION_GRANTED) {

                    aPermissionIsGranted = true;
                }
            }

            // if ANY permission is granted, try running the action
            if(aPermissionIsGranted) {
                // perform the action
                action();
            }
        }else{

            // we don't need special permission, so perform the action
            action();
        }
    }

    /**
     * Performs the given action, but only if the system has all specified permission granted
     */
    public void performWithAllPermission(){

        boolean permissionIsGranted = true;

        // check if we need to get permission
        if (hasMarshmallow()) {

            // cycle through the full list of requested permissions to set the user dialogs
            for (String manifest_permission : mPermissions) {

                // cycle through the granted permissions, and persist any failure to grant
                permissionIsGranted =
                        (ContextCompat.checkSelfPermission(mActivity, manifest_permission)
                        == PackageManager.PERMISSION_GRANTED) && permissionIsGranted;

            }

            // if ALL permission are granted, try running the action
            if(permissionIsGranted) {
                // perform the action
                action();
            }
        }else{

            // we don't need special permission, so perform the action
            action();
        }
    }


    // Create dialog for to explain to the user why we need to read the contacts list
    private void contactPermissionExplanation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(mPermissionDialogTitle);

        builder.setMessage(mPermissionDialogBody);

        // Set up the buttons
        builder.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();

                requestContactsReadPermission();
            }
        });

        builder.show();
    }


    private void requestContactsReadPermission(){
        // No explanation needed, we can request the permission.

        ActivityCompat.requestPermissions(mActivity, mPermissions, mRequestCode);

        // READ_CONTACTS is an
        // app-defined int constant. The callback method gets the
        // result of the request.
    }

    /**
     * This method must be called by the Orverided method onRequestPermissionsResult that is
     * defined in the host ACTIVITY.  This is how the app is notified about permission grating
     * The arguments for onRequestPermissionsResult are the same as here.
     * @param requestCode The integer code defined in Permision_Type
     * @param permissions The String permission
     * @param grantResults Array containing the permission granted flag.
     */
    public void setRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {

        if(requestCode == mRequestCode){

            boolean permissionIsGranted = true;

            if(grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                // permission was granted! Do the permission-related task.
                action();

            }else if(grantResults.length >= 2){

                // cycle through the full list of requested permissions to set the user dialogs
                for (int grantedResult : grantResults) {

                    // persist any failure to grant
                    permissionIsGranted = (grantedResult == PackageManager.PERMISSION_GRANTED) &&
                                    permissionIsGranted;
                }

                // if ALL permission are granted, try running the action
                if(permissionIsGranted) {
                    // perform the action
                    action();
                }
            }
        // Otherwise, permission denied!
        // DO NOTHING
        }

        // signals crossed??
    }

    // the action to be performed when the permission is granted
    abstract public void action();

    public interface SystemPermissionsInterfaceCallback{
        void setLiveInstance(SystemPermissions systemPermissions);
    }


    /**
     * Method for performing the Action if permission is granted, but won't ask for permission
     */
    public static boolean performActionIfPermissionGranted(Context context, int[] requestTypes) {

        boolean permissionIsGranted = true;

        String[] permissions = setPermissionsStringArray(requestTypes);

        if (hasMarshmallow()) {


            // cycle through the full list of requested permissions to set the user dialogs
            for (String manifest_permission : permissions) {

                // if the permission is already granted, perform the action
                if (ContextCompat.checkSelfPermission(context, manifest_permission)
                        != PackageManager.PERMISSION_GRANTED) {

                    permissionIsGranted = false;

                }
            }
        }

        return permissionIsGranted;
    }
}
