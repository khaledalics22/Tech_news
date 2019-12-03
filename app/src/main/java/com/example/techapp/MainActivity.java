package com.example.techapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<TechReportClass>>, ReportsAdapter.onItemClickInterface {
    public String url = "https://content.guardianapis.com/search?";
    private final int MAIN_LOADER = 0;
    private final int LOAD_PROGRESS = 1;
    private final int NO_RESULTS = 2;
    private final int NO_CONNECTION = 3;
    private final int SHOW_LIST = 4;
    private final int TRY_AGAIN = 5;
    private RecyclerView reportList;
    private TextView tvCurrentState;
    private ProgressBar progressBarMain;
    private ReportsAdapter adapter;
    private Toast toast;
    private LinearLayout cuuStatusLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setLogo(R.mipmap.ic_launcher_round);
            actionBar.setDisplayUseLogoEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
        setContentView(R.layout.activity_main);
        reportList = findViewById(R.id.rv_reports);
        tvCurrentState = findViewById(R.id.tv_no_connection);
        progressBarMain = findViewById(R.id.progress_circular);
        cuuStatusLayout = findViewById(R.id.no_connection_layout);
        Button btnTryAgain = findViewById(R.id.btr_try_again);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        reportList.setLayoutManager(layoutManager);
        adapter = new ReportsAdapter(this, new ArrayList<TechReportClass>());
        reportList.setAdapter(adapter);
        btnTryAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
            }
        });
        loadReports();
    }

    private boolean isInternetConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo status = connectivityManager.getActiveNetworkInfo();
        if (status != null && status.isConnectedOrConnecting())
            return true;
        return false;
    }

    private void loadReports() {
        try {
            if (isInternetConnected()) {
                showResponseStatus(LOAD_PROGRESS);
                getSupportLoaderManager().initLoader(MAIN_LOADER, null, this);
            } else {
                showResponseStatus(NO_CONNECTION);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @NonNull
    @Override
    public Loader<List<TechReportClass>> onCreateLoader(int id, @Nullable Bundle args) {
        String url = BuildUrl();
        return new TechReportLoader(this, url);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<TechReportClass>> loader, List<TechReportClass> data) {
        if (data == null || data.isEmpty()) {
            showResponseStatus(TRY_AGAIN);
        }
        updateUI(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<TechReportClass>> loader) {
        adapter.clear();
    }

    private void updateUI(List<TechReportClass> data) {
        if (data == null || data.size() == 0) {
            showResponseStatus(NO_RESULTS);
        } else {
            makeToast(getString(R.string.Load_Done));
            adapter.clear();
            adapter.addAll((ArrayList<TechReportClass>) data);
            adapter.notifyDataSetChanged();
            showResponseStatus(SHOW_LIST);
        }
    }

    /** to force the loader to fetch new data while running the application */
    private void refresh() {
        if(isInternetConnected()) {
            makeToast(getString(R.string.refresh));
            showResponseStatus(LOAD_PROGRESS);
            getSupportLoaderManager().restartLoader(MAIN_LOADER, null, this);
        }
        else showResponseStatus(NO_CONNECTION);
    }

    private void showResponseStatus(int status) {
        switch (status) {
            case LOAD_PROGRESS:
                progressBarMain.setVisibility(View.VISIBLE);
                cuuStatusLayout.setVisibility(View.GONE);
                reportList.setVisibility(View.GONE);
                break;
            case NO_RESULTS:
                tvCurrentState.setText(getString(R.string.no_results));
                cuuStatusLayout.setVisibility(View.VISIBLE);
                progressBarMain.setVisibility(View.GONE);
                reportList.setVisibility(View.GONE);
                break;
            case NO_CONNECTION:
                tvCurrentState.setText(getString(R.string.no_connection));
                cuuStatusLayout.setVisibility(View.VISIBLE);
                progressBarMain.setVisibility(View.GONE);
                reportList.setVisibility(View.GONE);
                break;
            case TRY_AGAIN:
                tvCurrentState.setText(getString(R.string.try_again));
                cuuStatusLayout.setVisibility(View.VISIBLE);
                progressBarMain.setVisibility(View.GONE);
                reportList.setVisibility(View.GONE);
                break;
            case SHOW_LIST:
                cuuStatusLayout.setVisibility(View.GONE);
                progressBarMain.setVisibility(View.GONE);
                reportList.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public void onItemClickListener(TechReportClass currReport) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(currReport.getmWebUrl()));
        startActivity(intent);
    }

    private void makeToast(String message) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.setText(message);
        toast.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                refresh();
                return true;
            case R.id.settings:
                startActivity(new Intent(this, Settings.class));
                return true;
            default:
                return false;
        }
    }

    public String BuildUrl() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String orderBy = sharedPrefs.getString(getString(R.string.pref_list_order_by_key),
                getString(R.string.pref_order_by_default));
        Uri uriBase = Uri.parse(url);
        Uri.Builder builder = uriBase.buildUpon();
        builder.appendQueryParameter("section", "technology")
                .appendQueryParameter("order-by", orderBy)
                .appendQueryParameter("page", String.valueOf(1))
                .appendQueryParameter("show-fields", "thumbnail")
                .appendQueryParameter("show-tags", "contributor")
                .appendQueryParameter("q", "technology")
                .appendQueryParameter("api-key", "eda90898-9281-4d8c-a6c0-d868cac47caa");
        return builder.build().toString();
    }
}
