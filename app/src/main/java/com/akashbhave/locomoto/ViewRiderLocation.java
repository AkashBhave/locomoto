package com.akashbhave.locomoto;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.Toast;

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

import java.util.ArrayList;

import io.cloudboost.CloudException;
import io.cloudboost.CloudObject;
import io.cloudboost.CloudObjectArrayCallback;
import io.cloudboost.CloudObjectCallback;
import io.cloudboost.CloudQuery;

public class ViewRiderLocation extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Intent getRiderInfo;

    // Variables of the rider's and driver's properties
    Double riderLat;
    Double riderLng;
    String riderUsername;
    Double driverLat;
    Double driverLng;
    String driverUsername;


    String downloadTaskType = "";

    public class DownloadTask extends AsyncTask<String, Void, Void> {

        String toastMessage = "";

        @Override
        protected Void doInBackground(String... params) {
            if (downloadTaskType.equals("acceptRequest")) {
                try {
                    CloudQuery query = new CloudQuery("Requests");
                    query.equalTo("reqUsername", riderUsername);
                    query.setLimit(1);
                    query.find(new CloudObjectArrayCallback() {
                        @Override
                        public void done(CloudObject[] x, CloudException t) throws CloudException {
                            if (x != null && x.length > 0) {
                                for (CloudObject object : x) {
                                    object.set("driverUsername", driverUsername);
                                    object.save(new CloudObjectCallback() {
                                        @Override
                                        public void done(CloudObject x, CloudException t) throws CloudException {
                                            toastMessage = "Request has been accepted!";
                                            // Starts the directions Intent
                                            Intent directionsIntent = new Intent(android.content.Intent.ACTION_VIEW,
                                                    Uri.parse("http://maps.google.com/maps?saddr=" + driverLat + "," + driverLng
                                                            + "&daddr=" + riderLat + "," + riderLng));
                                            directionsIntent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                                            startActivity(directionsIntent);
                                        }
                                    });
                                }
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
            if (!toastMessage.equals(""))
                Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();
        }
    }

    public void goBack(View view) {
        Intent viewAvailableIntent = new Intent(this, ViewAvailable.class);
        startActivity(viewAvailableIntent);
    }

    public void acceptRequest(View view) throws CloudException {
        DownloadTask acceptRequestTask = new DownloadTask();
        downloadTaskType = "acceptRequest";
        acceptRequestTask.execute("");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_rider_location);

        // Obtains the information parsed by the 'ViewAvailable' class that contains the rider's information
        getRiderInfo = getIntent();
        riderLat = getRiderInfo.getDoubleExtra("riderLat", 0);
        riderLng = getRiderInfo.getDoubleExtra("riderLng", 0);
        riderUsername = getRiderInfo.getStringExtra("riderUsername");
        driverLat = getRiderInfo.getDoubleExtra("driverLat", 0);
        driverLng = getRiderInfo.getDoubleExtra("driverLng", 0);
        driverUsername = getRiderInfo.getStringExtra("driverUsername");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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

        RelativeLayout mapLayout = (RelativeLayout) findViewById(R.id.relativeLayout);
        mapLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                // Ensures the two markers are in view
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                ArrayList<Marker> markers = new ArrayList<Marker>();

                // Custom icons for markers
                BitmapDescriptor locIcon1 = BitmapDescriptorFactory.fromResource(R.drawable.location1);
                BitmapDescriptor locIcon2 = BitmapDescriptorFactory.fromResource(R.drawable.location2);
                markers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(riderLat, riderLng)).title("Your Location").icon(locIcon1)));
                markers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(driverLat, driverLng)).title("Rider's Location").icon(locIcon2)));

                for (Marker marker : markers) {
                    builder.include(marker.getPosition());
                }
                LatLngBounds bounds = builder.build();
                int padding = 100; // offset from edges of the map in pixels
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                mMap.animateCamera(cu);
            }
        });

        Log.i("Rider's Information", riderUsername + " - " + riderLat + ", " + riderLng);
    }
}
