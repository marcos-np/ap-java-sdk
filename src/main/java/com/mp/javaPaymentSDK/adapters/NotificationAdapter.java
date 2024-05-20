package com.mp.javaPaymentSDK.adapters;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mp.javaPaymentSDK.models.responses.notification.Notification;
import org.json.JSONObject;

public class NotificationAdapter {

    public static Notification parseNotification(String notificationString) throws Exception {
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        XmlMapper xmlMapper = new XmlMapper();
        System.out.println("Notification received \n" + notificationString);

        if (notificationString.startsWith("{")) {
            JSONObject jsonObject = new JSONObject(notificationString);
            if (jsonObject.has("response")) {
                return gson.fromJson(jsonObject.get("response").toString(), Notification.class);
            }
            else {
                return gson.fromJson(notificationString, Notification.class);
            }
        }
        else {
            return xmlMapper.readValue(notificationString, Notification.class);
        }
    }

}
