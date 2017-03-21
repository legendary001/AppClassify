package com.ifeng.chenjh5.entity;

/**
 * Created by chenjh5 on 2017/3/21.
 */
public class User {
    private String uid;
    private String umos;
    private String umt;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUmos() {
        return umos;
    }

    public void setUmos(String umos) {
        this.umos = umos;
    }

    public String getUmt() {
        return umt;
    }

    public void setUmt(String umt) {
        this.umt = umt;
    }

    @Override
    public String toString() {
        return "User{" +
                "uid='" + uid + '\'' +
                ", umos='" + umos + '\'' +
                ", umt='" + umt + '\'' +
                '}';
    }
}
