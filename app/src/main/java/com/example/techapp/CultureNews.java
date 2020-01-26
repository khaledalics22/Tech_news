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
public class CultureNews extends Fragment implements LoaderManager.LoaderCallbacks<List<TechReportClass>> {
    public static String cul_url = "https://content.guardianapis.com/search?";

    public CultureNews() {
        // Required empty public constructor
    }

    private int pageNum = 1;
    private Toast toast;
    private TextView tvCurrentState;
    private ProgressBar progressBarMain;
    private LinearLayout cuuStatusLayout;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private ReportsAdapter adapter;
    private List<TechReportClass> cul_reports = null;
    private ProgressBar loadMoreProg;
    private final int CUL_LOADER = 1;
    private final int LOAD_PROGRESS = 1;
    private final int NO_RESULTS = 2;
    private final int NO_CONNECTION = 3;
    private final int SHOW_LIST = 4;
    private final int TRY_AGAIN = 5;
    private boolean loading = false;
    private SwipeRefreshLayout pullRefresh;


    private void loadMore() {
        pageNum++;
        buildUrl(cul_url);
        getActivity().getSupportLoaderManager().restartLoader(CUL_LOADER, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tech, container, false);
        recyclerView = view.findViewById(R.id.rv_reports);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        adapter = new ReportsAdapter(getContext(), new ArrayList<TechReportClass>());
        recyclerView.setAdapter(adapter);
        loadMoreProg = view.findViewById(R.id.load_more);
        tvCurrentState = view.findViewById(R.id.tv_no_connection);
        progressBarMain = view.findViewById(R.id.progress_circular);
        cuuStatusLayout = view.findViewById(R.id.no_connection_layout);
        pullRefresh = view.findViewById(R.id.swip_refresh);
        pullRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!loading) {
                    pageNum = 1;
                    buildUrl(cul_url);
                    refresh();
                }
                pullRefresh.setRefreshing(false);

            }
        });
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!loading && !recyclerView.canScrollVertically(View.FOCUS_DOWN)) {
                    loadMoreProg.setVisibility(View.VISIBLE);
                    loading = true;
                    loadMore();
                }
            }
        });
        Button btnTryAgain = view.findViewById(R.id.btr_try_again);
        btnTryAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
            }
        });
        return view;
    }

    private void refresh() {
        if (isInternetConnected()) {
            makeToast(getString(R.string.refresh));
            showResponseStatus(LOAD_PROGRESS);
            getActivity().getSupportLoaderManager().restartLoader(CUL_LOADER, null, this);
        } else showResponseStatus(NO_CONNECTION);
    }

    private void showResponseStatus(int status) {
        switch (status) {
            case LOAD_PROGRESS:
                progressBarMain.setVisibility(View.VISIBLE);
                cuuStatusLayout.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
                break;
            case NO_RESULTS:
                tvCurrentState.setText(getString(R.string.no_results));
                cuuStatusLayout.setVisibility(View.VISIBLE);
                progressBarMain.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
                break;
            case NO_CONNECTION:
                tvCurrentState.setText(getString(R.string.no_connection));
                cuuStatusLayout.setVisibility(View.VISIBLE);
                progressBarMain.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
                break;
            case TRY_AGAIN:
                tvCurrentState.setText(getString(R.string.try_again));
                cuuStatusLayout.setVisibility(View.VISIBLE);
                progressBarMain.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
                break;
            case SHOW_LIST:
                cuuStatusLayout.setVisibility(View.GONE);
                progressBarMain.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        loadReports();
    }

    private void loadReports() {
        try {
            if (isInternetConnected()) {
                showResponseStatus(LOAD_PROGRESS);
                getActivity().getSupportLoaderManager().initLoader(CUL_LOADER, null, this);
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
        String url = buildUrl(cul_url);
        return new TechReportLoader(getActivity().getApplicationContext(), url);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<TechReportClass>> loader, List<TechReportClass> data) {
        if (data == null || data.isEmpty()) {
            showResponseStatus(TRY_AGAIN);
        }
        updateUI(data);
    }

    private void updateUI(List<TechReportClass> data) {
        if (data == null || data.size() == 0) {
            if (loading) {
      //          makeToast(getString(R.string.noMoreReports));
            } else
                showResponseStatus(NO_RESULTS);
        } else {
    //            makeToast(getString(R.string.Load_Done));
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

    private void makeToast(String message) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
        toast.setText(message);
        toast.show();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<TechReportClass>> loader) {
        cul_reports.clear();
    }

    private boolean isInternetConnected() {
        ConnectivityManager manager = (ConnectivityManager) getActivity().getSystemService(getContext().CONNECTIVITY_SERVICE);
        NetworkInfo status = manager.getActiveNetworkInfo();
        if (status != null && status.isConnectedOrConnecting())
            return true;
        return false;
    }

    private String buildUrl(String url) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String orderBy = sharedPrefs.getString(getString(R.string.pref_list_order_by_key),
                getString(R.string.pref_order_by_default));
        Uri uriBase = Uri.parse(url);
        Uri.Builder builder = uriBase.buildUpon();
        builder.appendQueryParameter("section", "culture")
                .appendQueryParameter("order-by", orderBy)
                .appendQueryParameter("page", String.valueOf(pageNum))
                .appendQueryParameter("show-fields", "thumbnail")
                .appendQueryParameter("show-tags", "contributor")
                .appendQueryParameter("api-key", "eda90898-9281-4d8c-a6c0-d868cac47caa");
        return builder.build().toString();
    }
}
