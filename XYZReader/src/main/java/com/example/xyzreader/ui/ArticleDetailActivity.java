package com.example.xyzreader.ui;

import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class ArticleDetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {
    private Unbinder unbinder;

    private Cursor cursor;
    private long startId;

    @BindView(R.id.view_pager)
    ViewPager viewPager;

    private MyPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail);

        unbinder = ButterKnife.bind(this);

        DebugLog.logMethod();

        getSupportLoaderManager().initLoader(0, null, this);

        pagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        viewPager.setPageMargin(
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                1,
                getResources().getDisplayMetrics())
        );
        viewPager.setPageMarginDrawable(new ColorDrawable(0x22000000));

        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (cursor != null) {
                    cursor.moveToPosition(position);
                    startId = cursor.getLong(ArticleLoader.Query._ID);
                } else {
                    startId = -1;
                }
            }
        });

        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getData() != null) {
                startId = ItemsContract.Items.getItemId(getIntent().getData());
            }
        }
    }

    @Override
    protected void onDestroy() {
        unbinder.unbind();
        super.onDestroy();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        this.cursor = cursor;
        pagerAdapter.notifyDataSetChanged();

        // Select the start ID
        if (startId > 0) {
            this.cursor.moveToFirst();
            // TODO: optimize
            while (!this.cursor.isAfterLast()) {
                if (this.cursor.getLong(ArticleLoader.Query._ID) == startId) {
                    final int position = this.cursor.getPosition();
                    viewPager.setCurrentItem(position, false);
                    break;
                }
                this.cursor.moveToNext();
            }
            startId = 0;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        cursor = null;
        pagerAdapter.notifyDataSetChanged();
    }

    private class MyPagerAdapter extends FragmentStatePagerAdapter {
        MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            cursor.moveToPosition(position);
            return ArticleDetailFragment.newInstance(cursor.getLong(ArticleLoader.Query._ID));
        }

        @Override
        public int getCount() {
            return (cursor != null) ? cursor.getCount() : 0;
        }
    }
}
