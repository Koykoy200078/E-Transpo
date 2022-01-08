package etranspo.ph.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

import etranspo.ph.R;

public class ChangePasswordActivity extends AppCompatActivity
{
    private TextInputEditText oldPsw, newPsw, confirmPsw;
    private FirebaseUser firebaseUser;
    private FirebaseAuth firebaseAuth;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Change Password");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> startActivity(new Intent(ChangePasswordActivity.this, MainActivity.class)));

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        oldPsw = findViewById(R.id.oldPassword);
        newPsw = findViewById(R.id.newPassword);
        confirmPsw = findViewById(R.id.confirmPassword);
        Button changePsw = findViewById(R.id.resetPassword);
        progressBar = findViewById(R.id.progressBar);
        changePsw.setOnClickListener(v ->
        {
            String txtOldPsw = Objects.requireNonNull(oldPsw.getText()).toString();
            String txtNewPsw = Objects.requireNonNull(newPsw.getText()).toString();
            String txtConPsw = Objects.requireNonNull(confirmPsw.getText()).toString();
            if (txtOldPsw.isEmpty() || txtNewPsw.isEmpty() || txtConPsw.isEmpty())
            {
                Toast.makeText(ChangePasswordActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
            }
                else if (txtNewPsw.length() < 6)
            {
                Toast.makeText(ChangePasswordActivity.this, "The new password length should be more than 6 characters.", Toast.LENGTH_SHORT).show();
            }
                else if (!txtConPsw.equals(txtNewPsw))
            {
                Toast.makeText(ChangePasswordActivity.this, "Confirm password does not match new password", Toast.LENGTH_SHORT).show();
            }
                else
            {
                changePassword(txtOldPsw, txtNewPsw);
            }
        });
    }

    private void changePassword(String txtOldPsw, String txtNewPsw)
    {
        progressBar.setVisibility(View.VISIBLE);
        AuthCredential credential = EmailAuthProvider.getCredential(Objects.requireNonNull(firebaseUser.getEmail()), txtOldPsw);
        firebaseUser.reauthenticate(credential).addOnCompleteListener(task ->
        {
            if (task.isSuccessful())
            {
                firebaseUser.updatePassword(txtNewPsw).addOnCompleteListener(task1 ->
                {
                    if (task1.isSuccessful())
                    {
                        firebaseAuth.signOut();
                        Intent intent = new Intent(ChangePasswordActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                        else
                    {
                        Toast.makeText(ChangePasswordActivity.this, Objects.requireNonNull(task1.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
                else
            {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ChangePasswordActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}