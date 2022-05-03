package com.example.stocks;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class NewsAdaptor extends RecyclerView.Adapter<NewsAdaptor.NewsViewHolder> {
    ArrayList<News> data;
    Context context;

    public NewsAdaptor(ArrayList<News> data, Context context) {
        this.data = data;
        this.context = context;
    }

    @NonNull
    @Override
    public NewsAdaptor.NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = View.inflate(context, R.layout.news_item, null);
        return new NewsViewHolder(view);
    }

    public String convertDate(Long timestamp) {
        Long currentTimestamp = System.currentTimeMillis() / 1000L;
        Long difference = currentTimestamp - timestamp;
        if (difference < 60) {
            return String.valueOf(difference) + " " + (difference > 1 ? "seconds" : "second") + " ago";
        }
        else if (difference < 60 * 60) {
            difference /= 60;
            return String.valueOf(difference) + " " + (difference > 1 ? "minutes" : "minute") + " ago";
        }
        else {
            difference /= (60 * 60);
            return String.valueOf(difference) + " " + (difference > 1 ? "hours" : "hour") + " ago";
        }
    }

    @Override
    public void onBindViewHolder(@NonNull NewsAdaptor.NewsViewHolder holder, int position) {
        TextView headlineTextView;
        ImageView imageView = holder.newsImageView;
        TextView sourceTextView;
        TextView dateTextView;
        if (position == 0) {
            holder.newsLinearLayout.setOrientation(LinearLayout.VERTICAL);
            holder.firstLinearLayout.getLayoutParams().height = 0;
//            holder.firstLinearLayout.setLayou
            int dimensionInDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 380, context.getResources().getDisplayMetrics());
            imageView.getLayoutParams().width = dimensionInDp;
            dimensionInDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150, context.getResources().getDisplayMetrics());
            imageView.getLayoutParams().height = dimensionInDp;
            headlineTextView = holder.newsAfterTextView;
            sourceTextView = holder.newsSourceAfterTextView;
            dateTextView = holder.newsDateTextAfterView;
        }
        else {
            headlineTextView = holder.newsBeforeTextView;
            sourceTextView = holder.newsSourceTexView;
            dateTextView = holder.newsDateTextView;
        }

        headlineTextView.setText(data.get(position).headline);
        Picasso.get().load(data.get(position).image).into(imageView);
        sourceTextView.setText(data.get(position).source);
        dateTextView.setText(convertDate(data.get(position).datetime));
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    public class NewsViewHolder extends RecyclerView.ViewHolder {
        LinearLayout newsLinearLayout;
        TextView newsBeforeTextView;
        ImageView newsImageView;
        TextView newsAfterTextView;

        TextView newsSourceTexView;
        TextView newsDateTextView;

        TextView newsSourceAfterTextView;
        TextView newsDateTextAfterView;

        LinearLayout firstLinearLayout;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            newsLinearLayout = itemView.findViewById(R.id.newsLinearLayout);
            newsBeforeTextView = itemView.findViewById(R.id.newsItemBefore);
            newsImageView = itemView.findViewById(R.id.newsImage);
            newsAfterTextView = itemView.findViewById(R.id.newsItemAfter);
            newsSourceTexView = itemView.findViewById(R.id.newsSource);
            newsDateTextView = itemView.findViewById(R.id.newsDate);
            newsSourceAfterTextView = itemView.findViewById(R.id.newsSourceAfter);
            newsDateTextAfterView = itemView.findViewById(R.id.newsDateAfter);

            firstLinearLayout = itemView.findViewById(R.id.firstLinearLayout);
        }
    }
}
