package com.walkmanx21.junit.dto;


import lombok.*;

@Value(staticConstructor = "of")
public class User {

    private Integer id;
    private String username;
    private String password;
}
