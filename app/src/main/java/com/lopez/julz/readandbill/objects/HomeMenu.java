package com.lopez.julz.readandbill.objects;

import android.graphics.Color;
import android.graphics.drawable.Drawable;

public class HomeMenu {
    Drawable image;
    String title;
    String color;

    public HomeMenu() {
    }

    public HomeMenu(Drawable image, String title, String color) {
        this.image = image;
        this.title = title;
        this.color = color;
    }

    public Drawable getImage() {
        return image;
    }

    public void setImage(Drawable image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
