package com.example.stocks;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.Calendar;
import java.util.Locale;
import java.util.Set;

public class DisplayStockInfo extends AppCompatActivity {
    final String basicUrl = "https://stocksearchnodejs.wl.r.appspot.com/api/v1/";
    String ticker = "null";
    String companyFullName = null;
    RequestQueue queue;
    ViewPager2 viewPager;
    ViewPagerAdapter viewPagerAdapter;
    final Context context = this;
    Float currentPrice = 0.0F;

    final String amountPreferenceName = "Amount";
    final String amountKey = "amount";

    public interface SetUpView {
        public void setup(JSONObject data) throws JSONException;
    }

    public void setTextForView(String str, TextView view) {
        view.setText(str);
    }

    public class SetUpCompanyDescription implements SetUpView {
        public void setup(JSONObject data) throws JSONException {
            companyFullName = data.getString("name");
            String logoUrl = data.getString("logo");
            ImageView logoView = findViewById(R.id.logo);
            Picasso.get().load(logoUrl).into(logoView);

            setTextForView(data.getString("ticker"), findViewById(R.id.ticker));
            setTextForView(companyFullName, findViewById(R.id.company));
        }
    }

    public class SetUpCompanyLatestPrice implements SetUpView {
        @Override
        public void setup(JSONObject data) throws JSONException {
            DecimalFormat df = new DecimalFormat("0.00");
            Double price = data.getDouble("c");
            currentPrice = price.floatValue();
            Double change = data.getDouble("d");
            Double changePercentage = data.getDouble("dp");

            setTextForView("$" + df.format(price), findViewById(R.id.lastPrice));
            TextView changeTextView = findViewById(R.id.change);
            setTextForView("$" + df.format(change) + " (" + df.format(changePercentage) + "%)", changeTextView);
            ImageView trendImageView = findViewById(R.id.trend);
            if (change < 0) {
                viewPagerAdapter.color = "red";
                changeTextView.setTextColor(Color.RED);
                trendImageView.setImageDrawable(getResources().getDrawable( R.drawable.ic_trend_down ));
            }
            else {
                viewPagerAdapter.color = "green";
                changeTextView.setTextColor(Color.GREEN);
                trendImageView.setImageDrawable(getResources().getDrawable( R.drawable.ic_trend_up ));
            }

            // Setup two hourly price charts
            long endTimestamp = System.currentTimeMillis() / 1000L;
            long latestTimestamp = data.getLong("t");
            endTimestamp = endTimestamp - latestTimestamp > (5 * 60) ? latestTimestamp : endTimestamp;
            long beginTimestamp = endTimestamp - (6 * 60 * 60);
            String url = basicUrl + "stock/candle?symbol=" + ticker + "&resolution=5" + "&from=" + beginTimestamp + "&to=" + endTimestamp;
            sendRequest(url, new SetupHourlyPriceVariation());

            beginTimestamp = endTimestamp - (2 * 365 * 24 * 60 * 60);
            url = basicUrl + "stock/candle?symbol=" + ticker + "&resolution=D" + "&from=" + beginTimestamp + "&to=" + endTimestamp;
            sendRequest(url, new SetupHistoricalChart());
        }
    }

    public class SetupHourlyPriceVariation implements SetUpView {
        @Override
        public void setup(JSONObject data) throws JSONException {
            Log.e("gzy", "get data and action the hourlyPrice page now");
            viewPagerAdapter.hourlyPriceData = data;
            viewPagerAdapter.notifyItemChanged(0);
        }
    }

    public class SetupHistoricalChart implements SetUpView {
        @Override
        public void setup(JSONObject data) throws JSONException {
            Log.e("gzy", "get data and action the historical page now");
            viewPagerAdapter.historicalData = data;
            viewPagerAdapter.notifyItemChanged(1);
        }
    }

    void showCongratulationDialog(String message) {
        final Dialog congratulationDialog = new Dialog(context);
        congratulationDialog.setContentView(R.layout.congratulation_dialog_layout);
        congratulationDialog.setTitle("congratulation");

        TextView messageTextView = congratulationDialog.findViewById(R.id.congratulationMessage);
        messageTextView.setText(message);

        Button doneButton = congratulationDialog.findViewById(R.id.doneButton);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                congratulationDialog.dismiss();
            }
        });

        congratulationDialog.show();
    }

    void showAlertDialog(String message, Context parentContext) {
        final Dialog alertDialog = new Dialog(parentContext);
        alertDialog.setContentView(R.layout.alert_layout);
        alertDialog.setTitle("alert");

        TextView alertMessageTextView = alertDialog.findViewById(R.id.alertMessage);
        alertMessageTextView.setText(message);

        Window window = alertDialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();

        wlp.gravity = Gravity.BOTTOM;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);

        alertDialog.show();

        new android.os.Handler(Looper.getMainLooper()).postDelayed(
        new Runnable() {
            public void run() {
                Log.e("gzy", "times up!");
                alertDialog.dismiss();
            }
        },
        4000);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_stock_info);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        message = message.toUpperCase();
        ticker = message;

        setTitle(message);

        queue = Volley.newRequestQueue(this);

        sendRequest(basicUrl + "stock/profile2?symbol=" + message, new SetUpCompanyDescription());
        sendRequest(basicUrl + "quote?symbol=" + message, new SetUpCompanyLatestPrice());

        viewPager = findViewById(R.id.pager);
        viewPagerAdapter = new ViewPagerAdapter(ticker);
        viewPager.setAdapter(viewPagerAdapter);

        com.google.android.material.tabs.TabLayout tabLayout = findViewById(R.id.tab_layout);
        new TabLayoutMediator(tabLayout,
                viewPager,
                (tab, position) -> tab.setIcon(position == 0 ? R.drawable.ic_chart : R.drawable.ic_time)
        ).attach();

        // trade button
        Button tradeButton = findViewById(R.id.trade);
        tradeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.dialog_layout);
                dialog.setTitle("Title...");

                TextView header = dialog.findViewById(R.id.dialogHeader);
                header.setText("Trade " + companyFullName + " Shares");

                EditText editText = dialog.findViewById(R.id.inputValue);
                TextView tradeCaculateTextView = dialog.findViewById(R.id.tradeCaculate);
                DecimalFormat df = new DecimalFormat("0.00");
                tradeCaculateTextView.setText("0 * $" + df.format(currentPrice) + "/share = 0.00");
                TextView tradeRemainedMoneyTextView = dialog.findViewById(R.id.tradeRemainedMoney);
                SharedPreferences amountPref = getApplicationContext().getSharedPreferences(amountPreferenceName, Context.MODE_PRIVATE);
                SharedPreferences.Editor amountEditor = amountPref.edit();
                Float currentAmount = 0.0F;
                currentAmount = amountPref.getFloat(amountKey, currentAmount);
                tradeRemainedMoneyTextView.setText("$" + df.format(currentAmount) + " to buy " + ticker);
                editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        Integer boughtShares = editText.getText().toString().equals("") ? 0 : Integer.valueOf(editText.getText().toString());
                        Float boughtTotal = boughtShares * currentPrice;
                        tradeCaculateTextView.setText(boughtShares.toString() + " * $" + df.format(currentPrice) + "/share = " + df.format(boughtTotal));
                    }
                });

                Button buyButton = dialog.findViewById(R.id.buyBotton);
                buyButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Integer boughtShares = editText.getText().toString().equals("") ? 0 : Integer.valueOf(editText.getText().toString());

//                        dialog.dismiss();

                        Float boughtTotal = boughtShares * currentPrice;
                        Float remainedMoney = 0.0F;
                        remainedMoney = amountPref.getFloat(amountKey, remainedMoney);

                        if (boughtShares <= 0) {
                            showAlertDialog("Please enter a valid amount", dialog.getContext());
                        }
                        else if(boughtTotal > remainedMoney) {
                            showAlertDialog("Not enough money to buy", dialog.getContext());
                        }
                        else {
                            dialog.dismiss();
                            remainedMoney -= boughtTotal;
                            amountEditor.putFloat(amountKey, remainedMoney);
                            amountEditor.apply();

                            String message = "You have successfully bought " + boughtShares + " " + (boughtShares > 1 ? "shares" : "share") + " of " + ticker;
                            showCongratulationDialog(message);
                        }
                    }
                });

                dialog.show();
            }
        });
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
