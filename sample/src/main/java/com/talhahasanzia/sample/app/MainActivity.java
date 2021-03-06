package com.talhahasanzia.sample.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.talhahasanzia.annotation.Routeable;
import com.talhahasanzia.sample.app.model.MyParcelable;

import java.io.Serializable;

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

    public void onClick(View view) {

        boolean shouldFinish = ((AppCompatCheckBox) findViewById(R.id.finishCheckBox)).isChecked();
        String extraData = ((EditText) findViewById(R.id.dataEditText)).getText().toString();

        switch (view.getId()) {
            case R.id.simpleRoute:
                if (shouldFinish) {
                    SecondActivityRouter.route(MainActivity.this, true);
                } else {
                    SecondActivityRouter.route(MainActivity.this);
                }
                break;
            case R.id.stringRoute:
                if (shouldFinish) {
                    SecondActivityRouter.route(MainActivity.this, "myData", extraData, true);
                } else {
                    SecondActivityRouter.route(MainActivity.this, "myData", extraData);
                }
                break;
            case R.id.bundleRoute:
                Bundle bundle = new Bundle();
                bundle.putString("myData", extraData);
                if (shouldFinish) {
                    SecondActivityRouter.route(MainActivity.this, bundle, true);
                } else {
                    SecondActivityRouter.route(MainActivity.this, bundle);
                }
                break;
            case R.id.serializableRoute:

                if (shouldFinish) {
                    SecondActivityRouter.route(MainActivity.this, "myData", (Serializable) extraData, true);
                } else {
                    SecondActivityRouter.route(MainActivity.this, "myData", (Serializable) extraData);
                }
                break;
            case R.id.parcelableRoute:
                MyParcelable myParcelable = new MyParcelable();
                myParcelable.setmName(extraData);
                myParcelable.setmData(-1);
                if (shouldFinish) {
                    SecondActivityRouter.route(MainActivity.this, "myData", myParcelable, true);
                } else {
                    SecondActivityRouter.route(MainActivity.this, "myData", myParcelable);
                }
                break;
            case R.id.modifiedRoute:
                // modify intent example
                SecondActivityRouter.route(this, intent -> {
                    // incoming intent
                    // modify it
                    intent.putExtra("dummyFloat", 3.142799F);
                    // return it.
                    return intent;  // now the router will have your customized intent and will
                });
                break;

            case R.id.resultRoute:
                // startActivityForResult example
                // adding flag to make SecondActivity know this was started for Result
                // this is not needed to modify intent here, it is just for illustration purposes
                SecondActivityRouter.routeForResult(this, 399, intent -> {
                    intent.putExtra("isForResult", true);
                    return intent;
                });
                break;
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == 399) {
                Toast.makeText(this, "Result received!", Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    protected void onDestroy() {
        Toast.makeText(this, "Finished", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }


}
