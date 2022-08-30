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
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.circularimageview.CircularImageView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class RegisterUser extends AppCompatActivity implements LocationListener {

    private ImageButton backbtn, gpsbtn;
    private CircularImageView profile;
    private EditText name, phone, countryet, stateet, cityet, addresset, email, password, confirmpassword;
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

    private double latitude, longitude;
    private String[] locationpermisson;

    private LocationManager locationManager;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setStatusBarColor(Color.BLACK);
        setContentView(R.layout.activity_register_user);

        backbtn = findViewById(R.id.backbtn_register);
        gpsbtn = findViewById(R.id.gps);
        profile = findViewById(R.id.profileIv);
        name = findViewById(R.id.fullname);
        phone = findViewById(R.id.phonenumber);
        countryet = findViewById(R.id.country);
        stateet = findViewById(R.id.state);
        email = findViewById(R.id.email_register);
        cityet = findViewById(R.id.city);
        addresset = findViewById(R.id.completeaddress);
        password = findViewById(R.id.password_register);
        confirmpassword = findViewById(R.id.confirm_password);
        registerbtn = findViewById(R.id.login_btn_register);
        registerSeller = findViewById(R.id.donthaveaccount_register);

        locationpermisson = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        camerapermisson = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagepermisson = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);


        registerSeller.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterUser.this, RegisterSeller.class);
                startActivity(intent);
            }
        });

        registerbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //register user
                inputData();
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
                if (checklocalpermisson()) {
                    detectlocation();
                } else {
                    requestlocationpermisson();
                }
            }
        });
    }

    private String Sfullname, Sphonnumberid, Scountrydid, Sstate, Scity, Saddress, Semailid, Spasswordid, Sconfirmpasswordid;

    private void inputData() {
        Sfullname = name.getText().toString().trim();
        Sphonnumberid = phone.getText().toString().trim();
        Scountrydid = countryet.getText().toString().trim();
        Sstate = stateet.getText().toString().trim();
        Scity = cityet.getText().toString().trim();
        Saddress = addresset.getText().toString().trim();
        Semailid = email.getText().toString().trim();
        Spasswordid = password.getText().toString().trim();
        Sconfirmpasswordid = confirmpassword.getText().toString().trim();

        if (TextUtils.isEmpty(Sfullname)) {
            Toast.makeText(this, "Enter Name......", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(Sphonnumberid)) {
            Toast.makeText(this, "Enter Name......", Toast.LENGTH_SHORT).show();
            return;
        }
        createaccount();
    }

    private void createaccount() {
        progressDialog.setMessage("creating account....");
        progressDialog.show();

        firebaseAuth.createUserWithEmailAndPassword(Semailid, Spasswordid).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        saveFirebaseData();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(RegisterUser.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveFirebaseData() {
        progressDialog.setMessage("saving account info....");

        String timestamp = "" + System.currentTimeMillis();
        if (image_uri == null) {
            //save  info without image

            //setup data to save
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("uid", "" + firebaseAuth.getUid());
            hashMap.put("email", "" + Semailid);
            hashMap.put("name", "" + Sfullname);
            hashMap.put("phone", "" + Sphonnumberid);
            hashMap.put("country", "" + Scountrydid);
            hashMap.put("state", "" + Sstate);
            hashMap.put("city", "" + Scity);
            hashMap.put("address", "" + Saddress);
         //   hashMap.put("latitude", "" + latitude);
          //  hashMap.put("longitude", "" + longitude);
            hashMap.put("password", "" + Spasswordid);
            hashMap.put("timestamp", "" + timestamp);
            hashMap.put("accountType", "User");
            hashMap.put("online", "true");
            hashMap.put("shopOpen", "true");
            hashMap.put("profileImage", "");

            //save to db
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.child(firebaseAuth.getUid()).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            progressDialog.dismiss();
                            startActivity(new Intent(RegisterUser.this, MainUser.class));
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            startActivity(new Intent(RegisterUser.this, MainUser.class));
                            finish();
                        }
                    });
        } else {
            //save info with image
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

                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("uid", "" + firebaseAuth.getUid());
                                hashMap.put("email", "" + Semailid);
                                hashMap.put("name", "" + Sfullname);
                                hashMap.put("phone", "" + Sphonnumberid);
                                hashMap.put("country", "" + Scountrydid);
                                hashMap.put("state", "" + Sstate);
                                hashMap.put("city", "" + Scity);
                                hashMap.put("password", "" + Spasswordid);
                                hashMap.put("address", "" + Saddress);
                              //  hashMap.put("latitude", "" + latitude);
                             //   hashMap.put("longitude", "" + longitude);
                                hashMap.put("timestamp", "" + timestamp);
                                hashMap.put("accountType", "Seller");
                                hashMap.put("online", "true");
                                hashMap.put("shopOpen", "true");
                                hashMap.put("profileImage", downloadimageuri);
                                //save to db
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                                reference.child(firebaseAuth.getUid()).setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                progressDialog.dismiss();
                                                startActivity(new Intent(RegisterUser.this, MainUser.class));
                                                finish();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progressDialog.dismiss();
                                                startActivity(new Intent(RegisterUser.this, MainUser.class));
                                                finish();
                                            }
                                        });
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(RegisterUser.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
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

    private void pickfromgallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
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

    private void detectlocation() {
        Toast.makeText(this, "Please wait....", Toast.LENGTH_LONG).show();
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
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