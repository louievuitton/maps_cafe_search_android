package com.example.lab3;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LandingActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private CoordinatorLayout coordinatorLayout;
    private BottomNavigationView bottomNavigationView;
    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private Marker currentLocationMarker;
    private static final int REQUEST_USER_LOCATION_CODE = 99;
    private EditText searchEditText;
    private Snackbar snackbar;
    private double locationLat;
    private double locationLng;
    private ArrayList<CafeModel> nearbyCafes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkUserLocationPermission();
        }

        coordinatorLayout = findViewById(R.id.coordinatorLayout);
        searchEditText = findViewById(R.id.searchEditText);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.home);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home:
                        return true;
                    case R.id.nearby_cafes:
                        Collections.sort(nearbyCafes);
                        Intent intent = new Intent(LandingActivity.this, NearbyCafesActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("cafes", nearbyCafes);
                        intent.putExtra("bundle", bundle);
                        startActivity(intent);
                        finish();
                        return true;
                }
                return false;
            }
        });

        searchEditText.setFocusable(false);
        searchEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Place.Field> fieldList = Arrays.asList(Place.Field.ADDRESS, Place.Field.NAME, Place.Field.LAT_LNG);

                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldList).build(LandingActivity.this);
                startActivityForResult(intent, 100);
            }
        });

        // Initialize the SDK
        Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_key));

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    // used to retrieve results from google places api
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            snackbar = Snackbar.make(coordinatorLayout, "Cafe successfully search", BaseTransientBottomBar.LENGTH_SHORT)
                    .setAction("Dismiss", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            snackbar.dismiss();
                        }
                    });
            snackbar.show();

            // clear any existing markers on the map
            mMap.clear();
            Place place = Autocomplete.getPlaceFromIntent(data);
            searchEditText.setText(place.getName() + ", " + place.getAddress());
            LatLng latLng = place.getLatLng();
            locationLat = latLng.latitude;
            locationLng = latLng.longitude;
            currentLocationMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Current Cafe"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(12));

            fetchNearbyPlaces(locationLat, locationLng);
        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            Status status = Autocomplete.getStatusFromIntent(data);
            snackbar = Snackbar.make(coordinatorLayout, status.getStatusMessage(), BaseTransientBottomBar.LENGTH_SHORT)
                    .setAction("Dismiss", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            snackbar.dismiss();
                        }
                    });
            snackbar.show();
        }
    }

    // fetch nearby cafes given selected cafe location using google places api
    public void fetchNearbyPlaces(double lat, double lng) {
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+ lat +","+ lng +"&radius=3500&type=cafe&key=" + getResources().getString(R.string.google_maps_key);

        RequestQueue queue = Volley.newRequestQueue(this);

        // call get request to retrieve nearby cafes of selected location
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, new JSONObject(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    ArrayList<CafeModel> data = new ArrayList<>();
                    JSONArray results = response.getJSONArray("results");
                    if (results.length() > 0) {
                        for (int i = 0; i < results.length(); i++) {
                            JSONObject obj = (JSONObject) results.get(i);
                            if (Double.parseDouble(obj.getJSONObject("geometry").getJSONObject("location").getString("lat")) != lat && Double.parseDouble(obj.getJSONObject("geometry").getJSONObject("location").getString("lng")) != lng) {
                                CafeModel model = new CafeModel();
                                model.setId(obj.getString("place_id"));
                                model.setName(obj.getString("name"));
                                model.setLat(Double.parseDouble(obj.getJSONObject("geometry").getJSONObject("location").getString("lat")));
                                model.setLng(Double.parseDouble(obj.getJSONObject("geometry").getJSONObject("location").getString("lng")));
                                model.setPriceLevel(Integer.parseInt(obj.getString("price_level")));
                                model.setRating(Double.parseDouble(obj.getString("rating")));
                                model.setTotalRatings(Integer.parseInt(obj.getString("user_ratings_total")));
                                model.setVicinity(obj.getString("vicinity"));
                                data.add(model);

                                if (data.size() == 5) {
                                    break;
                                }
                            }
                        }

                        if (data.size() == 0) {
                            snackbar = Snackbar.make(coordinatorLayout, "No Nearby Cafes", BaseTransientBottomBar.LENGTH_SHORT)
                                    .setAction("Dismiss", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            snackbar.dismiss();
                                        }
                                    });
                            snackbar.show();
                        }
                        else {
                            nearbyCafes = data;

                            // display a green marker for nearby cafes
                            for (int i = 0; i < nearbyCafes.size(); i++) {
                                LatLng latLng = new LatLng(nearbyCafes.get(i).getLat(), nearbyCafes.get(i).getLng());
                                MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(nearbyCafes.get(i).getName());
                                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                                mMap.addMarker(markerOptions);
                            }
                        }
                    }
                    else {
                        snackbar = Snackbar.make(coordinatorLayout, "No nearby cafes in this area", BaseTransientBottomBar.LENGTH_SHORT)
                                .setAction("Dismiss", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        snackbar.dismiss();
                                    }
                                });
                        snackbar.show();
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(LandingActivity.this, "Error in request", Toast.LENGTH_SHORT).show();
            }
        });

        queue.add(jsonRequest);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
    }

    public boolean checkUserLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_USER_LOCATION_CODE);
            return false;
        }
        else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_USER_LOCATION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        if (googleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                }
                else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                return;
        }
    }

    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        googleApiClient.connect();
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (currentLocationMarker != null) {
            currentLocationMarker.remove();
        }

        locationLat = location.getLatitude();
        locationLng = location.getLongitude();
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        currentLocationMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Current Location"));

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(14));
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}