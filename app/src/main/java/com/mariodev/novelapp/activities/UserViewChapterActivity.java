package com.mariodev.novelapp.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mariodev.novelapp.adapters.ChapterPagerAdapter;
import com.mariodev.novelapp.adapters.DrawerListAdapter;
import com.mariodev.novelapp.helpers.LocaleHelper;
import com.mariodev.novelapp.helpers.SharedPref;
import com.mariodev.novelapp.models.ChapterModel;
import com.mariodev.novelapp.models.StoryModel;
import com.mariodev.novelapp.models.UserModel;
import com.mariodev.novelapp.MyApplication;
import com.mariodev.novelapp.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("ALL")
public class UserViewChapterActivity extends AppCompatActivity implements DrawerListAdapter.OnItemClickListener {

    SharedPref sharedPref;
    final Context mContext = UserViewChapterActivity.this;

    ImageView arrowBack, openNavImg;
    TextView storyNameTv;
    DrawerLayout drawerLayout;
    CoordinatorLayout coordinatorLayout;
    ProgressBar loadChapterData;
    ViewPager chapterViewPager;
    List<ChapterModel> chapterModelList;
    ChapterPagerAdapter chapterPagerAdapter;
    DatabaseReference chapterRef, storyRef, viewRef, userRef , libraryRef;
    DrawerListAdapter drawerListAdapter;
    RecyclerView chapterRecyclerList;
    String userId, chapterId, storyId , writerId;

    Animation fromBottom, toBottom, rotateOpen, rotateClose;
    FloatingActionButton showActionsBtn, voteActionBtn, commentActionBtn;

    Boolean clicked = false;

    InterstitialAd interstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_view_chapter);


        sharedPref = new SharedPref(this);


        storyId = getIntent().getStringExtra("storyId");




        coordinatorLayout = findViewById(R.id.coordinatorLayout);
        arrowBack = findViewById(R.id.arrowBack);
        storyNameTv = findViewById(R.id.storyNameTv);
        drawerLayout = findViewById(R.id.drawerLayout);
        openNavImg = findViewById(R.id.openNavImg);
        loadChapterData = findViewById(R.id.loadChapterData);
        chapterViewPager = findViewById(R.id.chapterViewPager);
        chapterRecyclerList = findViewById(R.id.chapterRecyclerList);
        showActionsBtn = findViewById(R.id.showActionsBtn);
        voteActionBtn = findViewById(R.id.voteActionBtn);
        commentActionBtn = findViewById(R.id.commentActionBtn);

        chapterRecyclerList.setHasFixedSize(true);
        chapterRecyclerList.setLayoutManager(new LinearLayoutManager(this));




        storyNameTv.setText(getIntent().getStringExtra("storyName"));

        userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        chapterModelList = new ArrayList<>();

        chapterRef = FirebaseDatabase.getInstance().getReference("Novels")
                .child("Chapters").child(storyId);

        storyRef = FirebaseDatabase.getInstance().getReference("Novels")
                .child("Stories").child(storyId);

        userRef = FirebaseDatabase.getInstance().getReference("Users");

        libraryRef = FirebaseDatabase.getInstance().getReference("Novels").child("Stories")
                .child(storyId).child("Users");

        chapterRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    chapterModelList.clear();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        ChapterModel chapterModel = dataSnapshot.child("data").getValue(ChapterModel.class);
                        assert chapterModel != null;
                        chapterModelList.add(chapterModel);

                    }
                    chapterPagerAdapter = new ChapterPagerAdapter(UserViewChapterActivity.this,
                            chapterModelList);

                    chapterViewPager.setAdapter(chapterPagerAdapter);
                    chapterPagerAdapter.notifyDataSetChanged();


                    drawerListAdapter = new DrawerListAdapter(UserViewChapterActivity.this,
                            chapterModelList);
                    chapterRecyclerList.setAdapter(drawerListAdapter);
                    drawerListAdapter.notifyDataSetChanged();
                    drawerListAdapter.setOnItemClickListener(UserViewChapterActivity.this);

                    loadChapterData.setVisibility(View.GONE);
                    chapterViewPager.setVisibility(View.VISIBLE);


                    setChapterViews(chapterViewPager.getCurrentItem());



                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        arrowBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        openNavImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.END);

            }
        });


        View includeView = findViewById(R.id.header);
        RelativeLayout writerLayout = includeView.findViewById(R.id.writerLayout);
        ConstraintLayout storyHeaderLayout = includeView.findViewById(R.id.storyHeaderLayout);
        final ImageView storyImgHeader = includeView.findViewById(R.id.storyImgHeader);
        final TextView storyNameTvHeader = includeView.findViewById(R.id.storyNameTvHeader);
        final ImageView writerImg = includeView.findViewById(R.id.writerImg);
        final TextView writerNameTvHeader = includeView.findViewById(R.id.writerNameTvHeader);

        storyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                StoryModel storyModel = snapshot.child("data").getValue(StoryModel.class);

                assert storyModel != null;
                Glide.with(getApplicationContext()).load(storyModel.getStoryImage())
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .into(storyImgHeader);
                storyNameTvHeader.setText(storyModel.getStoryName());

                writerId = storyModel.getStoryWriter();

                userRef = userRef.child(storyModel.getStoryWriter());

                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserModel userModel = snapshot.getValue(UserModel.class);

                        assert userModel != null;
                        if (userModel.getUserProfileImg() != null) {
                            Glide.with(getApplicationContext()).load(userModel.getUserProfileImg())
                                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                                    .into(writerImg);
                        }
                        writerNameTvHeader.setText(userModel.getUsername());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        storyHeaderLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserViewChapterActivity.this, UserViewStoryActivity.class);
                intent.putExtra("storyId", storyId);
                if (getIntent().getStringExtra("from")!=null &&
                        Objects.equals(getIntent().getStringExtra("from"), "library")){
                    intent.putExtra("from","library");
                }
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });


        writerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserViewChapterActivity.this, UserViewProfileActivity.class);
                intent.putExtra("userId", writerId);
                if (getIntent().getStringExtra("from")!=null &&
                        Objects.equals(getIntent().getStringExtra("from"), "library")){
                    intent.putExtra("from","library");
                }
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        fromBottom = AnimationUtils.loadAnimation(this, R.anim.from_bottom_anim);
        toBottom = AnimationUtils.loadAnimation(this, R.anim.to_bottom_anim);
        rotateOpen = AnimationUtils.loadAnimation(this, R.anim.rotate_open);
        rotateClose = AnimationUtils.loadAnimation(this, R.anim.rotate_close);

        showActionsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onAddBtnClick();
            }
        });

        voteActionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               if (MyApplication.isOnline(UserViewChapterActivity.this)){
                   Toast.makeText(mContext, getResources().getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
               }else {
                   if (chapterModelList.size() == 0) {
                       Toast.makeText(UserViewChapterActivity.this, getString(R.string.loading_dialog_text), Toast.LENGTH_SHORT).show();
                   } else {
                       setChapterVote(chapterViewPager.getCurrentItem());
                   }
               }

            }
        });

        commentActionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MyApplication.isOnline(UserViewChapterActivity.this)){
                    Toast.makeText(mContext, getResources().getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                }else {
                    if (chapterModelList.size() == 0) {
                        Toast.makeText(UserViewChapterActivity.this, getString(R.string.loading_dialog_text), Toast.LENGTH_SHORT).show();
                    } else {
                        chapterId = chapterModelList.get(chapterViewPager.getCurrentItem()).getChapterId();
                        Intent intent = new Intent(UserViewChapterActivity.this, ChapterCommentsActivity.class);
                        intent.putExtra("storyId", storyId);
                        intent.putExtra("chapterId", chapterId);
                        intent.putExtra("currPos", String.valueOf(chapterViewPager.getCurrentItem()));
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }
                }

            }
        });


        chapterViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                drawerListAdapter.notifyItemChanged(drawerListAdapter.selected_position);
                drawerListAdapter.selected_position = position;
                drawerListAdapter.notifyItemChanged(drawerListAdapter.selected_position);

            }

            @Override
            public void onPageSelected(int position) {
                setChapterViews(position);

                showInterstitial();



            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        if (getIntent().getStringExtra("from")!=null &&
                Objects.equals(getIntent().getStringExtra("from"), "library")){
            showPositionDialog();
            chapterRef.keepSynced(true);
            storyRef.keepSynced(true);
        }

    }



    private InterstitialAd newInterstitialAd() {
        InterstitialAd interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getString(R.string.interstitial_ad_unit_id));
        return interstitialAd;
    }

    private void showInterstitial() {
        if (interstitialAd != null && interstitialAd.isLoaded()) {
            interstitialAd.show();
        } else {
            goToNextChapter();
        }
    }

    private void loadInterstitial() {
        AdRequest adRequest = new AdRequest.Builder().build();
        interstitialAd.loadAd(adRequest);
    }

    private void goToNextChapter() {
        interstitialAd = newInterstitialAd();
        loadInterstitial();
    }





   private void showPositionDialog() {
       Integer defaultPos = 0;
           if (!sharedPref.loadPagerPosition(storyId).equals(defaultPos))
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(UserViewChapterActivity.this,  R.style.DialogTheme);
               String message = mContext.getResources().getString(R.string.restore_position_msg);
               builder.setMessage(message)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                           @RequiresApi(api = Build.VERSION_CODES.N)
                           @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                               chapterViewPager.setCurrentItem(sharedPref.loadPagerPosition(storyId));

                           }
                        })
                        .setNegativeButton(R.string.no, null);
               AlertDialog dialog = builder.create();
                dialog.show();
            }



   }


    private void onAddBtnClick() {
        setVisibility(clicked);
        setAnimation(clicked);
        clicked = !clicked;
    }

    private void setVisibility(Boolean clicked) {
        if (!clicked) {
            voteActionBtn.setVisibility(View.VISIBLE);
            commentActionBtn.setVisibility(View.VISIBLE);
        } else {
            voteActionBtn.setVisibility(View.INVISIBLE);
            commentActionBtn.setVisibility(View.INVISIBLE);
        }
    }

    private void setAnimation(Boolean clicked) {
        if (!clicked) {
            voteActionBtn.setAnimation(fromBottom);
            commentActionBtn.setAnimation(fromBottom);
            showActionsBtn.setAnimation(rotateOpen);
        } else {
            voteActionBtn.setAnimation(toBottom);
            commentActionBtn.setAnimation(toBottom);
            showActionsBtn.setAnimation(rotateClose);

        }
    }


    private void setChapterViews(int position) {
        final ChapterModel chapterModel = chapterModelList.get(position);
        viewRef = FirebaseDatabase.getInstance().getReference("Novels").child("Chapters")
                .child(chapterModel.getStoryId()).child(chapterModel.getChapterId()).child("views");
        userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        viewRef.child(userId).setValue(true);

    }

    private void setChapterVote(int position) {

        final MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.facebook);
        final MediaPlayer mediaPlayer2 = MediaPlayer.create(this, R.raw.facebook_pop);


        final ChapterModel chapterModel = chapterModelList.get(position);
       final DatabaseReference voteRef = FirebaseDatabase.getInstance().getReference("Novels").child("Chapters")
                .child(chapterModel.getStoryId()).child(chapterModel.getChapterId()).child("votes");

        userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        voteRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Snackbar snackbar;
                if (snapshot.hasChild(userId)) {
                    voteRef.child(userId).removeValue();
                    snackbar = Snackbar.make(coordinatorLayout, getResources().getString(R.string.remove_vote), Snackbar.LENGTH_SHORT);
                    mediaPlayer2.start();
                } else {
                    voteRef.child(userId).setValue(true);
                    snackbar = Snackbar.make(coordinatorLayout, getResources().getString(R.string.add_vote), Snackbar.LENGTH_SHORT);
                    mediaPlayer.start();
                }
                snackbar.show();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }


    private void addToLibrary(){
        libraryRef.child(userId).setValue(true);

    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
        }
        else {
            if (getIntent().getStringExtra("from")!=null &&
                    Objects.equals(getIntent().getStringExtra("from"), "library")){
                Intent intent = new Intent(UserViewChapterActivity.this, MainPageActivity.class);
                intent.putExtra("fragment", "library");
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

            }

            else {

                libraryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChild(userId)){
                            Intent intent = new Intent(UserViewChapterActivity.this, UserViewStoryActivity.class);
                            intent.putExtra("storyId", storyId);
                            startActivity(intent);
                            finish();
                            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

                        }else {
                            showSaveToLibDialog();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

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



    private void showSaveToLibDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(UserViewChapterActivity.this , R.style.DialogTheme);

        builder.setMessage(getApplicationContext().getResources().getString(R.string.add_to_library_message));

        builder.setPositiveButton(getApplicationContext().getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(final DialogInterface dialogInterface, int i) {
                addToLibrary();
                dialogInterface.dismiss();
                Intent intent = new Intent(UserViewChapterActivity.this, UserViewStoryActivity.class);
                intent.putExtra("storyId", storyId);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

            }
        });

        builder.setNegativeButton(getApplicationContext().getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                Intent intent = new Intent(UserViewChapterActivity.this, UserViewStoryActivity.class);
                intent.putExtra("storyId", storyId);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    @Override
    public void onItemClick(int position) {
        chapterViewPager.setCurrentItem(position);
        drawerLayout.closeDrawer(GravityCompat.END);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sharedPref.setPagerPosition(storyId , chapterViewPager.getCurrentItem());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sharedPref.setPagerPosition(storyId , chapterViewPager.getCurrentItem());
    }


    @Override
    protected void onStart() {
        super.onStart();
        checkLightMode();
        MyApplication.setLocale(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base , "ar"));
    }
}