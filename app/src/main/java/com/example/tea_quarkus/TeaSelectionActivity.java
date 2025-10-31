package com.example.tea_quarkus;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.os.ConfigurationCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.widget.Toast;

public class TeaSelectionActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TeaAdapter adapter;
    private final ArrayList<Tea> teaList = new ArrayList<>();
    private final ArrayList<Tea> filteredList = new ArrayList<>();
    private boolean isEnglish;
    private SearchView searchView;
    private Button btnSortAZ, btnSortZA;

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
        setContentView(R.layout.activity_tea_selection);

        isEnglish = getSharedPreferences("settings", MODE_PRIVATE)
                .getString("lang", "hu").equalsIgnoreCase("en");

        recyclerView = findViewById(R.id.teaRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        );

        adapter = new TeaAdapter(this, filteredList, this::isEnglishSelected);
        recyclerView.setAdapter(adapter);

        TeaApi teaApi = ApiClient.getTeaApi();

        teaApi.getAllTeas().enqueue(new Callback<List<Tea>>() {
            @Override
            public void onResponse(Call<List<Tea>> call, Response<List<Tea>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    teaList.clear();
                    teaList.addAll(response.body());

                    sortTeaList();

                    filteredList.clear();
                    filteredList.addAll(teaList);
                    adapter.notifyDataSetChanged();
                } else {
                    ApiResponseHandler.handleResponse(TeaSelectionActivity.this, response);
                }
            }

            @Override
            public void onFailure(Call<List<Tea>> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(TeaSelectionActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterTeas(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterTeas(newText);
                return true;
            }
        });

        btnSortAZ = findViewById(R.id.btnSortAZ);
        btnSortZA = findViewById(R.id.btnSortZA);

        btnSortAZ.setOnClickListener(v -> {
            filteredList.sort((t1, t2) -> getSortableName(t1).compareToIgnoreCase(getSortableName(t2)));
            adapter.notifyDataSetChanged();
        });

        btnSortZA.setOnClickListener(v -> {
            filteredList.sort((t1, t2) -> getSortableName(t2).compareToIgnoreCase(getSortableName(t1)));
            adapter.notifyDataSetChanged();
        });
    }

    private void filterTeas(String query) {
        filteredList.clear();
        if (query == null || query.isEmpty()) {
            filteredList.addAll(teaList);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Tea tea : teaList) {
                if (getSortableName(tea).toLowerCase().contains(lowerQuery)) {
                    filteredList.add(tea);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private boolean isEnglishSelected() {
        return isEnglish;
    }

    private void sortTeaList() {
        teaList.sort((t1, t2) -> getSortableName(t1).compareToIgnoreCase(getSortableName(t2)));
    }

    private String getSortableName(Tea tea) {
        String hu = tea.getName() != null ? tea.getName() : "";
        String en = tea.getNameEn() != null ? tea.getNameEn() : "";
        return isEnglish ? (en.isEmpty() ? hu : en) : hu;
    }

    static class TeaAdapter extends RecyclerView.Adapter<TeaAdapter.TeaViewHolder> {
        private final Context context;
        private final List<Tea> teas;
        private final Supplier<Boolean> isEnglishSupplier;

        TeaAdapter(Context context, List<Tea> teas, Supplier<Boolean> isEnglishSupplier) {
            this.context = context;
            this.teas = teas;
            this.isEnglishSupplier = isEnglishSupplier;
        }

        @NonNull
        @Override
        public TeaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_tea, parent, false);
            return new TeaViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TeaViewHolder holder, int position) {
            Tea tea = teas.get(position);
            boolean isEn = isEnglishSupplier.get();
            String displayName = isEn && tea.getNameEn() != null && !tea.getNameEn().isEmpty()
                    ? tea.getNameEn() : tea.getName();
            String displayDesc = isEn && tea.getDescriptionEn() != null && !tea.getDescriptionEn().isEmpty()
                    ? tea.getDescriptionEn() : tea.getDescription();
            holder.tvName.setText(displayName);
            holder.tvDescription.setText(displayDesc);
            holder.tvBrewTime.setText("⏱ " + tea.getBrewTime() + " min");
            holder.tvTemp.setText("🌡 " + tea.getWaterTemp() + "°C");

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, CountdownActivity.class);
                boolean isEnConfig = isEnglishSupplier.get();
                String extraName = isEnConfig && tea.getNameEn() != null && !tea.getNameEn().isEmpty() ? tea.getNameEn() : tea.getName();
                String extraDesc = isEnConfig && tea.getDescriptionEn() != null && !tea.getDescriptionEn().isEmpty() ? tea.getDescriptionEn() : tea.getDescription();
                String extraReco = isEnConfig && tea.getRecommendationEn() != null && !tea.getRecommendationEn().isEmpty() ? tea.getRecommendationEn() : tea.getRecommendation();
                intent.putExtra("teaName", extraName);
                intent.putExtra("teaDescription", extraDesc);
                intent.putExtra("brewTime", tea.getBrewTime());
                intent.putExtra("waterTemp", tea.getWaterTemp());
                intent.putExtra("recommendation", extraReco);
                intent.putStringArrayListExtra("purposeArray", new ArrayList<>(tea.getPurpose()));
                intent.putStringArrayListExtra("flavorArray", new ArrayList<>(tea.getFlavor()));
                intent.putStringArrayListExtra("dayTimeArray", new ArrayList<>(tea.getDayTime()));
                context.startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return teas.size();
        }

        static class TeaViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvDescription, tvBrewTime, tvTemp;

            TeaViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvTeaName);
                tvDescription = itemView.findViewById(R.id.tvTeaDescription);
                tvBrewTime = itemView.findViewById(R.id.tvBrewTime);
                tvTemp = itemView.findViewById(R.id.tvTemp);
            }
        }
    }
}