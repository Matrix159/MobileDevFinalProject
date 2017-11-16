package com.matrix159.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SignUpActivity extends AppCompatActivity {

    private static final Pattern EMAIL_REGEX = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
            Pattern.CASE_INSENSITIVE);

    @BindView(R.id.email_input) EditText email;
    @BindView(R.id.password_input) EditText password;
    @BindView(R.id.verify_pass_input) EditText verifyPassword;
    @BindView(R.id.toolbar) Toolbar toolbar;
    Animation shake;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);
        mAuth = FirebaseAuth.getInstance();
        shake = AnimationUtils.loadAnimation(this, R.anim.shake);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @OnClick(R.id.signup_button)
    public void verify() {
        String emailStr = email.getText().toString();
        if (emailStr.length() == 0) {
            Snackbar.make(email, R.string.email_required,
                    Snackbar.LENGTH_LONG).show();
            return;
        }
        if (!EMAIL_REGEX.matcher(emailStr).find()) {
            Snackbar.make(email, R.string.incorrect_email,
                    Snackbar.LENGTH_LONG).show();
            return;
        }
        String passStr = password.getText().toString();
        String verifyPassStr = verifyPassword.getText().toString();
        if (!verifyPassStr.equals(passStr)) {
            verifyPassword.startAnimation(shake);
            password.startAnimation(shake);
            return;
        }

        mAuth.createUserWithEmailAndPassword(emailStr, passStr).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                intent.putExtra("email", emailStr);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                String msg = task.getException().getMessage();
                Snackbar.make(email, msg, Snackbar.LENGTH_SHORT).show();
            }
        });

    }

}
