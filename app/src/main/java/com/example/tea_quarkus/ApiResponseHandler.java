package com.example.tea_quarkus;

import android.content.Context;
import android.widget.Toast;
import retrofit2.Response;
import java.io.IOException;

public class ApiResponseHandler {

    public static boolean handleResponse(Context context, Response<?> response) {
        if (response.isSuccessful()) {
            return true;
        }

        String errorMessage = "Failed (" + response.code() + ")";
        try {
            if (response.errorBody() != null) {
                String body = response.errorBody().string();
                if (!body.isEmpty()) errorMessage += ": " + body;
            }
        } catch (IOException ignored) {}

        if (response.code() == 401 || response.code() == 403) {
            errorMessage = "Unauthorized (" + response.code() + "): Admin access required.";
        }

        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
        return false;
    }
}