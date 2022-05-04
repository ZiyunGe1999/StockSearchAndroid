
package com.example.stocks;

import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.graphics.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> implements SwipeToDeleteCallback.ItemTouchHelperContract {

    private ArrayList<String> data;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView mTitle;
        RelativeLayout relativeLayout;
        View rowView;

        public MyViewHolder(View itemView) {
            super(itemView);

            rowView = itemView;
            mTitle = itemView.findViewById(R.id.txtTitle);
        }
    }

    public RecyclerViewAdapter(ArrayList<String> data) {
        this.data = data;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_row, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.mTitle.setText(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    @Override
    public void onRowMoved(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(data, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(data, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }

//    @Override
//    public void onRowSelected(MyViewHolder myViewHolder) {
//        myViewHolder.rowView.setBackgroundColor(Color.GRAY);
//    }

    @Override
    public void onRowClear(RecyclerView.ViewHolder myViewHolder) {
        if (myViewHolder instanceof RecyclerViewAdapter.MyViewHolder) {
            RecyclerViewAdapter.MyViewHolder myViewHoldertmp = (RecyclerViewAdapter.MyViewHolder) myViewHolder;
            myViewHoldertmp.rowView.setBackgroundColor(Color.WHITE);
        }
    }

    public void removeItem(int position) {
        data.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(String item, int position) {
        data.add(position, item);
        notifyItemInserted(position);
    }

    public ArrayList<String> getData() {
        return data;
    }
}


