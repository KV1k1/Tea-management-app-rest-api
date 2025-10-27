package com.example.tea_quarkus;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public class AdminTeaManagementActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TeaAdapter adapter;
    private List<Tea> teaList = new ArrayList<>();
    private Button btnAddTea, btnRefresh, btnBack;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_tea_management);

        tokenManager = new TokenManager(this);

        if (!isAdminUser()) {
            Toast.makeText(this, "Admin access required", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupRecyclerView();
        loadAllTeas();
    }

    private boolean isAdminUser() {
        String loginName = tokenManager.getLoginName();
        return "teaAdmin".equals(loginName);
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerView);
        btnAddTea = findViewById(R.id.btnAddTea);
        btnRefresh = findViewById(R.id.btnRefresh);
        btnBack = findViewById(R.id.btnBack);

        btnAddTea.setOnClickListener(v -> showAddTeaDialog());
        btnRefresh.setOnClickListener(v -> loadAllTeas());
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new TeaAdapter(teaList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Set click listeners for edit and delete
        adapter.setOnItemClickListener(new TeaAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(int position) {
                Tea tea = teaList.get(position);
                showEditTeaDialog(tea);
            }

            @Override
            public void onDeleteClick(int position) {
                Tea tea = teaList.get(position);
                showDeleteConfirmationDialog(tea);
            }
        });
    }

    private void loadAllTeas() {
        String token = tokenManager.getToken();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.1.196:8080")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        TeaApi teaApi = retrofit.create(TeaApi.class);
        Call<List<Tea>> call = teaApi.getAllTeasAdmin("Bearer " + token);

        call.enqueue(new Callback<List<Tea>>() {
            @Override
            public void onResponse(Call<List<Tea>> call, Response<List<Tea>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    teaList.clear();
                    teaList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    String msg = "Failed to load teas (" + response.code() + ")";
                    try {
                        if (response.errorBody() != null) {
                            String body = response.errorBody().string();
                            if (!body.isEmpty()) msg += ": " + body;
                        }
                    } catch (Exception ignored) {}
                    if (response.code() == 401 || response.code() == 403) {
                        msg = "Unauthorized (" + response.code() + "): Admin access required. Please re-login with an admin account.";
                    }
                    Toast.makeText(AdminTeaManagementActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Tea>> call, Throwable t) {
                Toast.makeText(AdminTeaManagementActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddTeaDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Tea");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_tea_form, null);
        builder.setView(dialogView);

        EditText etName = dialogView.findViewById(R.id.etTeaName);
        EditText etNameEn = dialogView.findViewById(R.id.etTeaNameEn);
        EditText etDescription = dialogView.findViewById(R.id.etDescription);
        EditText etDescriptionEn = dialogView.findViewById(R.id.etDescriptionEn);
        EditText etRecommendation = dialogView.findViewById(R.id.etRecommendation);
        EditText etRecommendationEn = dialogView.findViewById(R.id.etRecommendationEn);
        EditText etBrewTime = dialogView.findViewById(R.id.etBrewTime);
        EditText etWaterTemp = dialogView.findViewById(R.id.etWaterTemp);
        EditText etFlavors = dialogView.findViewById(R.id.etFlavors);
        EditText etPurposes = dialogView.findViewById(R.id.etPurposes);
        EditText etDayTimes = dialogView.findViewById(R.id.etDayTimes);

        setupMultiSelectField(etPurposes, getString(R.string.title_select_purposes), getResources().getStringArray(R.array.purpose_options));
        setupMultiSelectField(etFlavors, getString(R.string.title_select_flavors), getResources().getStringArray(R.array.flavor_options));
        setupMultiSelectField(etDayTimes, getString(R.string.title_select_daytimes), getResources().getStringArray(R.array.daytime_options));

        builder.setPositiveButton("Add", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String nameEn = etNameEn.getText().toString().trim();
            String description = etDescription.getText().toString().trim();
            String descriptionEn = etDescriptionEn.getText().toString().trim();
            String recommendation = etRecommendation.getText().toString().trim();
            String recommendationEn = etRecommendationEn.getText().toString().trim();
            String brewTimeStr = etBrewTime.getText().toString().trim();
            String waterTempStr = etWaterTemp.getText().toString().trim();

            if (name.isEmpty() || description.isEmpty() || brewTimeStr.isEmpty() || waterTempStr.isEmpty()) {
                Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int brewTime = Integer.parseInt(brewTimeStr);
                int waterTemp = Integer.parseInt(waterTempStr);

                Tea tea = new Tea();
                tea.setName(name);
                tea.setNameEn(nameEn);
                tea.setDescription(description);
                tea.setDescriptionEn(descriptionEn);
                tea.setRecommendation(recommendation);
                tea.setRecommendationEn(recommendationEn);
                tea.setBrewTime(brewTime);
                tea.setWaterTemp(waterTemp);
                tea.setFlavor(parseCommaSeparated(etFlavors.getText().toString()));
                tea.setPurpose(parseCommaSeparated(etPurposes.getText().toString()));
                tea.setDayTime(parseCommaSeparated(etDayTimes.getText().toString()));

                createTea(tea);

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showEditTeaDialog(Tea tea) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Tea");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_tea_form, null);
        builder.setView(dialogView);

        EditText etName = dialogView.findViewById(R.id.etTeaName);
        EditText etNameEn = dialogView.findViewById(R.id.etTeaNameEn);
        EditText etDescription = dialogView.findViewById(R.id.etDescription);
        EditText etDescriptionEn = dialogView.findViewById(R.id.etDescriptionEn);
        EditText etRecommendation = dialogView.findViewById(R.id.etRecommendation);
        EditText etRecommendationEn = dialogView.findViewById(R.id.etRecommendationEn);
        EditText etBrewTime = dialogView.findViewById(R.id.etBrewTime);
        EditText etWaterTemp = dialogView.findViewById(R.id.etWaterTemp);
        EditText etFlavors = dialogView.findViewById(R.id.etFlavors);
        EditText etPurposes = dialogView.findViewById(R.id.etPurposes);
        EditText etDayTimes = dialogView.findViewById(R.id.etDayTimes);

        // Pre-fill with existing data
        etName.setText(tea.getName());
        etNameEn.setText(tea.getNameEn());
        etDescription.setText(tea.getDescription());
        etDescriptionEn.setText(tea.getDescriptionEn());
        etRecommendation.setText(tea.getRecommendation());
        etRecommendationEn.setText(tea.getRecommendationEn());
        etBrewTime.setText(String.valueOf(tea.getBrewTime()));
        etWaterTemp.setText(String.valueOf(tea.getWaterTemp()));
        etFlavors.setText(listToCommaSeparated(tea.getFlavor()));
        etPurposes.setText(listToCommaSeparated(tea.getPurpose()));
        etDayTimes.setText(listToCommaSeparated(tea.getDayTime()));

        setupMultiSelectField(etPurposes, getString(R.string.title_select_purposes), getResources().getStringArray(R.array.purpose_options));
        setupMultiSelectField(etFlavors, getString(R.string.title_select_flavors), getResources().getStringArray(R.array.flavor_options));
        setupMultiSelectField(etDayTimes, getString(R.string.title_select_daytimes), getResources().getStringArray(R.array.daytime_options));

        builder.setPositiveButton("Update", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String nameEn = etNameEn.getText().toString().trim();
            String description = etDescription.getText().toString().trim();
            String descriptionEn = etDescriptionEn.getText().toString().trim();
            String recommendation = etRecommendation.getText().toString().trim();
            String recommendationEn = etRecommendationEn.getText().toString().trim();
            String brewTimeStr = etBrewTime.getText().toString().trim();
            String waterTempStr = etWaterTemp.getText().toString().trim();

            if (name.isEmpty() || description.isEmpty() || brewTimeStr.isEmpty() || waterTempStr.isEmpty()) {
                Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int brewTime = Integer.parseInt(brewTimeStr);
                int waterTemp = Integer.parseInt(waterTempStr);

                Tea updatedTea = new Tea();
                updatedTea.setName(name);
                updatedTea.setNameEn(nameEn);
                updatedTea.setDescription(description);
                updatedTea.setDescriptionEn(descriptionEn);
                updatedTea.setRecommendation(recommendation);
                updatedTea.setRecommendationEn(recommendationEn);
                updatedTea.setBrewTime(brewTime);
                updatedTea.setWaterTemp(waterTemp);
                updatedTea.setFlavor(parseCommaSeparated(etFlavors.getText().toString()));
                updatedTea.setPurpose(parseCommaSeparated(etPurposes.getText().toString()));
                updatedTea.setDayTime(parseCommaSeparated(etDayTimes.getText().toString()));

                updateTea(tea.getId(), updatedTea);

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showDeleteConfirmationDialog(Tea tea) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Tea")
                .setMessage("Are you sure you want to delete \"" + tea.getName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> deleteTea(tea.getId()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void createTea(Tea tea) {
        String token = tokenManager.getToken();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.1.196:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        TeaApi teaApi = retrofit.create(TeaApi.class);
        Call<Tea> call = teaApi.createTeaAdmin("Bearer " + token, tea);

        call.enqueue(new Callback<Tea>() {
            @Override
            public void onResponse(Call<Tea> call, Response<Tea> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AdminTeaManagementActivity.this, "Tea created successfully!", Toast.LENGTH_SHORT).show();
                    loadAllTeas();
                } else {
                    String msg = "Failed to create tea (" + response.code() + ")";
                    try {
                        if (response.errorBody() != null) {
                            String body = response.errorBody().string();
                            if (!body.isEmpty()) msg += ": " + body;
                        }
                    } catch (Exception ignored) {}
                    if (response.code() == 401 || response.code() == 403) {
                        msg = "Unauthorized (" + response.code() + "): Admin access required.";
                    }
                    Toast.makeText(AdminTeaManagementActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Tea> call, Throwable t) {
                Toast.makeText(AdminTeaManagementActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTea(long teaId, Tea tea) {
        String token = tokenManager.getToken();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.1.196:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        TeaApi teaApi = retrofit.create(TeaApi.class);
        Call<Tea> call = teaApi.updateTeaAdmin("Bearer " + token, teaId, tea);

        call.enqueue(new Callback<Tea>() {
            @Override
            public void onResponse(Call<Tea> call, Response<Tea> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AdminTeaManagementActivity.this, "Tea updated successfully!", Toast.LENGTH_SHORT).show();
                    loadAllTeas();
                } else {
                    String msg = "Failed to update tea (" + response.code() + ")";
                    try {
                        if (response.errorBody() != null) {
                            String body = response.errorBody().string();
                            if (!body.isEmpty()) msg += ": " + body;
                        }
                    } catch (Exception ignored) {}
                    if (response.code() == 401 || response.code() == 403) {
                        msg = "Unauthorized (" + response.code() + "): Admin access required.";
                    }
                    Toast.makeText(AdminTeaManagementActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Tea> call, Throwable t) {
                Toast.makeText(AdminTeaManagementActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteTea(long teaId) {
        String token = tokenManager.getToken();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.1.196:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        TeaApi teaApi = retrofit.create(TeaApi.class);
        Call<Void> call = teaApi.deleteTeaAdmin("Bearer " + token, teaId);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AdminTeaManagementActivity.this, "Tea deleted successfully!", Toast.LENGTH_SHORT).show();
                    loadAllTeas();
                } else {
                    String msg = "Failed to delete tea (" + response.code() + ")";
                    try {
                        if (response.errorBody() != null) {
                            String body = response.errorBody().string();
                            if (!body.isEmpty()) msg += ": " + body;
                        }
                    } catch (Exception ignored) {}
                    if (response.code() == 401 || response.code() == 403) {
                        msg = "Unauthorized (" + response.code() + "): Admin access required.";
                    }
                    Toast.makeText(AdminTeaManagementActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(AdminTeaManagementActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<String> parseCommaSeparated(String input) {
        if (input == null || input.trim().isEmpty()) return new ArrayList<>();
        List<String> result = new ArrayList<>();
        String[] parts = input.split(",");
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) result.add(trimmed);
        }
        return result;
    }

    private String listToCommaSeparated(List<String> list) {
        if (list == null || list.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i < list.size() - 1) sb.append(", ");
        }
        return sb.toString();
    }

    // Helper to load canonical HU arrays
    private String[] PURPOSE_OPTIONS_HU() { return getResources().getStringArray(R.array.purpose_options_hu); }
    private String[] FLAVOR_OPTIONS_HU()  { return getResources().getStringArray(R.array.flavor_options_hu); }
    private String[] DAYTIME_OPTIONS_HU() { return getResources().getStringArray(R.array.daytime_options_hu); }

    private void setupMultiSelectField(EditText target, String title, String[] options) {
        // Make field read-only and clickable
        target.setFocusable(false);
        target.setClickable(true);
        target.setLongClickable(false);
        target.setKeyListener(null);

        target.setOnClickListener(v -> showMultiSelectDialog(target, title, options));
    }

    private void showMultiSelectDialog(EditText target, String title, String[] displayOptions) {
        // Determine which category this field represents and get canonical HU arrays
        String[] canonicalHU;
        if (target.getId() == R.id.etPurposes) {
            canonicalHU = PURPOSE_OPTIONS_HU();
        } else if (target.getId() == R.id.etFlavors) {
            canonicalHU = FLAVOR_OPTIONS_HU();
        } else { // etDayTimes
            canonicalHU = DAYTIME_OPTIONS_HU();
        }

        // Build mapping index between display and canonical HU by position
        // Preselect based on current field value (stored as HU canonical)
        List<String> currentHU = parseCommaSeparated(target.getText() != null ? target.getText().toString() : "");
        // Convert current HU to display for checked[] toggle
        boolean[] checked = new boolean[displayOptions.length];
        for (int i = 0; i < displayOptions.length; i++) {
            final String hu = canonicalHU[i];
            checked[i] = currentHU.stream().anyMatch(s -> s.equalsIgnoreCase(hu));
        }

        // Start selected list as display labels based on current HU set
        List<String> selectedDisplay = new ArrayList<>();
        for (int i = 0; i < displayOptions.length; i++) {
            if (checked[i]) selectedDisplay.add(displayOptions[i]);
        }

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMultiChoiceItems(displayOptions, checked, (dialog, which, isChecked) -> {
                    String value = displayOptions[which];
                    if (isChecked) {
                        if (selectedDisplay.stream().noneMatch(s -> s.equalsIgnoreCase(value))) {
                            selectedDisplay.add(value);
                        }
                    } else {
                        selectedDisplay.removeIf(s -> s.equalsIgnoreCase(value));
                    }
                })
                .setPositiveButton(getString(R.string.dialog_ok), (dialog, which) -> {
                    // Map selected display labels back to canonical HU by index
                    List<String> selectedHU = new ArrayList<>();
                    for (int i = 0; i < displayOptions.length; i++) {
                        String disp = displayOptions[i];
                        if (selectedDisplay.stream().anyMatch(s -> s.equalsIgnoreCase(disp))) {
                            selectedHU.add(canonicalHU[i]);
                        }
                    }
                    target.setText(listToCommaSeparated(selectedHU));
                })
                .setNegativeButton(getString(R.string.dialog_cancel), null)
                .show();
    }
}