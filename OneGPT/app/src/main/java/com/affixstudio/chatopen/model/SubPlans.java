package com.affixstudio.chatopen.model;

public class SubPlans {
    String id;
    String names;

    public SubPlans(String id, String names) {
        this.id = id;
        this.names = names;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNames() {
        return names;
    }

    public void setNames(String names) {
        this.names = names;
    }
}
