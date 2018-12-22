package com.talhahasanzia.router.annotations;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.talhahasanzia.annotation.Routeable;

@Routeable
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        Toast.makeText(this, "Finished", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }
}
