package com.example.weekcalendar.helperclasses;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.weekcalendar.R;
import com.example.weekcalendar.customclasses.CustomDay;
import com.github.florent37.singledateandtimepicker.SingleDateAndTimePicker;

import java.util.Objects;

public class MyTimeDialog extends AppCompatDialogFragment {
    private MyTimeDialogListener listener;
    private Button b;

    public MyTimeDialog(Button b) {
        this.b = b;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (MyTimeDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement MyDateDialogueListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
        View scrolling = inflater.inflate(R.layout.select_time_dialog, null);
        SingleDateAndTimePicker s = scrolling.findViewById(R.id.date_selector_time);

        builder.setView(scrolling)
                .setTitle("Select Time")
                .setPositiveButton("Submit", (dialog, which) -> {
                    CustomDay selectedCustomDay = new CustomDay(s.getDate());
                    Toast.makeText(getContext(),"Selected " + selectedCustomDay.getTime(), Toast.LENGTH_SHORT).show();
                    listener.applyTimeText(selectedCustomDay, b);
                })
                .setNegativeButton("Cancel", (dialog, which) -> { });
        return builder.create();
    }

    public interface MyTimeDialogListener {
        void applyTimeText(CustomDay d, Button b);
    }
}