package com.mybank.tui;

import jexer.TAction;
import jexer.TApplication;
import jexer.TField;
import jexer.TText;
import jexer.TWindow;
import jexer.event.TMenuEvent;
import jexer.menu.TMenu;

import java.util.*;
import java.io.*;

public class TUIdemo extends TApplication {

    private static final int ABOUT_APP = 2000;
    private static final int CUST_INFO = 2010;

    static class Account {
        protected double balance;

        public Account(double balance) {
            this.balance = balance;
        }

        public double getBalance() {
            return balance;
        }

        public String getAccountType() {
            return "Generic";
        }
    }

    static class CheckingAccount extends Account {
        public CheckingAccount(double balance) {
            super(balance);
        }

        @Override
        public String getAccountType() {
            return "Checking";
        }
    }

    static class SavingsAccount extends Account {
        public SavingsAccount(double balance) {
            super(balance);
        }

        @Override
        public String getAccountType() {
            return "Savings";
        }
    }

    static class Customer {
        private String firstName;
        private String lastName;
        private List<Account> accounts = new ArrayList<>();
        private int id;

        public Customer(int id, String firstName, String lastName) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public void addAccount(Account acc) {
            accounts.add(acc);
        }

        public Account getAccount(int index) {
            return accounts.get(index);
        }

        public int getNumberOfAccounts() {
            return accounts.size();
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public int getId() {
            return id;
        }
    }

    static class Bank {
        private static List<Customer> customers = new ArrayList<>();
        private static int idCounter = 0;

        public static void addCustomer(Customer customer) {
            customers.add(customer);
            if (idCounter <= customer.getId()) {
                idCounter = customer.getId() + 1;
            }
        }

        public static Customer getCustomer(int index) {
            if (index < 0 || index >= customers.size()) return null;
            return customers.get(index);
        }

        public static int getNumOfCustomers() {
            return customers.size();
        }
    }

    public static void main(String[] args) throws Exception {
        new TUIdemo().run();
    }

    public TUIdemo() throws Exception {
        super(BackendType.SWING);

        addToolMenu();

        TMenu fileMenu = addMenu("&File");

        fileMenu.addItem(CUST_INFO, "&Customer Info");

        fileMenu.addDefaultItem(TMenu.MID_SHELL);
        fileMenu.addSeparator();
        fileMenu.addDefaultItem(TMenu.MID_EXIT);

        addWindowMenu();

        TMenu helpMenu = addMenu("&Help");

        helpMenu.addItem(ABOUT_APP, "&About...");

        setFocusFollowsMouse(true);

        // Завантажимо інформацію про клієнтів із ресурсу
        loadBankDataFromResource("data/test.dat");

        showCustomerDetails();
    }

    private void loadBankDataFromResource(String resourcePath) {
    try (InputStream input = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
        if (input == null) {
            System.err.println("Файл не знайдено: " + resourcePath);
            return;
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(input));
        String line;

        // Считываем количество клиентов
        while ((line = br.readLine()) != null && line.trim().isEmpty()) {
            // пропускаем пустые строки в начале
        }

        if (line == null) return;

        int numCustomers = Integer.parseInt(line.trim());

        for (int i = 0; i < numCustomers; i++) {
            // читаем строку с именем, фамилией и количеством счетов
            do {
                line = br.readLine();
            } while (line != null && line.trim().isEmpty());

            if (line == null) break;

            String[] parts = line.trim().split("\\s+");
            if (parts.length < 3) {
                System.err.println("Невірна строка з клієнтом: " + line);
                continue;
            }

            String firstName = parts[0];
            String lastName = parts[1];
            int numAccounts = Integer.parseInt(parts[2]);

            Customer customer = new Customer(i, firstName, lastName);

            for (int j = 0; j < numAccounts; j++) {
                do {
                    line = br.readLine();
                } while (line != null && line.trim().isEmpty());

                if (line == null) break;

                String[] accParts = line.trim().split("\\s+");
                if (accParts.length < 2) {
                    System.err.println("Невірна строка з рахунком: " + line);
                    continue;
                }

                String type = accParts[0];
                double balance = Double.parseDouble(accParts[1]);

                if ("S".equalsIgnoreCase(type)) {
                    customer.addAccount(new SavingsAccount(balance));
                } else if ("C".equalsIgnoreCase(type)) {
                    customer.addAccount(new CheckingAccount(balance));
                } else {
                    System.err.println("Невідомий тип рахунку: " + type);
                }
            }

            Bank.addCustomer(customer);
        }

    } catch (Exception e) {
        e.printStackTrace();
        System.err.println("Помилка при читанні test.dat: " + e.getMessage());
    }
}



    @Override
    protected boolean onMenu(TMenuEvent menu) {
        if (menu.getId() == ABOUT_APP) {
            messageBox("About", "Just a simple Jexer demo.\n(c) 2025").show();
            return true;
        }
        if (menu.getId() == CUST_INFO) {
            showCustomerDetails();
            return true;
        }
        return super.onMenu(menu);
    }

    private void showCustomerDetails() {
        TWindow custWin = addWindow("Customer Window", 2, 1, 40, 10, TWindow.NOZOOMBOX);
        custWin.newStatusBar("Enter customer number and press Show.");

        custWin.addLabel("Enter customer number: ", 2, 2);
        TField custNo = custWin.addField(26, 2, 3, false);
        TText details = custWin.addText("Owner Name: \nAccount Type: \nAccount Balance: ", 2, 4, 38, 8);

        custWin.addButton("&Show", 30, 2, new TAction() {
            @Override
            public void DO() {
                try {
                    int custNum = Integer.parseInt(custNo.getText().trim());
                    Customer customer = Bank.getCustomer(custNum);
                    if (customer == null)
                        throw new IndexOutOfBoundsException();

                    StringBuilder sb = new StringBuilder();

                    sb.append("Owner Name: ").append(customer.getFirstName()).append(" ").append(customer.getLastName())
                            .append(" (id=").append(customer.getId()).append(")\n");

                    if (customer.getNumberOfAccounts() > 0) {
                        Account acc = customer.getAccount(0);
                        sb.append("Account Type: ").append(acc.getAccountType()).append("\n");
                        sb.append("Account Balance: $").append(String.format("%.2f", acc.getBalance()));
                    } else {
                        sb.append("No accounts found.");
                    }

                    details.setText(sb.toString());

                } catch (Exception e) {
                    messageBox("Error", "Invalid customer number!").show();
                }
            }
        });
    }
}
