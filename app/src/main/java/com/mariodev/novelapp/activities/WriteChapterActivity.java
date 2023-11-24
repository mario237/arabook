package com.mariodev.novelapp.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.widget.ImageViewCompat;

import com.anstrontechnologies.corehelper.AnstronCoreHelper;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
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
import com.mariodev.novelapp.helpers.LocaleHelper;
import com.mariodev.novelapp.helpers.SharedPref;
import com.mariodev.novelapp.models.ChapterModel;
import com.mariodev.novelapp.models.StoryModel;

import java.io.File;
import java.util.Calendar;
import java.util.Objects;
import java.util.UUID;

@SuppressWarnings("ALL")
public class WriteChapterActivity extends AppCompatActivity {

    private static final int CODE_IMAGE_GALLERY = 1;
    private static final int PERMISSION_CODE = 10;
    SharedPref sharedPref;
    ConstraintLayout writeChapterLayout, chapterImageLayout;
    LinearLayout addChapterImgLayout, fontStyleLayout;
    ImageView deleteImg, chapterImg, styleOption, saveDraftImd;
    TextView boldText, italicText, underlinedText, normalText, publishTv, counterTv, fontSettingTv;
    ImageView back;
    EditText partTitle, partText;
    Uri mImageUri , compressedUri;

    // instance for firebase storage and StorageReference
    FirebaseStorage storage;
    StorageReference storageReference;
    AnstronCoreHelper coreHelper;

    DatabaseReference draftRef, storyReferences, currStoryRef, userPublishRef, storyRef;

    String url, userId, storyImageAlternative, storyID, chapterId, countOfChapters;


    AlertDialog loadingDialog;

    int number;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        checkLightMode();
        MyApplication.setLocale(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_chapter);


        //init Views
        writeChapterLayout = findViewById(R.id.writeChapterLayout);
        back = findViewById(R.id.arrowBack);
        styleOption = findViewById(R.id.colorOptions);
        chapterImageLayout = findViewById(R.id.chapterImageLayout);
        addChapterImgLayout = findViewById(R.id.addChapterImgLayout);
        chapterImg = findViewById(R.id.chapterImage);
        deleteImg = findViewById(R.id.deleteImg);
        saveDraftImd = findViewById(R.id.saveDraftImg);
        partTitle = findViewById(R.id.partTitle);
        partText = findViewById(R.id.partText);
        fontStyleLayout = findViewById(R.id.fontStyleLayout);
        boldText = findViewById(R.id.boldTxt);
        italicText = findViewById(R.id.italicTxt);
        underlinedText = findViewById(R.id.underlineTxt);
        normalText = findViewById(R.id.normalTxt);
        publishTv = findViewById(R.id.publishTv);
        counterTv = findViewById(R.id.counterText);
        fontSettingTv = findViewById(R.id.fontSettingTv);

        if (getIntent().getStringExtra("fromActivity") != null) {
            publishTv.setVisibility(View.INVISIBLE);
        } else {
            publishTv.setVisibility(View.VISIBLE);

        }

        countOfChapters = getIntent().getStringExtra("chapterNo");


        userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        coreHelper = new AnstronCoreHelper(this);

        draftRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(userId).child("Drafts");

        storyReferences = FirebaseDatabase.getInstance().getReference("Users")
                .child(userId).child("Drafts");

        storyID = getIntent().getStringExtra("storyId");
        chapterId = getIntent().getStringExtra("chapterId");


        currStoryRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .child("Drafts").child(storyID);


        storyRef = FirebaseDatabase.getInstance().getReference("Novels")
                .child("Stories");


//        KeyboardVisibilityEvent.setEventListener(WriteChapterActivity.this, new KeyboardVisibilityEventListener() {
//            @Override
//            public void onVisibilityChanged(boolean isOpen) {
//                if (isOpen) {
//                    Toast.makeText(WriteChapterActivity.this , "opened" , Toast.LENGTH_SHORT);
//                    //fontStyleLayout.setVisibility(View.VISIBLE);
//                } else {
//                    Toast.makeText(WriteChapterActivity.this , "closed" , Toast.LENGTH_SHORT);
//                   // fontStyleLayout.setVisibility(View.INVISIBLE);
//                }
//            }
//        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


        styleOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initStyleOption();
            }
        });

        chapterImageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });


        deleteImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chapterImg.setImageBitmap(null);
                addChapterImgLayout.setVisibility(View.VISIBLE);
                chapterImg.setVisibility(View.GONE);
                deleteImg.setVisibility(View.GONE);
                mImageUri = null;

            }
        });

        boldText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setBoldText();
            }
        });
        italicText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setItalicText();
            }
        });
        underlinedText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setUnderlinedText();
            }
        });
        normalText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getBackToNormalText();
            }
        });


        saveDraftImd.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                String chapterId = getIntent().getStringExtra("chapterId");

                if (chapterId == null && getIntent().getBooleanExtra("addChapter", true)) {
                    if (MyApplication.isOnline(getApplicationContext())) {
                        Toast.makeText(WriteChapterActivity.this, getResources().getString(R.string.no_internet), Toast.LENGTH_SHORT).show();

                    } else {
                        if (getIntent().getStringExtra("publish") != null) {
                            saveChapterToDraft();

                        } else {
                            uploadChapter();
                        }
                    }
                } else {
                    if (MyApplication.isOnline(getApplicationContext())) {
                        Toast.makeText(WriteChapterActivity.this, getResources().getString(R.string.no_internet), Toast.LENGTH_SHORT).show();

                    } else {
                        updateChapterData();
                    }
                }
            }
        });

        publishTv.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                if (MyApplication.isOnline(getApplicationContext())) {
                    Toast.makeText(WriteChapterActivity.this, getResources().getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                } else {
                    publishChapter();
                }
            }
        });

        setChapterData();

        partText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @SuppressLint("SetTextI18n")
            @Override
            public void afterTextChanged(Editable editable) {
                String[] WC = partText.getText().toString().split("\\s+");
                counterTv.setText(getResources().getString(R.string.word_count) + WC.length);
            }
        });

    }

    private void saveChapterToDraft() {

        final String storyId = getIntent().getStringExtra("storyId");
        String storyName = getIntent().getStringExtra("storyName");
        final String storyImageUri = getIntent().getStringExtra("storyImageUri");
        String storyDesc = getIntent().getStringExtra("storyDesc");
        String storyType = getIntent().getStringExtra("storyType");
        String storyTags = getIntent().getStringExtra("storyTags");
        String storyWriter = getIntent().getStringExtra("storyWriter");
        boolean completed = Boolean.parseBoolean(getIntent().getStringExtra("completed"));
        boolean adult = Boolean.parseBoolean(getIntent().getStringExtra("adult"));

        assert storyId != null;
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .child("Drafts")
                .child(storyId);


        final StoryModel storyModel = new StoryModel(
                storyId,
                storyWriter,
                storyImageUri,
                storyName,
                storyDesc,
                storyType,
                storyTags,
                "",
                adult,
                completed,
                false,
                0,
                0,
                0,
                0,
                0,
                ""

        );

        reference.setValue(storyModel).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                final String chapterName = partTitle.getText().toString().trim();
                final Spanned chapterText = partText.getText();
                loadingDialogMethod();
                if (mImageUri != null) {

                    // Defining the child of storageReference
                    final StorageReference ref = storageReference.child("chapters/" + UUID.randomUUID().toString());

                    ref.putFile(compressedUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @RequiresApi(api = Build.VERSION_CODES.N)
                                @Override
                                public void onSuccess(Uri uri) {

                                    url = uri.toString();
                                    String chapterId = draftRef.push().getKey();

                                    final ChapterModel chapterModel = new ChapterModel();

                                    chapterModel.setStoryId(storyID);
                                    chapterModel.setChapterId(chapterId);
                                    chapterModel.setChapterImage(url);
                                    chapterModel.setChapterName(chapterName);
                                    chapterModel.setChapterText(MyApplication.toHtml(chapterText));
                                    chapterModel.setWordCounter(counterTv.getText().toString());


                                    assert chapterId != null;
                                    assert storyID != null;

                                    draftRef.child(storyID).child("Chapters").child(chapterId).setValue(chapterModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            loadingDialog.dismiss();

                                            Intent intent = new Intent(WriteChapterActivity.this, MainPageActivity.class);
                                            intent.putExtra("fragment", "MyNovel");
                                            startActivity(intent);
                                            finish();
                                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                            loadingDialog.dismiss();

                                            Toast.makeText(WriteChapterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnCanceledListener(new OnCanceledListener() {
                                        @Override
                                        public void onCanceled() {
                                            loadingDialog.dismiss();

                                        }
                                    });
                                }


                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    loadingDialog.dismiss();

                                    Toast.makeText(WriteChapterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();


                                }
                            }).addOnCanceledListener(new OnCanceledListener() {
                                @Override
                                public void onCanceled() {
                                    loadingDialog.dismiss();

                                }
                            });


                        }
                    });

                } else {


                    reference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @RequiresApi(api = Build.VERSION_CODES.N)
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            StoryModel storyModel = snapshot.getValue(StoryModel.class);
                            assert storyModel != null;
                            storyImageAlternative = storyModel.getStoryImage();

                            String chapterId = draftRef.push().getKey();
                            final ChapterModel model = new ChapterModel();

                            model.setStoryId(storyId);
                            model.setChapterId(chapterId);
                            model.setChapterName(chapterName);
                            model.setChapterImage(storyImageUri);
                            model.setChapterText(MyApplication.toHtml(chapterText));
                            model.setWordCounter(counterTv.getText().toString());

                            assert chapterId != null;
                            draftRef.child(storyId).child("Chapters").child(chapterId).setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    loadingDialog.dismiss();

                                    Intent intent = new Intent(WriteChapterActivity.this, MainPageActivity.class);
                                    intent.putExtra("fragment", "MyNovel");
                                    startActivity(intent);
                                    finish();
                                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    loadingDialog.dismiss();

                                    Toast.makeText(WriteChapterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }).addOnCanceledListener(new OnCanceledListener() {
                                @Override
                                public void onCanceled() {
                                    loadingDialog.dismiss();

                                }
                            });

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


                }


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(WriteChapterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }


    private void publishChapter() {
         String storyID = getIntent().getStringExtra("storyId");
         String chapterID = getIntent().getStringExtra("chapterId");



        Calendar calendar = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm a");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("dd MMM");
        final String currentDate = simpleDateFormat2.format(calendar.getTime());
        final String currentTime = simpleDateFormat.format(calendar.getTime());


        loadingDialogMethod();

        final DatabaseReference chapterPublishRef = FirebaseDatabase.getInstance().getReference("Novels")
                .child("Chapters").child(storyID);

        if (getIntent().getStringExtra("publish") != null) {
            final String chapterName = partTitle.getText().toString().trim();
            final Spanned chapterText = partText.getText();


            if (mImageUri != null) {

                // Defining the child of storageReference
                final StorageReference ref = storageReference.child("chapters/" + UUID.randomUUID().toString());

                ref.putFile(compressedUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @RequiresApi(api = Build.VERSION_CODES.N)
                            @Override
                            public void onSuccess(Uri uri) {

                                url = uri.toString();
                                final String storyID = getIntent().getStringExtra("storyId");
                                String chapterId = chapterPublishRef.push().getKey();

                                final ChapterModel chapterModel = new ChapterModel(
                                        storyID,
                                        chapterId,
                                        url,
                                        chapterName,
                                        MyApplication.toHtml(chapterText),
//                                        Html.toHtml(chapterText, Html.FROM_HTML_MODE_LEGACY),
                                        currentDate + " " + currentTime,
                                        counterTv.getText().toString()
                                );

                                assert chapterId != null;
                                assert storyID != null;

                                chapterPublishRef.child(chapterId).child("data").setValue(chapterModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        loadingDialog.dismiss();
                                        Intent intent = new Intent(WriteChapterActivity.this, PublishedStoryActivity.class);
                                        intent.putExtra("storyId", chapterModel.getStoryId());
                                        startActivity(intent);
                                        finish();
                                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        loadingDialog.dismiss();

                                        Toast.makeText(WriteChapterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnCanceledListener(new OnCanceledListener() {
                                    @Override
                                    public void onCanceled() {
                                        loadingDialog.dismiss();

                                    }
                                });
                            }


                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                loadingDialog.dismiss();

                                Toast.makeText(WriteChapterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();


                            }
                        }).addOnCanceledListener(new OnCanceledListener() {
                            @Override
                            public void onCanceled() {
                                loadingDialog.dismiss();

                            }
                        });


                    }
                });

            }
            else {

                String chapterId = chapterPublishRef.push().getKey();

                final ChapterModel chapterModel = new ChapterModel(
                        storyID,
                        chapterId,
                        getIntent().getStringExtra("storyImageUri"),
                        chapterName,
                        MyApplication.toHtml(chapterText),
                        currentDate + " " + currentTime,
                        counterTv.getText().toString()

                );

                assert chapterId != null;

                chapterPublishRef.child(chapterId).child("data").setValue(chapterModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        loadingDialog.dismiss();
                        Intent intent = new Intent(WriteChapterActivity.this, PublishedStoryActivity.class);
                        intent.putExtra("storyId", chapterModel.getStoryId());
                        startActivity(intent);
                        finish();
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        loadingDialog.dismiss();

                        Toast.makeText(WriteChapterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnCanceledListener(new OnCanceledListener() {
                    @Override
                    public void onCanceled() {
                        loadingDialog.dismiss();

                    }
                });


            }

        } else {
            Calendar calendar2 = Calendar.getInstance();
            @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat3 = new SimpleDateFormat("hh:mm a");
            @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat4 = new SimpleDateFormat("dd MMM");
            final String currentStoryDate = simpleDateFormat4.format(calendar.getTime());
            final String currentStoryTime = simpleDateFormat3.format(calendar.getTime());
            if (!storyID.isEmpty() && !chapterID.isEmpty()) {

                final DatabaseReference currChapterRef = FirebaseDatabase.getInstance().getReference("Users")
                        .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                        .child("Drafts").child(storyID).child("Chapters").child(chapterID);

                final DatabaseReference publishedStoryRef = FirebaseDatabase.getInstance().getReference("Novels");
                final DatabaseReference publishedUserRef = FirebaseDatabase.getInstance().getReference("Users");


                currStoryRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()) {
                            StoryModel storyModel = snapshot.getValue(StoryModel.class);

                            assert storyModel != null;
                            storyModel.setDateTime(currentStoryDate + " " + currentStoryTime);
                            publishedStoryRef.child("Stories").child(storyModel.getStoryId()).child("data").setValue(storyModel);
                            publishedUserRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child("PublishedStories").child(storyModel.getStoryId()).setValue(storyModel.getStoryId());

                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


                currChapterRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            final ChapterModel chapterModel = snapshot.getValue(ChapterModel.class);

                            assert chapterModel != null;

                            chapterModel.setChapterDateTime(currentDate + " " + currentTime);

                            publishedStoryRef.child("Chapters").child(chapterModel.getStoryId()).child(chapterModel.getChapterId()).child("data")
                                    .setValue(chapterModel);

                            if (countOfChapters != null && countOfChapters.equals("1")) {
                                currStoryRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        loadingDialog.dismiss();
                                        Intent intent = new Intent(WriteChapterActivity.this, MainPageActivity.class);
                                        intent.putExtra("fragment", "MyNovel");
                                        startActivity(intent);
                                        finish();
                                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

                                    }
                                });
                            } else {


                                currChapterRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        loadingDialog.dismiss();
                                        Intent intent = new Intent(WriteChapterActivity.this, MainPageActivity.class);
                                        intent.putExtra("fragment", "MyNovel");
                                        startActivity(intent);
                                        finish();
                                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

                                    }
                                });
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }

    }


    private void chooseImage() {
        if (ActivityCompat.checkSelfPermission(WriteChapterActivity.this , Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(WriteChapterActivity.this , new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE} , PERMISSION_CODE);
        }else {
            startActivityForResult(new Intent().setAction(Intent.ACTION_GET_CONTENT)
                    .setType("image/*") , CODE_IMAGE_GALLERY);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CODE_IMAGE_GALLERY && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            mImageUri = data.getData();

            // Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mImageUri);
            File file = new File(SiliCompressor.with(this).compress(FileUtils.getPath(this , mImageUri) ,
                    new File(this.getCacheDir() , "temp")));

            compressedUri = Uri.fromFile(file);
            addChapterImgLayout.setVisibility(View.GONE);
            chapterImg.setVisibility(View.VISIBLE);
            deleteImg.setVisibility(View.VISIBLE);
            chapterImg.setImageURI(compressedUri);
            chapterImg.setScaleType(ImageView.ScaleType.CENTER_CROP);

        }
    }


    private void initStyleOption() {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(WriteChapterActivity.this);
        View bottomSheetView = LayoutInflater.from(getApplicationContext())
                .inflate(R.layout.layout_miscellaneous, (LinearLayout) findViewById(R.id.layout_miscellaneous));

        final ImageView imageColor1 = bottomSheetView.findViewById(R.id.imageColor1);
        final ImageView imageColor2 = bottomSheetView.findViewById(R.id.imageColor2);
        final ImageView imageColor3 = bottomSheetView.findViewById(R.id.imageColor3);
        final TextView addTxt = findViewById(R.id.add_txt);

        if (((ColorDrawable) writeChapterLayout.getBackground()).getColor()
                == getColor(R.color.colorPurple)) {
            imageColor1.setImageResource(R.drawable.ic_done);
            imageColor2.setImageResource(0);
            imageColor3.setImageResource(0);
        } else if (((ColorDrawable) writeChapterLayout.getBackground()).getColor()
                == getColor(R.color.colorAlwaysBlack)) {
            imageColor1.setImageResource(0);
            imageColor2.setImageResource(R.drawable.ic_done);
            imageColor3.setImageResource(0);
        } else {
            imageColor1.setImageResource(0);
            imageColor2.setImageResource(0);
            imageColor3.setImageResource(R.drawable.ic_done);
        }

        bottomSheetView.findViewById(R.id.viewColor1).setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onClick(View view) {
                writeChapterLayout.setBackgroundColor(getColor(R.color.colorPurple));
                back.setColorFilter(getResources().getColor( R.color.colorAlwaysWhite),
                        android.graphics.PorterDuff.Mode.SRC_IN);
                ImageViewCompat.setImageTintList(styleOption, ColorStateList.valueOf(getColor(R.color.colorAlwaysWhite)));
                ImageViewCompat.setImageTintList(saveDraftImd, ColorStateList.valueOf(getColor(R.color.colorAlwaysWhite)));
                publishTv.setTextColor(getColor(R.color.colorPink));
                addChapterImgLayout.setBackground(getDrawable(R.drawable.white_border));
                ImageViewCompat.setImageTintList((ImageView) findViewById(R.id.add_img), ColorStateList.valueOf(getColor(R.color.colorAlwaysWhite)));
                addTxt.setTextColor(getColor(R.color.colorAlwaysWhite));
                partTitle.setBackgroundColor(getColor(R.color.colorPurple));
                partTitle.setTextColor(getColor(R.color.colorAlwaysWhite));
                partText.setBackgroundColor(getColor(R.color.colorPurple));
                partText.setTextColor(getColor(R.color.colorAlwaysWhite));
                imageColor1.setImageResource(R.drawable.ic_done);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                boldText.setTextColor(getColor(R.color.colorAlwaysWhite));
                italicText.setTextColor(getColor(R.color.colorAlwaysWhite));
                underlinedText.setTextColor(getColor(R.color.colorAlwaysWhite));
                normalText.setTextColor(getColor(R.color.colorAlwaysWhite));
                counterTv.setTextColor(getColor(R.color.colorAlwaysWhite));
                fontSettingTv.setTextColor(getColor(R.color.colorPink));
            }
        });

        bottomSheetView.findViewById(R.id.viewColor2).setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onClick(View view) {
                writeChapterLayout.setBackgroundColor(getColor(R.color.colorAlwaysBlack));
                back.setColorFilter(getResources().getColor( R.color.colorAlwaysWhite),
                        android.graphics.PorterDuff.Mode.SRC_IN);
                ImageViewCompat.setImageTintList(styleOption, ColorStateList.valueOf(getColor(R.color.colorAlwaysWhite)));
                ImageViewCompat.setImageTintList(saveDraftImd, ColorStateList.valueOf(getColor(R.color.colorAlwaysWhite)));
                publishTv.setTextColor(getColor(R.color.colorAlwaysWhite));
                addChapterImgLayout.setBackground(getResources().getDrawable(R.drawable.white_border));
                ImageViewCompat.setImageTintList((ImageView) findViewById(R.id.add_img), ColorStateList.valueOf(getColor(R.color.colorAlwaysWhite)));
                addTxt.setTextColor(getColor(R.color.colorAlwaysWhite));
                partTitle.setBackgroundColor(getColor(R.color.colorAlwaysBlack));
                partTitle.setTextColor(getColor(R.color.colorAlwaysWhite));
                partText.setBackgroundColor(getColor(R.color.colorAlwaysBlack));
                partText.setTextColor(getColor(R.color.colorAlwaysWhite));
                partText.setBackgroundColor(getColor(R.color.colorAlwaysBlack));
                partText.setTextColor(getColor(R.color.colorAlwaysWhite));
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(R.drawable.ic_done);
                imageColor3.setImageResource(0);
                boldText.setTextColor(getColor(R.color.colorAlwaysWhite));
                italicText.setTextColor(getColor(R.color.colorAlwaysWhite));
                underlinedText.setTextColor(getColor(R.color.colorAlwaysWhite));
                normalText.setTextColor(getColor(R.color.colorAlwaysWhite));
                counterTv.setTextColor(getColor(R.color.colorAlwaysWhite));
                fontSettingTv.setTextColor(getColor(R.color.colorAlwaysWhite));


            }
        });

        bottomSheetView.findViewById(R.id.viewColor3).setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onClick(View view) {
                writeChapterLayout.setBackgroundColor(getColor(R.color.colorAlwaysWhite));
                back.setColorFilter(getResources().getColor( R.color.colorAlwaysBlack),
                        android.graphics.PorterDuff.Mode.SRC_IN);
                ImageViewCompat.setImageTintList(styleOption, ColorStateList.valueOf(getColor(R.color.colorAlwaysBlack)));
                ImageViewCompat.setImageTintList(saveDraftImd, ColorStateList.valueOf(getColor(R.color.colorAlwaysBlack)));
                publishTv.setTextColor(getColor(R.color.colorAlwaysBlack));
                ImageViewCompat.setImageTintList((ImageView) findViewById(R.id.add_img), ColorStateList.valueOf(getColor(R.color.colorAlwaysBlack)));
                addTxt.setTextColor(getColor(R.color.colorAlwaysBlack));
                partTitle.setBackgroundColor(getColor(R.color.colorAlwaysWhite));
                partTitle.setTextColor(getColor(R.color.colorAlwaysBlack));
                partText.setBackgroundColor(getColor(R.color.colorAlwaysWhite));
                partText.setTextColor(getColor(R.color.colorAlwaysBlack));
                partText.setBackgroundColor(getColor(R.color.colorAlwaysWhite));
                partText.setTextColor(getColor(R.color.colorAlwaysBlack));
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(R.drawable.ic_done);
                boldText.setTextColor(getColor(R.color.colorAlwaysBlack));
                italicText.setTextColor(getColor(R.color.colorAlwaysBlack));
                underlinedText.setTextColor(getColor(R.color.colorAlwaysBlack));
                normalText.setTextColor(getColor(R.color.colorAlwaysBlack));
                fontSettingTv.setTextColor(getColor(R.color.colorAlwaysBlack));


            }
        });


        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    private void setBoldText() {
        int startSelection = partText.getSelectionStart();
        int endSelection = partText.getSelectionEnd();
        String selectedTxt = partText.getText().subSequence(startSelection, endSelection).toString();
        String formatted = "<b>" + selectedTxt + "</b>";
        partText.setSelection(startSelection, endSelection);
        Editable editable = partText.getText();
        editable.replace(startSelection, endSelection, MyApplication.fromHtml(formatted)); //Html.fromHtml(formatted));


    }

    private void setUnderlinedText() {
        int startSelection = partText.getSelectionStart();
        int endSelection = partText.getSelectionEnd();
        String selectedTxt = partText.getText().subSequence(startSelection, endSelection).toString();
        String formatted = "<u>" + selectedTxt + "</u>";
        Editable editable = partText.getEditableText();
        editable.replace(startSelection, endSelection, MyApplication.fromHtml(formatted)); //Html.fromHtml(formatted));
    }

    private void setItalicText() {
        int startSelection = partText.getSelectionStart();
        int endSelection = partText.getSelectionEnd();
        String selectedTxt = partText.getText().subSequence(startSelection, endSelection).toString();
        String formatted = "<i>" + selectedTxt + "</i>";
        Editable editable = partText.getEditableText();
        editable.replace(startSelection, endSelection, MyApplication.fromHtml(formatted)); //Html.fromHtml(formatted));

    }


    private void getBackToNormalText() {

        int startSelection = partText.getSelectionStart();
        int endSelection = partText.getSelectionEnd();

        Spannable str = partText.getText();
        StyleSpan[] ss = str.getSpans(startSelection, endSelection, StyleSpan.class);
        UnderlineSpan[] ss2 = str.getSpans(startSelection, endSelection, UnderlineSpan.class);


        for (StyleSpan s : ss) {
            if (s.getStyle() == Typeface.BOLD) {
                str.removeSpan(s);
            }
            if (s.getStyle() == Typeface.ITALIC) {
                str.removeSpan(s);
            }

        }

        for (UnderlineSpan underlineSpan : ss2) {

            str.removeSpan(underlineSpan);
        }

        partText.setText(str);

        partText.setSelection(startSelection, endSelection);

    }


    private void uploadChapter() {

        String partTitleTxt = partTitle.getText().toString().trim();
        String partTextTxt = partText.getText().toString().trim();
        if (partTextTxt.isEmpty() || partTitleTxt.isEmpty()) {
            Toast.makeText(this, R.string.chapter_error, Toast.LENGTH_LONG).show();
        } else {
            final String chapterName = partTitle.getText().toString().trim();
            final Spanned chapterText = partText.getText();
            if (MyApplication.isOnline(getApplicationContext())) {
                loadingDialog.dismiss();
                Toast.makeText(this, getResources().getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
            } else {

                loadingDialogMethod();

                if (mImageUri != null) {


                    // Defining the child of storageReference
                    final StorageReference ref = storageReference.child("chapters/" + UUID.randomUUID().toString());

                    ref.putFile(compressedUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {


                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @RequiresApi(api = Build.VERSION_CODES.N)
                                @Override
                                public void onSuccess(Uri uri) {

                                    url = uri.toString();
                                    String storyID = getIntent().getStringExtra("storyId");
                                    String chapterId = draftRef.push().getKey();

                                    final ChapterModel chapterModel = new ChapterModel();

                                    chapterModel.setStoryId(storyID);
                                    chapterModel.setChapterId(chapterId);
                                    chapterModel.setChapterImage(url);
                                    chapterModel.setChapterName(chapterName);
                                    chapterModel.setChapterText(MyApplication.toHtml(chapterText));
                                    chapterModel.setWordCounter(counterTv.getText().toString());


                                    assert chapterId != null;
                                    assert storyID != null;

                                    draftRef.child(storyID).child("Chapters").child(chapterId).setValue(chapterModel)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    loadingDialog.dismiss();
                                                    Intent intent = new Intent(WriteChapterActivity.this, DraftStoryDetailsActivity.class);
                                                    intent.putExtra("storyId", chapterModel.getStoryId());
                                                    startActivity(intent);
                                                    finish();
                                                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                            loadingDialog.dismiss();

                                            Toast.makeText(WriteChapterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnCanceledListener(new OnCanceledListener() {
                                        @Override
                                        public void onCanceled() {
                                            loadingDialog.dismiss();

                                        }
                                    });
                                }


                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    loadingDialog.dismiss();

                                    Toast.makeText(WriteChapterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();


                                }
                            }).addOnCanceledListener(new OnCanceledListener() {
                                @Override
                                public void onCanceled() {
                                    loadingDialog.dismiss();

                                }
                            });


                        }
                    });

                } else {

                    final String storyID = getIntent().getStringExtra("storyId");
                    assert storyID != null;
                    storyReferences = storyReferences.child(storyID);

                    storyReferences.addListenerForSingleValueEvent(new ValueEventListener() {
                        @RequiresApi(api = Build.VERSION_CODES.N)
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                StoryModel storyModel = snapshot.getValue(StoryModel.class);
                                assert storyModel != null;
                                storyImageAlternative = storyModel.getStoryImage();
                                String chapterId = draftRef.push().getKey();
                                final ChapterModel model = new ChapterModel();

                                model.setStoryId(storyID);
                                model.setChapterId(chapterId);
                                model.setChapterName(chapterName);
                                model.setChapterImage(storyImageAlternative);
                                model.setChapterText(MyApplication.toHtml(chapterText));
                                model.setWordCounter(counterTv.getText().toString());

                                assert chapterId != null;
                                draftRef.child(storyID).child("Chapters").child(chapterId).setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        loadingDialog.dismiss();

                                        Intent intent = new Intent(WriteChapterActivity.this, DraftStoryDetailsActivity.class);
                                        intent.putExtra("storyId", model.getStoryId());
                                        startActivity(intent);
                                        finish();
                                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        loadingDialog.dismiss();

                                        Toast.makeText(WriteChapterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnCanceledListener(new OnCanceledListener() {
                                    @Override
                                    public void onCanceled() {
                                        loadingDialog.dismiss();

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
    }


    private void loadingDialogMethod() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(WriteChapterActivity.this);
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
                    Toast.makeText(WriteChapterActivity.this, getResources().getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
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

    private void warningDialog() {

        if ((getIntent().getStringExtra("publish") != null)) {
            Intent intent = new Intent(WriteChapterActivity.this, MainPageActivity.class);
            intent.putExtra("fragment", "MyNovel");
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        } else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(WriteChapterActivity.this, R.style.DialogTheme);
            builder.setMessage(R.string.draft_warning)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.N)
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                            uploadChapter();
                        }
                    })
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(WriteChapterActivity.this, MainPageActivity.class);
                            intent.putExtra("fragment", "MyNovel");
                            startActivity(intent);
                            finish();
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

                        }
                    });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }

    }

    private void setChapterData() {

        String storyID = getIntent().getStringExtra("storyId");
        String chapterId = getIntent().getStringExtra("chapterId");

        if (storyID != null && chapterId != null) {


            draftRef = draftRef.child(storyID).child("Chapters").child(chapterId);

            draftRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {

                        ChapterModel chapterModel = snapshot.getValue(ChapterModel.class);
                        assert chapterModel != null;
                        String imageUri = chapterModel.getChapterImage();
                        String chapterName = chapterModel.getChapterName();
                        String chapterText = chapterModel.getChapterText();

                        if (imageUri != null) {
                            Glide.with(getApplicationContext()).load(imageUri).into(chapterImg);
                            chapterImg.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            addChapterImgLayout.setVisibility(View.GONE);
                            chapterImg.setVisibility(View.VISIBLE);
                            deleteImg.setVisibility(View.VISIBLE);
                        }


                        partTitle.setText(chapterName);
                        partText.setText(MyApplication.fromHtml(chapterText)); //(Html.fromHtml(chapterText));


                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }

    }

    private void updateChapterData() {

        String storyImage = getIntent().getStringExtra("storyImage");
        String chapterImage = getIntent().getStringExtra("chapterImage");

        if (MyApplication.isOnline(getApplicationContext())) {
            showGoBackDialog();
        } else {
            final String chapterNameAlter = partTitle.getText().toString();
            final Spanned chapterTextAlter = partText.getText();

            final String storyID = getIntent().getStringExtra("storyId");
            final String chapterId = getIntent().getStringExtra("chapterId");
            assert storyID != null;
            assert chapterId != null;


            loadingDialogMethod();

            if (mImageUri != null) {


                if (storyImage != null && chapterImage != null) {
                    if (!chapterImage.equals(storyImage)) {
                        final StorageReference storyPhotoRef = FirebaseStorage.getInstance().getReferenceFromUrl(chapterImage);
                        storyPhotoRef.delete();
                    }
                }
                // Defining the child of storageReference
                final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users")
                        .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                        .child("Drafts").child(storyID).child("Chapters")
                        .child(chapterId);



                // Defining the child of storageReference
                final StorageReference ref = storageReference.child("chapters/" + UUID.randomUUID().toString());
                ref.putFile(compressedUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @RequiresApi(api = Build.VERSION_CODES.N)
                            @Override
                            public void onSuccess(Uri uri) {

                                url = uri.toString();

                                final ChapterModel chapterModel = new ChapterModel();
                                chapterModel.setStoryId(storyID);
                                chapterModel.setChapterId(chapterId);
                                chapterModel.setChapterName(chapterNameAlter);
                                chapterModel.setChapterImage(url);
                                chapterModel.setChapterText(MyApplication.toHtml(chapterTextAlter));
                                chapterModel.setWordCounter(counterTv.getText().toString());

                                databaseReference.setValue(chapterModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        loadingDialog.dismiss();
                                        Intent intent = new Intent(WriteChapterActivity.this, DraftStoryDetailsActivity.class);
                                        intent.putExtra("storyId", chapterModel.getStoryId());
                                        startActivity(intent);
                                        finish();
                                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

                                    }
                                });


                            }
                        });

                    }
                });


            } else {

                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users")
                        .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                        .child("Drafts").child(storyID).child("Chapters")
                        .child(chapterId);
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        final String storyId = getIntent().getStringExtra("storyId");
                        if (snapshot.exists()) {
                            final ChapterModel chapterModel = snapshot.getValue(ChapterModel.class);
                            assert chapterModel != null;

                            String chapterName = chapterModel.getChapterName();
                            String chapterText = chapterModel.getChapterText();

                            String currChapterName = partTitle.getText().toString();
                            Spanned currChapterText = partText.getText();


                            if (currChapterName.equals(chapterName) && MyApplication.toHtml(currChapterText).equals(chapterText)) {

                                //Html.toHtml(currChapterText,Html.FROM_HTML_MODE_LEGACY)
                                loadingDialog.dismiss();
                                Intent intent = new Intent(WriteChapterActivity.this, DraftStoryDetailsActivity.class);
                                intent.putExtra("storyId", storyId);
                                startActivity(intent);
                                finish();
                                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

                            } else {

                                chapterModel.setStoryId(storyID);
                                chapterModel.setChapterId(chapterId);
                                chapterModel.setChapterName(chapterNameAlter);
                                chapterModel.setChapterImage(chapterModel.getChapterImage());
                                chapterModel.setChapterText(MyApplication.toHtml(chapterTextAlter));
                                chapterModel.setWordCounter(counterTv.getText().toString());

                                //(Html.toHtml(chapterTextAlter, Html.FROM_HTML_MODE_LEGACY));

                                draftRef.setValue(chapterModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        loadingDialog.dismiss();
                                        Intent intent = new Intent(WriteChapterActivity.this, DraftStoryDetailsActivity.class);
                                        intent.putExtra("storyId", storyId);
                                        startActivity(intent);
                                        finish();
                                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        loadingDialog.dismiss();
                                        Toast.makeText(WriteChapterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });


                            }

                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        loadingDialog.dismiss();

                        Toast.makeText(WriteChapterActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }


                });


            }

        }
    }


    @Override
    public void onBackPressed() {

        String chapterId = getIntent().getStringExtra("chapterId");
        if (chapterId != null) {
            if (MyApplication.isOnline(getApplicationContext())) {
                showGoBackDialog();
            } else {
                updateChapterData();
            }
        } else {
            if (MyApplication.isOnline(getApplicationContext())) {
                showGoBackDialog();
            } else {
                warningDialog();
            }
        }

    }

    private void showGoBackDialog() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(WriteChapterActivity.this, R.style.DialogTheme);
        String message = getResources().getString(R.string.no_internet) + " " + getResources().getString(R.string.go_back);
        builder.setMessage(message)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(WriteChapterActivity.this, DraftStoryDetailsActivity.class);
                        intent.putExtra("storyId", storyID);
                        startActivity(intent);
                        finish();
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

                    }
                })
                .setNegativeButton(R.string.no, null);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
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