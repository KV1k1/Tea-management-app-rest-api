package com.example.tea_quarkus;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.List;

public class TokenManager {
    private static final String PREF_NAME = "AuthPref";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_LOGIN_NAME = "loginName";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public TokenManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void saveToken(String token, String loginName) {
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_LOGIN_NAME, loginName);
        editor.apply();
    }

    public String getToken() {
        return sharedPreferences.getString(KEY_TOKEN, null);
    }

    public String getLoginName() {
        return sharedPreferences.getString(KEY_LOGIN_NAME, null);
    }

    public boolean isLoggedIn() {
        return getToken() != null;
    }

    public void clear() {
        editor.clear();
        editor.apply();
    }

   }
