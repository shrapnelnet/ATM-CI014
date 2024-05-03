package com.shr4pnel.atm;

/**
 * Used as an intermediary between Database and Controls to store the current username and balance
 * Password cannot be directly accessed. Please don't!
 * @author <a href="https://github.com/shrapnelnet">Tyler</a>
 * @version 1.4.0
 * @since 1.0.0
 */
public class Account {
    /** The username of the account */
    String username;
    /** The balance of the account */
    int balance;

    /**
     * Constructor for Account
     * @param username The current users username
     * @param balance The balance of the user
     * @since 1.0.0
     */
    public Account(String username, int balance) {
        this.username = username;
        this.balance = balance;
    }
}
