package com.mast.android.orm.views;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.mast.android.orm.ItemClickListener;
import com.mast.android.orm.R;
import com.mast.android.orm.adapters.ListAdapter;
import com.mast.android.orm.js2p.Datum;
import com.mast.android.orm.js2p.Datum_Schema;
import com.mast.android.orm.js2p.Mast_new;
import com.mast.android.orm.js2p.Mast_new_Schema;
import com.mast.android.orm.subview.DividerDecoration;
import com.mast.orm.db.MastOrm;

import java.util.List;

public class MainActivity extends AppCompatActivity implements ItemClickListener<Datum> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        adapter = new ListAdapter(this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setupRecyclerView(recyclerView);
        initialize();
        MastOrm.initialize(getApplicationContext());
        Mast_new_Schema.load().type("sathish").save();
        Datum_Schema.load().link("some val").path("some val").sectionBreak(false).save();
        List<Mast_new> mast_news = Mast_new_Schema.load().find();

        List<Datum> datumList = Datum_Schema.load().find();
        adapter.addItems(datumList);
        Toast.makeText(getApplicationContext(), "Mast News Size " + datumList.size(), Toast.LENGTH_LONG).show();
        List<Datum> singleDatum = Datum_Schema.load().where(Datum_Schema.COLUMNS._ID, 1).find();
    }

    protected void setupRecyclerView(RecyclerView recyclerView) {
        if (recyclerView != null) {
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(recyclerView.getContext());
            recyclerView.setLayoutManager(linearLayoutManager);
            if (recyclerView.getAdapter() != adapter) {
                recyclerView.setAdapter(adapter);
            }

            adapter.setItemClickListener(this);
            recyclerView.addItemDecoration(new DividerDecoration(recyclerView.getContext(), LinearLayoutManager.VERTICAL));
        }

    }

    private void initialize() {
        toolbar.setTitle("Mast ORM");
        setSupportActionBar(toolbar);
    }

    @Override
    public void onClick(Datum item, int position, View view) {

    }

    Toolbar toolbar;
    ListAdapter adapter;
    RecyclerView recyclerView;
}
