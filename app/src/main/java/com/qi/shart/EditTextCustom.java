package com.qi.shart;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class EditTextCustom extends androidx.appcompat.widget.AppCompatEditText {

    public EditTextCustom(Context context) {
        super(context);
    }

    public EditTextCustom(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public EditTextCustom(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            String username = getText().toString();
            if (!username.matches("")) {
                checkUsernameAvailable(username);
            }

        }
        return super.dispatchKeyEvent(event);
    }

    public void checkUsernameAvailable(final String username) {
        FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
        Query q = firestoreDB.collection("profiles").whereEqualTo("usernameLower",username.toLowerCase());
        Activity a = (Activity) getContext();
        final TextView usernameAvailable = a.findViewById(R.id.usernameAvailable);

        final String myUsername = PreferenceData.getLoggedInUsername(getContext()).toLowerCase();
        q.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    if (document.getString("usernameLower").equals(myUsername)) {
                        //your own username
                        usernameAvailable.setText("Username is available.");
                        usernameAvailable.setTextColor(Color.parseColor("#228B22")); //dark green
                        return;
                    }
                    usernameAvailable.setText("Username is unavailable. Please choose another.");
                    usernameAvailable.setTextColor(Color.parseColor("#8B0000")); //dark red
                    return;
                }
                usernameAvailable.setText("Username is available.");
                usernameAvailable.setTextColor(Color.parseColor("#228B22")); //dark green
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "ERROR!",Toast.LENGTH_LONG).show();
            }
        });
    }

}