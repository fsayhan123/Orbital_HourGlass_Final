package com.example.weekcalendar.helperclasses;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.weekcalendar.R;
import com.example.weekcalendar.activities.ActivityCreateSharedEvent;
import com.example.weekcalendar.activities.ActivityEventDetailsPage;

public class DialogCreationEvent extends AppCompatDialogFragment {
    private ActivityCreateSharedEvent a;
    private String docRefID;
    public EditText allEmails;

    public DialogCreationEvent(String docRefID, ActivityCreateSharedEvent activity) {
        this.docRefID = docRefID;
        this.a = activity;
    }
    @NonNull
    @Override
    public android.app.Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_invite, null);
        this.allEmails = view.findViewById(R.id.edit_invite);
        builder.setView(view)
                .setTitle("Invite Others")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String[] usersToInvite = getEmails();
                        String userEmail = "alicia@gmail.com";
                        a.reportUID(docRefID, userEmail);
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
