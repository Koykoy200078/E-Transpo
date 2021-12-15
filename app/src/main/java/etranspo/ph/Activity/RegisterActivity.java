package etranspo.ph.Activity;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Objects;

import etranspo.ph.R;

public class RegisterActivity extends AppCompatActivity
{
    private TextInputEditText userName, emailAddress, password, mobile, fullName, address;
    private RadioGroup radioGroup;
    private ProgressBar progressBar;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Register");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> startActivity(new Intent(RegisterActivity.this, LoginActivity.class)));

        firebaseAuth = FirebaseAuth.getInstance();

        userName = findViewById(R.id.username);
        emailAddress = findViewById(R.id.email);
        password = findViewById(R.id.password);
        mobile = findViewById(R.id.phone);
        fullName = findViewById(R.id.fullname);
        address = findViewById(R.id.address);
        radioGroup = findViewById(R.id.radioButton);
        Button registerBtn = findViewById(R.id.register);
        progressBar = findViewById(R.id.progressBar);
        registerBtn.setOnClickListener(v -> {
            final String user_name = Objects.requireNonNull(userName.getText()).toString();
            final String email = Objects.requireNonNull(emailAddress.getText()).toString();
            final String txt_password = Objects.requireNonNull(password.getText()).toString();
            final String txt_fullname = Objects.requireNonNull(fullName.getText()).toString();
            final String txt_address = Objects.requireNonNull(address.getText()).toString();
            final String txt_mobile = Objects.requireNonNull(mobile.getText()).toString();
            int checkedId = radioGroup.getCheckedRadioButtonId();
            RadioButton selected_gender = radioGroup.findViewById(checkedId);
            if(selected_gender == null)
            {
                Toast.makeText(RegisterActivity.this, "Select your gender", Toast.LENGTH_SHORT).show();
            }
                else
            {
                final String gender = selected_gender.getText().toString();
                if(TextUtils.isEmpty(user_name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(txt_password) || TextUtils.isEmpty(txt_fullname) || TextUtils.isEmpty(txt_address) || TextUtils.isEmpty(txt_mobile))
                {
                    Toast.makeText(RegisterActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
                }
                    else
                {
                    register(user_name, email, txt_password, txt_fullname, txt_address, txt_mobile, gender);
                }
            }
        });
    }

    private void register(String user_name, String email, String txt_password, String txt_fullname, String txt_address, String txt_mobile, String gender)
    {
        progressBar.setVisibility(View.VISIBLE);
        firebaseAuth.createUserWithEmailAndPassword(email, txt_password).addOnCompleteListener(task -> {
            if(task.isSuccessful())
            {
                FirebaseUser rUser = firebaseAuth.getCurrentUser();
                assert rUser != null;
                String userId = rUser.getUid();
                databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("userId", userId);
                hashMap.put("username", user_name);
                hashMap.put("email", email);
                hashMap.put("fullname", txt_fullname);
                hashMap.put("address", txt_address);
                hashMap.put("mobile", txt_mobile);
                hashMap.put("gender", gender);
                hashMap.put("imageUrl", "default");
                databaseReference.setValue(hashMap).addOnCompleteListener(task1 -> {
                    progressBar.setVisibility(View.GONE);
                    if(task1.isSuccessful())
                    {
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                    else
                    {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(RegisterActivity.this, Objects.requireNonNull(task1.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else
            {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(RegisterActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}