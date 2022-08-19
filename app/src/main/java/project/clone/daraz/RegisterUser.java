package project.clone.daraz;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;

public class RegisterUser extends AppCompatActivity implements LocationListener{

    private ImageButton backbtn,gpsbtn;
    private ImageView profile;
    private EditText name,phone,countryet,stateet,cityet,addresset,email,password,confirmpassword;
    private Button registerbtn;
    private TextView registerSeller;


    private static final int LOCATION_REQUEST_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;

    private String[] camerapermisson;
    private String[] storagepermisson;

    //image picked uri
    private Uri image_uri;

    private double latitude,longitude;
    private String[] locationpermisson;

    private LocationManager locationManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setStatusBarColor(Color.BLACK);
        setContentView(R.layout.activity_register_user);

        backbtn=findViewById(R.id.backbtn_register);
        gpsbtn=findViewById(R.id.gps);
        profile=findViewById(R.id.profileIv);
        name=findViewById(R.id.fullname);
        phone=findViewById(R.id.phonenumber);
        countryet=findViewById(R.id.country);
        stateet=findViewById(R.id.state);
        email=findViewById(R.id.email_register);
        cityet=findViewById(R.id.city);
        addresset=findViewById(R.id.completeaddress);
        password=findViewById(R.id.password_register);
        confirmpassword=findViewById(R.id.confirm_password);
        registerbtn=findViewById(R.id.login_btn_register);
        registerSeller=findViewById(R.id.donthaveaccount_register);

        locationpermisson = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        camerapermisson=new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagepermisson=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        registerSeller.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(RegisterUser.this,RegisterSeller.class);
                startActivity(intent);
            }
        });

        registerbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //register user
            }
        });

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //pick image
                showImagepickDialog();
            }
        });
        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        gpsbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //detect current location
                if (checklocalpermisson()){
                    detectlocation();
                }
                else {
                    requestlocationpermisson();
                }
            }
        });
    }


    private void showImagepickDialog() {
        String[] options = {"Camera", "Gallery"};
        //dialog
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Pick Image").setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i==0)
                {
                    // camera picked
                    if (checkcamerapermisson()){
                        pickfromcamera();
                    }
                    else {
                        requestcamerapermisson();
                    }
                }
                else {
                    //gallery clicked
                    if (checkstoragepermisson()){
                        pickfromgallery();
                    }
                    else {
                        requestedstoragepermisson();
                    }
                }
            }
        }).show();
    }

    private void pickfromgallery(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);
    }
    private void pickfromcamera(){
        ContentValues contentValues=new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE,"Temp_Image Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"Temp_Image Description");

        image_uri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(intent,IMAGE_PICK_CAMERA_CODE);
    }
    private void detectlocation() {
        Toast.makeText(this, "Please wait....", Toast.LENGTH_LONG).show();
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }
    private void findAddress() {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder= new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude,longitude,1);

            String address=addresses.get(0).getAddressLine(0);
            String city=addresses.get(0).getLocality();
            String state=addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();

            //set address
            countryet.setText(country);
            stateet.setText(state);
            cityet.setText(city);
            addresset.setText(address);

        }
        catch (Exception e)
        {
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checklocalpermisson(){
        boolean result= ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private void requestlocationpermisson()
    {
        ActivityCompat.requestPermissions(this,locationpermisson,LOCATION_REQUEST_CODE);
    }

    private boolean checkstoragepermisson(){
        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==
                (PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private void requestedstoragepermisson(){
        ActivityCompat.requestPermissions(this,storagepermisson,STORAGE_REQUEST_CODE);
    }
    private boolean checkcamerapermisson(){
        boolean result = ContextCompat.checkSelfPermission(this
                ,Manifest.permission.CAMERA)==(PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==
                (PackageManager.PERMISSION_GRANTED);

        return result && result1;
    }
    private void requestcamerapermisson(){
        ActivityCompat.requestPermissions(this,camerapermisson,CAMERA_REQUEST_CODE);
    }


    @Override
    public void onLocationChanged(@NonNull Location location) {
        //location detected
        latitude = location.getLatitude();
        longitude=location.getLongitude();
        findAddress();
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        //gps disabled
        Toast.makeText(this, "Please turn on location", Toast.LENGTH_SHORT).show();
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case LOCATION_REQUEST_CODE:{
                if (grantResults.length>0)
                {
                    boolean locationaccepted = grantResults [0] == PackageManager.PERMISSION_GRANTED;
                    if (locationaccepted)
                    {
                        //permisson allowed
                        detectlocation();
                    }
                    else {
                        //permisson denied
                        Toast.makeText(this, "Location permisson is necessary....", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case CAMERA_REQUEST_CODE:{
                if (grantResults.length>0)
                {
                    boolean cameraAccepted = grantResults [0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults [1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted)
                    {
                        //permisson allowed
                        pickfromcamera();
                    }
                    else {
                        //permisson denied
                        Toast.makeText(this, "camera permisson is necessary....", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE:{
                if (grantResults.length>0)
                {
                    boolean storageAccepted = grantResults [1] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted)
                    {
                        //permisson allowed
                        pickfromgallery();
                    }
                    else {
                        //permisson denied
                        Toast.makeText(this, "storage permisson is necessary....", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }


        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode==RESULT_OK){
            if (requestCode == IMAGE_PICK_GALLERY_CODE)
            {
                image_uri=data.getData();
                profile.setImageURI(image_uri);
            }
            else if (requestCode==IMAGE_PICK_CAMERA_CODE){
                profile.setImageURI(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}