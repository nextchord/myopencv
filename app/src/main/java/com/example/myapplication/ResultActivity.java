package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.orhanobut.logger.Logger;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class ResultActivity extends AppCompatActivity {

    private Button buttonOK;
    private ListView listView;
    private TextView textGrade;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        listView = findViewById(R.id.list_grade);
        Intent intent = getIntent();

        textGrade = findViewById(R.id.text_view_grade);
        int grade = intent.getIntExtra("score", 0);
        textGrade.setText("最终得分："+grade);

        List<Result> list = (ArrayList<Result>)intent.getSerializableExtra("ansList");
        AnsAdapter adapter = new AnsAdapter(ResultActivity.this, R.layout.ans_item, list);
        listView.setAdapter(adapter);

        buttonOK = findViewById(R.id.button_ok);
        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }
}