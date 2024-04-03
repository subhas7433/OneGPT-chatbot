package com.affixstudio.chatopen.GetData;

import java.io.Serializable;

public class AssistantData implements Serializable {

    String 	tool_name;
    String 	tool_description;
    String 	tool_prompt;
    String 	tool_icon;

    public String getTool_name() {
        return tool_name;
    }

    public void setTool_name(String tool_name) {
        this.tool_name = tool_name;
    }

    public String getTool_description() {
        return tool_description;
    }

    public void setTool_description(String tool_description) {
        this.tool_description = tool_description;
    }

    public String getTool_prompt() {
        return tool_prompt;
    }

    public void setTool_prompt(String tool_prompt) {
        this.tool_prompt = tool_prompt;
    }

    public String getTool_icon() {
        return tool_icon;
    }

    public void setTool_icon(String tool_icon) {
        this.tool_icon = tool_icon;
    }

    public AssistantData(String tool_name, String tool_description, String tool_prompt, String tool_icon) {
        this.tool_name = tool_name;
        this.tool_description = tool_description;
        this.tool_prompt = tool_prompt;
        this.tool_icon = tool_icon;
    }
}
