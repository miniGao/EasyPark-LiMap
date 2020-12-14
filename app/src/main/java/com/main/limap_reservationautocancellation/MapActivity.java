package com.main.limap_reservationautocancellation;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.main.limap_reservationautocancellation.models.ParkingLot;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapActivity extends OptionMenuActivity implements OnMapReadyCallback {

    private Button btnToggleTraffic;
    private SupportMapFragment mapFragment;

    private static final String TAG = "MapActivity";
    private static final float INIT_ZOOM = 14f;

    private GoogleMap gMap;
    private FusedLocationProviderClient locationClient;
    private Location myLoc;
    private MarkerOptions filterMarkerOptions;

    private LocationRequest locationRequest;
    private LocationCallback locationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if(locationResult != null){
                for(Location location : locationResult.getLocations()){
                    myLoc = location;
                    zoomToMyLoc();
                    getAllParkingLotsFromDb();
                    showAllParkingLotsOnMap();
                    Toast.makeText(MapActivity.this, "Map Synced", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "location update: " + location.toString());
                }
            }
        }
    };

    private FirebaseFirestore db;
    private List<ParkingLot> parkingLotList;
    private ParkingLot selectedParkingLot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        if(ContextCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(MapActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            System.exit(1);
        }else{
            locationClient = LocationServices.getFusedLocationProviderClient(this);
            initLocRequest();
            db = FirebaseFirestore.getInstance();
            parkingLotList = new ArrayList<>();
            getAllParkingLotsFromDb();
            initMapComponent();
            initControlComponent();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        startLocationUpdate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdate();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopLocationUpdate();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        trackMyLocation(gMap);
        showAllParkingLotsOnMap();
        selectParkingLotHandler();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map_menu, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.nearestParkingLot:
                findNearestLot();
                return true;
            case R.id.oneHour:
                findLow1HrFare();
                return true;
            case R.id.sixHour:
                findLow6HrFare();
                return true;
            case R.id.twelveHour:
                findLow12HrFare();
                return true;
            case R.id.twentyfourHour:
                findLow24HrFare();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initLocRequest(){
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(15000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void startLocationUpdate(){
        LocationSettingsRequest request = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest).build();
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> responseTask = settingsClient.checkLocationSettings(request);
        responseTask.addOnSuccessListener(locationSettingsResponse -> {
            try{
                locationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
            }catch (SecurityException e){
                Log.d(TAG, "Security Permission Exception: " + e.getMessage());
            }

        });
        responseTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if(e instanceof ResolvableApiException){
                    ResolvableApiException exception = (ResolvableApiException) e;
                    try {
                        exception.startResolutionForResult(MapActivity.this, 1234);
                    } catch (IntentSender.SendIntentException sendIntentException) {
                        Log.e(TAG, "Location Solution Not Found");
                        sendIntentException.printStackTrace();
                    }
                }
            }
        });
    }

    private void stopLocationUpdate(){
        locationClient.removeLocationUpdates(locationCallback);
    }

    private void zoomToMyLoc(){
        if(gMap != null)
            gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myLoc.getLatitude(), myLoc.getLongitude()), INIT_ZOOM));
    }

    private void initMapComponent(){
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void findNearestLot(){
        getAllParkingLotsFromDb();
        gMap.clear();
        Map<ParkingLot, Double> distances = new HashMap<>();
        for(ParkingLot p : parkingLotList){
            GeoPoint geoPoint = p.getLocation();
            double distance = Math.sqrt(
                    Math.pow(geoPoint.getLatitude() - myLoc.getLatitude(), 2) + Math.pow(geoPoint.getLongitude() - myLoc.getLongitude(), 2)
            );
            distances.put(p, distance);
        }

        ParkingLot nearest = Collections.min(distances.entrySet(), Map.Entry.comparingByValue()).getKey();
        Log.d(TAG,"get min: " + nearest.getName());
        setFilterMarkerOptions(nearest, String.format("Available/Total: %s/%s", nearest.getAvailableNumOfSpots(), nearest.getTotalNumOfSpots()));
    }

    private void findLow1HrFare(){
        setPriceFilterOptions("1hour", "Hour");
    }

    private void findLow6HrFare(){
        setPriceFilterOptions("6hour", "6 Hours");
    }

    private void findLow12HrFare(){
        setPriceFilterOptions("12hour", "12 Hours");
    }

    private void findLow24HrFare(){
        setPriceFilterOptions("24hour", "24 Hours");
    }

    private void initControlComponent(){

        btnToggleTraffic = findViewById(R.id.btnToggleTraffic);
        btnToggleTraffic.setOnClickListener(v->{
            if(gMap.isTrafficEnabled()){
                gMap.setTrafficEnabled(false);
            }else{
                gMap.setTrafficEnabled(true);
            }
        });
    }

    private void trackMyLocation(GoogleMap gMap){
        try{
            final Task getLocation = locationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null);
            getLocation.addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    myLoc = (Location) task.getResult();
                    Log.d(TAG,"Track myLoc: " + myLoc.toString());
                    gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myLoc.getLatitude(), myLoc.getLongitude()), INIT_ZOOM));
                    gMap.setMyLocationEnabled(true);
                }else{
                    Log.d(TAG, "Tracking My Location Task Failed.");
                }
            });
        }catch (SecurityException e){
            Log.d(TAG, "Security Exception: " + e.getMessage());
        }
    }

    private void getAllParkingLotsFromDb(){
        db.collection("ParkingLotDemo").get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                parkingLotList.clear();
                for(QueryDocumentSnapshot documentSnapshot : task.getResult()){
                    parkingLotList.add(documentSnapshot.toObject(ParkingLot.class));
                }
                gMap.clear();
                onMapReady(gMap);
            }else{
                Log.e(TAG, "Get All Parking Lots Error: " + task.getException().toString());
            }
        });
    }

    private void showAllParkingLotsOnMap(){
        for(ParkingLot p : parkingLotList){
            LatLng lotLocation = new LatLng(p.getLocation().getLatitude(), p.getLocation().getLongitude());
            gMap.addMarker(new MarkerOptions()
                    .position(lotLocation).title(p.getName()).snippet(String.format("Available/Total: %s/%s", p.getAvailableNumOfSpots(), p.getTotalNumOfSpots()))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            );
        }
        if(filterMarkerOptions!=null){
            Marker marker = gMap.addMarker(filterMarkerOptions);
            marker.showInfoWindow();
            filterMarkerOptions=null;
        }
    }

    private void selectParkingLotHandler(){
        gMap.setOnInfoWindowClickListener(marker -> {
            getAllParkingLotsFromDb();
            for(ParkingLot p : parkingLotList){
                if(p.getLocation().compareTo(new GeoPoint(marker.getPosition().latitude, marker.getPosition().longitude))==0){
                    selectedParkingLot = p;
                    break;
                }
            }
            Log.d(TAG, "Marker: " + marker.getPosition().toString());
            Log.d(TAG, "Location: " + selectedParkingLot.getLocation().toString());
            Bundle bundle = new Bundle();
            bundle.putString("docId", selectedParkingLot.getDocId());
            bundle.putString("lotName", selectedParkingLot.getName());
            Intent intent = new Intent(getApplicationContext(), MapSelectParkingLotActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        });
    }

    private void setFilterMarkerOptions(ParkingLot p, String snippet){
        LatLng lotLocation = new LatLng(p.getLocation().getLatitude(), p.getLocation().getLongitude());
        filterMarkerOptions = new MarkerOptions()
                .position(lotLocation).title(p.getName()).snippet(snippet)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                );
    }

    private void setPriceFilterOptions(String key, String duration){
        getAllParkingLotsFromDb();
        Map<ParkingLot, Long> prices = new HashMap<>();
        for(ParkingLot p : parkingLotList){
            prices.put(p, p.getPrice().get(key));
        }
        ParkingLot lowest = Collections.min(prices.entrySet(), Map.Entry.comparingByValue()).getKey();
        Log.d(TAG,"get lowest: " + lowest.getPrice());
        NumberFormat currencyFmt = NumberFormat.getCurrencyInstance(Locale.CANADA);
        setFilterMarkerOptions(lowest, String.format("Rate: %s/%s", currencyFmt.format(lowest.getPrice().get(key)), duration));
    }
}