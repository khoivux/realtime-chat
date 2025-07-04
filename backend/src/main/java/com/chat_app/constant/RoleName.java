package com.chat_app.constant;

public enum RoleName {
    USER("USER"),
    ADMIN("ADMIN");

    private final String value;

    RoleName(String value){
        this.value = value;
    }

    public String toString(){
        return value;
    }
}