package com.mariodev.novelapp.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import com.anstrontechnologies.corehelper.AnstronCoreHelper;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iceteck.silicompressorr.FileUtils;
import com.iceteck.silicompressorr.SiliCompressor;
import com.mariodev.novelapp.MyApplication;
import com.mariodev.novelapp.R;
import com.mariodev.novelapp.adapters.CoverListAdapter;
import com.mariodev.novelapp.helpers.LocaleHelper;
import com.mariodev.novelapp.helpers.SharedPref;
import com.mariodev.novelapp.models.UserModel;

import java.io.File;
import java.util.Calendar;
import java.util.Objects;
import java.util.UUID;

public class EditProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int CODE_IMAGE_GALLERY = 1;
    private static final int PERMISSION_CODE = 10;

    SharedPref sharedPref;
    CardView profileCard;
    ImageView profileImg, coverImg;
    TextInputLayout userFullNameTEdt, usernameTEdt, bioTEdt, genderTEdt, birthDateTEdt;
    Button editProfileBtn, resetPassBtn;
    String userId, userFullNameTxt, usernameTxt, profileImgUri, userEmailTxt, bioTxt, genderTxt, birthDateTxt;
    Integer userCoverNumber;
    DatabaseReference userRef;
    FirebaseAuth auth;
    Uri mImageUri , compressedUri;

    // instance for firebase storage and StorageReference
    FirebaseStorage storage;
    StorageReference storageReference;
    AnstronCoreHelper coreHelper;


    AlertDialog dialogChooseGender, loadingDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkLightMode();
        MyApplication.setLocale(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        profileCard = findViewById(R.id.profileCard);
        profileImg = findViewById(R.id.profileImg);
        coverImg = findViewById(R.id.coverImg);
        userFullNameTEdt = findViewById(R.id.userFullNameTEdt);
        usernameTEdt = findViewById(R.id.usernameTEdt);
        bioTEdt = findViewById(R.id.bioTEdt);
        genderTEdt = findViewById(R.id.genderTEdt);
        birthDateTEdt = findViewById(R.id.birthDateTEdt);
        editProfileBtn = findViewById(R.id.editProfileBtn);
        resetPassBtn = findViewById(R.id.resetPassBtn);


        auth = FirebaseAuth.getInstance();

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        coreHelper = new AnstronCoreHelper(this);

        userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        coverImg.setOnClickListener(this);
        profileCard.setOnClickListener(this);
        editProfileBtn.setOnClickListener(this);
        resetPassBtn.setOnClickListener(this);

        Objects.requireNonNull(genderTEdt.getEditText()).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGenderDialog();
            }
        });
        Objects.requireNonNull(birthDateTEdt.getEditText()).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCalender();
            }
        });

        if (MyApplication.isOnline(getApplicationContext())) {
            Toast.makeText(this, getResources().getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
        } else {
            getUserData(userRef);
        }


    }

    private void getUserData(DatabaseReference databaseReference) {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    UserModel userModel = snapshot.getValue(UserModel.class);
                    assert userModel != null;

                    profileImgUri = userModel.getUserProfileImg();
                    userFullNameTxt = userModel.getUserFullName();
                    usernameTxt = userModel.getUsername();
                    userEmailTxt = userModel.getUserEmail();
                    bioTxt = userModel.getUserBio();
                    genderTxt = userModel.getUserGender();
                    birthDateTxt = userModel.getUserBirthOfDate();
                    userCoverNumber = userModel.getCoverNumber();

                    if (profileImgUri != null) {
                        Glide.with(getApplicationContext())
                                .load(profileImgUri)
                                .into(profileImg);
                    }


                    if (userFullNameTxt != null) {
                        Objects.requireNonNull(userFullNameTEdt.getEditText()).setText(userFullNameTxt);
                    }

                    if (bioTxt != null) {
                        Objects.requireNonNull(bioTEdt.getEditText()).setText(bioTxt);
                    }

                    if (userCoverNumber == null) {
                        coverImg.setImageResource(R.drawable.cover1);

                    } else {
                        setUserCoverImage(userCoverNumber);
                    }

                    Objects.requireNonNull(usernameTEdt.getEditText()).setText(usernameTxt);
                    Objects.requireNonNull(genderTEdt.getEditText()).setText(genderTxt);
                    Objects.requireNonNull(birthDateTEdt.getEditText()).setText(birthDateTxt);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setUserCoverImage(Integer number) {
        if (number == 1) {
            coverImg.setImageResource(R.drawable.cover1);
        } else if (number == 2) {
            coverImg.setImageResource(R.drawable.cover2);
        } else if (number == 3) {
            coverImg.setImageResource(R.drawable.cover3);
        } else if (number == 4) {
            coverImg.setImageResource(R.drawable.cover4);
        } else if (number == 5) {
            coverImg.setImageResource(R.drawable.cover5);
        } else if (number == 6) {
            coverImg.setImageResource(R.drawable.cover6);
        } else if (number == 7) {
            coverImg.setImageResource(R.drawable.cover7);
        } else if (number == 8) {
            coverImg.setImageResource(R.drawable.cover8);
        } else if (number == 9) {
            coverImg.setImageResource(R.drawable.cover9);
        }
    }

    private void chooseImage() {
        if (ActivityCompat.checkSelfPermission(EditProfileActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(EditProfileActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_CODE);
        } else {
            startActivityForResult(new Intent().setAction(Intent.ACTION_GET_CONTENT)
                    .setType("image/*"), CODE_IMAGE_GALLERY);
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CODE_IMAGE_GALLERY && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            mImageUri = data.getData();

            File file = new File(SiliCompressor.with(this).compress(FileUtils.getPath(this , mImageUri) ,
                    new File(this.getCacheDir() , "temp")));

            compressedUri = Uri.fromFile(file);

            profileImg.setImageURI(compressedUri);

        }
    }

    private void openGenderDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
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
        mDatePicker = new DatePickerDialog(EditProfileActivity.this, R.style.DialogTheme, new DatePickerDialog.OnDateSetListener() {
            @SuppressLint("SetTextI18n")
            public void onDateSet(DatePicker datepicker, int selectedYear, int selectedMonth, int selectedDay) {
                selectedMonth += 1;
                Objects.requireNonNull(birthDateTEdt.getEditText()).setText(selectedDay + "/" + selectedMonth + "/" + selectedYear);
            }
        }, mYear, mMonth, mDay);
        mDatePicker.show();

    }

    private void updateUserData(final DatabaseReference databaseReference) {


        if (MyApplication.isOnline(getApplicationContext())) {
            Toast.makeText(this, getResources().getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
        } else {
            loadingDialogMethod();


            if (mImageUri != null) {


                final StorageReference ref = storageReference.child("profiles/" + UUID.randomUUID().toString());

                ref.putFile(compressedUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                final UserModel userModel = new UserModel();

                                userModel.setUserId(userId);
                                userModel.setUsername(Objects.requireNonNull(usernameTEdt.getEditText()).getText().toString().trim());
                                userModel.setUserFullName(Objects.requireNonNull(userFullNameTEdt.getEditText()).getText().toString().trim());
                                userModel.setUserEmail(userEmailTxt);
                                userModel.setUserGender(Objects.requireNonNull(genderTEdt.getEditText()).getText().toString().trim());
                                userModel.setUserBirthOfDate(Objects.requireNonNull(birthDateTEdt.getEditText()).getText().toString().trim());
                                userModel.setUserBio(Objects.requireNonNull(bioTEdt.getEditText()).getText().toString().trim());
                                userModel.setUserProfileImg(uri.toString());




                                if (profileImgUri != null) {
                                    final StorageReference photoRef = FirebaseStorage.getInstance()
                                            .getReferenceFromUrl(profileImgUri);
                                    photoRef.delete();
                                }



                                databaseReference.setValue(userModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        loadingDialog.dismiss();
                                        Toast.makeText(EditProfileActivity.this,
                                                getResources().getString(R.string.data_updated), Toast.LENGTH_SHORT).show();

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(EditProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }
                        });
                    }
                });


            } else {

                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {

                            final UserModel userModel = snapshot.getValue(UserModel.class);

                            assert userModel != null;
                            userModel.setUserFullName(Objects.requireNonNull(userFullNameTEdt.getEditText()).getText().toString().trim());
                            userModel.setUserEmail(userEmailTxt);
                            userModel.setUserId(userId);
                            userModel.setUsername(Objects.requireNonNull(usernameTEdt.getEditText()).getText().toString().trim());
                            userModel.setUserProfileImg(profileImgUri);
                            userModel.setUserGender(Objects.requireNonNull(genderTEdt.getEditText()).getText().toString().trim());
                            userModel.setUserBirthOfDate(Objects.requireNonNull(birthDateTEdt.getEditText()).getText().toString().trim());
                            userModel.setUserBio(Objects.requireNonNull(bioTEdt.getEditText()).getText().toString().trim());



                            databaseReference.setValue(userModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        loadingDialog.dismiss();
                                        Toast.makeText(EditProfileActivity.this,
                                                getResources().getString(R.string.data_updated), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


            }

        }


    }

    private void loadingDialogMethod() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(EditProfileActivity.this);
        dialog.setCancelable(false);
        dialog.setView(R.layout.loading_dialog);
        loadingDialog = dialog.create();
        loadingDialog.show();

        // Hide after some seconds
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                    Toast.makeText(EditProfileActivity.this, getResources().getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
                }
            }
        };

        loadingDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                handler.removeCallbacks(runnable);
            }
        });

        handler.postDelayed(runnable, 15000);

    }


    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.profileCard:
                chooseImage();
                break;
            case R.id.editProfileBtn:
                updateUserData(userRef);
                break;
            case R.id.coverImg:
                showImageListDialog();
                break;
            case R.id.resetPassBtn:
                showResetPassDialog();
                break;
        }
    }

    private void showResetPassDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);

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
                    Toast.makeText(EditProfileActivity.this, getResources().getString(R.string.email_cannot_null), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(EditProfileActivity.this, getResources().getString(R.string.check_inbox), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else if (MyApplication.isOnline(getApplicationContext())) {
                    Toast.makeText(EditProfileActivity.this, getResources().getString(R.string.no_internet), Toast.LENGTH_SHORT).show();

                } else {
                    String error = Objects.requireNonNull(task.getException()).getMessage();
                    Toast.makeText(EditProfileActivity.this, "error:" + error, Toast.LENGTH_SHORT).show();
                }


            }
        });


    }


    private void showImageListDialog() {


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.choose_cover_layout, null);

        ListView coverListView = view.findViewById(R.id.coverListView);

        coverListView.setAdapter(new CoverListAdapter(this));

        builder.setView(view);

        final AlertDialog dialog = builder.create();
        dialog.show();

        coverListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                if (position == 0) {
                    coverImg.setImageResource(R.drawable.cover1);
                    userRef.child("coverNumber").setValue(position + 1);
                    dialog.dismiss();
                } else if (position == 1) {
                    coverImg.setImageResource(R.drawable.cover2);
                    userRef.child("coverNumber").setValue(position + 1);
                    dialog.dismiss();
                } else if (position == 2) {
                    coverImg.setImageResource(R.drawable.cover3);
                    userRef.child("coverNumber").setValue(position + 1);
                    dialog.dismiss();
                } else if (position == 3) {
                    coverImg.setImageResource(R.drawable.cover4);
                    userRef.child("coverNumber").setValue(position + 1);
                    dialog.dismiss();
                } else if (position == 4) {
                    coverImg.setImageResource(R.drawable.cover5);
                    userRef.child("coverNumber").setValue(position + 1);
                    dialog.dismiss();
                } else if (position == 5) {
                    coverImg.setImageResource(R.drawable.cover6);
                    userRef.child("coverNumber").setValue(position + 1);
                    dialog.dismiss();
                } else if (position == 6) {
                    coverImg.setImageResource(R.drawable.cover7);
                    userRef.child("coverNumber").setValue(position + 1);
                    dialog.dismiss();
                } else if (position == 7) {
                    coverImg.setImageResource(R.drawable.cover8);
                    userRef.child("coverNumber").setValue(position + 1);
                    dialog.dismiss();
                } else if (position == 8) {
                    coverImg.setImageResource(R.drawable.cover9);
                    userRef.child("coverNumber").setValue(position + 1);
                    dialog.dismiss();
                }
            }
        });


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
        super.attachBaseContext(LocaleHelper.onAttach(base, "ar"));
    }


}