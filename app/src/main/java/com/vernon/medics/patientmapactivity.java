package com.vernon.medics;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryDataEventListener;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

public class patientmapactivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    GoogleApiClient mgoogleapiclient;
    Location mlastlocation;
    LocationRequest mlocationrequest;

    private Button mlogout, mcall;
    private LatLng needlocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patientmapactivity);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mlogout = (Button) findViewById(R.id.logout);
        mcall = (Button) findViewById(R.id.call);
        try {
            mlogout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(patientmapactivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            mcall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("PatientRequest");
                    GeoFire geoFire = new GeoFire(ref);
                    geoFire.setLocation(userid, new GeoLocation(mlastlocation.getLatitude(),mlastlocation.getLongitude()));
                    needlocation = new LatLng(mlastlocation.getLatitude(),mlastlocation.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(needlocation).title("Patient here"));

                    mcall.setText("We are finding a doctor for you. Please be patient!...");

                    getClosestdoctor();
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "System Malfunction", Toast.LENGTH_SHORT).show();
        }

    }
    private int radius = 1;
    private Boolean doctorfound = false;
    private String doctorfoundid;

    private  void getClosestdoctor(){
        DatabaseReference doctorlocation = FirebaseDatabase.getInstance().getReference().child("doctorAvailable");
        GeoFire geoFire = new GeoFire(doctorlocation);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(needlocation.latitude,needlocation.longitude), radius);
       geoQuery.removeAllListeners();


       geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
           public void onKeyEntered(String key, GeoLocation location) {
                if (!doctorfound){
                     doctorfound = true;
                   doctorfoundid = key;


                   DatabaseReference doctorRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Doctors").child(doctorfoundid);
                   String patientsId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    HashMap map = new HashMap();map.put("PatientServiceId",patientsId);
                    doctorRef.updateChildren(map);
                    
                    
                    getdoctorlocaion();
                    mcall.setText("Finding doctor location for you please be patient...");
                }

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (!doctorfound)
                {
                    radius++;
                    getClosestdoctor();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
       });


    }
    private Marker mDoctorMarker;
    private void getdoctorlocaion(){
        DatabaseReference doctorlocationRef = FirebaseDatabase.getInstance().getReference().child("DoctorsWorking").child(doctorfoundid).child("l");
        doctorlocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    mcall.setText("Doctor found!");
                    if (map.get(0) != null) {
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if (map.get(0) != null) {
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng doctorLatLng = new LatLng(locationLat, locationLng);
                    if (mDoctorMarker != null) {
                        mDoctorMarker.remove();
                    }
                    Location loc1 = new Location("");
                    loc1.setLatitude(needlocation.latitude);
                    loc1.setLongitude(needlocation.longitude);

                    Location loc2 = new Location("");
                    loc2.setLatitude(doctorLatLng.latitude);
                    loc2.setLongitude(doctorLatLng.longitude);

                    float distance = loc1.distanceTo(loc2);
                    if (distance < 100) {
                        mcall.setText("Doctor has Arrived!");
                    } else {
                        mcall.setText("Doctor has Arrived!"+ String.valueOf(distance));
                    }

                    mDoctorMarker = mMap.addMarker(new MarkerOptions().position(doctorLatLng).title("your doctor"));
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

   @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
    }

    protected synchronized void buildGoogleApiClient(){
        mgoogleapiclient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mgoogleapiclient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        mlastlocation = location;
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(17));

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
       DatabaseReference ref = FirebaseDatabase.getInstance().getReference("doctorAvailable");

       GeoFire geoFire = new GeoFire(ref);
        geoFire.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLatitude()));
    }
    @SuppressLint("RestrictedApi")
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mlocationrequest = new LocationRequest();
        mlocationrequest.setInterval(1000);
        mlocationrequest.setFastestInterval(1000);
        mlocationrequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mgoogleapiclient, mlocationrequest, this);



    }

    @Override
    public void onConnectionSuspended(int i) {


    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

     @Override
  protected void onStop() {
      super.onStop();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
          DatabaseReference ref = FirebaseDatabase.getInstance().getReference("doctorAvailable");

        GeoFire geoFire = new GeoFire(ref);
       geoFire.removeLocation(userId) ;
    }
}
