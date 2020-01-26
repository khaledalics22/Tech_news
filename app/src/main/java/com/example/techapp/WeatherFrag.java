package com.example.techapp;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class WeatherFrag extends Fragment implements LoaderManager.LoaderCallbacks<List<TechReportClass>> {


    public WeatherFrag() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private Toast toast;
    private TextView tvCurrentState;
    private ProgressBar progressBarMain;
    private ReportsAdapter adapter;
    private LinearLayout cuuStatusLayout;
    private RecyclerView reportList;
    private String url = "https://content.guardianapis.com/search?";
    private final int WEATHER_LOADER = 3;
    private final int LOAD_PROGRESS = 1;
    private final int NO_RESULTS = 2;
    private final int NO_CONNECTION = 3;
    private final int SHOW_LIST = 4;
    private final int TRY_AGAIN = 5;
    private boolean loading = false;
    private ProgressBar loadMoreProg;
    private int pageNum = 1;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tech, container, false);
        reportList = view.findViewById(R.id.rv_reports);
        tvCurrentState = view.findViewById(R.id.tv_no_connection);
        progressBarMain = view.findViewById(R.id.progress_circular);
        cuuStatusLayout = view.findViewById(R.id.no_connection_layout);
        loadMoreProg = view.findViewById(R.id.load_more);
        Button btnTryAgain = view.findViewById(R.id.btr_try_again);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        reportList.setLayoutManager(layoutManager);
        adapter = new ReportsAdapter(getActivity(), new ArrayList<TechReportClass>());
        reportList.setAdapter(adapter);
        final SwipeRefreshLayout pullRefresh = view.findViewById(R.id.swip_refresh);
        pullRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!loading) {
                    pageNum = 1;
                    buildUrl();
                    refresh();
                }
                pullRefresh.setRefreshing(false);
            }
        });
        reportList.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!loading && !recyclerView.canScrollVertically(View.FOCUS_DOWN)) {
                    loading = true;
                    loadMore();
                    loadMoreProg.setVisibility(View.VISIBLE);
                }
            }
        });
        btnTryAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
            }
        });

        loadReports();
        return view;
    }

    private boolean isInternetConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(getActivity().CONNECTIVITY_SERVICE);
        NetworkInfo status = connectivityManager.getActiveNetworkInfo();
        if (status != null && status.isConnectedOrConnecting())
            return true;
        return false;
    }

    private void loadMore() {
        pageNum++;
        buildUrl();
        getActivity().getSupportLoaderManager().restartLoader(WEATHER_LOADER, null, this);
    }

    private void loadReports() {
        try {
            if (isInternetConnected()) {
                showResponseStatus(LOAD_PROGRESS);
                getActivity().getSupportLoaderManager().initLoader(WEATHER_LOADER, null, this);
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
        String url = buildUrl();
        return new TechReportLoader(getActivity(), url);
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
            if (loading) {
                //          makeToast(getActivity().getString(R.string.noMoreReports));
            } else
                showResponseStatus(NO_RESULTS);
        } else {
//            makeToast(getActivity().getString(R.string.Load_Done));
            if (!loading) {
                adapter.clear();
            } else {
                loadMoreProg.setVisibility(View.GONE);
            }
            adapter.addAll((ArrayList<TechReportClass>) data);
            adapter.notifyDataSetChanged();
            showResponseStatus(SHOW_LIST);
        }
        loading = false;
    }

    /**
     * to force the loader to fetch new data while running the application
     */
    private void refresh() {
        if (isInternetConnected()) {
            makeToast(getActivity().getString(R.string.refresh));
            showResponseStatus(LOAD_PROGRESS);
            getActivity().getSupportLoaderManager().restartLoader(WEATHER_LOADER, null, this);
        } else showResponseStatus(NO_CONNECTION);
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



    private void makeToast(String message) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
        toast.setText(message);
        toast.show();
    }

    public String buildUrl() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String orderBy = sharedPrefs.getString(getString(R.string.pref_list_order_by_key),
                getString(R.string.pref_order_by_default));
        Uri uriBase = Uri.parse(url);
        Uri.Builder builder = uriBase.buildUpon();
        builder.appendQueryParameter("section", "weather")
                .appendQueryParameter("order-by", orderBy)
                .appendQueryParameter("page", String.valueOf(pageNum))
                .appendQueryParameter("show-fields", "thumbnail")
                .appendQueryParameter("show-tags", "contributor")
                .appendQueryParameter("api-key", "eda90898-9281-4d8c-a6c0-d868cac47caa");
        return builder.build().toString();
    }

}
