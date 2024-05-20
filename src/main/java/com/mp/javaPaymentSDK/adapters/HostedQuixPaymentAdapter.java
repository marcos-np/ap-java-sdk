package com.mp.javaPaymentSDK.adapters;

import com.google.gson.GsonBuilder;
import com.mp.javaPaymentSDK.callbacks.ResponseListener;
import com.mp.javaPaymentSDK.enums.Endpoints;
import com.mp.javaPaymentSDK.enums.Error;
import com.mp.javaPaymentSDK.models.quix_models.QuixHostedRequest;
import com.mp.javaPaymentSDK.utils.HexUtils;
import com.mp.javaPaymentSDK.utils.SecurityUtils;
import com.mp.javaPaymentSDK.utils.Utils;
import com.mp.javaPaymentSDK.callbacks.RequestListener;
import com.google.gson.Gson;
import com.mp.javaPaymentSDK.models.Credentials;
import com.mp.javaPaymentSDK.models.quix_models.QuixHostedQuery;
import com.mp.javaPaymentSDK.models.requests.quix_hosted.HostedQuixAccommodation;
import com.mp.javaPaymentSDK.models.requests.quix_hosted.HostedQuixFlight;
import com.mp.javaPaymentSDK.models.requests.quix_hosted.HostedQuixItem;
import com.mp.javaPaymentSDK.models.requests.quix_hosted.HostedQuixService;
import kotlin.Pair;
import okhttp3.FormBody;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class HostedQuixPaymentAdapter {

    private Credentials credentials;

    private final NetworkAdapter networkAdapter = new NetworkAdapter();

    private final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    public HostedQuixPaymentAdapter(Credentials credentials) {
        this.credentials = credentials;
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    public void sendHostedQuixServiceRequest(HostedQuixService hostedQuixService, ResponseListener responseListener) {
        Pair<Boolean, String> isMissingCred = hostedQuixService.checkCredentials(credentials);
        if (isMissingCred.getFirst()) {
            responseListener.onError(Error.MISSING_PARAMETER, isMissingCred.getSecond());
            return;
        }

        hostedQuixService.setCredentials(credentials);

        String endpoint = Endpoints.HOSTED_ENDPOINT.getEndpoint(credentials.getEnvironment());

        Pair<Boolean, String> missingField = hostedQuixService.isMissingFields();
        if (missingField.getFirst()) {
            responseListener.onError(Error.MISSING_PARAMETER, missingField.getSecond());
            return;
        }

        String parsedAmount = Utils.getInstance().parseAmount(hostedQuixService.getAmount());
        if (parsedAmount == null) {
            responseListener.onError(Error.INVALID_AMOUNT, Error.INVALID_AMOUNT.getMessage());
            return;
        }
        hostedQuixService.setAmount(parsedAmount);

        QuixHostedQuery quixHostedQuery = new QuixHostedQuery(hostedQuixService, credentials);

        quixHostedQuery.setPaysolExtendedData(gson.toJson(hostedQuixService.getPaySolExtendedData()));

        String httpQuery1 = Utils.getInstance().buildQuery(QuixHostedRequest.class, quixHostedQuery);
        String httpQuery2 = Utils.getInstance().buildQuery(QuixHostedQuery.class, quixHostedQuery);
        String finalQueryParameter1 = Utils.getInstance().encodeUrl(httpQuery1);
        String finalQueryParameter2 = Utils.getInstance().encodeUrl(httpQuery2);
        String finalQueryParameter = finalQueryParameter1 + "&" + finalQueryParameter2;
        byte[] formattedRequest = finalQueryParameter.getBytes(StandardCharsets.UTF_8);

        byte[] clearIV = SecurityUtils.getInstance().generateIV();

        byte[] encryptedRequest = SecurityUtils.getInstance().cbcEncryption(
                formattedRequest, // formatted request
                credentials.getMerchantPass().getBytes(),
                clearIV,
                true
        );

        byte[] signature = SecurityUtils.getInstance().hash256(formattedRequest);

        HashMap<String, String> headers = new HashMap<>();
        headers.put("apiVersion", String.valueOf(quixHostedQuery.getApiVersion()));
        headers.put("encryptionMode", "CBC");
        headers.put("iv", SecurityUtils.getInstance().base64Encode(clearIV));

        HashMap<String, String> queryParameters = new HashMap<>();
        queryParameters.put("merchantId", String.valueOf(hostedQuixService.getMerchantId()));
        queryParameters.put("encrypted", SecurityUtils.getInstance().base64Encode(encryptedRequest));
        queryParameters.put("integrityCheck", HexUtils.getInstance().bytesToHex(signature).toLowerCase());

        networkAdapter.sendRequest(headers, queryParameters, new FormBody.Builder().build(), endpoint, new RequestListener() {
            @Override
            public void onError(Error error, String errorMessage) {
                System.out.println("An error occurred in Quix Payment - " + error.getMessage() + " - " + errorMessage);
                responseListener.onError(error, errorMessage);
            }

            @Override
            public void onResponse(int code, ResponseBody responseBody) {
                if (code == 200 || code == 307) {
                    try {
                        String url = responseBody.string();
                        if (Utils.getInstance().isValidURL(url)) {
                            responseListener.onRedirectionURLReceived(url);
                        } else {
                            responseListener.onError(Error.INVALID_RESPONSE_RECEIVED, Error.INVALID_RESPONSE_RECEIVED.getMessage());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        responseListener.onError(Error.INVALID_RESPONSE_RECEIVED, Error.INVALID_RESPONSE_RECEIVED.getMessage());
                    }
                } else if (code >= 400 && code < 500) {
                    String errorMessage = Error.CLIENT_ERROR.getMessage();
                    try {
                        errorMessage = responseBody.string();
                    }
                    catch (IOException exception) {
                        exception.printStackTrace();
                    }
                    responseListener.onError(Error.CLIENT_ERROR, errorMessage);
                } else {
                    String errorMessage = Error.SERVER_ERROR.getMessage();
                    try {
                        errorMessage = responseBody.string();
                    }
                    catch (IOException exception) {
                        exception.printStackTrace();
                    }
                    responseListener.onError(Error.SERVER_ERROR, errorMessage);
                }
            }
        });
    }

    public void sendHostedQuixFlightRequest(HostedQuixFlight hostedQuixFlight, ResponseListener responseListener) {
        Pair<Boolean, String> isMissingCred = hostedQuixFlight.checkCredentials(credentials);
        if (isMissingCred.getFirst()) {
            responseListener.onError(Error.MISSING_PARAMETER, isMissingCred.getSecond());
            return;
        }

        hostedQuixFlight.setCredentials(credentials);

        String endpoint = Endpoints.HOSTED_ENDPOINT.getEndpoint(credentials.getEnvironment());

        Pair<Boolean, String> missingField = hostedQuixFlight.isMissingFields();
        if (missingField.getFirst()) {
            responseListener.onError(Error.MISSING_PARAMETER, missingField.getSecond());
            return;
        }

        String parsedAmount = Utils.getInstance().parseAmount(hostedQuixFlight.getAmount());
        if (parsedAmount == null) {
            responseListener.onError(Error.INVALID_AMOUNT, Error.INVALID_AMOUNT.getMessage());
            return;
        }
        hostedQuixFlight.setAmount(parsedAmount);

        QuixHostedQuery quixHostedQuery = new QuixHostedQuery(hostedQuixFlight, credentials);

        quixHostedQuery.setPaysolExtendedData(gson.toJson(hostedQuixFlight.getPaysolExtendedData()));

        String httpQuery1 = Utils.getInstance().buildQuery(QuixHostedRequest.class, quixHostedQuery);
        String httpQuery2 = Utils.getInstance().buildQuery(QuixHostedQuery.class, quixHostedQuery);
        String finalQueryParameter1 = Utils.getInstance().encodeUrl(httpQuery1);
        String finalQueryParameter2 = Utils.getInstance().encodeUrl(httpQuery2);
        String finalQueryParameter = finalQueryParameter1 + "&" + finalQueryParameter2;
        byte[] formattedRequest = finalQueryParameter.getBytes(StandardCharsets.UTF_8);

        byte[] clearIV = SecurityUtils.getInstance().generateIV();

        byte[] encryptedRequest = SecurityUtils.getInstance().cbcEncryption(
                formattedRequest, // formatted request
                credentials.getMerchantPass().getBytes(),
                clearIV,
                true
        );

        byte[] signature = SecurityUtils.getInstance().hash256(formattedRequest);

        HashMap<String, String> headers = new HashMap<>();
        headers.put("apiVersion", String.valueOf(quixHostedQuery.getApiVersion()));
        headers.put("encryptionMode", "CBC");
        headers.put("iv", SecurityUtils.getInstance().base64Encode(clearIV));

        HashMap<String, String> queryParameters = new HashMap<>();
        queryParameters.put("merchantId", String.valueOf(hostedQuixFlight.getMerchantId()));
        queryParameters.put("encrypted", SecurityUtils.getInstance().base64Encode(encryptedRequest));
        queryParameters.put("integrityCheck", HexUtils.getInstance().bytesToHex(signature).toLowerCase());

        networkAdapter.sendRequest(headers, queryParameters, new FormBody.Builder().build(), endpoint, new RequestListener() {
            @Override
            public void onError(Error error, String errorMessage) {
                System.out.println("An error occurred in Quix Payment - " + error.getMessage() + " - " + errorMessage);
                responseListener.onError(error, errorMessage);
            }

            @Override
            public void onResponse(int code, ResponseBody responseBody) {
                if (code == 200) {
                    try {
                        String url = responseBody.string();
                        if (Utils.getInstance().isValidURL(url)) {
                            responseListener.onRedirectionURLReceived(url);
                        } else {
                            responseListener.onError(Error.INVALID_RESPONSE_RECEIVED, Error.INVALID_RESPONSE_RECEIVED.getMessage());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        responseListener.onError(Error.INVALID_RESPONSE_RECEIVED, Error.INVALID_RESPONSE_RECEIVED.getMessage());
                    }
                } else if (code >= 400 && code < 500) {
                    String errorMessage = Error.CLIENT_ERROR.getMessage();
                    try {
                        errorMessage = responseBody.string();
                    }
                    catch (IOException exception) {
                        exception.printStackTrace();
                    }
                    responseListener.onError(Error.CLIENT_ERROR, errorMessage);
                } else {
                    String errorMessage = Error.SERVER_ERROR.getMessage();
                    try {
                        errorMessage = responseBody.string();
                    }
                    catch (IOException exception) {
                        exception.printStackTrace();
                    }
                    responseListener.onError(Error.SERVER_ERROR, errorMessage);
                }
            }
        });
    }

    public void sendHostedQuixAccommodationRequest(HostedQuixAccommodation hostedQuixAccommodation, ResponseListener responseListener) {
        Pair<Boolean, String> isMissingCred = hostedQuixAccommodation.checkCredentials(credentials);
        if (isMissingCred.getFirst()) {
            responseListener.onError(Error.MISSING_PARAMETER, isMissingCred.getSecond());
            return;
        }

        hostedQuixAccommodation.setCredentials(credentials);

        String endpoint = Endpoints.HOSTED_ENDPOINT.getEndpoint(credentials.getEnvironment());

        Pair<Boolean, String> missingField = hostedQuixAccommodation.isMissingFields();
        if (missingField.getFirst()) {
            responseListener.onError(Error.MISSING_PARAMETER, missingField.getSecond());
            return;
        }

        String parsedAmount = Utils.getInstance().parseAmount(hostedQuixAccommodation.getAmount());
        if (parsedAmount == null) {
            responseListener.onError(Error.INVALID_AMOUNT, Error.INVALID_AMOUNT.getMessage());
            return;
        }
        hostedQuixAccommodation.setAmount(parsedAmount);

        QuixHostedQuery quixHostedQuery = new QuixHostedQuery(hostedQuixAccommodation, credentials);

        quixHostedQuery.setPaysolExtendedData(gson.toJson(hostedQuixAccommodation.getPaySolExtendedData()));

        String httpQuery1 = Utils.getInstance().buildQuery(QuixHostedRequest.class, quixHostedQuery);
        String httpQuery2 = Utils.getInstance().buildQuery(QuixHostedQuery.class, quixHostedQuery);
        String finalQueryParameter1 = Utils.getInstance().encodeUrl(httpQuery1);
        String finalQueryParameter2 = Utils.getInstance().encodeUrl(httpQuery2);
        String finalQueryParameter = finalQueryParameter1 + "&" + finalQueryParameter2;
        System.out.println("finalQueryParameter = " + finalQueryParameter);
        byte[] formattedRequest = finalQueryParameter.getBytes(StandardCharsets.UTF_8);

        byte[] clearIV = SecurityUtils.getInstance().generateIV();

        byte[] encryptedRequest = SecurityUtils.getInstance().cbcEncryption(
                formattedRequest, // formatted request
                credentials.getMerchantPass().getBytes(),
                clearIV,
                true
        );

        byte[] signature = SecurityUtils.getInstance().hash256(formattedRequest);

        HashMap<String, String> headers = new HashMap<>();
        headers.put("apiVersion", String.valueOf(quixHostedQuery.getApiVersion()));
        headers.put("encryptionMode", "CBC");
        headers.put("iv", SecurityUtils.getInstance().base64Encode(clearIV));

        HashMap<String, String> queryParameters = new HashMap<>();
        queryParameters.put("merchantId", String.valueOf(hostedQuixAccommodation.getMerchantId()));
        queryParameters.put("encrypted", SecurityUtils.getInstance().base64Encode(encryptedRequest));
        queryParameters.put("integrityCheck", HexUtils.getInstance().bytesToHex(signature).toLowerCase());

        networkAdapter.sendRequest(headers, queryParameters, new FormBody.Builder().build(), endpoint, new RequestListener() {
            @Override
            public void onError(Error error, String errorMessage) {
                System.out.println("An error occurred in Quix Payment - " + error.getMessage() + " - " + errorMessage);
                responseListener.onError(error, errorMessage);
            }

            @Override
            public void onResponse(int code, ResponseBody responseBody) {
                if (code == 200) {
                    try {
                        String url = responseBody.string();
                        if (Utils.getInstance().isValidURL(url)) {
                            responseListener.onRedirectionURLReceived(url);
                        } else {
                            responseListener.onError(Error.INVALID_RESPONSE_RECEIVED, Error.INVALID_RESPONSE_RECEIVED.getMessage());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        responseListener.onError(Error.INVALID_RESPONSE_RECEIVED, Error.INVALID_RESPONSE_RECEIVED.getMessage());
                    }
                } else if (code >= 400 && code < 500) {
                    String errorMessage = Error.CLIENT_ERROR.getMessage();
                    try {
                        errorMessage = responseBody.string();
                    }
                    catch (IOException exception) {
                        exception.printStackTrace();
                    }
                    responseListener.onError(Error.CLIENT_ERROR, errorMessage);
                } else {
                    String errorMessage = Error.SERVER_ERROR.getMessage();
                    try {
                        errorMessage = responseBody.string();
                    }
                    catch (IOException exception) {
                        exception.printStackTrace();
                    }
                    responseListener.onError(Error.SERVER_ERROR, errorMessage);
                }
            }
        });
    }

    public void sendHostedQuixItemRequest(HostedQuixItem hostedQuixItem, ResponseListener responseListener) {
        Pair<Boolean, String> isMissingCred = hostedQuixItem.checkCredentials(credentials);
        if (isMissingCred.getFirst()) {
            responseListener.onError(Error.MISSING_PARAMETER, isMissingCred.getSecond());
            return;
        }

        hostedQuixItem.setCredentials(credentials);

        String endpoint = Endpoints.HOSTED_ENDPOINT.getEndpoint(credentials.getEnvironment());

        Pair<Boolean, String> missingField = hostedQuixItem.isMissingFields();
        if (missingField.getFirst()) {
            responseListener.onError(Error.MISSING_PARAMETER, missingField.getSecond());
            return;
        }

        String parsedAmount = Utils.getInstance().parseAmount(hostedQuixItem.getAmount());
        if (parsedAmount == null) {
            responseListener.onError(Error.INVALID_AMOUNT, Error.INVALID_AMOUNT.getMessage());
            return;
        }
        hostedQuixItem.setAmount(parsedAmount);

        QuixHostedQuery quixHostedQuery = new QuixHostedQuery(hostedQuixItem, credentials);

        quixHostedQuery.setPaysolExtendedData(gson.toJson(hostedQuixItem.getPaySolExtendedData()));

        String httpQuery1 = Utils.getInstance().buildQuery(QuixHostedRequest.class, quixHostedQuery);
        String httpQuery2 = Utils.getInstance().buildQuery(QuixHostedQuery.class, quixHostedQuery);
        String finalQueryParameter1 = Utils.getInstance().encodeUrl(httpQuery1);
        String finalQueryParameter2 = Utils.getInstance().encodeUrl(httpQuery2);
        String finalQueryParameter = finalQueryParameter1 + "&" + finalQueryParameter2;
        byte[] formattedRequest = finalQueryParameter.getBytes(StandardCharsets.UTF_8);

        byte[] clearIV = SecurityUtils.getInstance().generateIV();

        byte[] encryptedRequest = SecurityUtils.getInstance().cbcEncryption(
                formattedRequest, // formatted request
                credentials.getMerchantPass().getBytes(),
                clearIV,
                true
        );

        byte[] signature = SecurityUtils.getInstance().hash256(formattedRequest);

        HashMap<String, String> headers = new HashMap<>();
        headers.put("apiVersion", String.valueOf(quixHostedQuery.getApiVersion()));
        headers.put("encryptionMode", "CBC");
        headers.put("iv", SecurityUtils.getInstance().base64Encode(clearIV));

        HashMap<String, String> queryParameters = new HashMap<>();
        queryParameters.put("merchantId", String.valueOf(hostedQuixItem.getMerchantId()));
        queryParameters.put("encrypted", SecurityUtils.getInstance().base64Encode(encryptedRequest));
        queryParameters.put("integrityCheck", HexUtils.getInstance().bytesToHex(signature).toLowerCase());

        networkAdapter.sendRequest(headers, queryParameters, new FormBody.Builder().build(), endpoint, new RequestListener() {
            @Override
            public void onError(Error error, String errorMessage) {
                System.out.println("An error occurred in Quix Payment - " + error.getMessage() + " - " + errorMessage);
                responseListener.onError(error, errorMessage);
            }

            @Override
            public void onResponse(int code, ResponseBody responseBody) {
                if (code == 200 || code == 307) {
                    try {
                        String url = responseBody.string();
                        if (Utils.getInstance().isValidURL(url)) {
                            responseListener.onRedirectionURLReceived(url);
                        } else {
                            responseListener.onError(Error.INVALID_RESPONSE_RECEIVED, Error.INVALID_RESPONSE_RECEIVED.getMessage());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        responseListener.onError(Error.INVALID_RESPONSE_RECEIVED, Error.INVALID_RESPONSE_RECEIVED.getMessage());
                    }
                } else if (code >= 400 && code < 500) {
                    String errorMessage = Error.CLIENT_ERROR.getMessage();
                    try {
                        errorMessage = responseBody.string();
                    }
                    catch (IOException exception) {
                        exception.printStackTrace();
                    }
                    responseListener.onError(Error.CLIENT_ERROR, errorMessage);
                } else {
                    String errorMessage = Error.SERVER_ERROR.getMessage();
                    try {
                        errorMessage = responseBody.string();
                    }
                    catch (IOException exception) {
                        exception.printStackTrace();
                    }
                    responseListener.onError(Error.SERVER_ERROR, errorMessage);
                }
            }
        });
    }
}
