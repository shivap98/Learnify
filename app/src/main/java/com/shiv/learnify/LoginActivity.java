package com.shiv.learnify;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;


public class LoginActivity extends AppCompatActivity {

    TextInputEditText email;
    TextInputEditText password;
    Button loginButton;
    Button signUpButton;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        signUpButton = findViewById(R.id.signUpButton);

        firebaseAuth = FirebaseAuth.getInstance();

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String mail = email.getText().toString();
                String pass = password.getText().toString();

                if (!checkForm(mail, pass))
                    return;

                //TODO: add showProgressDialog
                createNewUser(mail, pass);
            }
        });


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mail = email.getText().toString();
                String pass = password.getText().toString();

                if (!checkForm(mail, pass))
                    return;

                //TODO: add showProgressDialog
                signInUser(mail, pass);
            }
        });

    }

    private void signInUser(String mail, final String pass) {
        firebaseAuth.signInWithEmailAndPassword(mail, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.e("login", "success");
                        } else {
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthInvalidUserException e) {
                                email.setError("Email doesn't exist or has been disabled");
                                email.requestFocus();
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                password.setError("Wrong Password");
                                password.requestFocus();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
    }

    private void createNewUser(String mail, String pass) {
        firebaseAuth.createUserWithEmailAndPassword(mail, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.e("login", "success");
                        } else {
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthWeakPasswordException e) {
                                password.setError("Password length must be greater than 6 chars");
                                password.requestFocus();
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                email.setError("Please type valid email");
                                email.requestFocus();
                            } catch (FirebaseAuthUserCollisionException e) {
                                email.setError("User already exists");
                                email.requestFocus();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
    }


    private boolean checkForm(String mail, String pass) {
        boolean valid = true;

        if (pass.isEmpty()) {
            password.setError("Password cannot be empty");
            password.requestFocus();
            valid = false;
        } else
            password.setError(null);

        //TODO: add feature where you check for . after @ as well
        if (mail.isEmpty()) {
            email.setError("Please type in email");
            email.requestFocus();
            valid = false;
        } else
            email.setError(null);


        return valid;
    }


}
