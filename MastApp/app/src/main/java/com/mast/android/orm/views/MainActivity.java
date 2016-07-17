package com.mast.android.orm.views;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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
//        Datum_Schema.load().link("some val").path("some val").sectionBreak(false).save();
        List<Mast_new> mast_news = Mast_new_Schema.load().find();
        List<Datum> datumList = Datum_Schema.load().find();
//        Datum_Schema.load().where(Datum_Schema.COLUMNS._ID,2).delete();
        adapter.addItems(datumList);
        Toast.makeText(getApplicationContext(), "Mast News Size " + datumList.size(), Toast.LENGTH_LONG).show();
//        List<Datum> singleDatum = Datum_Schema.load().where(Datum_Schema.COLUMNS._ID, 1).find();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.activity_home_drawer, menu);

        //Dynamic Icon Tinting
        MenuItem menuItem = menu.getItem(0);
        Drawable drawable = menuItem.getIcon();
        if (drawable != null) {
            // If we don't mutate the drawable, then all drawable's with this id will have a color
            // filter applied to it.
            drawable.mutate();
            drawable.setColorFilter(ContextCompat.getColor(this, R.color.text), PorterDuff.Mode.SRC_ATOP);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_calendar:
                openDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void openDialog() {
        final Dialog dialog = new Dialog(this); // Context, this, etc.
        dialog.setContentView(R.layout.orm_add_item);
        dialog.setTitle(R.string.add_item);

        final EditText text = (EditText) dialog.findViewById(R.id.text);

        final EditText sequence = (EditText) dialog.findViewById(R.id.sequence);

        final EditText link = (EditText) dialog.findViewById(R.id.link);

        Button dialogButton = (Button) dialog.findViewById(R.id.dialog_ok);
        // if button is clicked, close the custom dialog
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Datum_Schema.load()
                        .link(link.getText().toString())
                        .sequence(Integer.parseInt(sequence.getText().toString()))
                        .text(text.getText().toString())
                        .save();
                refreshList();
                dialog.dismiss();
            }
        });

        Button dialogCacncelButton = (Button) dialog.findViewById(R.id.dialog_cancel);
        // if button is clicked, close the custom dialog
        dialogCacncelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void refreshList() {
        List<Datum> datumList = Datum_Schema.load().find();
        adapter.addItems(datumList);
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
