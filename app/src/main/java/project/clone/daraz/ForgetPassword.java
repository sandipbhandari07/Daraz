package project.clone.daraz;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

public class ForgetPassword extends AppCompatActivity {


    private ImageButton backbtn;
    private EditText emailrecover;
    private Button recoverbtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        backbtn=findViewById(R.id.backbtn);
        recoverbtn=findViewById(R.id.recover_btn);
        emailrecover=findViewById(R.id.emailrecover);

        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }
}