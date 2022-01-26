package com.lopez.julz.readandbill.dao;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Users {
    @PrimaryKey
    @NonNull
    private String id;

    @ColumnInfo (name = "Username")
    private String Username;

    @ColumnInfo (name = "Password")
    private String Password;

    public Users() {
    }

    public Users(@NonNull String id, String username, String password) {
        this.id = id;
        Username = username;
        Password = password;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }
}

