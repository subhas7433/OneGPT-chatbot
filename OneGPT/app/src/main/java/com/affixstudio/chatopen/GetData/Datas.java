package com.affixstudio.chatopen.GetData;

public class Datas {



    String email;
    int credits ,isMember;

    public Datas(String email, int credits, int isMember) {
        this.email = email;
        this.credits = credits;
        this.isMember = isMember;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public int getIsMember() {
        return isMember;
    }

    public void setIsMember(int isMember) {
        this.isMember = isMember;
    }
}
