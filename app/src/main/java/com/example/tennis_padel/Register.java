package com.example.tennis_padel;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
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
            String email = String.valueOf(editTextEmail.getText());
            String password = String.valueOf(editTextPassword.getText());
            registerViewModel.registerUser(email, password);
        });

        registerViewModel.userRegistered.observe(this, isRegistered -> {
            if (isRegistered) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            }
        });

        registerViewModel.registrationFailed.observe(this, isFailed -> {
            if (isFailed) {
                Toast.makeText(Register.this, "Registration failed.", Toast.LENGTH_SHORT).show();
            }
        });

        registerViewModel.isLoading.observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        textViewLogin.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), Login.class));
            finish();
        });
    }
}