package com.example.tea_quarkus;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TeaAdapter extends RecyclerView.Adapter<TeaAdapter.TeaViewHolder> {

    private List<Tea> teaList;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onEditClick(int position);
        void onDeleteClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public TeaAdapter(List<Tea> teaList) {
        this.teaList = teaList;
    }

    @NonNull
    @Override
    public TeaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tea_admin, parent, false);
        return new TeaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeaViewHolder holder, int position) {
        Tea tea = teaList.get(position);
        holder.tvName.setText(tea.getName());
        holder.tvDescription.setText(tea.getDescription());
        holder.tvBrewTime.setText("Brew: " + tea.getBrewTime() + " min");
        holder.tvTemp.setText("Temp: " + tea.getWaterTemp() + "°C");

        holder.btnEdit.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onEditClick(position);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onDeleteClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return teaList.size();
    }

    static class TeaViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDescription, tvBrewTime, tvTemp;
        Button btnEdit, btnDelete;

        TeaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvTeaName);
            tvDescription = itemView.findViewById(R.id.tvTeaDescription);
            tvBrewTime = itemView.findViewById(R.id.tvBrewTime);
            tvTemp = itemView.findViewById(R.id.tvTemp);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}