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

public class Register extends AppCompatActivity {
    private RegisterViewModel registerViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        registerViewModel = new ViewModelProvider(this).get(RegisterViewModel.class);
        initUI();
    }

    private void initUI() {
        TextInputEditText editTextEmail = findViewById(R.id.email);
        TextInputEditText editTextPassword = findViewById(R.id.password);
        MaterialTextView textViewLogin = findViewById(R.id.loginNow);
        MaterialButton buttonReg = findViewById(R.id.registerButton);
        ProgressBar progressBar = findViewById(R.id.progressBar);

        buttonReg.setOnClickListener(view -> {
            String email = editTextEmail.getText().toString().trim();
            String password = String.valueOf(editTextPassword.getText());
            if (email.isEmpty() || password.isEmpty())
                Toast.makeText(this, "Fill the Register field correctly", Toast.LENGTH_SHORT).show();
            else
                registerViewModel.registerUser(email, password);
        });

        // If user is authenticated the main activity gets selected
        registerViewModel.userRegistered.observe(this, isRegistered -> {
            if (isRegistered) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            }
        });

        // Error message if the registration fails
        registerViewModel.registrationFailed.observe(this, isFailed -> {
            if (isFailed) {
                Toast.makeText(Register.this, "Registration failed.", Toast.LENGTH_SHORT).show();
            }
        });

        registerViewModel.isLoading.observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        // If the sign in text is clicked the login activity is selected
        textViewLogin.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), Login.class));
            finish();
        });
    }
}