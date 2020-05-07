package com.example.myapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.myapplication.problem.Location;
import com.example.myapplication.problem.Waypoint;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class StartNavigationActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private ApplicationData appData = new ApplicationData();
    private ActionBar actionBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.start_navigation);
        actionBar=getSupportActionBar();
        actionBar.setTitle(appData.getCurrentBuilding().getName());
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.bringToFront();
        navigationView.setNavigationItemSelectedListener(this);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);


        ImageView scanLocation = findViewById(R.id.cameraBtn);
        scanLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StartNavigationActivity.this, ScanLocationActivity.class);
                startActivityForResult(intent, 1);
            }
        });

        Button startNavigation = findViewById(R.id.navigationBtn);
        startNavigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String startName = ((TextView)findViewById(R.id.currentLocation)).getText().toString();
                String destinationName = ((TextView)findViewById(R.id.destination)).getText().toString();
                System.out.println(appData.currentBuilding);
                Location start = appData.currentBuilding.getLocation(startName);
                Location destination = appData.currentBuilding.getLocation(destinationName);
                getWaypoints(String.valueOf(start.getId()), String.valueOf(destination.getId()));
    }
});

        generateSuggestedPlaces(3);

        }

@Override
public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (mToggle.onOptionsItemSelected(item)) {
        return true;
        }
        return super.onOptionsItemSelected(item);
        }

@Override
public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.chooseBuilding) {
        showBuildings();
        }
        return false;
        }

private void showBuildings() {
        List<String> buildingNames = appData.getBuildings();
final String[] buildings = new String[buildingNames.size()];
        int i = 0;
        for(String temp : buildingNames) {
            buildings[i] = temp;
            i++;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick a building");
        builder.setItems(buildings, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                appData.setCurrentBuildingName(buildings[which]);
                appData.setCurrentBuilding(appData.getBuildingByName(buildings[which]));
                generateSuggestedPlaces(5);
                actionBar = getSupportActionBar();
                actionBar.setTitle(buildings[which]);
            }

        });
        builder.show();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 1) {
            String msg = data.getStringExtra("returnedData");
            Location location =appData.getCurrentBuilding().getLocationById(Integer.parseInt(msg));
            EditText editText = findViewById(R.id.currentLocation);
            editText.setText(location.getName());
        }
    }

    public void getWaypoints(String start, String destination) {
        // the url is different for every computer.
        // for emulator use 10.0.0.2:5000/
        // for device, run ipconfig in cmd and get ipv4 address

        String url = "http://192.168.0.158:5000/route/";
        url = url.concat(appData.getCurrentBuilding().getName() + "?start=" + start + "&" + "destination=" + destination);
        System.out.println(url);
        final RequestQueue requestQueue = Volley.newRequestQueue(StartNavigationActivity.this);

        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d("JsonObject","Response: " + response.toString());

                        List<Waypoint> waypointList = JsonParser.parseRoute(response);
                        for (Waypoint waypoint:waypointList)
                            System.out.println(waypoint);
                        appData.setWaypoints(waypointList);
                        Intent intent = new Intent(StartNavigationActivity.this, NavigationActivity.class);
                        intent.putStringArrayListExtra("instructions", appData.getAllInstructions());
                        intent.putIntegerArrayListExtra("codesToScan", appData.getAllCodesToScan());
                        startActivity(intent);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Log.e("JsonError","Error on get JSON request!");
                    }
                });
        requestQueue.add(jsonObjectRequest);
    }
  
    private void generateSuggestedPlaces(int howMany) {
        final ListView lv = (ListView) findViewById(R.id.listView);
        List<Location> topLocations = appData.getCurrentBuilding().getTopLocations(howMany);
        String[] locations = new String[howMany];
        int i = 0;
        for(Location temp : topLocations) {
            locations[i] = temp.getName();
            i++;
        }

        List<String> locationsList = new ArrayList<String>(Arrays.asList(locations));
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1, locationsList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                // Get the Item from ListView
                View view = super.getView(position, convertView, parent);

                // Initialize a TextView for ListView each Item
                TextView tv = (TextView) view.findViewById(android.R.id.text1);

                // Set the text color of TextView (ListView Item)
                tv.setTextColor(Color.BLACK);
                tv.setBackgroundColor(Color.parseColor("#B7D4FF"));

                // Generate ListView Item using TextView
                return view;
            }
        };
        lv.setAdapter(arrayAdapter);

    }
}