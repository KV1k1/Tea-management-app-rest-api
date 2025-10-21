package com.example.tea_quarkus;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
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

public class QuizActivity extends AppCompatActivity {
    private TextView progressLabel, questionText, opcio3;
    private LinearLayout optionsContainer;
    private Button nextButton;
    private ProgressBar progressBar;

    private List<Question> questions;
    private int currentQuestionIndex = 0;
    private Map<String, List<String>> answers = new HashMap<>(); // Multi-selection ready

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_quiz);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        questionText = findViewById(R.id.questionText);
        optionsContainer = findViewById(R.id.optionsContainer);
        nextButton = findViewById(R.id.nextButton);
        progressBar = findViewById(R.id.progressBar);
        progressLabel = findViewById(R.id.progressLabel);
        opcio3 = findViewById(R.id.opcio3);

        ScrollView scrollView = findViewById(R.id.scrollView);

        // Initialize quiz questions
        questions = new ArrayList<>();
        questions.add(new Question("Milyen hatást szeretnél?", new String[]{"Élénkítő", "Emésztés", "Nyugtató", "Gyulladáscsökkentő", "Immunitás", "Stresszoldó", "Bőrbarát", "Antioxidáns"}, "purpose"));
        questions.add(new Question("Milyen ízvilágot keresel?", new String[]{"Gyümölcsös", "Virágos", "Friss", "Lágy", "Fanyar", "Édeskés", "Gazdag", "Mentolos"}, "flavor"));
        questions.add(new Question("Mikor fogod inni?", new String[]{"Reggel", "Délelőtt", "Délután", "Étkezés után", "Este"}, "dayTime"));

        if (questions.isEmpty()) {
            Toast.makeText(this, "No quiz questions available.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        showQuestion(currentQuestionIndex);

        nextButton.setOnClickListener(v -> {
            currentQuestionIndex++;
            if (currentQuestionIndex < questions.size()) {
                animateQuestionChange(() -> showQuestion(currentQuestionIndex));
            } else {
                // Finish quiz
                Intent i = new Intent(QuizActivity.this, RecommendationActivity.class);
                // Pass first answer for each type (single selection) for backward compatibility
                for (Map.Entry<String, List<String>> entry : answers.entrySet()) {
                    if (!entry.getValue().isEmpty()) {
                        i.putExtra(entry.getKey(), entry.getValue().get(0));
                    }
                    i.putStringArrayListExtra(entry.getKey() + "Array", new ArrayList<>(entry.getValue()));
                }
                startActivity(i);
                finish();
            }
        });
    }

    private void showQuestion(int index) {
        Question q = questions.get(index);
        questionText.setText(q.getQuestionText());
        optionsContainer.removeAllViews();
        nextButton.setEnabled(false);

        boolean isMultiSelect = index < 2; // First two questions allow 1–3 choices

        if (isMultiSelect) {
            // Fade in the "max 3 options" label
            opcio3.setVisibility(View.VISIBLE);
            AlphaAnimation fadeInOpcio = new AlphaAnimation(0, 1);
            fadeInOpcio.setDuration(400);
            opcio3.startAnimation(fadeInOpcio);
        } else {
            // Fade out when moving to last question
            if (opcio3.getVisibility() == View.VISIBLE) {
                AlphaAnimation fadeOutOpcio = new AlphaAnimation(1, 0);
                fadeOutOpcio.setDuration(300);
                fadeOutOpcio.setAnimationListener(new SimpleAnimationListener() {
                    @Override
                    public void onAnimationEnd(android.view.animation.Animation animation) {
                        opcio3.setVisibility(View.GONE);
                    }
                });
                opcio3.startAnimation(fadeOutOpcio);
            }
        }
        // Animate progress bar
        int targetProgress = (int) (((double) index / questions.size()) * 100);
        ObjectAnimator.ofInt(progressBar, "progress", progressBar.getProgress(), targetProgress)
                .setDuration(400).start();
        progressLabel.setText(targetProgress + "%");

        for (String option : q.getOptions()) {
            Button btn = new Button(this);
            btn.setText(option);
            btn.setAllCaps(false);
            btn.setTextSize(18f);
            btn.setTextColor(getResources().getColor(android.R.color.black));
            btn.setPadding(24, 24, 24, 24);

            GradientDrawable drawable = new GradientDrawable();
            drawable.setCornerRadius(24);
            drawable.setColor(getResources().getColor(android.R.color.white));
            drawable.setStroke(3, getResources().getColor(R.color.pomegranate));
            btn.setBackground(drawable);

            // Slide-up animation
            TranslateAnimation slideUp = new TranslateAnimation(0, 0, 300, 0);
            slideUp.setDuration(400);
            btn.startAnimation(slideUp);

            btn.setOnClickListener(v -> {
                List<String> selectedOptions = answers.getOrDefault(q.getType(), new ArrayList<>());

                if (isMultiSelect) {
                    // MULTI-SELECTION MODE (1–3)
                    if (selectedOptions.contains(option)) {
                        // Deselect
                        selectedOptions.remove(option);
                        drawable.setColor(getResources().getColor(android.R.color.white));
                    } else {
                        // Select new
                        if (selectedOptions.size() >= 3) {
                            Toast.makeText(this, "Maximum 3 opció választható!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        selectedOptions.add(option);
                        drawable.setColor(getResources().getColor(R.color.salmon));
                    }
                    answers.put(q.getType(), selectedOptions);

                    // Enable next if 1–3 options selected
                    nextButton.setEnabled(!selectedOptions.isEmpty());

                } else {
                    // SINGLE SELECTION MODE
                    selectedOptions.clear();
                    selectedOptions.add(option);
                    answers.put(q.getType(), selectedOptions);
                    nextButton.setEnabled(true);

                    // Reset all buttons
                    for (int i = 0; i < optionsContainer.getChildCount(); i++) {
                        Button b = (Button) optionsContainer.getChildAt(i);
                        GradientDrawable bg = (GradientDrawable) b.getBackground();
                        bg.setColor(getResources().getColor(android.R.color.white));
                    }
                    drawable.setColor(getResources().getColor(R.color.salmon));
                }

                // Update visual state of all buttons
                for (int i = 0; i < optionsContainer.getChildCount(); i++) {
                    Button b = (Button) optionsContainer.getChildAt(i);
                    GradientDrawable bg = (GradientDrawable) b.getBackground();
                    if (answers.getOrDefault(q.getType(), new ArrayList<>()).contains(b.getText().toString())) {
                        bg.setColor(getResources().getColor(R.color.salmon));
                    } else {
                        bg.setColor(getResources().getColor(android.R.color.white));
                    }
                }
            });

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 16, 0, 16);
            btn.setLayoutParams(params);
            optionsContainer.addView(btn);
        }

        // Animate question text
        AlphaAnimation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setDuration(400);
        questionText.startAnimation(fadeIn);
    }

    private void animateQuestionChange(Runnable onAnimationEnd) {
        AlphaAnimation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setDuration(300);
        fadeOut.setAnimationListener(new SimpleAnimationListener() {
            @Override
            public void onAnimationEnd(android.view.animation.Animation animation) {
                onAnimationEnd.run();
            }
        });

        questionText.startAnimation(fadeOut);
        for (int i = 0; i < optionsContainer.getChildCount(); i++) {
            optionsContainer.getChildAt(i).startAnimation(fadeOut);
            if (opcio3.getVisibility() == View.VISIBLE)
                opcio3.startAnimation(fadeOut);

        }
    }

    private abstract static class SimpleAnimationListener implements android.view.animation.Animation.AnimationListener {
        @Override public void onAnimationStart(android.view.animation.Animation animation) {}
        @Override public void onAnimationRepeat(android.view.animation.Animation animation) {}
    }

    private static class Question {
        private String questionText;
        private String[] options;
        private String type;

        public Question(String questionText, String[] options, String type) {
            this.questionText = questionText;
            this.options = options;
            this.type = type;
        }

        public String getQuestionText() { return questionText; }
        public String[] getOptions() { return options; }
        public String getType() { return type; }
    }
}