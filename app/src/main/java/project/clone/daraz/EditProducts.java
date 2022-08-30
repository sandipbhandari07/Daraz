package project.clone.daraz;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class EditProducts extends AppCompatActivity {

    private ImageButton backbtn;
    private ImageView productet;
    private EditText titleet,descet;
    private TextView categoryet,quantityet,priceet,discountpreiceet,discountnoteet;
    private SwitchCompat discuntswitchet;
    private Button updateproductbtn;

    private String productId;

    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;

    private String[] camerapermisson;
    private String[] storagepermisson;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    //image picked uri
    private Uri image_uri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_products);

        productId=getIntent().getStringExtra("productid");
        backbtn=findViewById(R.id.edit_backbtn_register_seller);
        productet=findViewById(R.id.edit_producticonIV);
        titleet=findViewById(R.id.edit_title);
        descet=findViewById(R.id.edit_tdesc);
        categoryet=findViewById(R.id.edit_category);
        quantityet=findViewById(R.id.edit_quantity);
        priceet=findViewById(R.id.edit_price);
        discuntswitchet=findViewById(R.id.edit_switch_discountpriceEt);
        discountnoteet=findViewById(R.id.edit_discountnote);
        discountpreiceet=findViewById(R.id.edit_discountprice);
        updateproductbtn=findViewById(R.id.editproductbtn);

        discountpreiceet.setVisibility(View.GONE);
        discountnoteet.setVisibility(View.GONE);

        camerapermisson = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagepermisson = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);
        loadproductsDetails();


        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        discuntswitchet.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    discountpreiceet.setVisibility(View.VISIBLE);
                    discountnoteet.setVisibility(View.VISIBLE);
                }
                else {
                    discountpreiceet.setVisibility(View.GONE);
                    discountnoteet.setVisibility(View.GONE);
                }
            }
        });
        categoryet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                categoryDialog();
            }
        });
       updateproductbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //input data
                //validate data
                //add data to db
                inputdata();
            }
        });
        productet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showimagepickdialog();
            }
        });
    }

    private void loadproductsDetails() {
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Products").child(productId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String lproductid=""+snapshot.child("productid").getValue();
                        String lproducttitle=""+snapshot.child("producttitle").getValue();
                        String lproductdesc=""+snapshot.child("productdesc").getValue();
                        String lproductcategory=""+snapshot.child("productcategory").getValue();
                        String lproductquantity=""+snapshot.child("productquantity").getValue();
                        String lproducticon=""+snapshot.child("producticon").getValue();
                        String lorginalprice=""+snapshot.child("orginalprice").getValue();

                        String ldiscountprice=""+snapshot.child("discountprice").getValue();



                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private String producttitle,productdesc,productcate,productqunatity,orginalprice,discountprice,discountnotes;
    private boolean discountAvailable = false;

    private void inputdata() {

        producttitle=titleet.getText().toString().trim();
        productdesc=descet.getText().toString().trim();
        productcate=categoryet.getText().toString().trim();
        productqunatity=quantityet.getText().toString().trim();
        orginalprice=priceet.getText().toString().trim();
        discountAvailable=discuntswitchet.isChecked();

        if (TextUtils.isEmpty(producttitle)){
            Toast.makeText(this, "title required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(productcate))
        {
            Toast.makeText(this, "category required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(orginalprice))
        {
            Toast.makeText(this, "price required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (discountAvailable){
            discountprice=discountpreiceet.getText().toString().trim();
            discountnotes=discountnoteet.getText().toString().trim();
            if (TextUtils.isEmpty(discountprice)){
                Toast.makeText(this, "Discount Price is required", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        else {
            discountprice= "0";
            discountnotes = "";
        }
        addProduct();
    }
    private void addProduct() {
        progressDialog.setMessage("Adding Product....");
        progressDialog.show();
        String timestamp = "" +System.currentTimeMillis();
        if (image_uri == null) {
            //save  info without image

            //setup data to save
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("productid", "" +timestamp);
            hashMap.put("producttitle", "" + producttitle);
            hashMap.put("productdesc", "" + productdesc);
            hashMap.put("productcategory", "" + productcate);
            hashMap.put("productquantity", "" + productqunatity);
            hashMap.put("producticon", ""); //setempty
            hashMap.put("orginalprice", "" + orginalprice);
            hashMap.put("discountprice", "" + discountprice);
            hashMap.put("discountnotes", "" + discountnotes);
            hashMap.put("discountavailable", "" +discountAvailable);
            hashMap.put("timestamp", "" + timestamp);
            hashMap.put("uid", ""+firebaseAuth.getUid());

            //save to db
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.child(firebaseAuth.getUid()).child("Products").child(timestamp).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            progressDialog.dismiss();
                            Toast.makeText(EditProducts.this, "Product added", Toast.LENGTH_SHORT).show();
                            clearData();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(EditProducts.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            //save info with image
            String filepathandname = "profile_images/" + "" + timestamp;

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

                                //setup data to save
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("productid", "" +timestamp);
                                hashMap.put("producttitle", "" + producttitle);
                                hashMap.put("productdesc", "" + productdesc);
                                hashMap.put("productcategory", "" + productcate);
                                hashMap.put("productquantity", "" + productqunatity);
                                hashMap.put("producticon", ""+downloadimageuri);
                                hashMap.put("orginalprice", "" + orginalprice);
                                hashMap.put("discountprice", "" + discountprice);
                                hashMap.put("discountnotes", "" + discountnotes);
                                hashMap.put("discountavailable", "" +discountAvailable);
                                hashMap.put("timestamp", "" + timestamp);
                                hashMap.put("uid", ""+firebaseAuth.getUid());

                                //save to db
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                                reference.child(firebaseAuth.getUid()).child("Products").child(timestamp).setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                progressDialog.dismiss();
                                                Toast.makeText(EditProducts.this, "Product added", Toast.LENGTH_SHORT).show();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progressDialog.dismiss();
                                                Toast.makeText(EditProducts.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(EditProducts.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void clearData() {

        titleet.setText("");
        descet.setText("");
        categoryet.setText("");
        quantityet.setText("");
        priceet.setText("");
        discountpreiceet.setText("");
        discountnoteet.setText("");
        productet.setImageResource(R.drawable.shop);
        image_uri=null;
    }

    private void categoryDialog() {
        AlertDialog.Builder builder= new AlertDialog.Builder(this);

        builder.setTitle("Product Category");
        builder.setItems(Constants.productcategories, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String category= Constants.productcategories[i];

                categoryet.setText(category);
            }
        }).show();
    }


    private void showimagepickdialog() {

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
                        requeststoragepermisson();
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

    private boolean checkstoragepermisson(){
        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==
                (PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private void requeststoragepermisson(){
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case CAMERA_REQUEST_CODE:{
                if (grantResults.length>0)
                {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
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
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
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
                productet.setImageURI(image_uri);
            }
            else if (requestCode==IMAGE_PICK_CAMERA_CODE){
                productet.setImageURI(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}