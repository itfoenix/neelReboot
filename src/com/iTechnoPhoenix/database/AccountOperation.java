/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iTechnoPhoenix.database;

import com.iTechnoPhoenix.model.Account;
import com.iTechnoPhoenix.model.AccountReceipt;
import com.iTechnoPhoenix.model.Customer;
import com.iTechnoPhoenix.model.Reason;
import com.iTechnoPhoenix.neelSupport.PhoenixSupport;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.StackPane;

/**
 *
 * @author choudhary
 */
public class AccountOperation {

    private PreparedStatement stmt;

    public int insertBill(Reason reason, StackPane window) {
        int i = 0;
        try {
            stmt = Connector.getConnection().prepareStatement("insert into accountbill (customer_id,amount,account_date,status,creation) values (?,?,now(),0,now())", Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, reason.getAccount().getCustomer().getCust_num());
            stmt.setDouble(2, reason.getAccount().getTotalAmt());
            i = stmt.executeUpdate();
            if (i > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    i = rs.getInt(1);
                }
                Connector.commit();
                stmt = Connector.getConnection().prepareStatement("insert into neel_logger(tname,operation,oDate) values ('accountbill', 'Insertion-Account Bill', now())");
                stmt.executeUpdate();
                Connector.commit();

//                PhoenixSupport.Info("बिलाची माहिती जतन झाली आहे.", "खर्च बिल बनवणे", window);
            } else {
                Connector.rollbackresult();
                PhoenixSupport.Error("बिलाची माहिती जतन नाही झाली आहे.", window);
            }
        } catch (SQLException ex) {
            Logger.getLogger(AccountOperation.class.getName()).log(Level.SEVERE, null, ex);
        }
        return i;
    }

    public void insertReason(Reason reason, StackPane window) {
        try {
            stmt = Connector.getConnection().prepareStatement("insert into reason (reason, amount, account_id) values (?,?,?)");
            stmt.setString(1, reason.getReason());
            stmt.setDouble(2, reason.getAmount());
            stmt.setInt(3, reason.getAccount().getAccount_id());
            int j = stmt.executeUpdate();
            if (j > 0) {
                Connector.commit();
//                PhoenixSupport.Info("बिलाची माहिती जतन झाली आहे.", "खर्च बिल बनवणे", window);
                System.out.println("Account Saved");
            } else {
                Connector.rollbackresult();
//                PhoenixSupport.Error("बिलाची माहिती जतन नाही झाली आहे.", window);
                System.out.println("Not Saved Account");
            }
        } catch (SQLException ex) {
            Logger.getLogger(AccountOperation.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public ObservableList<Reason> getAllAccount() {
        ObservableList<Reason> accountList = FXCollections.observableArrayList();
        try {
            stmt = Connector.getConnection().prepareStatement("select a.*, c.name from accountbill a join customer c on c.cust_num = a.customer_id where status != 1");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Reason reason = new Reason();
                Account account = new Account();
                account.setAccount_id(rs.getInt(1));
                Customer customer = new Customer();
                customer.setCust_num(rs.getInt(2));
                customer.setName(rs.getString(7));
                account.setCustomer(customer);
                reason.setAmount(rs.getDouble(3));
                account.setDate(rs.getDate(4).toString());
                reason.setReason(rs.getString(5));
                reason.setAccount(account);
                accountList.add(reason);

            }
        } catch (SQLException ex) {
            Logger.getLogger(AccountOperation.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return accountList;
    }

    public void deleteAccount(int id, StackPane window) {
        try {
            stmt = Connector.getConnection().prepareStatement("update accountbill set status = 1 where account_id = ?");
            stmt.setInt(1, id);
            int i = stmt.executeUpdate();
            if (i > 0) {
                Connector.commit();
                stmt = Connector.getConnection().prepareStatement("insert into neel_logger(tname,operation,oDate) values ('accountbill', 'Deletion-Account Bill', now())");
                stmt.executeUpdate();
                Connector.commit();
                System.out.println("deleted");
                PhoenixSupport.Info("बिल रद्ध करण्यात येत आहे.", "बिल राध करणे", window);
            } else {
                Connector.rollbackresult();
                PhoenixSupport.Error("बिल रद्ध नाही करण्यात आले आहे", window);

            }
        } catch (SQLException ex) {
            Logger.getLogger(AccountOperation.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void updateAccount(Reason reason, StackPane window) {
        try {
            stmt = Connector.getConnection().prepareStatement("update accountbill set amount = ?, reason = ? where account_id = ?");
            stmt.setDouble(1, reason.getAmount());
            stmt.setString(2, reason.getReason());
            stmt.setInt(3, reason.getAccount().getAccount_id());
            int i = stmt.executeUpdate();
            if (i > 0) {
                Connector.commit();
                PhoenixSupport.Info("बिलात बदली करण्यात येत आहे.", "बिल बदली करणे", window);
            } else {
                Connector.rollbackresult();
                PhoenixSupport.Error("बिलात बदली नाही करण्यात आले आहे", window);

            }
        } catch (SQLException ex) {
            Logger.getLogger(AccountOperation.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    public ObservableList<Reason> getAccountFromBillNumber(int number) {
        ObservableList<Reason> reasonList = FXCollections.observableArrayList();
        try {
            stmt = Connector.getConnection().prepareStatement("SELECT a.*, r.reason, r.amount, c.name, c.address FROM accountbill a join reason r ON r.account_id = a.account_id join customer c ON c.cust_num = a.customer_id WHERE a.account_id = ?");
            stmt.setInt(1, number);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Reason reason = new Reason();
                reason.setAccount_id(rs.getInt(1));
                reason.setName(rs.getString(9));
                reason.setReason(rs.getString(7));
                reason.setAmount(rs.getDouble(8));
                Account account = new Account();
                account.setDate(rs.getDate(4).toString());
                account.setTotalAmt(rs.getDouble(3));
                account.setAccount_id(rs.getInt(1));
                account.setStatus(rs.getInt(5));
                Customer customer = new Customer();
                customer.setCust_num(rs.getInt(2));
                customer.setName(rs.getString(9));
                customer.setAddress(rs.getString(10));
                account.setName(rs.getString(9));
                account.setCustomer(customer);
                reason.setAccount(account);
                reason.setDate(rs.getDate(4).toString());
                reasonList.add(reason);
            }
        } catch (SQLException ex) {
            Logger.getLogger(AccountOperation.class.getName()).log(Level.SEVERE, null, ex);
        }
        return reasonList;
    }

    public int insertReceiptDetails(AccountReceipt areceipt, StackPane window) {
        int i = 0;
        try {
            if (areceipt.getPaymode() == 2) {
                stmt = Connector.getConnection().prepareStatement("INSERT INTO accountreceipt(bill_id, delay, paid_amt, date, pay_mode, cheque_no, bank_id, cheq_status, cheqcancel_time) VALUES (?,?,?,now(),?,?,?,0,now())", Statement.RETURN_GENERATED_KEYS);
                stmt.setString(5, areceipt.getCheque_no());
                stmt.setInt(6, areceipt.getBank_id());
            }
            if (areceipt.getPaymode() == 1) {
                stmt = Connector.getConnection().prepareStatement("INSERT INTO accountreceipt(bill_id, delay, paid_amt, date, pay_mode) VALUES (?,?,?,now(),?)", Statement.RETURN_GENERATED_KEYS);
            }
            stmt.setDouble(2, areceipt.getDelay_amt());
            stmt.setInt(1, areceipt.getAccount().getAccount_id());
            stmt.setDouble(3, areceipt.getPaid_amt());
            stmt.setInt(4, areceipt.getPaymode());
            i = stmt.executeUpdate();
            if (i > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                while (rs.next()) {
                    i = rs.getInt(1);
                }
                Connector.commit();
                stmt = Connector.getConnection().prepareStatement("update");
                if (areceipt.getPaymode() == 1) {
                    stmt = Connector.getConnection().prepareStatement("insert into neel_logger(tname,operation,oDate) values ('accountreceipt', 'Insertion-Account Receipt Cash', now())");
                }
                if (areceipt.getPaymode() == 2) {
                    stmt = Connector.getConnection().prepareStatement("insert into neel_logger(tname,operation,oDate) values ('accountreceipt', 'Insertion-Account Receipt Cheque', now())");
                }
                stmt.executeUpdate();
                Connector.commit();
            } else {
                Connector.rollbackresult();
                PhoenixSupport.Error("पावती जतन नाही झाली आहे.", window);
            }
        } catch (SQLException ex) {
            Logger.getLogger(AccountOperation.class.getName()).log(Level.SEVERE, null, ex);
        }
        return i;
    }

    public boolean isExist(int i) {
        boolean result = false;
        try {
            stmt = Connector.getConnection().prepareStatement("SELECT * FROM accountreceipt WHERE bill_id = ? and status = 0");
            stmt.setInt(1, i);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                result = true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(AccountOperation.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public ObservableList<Account> searchBills(int i, String name, String sdate, String edate) {
        ObservableList<Account> accountList = FXCollections.observableArrayList();
        try {
            if (i == 1) {
                stmt = Connector.getConnection().prepareStatement("SELECT a.*, c.name, c.address FROM accountbill a join customer c on c.cust_num = a.customer_id WHERE a.account_id = ? OR c.name = ? AND a.account_date BETWEEN ? AND ? and a.status = 0");
                if (Pattern.matches("\\d", name)) {
                    stmt.setInt(1, Integer.parseInt(name));
                    stmt.setString(2, null);
                } else {
                    stmt.setInt(1, 0);
                    stmt.setString(2, name);
                }
                stmt.setString(3, sdate);
                stmt.setString(4, edate);
            } else if (i == 2) {
                stmt = Connector.getConnection().prepareStatement("SELECT a.*, c.name, c.address FROM accountbill a join customer c on c.cust_num = a.customer_id WHERE a.account_date BETWEEN ? AND ? and a.status = 0");
                stmt.setString(1, sdate);
                stmt.setString(2, edate);
            } else if (i == 3) {
                stmt = Connector.getConnection().prepareStatement("SELECT a.*, c.name, c.address FROM accountbill a join customer c on c.cust_num = a.customer_id WHERE a.account_id = ? OR c.name = ? and status = 0");
                if (Pattern.matches("\\d", name)) {
                    stmt.setInt(1, Integer.parseInt(name));
                    stmt.setString(2, null);
                } else {
                    stmt.setInt(1, 0);
                    stmt.setString(2, name);
                }
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Account account = new Account();
                account.setAccount_id(rs.getInt(1));
                Customer customer = new Customer();
                customer.setCust_num(rs.getInt(2));
                customer.setName(rs.getString(7));
                customer.setAddress(rs.getString(8));
                account.setCustomer(customer);
                account.setName(rs.getString(7));
                account.setTotalAmt(rs.getDouble(3));
                account.setDate(rs.getString(4));
                account.setStatus(rs.getInt(5));
                accountList.add(account);
            }
        } catch (SQLException ex) {
            Logger.getLogger(AccountOperation.class.getName()).log(Level.SEVERE, null, ex);
        }
        return accountList;
    }

}
