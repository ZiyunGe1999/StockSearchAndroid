package com.example.stocks;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;

import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import androidx.appcompat.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Time;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "com.example.stocks.STOCK";
    public static final String EXTRA_PARENT_KEY = "com.example.stocks.STOCK_PARENT_KEY";
    RecyclerView recyclerView;
    RecyclerView favoritesRecyclerView;
    SharesRecyclerViewAdapter mAdapter;
    RecyclerViewAdapter favoritesAdapter;
//    ArrayList<String> stringArrayList = new ArrayList<>();
    ArrayList<Share> shares = new ArrayList<>();
    ArrayList<String> favoritesList = new ArrayList<>();
    CoordinatorLayout coordinatorLayout;
    TextView todayDateTextView;

    DecimalFormat df;

    SharedPreferences amountPref;
    SharedPreferences.Editor amountEditor;
    final String amountKey = "amount";

    final  String portfolioPreferenceName = "Portfolio";
    SharedPreferences portfolioPref;
    SharedPreferences.Editor portfolioEditor;

    final String shareCostPreferenceName = "ShareCost";
    SharedPreferences shareCostPref;
    SharedPreferences.Editor shareCostEditor;

//    Long lastChangeTimestamp;
    final String basicUrl = "https://stocksearchnodejs.wl.r.appspot.com/api/v1/";
    RequestQueue queue;
    String currentStringOnSearch = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("gzy", "Create Main Activity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        df = new DecimalFormat("0.00");
//        lastChangeTimestamp = System.currentTimeMillis() / 1000L;
        queue = Volley.newRequestQueue(this);

        recyclerView = findViewById(R.id.recyclerView);
        favoritesRecyclerView = findViewById(R.id.favoritesRecyclerView);
        coordinatorLayout = findViewById(R.id.coordinatorLayout);

        todayDateTextView = findViewById(R.id.todayDate);
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat month_date = new SimpleDateFormat("MMM");
        String month_name = month_date.format(cal.getTime());
        todayDateTextView.setText(cal.get(Calendar.DAY_OF_MONTH) + " " + month_name + " " + cal.get(Calendar.YEAR));

        TextView finnhubTextView = findViewById(R.id.finnhub);
        finnhubTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openurl = new Intent(Intent.ACTION_VIEW, Uri.parse("https://finnhub.io/"));
                startActivity(openurl);
            }
        });


        amountPref = getApplicationContext().getSharedPreferences("Amount", 0);
        amountEditor = amountPref.edit();
        if (!amountPref.contains(amountKey)) {
            amountEditor.putFloat(amountKey, 25000.0F);
            amountEditor.apply();
        }
        TextView tv = findViewById(R.id.cashBalance);
        Float cash = 0.0F;
        cash = amountPref.getFloat(amountKey, cash);
        tv.setText("$" + df.format(cash));

        portfolioPref = getApplicationContext().getSharedPreferences(portfolioPreferenceName, Context.MODE_PRIVATE);
        portfolioEditor = portfolioPref.edit();
        shareCostPref = getApplicationContext().getSharedPreferences(shareCostPreferenceName, Context.MODE_PRIVATE);
        shareCostEditor = shareCostPref.edit();

        Map<String, ?> allEntries = portfolioPref.getAll();
        ArrayList<Share> sharesTmp = new ArrayList<>();
        Float netWorth = cash;
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
//            Log.d("map values", entry.getKey() + ": " + entry.getValue().toString());
            String shareTicker = entry.getKey();
            Integer shareNum = Integer.valueOf(entry.getValue().toString());
            if (shareNum > 0) {
                Float shareTotalCost = 0.0F;
                shareTotalCost = shareCostPref.getFloat(shareTicker, shareTotalCost);
                Share share = new Share(shareTicker, shareNum, shareTotalCost);
                sharesTmp.add(share);
                netWorth += shareTotalCost;
            }else {
                portfolioEditor.remove(shareTicker);
                shareCostEditor.remove(shareTicker);
            }
        }
        portfolioEditor.apply();
        shareCostEditor.apply();
        shares = sharesTmp;
        TextView netWorthTextView = findViewById(R.id.netWorth);
        netWorthTextView.setText("$" + df.format(netWorth));

        populateRecyclerView();
        enableSwipeToDeleteAndUndo();
    }

    public void dispalyStockInfoActivity(String s) {
        Intent intent = new Intent(this, DisplayStockInfo.class);
        intent.putExtra(EXTRA_MESSAGE, s);
        intent.putExtra(EXTRA_PARENT_KEY, "MainActivity");
        startActivity(intent);
        finish();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String s) {
                dispalyStockInfoActivity(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                s = s.toUpperCase();
                currentStringOnSearch = s;
                Log.e("gzy", "onQueryTextChange: " + s);
//                Long currentTimestamp = System.currentTimeMillis() / 1000L;
                if (s.length() > 1) {
                    final String searchS = s;
                    new android.os.Handler(Looper.getMainLooper()).postDelayed(
                            new Runnable() {
                                public void run() {
                                    Log.e("gzy", "1 second times up");
                                    if (!searchS.equals(currentStringOnSearch)) {
                                        return;
                                    }
                                    String url = basicUrl + "search?q=" + searchS;
                                    Log.e("gzy", "auto complete request send: " + url);
                                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                                            (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                                                @Override
                                                public void onResponse(JSONObject response) {
                                                    Log.e("gzy", "auto complete request returns: " + url);
                                                    try {
                                                        JSONArray result = response.getJSONArray("result");
                                                        ArrayList<String> autocompleteResult = new ArrayList<>();
                                                        for (int i = 0; i < result.length(); i++) {
                                                            JSONObject item = result.getJSONObject(i);
                                                            String symbol = item.getString("symbol");
                                                            String fullname = item.getString("description");
                                                            if (!symbol.matches(".*\\d.*") && !symbol.contains(".")) {
                                                                String cur = symbol + " | " + fullname;
                                                                autocompleteResult.add(cur);
                                                            }
                                                        }
                                                        SearchView.SearchAutoComplete searchAutoComplete = (SearchView.SearchAutoComplete)searchView.findViewById(androidx.appcompat.R.id.search_src_text);
                                                        searchAutoComplete.setDropDownBackgroundResource(R.color.white);
                                                        searchAutoComplete.setThreshold(1);
                                                        String dataArr[] = autocompleteResult.toArray(new String[autocompleteResult.size()]);
//                                        String dataArr[] = {"Apple" , "Amazon" , "Amd", "Microsoft", "Microwave", "MicroNews", "Intel", "Intelligence"};
                                                        ArrayAdapter<String> newsAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_dropdown_item_1line, dataArr);
                                                        searchAutoComplete.setAdapter(newsAdapter);
                                                        searchAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                            @Override
                                                            public void onItemClick(AdapterView<?> adapterView, View view, int itemIndex, long id) {
                                                                String queryString = adapterView.getItemAtPosition(itemIndex).toString();
//                                                                searchAutoComplete.setText("" + queryString);
//                                                                Toast.makeText(MainActivity.this, "you clicked " + queryString, Toast.LENGTH_SHORT).show();
                                                                String selectedTicker = queryString.substring(0, queryString.indexOf(" | "));
                                                                Intent intent = new Intent(MainActivity.this, DisplayStockInfo.class);
                                                                intent.putExtra(EXTRA_MESSAGE, selectedTicker);
                                                                intent.putExtra(EXTRA_PARENT_KEY, "MainActivity");
                                                                startActivity(intent);
                                                                finish();
                                                            }
                                                        });
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
                            },
                            1000);
                }
                return true;
            }
        });
        return true;
    }

    private void populateRecyclerView() {
//        stringArrayList.add("Item 1");
//        stringArrayList.add("Item 2");
//        stringArrayList.add("Item 3");
//        stringArrayList.add("Item 4");


        favoritesList.add("Favorite 1");
        favoritesList.add("Favorite 2");
        favoritesList.add("Favorite 3");
        favoritesList.add("Favorite 4");

        mAdapter = new SharesRecyclerViewAdapter(shares, this, findViewById(R.id.netWorth));
        recyclerView.setAdapter(mAdapter);

        favoritesAdapter = new RecyclerViewAdapter(favoritesList);
        favoritesRecyclerView.setAdapter(favoritesAdapter);
    }

    private void enableSwipeToDeleteAndUndo() {
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this, mAdapter) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
//                final int position = viewHolder.getAdapterPosition();
//                final String item = mAdapter.getData().get(position);
//                mAdapter.removeItem(position);
            }
        };

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(recyclerView);

        SwipeToDeleteCallback favoriteSwipeCallback = new SwipeToDeleteCallback(this, favoritesAdapter) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition();
                Log.e("gzy", "onSwiped: " + position);
//                final String item = favoritesAdapter.getData().get(position);
                favoritesAdapter.removeItem(position);
            }
        };
        ItemTouchHelper favoritesItemTouchhelper = new ItemTouchHelper(favoriteSwipeCallback);
        favoritesItemTouchhelper.attachToRecyclerView(favoritesRecyclerView);
    }


}

