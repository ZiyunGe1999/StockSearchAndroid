package com.example.stocks;

public class News {
    public final String source;
    public final Long datetime;
    public final String headline;
    public final String summary;
    public final String url;
    public final String image;

    public News(String source, Long datetime, String headline, String summary, String url, String image) {
        this.source = source;
        this.datetime = datetime;
        this.headline = headline;
        this.summary = summary;
        this.url = url;
        this.image = image;
    }
}
