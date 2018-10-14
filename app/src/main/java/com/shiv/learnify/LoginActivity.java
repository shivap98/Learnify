package com.shiv.learnify;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity {

    private final int PICK_IMAGE_REQUEST = 71;
    ImageView logo;
    ConstraintLayout signUpLayout;
    ConstraintLayout cancelLayout;
    boolean signUpMode = false;
    StorageReference storageReference;
    FrameLayout progressBarLayout;
    private TextInputEditText email;
    private TextInputEditText password;
    private Button loginButton;
    private Button signUpButton;
    private FirebaseAuth firebaseAuth;
    private TextInputEditText name;
    private TextInputEditText phone;
    private TextInputEditText university;
    private Button uploadButton;
    private ImageView previewDP;
    private Uri filePath;
    private String DPLink;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                previewDP.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        signUpButton = findViewById(R.id.signUpButton);

        logo = findViewById(R.id.logo);                     //visible by default
        signUpLayout = findViewById(R.id.signupLayout);
        signUpLayout.setVisibility(View.GONE);              //not gone in xml due to rendering bug
        cancelLayout = findViewById(R.id.cancelSignUpLayout);   //gone by default

        name = findViewById(R.id.name);
        phone = findViewById(R.id.phone);
        university = findViewById(R.id.university);
        uploadButton = findViewById(R.id.uploadPicButton);
        previewDP = findViewById(R.id.previewDP);

        progressBarLayout = findViewById(R.id.frameLayout);

        firebaseAuth = FirebaseAuth.getInstance();

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (signUpMode) {
                    String mail = email.getText().toString();
                    String pass = password.getText().toString();

                    String nameString = name.getText().toString();
                    String phoneString = phone.getText().toString();
                    String uniString = university.getText().toString();


                    if (!checkSignUpForm(mail, pass, nameString, phoneString, uniString)) {
                        return;
                    }


                    progressBarLayout.setVisibility(View.VISIBLE);

                    createNewUser(mail, pass, nameString, phoneString, uniString);
                } else {
                    showSignUp();
                    signUpMode = true;
                }
            }
        });

        cancelLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideSignUp();
                signUpMode = false;
            }
        });


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!signUpMode) {
                    String mail = email.getText().toString();
                    String pass = password.getText().toString();

                    if (!checkForm(mail, pass)) {
                        return;
                    }

                    progressBarLayout.setVisibility(View.VISIBLE);

                    signInUser(mail, pass);
                } else {
                    hideSignUp();
                    signUpMode = false;
                }
            }
        });

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });


    }

    private void uploadImage(String mail) {

        if (filePath != null) {
//            final ProgressDialog progressDialog = new ProgressDialog(this);
//            progressDialog.setTitle("Uploading...");
//            progressDialog.show();

            StorageReference ref = storageReference.child("images/" + mail);
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(LoginActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                        }
                    });
        }
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

                            FirebaseUser fu = firebaseAuth.getCurrentUser();
                            String uid = fu.getUid();
                            System.out.println(uid);

                            Intent i = new Intent(LoginActivity.this, MainActivity.class);
                            i.putExtra("uid", uid);
                            startActivity(i);
                            progressBarLayout.setVisibility(View.GONE);

                        } else {
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthInvalidUserException e) {
                                progressBarLayout.setVisibility(View.GONE);
                                email.setError("Email doesn't exist or has been disabled");
                                email.requestFocus();
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                progressBarLayout.setVisibility(View.GONE);
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
    private void createNewUser(final String mail, String pass, final String nameString, final String phoneString, String uniString) {
        firebaseAuth.createUserWithEmailAndPassword(mail, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.e("login", "success");

                            ArrayList<String> courses = new ArrayList<>();
                            courses.add("CS 250");
                            courses.add("CS 251");
                            courses.add("CS 291");
                            courses.add("MA 261");
                            courses.add("ECON 251");
                            courses.add("HIST 104");

                            Student stud = new Student(nameString, mail, phoneString, courses, null);

                            FirebaseUser fu = firebaseAuth.getCurrentUser();
                            String uid = fu.getUid();
                            System.out.println(uid);

                            storageReference = FirebaseStorage.getInstance().getReference();
                            uploadImage(mail);


                            DatabaseReference dr = FirebaseDatabase.getInstance().getReference()
                                    .child("Users");
                            dr.child(uid).setValue(stud);

                            Intent i = new Intent(LoginActivity.this, MainActivity.class);
                            i.putExtra("uid", uid);
                            startActivity(i);
                            progressBarLayout.setVisibility(View.GONE);

                        } else {
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthWeakPasswordException e) {
                                progressBarLayout.setVisibility(View.GONE);
                                password.setError("Password length must be greater than 6 chars");
                                password.requestFocus();
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                progressBarLayout.setVisibility(View.GONE);
                                email.setError("Please type valid email");
                                email.requestFocus();
                            } catch (FirebaseAuthUserCollisionException e) {
                                progressBarLayout.setVisibility(View.GONE);
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
        } else {
            password.setError(null);
        }
        if (mail.isEmpty()) {
            email.setError("Please type in an email");
            email.requestFocus();
            valid = false;
        } else if (!mail.endsWith(".edu")) {
            email.setError("Please type in a .edu email");
            email.requestFocus();
            valid = false;
        } else {
            email.setError(null);
        }
        return valid;
    }

    private boolean checkSignUpForm(String mail, String pass, String nameString, String phoneString, String uniString) {
        boolean valid = true;


        if (uniString.isEmpty()) {
            university.setError("Please enter your university");
            university.requestFocus();
            valid = false;
        }

        if (phoneString.isEmpty()) {
            phone.setError("Please enter your phone number");
            phone.requestFocus();
            valid = false;
        } else if (phoneString.length() < 10) {
            phone.setError("Please enter valid phone number");
            phone.requestFocus();
            valid = false;
        }
        if (nameString.isEmpty()) {
            name.setError("Please enter your name");
            name.requestFocus();
            valid = false;
        }

        if (pass.isEmpty()) {
            password.setError("Password cannot be empty");
            password.requestFocus();
            valid = false;
        } else {
            password.setError(null);
        }
        if (mail.isEmpty()) {
            email.setError("Please type in an email");
            email.requestFocus();
            valid = false;
        } else if (!mail.endsWith(".edu")) {
            email.setError("Please type in a .edu email");
            email.requestFocus();
            valid = false;
        } else {
            email.setError(null);
        }
        return valid;
    }

    /**
     * Animates all the sign up fields into view
     * Hides logo, shows signUpLayout, shows cancel button
     */
    void showSignUp() {
        signUpLayout.setVisibility(View.VISIBLE);
        cancelLayout.setVisibility(View.VISIBLE);
        logo.animate().scaleY(0);
        logo.setVisibility(View.GONE);
    }

    /**
     * Animates all the sign up fields into view
     * Opposite of above
     */
    void hideSignUp() {
        signUpLayout.setVisibility(View.GONE);
        cancelLayout.setVisibility(View.GONE);
        logo.setVisibility(View.VISIBLE);
        logo.animate().scaleY(1);
    }

    @Override
    public void onBackPressed() {
        if (signUpMode) {
            hideSignUp();
            signUpMode = false;
        } else {
            super.onBackPressed();
        }
    }
}
