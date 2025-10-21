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

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TeaSelectionActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TeaAdapter adapter;
    private final ArrayList<Tea> teaList = new ArrayList<>();
    private final ArrayList<Tea> filteredList = new ArrayList<>();
    private SearchView searchView;
    private Button btnSortAZ, btnSortZA;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tea_selection);

        recyclerView = findViewById(R.id.teaRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        );

        adapter = new TeaAdapter(this, filteredList);
        recyclerView.setAdapter(adapter);

        // ✅ Replace with your Quarkus server IP address (important if testing on a device)
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/") // example: use your PC's local IP
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        TeaApi teaApi = retrofit.create(TeaApi.class);

        // Fetch teas from backend
        teaApi.getAllTeas().enqueue(new Callback<List<Tea>>() {
            @Override
            public void onResponse(Call<List<Tea>> call, Response<List<Tea>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    teaList.clear();
                    teaList.addAll(response.body());

                    // Sort alphabetically A–Z initially
                    teaList.sort((t1, t2) -> t1.getName().compareToIgnoreCase(t2.getName()));

                    filteredList.clear();
                    filteredList.addAll(teaList);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Tea>> call, Throwable t) {
                t.printStackTrace();
            }
        });

        // Search setup
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

        // Sorting buttons
        btnSortAZ = findViewById(R.id.btnSortAZ);
        btnSortZA = findViewById(R.id.btnSortZA);

        btnSortAZ.setOnClickListener(v -> {
            filteredList.sort((t1, t2) -> t1.getName().compareToIgnoreCase(t2.getName()));
            adapter.notifyDataSetChanged();
        });

        btnSortZA.setOnClickListener(v -> {
            filteredList.sort((t1, t2) -> t2.getName().compareToIgnoreCase(t1.getName()));
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
                if (tea.getName().toLowerCase().contains(lowerQuery)) {
                    filteredList.add(tea);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    // RecyclerView Adapter
    static class TeaAdapter extends RecyclerView.Adapter<TeaAdapter.TeaViewHolder> {
        private final Context context;
        private final ArrayList<Tea> teas;

        TeaAdapter(Context context, ArrayList<Tea> teas) {
            this.context = context;
            this.teas = teas;
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
            holder.tvName.setText(tea.getName());
            holder.tvDescription.setText(tea.getDescription());
            holder.tvBrewTime.setText("⏱ " + tea.getBrewTime() + " min");
            holder.tvTemp.setText("🌡 " + tea.getWaterTemp() + "°C");

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, CountdownActivity.class);
                intent.putExtra("teaName", tea.getName());
                intent.putExtra("teaDescription", tea.getDescription());
                intent.putExtra("brewTime", tea.getBrewTime());
                intent.putExtra("waterTemp", tea.getWaterTemp());
                intent.putExtra("recommendation", tea.getRecommendation());
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
