package com.mp.javaPaymentSDK.adapters;

import com.mp.javaPaymentSDK.callbacks.NotificationListener;
import com.mp.javaPaymentSDK.callbacks.ResponseListener;
import com.mp.javaPaymentSDK.callbacks.RequestListener;
import com.mp.javaPaymentSDK.enums.Endpoints;
import com.mp.javaPaymentSDK.enums.Error;
import com.mp.javaPaymentSDK.enums.TransactionResult;
import com.mp.javaPaymentSDK.models.Credentials;
import com.mp.javaPaymentSDK.models.requests.hosted.HostedPaymentRecurrentInitial;
import com.mp.javaPaymentSDK.models.requests.hosted.HostedPaymentRedirection;
import com.mp.javaPaymentSDK.models.responses.notification.Notification;
import com.mp.javaPaymentSDK.utils.HexUtils;
import com.mp.javaPaymentSDK.utils.SecurityUtils;
import com.mp.javaPaymentSDK.utils.Utils;
import kotlin.Pair;
import okhttp3.FormBody;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class HostedPaymentAdapter {

    private Credentials credentials;

    private final NetworkAdapter networkAdapter = new NetworkAdapter();

    public HostedPaymentAdapter(Credentials credentials) {
        this.credentials = credentials;
    }

    public void sendHostedPaymentRequest(HostedPaymentRedirection hostedPaymentRedirection, ResponseListener responseListener) {

        Pair<Boolean, String> isMissingCred = hostedPaymentRedirection.checkCredentials(credentials);
        if (isMissingCred.getFirst()) {
            responseListener.onError(Error.MISSING_PARAMETER, isMissingCred.getSecond());
            return;
        }

        String endpoint = Endpoints.HOSTED_ENDPOINT.getEndpoint(credentials.getEnvironment());

        hostedPaymentRedirection.setCredentials(credentials);

        Pair<Boolean, String> isMissingField = hostedPaymentRedirection.isMissingField();
        if (isMissingField.getFirst()) {
            responseListener.onError(Error.MISSING_PARAMETER, isMissingField.getSecond());
            return;
        }

        String parsedAmount = Utils.getInstance().parseAmount(hostedPaymentRedirection.getAmount());
        if (parsedAmount == null) {
            responseListener.onError(Error.INVALID_AMOUNT, Error.INVALID_AMOUNT.getMessage());
            return;
        }
        hostedPaymentRedirection.setAmount(parsedAmount);

        String httpQuery = Utils.getInstance().buildQuery(HostedPaymentRedirection.class, hostedPaymentRedirection);
        String finalQueryParameter = Utils.getInstance().encodeUrl(httpQuery);
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
        headers.put("apiVersion", String.valueOf(hostedPaymentRedirection.getApiVersion()));
        headers.put("encryptionMode", "CBC");
        headers.put("iv", SecurityUtils.getInstance().base64Encode(clearIV));

        HashMap<String, String> queryParameters = new HashMap<>();
        queryParameters.put("merchantId", String.valueOf(hostedPaymentRedirection.getMerchantId()));
        queryParameters.put("encrypted", SecurityUtils.getInstance().base64Encode(encryptedRequest));
        queryParameters.put("integrityCheck", HexUtils.getInstance().bytesToHex(signature).toLowerCase());

        networkAdapter.sendRequest(headers, queryParameters, new FormBody.Builder().build(), endpoint, new RequestListener() {
            @Override
            public void onError(Error error, String errorMessage) {
                System.out.println("An error occurred in Hosted Payment - " + error.getMessage() + " - " + errorMessage);
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

    public void sendHostedRecurrentInitial(HostedPaymentRecurrentInitial hostedPaymentRecurrentInitial, ResponseListener responseListener) {

        Pair<Boolean, String> isMissingCred = hostedPaymentRecurrentInitial.checkCredentials(credentials);
        if (isMissingCred.getFirst()) {
            responseListener.onError(Error.MISSING_PARAMETER, isMissingCred.getSecond());
            return;
        }

        hostedPaymentRecurrentInitial.setCredentials(credentials);

        String endpoint = Endpoints.HOSTED_ENDPOINT.getEndpoint(credentials.getEnvironment());

        Pair<Boolean, String> isMissingField = hostedPaymentRecurrentInitial.isMissingField();
        if (isMissingField.getFirst()) {
            responseListener.onError(Error.MISSING_PARAMETER, isMissingField.getSecond());
            return;
        }

        String parsedAmount = Utils.getInstance().parseAmount(hostedPaymentRecurrentInitial.getAmount());
        if (parsedAmount == null) {
            responseListener.onError(Error.INVALID_AMOUNT, Error.INVALID_AMOUNT.getMessage());
            return;
        }
        hostedPaymentRecurrentInitial.setAmount(parsedAmount);

        String httpQuery = Utils.getInstance().buildQuery(HostedPaymentRecurrentInitial.class, hostedPaymentRecurrentInitial);
        String finalQueryParameter = Utils.getInstance().encodeUrl(httpQuery);
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
        headers.put("apiVersion", String.valueOf(hostedPaymentRecurrentInitial.getApiVersion()));
        headers.put("encryptionMode", "CBC");
        headers.put("iv", SecurityUtils.getInstance().base64Encode(clearIV));

        HashMap<String, String> queryParameters = new HashMap<>();
        queryParameters.put("merchantId", String.valueOf(hostedPaymentRecurrentInitial.getMerchantId()));
        queryParameters.put("encrypted", SecurityUtils.getInstance().base64Encode(encryptedRequest));
        queryParameters.put("integrityCheck", HexUtils.getInstance().bytesToHex(signature).toLowerCase());

        networkAdapter.sendRequest(headers, queryParameters, new FormBody.Builder().build(), endpoint, new RequestListener() {
            @Override
            public void onError(Error error, String errorMessage) {
                System.out.println("An error occurred in Hosted Payment - " + error.getMessage() + " - " + errorMessage);
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
}
