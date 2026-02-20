package com.livewave.ticket_api.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    public void sendPush(String token, String title, String body) throws Exception {

        Message message = Message.builder()
                .setToken(token)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .putData("type", "EVENT_REMINDER")
                .build();

        String response = FirebaseMessaging.getInstance().send(message);
        System.out.println("PUSH SENT: " + response);
    }
}