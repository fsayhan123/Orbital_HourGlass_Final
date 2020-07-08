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

public class Dialog extends AppCompatDialogFragment {
    private String url;
    private Activity a;
    public EditText email;
    public boolean status = false;


    public Dialog(String url) {
        this.url = url;
    }
    @NonNull
    @Override
    public android.app.Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        this.a = getActivity();
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_invite, null);
        email = view.findViewById(R.id.edit_invite);
        builder.setView(view)
                .setTitle("Invite Others")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Dialog.this.status = true;
                    }
                })
                .setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        return builder.create();
    }

    public boolean getStatus() {
        return this.status;
    }

    public String getEmail() {
        return this.email.toString();
    }
}
