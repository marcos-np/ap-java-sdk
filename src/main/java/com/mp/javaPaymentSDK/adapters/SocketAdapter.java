package com.mp.javaPaymentSDK.adapters;

import com.mp.javaPaymentSDK.callbacks.NotificationListener;
import com.mp.javaPaymentSDK.enums.Error;
import com.corundumstudio.socketio.transport.WebSocketTransport;
import io.socket.client.IO;
import io.socket.client.Socket;

public class SocketAdapter {

    public static String NOTIFICATION_EVENT_NAME = "paymentNotification";
    public static String REGISTER_EVENT_NAME = "registerNotification";
    private Socket socket;

    public SocketAdapter() {
        String socketURL = System.getenv().getOrDefault("SOCKET_URL", "https://api-explorer-pocv1-notifbe-wss.epg-addonpayments-doc.xyz");

        IO.Options options = IO.Options.builder()
                .setTransports(new String[]{WebSocketTransport.NAME})
                .build();

        try {
            socket = IO.socket(socketURL, options);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void connect(String merchantTransactionId, NotificationListener notificationListener) {
        socket.connect();

        socket.emit(REGISTER_EVENT_NAME, merchantTransactionId);

        socket.on(NOTIFICATION_EVENT_NAME, args -> {
            if (args[0] != null) {
                notificationListener.onNotificationReceived(args[0].toString());
            }
            else {
                notificationListener.onError(Error.INVALID_RESPONSE_RECEIVED, Error.INVALID_RESPONSE_RECEIVED.getMessage());
            }
        });

    }


}
