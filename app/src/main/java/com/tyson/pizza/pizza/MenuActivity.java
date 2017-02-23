package com.tyson.pizza.pizza;

import android.databinding.DataBindingUtil;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.tyson.pizza.pizza.databinding.ActivityMenuBinding;
import com.tyson.pizza.pizza.services.FetchAddressIntentService;

import java.util.ArrayList;

/**
 * Created by Tyson on 2/22/2017.
 *
 * This activity defines a basic menu interface.
 *
 * A list of pizza options are displayed in a recyclerView, and a listener for that list
 * of items indicate whether the user has selected a pizza from the list.
 *
 * Selected pizza items are summarized and totaled in the bottom sheet of the activity.
 * This bottom sheet also displays the approximate address of the user,
 * and provides the option to purchase the selected food items.
 *
 * This activity also uses a ViewFlipper to switch between a loading view, the content view,
 * and an error view. (Though the error  view currently isn't used.)
 */

public class MenuActivity extends AppCompatActivity implements
        PizzaMenuAdapter.PizzaMenuItemClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status>{

    private final static int FLIPPER_LOADING = 1;
    private final static int FLIPPER_CONTENT = 0;
    private final static int FLIPPER_ERROR = 2;
    private static final String TAG = "MenuActivity";

    private MenuActivityModel mViewModel;
    private RecyclerView mRecyclerView;
    private ViewFlipper mViewFlipper;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private AddressResultReceiver mResultReceiver;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //instead of setContentView(), we use the data binding equivalent
        ActivityMenuBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_menu);

        // set the model for the databinding
        mViewModel = new MenuActivityModel(this);
        binding.setMenu(mViewModel);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        mViewFlipper = (ViewFlipper) findViewById(R.id.content_flipper);
        mViewFlipper.setDisplayedChild(FLIPPER_LOADING);

        // set the animation for the viewFlipper; how new views come onto the screen
        mViewFlipper.setInAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
        mViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));


        // we don't use the FAB now, so it's hidden
        // I'll later use it to place the order
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


            }
        });
        fab.hide();

        // setting a behavior for the summary textView
        ((TextView) findViewById(R.id.pizza_order_summary))
                .setMovementMethod(new ScrollingMovementMethod());

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        //assign layout manager
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        connectGoogleApiClient();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            Snackbar.make(findViewById(R.id.coordinated_order_summary), R.string.credit,
                    Snackbar.LENGTH_LONG).setAction("Action", null).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        // connecting to google API
        mGoogleApiClient.connect();

        super.onStart();

        // initialize the menu data to display
        delaySetData();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }


    private void delaySetData() {

        (new Handler()).postDelayed(new Runnable() {

            public void run() {
                setMenuData();

            }
        }, 2000); //ms delay
    }


    // display the menu data, as defined in the strings file and the assets directory
    private void setMenuData() {

        // retrieve menu
        ArrayList<PizzaMenuItem> list = new ArrayList<>();

        int i;
        for (i = 0; i < 6; i++) {

            // fill out the pizza data, alternating images for the pizza
            list.add(new PizzaMenuItem(getResources().getStringArray(R.array.pizza_names)[i],
                    getResources().getStringArray(R.array.pizza_images)[i]));
        }


        // set the adapter and recyclerView with the menu data
        PizzaMenuAdapter adapter = new PizzaMenuAdapter(this, list);
        adapter.setListener(this);

        mRecyclerView.setAdapter(adapter);

        mViewModel.setPizzaList(list);


        // show the list view
        mViewFlipper.setDisplayedChild(FLIPPER_CONTENT);

    }

    @Override
    public void onItemClick(int position, PizzaMenuItem item) {
        // increment pizza order

        mViewModel.updateOrderItem(position, item);
    }

    @Override
    public void onItemLongClick(int position, PizzaMenuItem item) {
        // decrement pizza order

        mViewModel.updateOrderItem(position, item);
    }

    @Override
    public void onItemImageClick(int position, PizzaMenuItem item, View v) {

        // open pizza detail
        Snackbar.make(findViewById(R.id.coordinated_order_summary), R.string.detail_view_message,
                Snackbar.LENGTH_SHORT).setAction("Action", null).show();
    }


    public void submitOrder(View view){

        Snackbar.make(findViewById(R.id.coordinated_order_summary), R.string.order_submitted,
                Snackbar.LENGTH_SHORT).setAction("Action", null).show();
    }


    /**
     *  Google API interactions for fetching the local address,
     *  including use of the system permissions framework
     */

    protected synchronized void connectGoogleApiClient() {

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected: request permission");

        //For Android M and higher, clear the contacts reading system permission
        SystemPermissions locationPermissions = new SystemPermissions(this,
                new int[]{SystemPermissions.Permission_Request_Type.FINE_LOCATION}) {

            @Override
            public void action() {

                // location fetching
                setInitialLocation();
            }
        };

        // make sure to ask permissions for this activity
        locationPermissions.requestPermissionFromUserAndPerformAction(
                getString(R.string.location_permission_title),
                getString(R.string.location_permission_description));
    }


    // grab the cached location from the system
    private void setInitialLocation(){

        try {
            // first we need to get the location
            Log.d(TAG, "getting location");

            // see if there's a recent location on record
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            // get the address associated with the current location
            getAddress(mLastLocation);


        } catch (SecurityException e) {
            Log.e(TAG, "location permission not granted");
            e.printStackTrace();
        }
    }


    // update the location when the user clicks on the address
    public void updateLocation(View v){

        // request an updated location
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);


        try{
            LocationServices.FusedLocationApi
                    .requestLocationUpdates(mGoogleApiClient, locationRequest, new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {

                            // remember location
                            mLastLocation = location;

                            // get the address associated with the current location
                            getAddress(mLastLocation);
                        }
                    });

        } catch (SecurityException e) {
            Log.e(TAG, "location permission not granted");
            e.printStackTrace();
        }
    }


    private void getAddress(Location location) {
        // Determine whether a Geocoder is available.
        if (Geocoder.isPresent()) {

            // initialize the result receiver so we can retreive the address from the service
            if(mResultReceiver == null) {
                mResultReceiver = new AddressResultReceiver(new Handler());
            }

            startService(FetchAddressIntentService.getIntent(this, location, mResultReceiver));
        }
    }


    class AddressResultReceiver extends ResultReceiver {
        private AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // save and display address.
            if (resultCode == FetchAddressIntentService.Constants.SUCCESS_RESULT) {

                // Display the address string
                // or an error message sent from the intent service.
                mViewModel.setLocation((Address)resultData.getParcelable(
                        FetchAddressIntentService.Constants.RESULT_DATA_KEY));
            }else {

                // try again
                // get the address associated with the current location
                getAddress(mLastLocation);
            }

        }
    }
    @Override
    public void onResult(@NonNull Status status) {

    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this,"onConnectionSuspended",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this,"onConnectionFailed",Toast.LENGTH_SHORT).show();
    }
}
