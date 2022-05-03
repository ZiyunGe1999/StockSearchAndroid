package com.example.stocks;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.android.car.ui.toolbar.TabLayout;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.tabs.TabLayoutMediator;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
    DecimalFormat df;
    String webpage;

    final String amountPreferenceName = "Amount";
    final String amountKey = "amount";
    SharedPreferences amountPref;
    SharedPreferences.Editor amountEditor;

    final  String portfolioPreferenceName = "Portfolio";
    SharedPreferences portfolioPref;
    SharedPreferences.Editor portfolioEditor;

    final String shareCostPreferenceName = "ShareCost";
    SharedPreferences shareCostPref;
    SharedPreferences.Editor shareCostEditor;


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public interface SetUpView {
        public void setup(JSONObject data) throws JSONException;
    }

    public void setTextForView(String str, TextView view) {
        view.setText(str);
    }

    public void setupPortfolio() {
        Integer currentSharesOwned = 0;
        currentSharesOwned = portfolioPref.getInt(ticker, currentSharesOwned);

        TextView sharesOwnedTextView = findViewById(R.id.sharesOwned);
        sharesOwnedTextView.setText(currentSharesOwned.toString());

        Float currentSharesTotalCost = 0.0F;
        currentSharesTotalCost = shareCostPref.getFloat(ticker, currentSharesTotalCost);
        Float avgCost = (currentSharesOwned > 0) ? currentSharesTotalCost / currentSharesOwned : 0;
        TextView avgCostTextView = findViewById(R.id.avgCost);
        avgCostTextView.setText("$" + df.format(avgCost));
        TextView totalCostTextView = findViewById(R.id.totalCost);
        totalCostTextView.setText("$" + df.format(currentSharesTotalCost));

        Float sharesTotalCostNew = currentSharesOwned * currentPrice;
        Float costChange = sharesTotalCostNew - currentSharesTotalCost;
        costChange = (costChange >= -0.001 && costChange <= 0.001) ? 0.0F : costChange;
        Log.e("gzy", "bought: " + currentSharesTotalCost + " if sold now: " + sharesTotalCostNew + " costChange: " + costChange);
        TextView costChangeTextView = findViewById(R.id.portfolioChange);
        TextView maketValueTextView = findViewById(R.id.marketValue);
        costChangeTextView.setText("$" + df.format(costChange));
        maketValueTextView.setText("$" + df.format(sharesTotalCostNew));
        if (costChange > 0) {
            costChangeTextView.setTextColor(Color.GREEN);
            maketValueTextView.setTextColor(Color.GREEN);
        }
        else if (costChange < 0) {
            costChangeTextView.setTextColor(Color.RED);
            maketValueTextView.setTextColor(Color.RED);
        }
        else {
            costChangeTextView.setTextColor(Color.GRAY);
            maketValueTextView.setTextColor(Color.GRAY);
        }
    }

    public class SetupCompanySocialSentiments implements SetUpView {
        @Override
        public void setup(JSONObject data) throws JSONException {
            JSONArray redditSocial = data.getJSONArray("reddit");
            JSONArray twitterSocial = data.getJSONArray("twitter");
            Integer redditMention = 0;
            Integer twitterMention = 0;
            Integer twitterPositiveMention = 0;
            Integer twitterNegativeMention = 0;
            Integer redditPositiveMention = 0;
            Integer redditNegativeMention = 0;
            for (Integer i = 0; i < redditSocial.length(); i++) {
                JSONObject redditBlock = redditSocial.getJSONObject(i);
                redditMention += redditBlock.getInt("mention");
                redditPositiveMention += redditBlock.getInt("positiveMention");
                redditNegativeMention += redditBlock.getInt("negativeMention");
            }

            for (Integer i = 0; i < twitterSocial.length(); i++) {
                JSONObject twitterBlock = twitterSocial.getJSONObject(i);
                twitterMention += twitterBlock.getInt("mention");
                twitterPositiveMention += twitterBlock.getInt("positiveMention");
                twitterNegativeMention += twitterBlock.getInt("negativeMention");
            }

            setTextForView(redditMention.toString(), findViewById(R.id.socialTable00));
            setTextForView(redditPositiveMention.toString(), findViewById(R.id.socialTable10));
            setTextForView(redditNegativeMention.toString(), findViewById(R.id.socialTable20));

            setTextForView(twitterMention.toString(), findViewById(R.id.socialTable01));
            setTextForView(twitterPositiveMention.toString(), findViewById(R.id.socialTable11));
            setTextForView(twitterNegativeMention.toString(), findViewById(R.id.socialTable21));
        }
    }

    @SuppressLint("ResourceAsColor")
    public void setupCompanyPeers(JSONArray jsonArray) throws JSONException {
        Log.e("gzy", jsonArray.toString());
        LinearLayout companyPeersLinearLayout = findViewById(R.id.companyPeers);
        for (Integer i = 0; i < jsonArray.length(); i++) {
            String peer = jsonArray.getString(i);
            TextView tv = new TextView(getApplicationContext());
            setTextForView(peer + "  ", tv);
            tv.setTextColor(R.color.blue);
//            tv.setTextSize(20);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, DisplayStockInfo.class);
                    intent.putExtra(MainActivity.EXTRA_MESSAGE, peer);
                    startActivity(intent);
                }
            });
            companyPeersLinearLayout.addView(tv);
        }
    }

    public void webpageOnClick(View view){
        Intent openurl = new Intent(Intent.ACTION_VIEW, Uri.parse(webpage));
        startActivity(openurl);
    }

    public class SetUpCompanyDescription implements SetUpView {
        public void setup(JSONObject data) throws JSONException {
            companyFullName = data.getString("name");
            String logoUrl = data.getString("logo");
            ImageView logoView = findViewById(R.id.logo);
            Picasso.get().load(logoUrl).into(logoView);

            setTextForView(data.getString("ticker"), findViewById(R.id.ticker));
            setTextForView(companyFullName, findViewById(R.id.company));
            setTextForView(companyFullName, findViewById(R.id.socialSentimentsCompanyName));

            // setup about
            String ipo = data.getString("ipo");
            Integer index = ipo.indexOf("-");
            ipo = ipo.substring(index + 1) + "-" + ipo.substring(0, index);
            setTextForView(ipo, findViewById(R.id.ipoStartDate));
            String industry = data.getString("finnhubIndustry");
            setTextForView(industry, findViewById(R.id.industry));
            webpage = data.getString("weburl");
            TextView webpageTextView = findViewById(R.id.webpage);
            setTextForView(webpage, webpageTextView);
        }
    }

    public class SetUpCompanyLatestPrice implements SetUpView {
        @Override
        public void setup(JSONObject data) throws JSONException {
//            DecimalFormat df = new DecimalFormat("0.00");
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

            // setup portfolio
            setupPortfolio();

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

            // setup stats
            Double highPrice = data.getDouble("h");
            setTextForView("$" + df.format(highPrice), findViewById(R.id.highPirce));
            Double lowPrice = data.getDouble("l");
            setTextForView("$" + df.format(lowPrice), findViewById(R.id.lowPirce));
            Double openPrice = data.getDouble("o");
            setTextForView("$" + df.format(openPrice), findViewById(R.id.openPirce));
            Double prevClosePrice = data.getDouble("pc");
            setTextForView("$" + df.format(prevClosePrice), findViewById(R.id.prevClose));
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

    public void setupTrends(JSONArray data){
        WebView trendsWebView = findViewById(R.id.trendsChart);
        trendsWebView.getSettings().setJavaScriptEnabled(true);
        trendsWebView.setWebViewClient(new WebViewClient(){
            public void onPageFinished(WebView view, String url) {
                trendsWebView.loadUrl("javascript:setupHighCharts('" + data.toString() + "' )");
            }
        });
        trendsWebView.loadUrl("file:///android_asset/trends/trends.html");
    }

    public void setupEPS(JSONArray data) {
        WebView epsWebView = findViewById(R.id.epsChart);
        epsWebView.getSettings().setJavaScriptEnabled(true);
        epsWebView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                epsWebView.loadUrl("javascript:setupHighCharts('" + data.toString() + "' )");
            }
        });
        epsWebView.loadUrl("file:///android_asset/eps/eps.html");
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
        3500);
    }

    void setupTopNews(JSONArray data) throws JSONException {
        ArrayList<News> newsData = new ArrayList<>();
        for (int i = 0; i < data.length(); i++) {
            JSONObject obj = data.getJSONObject(i);
            if (newsData.size() >= 20) {
                break;
            }
            if (obj.has("source") && obj.has("datetime") && obj.has("headline") && obj.has("summary") && obj.has("url") && obj.has("image") && !obj.getString("image").isEmpty()) {
                News news = new News(obj.getString("source"), obj.getLong("datetime"), obj.getString("headline"), obj.getString("summary"), obj.getString("url"), obj.getString("image"));
                newsData.add(news);
            }
        }
        RecyclerView newsRecyclerView = findViewById(R.id.newsView);
        NewsAdaptor newsAdaptor = new NewsAdaptor(newsData, this);
        newsRecyclerView.setAdapter(newsAdaptor);
    }

    void requestTopNews() {
        long endTimestamp = System.currentTimeMillis();
        long startTimestamp = endTimestamp - (7 * 24 * 60 * 60 * 1000);
        Date endDate = new Date(endTimestamp);
        Date startDate = new Date(startTimestamp);
        DateFormat f = new SimpleDateFormat("yyyy-MM-dd");
        String url = basicUrl + "company-news?symbol=" + ticker + "&from=" + f.format(startDate) + "&to=" + f.format(endDate);
        sendRequest(url, null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_stock_info);

        df = new DecimalFormat("0.00");

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        message = message.toUpperCase();
        ticker = message;

        setTitle(message);

        queue = Volley.newRequestQueue(this);

        sendRequest(basicUrl + "stock/profile2?symbol=" + message, new SetUpCompanyDescription());
        sendRequest(basicUrl + "quote?symbol=" + message, new SetUpCompanyLatestPrice());
        sendRequest(basicUrl + "stock/peers?symbol=" + message, null);
        sendRequest(basicUrl + "stock/social-sentiment?symbol=" + message + "&from=2022-01-01", new SetupCompanySocialSentiments());
        sendRequest(basicUrl + "stock/recommendation?symbol=" + message, null);
        sendRequest(basicUrl + "stock/earnings?symbol=" + message, null);
        requestTopNews();

        viewPager = findViewById(R.id.pager);
        viewPagerAdapter = new ViewPagerAdapter(ticker);
        viewPager.setAdapter(viewPagerAdapter);

        com.google.android.material.tabs.TabLayout tabLayout = findViewById(R.id.tab_layout);
        new TabLayoutMediator(tabLayout,
                viewPager,
                (tab, position) -> tab.setIcon(position == 0 ? R.drawable.ic_chart : R.drawable.ic_time)
        ).attach();

        amountPref = getApplicationContext().getSharedPreferences(amountPreferenceName, Context.MODE_PRIVATE);
        amountEditor = amountPref.edit();
        portfolioPref = getApplicationContext().getSharedPreferences(portfolioPreferenceName, Context.MODE_PRIVATE);
        portfolioEditor = portfolioPref.edit();
        shareCostPref = getApplicationContext().getSharedPreferences(shareCostPreferenceName, Context.MODE_PRIVATE);
        shareCostEditor = shareCostPref.edit();

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
//                DecimalFormat df = new DecimalFormat("0.00");
                tradeCaculateTextView.setText("0 * $" + df.format(currentPrice) + "/share = 0.00");
                TextView tradeRemainedMoneyTextView = dialog.findViewById(R.id.tradeRemainedMoney);
//                SharedPreferences amountPref = getApplicationContext().getSharedPreferences(amountPreferenceName, Context.MODE_PRIVATE);
//                SharedPreferences.Editor amountEditor = amountPref.edit();
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

                            Integer holdShares = 0;
                            holdShares = portfolioPref.getInt(ticker, holdShares);
                            Log.e("gzy", "boughtShares: " + boughtShares + " | holdShares: " + holdShares);
                            portfolioEditor.putInt(ticker, boughtShares + holdShares);
                            portfolioEditor.apply();

                            Float holdCost = 0.0F;
                            holdCost = shareCostPref.getFloat(ticker, holdCost);
                            shareCostEditor.putFloat(ticker, boughtTotal + holdCost);
                            shareCostEditor.apply();

                            new android.os.Handler(Looper.getMainLooper()).postDelayed(
                                new Runnable() {
                                    public void run() {
                                        Log.e("gzy", "buy setupPortfolio times up!");
                                        setupPortfolio();
                                    }
                                },
                                500);

                            String message = "You have successfully bought " + boughtShares + " " + (boughtShares > 1 ? "shares" : "share") + " of " + ticker;
                            showCongratulationDialog(message);
                        }
                    }
                });

                Button sellButton = dialog.findViewById(R.id.sellButton);
                sellButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Integer sellShares = editText.getText().toString().equals("") ? 0 : Integer.valueOf(editText.getText().toString());

                        Integer holdShares = 0;
                        holdShares = portfolioPref.getInt(ticker, holdShares);
                        Log.e("gzy", "you hold " + holdShares + " shares of " + ticker);

                        if (sellShares <= 0) {
                            showAlertDialog("Please enter a valid amount", dialog.getContext());
                        }
                        else if (sellShares > holdShares) {
                            showAlertDialog("Not enough shares to sell", dialog.getContext());
                        }
                        else {
                            dialog.dismiss();
                            Float sellTotal = sellShares * currentPrice;
                            Float remainedMoney = 0.0F;
                            remainedMoney = amountPref.getFloat(amountKey, remainedMoney);
                            remainedMoney += sellTotal;
                            amountEditor.putFloat(amountKey, remainedMoney);
                            amountEditor.apply();

                            portfolioEditor.putInt(ticker, holdShares - sellShares);
                            portfolioEditor.apply();

                            Float holdCost = 0.0F;
                            holdCost = shareCostPref.getFloat(ticker, holdCost);
                            shareCostEditor.putFloat(ticker, holdCost - sellTotal);
                            shareCostEditor.apply();

                            new android.os.Handler(Looper.getMainLooper()).postDelayed(
                                    new Runnable() {
                                        public void run() {
                                            Log.e("gzy", "sell setupPortfolio times up!");
                                            setupPortfolio();
                                        }
                                    },
                                    500);

                            String message = "You have successfully sold " + sellShares + " " + (sellShares > 1 ? "shares" : "share") + " of " + ticker;
                            showCongratulationDialog(message);
                        }
                    }
                });

                dialog.show();
            }
        });

//        RecyclerView newsRecyclerView = findViewById(R.id.newsView);
////        LinearLayoutManager newsLinearLayoutManager = new LinearLayoutManager(this);
////        newsRecyclerView.setLayoutManager(newsLinearLayoutManager);
//        NewsAdaptor newsAdaptor = new NewsAdaptor(this);
//        newsRecyclerView.setAdapter(newsAdaptor);
    }

    public void sendRequest(String url, SetUpView setupView) {
        Log.e("gzy", url);
        // Request a json response from the provided URL.
        if (url.contains("peers")) {
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    try {
                        setupCompanyPeers(response);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
            queue.add(jsonArrayRequest);
        }
        else if (url.contains("recommendation")) {
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    setupTrends(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
            queue.add(jsonArrayRequest);
        }
        else if (url.contains("earnings")) {
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    setupEPS(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
            queue.add(jsonArrayRequest);
        }
        else if (url.contains("company-news")) {
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    try {
                        setupTopNews(response);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
            queue.add(jsonArrayRequest);
        }
        else {
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
}
