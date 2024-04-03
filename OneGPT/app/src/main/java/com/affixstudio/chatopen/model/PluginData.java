package com.affixstudio.chatopen.model;

public class PluginData {
    int id;
    String name;
    String 	des;
    String 	icon;
    int isVisible;

    public PluginData(int id, String name, String des, String icon, int isVisible) {
        this.id = id;
        this.name = name;
        this.des = des;
        this.icon = icon;
        this.isVisible = isVisible;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDes() {
        return des;
    }

    public String getIcon() {
        return icon;
    }

    public int getIsVisible() {
        return isVisible;
    }
}
