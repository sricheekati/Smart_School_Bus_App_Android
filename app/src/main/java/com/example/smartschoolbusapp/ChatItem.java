package com.example.smartschoolbusapp;

public abstract class ChatItem {
    public static final int TYPE_MESSAGE = 0;
    public static final int TYPE_DATE_HEADER = 1;

    public abstract int getType();
}