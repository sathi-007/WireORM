package com.mast.android.orm.adapters;

import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mast.android.orm.ItemClickListener;
import com.mast.android.orm.R;
import com.mast.android.orm.adapters.viewholders.ViewHolder;
import com.mast.android.orm.js2p.Datum;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sathish-n on 17/7/16.
 */

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.NewsItemViewHolder> {

    public ListAdapter(Context context) {
        this.context = context;
    }

    public void addItems(List<Datum> dotumList) {
        this.newsItems.clear();
        this.newsItems.addAll(dotumList);
        notifyDataSetChanged();
    }

    public void setItemClickListener(ItemClickListener<Datum> listener) {
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public NewsItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder");
        LayoutInflater minInflater = LayoutInflater.from(parent.getContext());
        ViewGroup vSpecialNews = (ViewGroup) minInflater.inflate(R.layout.orm_list_item, parent, false);
        return new NewsItemHolder(vSpecialNews);
    }

    public class NewsItemHolder extends NewsItemViewHolder implements View.OnClickListener{

        public NewsItemHolder(View view) {
            super(view);
            dotumId = (TextView) view.findViewById(R.id.dotumId);
            dotumText = (TextView) view.findViewById(R.id.dotumText);
            editImg = (ImageView) view.findViewById(R.id.editBtn);
        }

        @Override
        public void setData(Datum data, int position) {
            dotumId.setText("  "+data.getSequence());
            dotumText.setText(" "+data.getText());
        }

        @Override
        public void onClick(View v) {
            super.onClick(v);
        }

        TextView dotumId;
        TextView dotumText;
        ImageView editImg;
        ImageView deleteImg;
    }

    @Override
    public void onBindViewHolder(NewsItemViewHolder holder, int position) {
        Datum itemModel = newsItems.get(position);
        holder.setData(itemModel, position);
    }

    @Override
    public int getItemCount() {
        if (newsItems.size() > 0) {
            return newsItems.size();
        }
        else
            return 0;
    }

    abstract class NewsItemViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, ViewHolder<Datum> {

        public NewsItemViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (listener == null)
                return;
//
            int position = getLayoutPosition();
            Datum item = newsItems.get(position);
//
            if (item == null)
                return;
//
//            /**
//             * Dynamically adding TransitionName for shared element transition. Works only above Lollipop version.
//             */
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
//                imgView.setTransitionName(item.id + "");
//
            if (listener != null)
                listener.onClick(item, position, v);
        }

    }

    private final Context context;
    private ItemClickListener<Datum> listener;
    private final String TAG = ListAdapter.class.getSimpleName();
    private final List<Datum> newsItems = new ArrayList<>();
}
