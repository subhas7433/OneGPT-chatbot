package com.affixstudio.chatopen.model;

public class ModelList {
    String model_name;
    int isPremium;
    int id;

    int inputLimit;
    int display_rank;
    int isVisible;
    int contextSize;
    String description;
    String welcomeMsg;
    String specGraph;
    String iconUrl;

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public ModelList(int id,String model_name,
                     int isPremium, int inputLimit,
                     int display_rank, int isVisible,
                     int contextSize, String description, String welcomeMsg, String specGraph, String iconUrl) {
        this.model_name = model_name;
        this.isPremium = isPremium;
        this.id = id;

        this.inputLimit = inputLimit;
        this.display_rank = display_rank;
        this.isVisible = isVisible;
        this.contextSize = contextSize;
        this.description = description;
        this.welcomeMsg = welcomeMsg;// todo copy and paste
        this.specGraph = specGraph;
        this.iconUrl = iconUrl;
    }

    public int getDisplay_rank() {
        return display_rank;
    }

    public int getIsVisible() {
        return isVisible;
    }

    public int getId() {
        return id;
    }

    public String getWelcomeMsg() {
        return welcomeMsg;
    }
    public String getModel_name() {
        return model_name;
    }

    public void setModel_name(String model_name) {
        this.model_name = model_name;
    }

    public int getIsPremium() {
        return isPremium;
    }

    public void setIsPremium(int isPremium) {
        this.isPremium = isPremium;
    }

    public int getInputLimit() {
        return inputLimit;
    }

    public void setInputLimit(int inputLimit) {
        this.inputLimit = inputLimit;
    }

    public int getContextSize() {
        return contextSize;
    }

    public void setContextSize(int contextSize) {
        this.contextSize = contextSize;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSpecGraph() {
        return specGraph;
    }

    public void setSpecGraph(String specGraph) {
        this.specGraph = specGraph;
    }
}
