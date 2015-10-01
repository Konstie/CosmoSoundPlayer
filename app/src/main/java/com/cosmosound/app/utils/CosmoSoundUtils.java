package com.cosmosound.app.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CosmoSoundUtils {
    public static String formatMilliseconds(long durationInMillis) {
        Date date = new Date(durationInMillis);
        DateFormat formatter = new SimpleDateFormat("mm:ss");
        return formatter.format(date);
    }
}
