package com.example.lab3;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CafeRecViewAdapter extends RecyclerView.Adapter<CafeRecViewAdapter.ViewHolder> {

    private ArrayList<CafeModel> cafes = new ArrayList<>();
    private Context context;

    public CafeRecViewAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_cafe_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.cafeName.setText(cafes.get(position).getName());
        holder.cafeAddress.setText(cafes.get(position).getVicinity());
        holder.cafeRating.setText(cafes.get(position).getRating()+"");
        holder.cafeTotalRating.setText("(" + cafes.get(position).getTotalRatings() + ")");
    }

    @Override
    public int getItemCount() {
        return cafes.size();
    }

    public void setCafes(ArrayList<CafeModel> cafes) {
        this.cafes = cafes;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private TextView cafeName, cafeAddress, cafeRating, cafeTotalRating;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            cafeName = itemView.findViewById(R.id.cafeName);
            cafeAddress = itemView.findViewById(R.id.cafeAddress);
            cafeRating = itemView.findViewById(R.id.cafeRating);
            cafeTotalRating = itemView.findViewById(R.id.cafeTotalRatings);
        }
    }
}
