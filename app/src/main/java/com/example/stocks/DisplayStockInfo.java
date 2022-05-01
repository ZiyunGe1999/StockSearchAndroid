package com.example.stocks;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.android.car.ui.toolbar.TabLayout;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.tabs.TabLayoutMediator;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.Locale;
import java.util.Set;

public class DisplayStockInfo extends AppCompatActivity {
    final String basicUrl = "https://stocksearchnodejs.wl.r.appspot.com/api/v1/";
    RequestQueue queue;
    ViewPager2 viewPager;

    public interface SetUpView {
        public void setup(JSONObject data) throws JSONException;
    }

    public void setTextForView(String str, TextView view) {
        view.setText(str);
    }

    public class SetUpCompanyDescription implements SetUpView {
        public void setup(JSONObject data) throws JSONException {
            String logoUrl = data.getString("logo");
            ImageView logoView = findViewById(R.id.logo);
            Picasso.get().load(logoUrl).into(logoView);

            setTextForView(data.getString("ticker"), findViewById(R.id.ticker));
        }
    }

    public class SetUpCompanyLatestPrice implements SetUpView {
        @Override
        public void setup(JSONObject data) throws JSONException {
            DecimalFormat df = new DecimalFormat("0.00");
            Double price = data.getDouble("c");
            Double change = data.getDouble("d");
            Double changePercentage = data.getDouble("dp");

            setTextForView("$" + df.format(price), findViewById(R.id.lastPrice));
            TextView changeTextView = findViewById(R.id.change);
            setTextForView("$" + df.format(change) + " (" + df.format(changePercentage) + "%)", changeTextView);
            ImageView trendImageView = findViewById(R.id.trend);
            if (change < 0) {
                changeTextView.setTextColor(Color.RED);
                trendImageView.setImageDrawable(getResources().getDrawable( R.drawable.ic_trend_down ));
            }
            else {
                changeTextView.setTextColor(Color.GREEN);
                trendImageView.setImageDrawable(getResources().getDrawable( R.drawable.ic_trend_up ));
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_stock_info);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        message = message.toUpperCase();

        setTitle(message);

        queue = Volley.newRequestQueue(this);

        sendRequest(basicUrl + "stock/profile2?symbol=" + message, new SetUpCompanyDescription());
        sendRequest(basicUrl + "quote?symbol=" + message, new SetUpCompanyLatestPrice());

        viewPager = findViewById(R.id.pager);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter();
        viewPager.setAdapter(viewPagerAdapter);

        com.google.android.material.tabs.TabLayout tabLayout = findViewById(R.id.tab_layout);
        new TabLayoutMediator(tabLayout,
                viewPager,
                (tab, position) -> tab.setIcon(position == 0 ? R.drawable.ic_chart : R.drawable.ic_time)
        ).attach();
    }

    public void sendRequest(String url, SetUpView setupView) {
        // Request a json response from the provided URL.
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            setupView.setup(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);
    }
}
