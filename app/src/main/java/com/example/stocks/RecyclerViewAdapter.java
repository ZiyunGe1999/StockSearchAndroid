
package com.example.stocks;

import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.graphics.Color;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> implements SwipeToDeleteCallback.ItemTouchHelperContract {

    private ArrayList<String> data;
    public Context context;
    RequestQueue queue;
    final String basicUrl = "https://stocksearchnodejs.wl.r.appspot.com/api/v1/";
    DecimalFormat df;

    final String favoritePreferenceName = "Favorite";
    SharedPreferences favoritePref;
    SharedPreferences.Editor favoriteEditor;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView mTitle;
        public TextView fullnameTextView;
        public TextView updatedPriceTextView;
        public TextView changeTextView;
        public ImageView sharesTrendImageView;
        public ImageView sharesRightArrowImageView;
        View rowView;

        public MyViewHolder(View itemView) {
            super(itemView);

            rowView = itemView;
            mTitle = itemView.findViewById(R.id.txtTitle);
            fullnameTextView = itemView.findViewById(R.id.sharesNumText);
            updatedPriceTextView = itemView.findViewById(R.id.shareCostTotal);
            changeTextView = itemView.findViewById(R.id.shareChange);
            sharesTrendImageView = itemView.findViewById(R.id.sharesTrend);
            sharesRightArrowImageView = itemView.findViewById(R.id.rightArrow);
        }
    }

    public RecyclerViewAdapter(ArrayList<String> data, Context context) {
        this.data = data;
        this.context = context;
        queue = Volley.newRequestQueue(context);
        df = new DecimalFormat("0.00");

        favoritePref = context.getApplicationContext().getSharedPreferences(favoritePreferenceName, Context.MODE_PRIVATE);
        favoriteEditor = favoritePref.edit();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_row, parent, false);
        return new MyViewHolder(itemView);
    }

    public void updateFullname(MyViewHolder holder, String ticker) {
        String url = basicUrl + "stock/profile2?symbol=" + ticker;
        Log.e("gzy", "updateFullname send: " + url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e("gzy", "updateFullname return: " + url);
                try {
                    String name = response.getString("name");
                    holder.fullnameTextView.setText(name);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                updateFullname(holder, ticker);
            }
        });
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonObjectRequest);
    }

    public void updatePrice(MyViewHolder holder, String ticker) {
        if (holder.mTitle.getText().toString().equals(ticker) && data.contains(ticker)) {
            String url = basicUrl + "quote?symbol=" + ticker;
            Log.e("gzy", "updatePrice: " + url);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                @SuppressLint("ResourceAsColor")
                @Override
                public void onResponse(JSONObject response) {
                    Log.e("gzy", "updatePrice successfully: " + url);
                    try {
                        Double currentPrice = response.getDouble("c");
                        holder.updatedPriceTextView.setText("$" + df.format(currentPrice));
                        Double changePrice = response.getDouble("d");
                        Double changePercentage = response.getDouble("dp");
                        if (changePrice > 0) {
                            holder.sharesTrendImageView.setImageResource(R.drawable.ic_trend_up);
                            holder.changeTextView.setTextColor(Color.GREEN);
                        }
                        else {
                            holder.sharesTrendImageView.setImageResource(R.drawable.ic_trend_down);
                            holder.changeTextView.setTextColor(Color.RED);
                        }
                        holder.changeTextView.setText("$" + df.format(changePrice) + "(" + df.format(changePercentage) + "%)");
                        new android.os.Handler(Looper.getMainLooper()).postDelayed(
                                new Runnable() {
                                    public void run() {
                                        updatePrice(holder, ticker);
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
                                    updatePrice(holder, ticker);
                                }
                            },
                            15000);
                }
            });
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(jsonObjectRequest);
        }
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        String curTicker = data.get(position);
        holder.mTitle.setText(curTicker);
        updateFullname(holder, curTicker);
        updatePrice(holder, curTicker);
        holder.sharesRightArrowImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, DisplayStockInfo.class);
                intent.putExtra(MainActivity.EXTRA_MESSAGE, curTicker);
                intent.putExtra(MainActivity.EXTRA_PARENT_KEY, "MainActivity");
                context.startActivity(intent);
                ((Activity)context).finish();
            }
        });
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
        String curTicker = data.get(position);
        favoriteEditor.remove(curTicker);
        favoriteEditor.apply();
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


