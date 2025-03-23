package com.example.smartschoolbusapp;

public class DateHeader extends ChatItem {
    private String date;

    public DateHeader(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    @Override
    public int getType() {
        return TYPE_DATE_HEADER;
    }
}