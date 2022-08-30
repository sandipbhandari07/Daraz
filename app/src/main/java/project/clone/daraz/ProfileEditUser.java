package project.clone.daraz;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
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
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ProfileEditUser extends AppCompatActivity implements LocationListener{

    private ImageButton backbtn, gpsbtn;
    private ImageView profile;
    private EditText namet,  phonet, countryet, stateet, cityet, addresset;
    private Button updatebtn;

    private static final int LOCATION_REQUEST_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;

    private String[] camerapermisson;
    private String[] storagepermisson;
    private String[] locationpermisson;

    //image picked uri
    private Uri image_uri;

    private double latitude, longitude;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    private LocationManager locationManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setStatusBarColor(Color.BLACK);
        setContentView(R.layout.activity_profile_edit_user);

        backbtn = findViewById(R.id.eu_backbtn_register_seller);
        gpsbtn = findViewById(R.id.eu_gps_seller);
        profile = findViewById(R.id.eu_profileIv_seller);
        namet = findViewById(R.id.eu_fullname_selller);
        phonet = findViewById(R.id.eu_phonenumber_seller);
        countryet = findViewById(R.id.eu_country_seller);
        stateet = findViewById(R.id.eu_state_seller);
        cityet = findViewById(R.id.eu_city_seller);
        addresset = findViewById(R.id.eu_completeaddress_selller);
        updatebtn=findViewById(R.id.eu_login_btn_update_seller);


        //init permisson array
        locationpermisson = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        camerapermisson = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagepermisson = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);
        checkUser();


        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //pick image
                showImagepickDialog();
            }
        });
        updatebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                inputData();
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
                if (checklocalpermisson()) {
                    detectlocation();
                } else {
                    requestlocationpermisson();
                }
            }
        });
    }

    private String sname,sphone,scountry,sstate,scity,saddress;
    private void inputData() {
        //inputdata
        sname=namet.getText().toString().trim();
        sphone=phonet.getText().toString().trim();
        scountry=countryet.getText().toString().trim();
        sstate=stateet.getText().toString().trim();
        scity=cityet.getText().toString().trim();
        saddress=addresset.getText().toString().trim();

        updateprofile();
    }

    private void updateprofile() {
        progressDialog.setMessage("updating Profile......");
        progressDialog.show();

        if (image_uri == null){
            //update without image

            //setup data to update
            HashMap<String,Object> hashMap = new HashMap<>();
            hashMap.put("name",""+sname);
            hashMap.put("phone", "" + sphone);
            hashMap.put("country", "" + scountry);
            hashMap.put("state", "" + sstate);
            hashMap.put("city", "" + scity);
            hashMap.put("address", "" + saddress);
          //  hashMap.put("latitude", "" + latitude);
          //  hashMap.put("longitude", "" + longitude);

            //update to Db
            DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
            reference.child(firebaseAuth.getUid()).updateChildren(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            //updated
                            progressDialog.dismiss();
                            Toast.makeText(ProfileEditUser.this, "profile updated....", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //failed to update
                            progressDialog.dismiss();
                            Toast.makeText(ProfileEditUser.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }
        else {
            //update with image

            //image first
            String filepathandname = "profile_image/" + "" + firebaseAuth.getUid();

            //upload image
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filepathandname);
            storageReference.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful()) ;
                            Uri downloadimageuri = uriTask.getResult();
                            if (uriTask.isSuccessful()) {

                                //setup data to update
                                HashMap<String,Object> hashMap = new HashMap<>();
                                hashMap.put("name",""+sname);
                                hashMap.put("phone", "" + sphone);
                                hashMap.put("country", "" + scountry);
                                hashMap.put("state", "" + sstate);
                                hashMap.put("city", "" + scity);
                                hashMap.put("address", "" + saddress);
                                hashMap.put("latitude", "" + latitude);
                                hashMap.put("longitude", "" + longitude);
                                hashMap.put("profileImage", downloadimageuri);

                                //update to Db
                                DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
                                reference.child(firebaseAuth.getUid()).updateChildren(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                //updated
                                                progressDialog.dismiss();
                                                Toast.makeText(ProfileEditUser.this, "profile updated....", Toast.LENGTH_SHORT).show();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //failed to update
                                                progressDialog.dismiss();
                                                Toast.makeText(ProfileEditUser.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });

                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ProfileEditUser.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void checkUser() {
        FirebaseUser user= firebaseAuth.getCurrentUser();
        if (user == null)
        {
            startActivity(new Intent(ProfileEditUser.this,LoginActivity.class));
            finish();
        }
        else{
            loadMyinfo();
        }
    }


    private void loadMyinfo() {
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren())
                        {
                            String name=""+ds.child("name").getValue();
                            String accountType= ""+ds.child("accountType").getValue();
                            String address= ""+ds.child("address").getValue();
                            String city= ""+ds.child("city").getValue();
                            String state= ""+ds.child("state").getValue();
                            String country= ""+ds.child("country").getValue();
                            String email= ""+ds.child("email").getValue();
                        //    latitude= Double.parseDouble(""+ds.child("latitude").getValue());
                         //   longitude=Double.parseDouble( ""+ds.child("longitude").getValue());
                            String online= ""+ds.child("online").getValue();
                            String phone= ""+ds.child("phone").getValue();
                            String profileImage= ""+ds.child("profileImage").getValue();
                            String timestamp= ""+ds.child("timestamp").getValue();
                            String uid= ""+ds.child("uid").getValue();

                            namet.setText(name);
                            phonet.setText(phone);
                            countryet.setText(country);
                            stateet.setText(state);
                            cityet.setText(city);
                            addresset.setText(address);
                            try {
                                Picasso.get().load(profileImage).placeholder(R.drawable.logoma).into(profile);
                            }catch (Exception e)
                            {
                                profile.setImageResource(R.drawable.ic_baseline_person_24);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    private void pickfromgallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private void showImagepickDialog() {
        String[] options = {"Camera", "Gallery"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image").setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0) {
                    // camera picked
                    if (checkcamerapermisson()) {
                        pickfromcamera();
                    } else {
                        requestcamerapermisson();
                    }
                } else {
                    //gallery clicked
                    if (checkstoragepermisson()) {
                        pickfromgallery();
                    } else {
                        requestedstoragepermisson();
                    }
                }
            }
        }).show();
    }

    private void pickfromcamera() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_Image Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp_Image Description");

        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }

    private boolean checklocalpermisson(){
        boolean result= ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private void requestlocationpermisson()
    {
        ActivityCompat.requestPermissions(this,locationpermisson,LOCATION_REQUEST_CODE);
    }
    private void detectlocation() {
        Toast.makeText(this, "Please wait....", Toast.LENGTH_LONG).show();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
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

    @Override
    public void onLocationChanged(@NonNull Location location) {
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
    private boolean checkstoragepermisson(){
        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==
                (PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private void requestedstoragepermisson(){
        ActivityCompat.requestPermissions(this,storagepermisson,STORAGE_REQUEST_CODE);
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