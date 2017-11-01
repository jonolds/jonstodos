package com.jonolds.jonstodos;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TimePicker;
import static android.R.style.Theme_DeviceDefault_Dialog_NoActionBar_MinWidth;

public class PickTime extends DialogFragment implements TimePickerDialog.OnTimeSetListener{
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //Set default time to now
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Return TimePickerDialog
        return new TimePickerDialog(getActivity(), Theme_DeviceDefault_Dialog_NoActionBar_MinWidth, this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int i, int i1) {
        ((DetailsActivity)getActivity()).setTime(i, i1);
    }
}
