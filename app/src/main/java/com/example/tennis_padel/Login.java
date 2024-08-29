package com.example.tennis_padel;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;

public class Login extends AppCompatActivity {
    private LoginViewModel loginViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        initUI();

        // Observe the ban status
        loginViewModel.isUserBanned.observe(this, banned -> {
            if (banned) {
                Toast.makeText(Login.this, "Your account has been banned.", Toast.LENGTH_LONG).show();
            }
        });

        // Observe the ban status
        loginViewModel.isUserSuspended.observe(this, suspended -> {
            if (suspended) {
                Toast.makeText(Login.this, "Your account has been suspended.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void initUI() {
        TextInputEditText editTextEmail = findViewById(R.id.email);
        TextInputEditText editTextPassword = findViewById(R.id.password);
        MaterialTextView textViewRegister = findViewById(R.id.registerNow);
        MaterialButton buttonLog = findViewById(R.id.loginButton);
        ProgressBar progressBar = findViewById(R.id.progressBar);

        buttonLog.setOnClickListener(view -> {
            String email = editTextEmail.getText().toString().trim();
            String password = String.valueOf(editTextPassword.getText());
            if (email.isEmpty() || password.isEmpty())
                Toast.makeText(this, "Fill the Login field correctly", Toast.LENGTH_SHORT).show();
            else loginViewModel.loginUser(email, password);
        });

        // If user is authenticated the main activity gets selected
        loginViewModel.userAuthenticated.observe(this, isAuthenticated -> {
            if (isAuthenticated) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                UserDataRepository.getInstance().setIsFromLogin(true);
                finish();
            }
        });

        // Error message if the authentication fails
        loginViewModel.authenticationFailed.observe(this, isFailed -> {
            if (isFailed) {
                Toast.makeText(Login.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
            }
        });

        loginViewModel.isLoading.observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        // If the sign up text is clicked the register activity is selected
        textViewRegister.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), Register.class));
            finish();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // If user is already logged in than main activity gets selected
        if (loginViewModel.isUserLoggedIn()) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }
    }
}
