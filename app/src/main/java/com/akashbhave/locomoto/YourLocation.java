package com.akashbhave.locomoto;

import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import io.cloudboost.CloudException;
import io.cloudboost.CloudGeoPoint;
import io.cloudboost.CloudObject;
import io.cloudboost.CloudObjectArrayCallback;
import io.cloudboost.CloudObjectCallback;
import io.cloudboost.CloudQuery;

public class YourLocation extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;

    LocationManager locationManager;
    String provider;
    // For saving the user's location
    Location usersCurrentLocation = new Location(provider);

    TextView requestStatusView;
    Button requestButton;

    // If user's request is still wanted
    boolean requestActive = false;

    SharedPreferences sharedPreferences;

    String downloadTaskType = "";

    public class DownloadTask extends AsyncTask<String, Void, Void> {

        boolean resultGood = false;


        @Override
        protected Void doInBackground(String... params) {
            if (downloadTaskType.equals("request")) {
                try {
                    final CloudGeoPoint riderLocation = new CloudGeoPoint(usersCurrentLocation.getLatitude(), usersCurrentLocation.getLongitude());
                    CloudObject requestsObject = new CloudObject("Requests");
                    requestsObject.set("reqUsername", sharedPreferences.getString("currentUser", ""));
                    requestsObject.set("reqLocation", riderLocation);
                    requestsObject.save(new CloudObjectCallback() {
                        @Override
                        public void done(CloudObject x, CloudException t) throws CloudException {
                            Log.i("Rider's Location", "Saved");
                            Log.i("Rider Requester", "Saved");
                            resultGood = true;
                        }
                    });
                } catch (CloudException e) {
                    e.printStackTrace();
                }


            } else if (downloadTaskType.equals("cancelRequest")) {
                try {
                    // See who gave the request
                    CloudQuery userQuery = new CloudQuery("Requests");
                    userQuery.equalTo("reqUsername", sharedPreferences.getString("currentUser", ""));
                    userQuery.find(new CloudObjectArrayCallback() {
                        @Override
                        public void done(CloudObject[] x, CloudException t) throws CloudException {
                            if (x != null) {
                                for (CloudObject object : x) {
                                    object.delete(new CloudObjectCallback() {
                                        @Override
                                        public void done(CloudObject x, CloudException t) throws CloudException {
                                            if (x != null) {
                                                Log.i("Request", "Deleted");
                                                resultGood = true;
                                            } else {
                                                t.printStackTrace();
                                            }
                                        }
                                    });
                                }
                            } else {
                                t.printStackTrace();
                            }
                        }
                    });
                } catch (CloudException e) {
                    e.printStackTrace();
                }


            } else if (downloadTaskType.equals("saveLocation")) {
                try {
                    final CloudGeoPoint riderLocation = new CloudGeoPoint(usersCurrentLocation.getLatitude(), usersCurrentLocation.getLongitude());
                    // Puts the users location into its appropriate column
                    CloudQuery userQuery = new CloudQuery("Requests");
                    userQuery.equalTo("reqUsername", sharedPreferences.getString("currentUser", ""));
                    userQuery.find(new CloudObjectArrayCallback() {
                        @Override
                        public void done(CloudObject[] x, CloudException t) throws CloudException {
                            if (x != null) {
                                for (CloudObject object : x) {
                                    object.set("reqLocation", riderLocation);
                                    object.save(new CloudObjectCallback() {
                                        @Override
                                        public void done(CloudObject x, CloudException t) throws CloudException {
                                            Log.i("Rider's Location", "Saved");
                                            resultGood = true;
                                        }
                                    });
                                }
                            } else {
                                t.printStackTrace();
                            }
                        }
                    });
                } catch (CloudException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (resultGood) {
                if (downloadTaskType.equals("request")) {
                    requestStatusView.setText("Finding a driver...");
                    requestButton.setText("Cancel Request");
                    requestActive = true;

                } else if (downloadTaskType.equals("cancelRequest")) {
                    requestStatusView.setVisibility(View.INVISIBLE);
                    requestButton.setText("Request Ride");

                } else if (downloadTaskType.equals("saveLocation")) {

                }
            }
        }
    }

    public void requestRide(View view) {
        if (!requestActive) {
            requestStatusView.setVisibility(View.VISIBLE);
            downloadTaskType = "request";
            Log.i("Rider Map", "Ride Requested");
            DownloadTask requestRideTask = new DownloadTask();
            requestRideTask.execute("");
        } else {
            requestActive = false;
            downloadTaskType = "cancelRequest";
            Log.i("Rider Map", "Ride Cancelled");
            DownloadTask cancelRequestTask = new DownloadTask();
            cancelRequestTask.execute("");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_location);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        requestStatusView = (TextView) findViewById(R.id.requestStatusView);
        requestButton = (Button) findViewById(R.id.requestButton);

        sharedPreferences = this.getSharedPreferences(getPackageName(), MODE_PRIVATE);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);

        // All location services
        locationManager.requestLocationUpdates(provider, 400, 1, this);

        try {
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                usersCurrentLocation = location;
                Log.i("Users Location", location.getLatitude() + " " + location.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 10));
                mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("Your Location"));
                if (requestActive) {
                    usersCurrentLocation = location;
                    DownloadTask saveLocationTask = new DownloadTask();
                    downloadTaskType = "saveLocation";
                    saveLocationTask.execute("");
                }
            }
        } catch (NullPointerException n) {

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        locationManager.requestLocationUpdates(provider, 400, 1, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i("Users Updated Location", location.getLatitude() + " " + location.getLongitude());
        mMap.clear();
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 10));
        mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("Your Location"));

        // Updates user's location in the database
        if (requestActive) {
            usersCurrentLocation = location;
            DownloadTask saveLocationTask = new DownloadTask();
            downloadTaskType = "saveLocation";
            saveLocationTask.execute("");
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onBackPressed() {
        // Prevents the user from clicking the back button and returning to the signup page.
    }
}
