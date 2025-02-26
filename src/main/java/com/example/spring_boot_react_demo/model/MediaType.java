package com.example.spring_boot_react_demo.model;

public enum MediaType {
    VIDEO("video"),
    IMAGE("image"),
    AUDIO("audio")
    ;
    private final String name;
    MediaType(String name) {
        this.name = name;
    }
    public String getname() {
        return name;
    }
}
