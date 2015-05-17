package com.kankana.myapplication.model;

import com.googlecode.flickrjandroid.photos.Photo;

import java.util.List;

/**
 * Created by kankana on 5/17/2015.
 */
public interface IResultListener {
    void resultsUpdated(List<Photo> photos, Exception e);
}
