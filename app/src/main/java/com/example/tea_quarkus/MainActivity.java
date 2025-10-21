package com.example.tea_quarkus;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private Button btnTeaQuiz, btnTeaBrewing, btnLanguage;

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

        btnTeaQuiz.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, QuizActivity.class)));

        btnTeaBrewing.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, TeaSelectionActivity.class)));

        btnLanguage.setOnClickListener(v -> showLanguageDialog());
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
