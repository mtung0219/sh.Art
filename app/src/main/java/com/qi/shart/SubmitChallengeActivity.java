package com.qi.shart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import androidx.fragment.app.DialogFragment;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class SubmitChallengeActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private DatabaseReference mDatabase;
    private FirebaseFirestore firestoreDB;
    private final int PICK_IMAGE_REQUEST = 71;
    private final int EXTERNAL_STORAGE_REQUEST = 70;
    private Uri filePath;
    private Uri destination;
    private ImageView imageView;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private StorageReference ref;
    private String UUIDValue;
    private byte[] BAtest;
    private Boolean hasPic = false;
    private LinearLayout submitLayout;
    private ArrayAdapter<String> platformAdapter, typeAdapter;
    private Button platformButton, typeButton;
    private String[] platformItems, typeItems;
    private String platform="", type="";
    private Date startDateSubmit, endDateSubmit;
    private Switch dateSwitch;
    private TextView challengeText, hashtagText, numEntriesText, descText;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String UID;

    public void chooseImage(View view) {
        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 70);
    }

    public void startImageSelection() {
        Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        //intent.setType("image/*");
        //intent.setAction(Intent.ACTION_PICK);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"),PICK_IMAGE_REQUEST);
    }

    public void uploadImage(View view) {
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        if (destination != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            ref = storageReference.child("images/" + UUIDValue);
            UploadTask ulTask = ref.putBytes(BAtest);
            ulTask
            //ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(SubmitChallengeActivity.this, "UPLOAD SUCCESSFUL!",Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(SubmitChallengeActivity.this, "FAILED UPLOAD!",Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        imageView = findViewById(R.id.imgDisplay);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
            && data != null && data.getData() != null) {
            filePath = data.getData();
            submitLayout = findViewById(R.id.submitchallengeLayout);
            int layoutWidth = submitLayout.getWidth();
            destination = Uri.fromFile(new File(this.getCacheDir(),"IMG_" + System.currentTimeMillis()));
            UCrop.of(filePath, destination)
                    .withAspectRatio(1,1)
                    .withMaxResultSize(layoutWidth,layoutWidth)
                    .start(this);

        } else if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            final Uri resultUri = UCrop.getOutput(data);
            //imageView.setImageURI(resultUri);

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), destination);

                ByteArrayOutputStream baout = new ByteArrayOutputStream();

                //resize now handled by UCrop
                //Bitmap resized = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth()*0.5), (int) (bitmap.getHeight()*0.5), true);

                bitmap.compress(Bitmap.CompressFormat.WEBP,25,baout);

                //imageView.setImageURI(destination);
                imageView.setImageURI(destination);
                Log.d("TAG","should have appeared?");
                //imageView.setImageBitmap(bitmap);
                BAtest = baout.toByteArray();
                baout.close();
                hasPic = true;
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public File getFilename() throws IOException {
        String root = Environment.getExternalStorageDirectory().toString();
        File file = new File(root + "/MyFolder/Images");
        file.mkdirs();
        String uriString = System.currentTimeMillis() + ".jpeg";
        File imgFile = new File(file, uriString);
        if (imgFile.exists()) imgFile.delete();
        imgFile.createNewFile();

        return imgFile;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.submitchallenge);

        challengeText = findViewById(R.id.challengename_text);
        hashtagText = findViewById(R.id.hashtag_text);
        numEntriesText = findViewById(R.id.numEntries_text);
        descText = findViewById(R.id.desc_text);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        firestoreDB = FirebaseFirestore.getInstance();

        //setting up spinners------------------------------------------------------------
        platformButton = findViewById(R.id.platform_spinnerButton);
        typeButton = findViewById(R.id.type_spinner_Button);
        typeButton.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String stringSequence = "" + charSequence;
                if (stringSequence.equals("Series")) {
                    findViewById(R.id.numEntries_text).setVisibility(View.VISIBLE);
                    findViewById(R.id.numEntries_label).setVisibility(View.VISIBLE);
                    findViewById(R.id.asterisk5).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.numEntries_text).setVisibility(View.INVISIBLE);
                    findViewById(R.id.numEntries_label).setVisibility(View.INVISIBLE);
                    findViewById(R.id.asterisk5).setVisibility(View.INVISIBLE);
                }

            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });

        platformItems = new String[]{"Instagram","Amiibo","deviantArt","Tumblr","Pinterest","Facebook","Here","Other"};
        typeItems = new String[]{"Series","DTIYS","Other"};

        platformAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, platformItems);
        typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, typeItems);

        // Specify the layout to use when the list of choices appears
        platformAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        dateSwitch = findViewById(R.id.dateswitch);
        dateSwitch.setChecked(false);
        dateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) { //date sensitive switch
                if (b) {
                    findViewById(R.id.datesShow).setVisibility(View.VISIBLE);
                    findViewById(R.id.date_time_set).setVisibility(View.VISIBLE);
                    findViewById(R.id.date_time_set).setClickable(true);
                    findViewById(R.id.asterisk3).setVisibility(View.VISIBLE);
                } else {
                    startDateSubmit=null;endDateSubmit=null;
                    TextView datesShow = findViewById(R.id.datesShow);
                    datesShow.setText("");
                    datesShow.setVisibility(View.INVISIBLE);
                    findViewById(R.id.date_time_set).setVisibility(View.INVISIBLE);
                    findViewById(R.id.date_time_set).setClickable(false);
                    findViewById(R.id.asterisk3).setVisibility(View.INVISIBLE);
                }
            }
        });

        //spinner end----------------------------------------------------------------------
    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public void showRangePickerDialog(View view) {

        final DateFormat dateformatter = new SimpleDateFormat("MM/dd/yyyy"); //for viewing
        final DateFormat dateformatterSubmit = new SimpleDateFormat("yyyy-MM-dd"); //for querying purposes

        MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
        Date d = new Date();
        Long now = d.getTime();
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        final long startDate = cal.getTimeInMillis();
        cal.add(Calendar.MONTH, 3); //can only post challenges up to 3 months into the future
        long endDate = cal.getTimeInMillis();
        constraintsBuilder.setStart(startDate);
        constraintsBuilder.setEnd(endDate);
        builder.setCalendarConstraints(constraintsBuilder.build());
        builder.setTitleText("Pick start and end date.");
        MaterialDatePicker<Pair<Long, Long>> picker = builder.build();
        picker.show(getSupportFragmentManager(),picker.toString());

        picker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Pair<Long, Long>>() {
            @Override
            public void onPositiveButtonClick(Pair<Long, Long> selection) {
                if (selection.first==null || selection.second==null) {
                    Toast.makeText(SubmitChallengeActivity.this, "Pick both a start and end date!",Toast.LENGTH_LONG).show();
                    return;
                }
                Long sL = selection.first;
                Date sD = new Date(sL);
                Long eL = selection.second;
                Date eD = new Date(eL);
                //startDateSubmit = dateformatterSubmit.format(sD);
                //endDateSubmit = dateformatterSubmit.format(eD);
                startDateSubmit = sD;
                endDateSubmit = eD;
                String dateText = dateformatter.format(sD) + " - " + dateformatter.format(eD);
                ((TextView) findViewById(R.id.datesShow)).setText(dateText);
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        Toast.makeText(adapterView.getContext(),adapterView.getSelectedItem().toString(),Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    public void writeChallenge(View view) {
        Log.d("mainactivity", "STARTING DATA UPLOAD");

        if (!missingInfoValidation()) {
            createAlertDialogForMissingInfo();
            return;
        }

        //private TextView challengeText, hashtagText, numEntriesText, descText;
        int numEntriesInt =1;
        String title= challengeText.getText().toString();
        String hashtag=hashtagText.getText().toString();
        String numEntries=numEntriesText.getText().toString();
        if (!numEntries.equals("")) {
            numEntriesInt = Integer.parseInt(numEntries);
        }
        //String startDate=( (Button) findViewById(R.id.date_time_set)).getText().toString();
        String desc=descText.getText().toString();

        Date ts = new Date();
        //get the current user ID
        UID = user.getUid();
        Challenge challenge = new Challenge(UID,title,platform,hashtag,numEntriesInt,
                startDateSubmit,endDateSubmit, desc,ts, hasPic, UID,0L,0L, "", type);
        mDatabase.child("challenges").child("1").setValue(challenge);

        ArrayList<String> emptyList = new ArrayList<>();
        ArrayList<Boolean> emptyListBool = new ArrayList<>();
        challengeParticipation challengeP = new challengeParticipation(0, emptyList,emptyListBool);

        UUIDValue = UUID.randomUUID().toString();
        //-------------------------------------------------firestore---------------------------------------------
        firestoreDB.collection("challenges").document(UUIDValue).set(challenge);

        //set challengeParticipation document
        firestoreDB.collection("challengeParticipation").document(UUIDValue).set(challengeP);
        uploadImage(view);

    }

    public void platformButtonClick(View view) {
        new AlertDialog.Builder(this).setTitle("Select a Platform")
                .setAdapter(platformAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        platformButton.setText(platformItems[i]);
                        platform = platformItems[i];
                        dialogInterface.dismiss();
                    }
                }).create().show();
    }

    public void typeButtonClick(View view) {
        new AlertDialog.Builder(this).setTitle("Choose Challenge Type")
                .setAdapter(typeAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        typeButton.setText(typeItems[i]);
                        type = typeItems[i];
                        dialogInterface.dismiss();
                    }
                }).create().show();
    }

    public boolean missingInfoValidation() {
        boolean titleCheck = !challengeText.getText().toString().equals("");
        boolean platformCheck = !platform.equals("");
        boolean numEntriesCheck = !numEntriesText.getText().toString().equals("");
        boolean descCheck = !descText.getText().toString().equals("");
        boolean chTypeCheck = !type.equals("");

        //if not series type, num Entries is not required
        if (chTypeCheck &&! type.equals("Series")) numEntriesCheck = true;

        //requires title, platform, description, challenge Type, num Entries if series.
        return titleCheck && platformCheck && descCheck && chTypeCheck && numEntriesCheck
                && dateSwitch.isChecked();
    }

    public void createAlertDialogForMissingInfo() {
        /*DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    case DialogInterface.BUTTON_POSITIVE:
                        //writeSlot();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:

                        break;
                }
            }
        };*/
        AlertDialog.Builder areYouSure = new AlertDialog.Builder(this);
        areYouSure.setMessage("Please fill in missing info to continue.").show();
                //.setPositiveButton("Yes", dialogClickListener)
                //.setNegativeButton("No", dialogClickListener).show();
    }
}