package com.atm.service;

import com.atm.dao.*;
import com.atm.model.*;

import java.sql.SQLException;
import java.util.List;

/**
 * Service layer for administrative operations.
 * Requires separate admin authentication — ATM users cannot access these functions.
 */
public class AdminService {

    private final CustomerDAO customerDAO = new CustomerDAO();
    private final AccountDAO accountDAO = new AccountDAO();
    private final ATMCardDAO cardDAO = new ATMCardDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final TransferDAO transferDAO = new TransferDAO();

    /** View all customers */
    public List<Customer> getAllCustomers() throws SQLException {
        return customerDAO.findAll();
    }

    /** View all accounts with customer info */
    public List<Account> getAllAccounts() throws SQLException {
        return accountDAO.findAll();
    }

    /** View all ATM cards */
    public List<ATMCard> getAllCards() throws SQLException {
        return cardDAO.findAll();
    }

    /** View recent transactions */
    public List<Transaction> getRecentTransactions(int limit) throws SQLException {
        return transactionDAO.findAll(limit);
    }

    /** View recent transfers */
    public List<Transfer> getRecentTransfers(int limit) throws SQLException {
        return transferDAO.findAll(limit);
    }

    /** Block a card (admin action) */
    public void blockCard(int cardId) throws SQLException {
        cardDAO.blockCard(cardId, null);
    }

    /** Activate/unblock a card (admin action) */
    public void activateCard(int cardId) throws SQLException {
        cardDAO.activateCard(cardId);
    }

    /** Get customer by ID */
    public Customer getCustomerById(int customerId) throws SQLException {
        return customerDAO.findById(customerId);
    }

    /** Get account by ID */
    public Account getAccountById(int accountId) throws SQLException {
        return accountDAO.findById(accountId);
    }

    /** Get accounts for a customer */
    public List<Account> getAccountsByCustomerId(int customerId) throws SQLException {
        return accountDAO.findByCustomerId(customerId);
    }
}
