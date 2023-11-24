package com.mariodev.novelapp.activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mariodev.novelapp.admin.AdminActivity;
import com.mariodev.novelapp.helpers.LocaleHelper;
import com.mariodev.novelapp.helpers.SharedPref;
import com.mariodev.novelapp.MyApplication;
import com.mariodev.novelapp.R;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private final static int RC_SIGN_IN = 123;
    SharedPref sharedPref;
    ConstraintLayout loginLayout;
    RelativeLayout loginLoad;
    LinearLayout loginWithGoogleLayout;
    TextView signUpTxt, forgetPassTv;
    Button loginBtn;
    TextInputLayout emailTEdt, passTEdt;
    FirebaseAuth auth;
    GoogleSignInClient mGoogleSignInClient;


    @SuppressLint({"ClickableViewAccessibility", "RtlHardcoded"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkLightMode();
        MyApplication.setLocale(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //init views
        loginLayout = findViewById(R.id.loginPageLayout);
        loginLoad = findViewById(R.id.loginLoad);
        emailTEdt = findViewById(R.id.emailLoginEdt);
        passTEdt = findViewById(R.id.passwordLoginEdt);
        loginBtn = findViewById(R.id.loginBtn);
        loginWithGoogleLayout = findViewById(R.id.loginWithGoogleLayout);
        signUpTxt = findViewById(R.id.signUpTxt);
        forgetPassTv = findViewById(R.id.forgetPassTv);

        auth = FirebaseAuth.getInstance();
        createGoogleRequest();

        //on Click
        loginBtn.setOnClickListener(this);
        loginWithGoogleLayout.setOnClickListener(this);
        signUpTxt.setOnClickListener(this);
        loginWithGoogleLayout.setOnClickListener(this);
        forgetPassTv.setOnClickListener(this);


        if (auth.getCurrentUser() != null) {
            // User is logged in
            if (auth.getCurrentUser().isEmailVerified()){
                Intent intent = new Intent(LoginActivity.this, MainPageActivity.class);
                startActivity(intent);
                finish();

            }
        }


    }


    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.loginBtn:
                closeKeyboard();
                loginToMainPage();
                break;

            case R.id.loginWithGoogleLayout:
                loginLayout.setAlpha(0.3f);
                loginLoad.setVisibility(View.VISIBLE);
                signInGoogle();
                break;

            case R.id.signUpTxt:
                goToSignUp();
                break;

            case R.id.forgetPassTv:
                showResetPassDialog();
                break;
        }
    }

    private void showResetPassDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);

        View view = getLayoutInflater().inflate(R.layout.forget_pass_layout, null);

        final ConstraintLayout resetDialogLayout = view.findViewById(R.id.resetDialogLayout);
        final ProgressBar loadResetPass = view.findViewById(R.id.loadResetPass);
        final EditText resetEmailEdt = view.findViewById(R.id.resetEmailEdt);
        Button sendEmailBtn = view.findViewById(R.id.sendEmailBtn);
        Button cancelBtn = view.findViewById(R.id.cancelBtn);

        builder.setView(view);

        final AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);


        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        sendEmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (resetEmailEdt.getText().toString().isEmpty()) {
                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.email_cannot_null), Toast.LENGTH_SHORT).show();
                } else {
                    resetPassword(resetEmailEdt.getText().toString(), dialog, loadResetPass, resetDialogLayout);
                }

            }
        });

        dialog.show();

    }

    private void resetPassword(String emailTxt, final Dialog dialog, ProgressBar progressBar, ConstraintLayout constraintLayout) {
        progressBar.setVisibility(View.VISIBLE);
        constraintLayout.setVisibility(View.GONE);
        auth.sendPasswordResetEmail(emailTxt).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.check_inbox), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else if (MyApplication.isOnline(getApplicationContext())) {
                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.no_internet), Toast.LENGTH_SHORT).show();

                } else {
                    String error = Objects.requireNonNull(task.getException()).getMessage();
                    Toast.makeText(LoginActivity.this, "error:" + error, Toast.LENGTH_SHORT).show();
                }


            }
        });


    }

    private void createGoogleRequest() {
        // Configure Google Sign In


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();


        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void signInGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                assert account != null;
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                // ...
                loginLayout.setAlpha(1);
                loginLoad.setVisibility(View.GONE);
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {


        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            loginLayout.setVisibility(View.GONE);
                            loginLoad.setVisibility(View.VISIBLE);
                            // Sign in success, update UI with the signed-in user's information
                            final FirebaseUser user = auth.getCurrentUser();

                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");

                            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    assert user != null;
                                    if (snapshot.hasChild(user.getUid())) {

                                        startActivity(new Intent(LoginActivity.this, MainPageActivity.class));
                                    } else {
                                        startActivity(new Intent(LoginActivity.this, CompleteGoogleDataActivity.class));
                                    }
                                    finish();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });


                        }


                        // ...
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void loginToMainPage() {

        String emailTxt = Objects.requireNonNull(emailTEdt.getEditText()).getText().toString();
        String passTxt = Objects.requireNonNull(passTEdt.getEditText()).getText().toString();

        if (emailTxt.equals("arabookadmin@gmail.com") && passTxt.equals("ara2020@#")) {
            startActivity(new Intent(LoginActivity.this, AdminActivity.class));
            finish();
        } else if (!validateEmail() | !validatePassword()) {
            Toast.makeText(this, getResources().getString(R.string.invalid_account_data), Toast.LENGTH_SHORT).show();
        } else {

            loginLayout.setVisibility(View.GONE);
            loginLoad.setVisibility(View.VISIBLE);


            if (MyApplication.isOnline(getApplicationContext())) {
                loginLayout.setVisibility(View.VISIBLE);
                loginLoad.setVisibility(View.GONE);
                Toast.makeText(this, getResources().getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
            } else {
                loginLayout.setVisibility(View.GONE);
                loginLoad.setVisibility(View.VISIBLE);

                    auth.signInWithEmailAndPassword(emailTxt, passTxt).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                             if (Objects.requireNonNull(auth.getCurrentUser()).isEmailVerified()){
                                loginLayout.setVisibility(View.VISIBLE);
                                loginLoad.setVisibility(View.GONE);
                                startActivity(new Intent(LoginActivity.this, MainPageActivity.class));
                                finish();
                            }else {
                                loginLayout.setVisibility(View.VISIBLE);
                                loginLoad.setVisibility(View.GONE);
                                Toast.makeText(LoginActivity.this, getResources().getString(R.string.not_verified_yet), Toast.LENGTH_SHORT).show();
                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            loginLayout.setVisibility(View.VISIBLE);
                            loginLoad.setVisibility(View.GONE);
                            Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });



            }
        }


    }

        private Boolean validateEmail() {
            String emailTxt = Objects.requireNonNull(emailTEdt.getEditText()).getText().toString();
    //        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

            if (emailTxt.isEmpty()) {
                emailTEdt.getEditText().setError("Field cannot be empty");

                return false;

            } else if (!Patterns.EMAIL_ADDRESS.matcher(emailTxt).matches()) {
                emailTEdt.getEditText().setError("Invalid email address");

                return false;
            } else {
                emailTEdt.getEditText().setError(null);
                return true;
            }

        }

        private Boolean validatePassword() {
            String passwordTxt = Objects.requireNonNull(passTEdt.getEditText()).getText().toString();


            if (passwordTxt.isEmpty()) {
                passTEdt.getEditText().setError("Field cannot be empty");

                return false;
            } else if (passwordTxt.length() < 8) {
                passTEdt.getEditText().setError("Password is too short");

                return false;
            } else {
                passTEdt.getEditText().setError(null);
                return true;
            }

        }

    private void goToSignUp() {
        startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
        finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }


    private void checkLightMode() {
        sharedPref = new SharedPref(this);
        if (sharedPref.loadLightModeState()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        }
    }

    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base , "ar"));
    }



}
