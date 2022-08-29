package project.clone.daraz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class splashActivity extends AppCompatActivity {

    private static int SPLASH_SCREEN = 4150;

    Animation bottom,top;
    TextView txtslogan,txt;
    ImageView logo;

    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setStatusBarColor(Color.BLACK);
        setContentView(R.layout.activity_splash);

        bottom= AnimationUtils.loadAnimation(this,R.anim.bottom);
        top=AnimationUtils.loadAnimation(this,R.anim.top);

        txtslogan=findViewById(R.id.slogan);
        txt=findViewById(R.id.gtbs);
        logo=findViewById(R.id.logo);

        txtslogan.setAnimation(bottom);
        txt.setAnimation(bottom);
        logo.setAnimation(bottom);

        firebaseAuth = FirebaseAuth.getInstance();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null){
                    //user not logged in start login activity
                    startActivity(new Intent(splashActivity.this,LoginActivity.class));
                    finish();
                }
                else {
                    //user is logged in,check user type
                    checkuserType();
                }
            }
        },1000);
    }

    private void checkuserType() {
        //if user is seller , start seller main

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String accountType = "" + ds.child("accountType").getValue();
                            if (accountType.equals("Seller")) {
                                //user is seller
                                startActivity(new Intent(splashActivity.this, MainSeller.class));
                                finish();
                            }
                            else{

                                //user is buyer
                                startActivity(new Intent(splashActivity.this, MainUser.class));
                                finish();
                            }
                        }
                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}