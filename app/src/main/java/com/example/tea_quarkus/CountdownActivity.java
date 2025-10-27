package com.example.tea_quarkus;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
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
    private MaterialButton startButton, btnPause, btnRestart, backButton;
    private CircularProgressIndicator circularProgress;

    private ObjectAnimator rotationAnimator;
    private CountDownTimer timer;
    private boolean isRunning = false;
    private boolean isPaused = false;

    private long timeLeftMillis;
    private long totalTimeMillis;
    private float currentRotation = 0f;

    @Override
    protected void attachBaseContext(Context newBase) {
        String lang = newBase.getSharedPreferences("settings", MODE_PRIVATE)
                .getString("lang", "hu");
        super.attachBaseContext(LocaleHelper.setLocale(newBase, lang));
    }

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
        backButton = findViewById(R.id.backButton);

        // Get tea details from Intent
        String name = getIntent().getStringExtra("teaName");
        String description = getIntent().getStringExtra("teaDescription");
        int brewTime = getIntent().getIntExtra("brewTime", 3);
        int waterTemp = getIntent().getIntExtra("waterTemp", 80);
        String recommendation = getIntent().getStringExtra("recommendation");

        ArrayList<String> purposesHU = getIntent().getStringArrayListExtra("purposeArray");
        ArrayList<String> flavorsHU = getIntent().getStringArrayListExtra("flavorArray");
        ArrayList<String> dayTimeHU = getIntent().getStringArrayListExtra("dayTimeArray");

        // Map canonical HU tags to localized display labels for UI
        ArrayList<String> purposes = mapHuListToDisplay("purpose", purposesHU);
        ArrayList<String> flavors = mapHuListToDisplay("flavor", flavorsHU);
        ArrayList<String> dayTime = mapHuListToDisplay("dayTime", dayTimeHU);

        teaName.setText(name);

        backButton.setOnClickListener(v ->
                startActivity(new Intent(CountdownActivity.this, MainActivity.class)));

        // Build summary text
        StringBuilder summary = new StringBuilder();
        summary.append(description).append("\n\n");
        if (purposes != null && !purposes.isEmpty())
            summary.append(getString(R.string.label_purpose)).append(": ")
                    .append(String.join(", ", purposes)).append("\n");
        if (flavors != null && !flavors.isEmpty())
            summary.append(getString(R.string.label_flavor)).append(": ")
                    .append(String.join(", ", flavors)).append("\n");
        if (dayTime != null && !dayTime.isEmpty())
            summary.append(getString(R.string.label_daytime)).append(": ")
                    .append(String.join(", ", dayTime)).append("\n");
        summary.append(getString(R.string.label_reco_temp)).append(": ")
                .append(waterTemp).append("°C\n");
        summary.append(getString(R.string.label_tip)).append(": ")
                .append(recommendation);

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
                startButton.setText(getString(R.string.btn_resume));
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



        startButton.setText(getString(R.string.btn_running));
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

        startButton.setText(getString(R.string.btn_resume));
        startButton.setEnabled(true);
        btnPause.setEnabled(false);
    }

    private void resumeTimer() {
        startRotation(true);
        startCountdown(timeLeftMillis);
        startButton.setText(getString(R.string.btn_running));
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

        startButton.setText(getString(R.string.btn_start_brewing));
        startButton.setEnabled(true);
        btnPause.setEnabled(false);
    }

    private void completeTimer() {
        isRunning = false;
        isPaused = false;
        setKeepScreenOn(false);

        stopAnimations();
        circularProgress.setProgress(100);
        timerText.setText(getString(R.string.timer_done));

        Toast.makeText(this, getString(R.string.toast_enjoy), Toast.LENGTH_SHORT).show();

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

        startButton.setText(getString(R.string.btn_start_brewing));
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

    // Map canonical HU list to localized display labels by index mapping of synchronized arrays
    private ArrayList<String> mapHuListToDisplay(String type, ArrayList<String> huList) {
        ArrayList<String> result = new ArrayList<>();
        if (huList == null) return result;

        String[] display;
        String[] canonicalHU;
        switch (type) {
            case "purpose":
                display = getResources().getStringArray(R.array.purpose_options);
                canonicalHU = getResources().getStringArray(R.array.purpose_options_hu);
                break;
            case "flavor":
                display = getResources().getStringArray(R.array.flavor_options);
                canonicalHU = getResources().getStringArray(R.array.flavor_options_hu);
                break;
            case "dayTime":
                display = getResources().getStringArray(R.array.daytime_options);
                canonicalHU = getResources().getStringArray(R.array.daytime_options_hu);
                break;
            default:
                return huList;
        }

        for (String hu : huList) {
            int idx = indexOfIgnoreCase(canonicalHU, hu);
            if (idx >= 0 && idx < display.length) result.add(display[idx]);
        }
        return result;
    }

    private int indexOfIgnoreCase(String[] arr, String needle) {
        if (arr == null || needle == null) return -1;
        for (int i = 0; i < arr.length; i++) {
            if (needle.equalsIgnoreCase(arr[i])) return i;
        }
        return -1;
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
