package com.kankana.myapplication.presentation;

import android.os.AsyncTask;
import android.util.Log;

import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.FlickrException;
import com.googlecode.flickrjandroid.photos.Photo;
import com.googlecode.flickrjandroid.photos.SearchParameters;
import com.kankana.myapplication.model.IResultListener;
import com.kankana.myapplication.model.ServerHelper;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by kankana on 5/17/2015.
 */
public class SearchPhotosAsyncTask extends AsyncTask<Void, Void, List<Photo>> {

    static final String TAG = "SearchPhotosAsyncTask";
    static final int GRIDVIEWS_PER_PAGE = 20;

    static final Set<String> EXTRAS = new HashSet<String>();

    static {
        EXTRAS.add("owner_name");
        EXTRAS.add("url_l");
        EXTRAS.add("url_m");
        EXTRAS.add("original_format");
        EXTRAS.add("views");
        EXTRAS.add("date_taken");
        EXTRAS.add("description");
        EXTRAS.add("tags");
    }

    private int mPage;
    String mSearchTerm;
    private Exception mException;
    private final IResultListener mListener;
    Flickr f;

    public SearchPhotosAsyncTask(IResultListener listener,
                                 String searchTerm, int page) {
        mPage = page;
        mSearchTerm = searchTerm;
        mListener = listener;
        f = ServerHelper.getInstance().getFlickr();
    }


    @Override
    protected List<Photo> doInBackground(Void... arg0) {
        SearchParameters sp = new SearchParameters();
        sp.setExtras(EXTRAS);
        Log.i(TAG, "Search tag is " + mSearchTerm);
        if (mSearchTerm != null && !mSearchTerm.isEmpty()) {
            try {
                sp.setText(mSearchTerm);
                sp.setSort(SearchParameters.RELEVANCE);
                return ServerHelper.getInstance().getPhotosInterface().search(sp, GRIDVIEWS_PER_PAGE, mPage);

            } catch (FlickrException flickrEx) {
                Log.e(TAG, flickrEx.getErrorMessage());
                mException = flickrEx;
            } catch (Exception e) {
                e.printStackTrace();
                mException = e;
            }
        } else {
            try {
                /* Get the recent public photos
                *  Returns the list of interesting photos for the most recent day or a user-specified date.
                */
                Date day = null;
                return ServerHelper.getInstance().getInterestingInterface().getList(day, EXTRAS, GRIDVIEWS_PER_PAGE, mPage);

            } catch (FlickrException flickrEx) {
                Log.e(TAG, flickrEx.getErrorMessage());
                mException = flickrEx;
            } catch (Exception e) {
                e.printStackTrace();
                mException = e;
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<Photo> result) {
        if (result == null) {
            Log.e(TAG, "Error: null photo list");
            result = Collections.EMPTY_LIST;
        }
        mListener.resultsUpdated(result, mException);
    }

}
