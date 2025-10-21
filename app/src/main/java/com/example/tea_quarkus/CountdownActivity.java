package com.example.tea_quarkus;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;

public class CountdownActivity extends AppCompatActivity {

    private TextView teaName, teaDescription, timerText;
    private MaterialButton startButton, btnPause, btnRestart;
    private CircularProgressIndicator circularProgress;

    private ObjectAnimator rotationAnimator;
    private CountDownTimer timer;
    private boolean isRunning = false;
    private boolean isPaused = false;

    private long timeLeftMillis;
    private long totalTimeMillis;
    private float currentRotation = 0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_countdown);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        circularProgress = findViewById(R.id.circularProgress);
        teaName = findViewById(R.id.teaName);
        teaDescription = findViewById(R.id.teaDescription);
        timerText = findViewById(R.id.timerText);
        startButton = findViewById(R.id.startTimerButton);
        btnPause = findViewById(R.id.btnPause);
        btnRestart = findViewById(R.id.btnRestart);

        // Get tea details from Intent
        String name = getIntent().getStringExtra("teaName");
        String description = getIntent().getStringExtra("teaDescription");
        int brewTime = getIntent().getIntExtra("brewTime", 3);
        int waterTemp = getIntent().getIntExtra("waterTemp", 80);
        String recommendation = getIntent().getStringExtra("recommendation");

        ArrayList<String> purposes = getIntent().getStringArrayListExtra("purposeArray");
        ArrayList<String> flavors = getIntent().getStringArrayListExtra("flavorArray");
        ArrayList<String> dayTime = getIntent().getStringArrayListExtra("dayTimeArray");

        teaName.setText(name);

        // Build summary text
        StringBuilder summary = new StringBuilder();
        summary.append(description).append("\n\n");
        if (purposes != null && !purposes.isEmpty())
            summary.append("Amire jó: ").append(String.join(", ", purposes)).append("\n");
        if (flavors != null && !flavors.isEmpty())
            summary.append("Ízvilág: ").append(String.join(", ", flavors)).append("\n");
        if (dayTime != null && !dayTime.isEmpty())
            summary.append("Legjobb fogyasztási idő: ").append(String.join(", ", dayTime)).append("\n");
        summary.append("Ajánlott hőfok: ").append(waterTemp).append("°C\n");
        summary.append("Fogyasztási tipp: ").append(recommendation);

        teaDescription.setText(summary.toString());
        teaDescription.setBackgroundResource(R.drawable.shadow_box);
        teaDescription.setPadding(24, 24, 24, 24);

        totalTimeMillis = brewTime * 60 * 1000L;
        timeLeftMillis = totalTimeMillis;
        updateTimerText();

        // Button actions
        startButton.setOnClickListener(v -> {
            if (!isRunning) {
                if (isPaused) {
                    resumeTimer();
                } else {
                    startCountdown(timeLeftMillis);
                }
            }
        });

        btnPause.setOnClickListener(v -> {
            if (isRunning) pauseTimer();
        });

        btnRestart.setOnClickListener(v -> restartTimer());

        // Restore timer state if available
        if (savedInstanceState != null) {
            timeLeftMillis = savedInstanceState.getLong("timeLeftMillis", totalTimeMillis);
            isRunning = savedInstanceState.getBoolean("isRunning", false);
            isPaused = savedInstanceState.getBoolean("isPaused", false);
            currentRotation = savedInstanceState.getFloat("currentRotation", 0f);

            updateTimerText();
            circularProgress.setProgress((int) ((1 - (double) timeLeftMillis / totalTimeMillis) * 100));
            circularProgress.setRotation(currentRotation);

            if (isRunning) startCountdown(timeLeftMillis);
            else if (isPaused) {
                startButton.setText("Resume");
                startButton.setEnabled(true);
                btnPause.setEnabled(false);
            }
        }
    }

    private void setKeepScreenOn(boolean keepOn) {
        if (keepOn) getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        else getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void startCountdown(long duration) {
        isRunning = true;
        isPaused = false;

        setKeepScreenOn(true); // keep screen awake only while timer runs

        circularProgress.setIndeterminate(false);
        circularProgress.setMax(100);

        startRotation(true);

        circularProgress.setIndeterminate(false);
        circularProgress.setMax(100);
        circularProgress.setProgress(1); // show immediately

        timer = new CountDownTimer(duration, 50) { // update every 50ms
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftMillis = millisUntilFinished;
                updateTimerText();

                int progress = (int) ((1 - (double) timeLeftMillis / totalTimeMillis) * 100);
                circularProgress.setProgressCompat(progress, true); // smooth increment
            }

            @Override
            public void onFinish() {
                completeTimer();
            }
        }.start();



        startButton.setText("Running...");
        startButton.setEnabled(false);
        btnPause.setEnabled(true);
    }

    private void startRotation(boolean accelerate) {
        if (rotationAnimator != null) rotationAnimator.cancel();

        float start = currentRotation;
        float end = currentRotation + 360f;

        rotationAnimator = ObjectAnimator.ofFloat(circularProgress, "rotation", start, end);
        rotationAnimator.setDuration(4000);
        rotationAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        rotationAnimator.setInterpolator(accelerate
                ? new AccelerateDecelerateInterpolator()
                : new LinearInterpolator());
        rotationAnimator.start();
    }

    private void stopRotationSmooth() {
        if (rotationAnimator == null) return;

        float start = circularProgress.getRotation() % 360f;
        currentRotation = start;

        ObjectAnimator stopAnim = ObjectAnimator.ofFloat(circularProgress, "rotation", start, start + 20f);
        stopAnim.setDuration(300);
        stopAnim.setInterpolator(new DecelerateInterpolator());
        stopAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (rotationAnimator != null) rotationAnimator.cancel();
            }
        });
        stopAnim.start();
    }

    private void pauseTimer() {
        if (timer != null) timer.cancel();
        stopRotationSmooth();
        isRunning = false;
        isPaused = true;
        setKeepScreenOn(false);

        startButton.setText("Resume");
        startButton.setEnabled(true);
        btnPause.setEnabled(false);
    }

    private void resumeTimer() {
        startRotation(true);
        startCountdown(timeLeftMillis);
        startButton.setText("Running...");
    }

    private void restartTimer() {
        if (timer != null) timer.cancel();
        if (rotationAnimator != null) rotationAnimator.cancel();

        isRunning = false;
        isPaused = false;
        currentRotation = 0f;
        setKeepScreenOn(false);

        timeLeftMillis = totalTimeMillis;
        circularProgress.setProgress(0);
        circularProgress.setRotation(0f);
        updateTimerText();

        startButton.setText("Start Brewing");
        startButton.setEnabled(true);
        btnPause.setEnabled(false);
    }

    private void completeTimer() {
        isRunning = false;
        isPaused = false;
        setKeepScreenOn(false);

        stopAnimations();
        circularProgress.setProgress(100);
        timerText.setText("Done!");

        Toast.makeText(this, "Enjoy your 🍵", Toast.LENGTH_SHORT).show();

        try {
            MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.beep);
            if (mediaPlayer != null) {
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(MediaPlayer::release);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            if (vibrator != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(500);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        startButton.setText("Start Brewing");
        startButton.setEnabled(true);
        btnPause.setEnabled(false);
    }

    private void updateTimerText() {
        int minutes = (int) (timeLeftMillis / 1000) / 60;
        int seconds = (int) (timeLeftMillis / 1000) % 60;
        timerText.setText(String.format("%02d:%02d", minutes, seconds));
    }

    private void stopAnimations() {
        if (rotationAnimator != null) rotationAnimator.cancel();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("timeLeftMillis", timeLeftMillis);
        outState.putBoolean("isRunning", isRunning);
        outState.putBoolean("isPaused", isPaused);
        outState.putFloat("currentRotation", currentRotation);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) timer.cancel();
        stopAnimations();
    }
}
