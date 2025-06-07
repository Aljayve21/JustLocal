package com.example.justlocal.DashboardAdapter;

public class DashboardMenuItem {
    private String title;
    private int iconRes;
    private int count;
    private String action;

    public DashboardMenuItem(String title, int iconRes, String action) {
        this.title = title;
        this.iconRes = iconRes;
        this.action = action;
        this.count = 0;
    }

    public DashboardMenuItem(String title, int iconRes, String action, int count) {
        this.title = title;
        this.iconRes = iconRes;
        this.action = action;
        this.count = count;
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getIconRes() {
        return iconRes;
    }

    public void setIconRes(int iconRes) {
        this.iconRes = iconRes;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
