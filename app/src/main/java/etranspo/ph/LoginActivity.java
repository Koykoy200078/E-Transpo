package etranspo.ph;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity
{
    private TextView forgotPassword;
    private EditText email, password;
    private ProgressBar progressBar;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        TextView btn = findViewById(R.id.register);
        btn.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
        Button loginBtn = findViewById(R.id.login);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        progressBar = findViewById(R.id.progressBar);
        forgotPassword = findViewById(R.id.forgot);
        firebaseAuth = FirebaseAuth.getInstance();
        loginBtn.setOnClickListener(v ->
        {
            String text_email = email.getText().toString();
            String text_password = password.getText().toString();
            if(TextUtils.isEmpty(text_email) || TextUtils.isEmpty(text_password))
            {
                Toast.makeText(LoginActivity.this, "All fields required", Toast.LENGTH_SHORT).show();
            }
            else
            {
                login(text_email, text_password);
            }
        });

        forgotPassword.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, ForgetPasswordActivity.class)));
    }

    private void login(String text_email, String text_password)
    {
        progressBar.setVisibility(View.VISIBLE);
        firebaseAuth.signInWithEmailAndPassword(text_email, text_password).addOnCompleteListener(task ->
        {
            if (task.isSuccessful())
            {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
            else
            {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed()
    {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Exit?")
                .setMessage("Are you sure you want to close the app?")
                .setPositiveButton("Yes", (dialog, which) -> finish())
                .setNegativeButton("No", null)
                .show();
    }
}