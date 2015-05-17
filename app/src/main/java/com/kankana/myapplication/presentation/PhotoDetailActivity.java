package com.kankana.myapplication.presentation;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.googlecode.flickrjandroid.photos.Photo;
import com.kankana.myapplication.R;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by kankana on 5/17/2015.
 */
public class PhotoDetailActivity extends FragmentActivity {

    private List<Photo> mPhotos = new ArrayList<Photo>();
    private static final String TAG = "PhotoDetail";
    private ViewPager mPager;

    public static final String KEY_PHOTO_LIST_FILE =
            "com.kankana.KEY_PHOTO_LIST_FILE";
    public static final String PHOTO_LIST_FILE =
            "Photolist.json";
    public static final String KEY_START_INDEX =
            "com.kankana.KEY_START_INDEX";
    private static final String KEY_CURRENT_INDEX =
            "com.kankana.KEY_CURRENT_INDEX";
    public static final String ACTION_VIEW_PHOTOLIST =
            "android.intent.action.ACTION_VIEW_PHOTOLIST";


    public static void launchPhotoDetail(Context context, List<Photo> photos, int index) {
        if (doMarshallObject(context, photos, PHOTO_LIST_FILE)) {
            Intent photoViewer = new Intent(context, PhotoDetailActivity.class);
            photoViewer.setAction(ACTION_VIEW_PHOTOLIST);
            photoViewer.putExtra(KEY_START_INDEX, index);
            photoViewer.putExtra(KEY_PHOTO_LIST_FILE, PHOTO_LIST_FILE);
            context.startActivity(photoViewer);
        } else {
            Log.e(TAG, "Error marshalling photo list");
        }
    }

    private static boolean doMarshallObject(Context ctx, Object o, String file) {
        Gson gson = new Gson();
        String json = gson.toJson(o);
        try {
            FileOutputStream fos = ctx.openFileOutput(
                    file, Context.MODE_PRIVATE);
            fos.write(json.getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_details_activity); //has view pager
        handleIntent(getIntent());
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        /* mPager may be null if activity is closed before initViewPager */
        if (mPager != null) {
            savedInstanceState.putInt(KEY_CURRENT_INDEX, mPager.getCurrentItem());
        }
        if (!doMarshallObject(this, mPhotos, PHOTO_LIST_FILE)) {
            Log.e(TAG, "onSaveInstanceState: Error marshalling mPhotos");
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (mPhotos.isEmpty()) {
            String json = loadJson(PHOTO_LIST_FILE);
            if (json.length() == 0) {
                Log.e(TAG, String.format("Error reading '%s'", PHOTO_LIST_FILE));
            } else {
                Type collectionType = new TypeToken<Collection<Photo>>() {
                }.getType();
                mPhotos = new Gson().fromJson(json, collectionType);
            }
        }

        int pagerIndex = savedInstanceState.getInt(KEY_CURRENT_INDEX, 0);
        initViewPager(pagerIndex);
    }

    private void initViewPager(int pagerIndex) {
        PhotoViewerPagerAdapter mAdapter = new PhotoViewerPagerAdapter(getSupportFragmentManager());
        //Fragment f = mAdapter.getItem(pagerIndex);
        mAdapter.onPageSelected(pagerIndex);
        mPager = (ViewPager) findViewById(R.id.details_pager);

        mPager.setAdapter(mAdapter);
        mPager.setOnPageChangeListener(mAdapter);
        mPager.setCurrentItem(pagerIndex);
        //mPager.setOffscreenPageLimit(2);
    }


    class PhotoViewerPagerAdapter extends FragmentStatePagerAdapter
            implements ViewPager.OnPageChangeListener {

        public PhotoViewerPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return PhotoDetailsFragment.newInstance(mPhotos.get(position),
                    position);
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }

        @Override
        public int getCount() {
            return mPhotos.size();
        }
    }


    private String loadJson(String file) {
        StringBuilder json = new StringBuilder();
        try {
            FileInputStream in = this.openFileInput(file);
            InputStreamReader inputStreamReader = new InputStreamReader(in);
            BufferedReader bufferedReader =
                    new BufferedReader(inputStreamReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                json.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json.toString();
    }


    private void handleIntent(Intent intent) {

        final int startIndex = intent.getIntExtra(KEY_START_INDEX, 0);
        if (intent.getAction().equals(ACTION_VIEW_PHOTOLIST)) {
            Log.i(TAG, "Inside ACTION_VIEW_PHOTOLIST of handleIntent ");
            String photoListFile = intent.getStringExtra(KEY_PHOTO_LIST_FILE);
            String json = loadJson(photoListFile);
            if (json.length() > 0) {
                Type collectionType = new TypeToken<Collection<Photo>>() {
                }.getType();
                mPhotos = new Gson().fromJson(json, collectionType);
                Log.i(TAG, "startindex of image = " + startIndex);
                initViewPager(startIndex);
            } else {
                Log.e(TAG, String.format("Error reading '%s'", photoListFile));
            }
        } else {
            Log.e(TAG, "Unknown intent action: " + intent.getAction());
        }
    }


}
