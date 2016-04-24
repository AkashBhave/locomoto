package com.akashbhave.locomoto;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

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
    // A custom marker
    BitmapDescriptor locIcon1;
    BitmapDescriptor locIcon3;

    TextView requestStatusView;
    Button requestButton;

    // If user's request is still wanted
    boolean requestActive = false;
    Location driverLocation = new Location(provider);

    // Handler that will update the rider and driver's location in DownloadTask (downloadTaskType = 'saveLocation')
    Handler handler = new Handler();
    Handler anotherHandler = new Handler();
    Handler reqHandler = new Handler();
    Runnable reqRunnable;

    SharedPreferences sharedPreferences;

    String downloadTaskType = "";

    public class DownloadTask extends AsyncTask<String, Void, Void> {

        boolean resultGood = false;
        String resultType = "";
        String resultType2 = "";

        double driverDistance;
        float driverBearing;

        @Override
        protected Void doInBackground(final String... params) {
            if (downloadTaskType.equals("request")) {
                try {
                    final CloudGeoPoint riderLocation = new CloudGeoPoint(usersCurrentLocation.getLatitude(), usersCurrentLocation.getLongitude());
                    final String[] reqActualName = new String[1];

                    // Saves the requester's actual name
                    CloudQuery actualNameObject = new CloudQuery("User");
                    actualNameObject.equalTo("username", sharedPreferences.getString("currentUser", ""));
                    actualNameObject.find(new CloudObjectArrayCallback() {
                        @Override
                        public void done(CloudObject[] x, CloudException t) throws CloudException {
                            if (x != null) {
                                for (CloudObject object : x) {
                                    reqActualName[0] = object.getString("actualName");
                                }
                            } else {
                                t.printStackTrace();
                            }
                        }
                    });

                    CloudObject requestsObject = new CloudObject("Requests");
                    requestsObject.set("reqUsername", sharedPreferences.getString("currentUser", ""));
                    requestsObject.set("reqLocation", riderLocation);
                    requestsObject.set("reqActualName", reqActualName[0]);
                    requestsObject.save(new CloudObjectCallback() {
                        @Override
                        public void done(CloudObject x, CloudException t) throws CloudException {
                            Log.i("Rider's Location", "Saved");
                            Log.i("Rider Requester", "Saved");
                            resultGood = true;
                            requestActive = true;
                        }
                    });

                } catch (CloudException e) {
                    e.printStackTrace();
                }


            } else if (downloadTaskType.equals("cancelRequest")) {
                try {
                    // See who gave the request
                    CloudQuery userQuery = new CloudQuery("Requests");
                    Log.d("Cancel Request", "Executed");
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
                    final String[] driverUsername = {""};
                    // Updates the user's location
                    CloudQuery userQuery = new CloudQuery("Requests");
                    userQuery.equalTo("reqUsername", sharedPreferences.getString("currentUser", ""));
                    userQuery.find(new CloudObjectArrayCallback() {
                        @Override
                        public void done(CloudObject[] x, CloudException t) throws CloudException {
                            if (x != null) {
                                for (CloudObject object : x) {
                                    driverUsername[0] = object.getString("driverUsername");
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

                    // Updates the driver's location
                    CloudQuery driverQuery = new CloudQuery("User");
                    driverQuery.equalTo("username", driverUsername[0]);
                    driverQuery.find(new CloudObjectArrayCallback() {
                        @Override
                        public void done(CloudObject[] x, CloudException t) throws CloudException {
                            if (x != null) {
                                if (x.length > 0) {
                                    for (CloudObject object : x) {
                                        try {
                                            driverBearing = Float.parseFloat(object.getString("bearing"));
                                            JSONObject geopoint = new JSONObject(object.get("location").toString());
                                            // retrieve the coordinates
                                            String coordinates = geopoint.getString("coordinates").replace("[", "").replace("]", "");
                                            String[] latLng = coordinates.split(",");
                                            String stringLat = latLng[1].replace(",", "");
                                            String stringLng = latLng[0].replace(",", "");
                                            Double latitude = Double.parseDouble(stringLat);
                                            Double longitude = Double.parseDouble(stringLng);
                                            driverLocation.setLatitude(latitude);
                                            driverLocation.setLongitude(longitude);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            } else {
                                t.printStackTrace();
                            }
                        }
                    });

                    if (driverLocation.getLatitude() != 0 && driverLocation.getLongitude() != 0) {
                        Log.i("Driver Location", driverLocation.getLatitude() + "," + driverLocation.getLongitude());
                        double distance = usersCurrentLocation.distanceTo(driverLocation);
                        driverDistance = (double) (Math.round((distance * 0.000621371) * 100) / 100);
                        resultGood = true;
                        resultType = "driverDistance";
                        if (driverDistance == 0) {
                            resultType = "driverDistance0";
                            // Make a notification when the driver has arrived
                            final NotificationCompat.Builder nBuilder =
                                    new NotificationCompat.Builder(getApplicationContext())
                                            .setSmallIcon(R.drawable.location1)
                                            .setContentTitle("Your Driver is Here!")
                                            .setContentText("Your driver has arrived at your location.")
                                            .setVibrate(new long[]{0, 1000, 500, 1000, 500})
                                            .setAutoCancel(true);
                            // Intent that executes when you click on the notification
                            Intent resultIntent = new Intent(getApplicationContext(), YourLocation.class);
                            PendingIntent pendingIntent = PendingIntent.getActivity(
                                    getApplicationContext(),
                                    0,
                                    resultIntent,
                                    PendingIntent.FLAG_UPDATE_CURRENT
                            );
                            nBuilder.setContentIntent(pendingIntent);
                            // Issues the notification
                            int mNotificationId = 1; // Sets an ID for the notification

                            final NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                            // Builds the notification and issues it.
                            mNotifyMgr.notify(mNotificationId, nBuilder.build());

                            anotherHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    // Resets the location so the vibration stops
                                    driverLocation = new Location(provider);
                                    DownloadTask cancelRequestTask = new DownloadTask();
                                    downloadTaskType = "cancelRequest";
                                    cancelRequestTask.execute("");
                                }
                            }, 3000);
                        }
                    }
                } catch (CloudException e) {
                    e.printStackTrace();
                }

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (requestActive) {
                            DownloadTask updateLocations = new DownloadTask();
                            downloadTaskType = "saveLocation";
                            updateLocations.execute("");
                        } else {
                            Log.i("Update Runnable", "Stopped");
                            handler.removeCallbacksAndMessages(null);
                        }
                    }
                }, 5000);
            } else if (downloadTaskType.equals("getCurrentRequests")) {
                CloudQuery myRequests = new CloudQuery("Requests");
                myRequests.equalTo("reqUsername", sharedPreferences.getString("currentUser", ""));
                try {
                    myRequests.find(new CloudObjectArrayCallback() {
                        @Override
                        public void done(CloudObject[] x, CloudException t) throws CloudException {
                            if (x != null) {
                                if (x.length > 0) {
                                    Log.i("My Requests Amount", String.valueOf(x.length));
                                    for (CloudObject object : x) {
                                        try {
                                            String possibleTerm = object.get("driverUsername").toString();
                                            if (possibleTerm == null || possibleTerm.equals("")) {

                                            } else {
                                                String driverUsername = object.getString("driverUsername");
                                                Log.i("My Requests", "Driver Accepted: " + driverUsername);
                                                resultGood = true;
                                                resultType2 = "yes";
                                                reqHandler.removeCallbacksAndMessages(reqRunnable);
                                            }
                                        } catch (NullPointerException n) {
                                            Log.i("My Requests", "Driver Hasn't Accepted");
                                            resultGood = true;
                                            resultType2 = "no";

                                            reqRunnable = new Runnable() {
                                                @Override
                                                public void run() {
                                                    DownloadTask seeRequestsTask = new DownloadTask();
                                                    downloadTaskType = "getCurrentRequests";
                                                    seeRequestsTask.execute("");
                                                }
                                            };
                                            reqHandler.postDelayed(reqRunnable, 5000);
                                        }
                                    }
                                } else {
                                    Log.i("My Requests", "None");
                                    requestActive = false;
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
                    if (resultGood) {
                        requestStatusView.setText("Finding a driver...");
                        requestButton.setVisibility(View.VISIBLE);
                        requestButton.setText("Cancel Request");
                        requestActive = true;
                        DownloadTask seeRequestsTask = new DownloadTask();
                        downloadTaskType = "getCurrentRequests";
                        seeRequestsTask.execute("");
                    } else {
                        Log.i("Request Ride", "Not Successful");
                    }
                } else if (downloadTaskType.equals("cancelRequest")) {
                    requestStatusView.setVisibility(View.INVISIBLE);
                    requestButton.setVisibility(View.VISIBLE);
                    requestButton.setText("Request Ride");

                } else if (downloadTaskType.equals("saveLocation")) {
                    if (resultGood) {
                        Marker driverMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)));
                        if (resultType.equals("driverDistance")) {
                            requestStatusView.setText("Your driver is " + String.valueOf(driverDistance) + " miles away.");
                            requestStatusView.setVisibility(View.VISIBLE);

                            // Ensures the two markers are in view
                            mMap.clear();
                            LatLngBounds.Builder builder = new LatLngBounds.Builder();
                            ArrayList<Marker> markers = new ArrayList<Marker>();
                            markers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(usersCurrentLocation.getLatitude(), usersCurrentLocation.getLongitude()))
                                    .title("Your Location")
                                    .icon(locIcon1)));
                            driverMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(driverLocation.getLatitude(), driverLocation.getLongitude()))
                                    .title("Rider's Location")
                                    .icon(locIcon3)
                                    .rotation(driverBearing)
                                    .anchor(0.5f, 0.5f));
                            markers.add(driverMarker);

                            for (Marker marker : markers) {
                                builder.include(marker.getPosition());
                            }
                            LatLngBounds bounds = builder.build();
                            int padding = 100; // offset from edges of the map in pixels
                            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                            mMap.animateCamera(cu);
                        } else if (resultType.equals("driverDistance0")) {
                            requestActive = false;
                            requestStatusView.setText("Your driver has arrived!");
                            requestStatusView.setVisibility(View.VISIBLE);
                            requestButton.setText("Request Ride");
                            requestButton.setVisibility(View.VISIBLE);
                            driverMarker.remove();
                        }
                    }
                } else if (downloadTaskType.equals("getCurrentRequests")) {
                    if (resultGood) {
                        if (resultType2.equals("yes")) {
                            requestStatusView.setText("A driver is on their way!");
                            requestButton.setVisibility(View.INVISIBLE);
                            DownloadTask startUpdatingTask = new DownloadTask();
                            downloadTaskType = "saveLocation";
                            startUpdatingTask.execute("");
                        } else {
                            requestStatusView.setText("Finding a driver...");
                            requestButton.setVisibility(View.VISIBLE);
                        }
                        requestButton.setText("Cancel Request");
                        requestStatusView.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    }

    public void requestRide(View view) {
        if (requestActive) {
            requestActive = false;
            downloadTaskType = "cancelRequest";
            Log.i("Rider Map", "Ride Cancelled");
            DownloadTask cancelRequestTask = new DownloadTask();
            cancelRequestTask.execute("");
        } else {
            requestStatusView.setVisibility(View.VISIBLE);
            downloadTaskType = "request";
            Log.i("Rider Map", "Ride Requested");
            DownloadTask requestRideTask = new DownloadTask();
            requestRideTask.execute("");
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
        Log.i("My Username", sharedPreferences.getString("currentUser", ""));

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);
        locIcon1 = BitmapDescriptorFactory.fromResource(R.drawable.location1);
        locIcon3 = BitmapDescriptorFactory.fromResource(R.drawable.location3);

        // All location services
        locationManager.requestLocationUpdates(provider, 400, 1, this);

        try {
            Location location = locationManager.getLastKnownLocation(provider);
            //if (location != null) {
            if (requestActive) {
                usersCurrentLocation = location;
                DownloadTask saveLocationTask = new DownloadTask();
                downloadTaskType = "saveLocation";
                saveLocationTask.execute("");
            } else {
                // Checks to see if there are any current requests made by the current user
                usersCurrentLocation = location;
                DownloadTask getCurrentRequestsTask = new DownloadTask();
                downloadTaskType = "getCurrentRequests";
                getCurrentRequestsTask.execute("");
            }
            //}
            usersCurrentLocation = location;
            Log.i("Users Location", location.getLatitude() + " " + location.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 10));
            mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("Your Location").icon(locIcon1));
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

        mMap.clear();
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(usersCurrentLocation.getLatitude(), usersCurrentLocation.getLongitude()), 10));
        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(usersCurrentLocation.getLatitude(), usersCurrentLocation.getLongitude()))
                .title("Your Location")
                .icon(locIcon1));
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i("Users Updated Location", location.getLatitude() + " " + location.getLongitude());
        mMap.clear();
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 10));
        mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("Your Location").icon(locIcon1));

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
