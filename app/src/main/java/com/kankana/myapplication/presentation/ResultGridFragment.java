package com.kankana.myapplication.presentation;

import android.app.Fragment;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.commonsware.cwac.endless.EndlessAdapter;
import com.googlecode.flickrjandroid.photos.Photo;
import com.kankana.myapplication.model.IResultListener;
import com.kankana.myapplication.R;
import com.kankana.myapplication.model.ServerHelper;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kankana on 5/17/2015.
 */
public class ResultGridFragment extends Fragment implements IResultListener {

    MainActivity mActivity;
    private EndlessGridAdapter mAdapter;
    ViewGroup mLayout;
    ViewGroup mNoConnectionLayout;
    private final List<Photo> mPhotos = new ArrayList<Photo>();
    static String mSearchQuery = "";
    GridView mGridView;

    boolean mMorePages = true;
    ViewGroup mNoResultsLayout;
    int mPage = 1;
    private static final String KEY_SEARCH_QUERY =
            "com.kankana.KEY_SEARCH_QUERY";

    public static ResultGridFragment newInstance(String searchTerm) {
        ResultGridFragment fragment =
                new ResultGridFragment();
        ResultGridFragment.mSearchQuery = searchTerm;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        setRetainInstance(shouldRetainInstance());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mLayout = (RelativeLayout) inflater.inflate(R.layout.gridview_activity, container,
                false);
        mNoConnectionLayout = (ViewGroup) mLayout.findViewById(R.id.conn_err_layout);
        mNoResultsLayout = (ViewGroup) mLayout.findViewById(R.id.search_failed_layout);
        mGridView = (GridView) mLayout.findViewById(R.id.gridLayout);
        mAdapter = new EndlessGridAdapter(mPhotos);
        mAdapter.setRunInBackground(false);
        mGridView.setAdapter(mAdapter);

        mGridView.setOnItemClickListener(new GridView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                /* Launch the detail view of the image along with other image list so that user can scroll*/
                PhotoDetailActivity.launchPhotoDetail(mActivity, mPhotos, position);
            }
        });
        return mLayout;
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putString(KEY_SEARCH_QUERY, mSearchQuery);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null && "".equals(mSearchQuery)) {
            String searchQuery = savedInstanceState.getString(
                    KEY_SEARCH_QUERY);
            if (searchQuery != null) {
                mSearchQuery = searchQuery;
            } else {
                Log.e("onActivityCreated", "No searchquery found in savedInstanceState");
            }
        }
    }


    @Override
    public void onResume() {
        super.onResume();

        /* Update our reference to the activity as it may have changed */
        mActivity = (MainActivity) getActivity();
        startTask(mPage);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) mActivity.getSystemService(getActivity().CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();


    }

    private void startTask(int page) {

        if(!isNetworkAvailable()){
            mNoConnectionLayout.setVisibility(View.VISIBLE);
            mGridView.setVisibility(View.GONE);
            mLayout.findViewById(R.id.tv).setVisibility(View.GONE);
            return;
        }
        getActivity().setProgressBarIndeterminateVisibility(Boolean.TRUE);
        if(!mSearchQuery.isEmpty()) {
            ((TextView) mLayout.findViewById(R.id.tv)).setText(getResources().getString(R.string.str_tag_search_header, mSearchQuery));
        }
        SearchPhotosAsyncTask mTask = new SearchPhotosAsyncTask(this, mSearchQuery, page);
        mTask.execute();
    }


    protected boolean shouldRetainInstance() {
        return true;
    }

    @Override
    public void resultsUpdated(List<Photo> photos,Exception e) {

        mActivity.setProgressBarIndeterminateVisibility(Boolean.FALSE);

        if (ServerHelper.getInstance().handleFlickrUnavailable(mActivity,e)) {
            return;
        }
        if (photos == null) {
            mNoConnectionLayout.setVisibility(View.VISIBLE);
            mGridView.setVisibility(View.GONE);
            return;
        }
        if (photos.isEmpty()) {
            mMorePages = false;
        }
        mNoConnectionLayout.setVisibility(View.GONE);
        mGridView.setVisibility(View.VISIBLE);
        //checkForNewPhotos(photos);
        mPhotos.addAll(photos);
        mAdapter.onDataReady();
        if (photos.isEmpty()) {
            /* If first page (2 as mPage will have already been incremented),
             * and results are empty, show no search results layout */
            if (mPage == 2) {
                mNoResultsLayout.setVisibility(View.VISIBLE);
                mGridView.setVisibility(View.GONE);
            }
        }
    }

    class EndlessGridAdapter extends EndlessAdapter {

        public EndlessGridAdapter(List<Photo> list) {
            super(new GridAdapter(list));
        }

        @Override
        protected boolean cacheInBackground() throws Exception {
            //return true;
            return ResultGridFragment.this.cacheInBackground();
        }

        @Override
        protected void appendCachedData() {
        }

        @Override
        protected View getPendingView(ViewGroup parent) {
            return new View(mActivity);
        }
    }

    class GridAdapter extends ArrayAdapter<Photo> {

        public GridAdapter(List<Photo> items) {
            super(getActivity(), R.layout.grid_item,
                    items);
        }

        @Override
        public View getView(final int position, View convertView,
                            ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                convertView = mActivity.getLayoutInflater().inflate(
                        R.layout.grid_item, null);
                holder = new ViewHolder();
                holder.image = (ImageView) convertView.findViewById(
                        R.id.image_item);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final Photo photo = (Photo) mAdapter.getItem(position);
            String thumbnailUrl = photo.getLargeSquareUrl();

            Picasso.with(mActivity).load(thumbnailUrl).into(holder.image);
            return convertView;
        }
    }

    public static class ViewHolder {

        public ImageView image;
    }

    boolean cacheInBackground() {
        ++mPage;
        startTask(mPage);
        return mMorePages;
    }

}