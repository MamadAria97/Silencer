package com.example.silencer.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.silencer.R;
import com.example.silencer.model.Timer;

import java.util.List;

public class SetTimeAdapter extends RecyclerView.Adapter<SetTimeAdapter.TimerViewHolder> {

    private List<Timer> timerModel;
    private OnAddressDeleteListener deleteListener;

    public SetTimeAdapter(List<Timer> timerModel, OnAddressDeleteListener deleteListener) {
        this.timerModel = timerModel;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public TimerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.timer_list_row, parent, false);
        return new TimerViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull TimerViewHolder holder, int position) {
        Timer timer = timerModel.get(position);

        holder.txt.setText("From " + timer.startTimeHour + ":" + timer.startTimeMinute + " To " + timer.EndTimeHour + ":" + timer.EndTimeMinute);

        holder.btnDelete.setOnClickListener(v -> deleteListener.onDelete(position));
    }

    @Override
    public int getItemCount() {
        return timerModel.size();
    }

    public static class TimerViewHolder extends RecyclerView.ViewHolder {
        TextView txt;
        ImageButton btnDelete;

        public TimerViewHolder(@NonNull View itemView) {
            super(itemView);
            txt = itemView.findViewById(R.id.txt);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    public interface OnAddressDeleteListener {
        void onDelete(int position);
    }
}
