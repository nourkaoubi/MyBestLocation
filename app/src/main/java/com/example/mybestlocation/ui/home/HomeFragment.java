/*package com.example.mybestlocation.ui.home;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.mybestlocation.R;
import com.example.mybestlocation.databinding.FragmentHomeBinding;
import com.example.mybestlocation.Location; // Import the Location model
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;

import android.Manifest;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements OnMapReadyCallback {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private FragmentHomeBinding binding;
    private FusedLocationProviderClient fusedLocationClient;

    private RequestQueue requestQueue; // For making API calls
    private GoogleMap mGoogleMap; // Store the GoogleMap instance

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Initialize the map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.maps);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Initialize the request queue for API calls
        requestQueue = Volley.newRequestQueue(requireContext());

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mGoogleMap = googleMap; // Store the GoogleMap instance

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
            getDeviceLocation(googleMap);

            // Fetch and display saved locations from the API
            displaySavedLocations(googleMap);

            // Set up an OnMapClickListener to capture user clicks on the map
            mGoogleMap.setOnMapClickListener(latLng -> {
                double clickedLatitude = latLng.latitude;
                double clickedLongitude = latLng.longitude;

                // Show the dialog with clicked position
                showAddLocationDialog(clickedLatitude, clickedLongitude);
            });
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void getDeviceLocation(GoogleMap googleMap) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
                if (location != null) {
                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    googleMap.addMarker(new MarkerOptions().position(currentLocation).title("You are here"));
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                } else {
                    Toast.makeText(getContext(), "Unable to get current location", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void showAddLocationDialog(double latitude, double longitude) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add New Location");

        // Create a LinearLayout to organize the inputs
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16, 16, 16, 16);

        // Add an EditText for the pseudo
        final EditText inputPseudo = new EditText(getContext());
        inputPseudo.setHint("Enter your pseudo");
        layout.addView(inputPseudo);

        // Add a TextView to display latitude
        final TextView latitudeView = new TextView(getContext());
        latitudeView.setText("Latitude: " + latitude);
        latitudeView.setTextSize(16);
        layout.addView(latitudeView);

        // Add a TextView to display longitude
        final TextView longitudeView = new TextView(getContext());
        longitudeView.setText("Longitude: " + longitude);
        longitudeView.setTextSize(16);
        layout.addView(longitudeView);

        builder.setView(layout);

        // Handle the "Add" button
        builder.setPositiveButton("Add", (dialog, which) -> {
            String pseudo = inputPseudo.getText().toString();

            // Log the data for debugging
            Log.d("AddLocation", "Adding favorite location: pseudo=" + pseudo +
                    ", latitude=" + latitude + ", longitude=" + longitude);

            // Call the function to save this favorite location
            addFavoriteLocation(pseudo, latitude, longitude);
        });

        // Handle the "Cancel" button
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        // Show the dialog
        builder.show();
    }

    public void addFavoriteLocation(String pseudo, double latitude, double longitude) {
        // Your server URL to insert the favorite location
        String serverUrl = "http://192.168.1.6/servicephp/addPostion.php";

        // Create a request to send the data to the server
        StringRequest stringRequest = new StringRequest(Request.Method.POST, serverUrl,
                response -> {
                    // On success, show the success message and add the marker to the map
                    Toast.makeText(getContext(), "Location added successfully!", Toast.LENGTH_SHORT).show();
                    addMarkerToMap(latitude, longitude, pseudo); // Update the map with the new marker
                },
                error -> {
                    // On failure, show the error message
                    Toast.makeText(getContext(), "Failed to add location: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public byte[] getBody() {
                // Prepare the POST data to send as JSON
                String jsonData = "{\"pseudo\":\"" + pseudo + "\",\"latitude\":" + latitude + ",\"longitude\":" + longitude + "}";
                return jsonData.getBytes(StandardCharsets.UTF_8);
            }
        };

        // Add the request to the request queue
        requestQueue.add(stringRequest);
    }

    private void addMarkerToMap(double latitude, double longitude, String title) {
        if (mGoogleMap != null) {
            // Add marker on the map for the new favorite location
            LatLng location = new LatLng(latitude, longitude);
            mGoogleMap.addMarker(new MarkerOptions()
                    .position(location)
                    .title(title));
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
        } else {
            Log.e("HomeFragment", "GoogleMap is not ready yet!");
        }
    }

    private void displaySavedLocations(GoogleMap googleMap) {
        String url = "http://192.168.1.6/servicephp/location.php";
       // googleMap.clear();
        // Fetch data from your database via an API
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject jsonObject = response.getJSONObject(i);
                            String pseudo = jsonObject.getString("pseudo");
                            double latitude = jsonObject.getDouble("latitude");
                            double longitude = jsonObject.getDouble("longitude");

                            // Add a marker for each location
                            LatLng latLng = new LatLng(latitude, longitude);
                            googleMap.addMarker(new MarkerOptions().position(latLng).title(pseudo));
                        }
                    } catch (JSONException e) {
                        Log.e("JSON Error", "Error parsing locations: " + e.getMessage());
                    }
                },
                error -> Log.e("API Error", "Error fetching data: " + error.getMessage())
        );

        // Add the request to the queue
        requestQueue.add(jsonArrayRequest);
    }

    private List<Location> parseLocationsFromResponse(JSONArray response) {
        List<Location> locations = new ArrayList<>();
        try {
            for (int i = 0; i < response.length(); i++) {
                JSONObject jsonObject = response.getJSONObject(i);
                String pseudo = jsonObject.getString("pseudo");  // Extract the pseudo
                double latitude = jsonObject.getDouble("latitude");
                double longitude = jsonObject.getDouble("longitude");

                // Pass the pseudo along with the other parameters to the Location constructor
                Location location = new Location( pseudo, latitude, longitude);
                locations.add(location);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return locations;
    }

}*/

package com.example.mybestlocation.ui.home;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.android.volley.toolbox.StringRequest;
import com.example.mybestlocation.R;
import com.example.mybestlocation.databinding.FragmentHomeBinding;
import com.example.mybestlocation.Location; // Import the Location model
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;

import android.Manifest;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements OnMapReadyCallback {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private FragmentHomeBinding binding;
    private FusedLocationProviderClient fusedLocationClient;

    private RequestQueue requestQueue; // For making API calls
    private GoogleMap mGoogleMap; // Store the GoogleMap instance

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Initialize the map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.maps);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Initialize the request queue for API calls
        requestQueue = Volley.newRequestQueue(requireContext());

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mGoogleMap = googleMap; // Store the GoogleMap instance

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
            getDeviceLocation(googleMap);

            // Fetch and display saved locations from the API
            displaySavedLocations(googleMap);

            // Set up an OnMapClickListener to capture user clicks on the map
            googleMap.setOnMapClickListener(latLng -> {
                // Show a dialog to allow the user to add this location to their favorites
                showAddLocationDialog(latLng.latitude, latLng.longitude);
            });
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void getDeviceLocation(GoogleMap googleMap) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
                if (location != null) {
                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    googleMap.addMarker(new MarkerOptions().position(currentLocation).title("You are here"));
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                } else {
                    Toast.makeText(getContext(), "Unable to get current location", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void showAddLocationDialog(double latitude, double longitude) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add New Location");


        final EditText inputPseudo = new EditText(getContext());


        inputPseudo.setHint("Enter your pseudo");
        TextView locationText = new TextView(getContext());
        locationText.setText("Latitude: " + latitude + "\nLongitude: " + longitude);
        locationText.setPadding(0, 10, 0, 20);


        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(inputPseudo);
        layout.addView(locationText);

        builder.setView(layout);
        builder.setPositiveButton("Add", (dialog, which) -> {
            String pseudo = inputPseudo.getText().toString();

            // Log the data for debugging
            Log.d("AddLocation", "Adding favorite location: pseudo=" + pseudo +
                    ", latitude=" + latitude + ", longitude=" + longitude);

            // Call the function to save this favorite location
            addFavoriteLocation( pseudo, latitude, longitude);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    public void addFavoriteLocation(String pseudo, double latitude, double longitude) {
        // Your server URL to insert the favorite location
        String serverUrl = "http://192.168.1.6/servicephp/addPostion.php";

        // Create a request to send the data to the server
        StringRequest stringRequest = new StringRequest(Request.Method.POST, serverUrl,
                response -> {
                    // On success, show the success message and add the marker to the map
                    Toast.makeText(getContext(), "Location added successfully!", Toast.LENGTH_SHORT).show();
                    addMarkerToMap(latitude, longitude, pseudo); // Update the map with the new marker
                },
                error -> {
                    // On failure, show the error message
                    Toast.makeText(getContext(), "Failed to add location: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public byte[] getBody() {
                // Prepare the POST data to send as JSON
                String jsonData = "{\"pseudo\":\"" + pseudo + "\",\"latitude\":" + latitude + ",\"longitude\":" + longitude + "}";
                return jsonData.getBytes(StandardCharsets.UTF_8);
            }
        };

        // Add the request to the request queue
        requestQueue.add(stringRequest);
    }

    private void addMarkerToMap(double latitude, double longitude, String title) {
        if (mGoogleMap != null) {
            // Add marker on the map for the new favorite location
            LatLng location = new LatLng(latitude, longitude);
            mGoogleMap.addMarker(new MarkerOptions()
                    .position(location)
                    .title(title));
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
        } else {
            Log.e("HomeFragment", "GoogleMap is not ready yet!");
        }
    }

    private void displaySavedLocations(GoogleMap googleMap) {
        String url = "http://192.168.1.6/servicephp/location.php";

        // Fetch data from your database via an API
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject jsonObject = response.getJSONObject(i);
                            String pseudo = jsonObject.getString("pseudo");
                            double latitude = jsonObject.getDouble("latitude");
                            double longitude = jsonObject.getDouble("longitude");

                            // Add a marker for each location
                            LatLng latLng = new LatLng(latitude, longitude);
                            googleMap.addMarker(new MarkerOptions().position(latLng).title(pseudo));
                        }
                    } catch (JSONException e) {
                        Log.e("JSON Error", "Error parsing locations: " + e.getMessage());
                    }
                },
                error -> Log.e("API Error", "Error fetching data: " + error.getMessage())
        );

        // Add the request to the queue
        requestQueue.add(jsonArrayRequest);
    }


    private List<Location> parseLocationsFromResponse(JSONArray response) {
        List<Location> locations = new ArrayList<>();
        try {
            for (int i = 0; i < response.length(); i++) {
                JSONObject jsonObject = response.getJSONObject(i);
                String pseudo = jsonObject.getString("pseudo");  // Extract the pseudo
                double latitude = jsonObject.getDouble("latitude");
                double longitude = jsonObject.getDouble("longitude");

                // Pass the pseudo along with the other parameters to the Location constructor
                Location location = new Location( pseudo, latitude, longitude);
                locations.add(location);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return locations;
    }

}