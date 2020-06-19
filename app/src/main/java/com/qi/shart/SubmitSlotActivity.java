package com.qi.shart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class SubmitSlotActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private FirebaseFirestore firestoreDB;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private Uri filePath;
    private Uri destination;
    private StorageReference ref;
    private final int PICK_IMAGE_REQUEST = 71;
    private final int EXTERNAL_STORAGE_REQUEST = 70;
    private RelativeLayout submitLayout;
    private ImageView imageView;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String UID, chName, UUIDValue;
    private boolean isLoggedIn;
    private boolean doesSubmissionExist = false;
    private int numEntries;
    private int numPos;
    private byte[] BAtest;
    private int totalChallengeNum;
    private Long mCacheKey;

    private TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.submitslot);
        firestoreDB = FirebaseFirestore.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        firestoreDB = FirebaseFirestore.getInstance();
        UUIDValue = getIntent().getStringExtra("chID");
        Log.d("TAG", "the UUIDValue here is " + UUIDValue);
        chName = getIntent().getStringExtra("chName");
        numEntries = getIntent().getIntExtra("numEntries",777);
        numPos = getIntent().getIntExtra("dayNum",999);
        isLoggedIn = PreferenceData.getUserLoggedInStatus(this);
        UID = user.getUid();

        title = findViewById(R.id.submitslot_title);
        Log.d("TAG", "the position here is " + numPos);
        title.setText("Upload your entry for day " + numPos + ":");

    }

    public void chooseImage(View view) {
        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 70);
    }

    public void checkIfSubmissionExists(View view) {
        DocumentReference dR = firestoreDB.collection(UID).document(UUIDValue + "_" + numPos);
        dR.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    mCacheKey = 0L;
                    if (document.exists()) {
                        Log.d("TAG", "Document exists!");
                        doesSubmissionExist = true;
                        mCacheKey = (Long) document.get("cacheKey");
                        createAlertDialogForOverwrite();
                    } else {
                        doesSubmissionExist = false;
                        Log.d("TAG", "Document does not exist!");
                        writeSlot();
                    }
                } else {
                    Log.d("TAG", "Failed checkIfSubmissionExists with: ", task.getException());
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case EXTERNAL_STORAGE_REQUEST: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("TAG","PERMISSION GRANTED");
                    startImageSelection();
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.d("TAG","PERMISSION NOT GRANTED");
                    Toast.makeText(this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void startImageSelection() {
        Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        //intent.setType("image/*");
        //intent.setAction(Intent.ACTION_PICK);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"),PICK_IMAGE_REQUEST);
    }

    public void createAlertDialogForOverwrite() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    case DialogInterface.BUTTON_POSITIVE:
                        writeSlot();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:

                        break;
                }
            }
        };
        AlertDialog.Builder areYouSure = new AlertDialog.Builder(this);
        areYouSure.setMessage("You have already submitted an entry for this day. This will overwrite your previous submission. Continue?")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }
    public void writeSlot() {
        //image upload variables
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        if (doesSubmissionExist) {
            mCacheKey+=1;
        }

        //data upload variables
        String desc= ( (TextView) findViewById(R.id.submitslot_desc_text)).getText().toString();
        Date ts = new Date();
        String posterID= UID;
        String username = PreferenceData.getLoggedInUsername(this);
        String imageURI = UUIDValue;
        ArrayList<String> likes = new ArrayList<>();
        likes.add("placeholder");
        // for new challenges
                challengeSlotDetail challengeSlotDetail =
                new challengeSlotDetail(desc, imageURI, posterID, ts, username,
                        numPos,0,likes,"","", chName, mCacheKey, numEntries);

        firestoreDB.collection("challengeSlotURIs").document(UUIDValue).collection(String.valueOf(numPos))
        .document(UUIDValue + "_" + String.valueOf(numPos) + "_" + posterID).set(challengeSlotDetail);

        firestoreDB.collection(posterID).document(UUIDValue + "_" + String.valueOf(numPos)).set(challengeSlotDetail);
        firestoreDB.collection("allSubmissions").document(
                UUIDValue + "_" + String.valueOf(numPos) + "_" + posterID).set(challengeSlotDetail);

        firestoreDB.collection("challenges").document(UUIDValue).update("numSubmissions", FieldValue.increment(1));

        uploadImage_slot();
    }


    public void uploadImage_slot() {

        if (destination != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            ref = storageReference.child(UUIDValue + "/" + UUIDValue + "_" + numPos + "_" + UID);
            UploadTask ulTask = ref.putBytes(BAtest);
            ulTask
                    //ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(SubmitSlotActivity.this, "UPLOAD SUCCESSFUL!",Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(SubmitSlotActivity.this, "FAILED UPLOAD!",Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        imageView = findViewById(R.id.submitslot_imgDisplay);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            filePath = data.getData();
            submitLayout = findViewById(R.id.submitslotLayout);
            int layoutWidth = submitLayout.getWidth();
            destination = Uri.fromFile(new File(this.getCacheDir(),"IMG_" + System.currentTimeMillis()));
            UCrop.of(filePath, destination)
                    .withAspectRatio(1,1)
                    .withMaxResultSize(layoutWidth,layoutWidth)
                    .start(this);

        } else if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            final Uri resultUri = UCrop.getOutput(data);
            imageView.setImageURI(resultUri);

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), destination);

                ByteArrayOutputStream baout = new ByteArrayOutputStream();

                //resize now handled by UCrop
                //Bitmap resized = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth()*0.5), (int) (bitmap.getHeight()*0.5), true);

                bitmap.compress(Bitmap.CompressFormat.WEBP,25,baout);

                imageView.setImageURI(destination);

                Log.d("TAG","should have appeared?");
                //imageView.setImageBitmap(bitmap);
                BAtest = baout.toByteArray();
                baout.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
