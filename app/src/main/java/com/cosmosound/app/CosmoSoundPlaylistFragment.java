package com.cosmosound.app;

import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.*;
import android.widget.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

public class CosmoSoundPlaylistFragment extends Fragment {

    public static int currentTrackDuration;
    public static String currentTrackInfo;
    public static String dataSource = "";

    private Handler updateHandler = new Handler();

    private int trackPosition;

    private EditText mFilterField;

    private CosmoSoundService csService;
    private Intent launchIntent;
    private boolean musicBound = false;

    private ArrayList<MusicTrack> tracks;
    private ListView trackList;
    private CosmoSoundSongAdapter adapter;

    private ActionBar actionBar;

    private ToggleButton mBtnPlayPause;

    private ImageView mBtnSkipNext;
    private ImageView mBtnSkipPrev;

    private static TextView mTrackStart;
    private static TextView mTrackEnd;
    private static TextView mTrackInfo;
    private static SeekBar mSeekbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tracklist, parent, false);

        actionBar = ((CosmoSoundPlaylistActivity) getActivity()).getSupportActionBar();

        assert actionBar != null;
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setIcon(R.mipmap.ic_launcher);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setCustomView(R.layout.search_filter_layout);

        mFilterField = (EditText) actionBar.getCustomView().findViewById(R.id.search_filter_field);

        trackList = (ListView) rootView.findViewById(R.id.tracklist);

        View controlsPlayback = rootView.findViewById(R.id.controllers);

        mBtnSkipPrev = (ImageView) controlsPlayback.findViewById(R.id.prev);
        mBtnSkipNext = (ImageView) controlsPlayback.findViewById(R.id.next);
        mBtnPlayPause = (ToggleButton) controlsPlayback.findViewById(R.id.imageView1);
        mBtnPlayPause.setEnabled(false);
        ImageView mBtnStop = (ImageView) controlsPlayback.findViewById(R.id.stop);
        ImageView mBtnAddFromSource = (ImageView) controlsPlayback.findViewById(R.id.source);
        mSeekbar = (SeekBar) controlsPlayback.findViewById(R.id.seekBar1);
        mTrackStart = (TextView) controlsPlayback.findViewById(R.id.startText);
        mTrackEnd = (TextView) controlsPlayback.findViewById(R.id.endText);
        mTrackInfo = (TextView) controlsPlayback.findViewById(R.id.track_info);

        mBtnPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tracks.size() > 0) {
                    playPause();
                }
            }
        });

        mBtnSkipPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                skipToPrev();
            }
        });

        mBtnSkipNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                skipToNext();
            }
        });

        mBtnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTrack();
            }
        });

        mBtnAddFromSource.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SourceBrowserActivity.class);
                startActivity(intent);
            }
        });

        return rootView;
    }

    public void getTrackList() {
        ContentResolver contentResolver = getActivity().getContentResolver();
        Uri songsUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Log.w("TAAAAAAAG", dataSource);
        String search = "%" + dataSource + "%"; // сюда передается статическая переменная с целевой папкой для воспр.
        Cursor tracksCursor = contentResolver.query(songsUri,
                null,
                MediaStore.Audio.Media.DATA + " like ? ",
                new String[] {search},
                null);

        if (tracksCursor != null && tracksCursor.moveToFirst()) {
            int id = tracksCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int title = tracksCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int author = tracksCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int album = tracksCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            int duration = tracksCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);

            do {
                long keyID = tracksCursor.getLong(id);
                String keyTitle = tracksCursor.getString(title);
                String keyAuthor = tracksCursor.getString(author);
                String keyAlbum = tracksCursor.getString(album);
                int durationInMillis = tracksCursor.getInt(duration);

                String keyDuration = CosmoSoundUtils.formatMilliseconds(durationInMillis);

                tracks.add(new MusicTrack(keyID, keyTitle, keyAuthor, keyAlbum, keyDuration));
            } while (tracksCursor.moveToNext());

            tracksCursor.close();
        }
    }

    private ServiceConnection csConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            CosmoSoundService.CosmoSoundMusicBinder binder = (CosmoSoundService.CosmoSoundMusicBinder) service;
            csService = binder.getService();
            csService.setTrackList(tracks);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        if (launchIntent == null) {
            launchIntent = new Intent(getActivity(), CosmoSoundService.class);
            getActivity().bindService(launchIntent, csConnection, Context.BIND_AUTO_CREATE);
            getActivity().startService(launchIntent);
        }
        mBtnPlayPause.setChecked(loadPrefs("playpause"));
    }

    @Override
    public void onResume() {
        super.onResume();

        if (tracks == null) {
            tracks = new ArrayList<>();
            getTrackList();
        }

        adapter = new CosmoSoundSongAdapter(tracks, getActivity());
        adapter.notifyDataSetChanged();
        trackList.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        trackList.setAdapter(adapter);

        trackList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                playTrack(position);
            }
        });

        TextWatcher filterTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                adapter.getFilter().filter(s);
                adapter.notifyDataSetChanged();
            }
        };
        mFilterField.addTextChangedListener(filterTextWatcher);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME);

        mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && csService != null) {
                    csService.seek(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public void playTrack(int position) { // воспроизведение трека при выборе из списка
        mBtnSkipPrev.setClickable(true);
        mBtnSkipNext.setClickable(true);
        mBtnPlayPause.setEnabled(true);
        mBtnPlayPause.setChecked(true);
        trackPosition = position;
        csService.setTrackPosition(trackPosition);
        csService.playTrack();
        trackList.setItemChecked(position, true);
        initControllers();
        mTrackEnd.setText(tracks.get(csService.getTrackPosition()).getTrackDuration());
        setTrackDetails();
    }

    private void initControllers() { // запустить обновление текущей позиции трека
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        setCurrentTrackDurationAndWait();
        updateHandler.postDelayed(updateTrackTime, 100);
    }

    public void playPause() {
        if (!csService.isPlaying()) {
            initControllers();
        }
        csService.playPause();
    }

    public void setTrackDetails() { // обновить информацию про текущую песню в плейлисте
        currentTrackInfo = "" + tracks.get(csService.getTrackPosition()).getTrackAuthor() + " — " +
                tracks.get(csService.getTrackPosition()).getTrackTitle() + " — " +
                tracks.get(csService.getTrackPosition()).getTrackAlbum();
        mTrackInfo.setText(currentTrackInfo);
    }

    public void skipToPrev() {
        csService.setTrackPosition(--trackPosition);
        if (csService.getTrackPosition() < 0) {
            csService.setTrackPosition(++trackPosition);
            Toast.makeText(getActivity(), "Playlist start reached!", Toast.LENGTH_SHORT).show();
        } else if (tracks.size() == 0) {
            mBtnSkipPrev.setClickable(false);
        } else {
            csService.playTrack();
            setCurrentTrackDurationAndWait();
            mTrackEnd.setText(tracks.get(trackPosition).getTrackDuration());
            setTrackDetails();
            trackList.setItemChecked(trackPosition, true);
        }
        mBtnPlayPause.setChecked(true);
    }

    public void skipToNext() {
        csService.setTrackPosition(++trackPosition);
        if (csService.getTrackPosition() >= tracks.size()) {
            csService.setTrackPosition(0);
            if (tracks.size() > 0) {
                csService.playTrack();
                trackList.setItemChecked(0, true);
                setCurrentTrackDurationAndWait();
            } else {
                mBtnSkipNext.setClickable(false);
            }
        } else if (tracks.size() == 0) {
            mBtnSkipNext.setClickable(false);
        } else {
            csService.playTrack();
            trackList.setItemChecked(trackPosition, true);
            setCurrentTrackDurationAndWait();
            mTrackEnd.setText(tracks.get(csService.getTrackPosition()).getTrackDuration());
            mBtnPlayPause.setChecked(true);
            setTrackDetails();
        }

    }

    public void stopTrack() {
        csService.stopTrack();
        mSeekbar.setProgress(0);
        mTrackStart.setText("00:00");
        mBtnPlayPause.setChecked(false);
    }

    private void setCurrentTrackDurationAndWait() {
        if (csService != null && musicBound) {
            try {
                Thread.sleep(100); // подождать, пока прогрузятся данные о продолжительности трека
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            currentTrackDuration = csService.getDuration();
        }
    }

    public void setSeekBarProgress() {
        int inTrackPosition;
        inTrackPosition = csService.getTrackCurrentPosition();
        mTrackStart.setText(CosmoSoundUtils.formatMilliseconds(inTrackPosition));
        mTrackEnd.setText(CosmoSoundUtils.formatMilliseconds(currentTrackDuration));
        mTrackInfo.setText(currentTrackInfo);
        mSeekbar.setMax(currentTrackDuration);
        mSeekbar.setProgress(inTrackPosition);
    }

    private Runnable updateTrackTime = new Runnable() { // таск, который обновляет позицию ползунка
        @Override
        public void run() {
            setSeekBarProgress();

            updateHandler.postDelayed(this, 100);
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_search:
                if (mFilterField.getVisibility() == View.GONE) {
                    mFilterField.setVisibility(View.VISIBLE);
                } else {
                    mFilterField.setVisibility(View.GONE);
                }
                return true;
            case R.id.action_sort:
                View sortItem = getActivity().findViewById(R.id.action_sort);
                PopupMenu sortMenu = new PopupMenu(getActivity(), sortItem);
                sortMenu.getMenu().add(0, 0, 0, "Sort by title");
                sortMenu.getMenu().add(0, 1, 1, "Sort by authors");
                sortMenu.getMenu().add(0, 2, 2, "Sort by albums");
                sortMenu.getMenu().add(0, 3, 3, "Sort by duration");
                sortMenu.getMenu().add(0, 4, 4, "Shuffle");

                sortMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case 0:
                                Collections.sort(tracks, new Comparator<MusicTrack>() {
                                    @Override
                                    public int compare(MusicTrack lhs, MusicTrack rhs) {
                                        return lhs.getTrackTitle().compareTo(rhs.getTrackTitle());
                                    }
                                });
                                adapter.notifyDataSetChanged();
                                trackList.setAdapter(adapter);
                                break;
                            case 1:
                                Collections.sort(tracks, new Comparator<MusicTrack>() {
                                    @Override
                                    public int compare(MusicTrack lhs, MusicTrack rhs) {
                                        return lhs.getTrackAuthor().compareTo(rhs.getTrackAuthor());
                                    }
                                });
                                adapter.notifyDataSetChanged();
                                trackList.setAdapter(adapter);
                                break;
                            case 2:
                                Collections.sort(tracks, new Comparator<MusicTrack>() {
                                    @Override
                                    public int compare(MusicTrack lhs, MusicTrack rhs) {
                                        return lhs.getTrackAlbum().compareTo(rhs.getTrackAlbum());
                                    }
                                });
                                adapter.notifyDataSetChanged();
                                trackList.setAdapter(adapter);
                                break;
                            case 3:
                                Collections.sort(tracks, new Comparator<MusicTrack>() {
                                    @Override
                                    public int compare(MusicTrack lhs, MusicTrack rhs) {
                                        return lhs.getTrackDuration().compareTo(rhs.getTrackDuration());
                                    }
                                });
                                adapter.notifyDataSetChanged();
                                trackList.setAdapter(adapter);
                                break;
                            case 4:
                                Collections.shuffle(tracks, new Random(System.nanoTime()));
                                adapter.notifyDataSetChanged();
                                trackList.setAdapter(adapter);
                                break;
                            default:
                                return false;
                        }
                        return true;
                    }
                });
                sortMenu.show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public CosmoSoundService getService() {
        return csService;
    }

    public Intent getLaunchIntent() {
        return launchIntent;
    }

    private void savePrefs(String key, boolean value) {
        SharedPreferences sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    private boolean loadPrefs(String key) {
        SharedPreferences sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(key, mBtnPlayPause.isChecked());
    }

    public void loadAllTracks() {
        dataSource = "";
        tracks.clear();
        getTrackList();
        adapter = new CosmoSoundSongAdapter(tracks, getActivity());
        adapter.notifyDataSetChanged();
        trackList.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        trackList.setAdapter(adapter);
    }

    @Override
    public void onStop() {
        //if (musicBound) {
        //    getActivity().unbindService(csConnection);
        //    musicBound = false;
        //}
        savePrefs("playpause", mBtnPlayPause.isChecked());
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.w("Cosmosound lifecycle", "Playlist activity destroyed!");
        if (csService != null) {
            getActivity().stopService(launchIntent);
            musicBound = false;
        }
        super.onDestroy();
    }
}
