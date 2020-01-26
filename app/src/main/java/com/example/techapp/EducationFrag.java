package com.example.techapp;


import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
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
public class EducationFrag extends Fragment implements LoaderManager.LoaderCallbacks<List<TechReportClass>> {

    private String CHANNEL_ID = "news exist";

    public EducationFrag() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    private Toast toast;
    private TextView tvCurrentState;
    private ProgressBar progressBarMain;
    private ReportsAdapter adapter;
    private LinearLayout cuuStatusLayout;
    private RecyclerView reportList;
    private String url = "https://content.guardianapis.com/search?";
    private final int LOAD_PROGRESS = 1;
    private final int NO_RESULTS = 2;
    private final int NO_CONNECTION = 3;
    private final int SHOW_LIST = 4;
    private final int TRY_AGAIN = 5;
    private boolean loading = false;
    private ProgressBar loadMoreProg;
    private int pageNum = 1;
    private final int EDU_LOADER = 4;

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

    private void showNotification(NotificationCompat.Builder builder) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getActivity());

        // notificationId is a unique int for each notification that you must define
        int notificationId = 3;
        notificationManager.notify(notificationId, builder.build());

    }

    private NotificationCompat.Builder createNotification(String title, String content, int icon) {
        Intent intent = new Intent(getActivity(), AlertDialog.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getActivity(), 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity(), CHANNEL_ID)
                .setSmallIcon(icon)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        return builder;
    }

    private void loadMore() {
        pageNum++;
        buildUrl();
        getActivity().getSupportLoaderManager().restartLoader(EDU_LOADER, null, this);
    }

    private void loadReports() {
        try {
            if (isInternetConnected()) {
                showResponseStatus(LOAD_PROGRESS);
                getActivity().getSupportLoaderManager().initLoader(EDU_LOADER, null, this);
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
                //      makeToast(getContext().getString(R.string.noMoreReports));
            } else
                showResponseStatus(NO_RESULTS);
        } else {
            //    makeToast(getContext().getString(R.string.Load_Done));
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
            makeToast(getString(R.string.refresh));
            showResponseStatus(LOAD_PROGRESS);
            getActivity().getSupportLoaderManager().restartLoader(EDU_LOADER, null, this);
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

//    @Override
//    public void onItemClickListener(TechReportClass currReport) {
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        intent.setData(Uri.parse(currReport.getmWebUrl()));
//        startActivity(intent);
//    }

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
        builder.appendQueryParameter("section", "education")
                .appendQueryParameter("order-by", orderBy)
                .appendQueryParameter("page", String.valueOf(pageNum))
                .appendQueryParameter("show-fields", "thumbnail")
                .appendQueryParameter("show-tags", "contributor")
                .appendQueryParameter("api-key", "eda90898-9281-4d8c-a6c0-d868cac47caa");
        return builder.build().toString();
    }

}
