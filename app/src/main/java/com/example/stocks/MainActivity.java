package com.example.stocks;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.view.View;
import android.widget.TextView;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {


    RecyclerView recyclerView;
    RecyclerViewAdapter mAdapter;
    ArrayList<String> stringArrayList = new ArrayList<>();
    CoordinatorLayout coordinatorLayout;
    TextView todayDateTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        coordinatorLayout = findViewById(R.id.coordinatorLayout);

        todayDateTextView = findViewById(R.id.todayDate);
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat month_date = new SimpleDateFormat("MMM");
        String month_name = month_date.format(cal.getTime());
        todayDateTextView.setText(cal.get(Calendar.DAY_OF_MONTH) + " " + month_name + " " + cal.get(Calendar.YEAR));


        populateRecyclerView();
        enableSwipeToDeleteAndUndo();


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

        mAdapter = new RecyclerViewAdapter(stringArrayList);
        recyclerView.setAdapter(mAdapter);


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
    }


}

