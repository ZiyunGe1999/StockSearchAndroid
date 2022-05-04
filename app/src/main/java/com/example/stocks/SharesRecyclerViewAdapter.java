package com.example.stocks;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;

public class SharesRecyclerViewAdapter extends RecyclerView.Adapter<SharesRecyclerViewAdapter.SharesViewHolder> implements SwipeToDeleteCallback.ItemTouchHelperContract {

    private ArrayList<Share> shares;

    public class SharesViewHolder extends RecyclerView.ViewHolder {

        private TextView mTitle;
        RelativeLayout relativeLayout;
        View rowView;

        public SharesViewHolder(View itemView) {
            super(itemView);

            rowView = itemView;
            mTitle = itemView.findViewById(R.id.txtTitle);
        }
    }

    public SharesRecyclerViewAdapter(ArrayList<Share> shares) {
        this.shares = shares;
    }

    @Override
    public SharesRecyclerViewAdapter.SharesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_row, parent, false);
        return new SharesRecyclerViewAdapter.SharesViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SharesRecyclerViewAdapter.SharesViewHolder holder, int position) {
        holder.mTitle.setText(shares.get(position).ticker);
    }

    @Override
    public int getItemCount() {
        return shares == null ? 0 : shares.size();
    }

    @Override
    public void onRowMoved(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(shares, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(shares, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }

//    @Override
//    public void onRowSelected(SharesRecyclerViewAdapter.SharesViewHolder myViewHolder) {
//        myViewHolder.rowView.setBackgroundColor(Color.GRAY);
//    }

    @Override
    public void onRowClear(RecyclerView.ViewHolder myViewHolder) {
        if (myViewHolder instanceof SharesRecyclerViewAdapter.SharesViewHolder) {
//            myViewHolder.rowView.setBackgroundColor(Color.WHITE);
        }
    }


    public void removeItem(int position) {
        shares.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(Share item, int position) {
        shares.add(position, item);
        notifyItemInserted(position);
    }
}
