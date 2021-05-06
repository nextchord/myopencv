package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class StandardActivity extends AppCompatActivity {

    private EditText etext1;
    private EditText etext2;
    private EditText etext3;
    private Button buttonOk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_standard);

        etext1 = findViewById(R.id.editText1);
        etext2 = findViewById(R.id.editText2);
        etext3 = findViewById(R.id.editText3);
        buttonOk = findViewById(R.id.button_standard_ok);


        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringBuilder sb = new StringBuilder();
                sb.append(etext1.getText());
                sb.append(etext2.getText());
                sb.append(etext3.getText());
                String standard = sb.toString().trim();
                if(standard.length() != 27){
                    Toast.makeText(getApplicationContext(), "请确保标准答案设置符合要求", Toast.LENGTH_SHORT).show();
                }
                else {
                    Intent intent = new Intent(StandardActivity.this, PictureActivity.class);
                    intent.putExtra("standard", standard);
                    startActivity(intent);
                }
            }
        });

    }
}