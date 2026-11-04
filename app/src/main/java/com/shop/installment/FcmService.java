package com.shop.installment;
import com.google.firebase.messaging.FirebaseMessagingService; import com.google.firebase.messaging.RemoteMessage;
// Instant wake-up: the server pings, the app immediately syncs and applies lock/unlock.
public class FcmService extends FirebaseMessagingService {
    @Override public void onNewToken(String t) { Policy.sp(this).edit().putString("fcm", t).apply(); Sync.run(this); }
    @Override public void onMessageReceived(RemoteMessage m) { Sync.run(this); RealtimeService.start(this); }
}
