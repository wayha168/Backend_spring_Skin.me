package com.project.skin_me.request;

import lombok.Data;

@Data
public class UserUpdateRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private Boolean enabled;
}
