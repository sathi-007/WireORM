package com.mast.android.orm.views;

import android.os.Bundle;
import android.os.Debug;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.mast.android.orm.R;
import com.mast.android.orm.js2p.Cbz_comm;
import com.mast.android.orm.js2p.Cbz_comm_Schema;
import com.mast.android.orm.mocks.MockSingleton;
import com.mast.orm.db.MastOrm;

import java.util.List;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Property;
import de.greenrobot.daogenerator.Schema;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_new);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        MastOrm.initialize(this);
//        Debug.startMethodTracing("example");
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                insert1000Rows();
            }
        });
//        Debug.stopMethodTracing();
    }

    public void fetchData(View view){
//        Debug.startMethodTracing("mast_orm_fetch");
        List<Cbz_comm> cbz_commList = Cbz_comm_Schema.load().findData();

//        Debug.stopMethodTracing();
        Toast.makeText(this,"10 rows fetched "+cbz_commList.size(), Toast.LENGTH_LONG).show();
    }

    private void insert1000Rows(){
        List<Cbz_comm> cbz_commList = MockSingleton.getInstance().getCbzCommList(10);
        Debug.startMethodTracing("mast_orm_insert");
                for(Cbz_comm cbz_comm:cbz_commList){
                    Cbz_comm_Schema.load().insert(cbz_comm);
                }

        Debug.stopMethodTracing();
        Toast.makeText(this,"10 rows inserted", Toast.LENGTH_LONG).show();
    }
}
