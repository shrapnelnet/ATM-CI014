package com.shr4pnel.atm;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import at.favre.lib.crypto.bcrypt.BCrypt;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class DatabaseTests {
    private final Logger logger = LogManager.getLogger();
    private static Connection connection;
    private final String username = "Tyler";

    @BeforeEach
    @DisplayName("Recreate database before each test")
    void setup() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        try (
            Statement statement = connection.createStatement()
        ) {
            statement.executeUpdate("drop table if exists accounts");
            statement.executeUpdate(
                "create table accounts (uid character(36) primary key, username varchar unique not null, hash varchar, balance int)");
        } catch (SQLException err) {
            logger.error("setup: Tests failed");
            fail(err.getMessage());
        }
    }

    @AfterAll
    static void teardown() throws SQLException {
        connection.close();
    }

    @Test
    @DisplayName("Verify successful initialization of SQLite DB")
    void initialize() {
        try (
            Statement statement = connection.createStatement()
        ) {
            DatabaseMetaData dbmd = connection.getMetaData();
            ResultSet dmbdResultSet = dbmd.getTables(null, null, "accounts", null);
            logger.info("initialize: Checking for existence of database");
            assertTrue(dmbdResultSet.next());
            ResultSet rs = statement.executeQuery("select * from accounts");
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();
            int i;
            String[] expectedColumns = {"", "uid", "username", "hash", "balance"};
            logger.info("initialize: Checking column names are correct");
            for (i = 1; i < columnCount; ++i) {
                assertEquals(rsmd.getColumnLabel(i), expectedColumns[i],
                    rsmd.getColumnLabel(i) + " should be " + expectedColumns[i]);
            }
            logger.info("initialize: Tests passed");
        } catch (SQLException err) {
            logger.error("DatabaseTests:initialize: Fatal error on initialization");
            fail("Initialization failed: " + err.getMessage());
        }
    }

    @Test
    @DisplayName("Verify account creation success")
    void createAccount() {
        try (
            PreparedStatement statement = connection.prepareStatement(
                "insert into accounts values (?, ?, ?, ?)")
        ) {
            final String password = "password";
            final int balance = 500;
            final String uid = randomUUID().toString();
            final String hash = BCrypt.withDefaults().hashToString(12, password.toCharArray());
            statement.setString(1, uid);
            statement.setString(2, username);
            statement.setString(3, hash);
            statement.setInt(4, balance);
            logger.info("createAccount: Checking for successful account creation");
            assertNotEquals(statement.executeUpdate(), 0, "Account creation did not succeed");
            logger.info("createAccount: Tests passed");
        } catch (SQLException err) {
            logger.error("createAccount: Fatal error while creating account");
            fail(err.getMessage());
        }
    }

    @Test
    @DisplayName("Verify successful login")
    void tryLogin() {
        try (
            PreparedStatement statement = connection.prepareStatement(
                "select hash, balance, username from accounts where username=?");
        ) {
            Account expectedAccount = new Account("Tyler", 500);
            makeDummyAccount();
            statement.setString(1, "Tyler");
            ResultSet results = statement.executeQuery();
            boolean success = results.next();
            logger.info("tryLogin: Checking for existence of test account in database");
            assertTrue(success, "tryLogin: Could not select account with valid username");
            String hash = results.getString("hash");
            String username = results.getString("username");
            int balance = results.getInt("balance");
            logger.info("tryLogin: Checking that hash is not null");
            assertNotNull(hash, "Hash did not match");
            BCrypt.Result hashSuccess =
                BCrypt.verifyer().verify("password".toCharArray(), hash.getBytes());
            logger.info("tryLogin: Checking for password hash verification");
            assertTrue(hashSuccess.verified, "tryLogin: Valid password did not result in successful hash validation");
            Account actualAccount = new Account(username, balance);
            logger.info("tryLogin: Checking account object is as expected");
            assertEquals(expectedAccount.username, actualAccount.username, "tryLogin: Expected account username did not match actual");
            assertEquals(expectedAccount.balance, actualAccount.balance, "tryLogin: Expected account balance did not match actual");
            logger.info("tryLogin: tests passed");
        } catch (SQLException err) {
            logger.warn("Database:tryLogin: error while logging in");
            fail(err.getMessage());
        }
    }

    @Test
    @DisplayName("Verify that depositing money into account works")
    void deposit() {
        try (
            PreparedStatement statement = connection.prepareStatement(
                "update accounts set balance=balance+? where username=?");
            PreparedStatement getNewValues = connection.prepareStatement("select balance from accounts where username=?")
        ) {
            makeDummyAccount();
            int amount = 50;
            
            statement.setInt(1, amount);
            statement.setString(2, username);
            logger.info("deposit: Testing for initial deposit success");
            assertNotEquals(0, statement.executeUpdate(), "Account was not deposited into successfully");
            logger.info("deposit: Testing to ensure value in database is correct");
            getNewValues.setString(1, "Tyler");
            ResultSet rs = getNewValues.executeQuery();
            assertEquals(550, rs.getInt("balance"), "Balance in database is incorrect");
            logger.info("deposit: Tests passed");
        } catch (SQLException err) {
            logger.error("deposit: Deposit failed");
            fail(err.getMessage());
        }
    }

    @Test
    @DisplayName("Test successful withdrawal")
    void withdraw() {
        try (
            PreparedStatement getNewAmount = connection.prepareStatement("select balance from accounts where username=?");
            PreparedStatement statement = connection.prepareStatement(
                "update accounts set balance=balance-? where username=?")
        ) {
            int amount = 50;
            
            makeDummyAccount();
            statement.setInt(1, amount);
            statement.setString(2, username);
            logger.info("withdraw: Testing for successful db withdrawal");
            assertNotEquals(0, statement.executeUpdate(), "Withdrawal from database was unsuccessful");
            getNewAmount.setString(1, "Tyler");
            ResultSet rs = getNewAmount.executeQuery();
            logger.info("withdraw: Testing for correct value in db after withdrawal");
            assertEquals(450, rs.getInt("balance"));
            logger.info("withdraw: Tests passed");
        } catch (SQLException err) {
            logger.error("Database:withdraw: Fatal error while withdrawing");
            fail(err.getMessage());
        }
    }

    @Test
    @DisplayName("Verify balance being fetched is correct")
    void balance() {
        try (
            PreparedStatement statement = connection.prepareStatement(
                "select balance from accounts where username=?")
        ) {
            makeDummyAccount();
            statement.setString(1, username);
            ResultSet rs = statement.executeQuery();
            logger.info("balance: Checking balance returned from database is correct");
            assertEquals(500, rs.getInt("balance"));
        } catch (SQLException err) {
            logger.error("balance: Error occurred while fetching balance from db");
            fail(err.getMessage());
        }
    }



    void makeDummyAccount() {
        try (
            PreparedStatement setupStatement = connection.prepareStatement("insert into accounts values (?, ?, ?, ?)");
            ) {
            setupStatement.setString(1, randomUUID().toString());
            setupStatement.setString(2, "Tyler");
            setupStatement.setString(3, BCrypt.withDefaults().hashToString(12,
                "password".toCharArray()));
            setupStatement.setInt(4, 500);
            setupStatement.executeUpdate();
        } catch (SQLException err) {
            logger.error("makeDummyAccount: Failed to generate dummy account");
            fail(err.getMessage());
        }
    }

}