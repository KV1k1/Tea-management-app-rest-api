package com.example.tea_quarkus;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RecommendationActivity extends AppCompatActivity {

    private TextView resultText, loadingText;
    private Button startTimerBtn;
    private ProgressBar loadingBar;
    private ImageView teaProg;

    private ArrayList<Tea> teaList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recommendation);

        // Edge-to-edge padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        resultText = findViewById(R.id.resultText);
        startTimerBtn = findViewById(R.id.startTimerButton);
        teaProg = findViewById(R.id.teaProg);
        loadingBar = findViewById(R.id.loadingBar);
        loadingText = findViewById(R.id.loadingText);

        startTimerBtn.setVisibility(View.GONE);

        simulateLoadingAndFetchTea();
    }

    private void simulateLoadingAndFetchTea() {
        resultText.setVisibility(View.GONE);
        new Thread(() -> {
            for (int i = 0; i <= 100; i++) {
                int progress = i;
                runOnUiThread(() -> {
                    loadingBar.setProgress(progress);
                    teaProg.setTranslationX((loadingBar.getWidth() - teaProg.getWidth()) * progress / 100f);
                    teaProg.setRotation((float) Math.sin(progress * Math.PI / 50) * 15);
                });

                try {
                    Thread.sleep(25);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            runOnUiThread(() -> {
                fetchTeasFromBackend();
            });
        }).start();
    }

    private void fetchTeasFromBackend() {
        loadingText.setVisibility(View.VISIBLE);

        // Replace with your PC or server IP
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/") // <-- your Quarkus backend IP
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        TeaApi teaApi = retrofit.create(TeaApi.class);

        teaApi.getAllTeas().enqueue(new Callback<List<Tea>>() {
            @Override
            public void onResponse(Call<List<Tea>> call, Response<List<Tea>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    teaList.clear();
                    teaList.addAll(response.body());
                    loadingText.setVisibility(View.GONE);
                    loadBestTea();
                    fadeInRecommendation();
                } else {
                    Toast.makeText(RecommendationActivity.this, "Failed to load teas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Tea>> call, Throwable t) {
                loadingText.setVisibility(View.GONE);
                Toast.makeText(RecommendationActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fadeInRecommendation() {
        resultText.setAlpha(0f);
        resultText.setVisibility(View.VISIBLE);
        resultText.animate().alpha(1f).setDuration(1500).start();
        teaProg.animate().alpha(0f).setDuration(800).start();
        loadingBar.animate().alpha(0f).setDuration(800).start();
        findViewById(R.id.loadingText).animate().alpha(0f).setDuration(800).start();
    }

    private void loadBestTea() {
        ArrayList<String> purposes = getIntent().getStringArrayListExtra("purposeArray");
        ArrayList<String> flavors = getIntent().getStringArrayListExtra("flavorArray");
        ArrayList<String> dayTimes = getIntent().getStringArrayListExtra("dayTimeArray");

        Tea bestTea = null;
        int maxMatches = -1;

        for (Tea tea : teaList) {
            int matches = 0;

            if (tea.getPurpose() != null && purposes != null)
                for (String sel : purposes)
                    for (String p : tea.getPurpose())
                        if (p.equalsIgnoreCase(sel)) matches++;

            if (tea.getFlavor() != null && flavors != null)
                for (String sel : flavors)
                    for (String f : tea.getFlavor())
                        if (f.equalsIgnoreCase(sel)) matches++;

            if (tea.getDayTime() != null && dayTimes != null)
                for (String sel : dayTimes)
                    for (String d : tea.getDayTime())
                        if (d.equalsIgnoreCase(sel)) matches++;

            if (matches > maxMatches) {
                bestTea = tea;
                maxMatches = matches;
            }
        }

        if (bestTea != null) {
            resultText.setText("A teád: " + bestTea.getName() + "\n\n" + bestTea.getDescription());
            startTimerBtn.setVisibility(View.VISIBLE);

            Tea finalBestTea = bestTea;
            startTimerBtn.setOnClickListener(v -> {
                Intent i = new Intent(RecommendationActivity.this, CountdownActivity.class);
                i.putExtra("teaName", finalBestTea.getName());
                i.putExtra("teaDescription", finalBestTea.getDescription());
                i.putExtra("brewTime", finalBestTea.getBrewTime());
                i.putExtra("waterTemp", finalBestTea.getWaterTemp());
                i.putExtra("recommendation", finalBestTea.getRecommendation());
                i.putStringArrayListExtra("purposeArray", new ArrayList<>(finalBestTea.getPurpose()));
                i.putStringArrayListExtra("flavorArray", new ArrayList<>(finalBestTea.getFlavor()));
                i.putStringArrayListExtra("dayTimeArray", new ArrayList<>(finalBestTea.getDayTime()));
                startActivity(i);
            });

        } else {
            resultText.setText("Nem találtunk megfelelő teát a választásaid alapján.");
        }
    }
}
