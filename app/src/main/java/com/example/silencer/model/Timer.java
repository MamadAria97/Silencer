package com.example.silencer.model;

public class Timer {
    public Integer startTimeHour;
    public Integer startTimeMinute;
    public Integer EndTimeHour;
    public Integer EndTimeMinute;


    public int getRequestCode(boolean isStart) {
        return isStart ? (startTimeHour * 100 + startTimeMinute) : (EndTimeHour * 100 + EndTimeMinute);
    }
}
