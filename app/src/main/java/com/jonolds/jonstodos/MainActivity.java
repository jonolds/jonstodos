package com.jonolds.jonstodos;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
//import static android.provider.AlarmClock.EXTRA_MESSAGE;

public class MainActivity extends AppCompatActivity {
    ImageView red;
    ConnectivityManager cm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    protected void onStart() {
        super.onStart();
        connect();
        //This starts the Async process that ultimately populates the task list
        new Asyn().execute();
    }

    //Connectivity functions. The red image is on top of the green image and I toggle its visibility
    //to display connection status.
    public void connect() {
        red = (ImageView) this.findViewById(R.id.imageView2);
        cm = (ConnectivityManager)getSystemService((Context.CONNECTIVITY_SERVICE));
        MyBroadCastReceiver myBR = new MyBroadCastReceiver();
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        this.registerReceiver(myBR, intentFilter);
    }
    protected class MyBroadCastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            NetworkInfo activeNet = cm.getActiveNetworkInfo();
            if((activeNet != null) && (activeNet.isConnectedOrConnecting())) {
                red.setVisibility(View.INVISIBLE);
                deleteFile("dbchanges.txt");
            }
            else
                red.setVisibility(View.VISIBLE);
        }
    }

    //Aysnchronous class to run database operations
    private class Asyn extends AsyncTask<String, Void, Cursor> {
        String[] projection = {
                ToDoProvider.TODO_TABLE_COL_ID,
                ToDoProvider.TODO_TABLE_COL_TITLE,
        };
        int operation;
        protected Cursor doInBackground(String... str) {
            operation = str.length;
            return getContentResolver().query(ToDoProvider.CONTENT_URI, projection, null, null, "_ID ASC");
        }
        protected void onPostExecute(Cursor cursor) {
            if(operation == 0)
                populate(cursor);
            else {
                if(cursor != null){
                    for(int i = 0; i < cursor.getCount(); i++) {
                        cursor.moveToPosition(i);
                        int itemToDel = cursor.getInt(0);
                        getContentResolver().delete(Uri.parse(ToDoProvider.CONTENT_URI + "/" + (itemToDel)),null,null);
                    }
                    cursor.close();
                }
                new Asyn().execute();
            }
        }
    }

    //Once the cursor is returned, this function uses the data to populate the task list
    public void populate(Cursor cursor) {
        List<String> toDos = new ArrayList<>();
        if(cursor != null) {
            if(cursor.getCount() > 0){
                for(int i = 0; i < cursor.getCount(); i++) {
                    cursor.moveToPosition(i);
                    toDos.add((i+1) + ": " + cursor.getString(1));
                }
            }
            cursor.close();
        }
        //Create adapter to plug data into listview
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, R.layout.content_list, toDos);
        ListView listView = (ListView) findViewById(R.id.listview);
        listView.setAdapter(dataAdapter);
        listView.setTextFilterEnabled(true);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                detailTask(position);
                Log.e("Main List Pos ", String.valueOf(position));
                Log.e("Main List id ", String.valueOf(id));
            }
        });
    }

    public void clearList(View view){
        new Asyn().execute("_ID ASC");
    }

    //called when a user clicks a listed task.
    public void detailTask(int position) {
        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra("position", position);
        startActivity(intent);
    }
    //Called when the user taps the Send button
    public void addTask(View view) {
        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra("position", -1);
        startActivity(intent);
    }
}