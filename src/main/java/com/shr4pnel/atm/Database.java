package com.shr4pnel.atm;

import at.favre.lib.crypto.bcrypt.BCrypt;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * An ORM to handle queries to the database. Uses a local SQLite3 database.
 *
 * @author <a href="https://github.com/shrapnelnet">Tyler</a>
 * @version 1.4.0
 * @since 1.0.0
 */
public class Database {
    /**
     * Logger instance for Database
     */
    private final Logger databaseLogger = LogManager.getLogger();

    /**
     * Creates all necessary tables and columns for the database to be functional.
     * This is not a constructor, because the database should not be reinitialized on each instantiation.
     */
    protected void initialize() {
        try (
            Connection connection = DriverManager.getConnection("jdbc:sqlite:accounts.db");
            Statement statement = connection.createStatement()
        ) {
            databaseLogger.trace("initialize: Initializing database at accounts.db");
            statement.executeUpdate(
                "create table if not exists accounts (uid character(36) primary key, username varchar unique not null, hash varchar, balance int)");
        } catch (SQLException err) {
            databaseLogger.error("Database:initialize: Fatal error on initialization");
        }
    }

    /**
     * Creates a new account in the accounts table
     *
     * @param username The username of the new user
     * @param password The plaintext password
     * @return A boolean representing the success of the insert operation
     */
    protected boolean createAccount(String username, String password) {
        try (
            Connection connection = DriverManager.getConnection("jdbc:sqlite:accounts.db");
            PreparedStatement statement = connection.prepareStatement(
                "insert into accounts values (?, ?, ?, ?)")
        ) {
            databaseLogger.trace("createAccount: Creating account {}", username);
            final int balance = 500;
            final String uid = UUID.randomUUID().toString();
            final String hash = BCrypt.withDefaults().hashToString(12, password.toCharArray());
            statement.setString(1, uid);
            statement.setString(2, username);
            statement.setString(3, hash);
            statement.setInt(4, balance);
            return statement.executeUpdate() != 0;
        } catch (SQLException err) {
            databaseLogger.warn("Database:createAccount: Fatal error while creating account");
            databaseLogger.warn(err.getMessage());
            return false;
        }
    }

    /**
     * Used to log a user in.
     * Checks if username is present in accounts, then validates the hash against the password.
     *
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
                databaseLogger.trace("tryLogin: Record does not exist in database");
                return null;
            }
            BCrypt.Result success =
                BCrypt.verifyer().verify(password.toCharArray(), hash.getBytes());
            if (success.verified) {
                return new Account(username, balance);
            }
            databaseLogger.trace("tryLogin: Hash didn't match, password wrong");
            return null;
        } catch (SQLException err) {
            databaseLogger.warn("Database:tryLogin: error while logging in");
            databaseLogger.warn(err.getMessage());
            return null;
        }
    }

    /**
     * Deposits money into the users entry in the database
     *
     * @param username The username of the current user depositing money
     * @param amount   The amount of money to be deposited
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
            databaseLogger.trace("deposit: Depositing £" + amount + " into account: " + username);
            return statement.executeUpdate() != 0;
        } catch (SQLException err) {
            databaseLogger.warn("Database:deposit: Fatal error while depositing");
            databaseLogger.warn(err.getMessage());
            return false;
        }
    }

    /**
     * Withdraws money out of the users entry in the database
     *
     * @param username The username of the current user withdrawing money
     * @param amount   The amount of money to be withdrawn
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
            databaseLogger.trace("withdraw: Withdrawing £" + amount + " from account: " + username);
            return statement.executeUpdate() != 0;
        } catch (SQLException err) {
            databaseLogger.warn("Database:withdraw: Fatal error while withdrawing");
            databaseLogger.warn(err.getMessage());
            return false;
        }
    }

    /**
     * Gets the balance of the current user
     *
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
            databaseLogger.warn(err.getMessage());
            databaseLogger.warn("balance: Failed to get account balance");
            return -1;
        }
    }

    /**
     * Changes the user's password within the database
     *
     * @param username    The username of the currently logged-in user
     * @param password    The original password of the user
     * @param newPassword The new password of the user
     * @return A boolean representing the success of the operation
     */
    protected boolean changePassword(String username, String password, String newPassword) {
        try (
            Connection connection = DriverManager.getConnection("jdbc:sqlite:accounts.db");
            PreparedStatement getCurrentHash = connection.prepareStatement(
                "select hash from accounts where username=?");
            PreparedStatement statement = connection.prepareStatement(
                "update accounts set hash=? where username=?")
        ) {
            getCurrentHash.setString(1, username);
            ResultSet getCurrentHashResultSet = getCurrentHash.executeQuery();
            String currentHash = getCurrentHashResultSet.getString("hash");
            BCrypt.Result oldPasswordMatchesResult =
                BCrypt.verifyer().verify(password.toCharArray(), currentHash.toCharArray());
            boolean oldPasswordMatches = oldPasswordMatchesResult.verified;
            if (!oldPasswordMatches) {
                databaseLogger.info("changePassword: Incorrect password. Exiting.");
                return false;
            }
            String newHash = BCrypt.withDefaults().hashToString(12, newPassword.toCharArray());
            statement.setString(1, newHash);
            statement.setString(2, username);
            boolean success = statement.executeUpdate() != 0;
            if (success) {
                databaseLogger.info("changePassword: Password change succeeded.");
            } else {
                databaseLogger.info("changePassword: Password change did not succeed.");
            }
            return success;
        } catch (SQLException err) {
            databaseLogger.warn("changePassword: Could not update password in database");
            databaseLogger.warn(err.getMessage());
        }
        return true;
    }
}
