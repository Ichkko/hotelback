package com.example.hotelback.service;

public interface GoogleAuthService {

    GoogleUserInfo verify(String idToken);

    record GoogleUserInfo(String email, String name) {
    }
}
