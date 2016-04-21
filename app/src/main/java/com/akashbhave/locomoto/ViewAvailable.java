package com.akashbhave.locomoto;

import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.cloudboost.CloudException;
import io.cloudboost.CloudGeoPoint;
import io.cloudboost.CloudObject;
import io.cloudboost.CloudObjectArrayCallback;
import io.cloudboost.CloudQuery;

public class ViewAvailable extends AppCompatActivity implements LocationListener {

    ListView listView;
    ArrayList<String> usersNames = new ArrayList<String>();
    ArrayList<Location> usersLocations = new ArrayList<Location>();
    ArrayList<String> distances = new ArrayList<String>();
    ArrayAdapter arrayAdapter;

    LocationManager locationManager;
    String provider;
    // For saving the user's location
    Location usersCurrentLocation;

    // If user's request is still wanted
    boolean requestActive = false;

    SharedPreferences sharedPreferences;

    String downloadTaskType = "";

    public class DownloadTask extends AsyncTask<String, Void, Void> {

        boolean resultGood = false;

        @Override
        protected Void doInBackground(String... params) {
            if (downloadTaskType.equals("updateLocation")) {
                try {
                    final Location driverLocation = new Location(provider);
                    driverLocation.setLatitude(usersCurrentLocation.getLatitude());
                    driverLocation.setLongitude(usersCurrentLocation.getLongitude());
                    CloudQuery query = new CloudQuery("Requests");
                    query.orderByAsc("reqLocation");
                    query.equalTo("driverUsername", null);
                    query.setLimit(10);
                    query.find(new CloudObjectArrayCallback() {
                        @Override
                        public void done(CloudObject[] x, CloudException t) throws CloudException {
                            if(x != null) {
                                resultGood = true;
                                Log.i("Amount", String.valueOf(x.length));
                                usersNames.clear();
                                usersLocations.clear();
                                distances.clear();
                                for(CloudObject object : x) {
                                    try {
                                        JSONObject geopoint = new JSONObject(object.get("reqLocation").toString());
                                        // retrieve the coordinates
                                        String coordinates = geopoint.getString("coordinates").replace("[", "").replace("]", "");
                                        Double latitude = Double.parseDouble(coordinates.split(",")[0]);
                                        Double longitude = Double.parseDouble(coordinates.split(",")[1]);

                                        Location aUserLocation = new Location(provider);
                                        aUserLocation.setLatitude(latitude);
                                        aUserLocation.setLongitude(longitude);

                                        Log.i("User Lat/Lng", String.valueOf(aUserLocation.getLatitude()) + "/" + String.valueOf(aUserLocation.getLongitude()));

                                        double distance = driverLocation.distanceTo(aUserLocation);
                                        double distanceMiles = distance * 0.000621371;

                                        usersNames.add(object.get("reqUsername").toString());
                                        usersLocations.add(aUserLocation);
                                        distances.add(String.valueOf(distanceMiles));
                                        Log.i("Distance", String.valueOf(distanceMiles));

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } else if(t != null) {
                                t.printStackTrace();
                            }
                        }
                    });

                } catch (CloudException c) {
                    c.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (resultGood) {
                if (downloadTaskType.equals("updateLocation")) {
                    // Changes the listView
                    arrayAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_available);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);

        // All location services
        locationManager.requestLocationUpdates(provider , 400, 1, this);

        requestActive = true;
        try {
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                if (requestActive) {
                    usersCurrentLocation = location;
                    DownloadTask saveLocationTask = new DownloadTask();
                    downloadTaskType = "updateLocation";
                    saveLocationTask.execute("");
                }
            }
        } catch (NullPointerException n) {

        }

        usersNames.addAll(distances);
        listView = (ListView) findViewById(R.id.listView);
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_2, android.R.id.text1, usersNames) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                String entry1 = usersNames.get(position);
                String entry2 = distances.get(position);

                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);
                text1.setText(entry1);
                text2.setText(entry2);

                return view;
            }
        };

        listView.setAdapter(arrayAdapter);
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


    @Override
    public void onLocationChanged(Location location) {
        // Updates user's location in the database
        if (requestActive) {
            usersCurrentLocation = location;
            DownloadTask saveLocationTask = new DownloadTask();
            downloadTaskType = "updateLocation";
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
