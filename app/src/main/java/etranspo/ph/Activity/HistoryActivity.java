package etranspo.ph.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import etranspo.ph.Adapter.HistoryAdapter;
import etranspo.ph.Entity.History;
import etranspo.ph.R;
import etranspo.ph.SQLite.ORM.HistoryORM;

public class HistoryActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    // Init ui elements
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.historySwipeRefreshLayout)
    SwipeRefreshLayout historySwipeRefreshLayout;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.historyRecyclerView)
    RecyclerView historyRecyclerView;

    // Variables
    HistoryORM h = new HistoryORM();
    List<History> historyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("History");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> startActivity(new Intent(HistoryActivity.this, MainActivity.class)));

        ButterKnife.bind(this);

        historySwipeRefreshLayout.setOnRefreshListener(this);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        historyRecyclerView.setLayoutManager(layoutManager);
        getData();
    }

    @Override
    public void onRefresh() {
        new Handler().postDelayed(() -> {
            getData();
            historySwipeRefreshLayout.setRefreshing(false);
        }, 2000);
    }

    private void getData() {
        historyList = h.getAll(getApplicationContext());
        RecyclerView.Adapter adapter = new HistoryAdapter(historyList);
        historyRecyclerView.setAdapter(adapter);
    }
}
