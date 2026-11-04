package com.shop.installment;
import android.app.*; import android.content.*; import android.content.pm.ServiceInfo; import android.os.*; import org.json.JSONObject;
// Keeps a live connection to the server so lock/unlock commands arrive within seconds.
public class RealtimeService extends Service {
    static volatile boolean running; Thread t;
    static void start(Context c) {
        try { if (Policy.sp(c).getBoolean("released", false)) return; c.startForegroundService(new Intent(c, RealtimeService.class)); } catch (Exception ignored) {}
    }
    @Override public IBinder onBind(Intent i) { return null; }
    @Override public int onStartCommand(Intent i, int f, int id) {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.createNotificationChannel(new NotificationChannel("live", "Server connection", NotificationManager.IMPORTANCE_MIN));
        Notification n = new Notification.Builder(this, "live").setSmallIcon(android.R.drawable.stat_notify_sync).setContentTitle("Connected to shop server").setOngoing(true).build();
        if (Build.VERSION.SDK_INT >= 34) startForeground(1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE); else startForeground(1, n);
        if (t == null || !t.isAlive()) { running = true; t = new Thread(this::loop); t.start(); }
        return START_STICKY;
    }
    void loop() {
        SharedPreferences sp = Policy.sp(this);
        while (running) {
            try {
                if (sp.getBoolean("released", false)) { stopSelf(); return; }
                if (!sp.contains("token")) { Sync.run(this); Thread.sleep(15000); continue; }
                JSONObject o = Api.post("wait", new JSONObject().put("device_id", sp.getInt("device_id", 0)).put("token", sp.getString("token", "")).put("sig", sp.getString("sig", "")), 45000);
                if (o.optBoolean("changed")) { Sync.run(this); Thread.sleep(2000); }
            } catch (InterruptedException e) { return; } catch (Exception e) { try { Thread.sleep(15000); } catch (InterruptedException x) { return; } }
        }
    }
    @Override public void onDestroy() { running = false; super.onDestroy(); }
}
