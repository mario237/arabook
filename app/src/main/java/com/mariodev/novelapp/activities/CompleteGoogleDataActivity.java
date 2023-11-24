package com.mariodev.novelapp.activities;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
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

public class CompleteGoogleDataActivity extends AppCompatActivity {

    SharedPref sharedPref;

    ConstraintLayout completeGoogleLayout;
    ProgressBar completeLoad;
    TextInputLayout genderTEdt, birthOfDateTEdt;
    CheckBox policyCheck;
    Button proceedBtn;


    private FirebaseAuth auth;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkLightMode();
        MyApplication.setLocale(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_google_data);

        //init views
        completeGoogleLayout = findViewById(R.id.completeGoogleLayout);
        completeLoad = findViewById(R.id.completetLoad);
        genderTEdt = findViewById(R.id.genderRegisterEdt);
        birthOfDateTEdt = findViewById(R.id.birthDateRegisterEdt);
        proceedBtn = findViewById(R.id.proceedBtn);
        policyCheck = findViewById(R.id.policyCheck);

        //init firebase
        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference("Users");

        //OnClick
        Objects.requireNonNull(genderTEdt.getEditText()).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGenderDialog();
            }
        });
        Objects.requireNonNull(birthOfDateTEdt.getEditText()).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCalender();
            }
        });

        proceedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewAccount();
            }
        });
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
        String birthOfDateTxt = Objects.requireNonNull(birthOfDateTEdt.getEditText()).getText().toString();

        if (birthOfDateTxt.isEmpty()) {
            birthOfDateTEdt.getEditText().setError("Field cannot be empty");

            return false;
        } else {
            birthOfDateTEdt.getEditText().setError(null);
            return true;
        }


    }

    private void openGenderDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(CompleteGoogleDataActivity.this);


        View view = getLayoutInflater().inflate( R.layout.gender_layout, null);

        builder.setView(view);

        final AlertDialog dialog = builder.create();



        view.findViewById(R.id.male_layout).setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                TextView male = view.findViewById(R.id.maleTxt);
                String maleTxt = male.getText().toString();
                Objects.requireNonNull(genderTEdt.getEditText()).setText(maleTxt);
                dialog.dismiss();
            }
        });
        view.findViewById(R.id.female_layout).setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                TextView female = view.findViewById(R.id.femaleTxt);
                String femaleTxt = female.getText().toString();
                Objects.requireNonNull(genderTEdt.getEditText()).setText(femaleTxt);
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    private void openCalender() {

        Calendar mCurrentDate = Calendar.getInstance();
        int mYear = mCurrentDate.get(Calendar.YEAR);
        int mMonth = mCurrentDate.get(Calendar.MONTH);
        int mDay = mCurrentDate.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog mDatePicker;
        mDatePicker = new DatePickerDialog(CompleteGoogleDataActivity.this, R.style.DialogTheme, new DatePickerDialog.OnDateSetListener() {
            @SuppressLint("SetTextI18n")
            public void onDateSet(DatePicker datepicker, int selectedYear, int selectedMonth, int selectedDay) {
                selectedMonth += 1;
                Objects.requireNonNull(birthOfDateTEdt.getEditText()).setText(selectedDay + "/" + selectedMonth + "/" + selectedYear);
            }
        }, mYear, mMonth, mDay);
        mDatePicker.show();

    }

    private void showPolicyDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(CompleteGoogleDataActivity.this, R.style.DialogTheme);
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

    private void createNewAccount() {

        if (!validateGender() | !validateBirthOfDate()) {
            Toast.makeText(this, "All field is required", Toast.LENGTH_SHORT).show();
        } else if (!policyCheck.isChecked()) {
            showPolicyDialog();
        } else {
            completeGoogleLayout.setVisibility(View.GONE);
            completeLoad.setVisibility(View.VISIBLE);

            GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);
            String id = Objects.requireNonNull(auth.getCurrentUser()).getUid();

            UserModel userModel = new UserModel();
            assert signInAccount != null;
            userModel.setUserId(id);
            userModel.setUsername(signInAccount.getDisplayName());
            userModel.setUserEmail(signInAccount.getEmail());
            userModel.setUserGender(Objects.requireNonNull(genderTEdt.getEditText()).getText().toString());
            userModel.setUserBirthOfDate(Objects.requireNonNull(birthOfDateTEdt.getEditText()).getText().toString());
            userModel.setUserFullName(signInAccount.getDisplayName());

            reference.child(id).setValue(userModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    startActivity(new Intent(CompleteGoogleDataActivity.this, MainPageActivity.class));
                    finish();
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(CompleteGoogleDataActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
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