package com.example.myapplication;

import java.io.Serializable;

public class Result implements Serializable {
    private int id;
    private String answer;
    private String standard;
    private boolean isRight;

    public Result(int id, String answer, String standard) {
        this.id = id;
        this.answer = answer;
        this.standard = standard;
        this.isRight = answer.equals(standard);
    }
    public String getId() {return id+"";}

    public String getAnswer() {
        return answer;
    }

    public String getStandard() {
        return standard;
    }

    public String isRight() {
        if(isRight)
            return "正确";
        return "错误";
    }
}
