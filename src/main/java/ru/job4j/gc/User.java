package ru.job4j.gc;

public class User {

    private int id;

    private String string;

    public User(int id, String string) {
        this.id = id;
        this.string = string;
    }

    public static void main(String[] args) {
        for (int i = 0; i < 15000; i++) {
            new User(i, "N" + i);
        }
        System.gc();
    }
}


