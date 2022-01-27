package com.lopez.julz.readandbill.dao;

import androidx.room.Database;
import androidx.room.Query;
import androidx.room.RoomDatabase;

import java.util.List;

@Database(entities = {
        TrackNames.class,
        Tracks.class,
        Users.class,
        ReadingSchedules.class,
        DownloadedPreviousReadings.class,
        Readings.class,
        Rates.class,
    }, version = 24)
public abstract class AppDatabase extends RoomDatabase {
    public abstract TrackNamesDao trackNamesDao();

    public abstract TracksDao tracksDao();

    public abstract UsersDao usersDao();

    public abstract ReadingSchedulesDao readingSchedulesDao();

    public abstract DownloadedPreviousReadingsDao downloadedPreviousReadingsDao();

    public abstract ReadingsDao readingsDao();

    public abstract RatesDao ratesDao();
}
