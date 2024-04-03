package com.affixstudio.chatopen.model;

public class TableData {

    int id;
    String url;
    String prompt;

    public TableData(int id, String url, String prompt) {
        this.id = id;
        this.url = url;
        this.prompt = prompt;
    }

    public String getPrompt() {
        return prompt;
    }

    public int getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }
}
