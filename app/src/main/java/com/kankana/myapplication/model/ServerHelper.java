package com.kankana.myapplication.model;

/**
 * Created by kankana on 5/17/2015.
 */

import android.content.Context;
import android.widget.Toast;

import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.FlickrException;
import com.googlecode.flickrjandroid.REST;
import com.googlecode.flickrjandroid.interestingness.InterestingnessInterface;
import com.googlecode.flickrjandroid.photos.PhotosInterface;
import com.kankana.myapplication.R;

import javax.xml.parsers.ParserConfigurationException;

public class ServerHelper {
    private static ServerHelper instance = null;
    private static final String API_KEY = "183f68dcc5d5045e25e42f30369c2922";
    private static final String API_SECRET = "7082f671b8f70fb4 ";
    static final String ERR_CODE_FLICKR_UNAVAILABLE = "105";
    private ServerHelper() {
    }

    public static ServerHelper getInstance() {
        if (instance == null) {
            instance = new ServerHelper();
        }

        return instance;
    }

    public Flickr getFlickr() {
        try {
            return new Flickr(API_KEY, API_SECRET, new REST());
        } catch (ParserConfigurationException e) {
            return null;
        }
    }

    public InterestingnessInterface getInterestingInterface() {
        Flickr f = getFlickr();
        if (f != null) {
            return f.getInterestingnessInterface();
        } else {
            return null;
        }
    }

    public PhotosInterface getPhotosInterface() {
        Flickr f = getFlickr();
        if (f != null) {
            return f.getPhotosInterface();
        } else {
            return null;
        }
    }

    public boolean handleFlickrUnavailable(Context context, Exception e) {
        if (e != null && e instanceof FlickrException) {
            if (((FlickrException) e).getErrorCode().equals(
                    ERR_CODE_FLICKR_UNAVAILABLE)) {
                Toast.makeText(context, context.getString(R.string.server_down),
                        Toast.LENGTH_LONG).show();
                return true;
            }
        }
        return false;
    }

}
