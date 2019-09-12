package ch.fhnw.ip6_feedbackapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginEmailActivity extends AppCompatActivity {
    EditText emailField;
    EditText passwordField;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_email);
        emailField = findViewById(R.id.editTextEmail);
        passwordField = findViewById(R.id.editTextPassword);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is already signed in
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(i);
                } else {

                }
            }
        };
    }

    public void login(View view) {
        String email = emailField.getText().toString();
        boolean isEmail = Patterns.EMAIL_ADDRESS.matcher(email).matches();
        String password = passwordField.getText().toString();
        if (isEmail && !password.isEmpty()) {
            //Login
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                // Login did not work
                                Toast t = Toast.makeText(getApplicationContext(), "Login fehlgeschlagen", Toast.LENGTH_LONG);
                                t.show();
                            } else {
                                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(i);
                            }
                        }
                    });
        } else {
            // Error: E-Mail erroneus and/or PW missing
            Toast t = Toast.makeText(getApplicationContext(), "Bitte geben Sie E-Mail und Passwort ein", Toast.LENGTH_LONG);
            t.show();
        }
    }

    public void setupNewAccount(View view) {
        String email = emailField.getText().toString();
        boolean isEmail = Patterns.EMAIL_ADDRESS.matcher(email).matches();
        String password = passwordField.getText().toString();

        Intent i = new Intent(getApplicationContext(), EmailSignupActivity.class);
        if (isEmail) {
            i.putExtra("email", email);
        } else {
            i.putExtra("email", "");
        }
        if (!password.isEmpty()) {
            i.putExtra("password", password);
        } else {
            i.putExtra("password", "");
        }
        startActivity(i);
    }

    public void forgotPassword(View view) {
        String email = emailField.getText().toString();
        boolean isEmail = Patterns.EMAIL_ADDRESS.matcher(email).matches();

        if (email.isEmpty() || !isEmail) {
            Toast t = Toast.makeText(getApplicationContext(), "Bitte geben Sie eine gültige E-Mail Adresse an", Toast.LENGTH_LONG);
            t.show();
        } else {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast t = Toast.makeText(getApplicationContext(), "Wir haben Ihnen eine E-Mail gesendet", Toast.LENGTH_LONG);
                        t.show();
                    } else {
                        Toast t = Toast.makeText(getApplicationContext(), "Diese E-Mail Adresse ist nicht registriert", Toast.LENGTH_LONG);
                        t.show();
                    }
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
