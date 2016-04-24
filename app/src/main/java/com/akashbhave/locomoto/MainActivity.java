package com.akashbhave.locomoto;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.UUID;

import io.cloudboost.CloudApp;
import io.cloudboost.CloudException;
import io.cloudboost.CloudUser;
import io.cloudboost.CloudUserCallback;


public class MainActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;

    TextView roleDescView;
    TextView selectView;
    EditText nameInput;
    Button startButton;
    RadioGroup radioGroup;

    // If the user is a driver or a rider
    String userRole;
    String userName;
    boolean roleSelected = false;

    public class DownloadTask extends AsyncTask<String, Void, Void> {

        String toastMessage = "";

        @Override
        protected Void doInBackground(String... params) {
            try {
                if (roleSelected) {
                    CloudUser aUser = new CloudUser();

                    // Creates a random id/username for each user
                    final String id = UUID.randomUUID().toString().replaceAll("-", "");
                    aUser.setUserName(id);
                    aUser.setPassword("pass");
                    aUser.setEmail("");
                    aUser.set("role", userRole);
                    aUser.set("actualName", userName);
                    aUser.signUp(new CloudUserCallback() {
                        @Override
                        public void done(CloudUser user, CloudException e) throws CloudException {
                            if (e == null) {
                                Log.i("User Sign In", "Role: " + userRole);

                                // Puts in a SharedPreferences that user has already logged in
                                sharedPreferences.edit().putBoolean("isUserIn", true).apply();
                                sharedPreferences.edit().putString("role", userRole).apply();
                                sharedPreferences.edit().putString("currentUser", id).apply();
                                redirectUser();
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
            if (!toastMessage.equals(""))
                Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();
        }
    }


    public void getStarted(View view) throws CloudException {
        userName = nameInput.getText().toString();
        if (userName.length() > 5) {
            DownloadTask registerUser = new DownloadTask();
            registerUser.execute("");
        } else {
            Toast.makeText(MainActivity.this, "Your name must be at least 6 characters", Toast.LENGTH_LONG).show();
        }
    }

    public void redirectUser() {
        if (sharedPreferences.getString("role", "").equals("rider")) {
            Log.i("Rider", "Redirect Map");
            Intent toRiderMap = new Intent(getApplicationContext(), YourLocation.class);
            startActivity(toRiderMap);
        } else {
            Log.i("Driver", "Redirect Map");
            Intent toDriverMap = new Intent(getApplicationContext(), ViewAvailable.class);
            startActivity(toDriverMap);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initializes the app in the databases
        CloudApp.init("izjqixzyfjbd", "2be811d8-124e-49da-ad43-6bac3ad5f28c");

        // Checks to see if user has already opened app and set up
        sharedPreferences = this.getSharedPreferences(getPackageName(), MODE_PRIVATE);
        boolean isUserIn = sharedPreferences.getBoolean("isUserIn", false);
        if (isUserIn) redirectUser();

        Typeface OpenSans = Typeface.createFromAsset(getAssets(), "fonts/OpenSans.ttf");

        roleDescView = (TextView) findViewById(R.id.roleDescView);
        roleDescView.setTypeface(OpenSans);
        selectView = (TextView) findViewById(R.id.selectView);
        nameInput = (EditText) findViewById(R.id.nameInput);
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
