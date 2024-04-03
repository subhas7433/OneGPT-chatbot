package com.affixstudio.chatopen.model;

public class ExpertAIData {


    String name;
    String des;
    String prompt;
    String icon;

    public ExpertAIData(String name, String des, String prompt, String icon)
    {
        this.name = name;
        this.des = des;
        this.prompt = prompt;
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public String getDes() {
        return des;
    }

    public String getPrompt() {
        return prompt;
    }

    public String getIcon() {
        return icon;
    }
}
