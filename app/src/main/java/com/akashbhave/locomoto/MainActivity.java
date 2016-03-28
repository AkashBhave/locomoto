package com.akashbhave.locomoto;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;


import io.cloudboost.CloudApp;
import io.cloudboost.CloudException;
import io.cloudboost.CloudObject;
import io.cloudboost.CloudObjectCallback;
import io.cloudboost.CloudUser;
import io.cloudboost.CloudUserCallback;


public class MainActivity extends AppCompatActivity {

    CloudObject notes = new CloudObject("Notes");

    TextView roleDescView;
    TextView selectView;
    Button startButton;
    RadioGroup radioGroup;

    // If the user is a driver or a rider
    String userRole;
    boolean roleSelected = false;

    public class DownloadTask extends AsyncTask<String, Void, Void> {

        String toastMessage = "";

        @Override
        protected Void doInBackground(String... params) {
            try {
                if (roleSelected) {
                    CloudUser aUser = new CloudUser();
                    aUser.setUserName("user");
                    aUser.setPassword("pass");
                    aUser.setEmail("");
                    aUser.set("role", userRole);
                    aUser.signUp(new CloudUserCallback() {
                        @Override
                        public void done(CloudUser user, CloudException e) throws CloudException {
                            if (e == null) {
                                Log.i("User Sign In", "Role: " + userRole);
                                notes.set("currentUser", user.getUserName());
                                notes.save(new CloudObjectCallback() {
                                    @Override
                                    public void done(CloudObject x, CloudException t) throws CloudException {
                                        redirectUser();
                                    }
                                });
                            } else {
                                e.printStackTrace();
                            }
                        }
                    });
                } else {
                    toastMessage = "Please select a role";
                }
            } catch (CloudException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(!toastMessage.equals("")) Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();
        }
    }


    public void getStarted(View view) throws CloudException {
        DownloadTask registerUser = new DownloadTask();
        registerUser.execute("");
    }

    public void redirectUser() {
        if(CloudUser.getcurrentUser().get("role").equals("rider")) {
            Log.i("Rider", "Redirect Map");
            Intent toRiderMap = new Intent(getApplicationContext(), YourLocation.class);
            startActivity(toRiderMap);
        } else {

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initializes the app in the databases
        CloudApp.init("ktdffagvxbnq", "08e13453-be8b-44ec-8b3e-cd8f4f5fd31c");

        if(notes.get("currentUser").equals("user")) {
            redirectUser();
        } else {

        }

        Typeface OpenSans = Typeface.createFromAsset(getAssets(), "fonts/OpenSans.ttf");

        roleDescView = (TextView) findViewById(R.id.roleDescView);
        roleDescView.setTypeface(OpenSans);
        selectView = (TextView) findViewById(R.id.selectView);
        startButton = (Button) findViewById(R.id.startButton);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.driverRButton) {
                    roleDescView.setText("You are going to be a driver.");
                    userRole = "driver";
                    roleSelected = true;
                } else if (checkedId == R.id.riderRButton) {
                    roleDescView.setText("You are going to be a rider.");
                    userRole = "rider";
                    roleSelected = true;
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
