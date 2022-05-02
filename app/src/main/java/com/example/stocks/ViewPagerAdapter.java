package com.example.stocks;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

public class ViewPagerAdapter extends RecyclerView.Adapter<ViewPagerAdapter.ViewPagerViewHolder> {
    JSONObject hourlyPriceData = null;
    JSONObject historicalData = null;
    String ticker;
    String color = null;

    ViewPagerAdapter(String stock) {
        ticker = stock;
    }

    @NonNull
    @Override
    public ViewPagerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewPagerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_page, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewPagerViewHolder holder, @SuppressLint("RecyclerView") int position) {
//        Log.e("gzy", "it's position " + position);
        holder.priceWebView.setWebViewClient(new WebViewClient(){
            public void onPageFinished(WebView view, String url){
                if (hourlyPriceData != null && color != null && position == 0) {
                    holder.priceWebView.loadUrl("javascript:setupHighCharts('" + hourlyPriceData.toString() + "', '" + ticker + "', '" + color + "' )");
                }
                if (historicalData != null && position == 1) {
                    holder.priceWebView.loadUrl("javascript:setupHighCharts('" + historicalData.toString() + "', '" + ticker + "' )");
                }
            }
        });
        if (position == 0) {
            holder.priceWebView.loadUrl("file:///android_asset/priceHighcharts/priceHighcharts.html");
        }
        else {
            holder.priceWebView.loadUrl("file:///android_asset/historical/historical.html");
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    class ViewPagerViewHolder extends RecyclerView.ViewHolder {

        LinearLayout linearLayout;
        WebView priceWebView;

        public ViewPagerViewHolder(@NonNull View itemView) {
            super(itemView);
            linearLayout = itemView.findViewById(R.id.itemLayout);
            priceWebView = itemView.findViewById(R.id.price_webview);
            priceWebView.getSettings().setJavaScriptEnabled (true);
//            priceWebView.getSettings().setUseWideViewPort(true);
//            priceWebView.getSettings().setLoadWithOverviewMode(true);
        }
    }
}
