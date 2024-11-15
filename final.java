import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

class CurrencyConverter {
    private Map<String, Double> exchangeRates;

    public CurrencyConverter() {
        this.exchangeRates = new HashMap<>();
    }

    public void addExchangeRate(String currency, double rate) {
        exchangeRates.put(currency.toUpperCase(), rate);
    }

    public double convert(String fromCurrency, String toCurrency, double amount) throws Exception {
        fromCurrency = fromCurrency.toUpperCase();
        toCurrency = toCurrency.toUpperCase();

        if (!exchangeRates.containsKey(fromCurrency)) {
            throw new Exception("Currency not found: " + fromCurrency);
        }
        if (!exchangeRates.containsKey(toCurrency)) {
            throw new Exception("Currency not found: " + toCurrency);
        }

        double fromRate = exchangeRates.get(fromCurrency);
        double toRate = exchangeRates.get(toCurrency);
        return (amount / fromRate) * toRate;
    }

    public void printRates() {
        System.out.println("Available exchange rates:");
        for (Map.Entry<String, Double> entry : exchangeRates.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }

    public Map<String, Double> getExchangeRates() {
        return exchangeRates;
    }
}

class FileHandler {
    public static void loadExchangeRates(CurrencyConverter converter, String filename) throws FileNotFoundException {
        File file = new File(filename);
        Scanner scanner = new Scanner(file);

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] parts = line.split(":");

            if (parts.length == 2) {
                String currency = parts[0];
                double rate = Double.parseDouble(parts[1]);
                converter.addExchangeRate(currency, rate);
            }
        }
        scanner.close();
    }

    public static void saveExchangeRates(CurrencyConverter converter, String filename) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        for (Map.Entry<String, Double> entry : converter.getExchangeRates().entrySet()) {
            writer.write(entry.getKey() + ":" + entry.getValue());
            writer.newLine();
        }
        writer.close();
    }
}

class ConverterRunnable implements Runnable {
    private CurrencyConverter converter;
    private String filename;

    public ConverterRunnable(CurrencyConverter converter, String filename) {
        this.converter = converter;
        this.filename = filename;
    }

    @Override
    public void run() {
        try {
            FileHandler.loadExchangeRates(converter, filename);
            System.out.println("Exchange rates loaded successfully in the background.");
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + filename);
        }
    }
}

class Main {
    public static void main(String[] args) {
        CurrencyConverter converter = new CurrencyConverter();
        String filename = "rates.txt";

        // Start loading exchange rates in a separate thread
        Thread loadRatesThread = new Thread(new ConverterRunnable(converter, filename));
        loadRatesThread.start();

        try {
            loadRatesThread.join(); // Wait for the thread to finish loading
        } catch (InterruptedException e) {
            System.out.println("Loading interrupted.");
        }

        // User interface for currency conversion
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to the Currency Converter!");
        converter.printRates();

        while (true) {
            System.out.print("\nDo you want to convert, update a rate, or exit? (convert/update/exit): ");
            String choice = scanner.nextLine();

            if (choice.equalsIgnoreCase("exit")) {
                System.out.println("Exiting...");
                break;
            } else if (choice.equalsIgnoreCase("convert")) {
                System.out.print("Enter the currency code to convert from: ");
                String fromCurrency = scanner.nextLine();

                System.out.print("Enter the currency code to convert to: ");
                String toCurrency = scanner.nextLine();

                System.out.print("Enter the amount to convert: ");
                String amountStr = scanner.nextLine();

                try {
                    double amount = Double.parseDouble(amountStr);
                    double convertedAmount = converter.convert(fromCurrency, toCurrency, amount);
                    System.out.println("Converted amount: " + convertedAmount + " " + toCurrency);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid amount entered. Please enter a number.");
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
            } else if (choice.equalsIgnoreCase("update")) {
                System.out.print("Enter the currency code to update: ");
                String currency = scanner.nextLine().toUpperCase();

                System.out.print("Enter the new exchange rate: ");
                String rateStr = scanner.nextLine();

                try {
                    double rate = Double.parseDouble(rateStr);
                    converter.addExchangeRate(currency, rate);
                    FileHandler.saveExchangeRates(converter, filename);
                    System.out.println("Exchange rate for " + currency + " updated to " + rate + " and saved to file.");
                } catch (NumberFormatException e) {
                    System.out.println("Invalid rate entered. Please enter a numeric value.");
                } catch (IOException e) {
                    System.out.println("Error saving updated rates to file.");
                }
            } else {
                System.out.println("Invalid choice. Please type 'convert', 'update', or 'exit'.");
            }
        }
        scanner.close();
    }
}


