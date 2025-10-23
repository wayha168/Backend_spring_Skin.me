package com.project.skin_me.request;

import java.util.List;

public record ChatGPTRequest(String model, List<Message> messages) {

    public static record Message(String role, String content) {}
}
