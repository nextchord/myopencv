package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class TempletSelectActivity extends AppCompatActivity {

    private ListView listViewTemplates;
    private String[] templates = new String[]{"公务员考试行测答题卡"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_templet_select);
        listViewTemplates = this.findViewById(R.id.list_view_templates);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                TempletSelectActivity.this, R.layout.activity_templet_select, templates);
        listViewTemplates.setAdapter(adapter);
    }
}