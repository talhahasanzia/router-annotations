package com.talhahasanzia.router.annotations;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.talhahasanzia.annotation.Routeable;
import com.talhahasanzia.router.annotations.model.MyParcelable;

@Routeable
public class SecondActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
    }

    @Override
    protected void onResume() {
        super.onResume();
        parseData();
    }

    private void parseData() {
        TextView textView = findViewById(R.id.textView);

        if (getIntent().getStringExtra("myData") != null) {
            textView.setText(getIntent().getStringExtra("myData"));
        }

        if (getIntent().getBundleExtra("myData") != null) {
            textView.setText(getIntent().getBundleExtra("myData").getString("myData"));
        }

        if (getIntent().getSerializableExtra("myData") != null) {
            textView.setText((String) getIntent().getSerializableExtra("myData"));
        }

        if (getIntent().getParcelableExtra("myData") != null) {
            String data = ((MyParcelable) getIntent().getParcelableExtra("myData")).getData();
            textView.setText(data);
        }


    }
}
