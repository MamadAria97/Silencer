package com.example.silencer.activity;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.silencer.adapter.SetTimeAdapter;
import com.example.silencer.classes.SilentModeReceiver;
import com.example.silencer.databinding.ActivityMainBinding;
import com.example.silencer.model.Timer;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;

    private boolean isStartTime = true;

    private static final String PREFS_NAME = "TimerListSelceted";
    private static final String KEY_Timers_LIST = "TimerList";
    private SetTimeAdapter adapter;
    private List<Timer> timerListModel;
    Timer timerModel = new Timer();

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            }
        }

        timerListModel = loadTimersList();
        adapter = new SetTimeAdapter(timerListModel, this::removeTimersList);
        binding.recycler.setLayoutManager(new LinearLayoutManager(this));
        binding.recycler.setAdapter(adapter);

        binding.btnSetSchedule.setOnClickListener(v -> {
            if (isStartTime) {
                checkAndRequestDndPermission();

                timerModel.startTimeHour = binding.startTimePicker.getHour();
                timerModel.startTimeMinute = binding.startTimePicker.getMinute();
                int timerIdStart = timerModel.startTimeHour * 100 + timerModel.startTimeMinute;

                setAlarm(timerModel.startTimeHour, timerModel.startTimeMinute, true, timerIdStart);

                isStartTime = false;
                binding.timerTxt.setText("Set Stop For Silencer");
                binding.btnSetSchedule.setText("Stop Silencer");
                binding.startTimePicker.setVisibility(View.GONE);
                binding.endTimePicker.setVisibility(View.VISIBLE);
            } else {
                timerModel.EndTimeHour = binding.endTimePicker.getHour();
                timerModel.EndTimeMinute = binding.endTimePicker.getMinute();
                int timerIdEnd = timerModel.EndTimeHour * 100 + timerModel.EndTimeMinute;

                setAlarm(timerModel.EndTimeHour, timerModel.EndTimeMinute, false, timerIdEnd);

                Timer newTimerModel = new Timer();
                newTimerModel.startTimeHour = timerModel.startTimeHour;
                newTimerModel.startTimeMinute = timerModel.startTimeMinute;
                newTimerModel.EndTimeHour = timerModel.EndTimeHour;
                newTimerModel.EndTimeMinute = timerModel.EndTimeMinute;

                timerListModel.add(newTimerModel);
                adapter.notifyItemInserted(timerListModel.size() - 1);
                saveTimersList();

                isStartTime = true;
                binding.timerTxt.setText("Set Start For Silencer");
                binding.btnSetSchedule.setText("Start Silencer");
                binding.startTimePicker.setVisibility(View.VISIBLE);
                binding.endTimePicker.setVisibility(View.GONE);
            }
        });
    }


    private void checkAndRequestDndPermission() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null && !notificationManager.isNotificationPolicyAccessGranted()) {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            startActivity(intent);
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private void setAlarm(int hour, int minute, boolean enableSilent, int timerId) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, SilentModeReceiver.class);
        intent.putExtra("enableSilent", enableSilent);
        intent.putExtra("timerId", timerId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                timerId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }

    private void cancelAlarm(int timerId) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, SilentModeReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                timerId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    private void removeTimersList(int position) {
        Timer timer = timerListModel.get(position);

        cancelAlarm(timer.getRequestCode(true));
        cancelAlarm(timer.getRequestCode(false));

        timerListModel.remove(position);
        adapter.notifyItemRemoved(position);
        saveTimersList();
    }

    private void saveTimersList() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(timerListModel);

        editor.putString(KEY_Timers_LIST, json);
        editor.apply();
    }

    private List<Timer> loadTimersList() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString(KEY_Timers_LIST, null);

        if (json == null || json.equals("null")) {
            return new ArrayList<>();
        }

        Type type = new TypeToken<List<Timer>>() {
        }.getType();
        try {
            return gson.fromJson(json, type);
        } catch (JsonSyntaxException e) {
            return new ArrayList<>();
        }
    }
}
