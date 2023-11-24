package com.mariodev.novelapp.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mariodev.novelapp.helpers.LocaleHelper;
import com.mariodev.novelapp.helpers.SharedPref;
import com.mariodev.novelapp.models.PoemModel;
import com.mariodev.novelapp.models.PostModel;
import com.mariodev.novelapp.MyApplication;
import com.mariodev.novelapp.R;

import java.util.Calendar;
import java.util.Objects;

public class PostOrPoemActivity extends AppCompatActivity {
    SharedPref sharedPref;
    ImageView arrowBack;
    TextView postOrPoemTv , publishTv , counterTextTv;
    String postOrPoemTxt , userId;
    EditText poemTitleEdt, poemOrPostContentEdt;
    DatabaseReference postRef , poemRef;
    AlertDialog loadingDialog;
    final int MAX_WORDS = 250;
    int countOfWords = 0;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkLightMode();
        MyApplication.setLocale(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_or_poem);
        arrowBack = findViewById(R.id.arrowBack);
        postOrPoemTv = findViewById(R.id.postOrPoemTv);
        poemTitleEdt = findViewById(R.id.poemTitle);
        poemOrPostContentEdt = findViewById(R.id.poemOrPostContentEdt);
        publishTv = findViewById(R.id.publishTv);
        counterTextTv = findViewById(R.id.counterTextTv);

        postOrPoemTxt = getIntent().getStringExtra("whichOne");

        assert postOrPoemTxt != null;
        if (postOrPoemTxt.equals("post")){
            postOrPoemTv.setText(getResources().getString(R.string.new_post));
        }
        else  if (postOrPoemTxt.equals("poem")){
            postOrPoemTv.setText(getResources().getString(R.string.new_poem));
            poemTitleEdt.setVisibility(View.VISIBLE);
            counterTextTv.setVisibility(View.VISIBLE);

         poemOrPostContentEdt.addTextChangedListener(new TextWatcher() {
             @Override
             public void beforeTextChanged(CharSequence s, int start, int before, int count) {

             }

             @Override
             public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

             }

             @SuppressLint("SetTextI18n")
             @Override
             public void afterTextChanged(Editable editable) {
                countOfWords =  countWords(editable.toString());
                 counterTextTv.setText(getResources().getString(R.string.word_count) + countOfWords);
             }
         });

        }

        countOfWords =  countWords(poemOrPostContentEdt.getText().toString());
        counterTextTv.setText(getResources().getString(R.string.word_count) + countOfWords);

        publishTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeKeyboard();
                String poemOrPostContentTxt = poemOrPostContentEdt.getText().toString().trim();

                if (postOrPoemTxt.equals("post")){
                    if (validateContent(poemOrPostContentTxt)){
                        Toast.makeText(PostOrPoemActivity.this, getResources().getString(R.string.post_empty), Toast.LENGTH_SHORT).show();
                    }else {
                        uploadToDB("post");
                    }
                }
                else {
                    if (!validateTitle()){
                        Toast.makeText(PostOrPoemActivity.this, getResources().getString(R.string.enter_poem_title), Toast.LENGTH_SHORT).show();
                    }else {
                        if (validateContent(poemOrPostContentTxt)){
                            Toast.makeText(PostOrPoemActivity.this, getResources().getString(R.string.poem_empty), Toast.LENGTH_SHORT).show();
                        }
                        else {
                            if (countWords(poemOrPostContentEdt.getText().toString()) > MAX_WORDS){
                                Toast.makeText(PostOrPoemActivity.this,getResources().getString(R.string.not_250_word) , Toast.LENGTH_SHORT).show();
                            }else {
                                uploadToDB("poem");
                            }
                        }
                    }
                }
            }
        });



        arrowBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        postRef = FirebaseDatabase.getInstance().getReference("Posts");

        poemRef = FirebaseDatabase.getInstance().getReference("Poems");


    }

    private int countWords(String s) {
        String trim = s.trim();
        if (trim.isEmpty())
            return 0;
        return trim.split("\\s+").length; // separate string around spaces
    }



    private void uploadToDB(String type) {
        loadingDialogMethod();
        Calendar calendar = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm a");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("dd MMM");
        final String currentDate = simpleDateFormat2.format(calendar.getTime());
        final String currentTime = simpleDateFormat.format(calendar.getTime());

        if (type.equals("post")){
          String key = postRef.push().getKey();

            PostModel postModel = new PostModel(
                    userId,
                    key,
                    poemOrPostContentEdt.getText().toString().trim(),
                    currentDate + " " + currentTime
                    );

            assert key != null;
            postRef.child(key).child("data").setValue(postModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    loadingDialog.dismiss();
                    onBackPressed();
                }
            });

        }else {
            String key = poemRef.push().getKey();

            PoemModel poemModel = new PoemModel(
                    userId,
                    key,
                    poemTitleEdt.getText().toString().trim(),
                    poemOrPostContentEdt.getText().toString().trim(),
                    currentDate + " " + currentTime
            );

            assert key != null;
            poemRef.child(key).child("data").setValue(poemModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    loadingDialog.dismiss();
                    onBackPressed();
                }
            });
        }
    }

    private void loadingDialogMethod() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(PostOrPoemActivity.this);
        dialog.setCancelable(false);
        dialog.setView(R.layout.loading_dialog);
        loadingDialog = dialog.create();
        loadingDialog.show();

    }


    private boolean validateContent(String poemOrPostContentTxt){
        return poemOrPostContentTxt.isEmpty();
    }

    private boolean validateTitle(){
        String poemTitleTxt = poemTitleEdt.getText().toString().trim();
        return !poemTitleTxt.isEmpty();
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
    public void onBackPressed() {
        Intent intent = new Intent(PostOrPoemActivity.this, ChoosePostOrPoemActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
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