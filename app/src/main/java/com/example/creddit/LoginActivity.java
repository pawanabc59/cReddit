package com.example.creddit;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.creddit.RoomDatabase.DatabaseClient;
import com.example.creddit.RoomDatabase.LoggedInUser;
import com.example.creddit.RoomDatabase.MyRoomDatabase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    TextView txt_signup;
    TextInputLayout textEmail, textPassword;
    TextInputEditText editEmail, editPassword;
    Button btnLogin;
    TextView textRegister, login_forgot_pass_txt;
    ProgressBar loginProgressBar;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference mRef, mRef2;
    FirebaseUser firebaseUser;

    SharedPref sessionManager;
    MyRoomDatabase myRoomDatabase;

    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SharedPref(this);
        if (sessionManager.loadNightModeState() == true) {

            setTheme(R.style.darktheme);
        } else {
            setTheme(R.style.AppTheme);
        }

        setContentView(R.layout.activity_login);

        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        mRef = firebaseDatabase.getReference("creddit").child("users");

        textEmail = findViewById(R.id.textLoginUsername);
        textPassword = findViewById(R.id.textLoginPassword);

        editEmail = findViewById(R.id.login_username);
        editPassword = findViewById(R.id.login_password);

        btnLogin = findViewById(R.id.login_btn_login);
        loginProgressBar = findViewById(R.id.loginProgressBar);

        login_forgot_pass_txt = findViewById(R.id.login_forgot_pass_txt);

        textRegister = findViewById(R.id.login_signup_txt);

        textRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivity(intent);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnLogin.setVisibility(View.GONE);
                loginProgressBar.setVisibility(View.VISIBLE);

                String memail = editEmail.getText().toString().trim();
                String mpassword = editPassword.getText().toString().trim();
                if (memail.isEmpty()) {
                    editEmail.setError("Please insert email");
                    loginProgressBar.setVisibility(View.GONE);
                    btnLogin.setVisibility(View.VISIBLE);

                } else if (mpassword.isEmpty()) {
                    editPassword.setError("Please insert password");
                    loginProgressBar.setVisibility(View.GONE);
                    btnLogin.setVisibility(View.VISIBLE);

                } else if (!memail.matches(emailPattern)) {
                    editEmail.setError("Please provide valid email address");
                    loginProgressBar.setVisibility(View.GONE);
                    btnLogin.setVisibility(View.VISIBLE);

                } else if (mpassword.length() < 6) {
                    editPassword.setError("Password is too short");
                    loginProgressBar.setVisibility(View.GONE);
                    btnLogin.setVisibility(View.VISIBLE);

                } else {
                    Login(memail, mpassword);
                }
            }
        });

        txt_signup = findViewById(R.id.login_signup_txt);
        txt_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivity(intent);
                finish();
            }
        });

        login_forgot_pass_txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText resetPassword1 = new EditText(view.getContext());
                resetPassword1.setTextColor(R.attr.textcolor);
                AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(view.getContext());
                passwordResetDialog.setTitle("Reset Password");
                passwordResetDialog.setMessage("Enter email to receive reset link");
                passwordResetDialog.setView(resetPassword1);

                passwordResetDialog.setPositiveButton("Email me", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String mail = resetPassword1.getText().toString().trim();
                        if (mail.equals("")) {
                            resetPassword1.setError("Please provide email first");
                            Toast.makeText(getApplicationContext(),"Please provide email first", Toast.LENGTH_SHORT).show();
                        } else {

                            firebaseAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(getApplicationContext(), "Reset link sent to your email", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getApplicationContext(), "Error in sending email", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        dialogInterface.dismiss();
                    }
                });

                passwordResetDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                passwordResetDialog.create().show();

            }
        });
    }

    public void Login(final String email, final String password) {
        if (FirebaseAuth.getInstance().getCurrentUser() != null){
            firebaseAuth.signOut();
        }
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                        if (firebaseUser.isEmailVerified()) {

                            String uid = firebaseUser.getUid();
                            mRef2 = mRef.child(uid);
                            mRef2.child("email").setValue(email);
                            if (task.isSuccessful()) {

//                            this is to add the profile of the user if user has uploaded the profile image earlier then that url will be loaded otherwise the null value will be set.
                                mRef2.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        sessionManager.put_showNSFW(dataSnapshot.child("showNSFW").getValue(Integer.class));
                                        sessionManager.put_blurNSFW(dataSnapshot.child("blurNSFW").getValue(Integer.class));
                                        try {
                                            String profileImagePath = dataSnapshot.child("profileImage").getValue().toString();
                                            mRef2.child("profileImage").setValue(profileImagePath);
                                            mRef2.child("optionalName").setValue(dataSnapshot.child("optionalName").getValue().toString());
                                            mRef2.child("optionalAbout").setValue(dataSnapshot.child("optionalAbout").getValue().toString());
                                            mRef2.child("profileBannerImage").setValue(dataSnapshot.child("profileBannerImage").getValue().toString());
                                            mRef2.child("savedImages").child("numberOfSavedImages").setValue(dataSnapshot.child("savedImages").child("numberOfSavedImages").getValue(Integer.class));
                                            mRef2.child("showNSFW").setValue(dataSnapshot.child("showNSFW").getValue(Integer.class));
                                            mRef2.child("blurNSFW").setValue(dataSnapshot.child("blurNSFW").getValue(Integer.class));
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            mRef2.child("profileImage").setValue("null");
                                            mRef2.child("optionalName").setValue(email);
                                            mRef2.child("optionalAbout").setValue("none");
                                            mRef2.child("profileBannerImage").setValue("null");
                                            mRef2.child("savedImages").child("numberOfSavedImages").setValue(0);
                                            mRef2.child("showNSFW").setValue(0);
                                            mRef2.child("blurNSFW").setValue(0);
                                            sessionManager.put_showNSFW(0);
                                            sessionManager.put_blurNSFW(0);
                                        }

                                        myRoomDatabase = DatabaseClient.databaseClient(LoginActivity.this);

                                        LoggedInUser loggedInUser = new LoggedInUser(firebaseUser.getUid(), email, password,
                                                dataSnapshot.child("profileImage").getValue().toString(), dataSnapshot.child("optionalName").getValue().toString());
                                        myRoomDatabase.loggedInUserDAO().loggedInUserInsertion(loggedInUser);

                                        loginProgressBar.setVisibility(View.GONE);
                                        btnLogin.setVisibility(View.VISIBLE);
                                        Toast.makeText(getApplicationContext(), "Welcome User", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);


                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        Toast.makeText(getApplicationContext(), "Login Failed", Toast.LENGTH_SHORT).show();
                                        editEmail.setText("");
                                        editPassword.setText("");
                                        loginProgressBar.setVisibility(View.GONE);
                                        btnLogin.setVisibility(View.VISIBLE);
                                    }
                                });


//                          this was earlier code :  mRef2.child("profileImage").setValue("null");

                            } else {
                                loginProgressBar.setVisibility(View.GONE);
                                btnLogin.setVisibility(View.VISIBLE);
                                Toast.makeText(getApplicationContext(), "Sorry! Login failed", Toast.LENGTH_SHORT);
                                editEmail.setText("");
                                editPassword.setText("");
                            }

                        } else {
                            Toast.makeText(getApplicationContext(), "Please verify your email address first!", Toast.LENGTH_SHORT).show();
                            loginProgressBar.setVisibility(View.GONE);
                            btnLogin.setVisibility(View.VISIBLE);
                            editEmail.setText("");
                            editPassword.setText("");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loginProgressBar.setVisibility(View.GONE);
                        btnLogin.setVisibility(View.VISIBLE);
                        Toast.makeText(getApplicationContext(), "Either Username or Password is wrong!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
