package com.qi.shart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.ActionMenuItem;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public abstract class BaseActivity extends AppCompatActivity {
    private NavigationView nv;
    private DrawerLayout d1;
    private FirebaseStorage firestoreStorage = FirebaseStorage.getInstance();
    private FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();;
    private String docID, posterID;
    private Profile pf;
    private boolean isUserLoggedIn;
    private TabLayout tabLayout;
    private CircleImageView profpic;
    private ArrayList<challengeSlotDetail> slotDetails;
    private profileParticipation PP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceId());
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        //Log.d("MENU","IS ON PREPARE OPTIONS MENU CALLED??");
        //getMenuInflater().inflate(R.menu.topbar, menu);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.topbar, menu);
        return true;
    }

    protected abstract int getLayoutResourceId();

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(R.layout.activity_base);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.maintoolbar);
        /*myToolbar.inflateMenu(R.menu.topbar);

        final Menu menu = myToolbar.getMenu();
        myToolbar.setSubtitle("Testing");
        ActionMenuItemView item = findViewById(R.id.menu_item);
        item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onOptionsItemSelected(menu.findItem(R.id.menu_item));

        });*/
        setSupportActionBar(myToolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        d1 = findViewById(R.id.activity_main_drawer);
        d1.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        //ActionBarDrawerToggle t = new ActionBarDrawerToggle(this, d1,R.string.Open,"Close");
        //d1.addDrawerListener(t);
        isUserLoggedIn = PreferenceData.getUserLoggedInStatus(this);
        if (isUserLoggedIn) {
            pullProfile();
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            posterID = user.getUid();
        } else {
            posterID = "none";
        }
        pullChallengesDoing();

        nv = findViewById(R.id.nv);
        nv.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch(id)
                {
                    case R.id.action_EditProfile:
                        goToAddLinksNavDrawer();
                    case R.id.action_ChangeProfilePicture:
                        break;
                    case R.id.action_SignOut:
                        signOutNavDrawer();
                        Toast.makeText(BaseActivity.this, "Signed out",Toast.LENGTH_SHORT).show();break;
                    case R.id.action_DeleteAccount:
                        deleteAccount();
                        Toast.makeText(BaseActivity.this, "DELETED ACCOUNT TEST",Toast.LENGTH_SHORT).show();break;
                    default:
                        return true;
                }
                return true;
            }
        });

        //getLayoutInflater().inflate(layoutResID, (ViewGroup) findViewById(R.id.content));
        getLayoutInflater().inflate(layoutResID, (ViewGroup) findViewById(R.id.content));
    }

    private void signOutNavDrawer() {
        FirebaseAuth.getInstance().signOut();
        PreferenceData.clearLoggedInInfo(this);
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void goToAddLinksNavDrawer() {
        Intent intent = new Intent(this, AddSocialMediaActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Toast.makeText(this, "menu item pressed", Toast.LENGTH_SHORT).show();
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
            case R.id.action_EditProfile:
                d1.openDrawer(GravityCompat.START);
                return true;
        }
        return true;
    }

    private void pullProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        docID = user.getUid();
        DocumentReference docpath = firestoreDB.collection("profiles").document(docID);
        Log.d("TAG",docID);
        docpath.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Log.d("TAG","profile get successful...");
                    if (task.getResult()==null) Log.d("TAG","no results??");
                    pf = task.getResult().toObject(Profile.class);
                    Log.d("TAG","profile toObject successful..");

                    TextView nvUsername = nv.findViewById(R.id.navDrawer_username);
                    profpic = nv.findViewById(R.id.navDrawer_profPic);
                    nvUsername.setText(pf.getUsername());
                    retrieveFromFirebase();
                } else {
                    Log.d("TAG","ERROR GETTING PROFILE: ", task.getException());
                }
            }
        });
    }
    private void pullChallengesDoing() {
        DocumentReference docpath = firestoreDB.collection("profileParticipation").document(posterID);
        docpath.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    PP = task.getResult().toObject(profileParticipation.class);
                    pullSubmissions();

                    Log.d("TAG","profile toObject successful..");
                } else {
                    Log.d("TAG","ERROR GETTING PROFILE: ", task.getException());
                }
            }
        });
    }
    private void pullSubmissions() {
        slotDetails = new ArrayList<>();
        CollectionReference sortRef = firestoreDB.collection(posterID);
        Query query = sortRef.orderBy("ts", Query.Direction.DESCENDING);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        slotDetails.add(document.toObject(challengeSlotDetail.class));
                    }
                    tabSetup();
                } else {
                    Log.d("TAG", "ERROR GETTING DOCUMENTS: ", task.getException());
                }
            }
        });
    }

    //pulling profile picture
    private void retrieveFromFirebase() {
        long cacheKey = pf.getProfPicCache();
        String imagePath = "profilePictures/" + posterID;
        StorageReference storageRef = firestoreStorage.getReference();
        StorageReference imageRef = storageRef.child(imagePath);
        try {
            Glide.with(this)
                    .load(imageRef)
                    .signature(new ObjectKey(String.valueOf(cacheKey)))
                    .error(
                            Glide.with(this)
                                    .load(R.mipmap.ic_launcher_round)
                    )
                    .into(profpic);
        } catch (Exception e) {
            profpic.setImageResource(R.mipmap.ic_launcher_round);
            Log.e("TAG", "IMG PULL FAILED", e);
        }
    }
    public void switchContent(int id, Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(id, fragment)
                .addToBackStack(null)
                .commit();

    }

    public void goToMyProfile() {
        tabLayout.selectTab(tabLayout.getTabAt(2));
    }

    private void tabSetup() {
        final CustomViewPager mainpager = findViewById(R.id.mainpager);
        tabLayout = findViewById(R.id.tabs);

        /*tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                Log.d("viewpageAdapter","TAB RESELECTED " + tab.getPosition());
                mainpager.setPagingEnabled(true);
                switch (tab.getPosition()) {
                    case 0:
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.mainFragment_layout, new mainfragment())
                                .commit();
                        break;
                    case 1:
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_myChecklist_layout, profileMyChallenges_fragment.newInstance(PP, slotDetails))
                                .commit();
                        break;
                    case 2:
                        getSupportFragmentManager().beginTransaction()
                            .replace(R.id.profileFragment_layout, profilefragment.getInstance(posterID))
                            .commit();
                        break;
                }

            }
        });*/
        mainpager.setAdapter(new ViewPageAdapter(getSupportFragmentManager(),this, tabLayout, posterID,PP,slotDetails));
        tabLayout.setupWithViewPager(mainpager);
        for (int i = 0; i < tabLayout.getTabCount(); i++ ) {
            switch (i) {
                case 0:
                    tabLayout.getTabAt(i).setIcon(R.drawable.mypicstemp);
                    break;
                case 1:
                    tabLayout.getTabAt(i).setIcon(R.drawable.discovertemp);
                    break;
                case 2:
                    tabLayout.getTabAt(i).setIcon(R.drawable.persontemp);
                    break;
            }
        }
    }
    private void deleteAccount() {
        //mark account as inactive
        //set profile/meta(index) to false

        //signOutNavDrawer();
        final String UID = docID;
        final LinkedList<String> tempStore = new LinkedList<>();
        //setting us to inactive in challengeParticipation + update counter in challenges collection
        firestoreDB.collection("challengeParticipation")
                .whereArrayContains("activeParticipants",UID).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        String chID = doc.getId();
                        tempStore.add(chID);
                        challengeParticipation CP = doc.toObject(challengeParticipation.class);

                        int index = CP.getActiveParticipants().indexOf(UID);
                        Log.d("TAG","pos in activeParticipants is " + String.valueOf(index));
                        CP.setInactive(index);
                        CP.decrementCurrParticipants();

                        firestoreDB.collection("challengeParticipation").document(chID).set(CP);
                    }
                    for (String chID : tempStore) {
                        firestoreDB.collection("challenges").document(chID).update("activeParticipants", FieldValue.increment(-1));
                    }
                } else {
                    Log.d("TAG", "ERROR DELETING ACCOUNT: ", task.getException());
                }
            }
        });

        //set profiles/meta entry to inactive
        firestoreDB.collection("profiles").document("meta").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot ds = task.getResult();
                    HashMap<String, Integer> meta = (HashMap<String,Integer>) ds.get("accountActive");
                    meta.put(docID, 0);

                    firestoreDB.collection("profiles").document("meta").update("accountActive",meta);
                }
            }
        });


    }
}