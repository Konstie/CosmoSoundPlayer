package com.cosmosound.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.melnykov.fab.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SourceBrowserFragment extends ListFragment {
    private List<String> path = null;
    private String root;
    private TextView pathValue;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_filebrowser, parent, false);

        pathValue = (TextView) rootView.findViewById(R.id.path);
        root = Environment.getExternalStorageDirectory().getPath();
        getFolder(root);

        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sourcePath = pathValue.getText().toString();

                CosmoSoundPlaylistFragment.dataSource = sourcePath.replace("/storage/sdcard0/", "");
                Intent intent = new Intent(getActivity(), CosmoSoundPlaylistActivity.class);
                startActivity(intent);
                //getActivity().onBackPressed();
            }
        });

        return rootView;
    }

    private void getFolder(String dirPath) { // вытаскиваем все папки и файлы с расширением mp3
        pathValue.setText(dirPath);
        List<String> items = new ArrayList<>();
        path = new ArrayList<>();

        File fileItem = new File(dirPath);
        File[] files = fileItem.listFiles();

        if (!dirPath.equals(root)) {
            items.add(root);
            path.add(root);
            items.add("←");
            path.add(fileItem.getParent());
        }

        for (File file : files) {
            if (!file.isHidden() && file.canRead()) {
                if (file.isDirectory()) {
                    path.add(file.getPath());
                    items.add(file.getName() + "/");
                } else {
                    if (file.getName().endsWith(".mp3")) {
                        items.add(file.getName());
                    }
                }
            }
        }

        ArrayAdapter<String> fileList =
                new ArrayAdapter<>(getActivity(), R.layout.file_row, items);
        setListAdapter(fileList);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        File file = null;
        try {
             file = new File(path.get(position));
        } catch (IndexOutOfBoundsException exc) {
            Toast.makeText(getActivity(),
                    "Not a folder, but mp3. You may add current folder's contents to playlist",
                    Toast.LENGTH_SHORT).show();
        }

        if (file != null && file.isDirectory() && file.canRead()) {
            getFolder(path.get(position));
        }
    }
}
