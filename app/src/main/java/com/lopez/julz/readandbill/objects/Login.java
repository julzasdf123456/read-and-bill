package com.lopez.julz.readandbill.objects;

public class Login {

    private String username, password, id;

    public Login(String username, String password, String id) {
        this.username = username;
        this.password = password;
        this.id = id;
    }

    public Login(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
