package com.mast.android.orm;

import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.mast.android.orm.js2p.Datum_Schema;
import com.mast.android.orm.js2p.Mast_new;
import com.mast.android.orm.js2p.Mast_new_Schema;
import com.mast.orm.db.MastOrm;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MastOrm.initialize(getApplicationContext());
        Mast_new_Schema.load().type("sathish").save();
        Datum_Schema.load().link("some val").path("some val").sectionBreak(false).save();
        List<Mast_new> mast_news = Mast_new_Schema.load().find();
        Toast.makeText(getApplicationContext(),"Mast News Size "+mast_news.size(),Toast.LENGTH_LONG).show();
    }
}
