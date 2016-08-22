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
import com.mast.android.orm.js2p.CommentaryList_Schema;
import com.mast.android.orm.mocks.MockSingleton;
import com.mast.android.orm.realm.objects.CommentaryList;
import com.mast.android.orm.realm.objects.OverSeperator;
import com.mast.orm.db.MastOrm;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class MainActivity extends AppCompatActivity {

    Realm realm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_new);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        MastOrm.initialize(this);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
//                insert1000Rows();
//                insert1000RealmRows();
            }
        });
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(this)
                .name(Realm.DEFAULT_REALM_NAME)
                .schemaVersion(0)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);
        this.realm = Realm.getDefaultInstance();
    }

    public void fetchMastData(View view){
        Debug.startMethodTracing("mast_orm_fetch");
        List<com.mast.android.orm.js2p.CommentaryList> cbz_commList = CommentaryList_Schema.load().findData();
        Debug.stopMethodTracing();
        Toast.makeText(this," rows fetched "+cbz_commList.size(), Toast.LENGTH_LONG).show();
    }

    public void fetchRealmData(View view){
        Debug.startMethodTracing("realm_fetch");
        List<CommentaryList> commentaryLists = realm.where(CommentaryList.class).findAll();
        Debug.stopMethodTracing();
        Toast.makeText(this," rows fetched "+commentaryLists.size(), Toast.LENGTH_LONG).show();
    }

    public void insertMastData(View view){
        List<com.mast.android.orm.js2p.CommentaryList> cbz_commList = MockSingleton.getInstance().getMockMastCommentaryList(500);
        Debug.startMethodTracing("mast_orm_insert");
                for(com.mast.android.orm.js2p.CommentaryList cbz_comm:cbz_commList){
                    CommentaryList_Schema.load().insert(cbz_comm);
                }

        Debug.stopMethodTracing();
        Toast.makeText(this,"10 rows inserted", Toast.LENGTH_LONG).show();
    }

    public void insertRealmData(View view){
        List<CommentaryList> commentaryLists = getMockRealmCommentaryList(500);
        Debug.startMethodTracing("realm_insert");
        realm.beginTransaction();
        for(CommentaryList cbz_comm:commentaryLists){
            realm.copyToRealm(cbz_comm);
        }
        realm.commitTransaction();
        Debug.stopMethodTracing();
        Toast.makeText(this,"10 rows inserted", Toast.LENGTH_LONG).show();
    }


    private List<CommentaryList> getMockRealmCommentaryList(int count){
        List<CommentaryList> commentaryList = new ArrayList<>();
        for(int i=0;i<count;i++){
            commentaryList.add(getMockRealmCommentary(i));
        }
        return commentaryList;
    }

    private CommentaryList getMockRealmCommentary(int index){
        CommentaryList commentaryList = new CommentaryList();
        commentaryList.setCommText("commText "+index);
        commentaryList.setEvent("No Event");
        commentaryList.setInningsId(index);
        commentaryList.setOverSeparator(getMockOverSeperator());
        return commentaryList;
    }

    private OverSeperator getMockOverSeperator(){
        OverSeperator overSeparator = new OverSeperator();
        overSeparator.setCommText("commText ");
        overSeparator.setInningsId(3);
        return overSeparator;
    }

}
