package com.example.xyzreader.ui;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.Loader;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class ArticleDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "ArticleDetailFragment";

    public static final String ARG_ITEM_ID = "item_id";

    public static final int LOADER_ID = 1002;

    private Cursor cursor;
    private long itemId;

    private Unbinder unbinder;

    @BindView(R.id.content_main)
    CoordinatorLayout coordinatorLayout;

    @BindView(R.id.image_view_backdrop)
    ImageView imageView;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.share_fab)
    FloatingActionButton shareFAB;

    @BindView(R.id.article_title_date)
    TextView textViewTitleDate;

    @BindView(R.id.article_body)
    TextView textViewArticleBody;

    public ArticleDetailFragment() {
    }

    public static ArticleDetailFragment newInstance(long itemId) {
        DebugLog.logMethod();
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DebugLog.logMethod();

        /*
        To enable debugging in LoaderManager.
        Ref: http://stackoverflow.com/a/21709525
         */
        //LoaderManager.enableDebugLogging(true);

        if (getArguments() != null) {
            itemId = getArguments().getLong(ARG_ITEM_ID, -1);
        }
        if (savedInstanceState != null) {
            itemId = savedInstanceState.getLong(ARG_ITEM_ID, -1);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        DebugLog.logMethod();
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        DebugLog.logMethod();
        View view = inflater.inflate(R.layout.fragment_article_detail, container, false);
        unbinder = ButterKnife.bind(this, view);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DebugLog.logMethod();
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            }
        });

        return view;
    }

    @OnClick(R.id.share_fab)
    public void onShareFabClick() {
        DebugLog.logMethod();
        String articleBody = "";
        if (textViewArticleBody.getText() != null) {
            articleBody = textViewArticleBody.getText().toString();
        }
        if (TextUtils.isEmpty(articleBody)) {
            Snackbar.make(
                    coordinatorLayout,
                    getString(R.string.error_share_article),
                    Snackbar.LENGTH_LONG
            ).show();
            return;
        }

        startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                .setType("text/plain")
                .setText(articleBody)
                .getIntent(), getString(R.string.action_share)));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        DebugLog.logMethod();
        outState.putLong(ARG_ITEM_ID, itemId);
    }

    @Override
    public void onDestroyView() {
        DebugLog.logMethod();
        /*
        This is absolutely necessary to perform. On orientation change, the loader is not destroyed.
        Hence in the subsequent call to onActivityCreated, the same loader is reused. This reused
        loader on running the query yields no result. To prevent this, destroy the loader.
         */
        getLoaderManager().destroyLoader(LOADER_ID);

        cursor = null;
        unbinder.unbind();
        super.onDestroyView();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        DebugLog.logMethod();
        DebugLog.logMessage("Item id: " + itemId);
        textViewTitleDate.setText(getString(R.string.loading));
        return ArticleLoader.newInstanceForItemId(getActivity(), itemId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        DebugLog.logMethod();
        if (!isAdded()) {
            DebugLog.logMessage("Fragment is not added to it");
            if (cursor != null) {
                cursor.close();
            }
            return;
        }

        this.cursor = cursor;
        if (this.cursor == null
                || this.cursor.getCount() == 0
                || !this.cursor.moveToNext()) {
            DebugLog.logMessage("Error loading cursor from db");
            if (this.cursor != null) {
                DebugLog.logMessage("Count = 0");
                this.cursor.close();
            }
            textViewTitleDate.setText(getString(R.string.db_error));
            return;
        }

        updateViews();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        DebugLog.logMethod();
        cursor = null;
    }

    private void updateViews() {
        DebugLog.logMethod();

        Glide.with(imageView.getContext())
                .load(cursor.getString(ArticleLoader.Query.PHOTO_URL))
                .crossFade(1000)
                .into(imageView);

        /*
        Setting author's name as title since article title is too long to
        be completely visible in the toolbar.
         */
        toolbar.setTitle(String.format(
                getString(R.string.by_author),
                cursor.getString(ArticleLoader.Query.AUTHOR)
        ));

        textViewTitleDate.setText(Html.fromHtml(
                cursor.getString(ArticleLoader.Query.TITLE)
                + "<br>"
                + DateUtils.getRelativeTimeSpanString(
                        cursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                        System.currentTimeMillis(),
                        DateUtils.HOUR_IN_MILLIS,
                        DateUtils.FORMAT_ABBREV_ALL
                ).toString()
        ));
        textViewArticleBody.setText(Html.fromHtml(cursor.getString(ArticleLoader.Query.BODY)));
    }
}
