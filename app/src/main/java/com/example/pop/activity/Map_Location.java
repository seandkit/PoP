package com.example.pop.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.pop.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class Map_Location extends AppCompatActivity implements OnMapReadyCallback {

    GoogleMap map;
    Double latLocationA = 0.0;
    Double longLocationA = 0.0;
    String vendorNameA = "";
    String locationA = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map__location);
        Double latLocation = getIntent().getDoubleExtra("lat", 0);
        Double longLocation = getIntent().getDoubleExtra("long", 0);
        String vendorName = getIntent().getStringExtra("title");
        String location = getIntent().getStringExtra("snippet");
        latLocationA = latLocation;
        longLocationA = longLocation;
        vendorNameA = vendorName;
        locationA = location;

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.mapNearBy);
        mapFragment.getMapAsync(this);
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng shopMarker = new LatLng(latLocationA, longLocationA);
        map = googleMap;
        map.addMarker(new MarkerOptions().position(shopMarker)
                .title(vendorNameA).snippet(locationA));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(shopMarker, 15));

    }
}
