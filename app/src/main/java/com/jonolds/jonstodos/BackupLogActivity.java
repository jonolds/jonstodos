package com.jonolds.jonstodos;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileReader;

public class BackupLogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup_log);
        loadText();
    }

    public void back(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void loadText() {
        TextView tv = (TextView) this.findViewById(R.id.fileView);
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader BRead = new BufferedReader(new FileReader(this.getFilesDir() + "/dbchanges.txt"));
            String line;
            while((line = BRead.readLine()) != null)
                text.append(line);
            text.append("\n\r");
            BRead.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        tv.setText(text);
    }
}
