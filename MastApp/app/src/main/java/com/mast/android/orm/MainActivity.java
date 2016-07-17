package com.mast.android.orm;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.mast.orm.db.MastOrm;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MastOrm.initialize(getApplicationContext());
        Mast_new.load().type("sathish").save();
    }
}
