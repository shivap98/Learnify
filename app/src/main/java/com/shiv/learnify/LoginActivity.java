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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;


public class LoginActivity extends AppCompatActivity {

    private TextInputEditText email;
    private TextInputEditText password;
    private Button loginButton;
    private Button signUpButton;

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

    /**
     * sign in the user in firebase and check if everything is fine or not
     * check if email address exits or not
     * check if password is correct or not
     *
     * @param mail email address
     * @param pass password
     */

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

    /**
     * creates new user in firebase and check if everything is fine or not
     * check for valid password i.e. greater than 6 char
     * check for valid email address
     * check if email already exist in firebase
     *
     * @param mail email address
     * @param pass password
     */

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

    /**
     * @param mail - email address specified
     * @param pass - password specified
     * @return if the email address and password valid or not
     */

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
        } else if (!mail.endsWith(".edu")) {
            email.setError("Please type .edu email");
            email.requestFocus();
            valid = false;
        } else
            email.setError(null);


        return valid;
    }


}
