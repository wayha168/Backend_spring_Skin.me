package com.project.skin_me.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
public class UserDto {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String googleId;
    private boolean enabled;
    private LocalDateTime registrationDate;
    private LocalDateTime lastLogin;

    @JsonProperty("isOnline")
    private boolean isOnline;
    private Set<String> roles;

}
