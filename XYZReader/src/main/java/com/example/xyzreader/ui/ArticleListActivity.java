package com.example.xyzreader.ui;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;
import com.example.xyzreader.data.UpdaterService;
import com.example.xyzreader.remote.UpdateEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class ArticleListActivity extends AppCompatActivity implements
        SwipeRefreshLayout.OnRefreshListener,
        LoaderManager.LoaderCallbacks<Cursor>,
        ArticleListAdapter.ArticleClickListener {
    private static final int LOADER_ID = 1001;

    private Unbinder unbinder;

    @BindView(R.id.coordinator_layout)
    CoordinatorLayout coordinatorLayout;

    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private ArticleListAdapter articleListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_list);
        DebugLog.logMethod();

        unbinder = ButterKnife.bind(this);
        EventBus.getDefault().register(this);

        swipeRefreshLayout.setOnRefreshListener(this);

        int columnCount = getResources().getInteger(R.integer.list_column_count);
        StaggeredGridLayoutManager staggeredGridLayoutManager =
                new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);

        articleListAdapter = new ArticleListAdapter(this);
        recyclerView.setAdapter(articleListAdapter);

        getSupportLoaderManager().initLoader(LOADER_ID, null, this);
        onRefresh();
    }

    @Override
    public void onRefresh() {
        DebugLog.logMethod();
        startService(new Intent(this, UpdaterService.class));
    }

    @Override
    protected void onDestroy() {
        DebugLog.logMethod();
        swipeRefreshLayout.setOnRefreshListener(null);
        getLoaderManager().destroyLoader(LOADER_ID);
        EventBus.getDefault().unregister(this);
        unbinder.unbind();
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateEvent(UpdateEvent updateEvent) {
        swipeRefreshLayout.setRefreshing(false);
        if (updateEvent.isSuccessful()) {
            return;
        }

        Snackbar snackbar = Snackbar.make(coordinatorLayout, getString(R.string.db_error), Snackbar.LENGTH_SHORT);
        snackbar.setAction("OK", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        snackbar.show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        DebugLog.logMethod();
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        DebugLog.logMethod();
        DebugLog.logMessage("Cursor loader id: " + cursorLoader.getId());
        if (cursorLoader.getId() == LOADER_ID) {
            articleListAdapter.setCursor(cursor);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        DebugLog.logMethod();
        articleListAdapter.setCursor(null);
    }

    @Override
    public void onArticleClick(long articleId) {
        DebugLog.logMethod();
        Intent intent = new Intent(this, ArticleDetailActivity.class);
        intent.setData(ItemsContract.Items.buildItemUri(articleId));
        intent.setAction(Intent.ACTION_VIEW);
        startActivity(intent);
    }
}
