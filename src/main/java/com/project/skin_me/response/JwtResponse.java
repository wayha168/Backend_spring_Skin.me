package com.project.skin_me.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtResponse {

    private Long id;
    private String jwtToken;
    private java.util.Set<String> roles;

}
