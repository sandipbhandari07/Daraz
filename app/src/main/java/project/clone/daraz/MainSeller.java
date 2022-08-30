package project.clone.daraz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class MainSeller extends AppCompatActivity {

    private TextView nametv,shopnametv,emailtv,tabproductstv,taborderstv,filterproductstv;
    private ImageButton logoutbtn,editprofile,addproductbtn,filterproductbtn;
    private ImageView profileIv;
    private EditText searchproducts;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private RelativeLayout productsrl,ordersrl;
    private RecyclerView productsrv;

    private AdapterProuctsSeller adapter;
    private ArrayList<ModelProduct> productslist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setStatusBarColor(Color.BLACK);
        setContentView(R.layout.activity_main_seller);

        nametv=findViewById(R.id.SnameTv);
        logoutbtn=findViewById(R.id.Slogoutbtn);
        editprofile=findViewById(R.id.Sedit);
        profileIv=findViewById(R.id.SprofileIv);
        addproductbtn=findViewById(R.id.Sadd);
        shopnametv=findViewById(R.id.SshopenameTv);
        emailtv=findViewById(R.id.Semailtv);
        taborderstv=findViewById(R.id.sorderstv);
        tabproductstv=findViewById(R.id.sproductstv);
        productsrl=findViewById(R.id.productsRl);
        ordersrl=findViewById(R.id.ordersRl);
        searchproducts=findViewById(R.id.searchproductet);
        filterproductbtn=findViewById(R.id.sfilterproductbtn);
        filterproductstv=findViewById(R.id.sfilterproductstv);

        productsrv=findViewById(R.id.sproductsRv);



        progressDialog= new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);
        firebaseAuth= FirebaseAuth.getInstance();
        checkUser();
        loadallproducts();

        showproductsui();

        searchproducts.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    adapter.getFilter().filter(charSequence);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        editprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //open edit profile activity
                startActivity(new Intent(MainSeller.this,ProfileEditSeller.class));
            }
        });
        logoutbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //makeoffline
                //signout
                //gotologin activity
                makemeoffline();

            }
        });
        addproductbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainSeller.this,AddProduct.class));
            }
        });
        tabproductstv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //load products
                showproductsui();
            }
        });
        taborderstv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //load orders
                showordersui();
            }
        });
        filterproductbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder=new AlertDialog.Builder(MainSeller.this);
                builder.setTitle("Choose Category:")
                        .setItems(Constants.productcategories1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String selected = Constants.productcategories1[i];
                                filterproductstv.setText(selected);
                                if (selected.equals("All"))
                                {
                                    loadallproducts();
                                }
                                else
                                {
                                    loadfiltereproducts(selected);
                                }
                            }
                        })
                        .show();
            }
        });
    }

    private void loadfiltereproducts(String selected) {
        productslist=new ArrayList<>();
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        productslist.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            String productcategory = ""+ds.child("productcategory").getValue();
                            if (selected.equals(productcategory))
                            {
                                ModelProduct modelProduct=ds.getValue(ModelProduct.class);
                                productslist.add(modelProduct);
                            }

                        }
                        adapter=new AdapterProuctsSeller(MainSeller.this,productslist);

                        productsrv.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadallproducts() {

        productslist=new ArrayList<>();
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        productslist.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            ModelProduct modelProduct=ds.getValue(ModelProduct.class);
                            productslist.add(modelProduct);
                        }
                        adapter=new AdapterProuctsSeller(MainSeller.this,productslist);

                        productsrv.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void showordersui() {
        //showp [roducts ui and hide orders ui
        ordersrl.setVisibility(View.VISIBLE);
        productsrl.setVisibility(View.GONE);

        taborderstv.setTextColor(getResources().getColor(R.color.black));
        taborderstv  .setBackgroundResource(R.drawable.shape_rect_01);

        tabproductstv.setTextColor(getResources().getColor(R.color.colorwhite));
        tabproductstv.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }

    private void showproductsui() {
        //showp [roducts ui and hide orders ui
        productsrl.setVisibility(View.VISIBLE);
        ordersrl.setVisibility(View.GONE);

        tabproductstv.setTextColor(getResources().getColor(R.color.black));
        tabproductstv.setBackgroundResource(R.drawable.shape_rect_01);

        taborderstv.setTextColor(getResources().getColor(R.color.colorwhite));
        taborderstv.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }

    private void makemeoffline() {
        progressDialog.setMessage("Logging Out...");

        HashMap<String,Object> hashMap =new HashMap<>();
        hashMap.put("online","false");

        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        //update successs
                        firebaseAuth.signOut();
                        checkUser();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(MainSeller.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUser() {
        FirebaseUser user= firebaseAuth.getCurrentUser();
        if (user == null)
        {
            startActivity(new Intent(MainSeller.this,LoginActivity.class));
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
                            String email =""+ds.child("email").getValue();
                            String shopname=""+ds.child("shopname").getValue();
                            String profileimgae=""+ds.child("profileImage").getValue();

                            nametv.setText(name);
                            shopnametv.setText(shopname);
                            emailtv.setText(email);
                            try {
                                Picasso.get().load(profileimgae).placeholder(R.drawable.shop).into(profileIv);
                            }
                            catch (Exception e){

                                profileIv.setImageResource(R.drawable.shop);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}