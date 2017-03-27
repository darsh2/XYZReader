package com.example.xyzreader.ui;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;

/**
 * Created by darshan on 26/3/17.
 */

public class ArticleListAdapter extends RecyclerView.Adapter<ArticleListAdapter.ViewHolder> {
    private Context context;
    private Cursor cursor;

    private ArticleClickListener articleClickListener;

    public ArticleListAdapter(AppCompatActivity activity) {
        this.context = activity.getApplicationContext();
        this.articleClickListener = (ArticleClickListener) activity;
    }

    public void setCursor(Cursor cursor) {
        this.cursor = cursor;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return cursor == null ? 0 : cursor.getCount();
    }

    @Override
    public long getItemId(int position) {
        cursor.moveToPosition(position);
        return cursor.getLong(ArticleLoader.Query._ID);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_article, parent, false);
        final ViewHolder viewHolder = new ViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (articleClickListener != null) {
                    articleClickListener.onArticleClick(
                            getItemId(viewHolder.getAdapterPosition())
                    );
                }
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        cursor.moveToPosition(position);
        holder.titleView.setText(cursor.getString(ArticleLoader.Query.TITLE));
        holder.authorView.setText(String.format(
                context.getString(R.string.by_author),
                cursor.getString(ArticleLoader.Query.AUTHOR)
        ));
        holder.dateView.setText(DateUtils.getRelativeTimeSpanString(
                cursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                System.currentTimeMillis(),
                DateUtils.HOUR_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_ALL).toString()
        );

        holder.thumbnailView.setAspectRatio(cursor.getFloat(ArticleLoader.Query.ASPECT_RATIO));
        Glide.with(holder.thumbnailView.getContext())
                .load(cursor.getString(ArticleLoader.Query.THUMB_URL))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .crossFade()
                .centerCrop()
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model,
                                                   Target<GlideDrawable> target,
                                                   boolean isFromMemoryCache, boolean isFirstResource) {
                        Bitmap bitmap = ((GlideBitmapDrawable) resource.getCurrent()).getBitmap();
                        Palette palette = Palette.generate(bitmap);
                        int defaultColor = 0xFF333333;
                        int color = palette.getDarkMutedColor(defaultColor);
                        holder.cardView.setBackgroundColor(color);
                        return false;
                    }
                })
                .into(holder.thumbnailView);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        DynamicHeightNetworkImageView thumbnailView;
        TextView titleView;
        TextView authorView;
        TextView dateView;

        ViewHolder(View view) {
            super(view);
            this.cardView = (CardView) view;
            thumbnailView = (DynamicHeightNetworkImageView) view.findViewById(R.id.thumbnail);
            titleView = (TextView) view.findViewById(R.id.article_title);
            authorView = (TextView) view.findViewById(R.id.article_author);
            dateView = (TextView) view.findViewById(R.id.article_date);
        }
    }

    public interface ArticleClickListener {
        void onArticleClick(long articleId);
    }
}
