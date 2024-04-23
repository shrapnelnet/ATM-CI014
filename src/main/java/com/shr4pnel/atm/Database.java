package com.shr4pnel.atm;

import at.favre.lib.crypto.bcrypt.BCrypt;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

public class Database {

    protected void initialize() {
        try (
            Connection connection = DriverManager.getConnection("jdbc:sqlite:accounts.db");
            Statement statement = connection.createStatement()
        ) {
            Log.trace("Database::initialize: Initializing database at accounts.db");
            statement.executeUpdate(
                "create table if not exists accounts (uid character(36) primary key, username varchar unique not null, hash varchar, balance int)");
        } catch (SQLException err) {
            Log.error("Database:initialize: Fatal error on initialization");
        }
    }

    protected boolean createAccount(String username, String password) {
        try (
            Connection connection = DriverManager.getConnection("jdbc:sqlite:accounts.db");
            PreparedStatement statement = connection.prepareStatement(
                "insert into accounts values (?, ?, ?, ?)")
        ) {
            Log.trace("Database::createAccount: Creating account " + username);
            final int balance = 500;
            final String uid = UUID.randomUUID().toString();
            final String hash = BCrypt.withDefaults().hashToString(12, password.toCharArray());
            statement.setString(1, uid);
            statement.setString(2, username);
            statement.setString(3, hash);
            statement.setInt(4, balance);
            // 1 if insert succeeds
            return statement.executeUpdate() != 0;
        } catch (SQLException err) {
            Log.warn("Database:createAccount: Fatal error while creating account");
            Log.warn(err.getMessage());
            return false;
        }
    }

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
