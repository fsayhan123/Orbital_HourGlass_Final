package com.example.weekcalendar.helperclasses;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.weekcalendar.R;
import com.example.weekcalendar.activities.ActivityCreateSharedEvent;

import java.util.Map;
import java.util.Objects;

public class DialogCreationEvent extends AppCompatDialogFragment {
    private ActivityCreateSharedEvent a;
    private Map<String, Object> data;
    private EditText allEmails;

    public DialogCreationEvent(Map<String, Object> data, ActivityCreateSharedEvent activity) {
        this.data = data;
        this.a = activity;
    }
    @NonNull
    @Override
    public android.app.Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_invite, null);
        this.allEmails = view.findViewById(R.id.edit_invite);
        builder.setView(view)
                .setTitle("Invite Others")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        a.c.add(data)
                                .addOnSuccessListener(docRef -> {
                                    Log.d(ActivityCreateSharedEvent.class.getSimpleName(), "DocumentSnapshot successfully written!");
                                    String[] usersToInvite = getEmails();
                                    for (String email : usersToInvite) {
                                        a.sendNotification(docRef.getId(), email.replaceAll("\\s",""));
                                    }
                                })
                                .addOnFailureListener(e -> Log.w(ActivityCreateSharedEvent.class.getSimpleName(), "Error writing document", e));
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        return builder.create();
    }

    public String[] getEmails() {
        return this.allEmails.getText().toString().split(",");
    }
}
