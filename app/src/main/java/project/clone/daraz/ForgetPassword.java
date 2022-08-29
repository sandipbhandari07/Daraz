package project.clone.daraz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class ForgetPassword extends AppCompatActivity {


    private ImageButton backbtn;
    private EditText emailrecover;
    private Button recoverbtn;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setStatusBarColor(Color.BLACK);
        setContentView(R.layout.activity_forget_password);

        backbtn=findViewById(R.id.backbtn);
        recoverbtn=findViewById(R.id.recover_btn);
        emailrecover=findViewById(R.id.emailrecover);

        firebaseAuth=FirebaseAuth.getInstance();
        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        recoverbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recoverpassword();
            }
        });
    }

    private String fpemail;
    private void recoverpassword() {
        fpemail = emailrecover.getText().toString().trim();
        if (!Patterns.EMAIL_ADDRESS.matcher(fpemail).matches()) {
            Toast.makeText(this, "Invalid email...", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Sending instructions to reset password......");
        progressDialog.show();
        firebaseAuth.sendPasswordResetEmail(fpemail)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Toast.makeText(ForgetPassword.this, "password reset send to your email", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(ForgetPassword.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }
}