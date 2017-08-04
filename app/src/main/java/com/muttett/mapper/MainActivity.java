package com.muttett.mapper;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.ArrayMap;
import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.muttett.mapper.model.RunnerLocation;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

  private static final String TAG = "Mapper";
  private static final String FIREBASE_URL = "https://glaring-torch-abcd.firebaseio.com/";
  private static final String FIREBASE_CHILD = "location";
  private static final LatLng INITIAL_LATLNG = new LatLng(37.784457, -122.450556);
  private static final int INITIAL_ZOOM = 13;

  private long mSeconds;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
    mapFragment.getMapAsync(this);
  }

  @Override
  public void onMapReady(final GoogleMap googleMap) {
    googleMap.moveCamera(CameraUpdateFactory.newLatLng(INITIAL_LATLNG));
    googleMap.moveCamera(CameraUpdateFactory.zoomTo(INITIAL_ZOOM));
    Firebase firebaseRef = new Firebase(FIREBASE_URL).child(FIREBASE_CHILD);

    firebaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot snapshot) {
        ArrayMap<Long, RunnerLocation> arrayMap = new ArrayMap<>();
        long start = 0;

        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
          RunnerLocation runnerLocation = dataSnapshot.getValue(RunnerLocation.class);
          long timeInSeconds = Math.round(runnerLocation.date.getTime() / 1000);

          if (start == 0) {
            start = timeInSeconds;
          }

          Log.d(TAG, String.format("%d  --  %s", timeInSeconds, runnerLocation.shortAddress));
          arrayMap.put(timeInSeconds, runnerLocation);
        }

        showLocations(start, arrayMap, googleMap);
      }

      @Override
      public void onCancelled(FirebaseError firebaseError) {
        // No op
      }
    });
  }

  private void showLocations(long start, final ArrayMap<Long, RunnerLocation> arrayMap, final GoogleMap googleMap) {
    final Handler handler = new Handler();
    mSeconds = start;
    final PolylineOptions rectOptions = new PolylineOptions();

    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        try {
          if (arrayMap.containsKey(mSeconds)) {
            RunnerLocation runnerLocation = arrayMap.get(mSeconds);
            LatLng point = new LatLng(runnerLocation.latitude, runnerLocation.longitude);

            googleMap.addMarker(new MarkerOptions()
                .position(point)
                .draggable(false)
                .anchor(0.5f, 0.5f)
                .snippet(runnerLocation.shortAddress)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.slider_knob))
                .title(runnerLocation.shortAddress));

            rectOptions.add(point).color(Color.RED).width(6);
            googleMap.addPolyline(rectOptions);
          }

          mSeconds++;
        } catch (Exception e) {
          Log.e(TAG, "Error updating map", e);
        } finally {
          handler.postDelayed(this, 5);
        }
      }
    };

    handler.postDelayed(runnable, 5);
  }
}
