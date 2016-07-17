package com.mast.android.orm;

import android.view.View;

public interface ItemClickListener<T> {
    void onClick(T item, int position, View view);
}