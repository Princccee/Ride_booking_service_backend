package com.ridebooking.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.stereotype.Service;

@Service
public class FCMService {

    /**
     * Sends a push notification to a single device using its FCM token.
     *
     * @param fcmToken The Firebase Cloud Messaging registration token of the target device.
     * @param title    The title of the notification.
     * @param body     The body/content of the notification.
     */
    public void sendNotification(String fcmToken, String title, String body) {
        Message message = Message.builder()
                .setToken(fcmToken)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .build();

        try {
            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("Successfully sent message: " + response);
        } catch (FirebaseMessagingException e) {
            System.err.println("Error sending FCM message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Send data message (not visible to user, used for background logic)
     */
    public void sendDataMessage(String fcmToken, String key, String value) {
        Message message = Message.builder()
                .setToken(fcmToken)
                .putData(key, value)
                .build();

        try {
            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("Successfully sent data message: " + response);
        } catch (FirebaseMessagingException e) {
            System.err.println("Error sending data message: " + e.getMessage());
        }
    }
}
