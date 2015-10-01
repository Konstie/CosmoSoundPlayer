package com.cosmosound.app.entity;

public class MusicTrack {
    private long trackID;
    private String trackTitle;
    private String trackAuthor;
    private String trackAlbum;
    private String trackDuration;

    public MusicTrack() {}

    public MusicTrack(long id, String trackTitle, String trackAuthor, String trackAlbum, String trackDuration) {
        this.trackID = id;
        this.trackTitle = trackTitle;
        this.trackAuthor = trackAuthor;
        this.trackAlbum = trackAlbum;
        this.trackDuration = trackDuration;
    }

    public long getTrackID() {
        return trackID;
    }

    public String getTrackTitle() {
        return trackTitle;
    }

    public String getTrackAuthor() {
        return trackAuthor;
    }

    public String getTrackAlbum() {
        return trackAlbum;
    }

    public String getTrackDuration() {
        return trackDuration;
    }
}
