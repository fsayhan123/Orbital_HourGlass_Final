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

import java.text.SimpleDateFormat;

public class MyDateDialog extends AppCompatDialogFragment {
    private MyDateDialogEventListener listener;
    private Button b;
    private static SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMM yyyy");

    public MyDateDialog(Button b) {
        this.b = b;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (MyDateDialogEventListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement MyDateDialogEventueListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View scrolling = inflater.inflate(R.layout.select_date_dialog, null);
        SingleDateAndTimePicker s = scrolling.findViewById(R.id.date_selector_day);

        builder.setView(scrolling)
                .setTitle("Select Date")
                .setPositiveButton("Submit", (dialog, which) -> {
                    CustomDay selectedCustomDay = new CustomDay(s.getDate());
                    Toast.makeText(getContext(),"Selected " + selectedCustomDay.getFullDateForView(), Toast.LENGTH_SHORT).show();
                    listener.applyDateText(selectedCustomDay, b);
                })
                .setNegativeButton("Cancel", (dialog, which) -> { });
        return builder.create();
    }

    public interface MyDateDialogEventListener {

        /**
         * Changes text on the select date button to reflect user's choice.
         * @param d CustomDay user chose
         * @param b button to set text on
         */
        void applyDateText(CustomDay d, Button b);
    }
}