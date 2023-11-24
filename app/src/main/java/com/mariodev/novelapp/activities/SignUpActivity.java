package com.mariodev.novelapp.activities;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mariodev.novelapp.helpers.LocaleHelper;
import com.mariodev.novelapp.helpers.SharedPref;
import com.mariodev.novelapp.models.UserModel;
import com.mariodev.novelapp.MyApplication;
import com.mariodev.novelapp.R;

import java.util.Calendar;
import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {

    SharedPref sharedPref;

    ConstraintLayout signUpLayout;
    ProgressBar createAccLoad;
    TextView loginTxt;
    TextInputLayout usernameTEdt, emailTEdt, passTEdt, genderTEdt, birthOdDateTEdt;
    Button registerBtn;
    CheckBox policyCheck;

    private AlertDialog dialogChooseGender;

    private FirebaseAuth auth;
    private DatabaseReference reference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkLightMode();
        MyApplication.setLocale(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        //init views
        signUpLayout = findViewById(R.id.signUpLayout);
        createAccLoad = findViewById(R.id.createAccountLoad);
        usernameTEdt = findViewById(R.id.usernameRegisterEdt);
        emailTEdt = findViewById(R.id.emailRegisterEdt);
        passTEdt = findViewById(R.id.passwordRegisterEdt);
        genderTEdt = findViewById(R.id.genderRegisterEdt);
        birthOdDateTEdt = findViewById(R.id.birthDateRegisterEdt);
        loginTxt = findViewById(R.id.loginTxt);
        registerBtn = findViewById(R.id.registerBtn);
        policyCheck = findViewById(R.id.policyCheck);

        //init firebase
        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference("Users");

        //makePassHintRTL();

        //OnClick
        Objects.requireNonNull(genderTEdt.getEditText()).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGenderDialog();
            }
        });
        Objects.requireNonNull(birthOdDateTEdt.getEditText()).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCalender();
            }
        });

        loginTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToLogin();
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewAccount();
            }
        });

    }

    private void createNewAccount() {

        if (!validateName() | !validateEmail() | !validatePassword() | !validateGender() | !validateBirthOfDate()) {
            Toast.makeText(this, "Invalid Register Data", Toast.LENGTH_SHORT).show();
        } else if (!policyCheck.isChecked()) {
            showPolicyDialog();
        } else {
            signUpLayout.setVisibility(View.GONE);
            createAccLoad.setVisibility(View.VISIBLE);

            if (MyApplication.isOnline(getApplicationContext())) {
                signUpLayout.setVisibility(View.VISIBLE);
                createAccLoad.setVisibility(View.GONE);
                Toast.makeText(this, getResources().getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
            } else {
                final String emailTxt = Objects.requireNonNull(emailTEdt.getEditText()).getText().toString();
                final String passTxt = Objects.requireNonNull(passTEdt.getEditText()).getText().toString();
                auth.createUserWithEmailAndPassword(emailTxt, passTxt).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {

                        Objects.requireNonNull(auth.getCurrentUser()).sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                               if (task.isSuccessful()){
                                   String userId = auth.getUid();
                                   String userTxt = Objects.requireNonNull(usernameTEdt.getEditText()).getText().toString().trim();
                                   String genderTxt = Objects.requireNonNull(genderTEdt.getEditText()).getText().toString();
                                   String birthTxt = Objects.requireNonNull(birthOdDateTEdt.getEditText()).getText().toString();

                                   UserModel userModel = new UserModel();
                                   userModel.setUserId(userId);
                                   userModel.setUsername(userTxt);
                                   userModel.setUserEmail(emailTxt);
                                   userModel.setUserGender(genderTxt);
                                   userModel.setUserBirthOfDate(birthTxt);
                                   userModel.setUserFullName(userTxt);

                                   assert userId != null;
                                   reference.child(userId).setValue(userModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                                       @Override
                                       public void onSuccess(Void aVoid) {
                                            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                                            startActivity(intent);
                                            finish();
                                           overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

                                           Toast.makeText(SignUpActivity.this, getResources().getString(R.string.check_inbox_for_email), Toast.LENGTH_SHORT).show();
                                       }
                                   });
                               }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(SignUpActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });


                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        signUpLayout.setVisibility(View.VISIBLE);
                        createAccLoad.setVisibility(View.GONE);
                        Toast.makeText(SignUpActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }


        }
    }

    private void showPolicyDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this, R.style.DialogTheme);
        builder.setMessage(getResources().getString(R.string.terms_and_policy_txt))
                .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        policyCheck.setChecked(true);
                    }
                })
                .setNegativeButton(getResources().getString(R.string.no), null);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    private void openGenderDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
        View view = LayoutInflater.from(this).inflate(
                R.layout.gender_layout,
                (ViewGroup) findViewById(R.id.chooseGenderCard));
        builder.setView(view);

        dialogChooseGender = builder.create();

        if (dialogChooseGender.getWindow() != null) {

            dialogChooseGender.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        view.findViewById(R.id.male_layout).setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                TextView male = view.findViewById(R.id.maleTxt);
                String maleTxt = male.getText().toString();
                Objects.requireNonNull(genderTEdt.getEditText()).setText(maleTxt);
                dialogChooseGender.dismiss();
            }
        });
        view.findViewById(R.id.female_layout).setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                TextView female = view.findViewById(R.id.femaleTxt);
                String femaleTxt = female.getText().toString();
                Objects.requireNonNull(genderTEdt.getEditText()).setText(femaleTxt);
                dialogChooseGender.dismiss();
            }
        });

        dialogChooseGender.show();

    }

    private void openCalender() {

        Calendar mCurrentDate = Calendar.getInstance();
        int mYear = mCurrentDate.get(Calendar.YEAR);
        int mMonth = mCurrentDate.get(Calendar.MONTH);
        int mDay = mCurrentDate.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog mDatePicker;
        mDatePicker = new DatePickerDialog(SignUpActivity.this, R.style.DialogTheme, new DatePickerDialog.OnDateSetListener() {
            @SuppressLint("SetTextI18n")
            public void onDateSet(DatePicker datepicker, int selectedYear, int selectedMonth, int selectedDay) {
                selectedMonth += 1;
                Objects.requireNonNull(birthOdDateTEdt.getEditText()).setText(selectedDay + "/" + selectedMonth + "/" + selectedYear);
            }
        }, mYear, mMonth, mDay);
        mDatePicker.show();

    }

    private void goToLogin() {
        startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

    }

    private Boolean validateName() {
        String nameTxt = Objects.requireNonNull(usernameTEdt.getEditText()).getText().toString();

        if (nameTxt.isEmpty()) {
            usernameTEdt.getEditText().setError("Field cannot be empty");

            return false;
        } else {
            usernameTEdt.setError(null);
            return true;
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

    private Boolean validateGender() {
        String genderTxt = Objects.requireNonNull(genderTEdt.getEditText()).getText().toString();

        if (genderTxt.equals("")) {
            genderTEdt.getEditText().setError("Field cannot be empty ");
            return false;
        } else {
            genderTEdt.getEditText().setError(null);
            return true;
        }


    }

    private Boolean validateBirthOfDate() {
        String birthOfDateTxt = Objects.requireNonNull(birthOdDateTEdt.getEditText()).getText().toString();

        if (birthOfDateTxt.isEmpty()) {
            birthOdDateTEdt.getEditText().setError("Field cannot be empty");

            return false;
        } else {
            birthOdDateTEdt.getEditText().setError(null);
            return true;
        }


    }


    @Override
    public void onBackPressed() {
        startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

    }

    private void checkLightMode() {
        sharedPref = new SharedPref(this);
        if (sharedPref.loadLightModeState()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        }
    }
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base , "ar"));
    }


}