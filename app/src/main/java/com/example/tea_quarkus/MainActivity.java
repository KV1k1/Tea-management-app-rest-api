package com.example.tea_quarkus;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.util.Base64;
import org.json.JSONArray;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {

    private Button btnTeaQuiz, btnTeaBrewing, btnLanguage, btnLogout;

    private Button btnAdminPanel;


    @Override
    protected void attachBaseContext(Context newBase) {
        // Load saved language before activity is created
        String lang = newBase.getSharedPreferences("settings", MODE_PRIVATE)
                .getString("lang", "hu");
       Context context = LocaleHelper.setLocale(newBase, lang);
        super.attachBaseContext(LocaleHelper.setLocale(newBase, lang));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnTeaQuiz = findViewById(R.id.btnQuiz);
        btnTeaBrewing = findViewById(R.id.btnBrewing);
        btnLanguage = findViewById(R.id.btnLanguage);
        btnLogout = findViewById(R.id.btnLogout);

        btnAdminPanel = findViewById(R.id.btnAdminPanel);
        if (isAdminUser()) {
            btnAdminPanel.setVisibility(View.VISIBLE);
            btnAdminPanel.setOnClickListener(v ->
                    startActivity(new Intent(MainActivity.this, AdminTeaManagementActivity.class)));
        } else {
            btnAdminPanel.setVisibility(View.GONE);
        }

        btnTeaQuiz.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, QuizActivity.class)));

        btnTeaBrewing.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, TeaSelectionActivity.class)));

        btnLanguage.setOnClickListener(v -> showLanguageDialog());

            btnLogout.setOnClickListener(v -> {
            new TokenManager(this).clear();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });
    }

    private boolean isAdminUser() {
        TokenManager tokenManager = new TokenManager(this);
        String token = tokenManager.getToken();
        String loginName = tokenManager.getLoginName();

        boolean hasAdminRole = hasAdminRoleFromToken(token);

        // Fallback to legacy username check to avoid locking out admin if token doesn't carry roles
        boolean isLegacyAdmin = "teaAdmin".equals(loginName);

        System.out.println("Current login name: " + loginName);
        System.out.println("Has ADMIN role from token: " + hasAdminRole);
        System.out.println("Legacy admin username match: " + isLegacyAdmin);

        return hasAdminRole || isLegacyAdmin;
    }

    private boolean hasAdminRoleFromToken(String token) {
        if (token == null) return false;
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return false; // Not a JWT
            byte[] payloadBytes = Base64.decode(parts[1], Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
            String payloadJson = new String(payloadBytes, StandardCharsets.UTF_8);
            JSONObject payload = new JSONObject(payloadJson);

            // Try common claim names
            if (payload.has("groups")) {
                if (arrayHasAdmin(payload.getJSONArray("groups"))) return true;
            }
            if (payload.has("roles")) {
                if (arrayHasAdmin(payload.getJSONArray("roles"))) return true;
            }
            if (payload.has("authorities")) {
                if (arrayHasAdmin(payload.getJSONArray("authorities"))) return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private boolean arrayHasAdmin(JSONArray arr) {
        if (arr == null) return false;
        for (int i = 0; i < arr.length(); i++) {
            String v = String.valueOf(arr.opt(i));
            if ("ADMIN".equalsIgnoreCase(v)) return true;
        }
        return false;
    }

    private void showLanguageDialog() {
        String[] languages = {
                getString(R.string.dialog_hungarian),
                getString(R.string.dialog_english)
        };
        String[] codes = {"hu", "en"};

        String currentLang = getSharedPreferences("settings", MODE_PRIVATE)
                .getString("lang", "hu");
        int checkedItem = currentLang.equals("en") ? 1 : 0;

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_title))
                .setSingleChoiceItems(languages, checkedItem, null)
                .setPositiveButton(getString(R.string.dialog_ok), (dialog, whichButton) -> {
                    int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                    String selectedLang = codes[selectedPosition];

                    getSharedPreferences("settings", MODE_PRIVATE)
                            .edit()
                            .putString("lang", selectedLang)
                            .apply();

                    LocaleHelper.setLocale(MainActivity.this, selectedLang);

                    recreate();

                })
                .setNegativeButton(getString(R.string.dialog_cancel), null)
                .show();
    }
}
