package com.jonolds.jonstodos;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.FileOutputStream;
import static android.app.AlarmManager.RTC_WAKEUP;

public class DetailsActivity extends AppCompatActivity {
    ConnectivityManager cm;
    DetailsActivity.MyBroadCastReceiver myBR;
    NetworkInfo activeNet;
    int position;
    int id;
    Calendar calendar;
    String[] projection = {
            ToDoProvider.TODO_TABLE_COL_ID,
            ToDoProvider.TODO_TABLE_COL_TITLE,
            ToDoProvider.TODO_TABLE_COL_CONTENT,
            ToDoProvider.TODO_TABLE_COL_DONE,
            ToDoProvider.TODO_TABLE_COL_DATE,
            ToDoProvider.TODO_TABLE_COL_TIME};
    ContentValues cv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
    }
    protected void onStart() {
        super.onStart();
        connection();
        position = (int) getIntent().getSerializableExtra("position");
        Log.e("onStart pos ", String.valueOf(position));
        if(position != -1)
            new Asyn().execute(String.valueOf(1), String.valueOf(position));
        else {
            Button button = (Button) findViewById(R.id.delete);
            button.setText(R.string.clear);
        }
    }

    public void scheduleNotification(Cursor cursor){

        long alarmTime = calendar.getTimeInMillis();
        AlarmManager alarmMgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NotificationCreator.class);
        if (cursor != null) {
            cursor.moveToLast();
            String idTitleDesc = String.valueOf(cursor.getInt(0) + "___" + cursor.getString(1) + "___" + cursor.getString(2));
            intent.putExtra("idTitleDesc", idTitleDesc);
            PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
            //alarmMgr.setExact(AlarmManager.ELAPSED_REALTIME, 6000, alarmIntent);
            alarmMgr.setExact(RTC_WAKEUP, alarmTime, alarmIntent);
            cursor.close();
        }
    }

    //Aysnchronous class to run database operations
    private class Asyn extends AsyncTask<String, Void, Cursor> {
        int operation;
        int didWork;

        protected Cursor doInBackground(String... str) {
            operation = Integer.parseInt(str[0]);
            if ((operation == 1)||(operation == 2)||(operation == 4)) {
                Log.e("Pre ", "1, 2, 5");
                return getContentResolver().query(ToDoProvider.CONTENT_URI, projection, null, null, "_ID ASC");
            }
            else if (operation == 3) {
                Log.e("Pre ", "3");
                //Delete the note
                didWork = getContentResolver().delete(Uri.parse(ToDoProvider.CONTENT_URI + "/" + (id)),null,null);
                return getContentResolver().query(ToDoProvider.CONTENT_URI, projection, null, null, "_ID ASC");
            }
            else if (operation == 5) {
                Log.e("Pre ", "5");
                getContentResolver().insert(ToDoProvider.CONTENT_URI, cv);
                return getContentResolver().query(ToDoProvider.CONTENT_URI, projection, null, null, "_ID ASC");
            }
            else if (operation == 6) {
                Log.e("Pre ", "6");
                return getContentResolver().query(ToDoProvider.CONTENT_URI, projection, null, null, "_ID ASC");
            }
            else if (operation == 7) {
                Log.e("Pre ", "7");
                Log.e("id ", String.valueOf(id));
                didWork = getContentResolver().update(Uri.parse(ToDoProvider.CONTENT_URI + "/" + (id)), cv, null, null);
                return getContentResolver().query(ToDoProvider.CONTENT_URI, projection, null, null, "_ID ASC");
            }
            else
                return getContentResolver().query(ToDoProvider.CONTENT_URI, projection, null, null, "_ID ASC");//***WRONG
        }
        //Called after background tasks are done
        protected void onPostExecute(Cursor cursor) {
            if(operation == 1) {
                Log.e("Post ", "1");
                loadTaskInfo(cursor, position);
            }
            else if (operation == 2) {
                Log.e("Post ", "2");
                deleteItem(cursor, position);
            }
            else if (operation == 3) {
                Log.e("Post ", "3");
                //If it didWork, then create a Toast Message saying that the note was deleted
                Toast.makeText(getApplicationContext(),((didWork == 1) ? "Note Deleted" : "Note NOT****** Deleted"), Toast.LENGTH_SHORT).show();
                cursor.close();
                back();
            }
            else if (operation == 4) {
                Log.e("Post ", "4");
                scheduleNotification(cursor);
            }
            else if (operation == 5) {
                Log.e("Post ", "5");
                saveNewGotCursor(cursor);
            }
            else if (operation == 6) {
                Log.e("Post ", "6");
                updateTask(cursor);
            }
            else if (operation ==7) {
                Log.e("Post ", "7");
                updateTaskCallback(cursor, didWork);
            }
            else {

            }
        }
    }

    public void loadTaskInfo(Cursor cursor, int position) {
        if (cursor != null) {
            cursor.moveToPosition(position);
            id = cursor.getInt(0);
            setName(cursor.getString(1));
            setDesc(cursor.getString(2));
            setDone(cursor.getInt(3));
            setDateBox(cursor.getString(4));
            setTimeBox(cursor.getString(5));
            cursor.close();
        }
    }

    //clear all input spaces if new task, delete if existing task
    public void delSplitter(View view){
        if(position == -1) {
            setName("");
            setDesc("");
            setDone(0);
            setTimeBox("");
            setDateBox("");
        }
        else
            new Asyn().execute(String.valueOf(2), String.valueOf(position));
    }
    public void deleteItem(Cursor cursor, int position){
        if (cursor != null) {
            cursor.moveToPosition(position);//Move the cursor to the beginning
            if((activeNet == null) || (!activeNet.isConnectedOrConnecting())) {
                saveToFile("DEL*" + cursor.getInt(0) + "***");
            }
            int itemToDel = cursor.getInt(0);//Get the ID (int) of the newest note (column 0)
            //Make a toast message stating the note to be deleted
            Toast.makeText(getApplicationContext(), "Deleting note named: "  + Integer.toString(itemToDel), Toast.LENGTH_SHORT).show();
            new Asyn().execute(String.valueOf(3));

        }
    }

    public void getScheduleNotificationCursor() {
        new Asyn().execute(String.valueOf(4));
    }

    //Save/Update Block
    public void saveNewTask(View view) {
        if(!getName().isEmpty() && !getDesc().isEmpty()) {
            cv = new ContentValues();
            cv.put(ToDoProvider.TODO_TABLE_COL_TITLE, getName());
            cv.put(ToDoProvider.TODO_TABLE_COL_CONTENT, getDesc());
            cv.put(ToDoProvider.TODO_TABLE_COL_DONE, getDone());
            cv.put(ToDoProvider.TODO_TABLE_COL_DATE, getDateBox());
            cv.put(ToDoProvider.TODO_TABLE_COL_TIME, getTimeBox());

            //If position is -1 the task is new. Delete button is repurposed to a clear button and save adds a new task
            if(position == -1)
                new Asyn().execute(String.valueOf(5));
                //If position is not -1 the task already exists and can be updated or deleted.
            else
                new Asyn().execute(String.valueOf(6));
            back();
        }
    }
    public void updateTask(Cursor cursor){
        if (cursor != null) {
            cursor.moveToPosition(position);
            //check connection and also save updated data to file if disconnected
            if ((activeNet == null) || (!activeNet.isConnectedOrConnecting())) {
                saveToFile("UPD*" + String.valueOf(cursor.getInt(0)) + "*" + getName() + "*" +
                        getDesc() + "*" + String.valueOf(getDone()) + "*" + getDateBox() + "*" + getTimeBox() + "***");
            }
            Toast.makeText(getApplicationContext(), "Updating note named: " + Integer.toString(id), Toast.LENGTH_SHORT).show();

            new Asyn().execute(String.valueOf(7));
        }
    }

    public void updateTaskCallback(Cursor cursor, int didWork) {
        Toast.makeText(getApplicationContext(), ((didWork == 1) ? "Note Updated" : "Note NOT****** Updated"), Toast.LENGTH_SHORT).show();
        cursor.close();
    }
    public void saveNewGotCursor(Cursor cursor) {
        //Check connection and save data to file as well as database if disconnected
        if((activeNet == null) || (!activeNet.isConnectedOrConnecting()))
            saveToFile("NEW*" + getName() + "*" + getDesc() + "*" + String.valueOf(getDone()) + getDateBox() + "*" + getTimeBox() + "***");
        if((!getDateBox().isEmpty()) && (!getTimeBox().isEmpty()))
            getScheduleNotificationCursor();
        if (cursor != null) {
            Toast.makeText(getApplicationContext(), Integer.toString(cursor.getCount()), Toast.LENGTH_SHORT).show();
            cursor.close();
        }
    }

    //Connection monitoring function/class
    public void connection() {
        cm = (ConnectivityManager)getSystemService((Context.CONNECTIVITY_SERVICE));
        myBR = new DetailsActivity.MyBroadCastReceiver();
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        this.registerReceiver(myBR, intentFilter);
    }
    protected class MyBroadCastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ImageView red = (ImageView)findViewById(R.id.imageView2);
            activeNet = cm.getActiveNetworkInfo();
            if((activeNet != null) && (activeNet.isConnectedOrConnecting())) {
                red.setVisibility(View.INVISIBLE);
                deleteFile("dbchanges.txt");
            }
            else
                red.setVisibility(View.VISIBLE);
        }
    }

    //Time/Date pickers and setters
    public void pickDateTime(View view) {
        (new PickDate()).show(getSupportFragmentManager(), "datePicker");
    }
    public void setDate(int y, int m, int d) {
        calendar = Calendar.getInstance();
        calendar.set(y, (m-1), d);
        TextView date;
        (date = (TextView) this.findViewById(R.id.dueDate)).setTextColor(Color.BLACK);
        date.setText(String.valueOf(m + "/" + d + "/" + y));
    }
    public void setTime(int h, int m) {
        calendar.set(Calendar.HOUR_OF_DAY, h);
        calendar.set(Calendar.MINUTE, m);
        TextView time;
        (time = (TextView) this.findViewById(R.id.dueTime)).setTextColor(Color.BLACK);
        if(h > 11) {
            if(m < 10)
                time.setText(String.valueOf((h-12) +":" + "0" + m + " PM"));
            else
                time.setText(String.valueOf((h-12) +":" + m + " PM"));
        }
        else {
            if(h == 0)
                h = 12;
            if(m < 10)
                time.setText(String.valueOf(h + ":" + "0" + m + " AM"));
            else
                time.setText(String.valueOf(h + ":" + m + " AM"));
        }
    }

    //Getters and setters
    public String getName() {
        return ((EditText) this.findViewById(R.id.nameEdit)).getText().toString();
    }
    public void setName(String str) {
        ((EditText)this.findViewById(R.id.nameEdit)).setText(str);
    }

    public String getDesc() {
        return ((EditText) this.findViewById(R.id.descEdit)).getText().toString();
    }
    public void setDesc(String str) {
        ((EditText) this.findViewById(R.id.descEdit)).setText(str);
    }

    public int getDone() {
        return ((CheckBox) findViewById(R.id.doneCheck)).isChecked() ? 1 : 0;
    }
    public void setDone(int done) {
        CheckBox doneBox = (CheckBox) findViewById(R.id.doneCheck);
        doneBox.setChecked(done == 1);
    }

    public String getDateBox() {
        return ((TextView) this.findViewById(R.id.dueDate)).getText().toString();
    }
    public void setDateBox(String str) {
        ((TextView) this.findViewById(R.id.dueDate)).setText(str);
    }

    public String getTimeBox() {
        return ((TextView) this.findViewById(R.id.dueTime)).getText().toString();
    }
    public void setTimeBox(String str) {
        ((TextView) this.findViewById(R.id.dueTime)).setText(str);
    }

    //back to main list
    public void back(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
    public void back() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }


    public void viewLog(View view) {
        Intent intent = new Intent(this, BackupLogActivity.class);
        startActivity(intent);
    }
    public void saveToFile(String str) {
        try {
            FileOutputStream fos = openFileOutput("dbchanges.txt" , Context.MODE_APPEND);
            fos.write(str.getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
    //Test function to print all DB rows to monitor
    public void tableData() {
        Cursor cursor = getContentResolver().query(ToDoProvider.CONTENT_URI,projection,null,null,"_ID ASC");
        if(cursor != null) {
            if(cursor.getCount() > 0){
                for(int i = 0; i < cursor.getCount(); i++) {
                    cursor.moveToPosition(i);
                    Log.e("Cursor pos(i) ", String.valueOf(i));
                    Log.e("ID ", String.valueOf(cursor.getInt(0)));
                    Log.e("Title ", cursor.getString(1));
                    Log.e("Content ", cursor.getString(2));
                    Log.e("Done ", String.valueOf(cursor.getInt(3)));
                    Log.e("Date ", cursor.getString(4));
                    Log.e("Time ", cursor.getString(5));
                }
            }
            cursor.close();
        }
    }*/
}

