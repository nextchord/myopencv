package com.example.myapplication;

public class Template {
    private String name;
    private int templateId;

    public Template(String name, int templateId) {
        this.name = name;
        this.templateId = templateId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTemplateId() {
        return templateId;
    }

    public void setTemplateId(int templateId) {
        this.templateId = templateId;
    }
}
