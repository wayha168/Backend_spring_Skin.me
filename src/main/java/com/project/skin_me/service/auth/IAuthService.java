package com.project.skin_me.service.auth;

import com.project.skin_me.model.User;
import com.project.skin_me.request.SignupRequest;

public interface IAuthService  {
    User signup(SignupRequest signupRequest);

}
