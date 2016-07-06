package com.example.android.newsapp;

import android.text.Html;

/**
 * Created by IrvinShandy on 7/1/16.
 */
public class Article {
    private String mTitle;
    private String mSnippet;
    private String mThumbnailUrl;
    private String mWebUrl;

    public Article(String title, String snippet, String thumbnailUrl, String webUrl) {
        mTitle = stripHtml(title);
        mSnippet = stripHtml(snippet);
        mThumbnailUrl = thumbnailUrl;
        mWebUrl = webUrl;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getSnippet() {
        return mSnippet;
    }

    public String getThumbnailUrl() {
        return mThumbnailUrl;
    }

    public String getWebUrl() {
        return mWebUrl;
    }

    public String stripHtml(String html) {
        return Html.fromHtml(html).toString();
    }
}
