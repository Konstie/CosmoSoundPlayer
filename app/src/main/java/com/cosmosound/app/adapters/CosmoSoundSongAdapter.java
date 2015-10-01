package com.cosmosound.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.cosmosound.app.R;
import com.cosmosound.app.entity.MusicTrack;

import java.util.ArrayList;

public class CosmoSoundSongAdapter extends BaseAdapter implements Filterable {

    private ArrayList<MusicTrack> originalTracks = new ArrayList<>();
    private ArrayList<MusicTrack> tracks;
    private LayoutInflater inflater;
    private MusicTrackFilter mFilter;

    public CosmoSoundSongAdapter(ArrayList<MusicTrack> tracks, Context context) {
        this.tracks = tracks;
        this.inflater = LayoutInflater.from(context);
        cloneItems(tracks);
    }

    protected void cloneItems(ArrayList<MusicTrack> tracks) {
        for (MusicTrack track : tracks) {
            this.originalTracks.add(track);
        }
    }

    @Override
    public int getCount() {
        return tracks != null ? tracks.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        MusicTrack track;
        track = tracks != null ? tracks.get(position) : null;
        return track;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.song_item, null);

        ViewHolder holder = new ViewHolder(convertView);

        MusicTrack keyTrack = tracks.get(position);

        holder.playIcon.invalidate();
        holder.title.setText(keyTrack.getTrackTitle());
        holder.author.setText(keyTrack.getTrackAuthor());
        holder.album.setText(keyTrack.getTrackAlbum());
        holder.duration.setText(keyTrack.getTrackDuration());

        convertView.setTag(position);

        return convertView;
    }

    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new MusicTrackFilter();
        }
        return mFilter;
    }

    public static class ViewHolder {
        public final ImageView playIcon;
        public final TextView title;
        public final TextView author;
        public final TextView album;
        public final TextView duration;

        public ViewHolder(View view) {
            playIcon = (ImageView) view.findViewById(R.id.play_icon);
            title = (TextView) view.findViewById(R.id.title);
            author = (TextView) view.findViewById(R.id.author);
            album = (TextView) view.findViewById(R.id.album);
            duration = (TextView) view.findViewById(R.id.duration);
        }
    }

    private class MusicTrackFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if (constraint == null || constraint.length() == 0) {
                results.values = originalTracks;
                results.count = originalTracks.size();
            } else {
                String prefixString = constraint.toString().toLowerCase();
                final ArrayList<MusicTrack> filteredItems = new ArrayList<>();
                final ArrayList<MusicTrack> localItems = new ArrayList<>();
                localItems.addAll(originalTracks);

                for (final MusicTrack track : localItems) {
                    final String trackTitle = track.getTrackTitle().toLowerCase();
                    final String trackAuthor = track.getTrackAuthor().toLowerCase();
                    final String trackAlbum = track.getTrackAlbum();

                    if (trackTitle.startsWith(prefixString)) {
                        filteredItems.add(track);
                    } else if (trackAuthor.startsWith(prefixString)) {
                        filteredItems.add(track);
                    } else if (trackAlbum.startsWith(prefixString)) {
                        filteredItems.add(track);
                    }
                }

                results.values = filteredItems;
                results.count = filteredItems.size();
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            final ArrayList<MusicTrack> localItems = (ArrayList<MusicTrack>) results.values;
            tracks.clear();
            notifyDataSetChanged();
            for (MusicTrack track : localItems) {
                tracks.add(track);
            }
        }
    }
}
