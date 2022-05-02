package com.example.stocks;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.TextView;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "com.example.stocks.STOCK";
    RecyclerView recyclerView;
    RecyclerView favoritesRecyclerView;
    RecyclerViewAdapter mAdapter;
    RecyclerViewAdapter favoritesAdapter;
    ArrayList<String> stringArrayList = new ArrayList<>();
    ArrayList<String> favoritesList = new ArrayList<>();
    CoordinatorLayout coordinatorLayout;
    TextView todayDateTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        favoritesRecyclerView = findViewById(R.id.favoritesRecyclerView);
        coordinatorLayout = findViewById(R.id.coordinatorLayout);

        todayDateTextView = findViewById(R.id.todayDate);
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat month_date = new SimpleDateFormat("MMM");
        String month_name = month_date.format(cal.getTime());
        todayDateTextView.setText(cal.get(Calendar.DAY_OF_MONTH) + " " + month_name + " " + cal.get(Calendar.YEAR));


        populateRecyclerView();
        enableSwipeToDeleteAndUndo();

        SharedPreferences pref = getApplicationContext().getSharedPreferences("Amount", 0);
        SharedPreferences.Editor editor = pref.edit();
        if (!pref.contains("amount")) {
            editor.putFloat("amount", 25000.0F);
            editor.apply();
        }
    }

    public void dispalyStockInfoActivity(String s) {
        Intent intent = new Intent(this, DisplayStockInfo.class);
        intent.putExtra(EXTRA_MESSAGE, s);
        startActivity(intent);
    }

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
                Log.e("gzy", "onQueryTextChange: " + s);
                return true;
            }
        });
        return true;
    }

    private void populateRecyclerView() {
        stringArrayList.add("Item 1");
        stringArrayList.add("Item 2");
        stringArrayList.add("Item 3");
        stringArrayList.add("Item 4");
//        stringArrayList.add("Item 5");
//        stringArrayList.add("Item 6");
//        stringArrayList.add("Item 7");
//        stringArrayList.add("Item 8");
//        stringArrayList.add("Item 9");
//        stringArrayList.add("Item 10");
//        stringArrayList.add("Item 11");
//        stringArrayList.add("Item 12");

        favoritesList.add("Favorite 1");
        favoritesList.add("Favorite 2");
        favoritesList.add("Favorite 3");
        favoritesList.add("Favorite 4");

        mAdapter = new RecyclerViewAdapter(stringArrayList);
        recyclerView.setAdapter(mAdapter);

        favoritesAdapter = new RecyclerViewAdapter(favoritesList);
        favoritesRecyclerView.setAdapter(favoritesAdapter);
    }

    private void enableSwipeToDeleteAndUndo() {
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this, mAdapter) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {


                final int position = viewHolder.getAdapterPosition();
                final String item = mAdapter.getData().get(position);

                mAdapter.removeItem(position);


//                Snackbar snackbar = Snackbar
//                        .make(coordinatorLayout, "Item was removed from the list.", Snackbar.LENGTH_LONG);
//                snackbar.setAction("UNDO", new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//
//                        mAdapter.restoreItem(item, position);
//                        recyclerView.scrollToPosition(position);
//                    }
//                });
//
//                snackbar.setActionTextColor(Color.YELLOW);
//                snackbar.show();

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

