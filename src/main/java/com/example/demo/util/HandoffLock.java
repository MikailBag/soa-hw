package com.example.demo.util;

public class HandoffLock {
    public static final Object IS_EMPTY = new Object();

    private static class Node {
        Thread thread;
    }
}
