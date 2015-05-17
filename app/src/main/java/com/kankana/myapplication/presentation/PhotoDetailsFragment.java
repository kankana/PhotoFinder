package com.kankana.myapplication.presentation;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.googlecode.flickrjandroid.photos.Photo;
import com.googlecode.flickrjandroid.photos.Size;
import com.googlecode.flickrjandroid.tags.Tag;
import com.kankana.myapplication.R;
import com.squareup.picasso.Picasso;

import java.util.Collection;

/**
 * Created by kankana on 5/17/2015.
 */
public class PhotoDetailsFragment extends Fragment {

    private int mNum;
    private Photo mBasePhoto;
    ViewGroup mLayout;
    ImageView mImageView;
    TextView mTextViewTitle, mTextViewAuthor, mTextViewDescription, mTextViewViews, mTextViewTags, mTextViewDateTaken, mTextViewImageFormat;
    PhotoDetailActivity mActivity;

    private static final String TAG = "PhotoDetailsFragment";

    public static PhotoDetailsFragment newInstance(Photo photo, int num) {
        PhotoDetailsFragment photoFragment = new PhotoDetailsFragment();
        photoFragment.mBasePhoto = photo;
        Bundle args = new Bundle();
        args.putInt("num", num);
        photoFragment.setArguments(args);

        return photoFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mNum = savedInstanceState.getInt("mNum", 0);
            mBasePhoto = (Photo) savedInstanceState.getSerializable(mNum + "_basePhoto");
        }
        mActivity = (PhotoDetailActivity) getActivity();
        setRetainInstance(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        /* Update our reference to the activity as it may have changed */
        mActivity = (PhotoDetailActivity) getActivity();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("mNum", mNum);
        savedInstanceState.putSerializable(mNum + "_basePhoto", mBasePhoto);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mLayout = (RelativeLayout) inflater.inflate(
                R.layout.photo_details_fragment, container, false);
        mImageView = (ImageView) mLayout.findViewById(R.id.image);
        mTextViewTitle = (TextView) mLayout.findViewById(R.id.textViewTitle);
        mTextViewAuthor = (TextView) mLayout.findViewById(R.id.tv_author);
        mTextViewDescription = (TextView) mLayout.findViewById(R.id.tv_description);
        mTextViewTags = (TextView) mLayout.findViewById(R.id.tv_tags);
        mTextViewViews = (TextView) mLayout.findViewById(R.id.tv_viewCount);
        mTextViewDateTaken = (TextView) mLayout.findViewById(R.id.tv_date_taken);
        mTextViewImageFormat = (TextView)mLayout.findViewById(R.id.tv_original_format);

        renderPhoto();
        return mLayout;
    }


    private void renderPhoto() {
        /* Fetch the main image */
        if (mBasePhoto != null) {
            Size size = mBasePhoto.getLargeSize();
            if (size != null) {
                Log.i(TAG, "get flickr large url");
                Picasso.with(mActivity).load(mBasePhoto.getLargeUrl()).into(mImageView, null);
            } else {
                Log.i(TAG, "medium url");
            /* No large size available, fall back to medium */
                Picasso.with(mActivity).load(mBasePhoto.getMediumUrl()).into(mImageView, null);
            }
            mTextViewTitle.setText(getResources().getString(R.string.str_imageTitle, mBasePhoto.getTitle()));
            mTextViewAuthor.setText(getResources().getString(R.string.str_author, mBasePhoto.getOwner().getUsername()));
            mTextViewDescription.setText(getString(R.string.str_description) + Html.fromHtml(mBasePhoto.getDescription())/*"getResources().getString(R.string.str_description,mBasePhoto.getDescription())*/);
            mTextViewViews.setText(getResources().getString(R.string.str_views, mBasePhoto.getViews()));
            mTextViewDateTaken.setText(getString(R.string.str_dateTaken, mBasePhoto.getDateTaken()));
            mTextViewImageFormat.setText(getString(R.string.str_original_format,mBasePhoto.getOriginalFormat()));

            /* Logic to get the photo tags and set in ui */
            StringBuilder tags = new StringBuilder();
            final Collection<Tag> allTags = mBasePhoto.getTags();
            int count = 0;
            for (Tag t : allTags) {
                tags.append(t.getValue());
                if (count < allTags.size() - 1) {
                    tags.append('-');
                }
                count++;
            }
            if (tags.toString().trim().isEmpty()) {
                mTextViewTags.setText("Tags: No tags");
            } else {
                mTextViewTags.setText(getResources().getString(R.string.str_tags, tags.toString()));
            }

        } else {
            Log.e(TAG, "displayImage: mBasePhoto is null");
        }
    }
}
