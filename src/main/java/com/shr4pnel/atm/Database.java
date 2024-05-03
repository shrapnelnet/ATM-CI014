package com.shr4pnel.atm;

import at.favre.lib.crypto.bcrypt.BCrypt;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

/**
 * An ORM to handle queries to the database. Uses a local SQLite3 database.
 * @author <a href="https://github.com/shrapnelnet">Tyler</a>
 * @version 1.4.0
 * @since 1.0.0
 */
public class Database {
    /**
     * Creates all necessary tables and columns for the database to be functional.
     * This is not a constructor, because the database should not be reinitialized on each instantiation.
     */
    protected void initialize() {
        try (
            Connection connection = DriverManager.getConnection("jdbc:sqlite:accounts.db");
            Statement statement = connection.createStatement()
        ) {
            Log.trace("Database::initialize: Initializing database at accounts.db");
            statement.executeUpdate("create table if not exists accounts (uid character(36) primary key, username varchar unique not null, hash varchar, balance int)");
        } catch (SQLException err) {
            Log.error("Database:initialize: Fatal error on initialization");
        }
    }

    /**
     * Creates a new account in the accounts table
     * @param username The username of the new user
     * @param password The plaintext password
     * @return A boolean representing the success of the insert operation
     */
    protected boolean createAccount(String username, String password) {
        try (
            Connection connection = DriverManager.getConnection("jdbc:sqlite:accounts.db");
            PreparedStatement statement = connection.prepareStatement("insert into accounts values (?, ?, ?, ?)")
        ) {
            Log.trace("Database::createAccount: Creating account " + username);
            final int balance = 500;
            final String uid = UUID.randomUUID().toString();
            final String hash = BCrypt.withDefaults().hashToString(12, password.toCharArray());
            statement.setString(1, uid);
            statement.setString(2, username);
            statement.setString(3, hash);
            statement.setInt(4, balance);
            return statement.executeUpdate() != 0;
        } catch (SQLException err) {
            Log.warn("Database:createAccount: Fatal error while creating account");
            Log.warn(err.getMessage());
            return false;
        }
    }

    /**
     * Used to log a user in.
     * Checks if username is present in accounts, then validates the hash against the password.
     * @param username The username of the user logging in
     * @param password The password of the user logging in
     * @return An account instance on success, or null if the user does not exist or the password is incorrect
     */
    protected Account tryLogin(String username, String password) {
        try (
            Connection connection = DriverManager.getConnection("jdbc:sqlite:accounts.db");
            PreparedStatement statement = connection.prepareStatement(
                "select hash, balance from accounts where username=?")
        ) {
            statement.setString(1, username);
            ResultSet results = statement.executeQuery();
            results.next();
            String hash = results.getString("hash");
            int balance = results.getInt("balance");
            if (hash == null) {
                Log.trace("Database::tryLogin: Record does not exist in database");
                return null;
            }
            BCrypt.Result success =
                BCrypt.verifyer().verify(password.toCharArray(), hash.getBytes());
            if (success.verified) {
                return new Account(username, balance);
            }
            Log.trace("Database::tryLogin: Hash didn't match, password wrong");
            return null;
        } catch (SQLException err) {
            Log.warn("Database:tryLogin: error while logging in");
            Log.warn(err.getMessage());
            return null;
        }
    }

    /**
     * Deposits money into the users entry in the database
     * @param username The username of the current user depositing money
     * @param amount The amount of money to be deposited
     * @return A boolean representing the success of the query
     */
    protected boolean deposit(String username, int amount) {
        try (
            Connection connection = DriverManager.getConnection("jdbc:sqlite:accounts.db");
            PreparedStatement statement = connection.prepareStatement(
                "update accounts set balance=balance+? where username=?")
        ) {
            statement.setInt(1, amount);
            statement.setString(2, username);
            Log.trace("Database::deposit: Depositing £" + amount + " into account: " + username);
            return statement.executeUpdate() != 0;
        } catch (SQLException err) {
            Log.warn("Database:deposit: Fatal error while depositing");
            Log.warn(err.getMessage());
            return false;
        }
    }

    /**
     * Withdraws money out of the users entry in the database
     * @param username The username of the current user withdrawing money
     * @param amount The amount of money to be withdrawn
     * @return A boolean representing the success of the query
     */
    protected boolean withdraw(String username, int amount) {
        try (
            Connection connection = DriverManager.getConnection("jdbc:sqlite:accounts.db");
            PreparedStatement statement = connection.prepareStatement(
                "update accounts set balance=balance-? where username=?")
        ) {
            statement.setInt(1, amount);
            statement.setString(2, username);
            Log.trace("Database::withdraw: Withdrawing £" + amount + " from account: " + username);
            return statement.executeUpdate() != 0;
        } catch (SQLException err) {
            Log.warn("Database:withdraw: Fatal error while withdrawing");
            Log.warn(err.getMessage());
            return false;
        }
    }

    /**
     * Gets the balance of the current user
     * @param username The username of the currently logged-in user
     * @return An integer representing the balance of the user
     */
    protected int balance(String username) {
        try (
            Connection connection = DriverManager.getConnection("jdbc:sqlite:accounts.db");
            PreparedStatement statement = connection.prepareStatement(
                "select balance from accounts where username=?")
        ) {
            statement.setString(1, username);
            ResultSet rs = statement.executeQuery();
            return rs.getInt("balance");
        } catch (SQLException err) {
            Log.warn(err.getMessage());
            Log.warn("Database::balance: Failed to get account balance");
            return -1;
        }
    }

}
