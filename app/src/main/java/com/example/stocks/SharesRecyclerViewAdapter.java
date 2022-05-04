package com.example.stocks;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;

public class SharesRecyclerViewAdapter extends RecyclerView.Adapter<SharesRecyclerViewAdapter.SharesViewHolder> implements SwipeToDeleteCallback.ItemTouchHelperContract {

    private ArrayList<Share> shares;
    final Context context;
    RequestQueue queue;
    DecimalFormat df;
    final String basicUrl = "https://stocksearchnodejs.wl.r.appspot.com/api/v1/";
    TextView netWorthTextView;

    public class SharesViewHolder extends RecyclerView.ViewHolder {

        private TextView mTitle;
        public TextView sharesNumTexView;
        public TextView sharesCostTextView;
        public TextView sharesChangeTextView;
        public ImageView sharesTrendImageView;
        public ImageView sharesRightArrowImageView;
        View rowView;

        public SharesViewHolder(View itemView) {
            super(itemView);

            rowView = itemView;
            mTitle = itemView.findViewById(R.id.txtTitle);
            sharesNumTexView = itemView.findViewById(R.id.sharesNumText);
            sharesCostTextView = itemView.findViewById(R.id.shareCostTotal);
            sharesChangeTextView = itemView.findViewById(R.id.shareChange);
            sharesTrendImageView = itemView.findViewById(R.id.sharesTrend);
            sharesRightArrowImageView = itemView.findViewById(R.id.rightArrow);
        }
    }

    public SharesRecyclerViewAdapter(ArrayList<Share> shares, Context context, TextView netWorthTextView) {
        this.shares = shares;
        this.context = context;
        queue = Volley.newRequestQueue(context);
        df = new DecimalFormat("0.00");
        this.netWorthTextView = netWorthTextView;
    }

    @Override
    public SharesRecyclerViewAdapter.SharesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_row, parent, false);
        return new SharesRecyclerViewAdapter.SharesViewHolder(itemView);
    }

    public void updateInfoForHolder(SharesRecyclerViewAdapter.SharesViewHolder holder, Share share, Double sharesPrevTotalCost) {
        if (holder.mTitle.getText().toString().equals(share.ticker) && shares.contains(share)) {
            String url = basicUrl + "quote?symbol=" + share.ticker;
            Log.e("gzy", "updateInfoForHolder: " + url);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                @SuppressLint("ResourceAsColor")
                @Override
                public void onResponse(JSONObject response) {
                    Log.e("gzy", "updateInfoForHolder successfully: " + url);
                    try {
                        Double currentPrice = response.getDouble("c");
                        Double maketTotalPrice = currentPrice * share.sharesNum;
                        holder.sharesCostTextView.setText("$" + df.format(maketTotalPrice));
                        Double change = maketTotalPrice - share.totalCost;
                        change = change <= 0.001 && change >= -0.001 ? 0 : change;
                        Double changePercentage = change / share.totalCost * 100;
                        if (change > 0) {
                            holder.sharesTrendImageView.setImageResource(R.drawable.ic_trend_up);
                            holder.sharesChangeTextView.setTextColor(Color.GREEN);
                            Double prevNetWorth = Double.parseDouble(netWorthTextView.getText().toString().substring(1));
                            Log.e("gzy", "change > 0 | prevNetWorth: " + prevNetWorth.toString());
                            double currentNetWorth = prevNetWorth - sharesPrevTotalCost + maketTotalPrice;
                            netWorthTextView.setText("$" + df.format(currentNetWorth));
                        }
                        else if (change < 0) {
                            holder.sharesChangeTextView.setTextColor(Color.RED);
                            Double prevNetWorth = Double.parseDouble(netWorthTextView.getText().toString().substring(1));
                            Log.e("gzy", "change < 0 | prevNetWorth: " + prevNetWorth.toString());
                            double currentNetWorth = prevNetWorth - sharesPrevTotalCost + maketTotalPrice;
                            netWorthTextView.setText("$" + df.format(currentNetWorth));
                        }
                        else {
                            holder.sharesTrendImageView.setImageResource(android.R.color.transparent);
                            holder.sharesChangeTextView.setTextColor(R.color.black);
                        }
                        holder.sharesChangeTextView.setText("$" + df.format(change) + "(" + df.format(changePercentage) + "%)");
                        new android.os.Handler(Looper.getMainLooper()).postDelayed(
                                new Runnable() {
                                    public void run() {
                                        updateInfoForHolder(holder, share, maketTotalPrice);
                                    }
                                },
                                15000);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    new android.os.Handler(Looper.getMainLooper()).postDelayed(
                            new Runnable() {
                                public void run() {
                                    updateInfoForHolder(holder, share, sharesPrevTotalCost);
                                }
                            },
                            15000);
                }
            });
            queue.add(jsonObjectRequest);
        }
    }

    @Override
    public void onBindViewHolder(SharesRecyclerViewAdapter.SharesViewHolder holder, int position) {
        Share share = shares.get(position);
        holder.mTitle.setText(share.ticker);
        if (share.sharesNum > 1) {
            holder.sharesNumTexView.setText(share.sharesNum.toString() + " shares");
        }
        else {
            holder.sharesNumTexView.setText(share.sharesNum.toString() + " share");
        }
        holder.sharesRightArrowImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, DisplayStockInfo.class);
                intent.putExtra(MainActivity.EXTRA_MESSAGE, share.ticker);
                intent.putExtra(MainActivity.EXTRA_PARENT_KEY, "MainActivity");
                context.startActivity(intent);
                ((Activity)context).finish();
            }
        });
        updateInfoForHolder(holder, share, Double.valueOf(share.totalCost));
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
