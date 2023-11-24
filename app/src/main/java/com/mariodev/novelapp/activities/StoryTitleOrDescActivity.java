package com.mariodev.novelapp.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mariodev.novelapp.helpers.LocaleHelper;
import com.mariodev.novelapp.helpers.SharedPref;
import com.mariodev.novelapp.models.StoryModel;
import com.mariodev.novelapp.MyApplication;
import com.mariodev.novelapp.R;

import java.util.Objects;

public class StoryTitleOrDescActivity extends AppCompatActivity {

    SharedPref sharedPref;

    TextView barTitle;
    ImageView backImg;
    EditText storyEdt;
    String storyName, storyDesc, storyId, userId;
    DatabaseReference reference, publishedRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkLightMode();
        MyApplication.setLocale(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_title_or_desc);

        barTitle = findViewById(R.id.editStoryTxt);
        backImg = findViewById(R.id.arrowBack);
        storyEdt = findViewById(R.id.storyEdt);




        storyName = getIntent().getStringExtra("storyName");
        storyDesc = getIntent().getStringExtra("storyDesc");

        if (storyName != null) {
            barTitle.setText(getResources().getString(R.string.edit_story_name));
            storyEdt.setText(storyName);
        } else if (storyDesc != null) {
            barTitle.setText(getResources().getString(R.string.edit_story_desc));
            storyEdt.setText(storyDesc);
        }


        storyId = getIntent().getStringExtra("storyId");

        userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        reference = FirebaseDatabase.getInstance().getReference("Users")
                .child(userId).child("Drafts").child(storyId);

        publishedRef = FirebaseDatabase.getInstance().getReference("Novels").child("Stories").child(storyId);

        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {

        if (getIntent().getStringExtra("from") != null) {
            publishedRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    StoryModel storyModel = snapshot.child("data").getValue(StoryModel.class);
                    assert storyModel != null;

                    String storyEdtTxt = storyEdt.getText().toString().trim();

                    if (storyName != null && storyModel.getStoryName().equals(storyEdtTxt)) {
                        Intent intent = new Intent(StoryTitleOrDescActivity.this, PublishedStoryActivity.class);
                        intent.putExtra("storyId", storyId);
                        startActivity(intent);
                        finish();
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

                    } else if (storyDesc != null && storyModel.getStoryDescription().equals(storyEdtTxt)) {
                        Intent intent = new Intent(StoryTitleOrDescActivity.this, PublishedStoryActivity.class);
                        intent.putExtra("storyId", storyId);
                        startActivity(intent);
                        finish();
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

                    } else {
                        showBackDialog();
                    }


                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        } else {
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    StoryModel storyModel = snapshot.getValue(StoryModel.class);
                    assert storyModel != null;

                    String storyEdtTxt = storyEdt.getText().toString().trim();

                    if (storyName != null && storyModel.getStoryName().equals(storyEdtTxt)) {
                        Intent intent = new Intent(StoryTitleOrDescActivity.this, DraftStoryDetailsActivity.class);
                        intent.putExtra("storyId", storyId);
                        startActivity(intent);
                        finish();
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

                    } else if (storyDesc != null && storyModel.getStoryDescription().equals(storyEdtTxt)) {
                        Intent intent = new Intent(StoryTitleOrDescActivity.this, DraftStoryDetailsActivity.class);
                        intent.putExtra("storyId", storyId);
                        startActivity(intent);
                        finish();
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

                    } else {
                        showBackDialog();
                    }


                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }

    }

    private void showBackDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(StoryTitleOrDescActivity.this, R.style.DialogTheme);
        String message = getResources().getString(R.string.accept_edit_message);
        builder.setMessage(message)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        editStoryData();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (getIntent().getStringExtra("from") != null) {
                            Intent intent = new Intent(StoryTitleOrDescActivity.this, PublishedStoryActivity.class);
                            intent.putExtra("storyId", storyId);
                            startActivity(intent);
                            finish();
                            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

                        } else {
                            Intent intent = new Intent(StoryTitleOrDescActivity.this, DraftStoryDetailsActivity.class);
                            intent.putExtra("storyId", storyId);
                            startActivity(intent);
                            finish();
                            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

                        }
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void editStoryData() {
        String storyEdtTxt = storyEdt.getText().toString().trim();

        if (getIntent().getStringExtra("from") != null) {
            if (storyName != null) {
                publishedRef.child("data").child("storyName").setValue(storyEdtTxt).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Intent intent = new Intent(StoryTitleOrDescActivity.this, PublishedStoryActivity.class);
                        intent.putExtra("storyId", storyId);
                        startActivity(intent);
                        finish();
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(StoryTitleOrDescActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else if (storyDesc != null) {
                publishedRef.child("data").child("storyDescription").setValue(storyEdtTxt).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Intent intent = new Intent(StoryTitleOrDescActivity.this, PublishedStoryActivity.class);
                        intent.putExtra("storyId", storyId);
                        startActivity(intent);
                        finish();
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(StoryTitleOrDescActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else {
            if (storyName != null) {
                reference.child("storyName").setValue(storyEdtTxt).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Intent intent = new Intent(StoryTitleOrDescActivity.this, DraftStoryDetailsActivity.class);
                        intent.putExtra("storyId", storyId);
                        startActivity(intent);
                        finish();
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(StoryTitleOrDescActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else if (storyDesc != null) {
                reference.child("storyDescription").setValue(storyEdtTxt).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Intent intent = new Intent(StoryTitleOrDescActivity.this, DraftStoryDetailsActivity.class);
                        intent.putExtra("storyId", storyId);
                        startActivity(intent);
                        finish();
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(StoryTitleOrDescActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
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