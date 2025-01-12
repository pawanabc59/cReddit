package com.example.creddit.RoomDatabase;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {HistoryTab.class, LoggedInUser.class, HistoryPosts.class} , version = 1, exportSchema = false)
public abstract class MyRoomDatabase extends RoomDatabase {
    public abstract DAO dao();
    public abstract LoggedInUserDAO loggedInUserDAO();
    public abstract HistoryPostsDAO historyPostsDAO();
}
