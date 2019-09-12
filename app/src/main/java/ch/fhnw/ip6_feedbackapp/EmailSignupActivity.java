package ch.fhnw.ip6_feedbackapp;

import android.content.Intent;
import android.os.Bundle;
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
import com.google.firebase.auth.UserProfileChangeRequest;

public class EmailSignupActivity extends AppCompatActivity {
    EditText emailField;
    EditText passwordField;
    EditText usernameField;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_signup);

        emailField = findViewById(R.id.editTextEmail);
        passwordField = findViewById(R.id.editTextPassword);
        usernameField = findViewById(R.id.editTextUsername);

        // Pass data from the LoginEmailActivity into the appropriate TextFields if necessary
        Intent intent = getIntent();
        String email = intent.getStringExtra("email");
        if (email != null) {
            emailField.setText(email);
        }
        String password = intent.getStringExtra("password");
        if (password != null) {
            passwordField.setText(password);
        }

        mAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(i);
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(mAuthStateListener);
    }

    public void register(View view) {

        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();
        String username = usernameField.getText().toString();

        if (!email.isEmpty() && !password.isEmpty() && !username.isEmpty()) {

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                changeUserProfile();
                                sendEmail();
                                //FirebaseUser user = mAuth.getCurrentUser();
                                //updateUI(user);
                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(getApplicationContext(), "Fehler bei der Registrierung",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            Toast t = Toast.makeText(getApplicationContext(), "Bitte alle Felder ausfüllen", Toast.LENGTH_LONG);
            t.show();
        }
    }

    public void sendEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Bitte bestätige deine E-Mail Adresse!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void changeUserProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String username = usernameField.getText().toString();
        UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(username).build();
        user.updateProfile(profileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                FirebaseUser user2 = FirebaseAuth.getInstance().getCurrentUser();
            }
        });
    }
}
