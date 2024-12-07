package Achraf.ace.services;

import Achraf.ace.stubs.Bank;
import Achraf.ace.stubs.BankServiceGrpc;
import io.grpc.stub.StreamObserver;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Math.random;

public class BankGrpcService extends BankServiceGrpc.BankServiceImplBase {

    // Static map to store currency exchange rates
    private static final Map<String, Double> currencies = new HashMap<>();

    static {
        // Initialize the currency rates
        currencies.put("USD", 1.0);  // Base currency
        currencies.put("EUR", 0.84); // Example: 1 USD = 0.84 EUR
        currencies.put("MAD", 10.0); // Example: 1 USD = 10 MAD
    }

    @Override
    public void convert(Bank.ConvertCurrencyRequest request, StreamObserver<Bank.ConvertCurrencyResponse> responseObserver) {
        // Extract data from the request
        String currencyFrom = request.getCurrencyFrom();
        String currencyTo = request.getCurrencyTo();
        double amount = request.getAmount();

        // Fetch the conversion rates for the specified currencies
        Double fromRate = currencies.get(currencyFrom);
        Double toRate = currencies.get(currencyTo);

        // Validate the rates
        if (fromRate == null || toRate == null) {
            responseObserver.onError(new IllegalArgumentException("Invalid currency code: " + currencyFrom + " or " + currencyTo));
            return;
        }

        // Perform the currency conversion
        double result = amount * fromRate / toRate;

        // Build the response object
        Bank.ConvertCurrencyResponse response = Bank.ConvertCurrencyResponse.newBuilder()
                .setCurrencyFrom(currencyFrom)
                .setCurrencyTo(currencyTo)
                .setAmount(amount)
                .setResult(result)
                .build();

        // Send the response to the client
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getStream(Bank.ConvertCurrencyRequest request, StreamObserver<Bank.ConvertCurrencyResponse> responseObserver) {
         String currencyFrom = request.getCurrencyFrom();
        String currencyTo = request.getCurrencyTo();
        double amount = request.getAmount();
        Timer timer = new Timer(); // On injecte un objet chaque seconde
        timer.schedule(new TimerTask() {
            int counter = 0;

            @Override
            public void run() {
                Bank.ConvertCurrencyResponse response = Bank.ConvertCurrencyResponse.newBuilder()
                        .setCurrencyFrom(currencyFrom)
                        .setCurrencyTo(currencyTo)
                        .setAmount(amount)
                        .setResult(amount * (random() * 100))
                        .build();
                responseObserver.onNext(response);
                ++counter;
                if (counter == 20) {
                    responseObserver.onCompleted();
                    timer.cancel();
                }
            }
        }, 0, 1000);
    }
    public StreamObserver<Bank.ConvertCurrencyRequest> performStream(StreamObserver<Bank.ConvertCurrencyResponse> responseObserver) {
        return new StreamObserver<Bank.ConvertCurrencyRequest>() {
            double sum = 0, result = 0;

            @Override
            public void onNext(Bank.ConvertCurrencyRequest convertCurrencyRequest) {
                String currencyFrom = convertCurrencyRequest.getCurrencyFrom();
                String currencyTo = convertCurrencyRequest.getCurrencyTo();
                double amount = convertCurrencyRequest.getAmount();
                double fromRate = currencies.get(currencyFrom);
                double toRate = currencies.get(currencyTo);
                sum += convertCurrencyRequest.getAmount();
                result = sum * fromRate / toRate;
            }

            @Override
            public void onError(Throwable throwable) {
            }

            @Override
            public void onCompleted() {
                Bank.ConvertCurrencyResponse response = Bank.ConvertCurrencyResponse.newBuilder()
                        .setResult(result)
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        };
    }
    @Override
    public StreamObserver<Bank.ConvertCurrencyRequest> fullCurrencyStream(StreamObserver<Bank.ConvertCurrencyResponse> responseObserver) {
        return new StreamObserver<Bank.ConvertCurrencyRequest>() {
            @Override
            public void onNext(Bank.ConvertCurrencyRequest convertCurrencyRequest) {
                Bank.ConvertCurrencyResponse response = Bank.ConvertCurrencyResponse.newBuilder()
                        .setResult(convertCurrencyRequest.getAmount() * random() + 80)
                        .build();
                responseObserver.onNext(response);
            }

            @Override
            public void onError(Throwable throwable) {
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }

}



