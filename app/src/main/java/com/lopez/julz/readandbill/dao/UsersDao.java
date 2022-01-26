package com.lopez.julz.readandbill.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface UsersDao {
    @Insert
    void insertAll(Users... users);

    @Update
    void updateAll(Users... users);

    @Query("SELECT * FROM Users WHERE Username = :username AND Password = :password")
    Users getOne(String username, String password);
}
