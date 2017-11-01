package com.jonolds.jonstodos;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;
import static android.R.style.Theme_DeviceDefault_Dialog_NoActionBar_MinWidth;

public class PickDate extends DialogFragment implements DatePickerDialog.OnDateSetListener{
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //set to default date to now
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int monthOfYear = c.get(Calendar.MONTH);
        int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);

        //Return DatePickerDialog
        return new DatePickerDialog(getActivity(), Theme_DeviceDefault_Dialog_NoActionBar_MinWidth, this, year, monthOfYear,
                dayOfMonth);
    }

    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
        //Automatically load timePicker after datePicker
        (new PickTime()).show(getActivity().getSupportFragmentManager(), "timePicker");
        ((DetailsActivity)getActivity()).setDate(i, i1+1, i2);
    }
}
