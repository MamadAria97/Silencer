package com.example.silencer.model;

import java.util.ArrayList;
import java.util.List;

public class Timer {
    public Integer startTimeHour;
    public Integer startTimeMinute;
    public Integer EndTimeHour;
    public Integer EndTimeMinute;
    public List<String> selectedDays = new ArrayList<>();

    public int getRequestCode(boolean isStart) {
        return isStart ? (startTimeHour * 100 + startTimeMinute) : (EndTimeHour * 100 + EndTimeMinute);
    }
}

