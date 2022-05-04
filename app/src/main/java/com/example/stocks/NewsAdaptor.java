package com.example.stocks;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class NewsAdaptor extends RecyclerView.Adapter<NewsAdaptor.NewsViewHolder> {
    ArrayList<News> data;
    Context context;

    RecyclerView myRecyclerView;

    private String encodeURL(String url) {
        String result = URLEncoder.encode(url);
        return result;
    }

    private final View.OnClickListener myClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int itemPosition = myRecyclerView.getChildLayoutPosition(view);
            Dialog itemDialog = new Dialog(context);
            itemDialog.setContentView(R.layout.news_dialog);
            itemDialog.setTitle("news_dialog");

            News news = data.get(itemPosition);

            TextView tv = itemDialog.findViewById(R.id.dialogSource);
            tv.setText(news.source);

            DateFormat f = new SimpleDateFormat("MMM dd, yyyy");
            tv = itemDialog.findViewById(R.id.dialogDate);
            Date date = new Date(news.datetime * 1000);
            tv.setText(f.format(date));

            tv = itemDialog.findViewById(R.id.dialogHeadline);
            tv.setText(news.headline);

            tv = itemDialog.findViewById(R.id.dialogSummary);
            tv.setText(news.summary);

            ImageView chrome = itemDialog.findViewById(R.id.chrome);
            chrome.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent openurl = new Intent(Intent.ACTION_VIEW, Uri.parse(news.url));
                    context.startActivity(openurl);
                }
            });

            ImageView twitter = itemDialog.findViewById(R.id.twitter);
            twitter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String url = "https://twitter.com/intent/tweet?text=" + news.headline + "&url=" + encodeURL(news.url);
                    Log.e("gzy", "twitter: " + url);
                    Intent openurl = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    context.startActivity(openurl);
                }
            });

            ImageView facebook = itemDialog.findViewById(R.id.facebook);
            facebook.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String url = "https://www.facebook.com/sharer/sharer.php?u=" + encodeURL(news.url) + "&amp;src=sdkpreparse";
                    Log.e("gzy", "facebook: " + url);
                    Intent openurl = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    context.startActivity(openurl);
                }
            });

            itemDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            itemDialog.show();
        }
    };

    public NewsAdaptor(ArrayList<News> data, Context context, RecyclerView myRecyclerView) {
        this.data = data;
        this.context = context;
        this.myRecyclerView = myRecyclerView;
    }

    @NonNull
    @Override
    public NewsAdaptor.NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = View.inflate(context, R.layout.news_item, null);
        view.setOnClickListener(myClickListener);
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
    public int getItemViewType(int position) {
        return position;
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
