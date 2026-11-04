package com.shop.installment;
import android.app.*; import android.content.*; import android.content.pm.PackageManager; import android.location.*; import android.os.Build;
import androidx.work.*; import java.text.SimpleDateFormat; import java.util.*; import java.util.concurrent.TimeUnit; import org.json.*;
final class Sync {
    static void schedule(Context c) {
        PeriodicWorkRequest w = new PeriodicWorkRequest.Builder(SyncWorker.class, 15, TimeUnit.MINUTES)
            .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()).build();
        WorkManager.getInstance(c).enqueueUniquePeriodicWork("sync", ExistingPeriodicWorkPolicy.KEEP, w);
        WorkManager.getInstance(c).enqueue(new OneTimeWorkRequest.Builder(SyncWorker.class).build());
    }
    static synchronized void run(Context c) {
        SharedPreferences sp = Policy.sp(c);
        try {
            if (sp.getBoolean("released", false)) return;
            if (!sp.contains("token")) {
                String code = sp.getString("enroll_code", null);
                if (code == null) { sp.edit().putString("conn", "Not registered").apply(); return; }
                JSONObject r = new JSONObject().put("enroll_code", code).put("brand", Build.MANUFACTURER).put("model", Build.MODEL)
                    .put("android_version", Build.VERSION.RELEASE).put("app_version", Policy.verName(c));
                JSONObject o = Api.post("register", r);
                sp.edit().putInt("device_id", o.getInt("device_id")).putString("token", o.getString("token")).remove("enroll_code").apply();
            }
            JSONObject h = new JSONObject().put("device_id", sp.getInt("device_id", 0)).put("token", sp.getString("token", ""))
                .put("app_version", Policy.verName(c)).put("android_version", Build.VERSION.RELEASE);
            if (sp.getBoolean("loc_enabled", false)) { Location l = loc(c); if (l != null) h.put("lat", l.getLatitude()).put("lng", l.getLongitude()); }
            JSONObject o = Api.post("heartbeat", h);
            String old = sp.getString("reminder", "");
            sp.edit().putString("conn", "Connected").putString("last_sync", new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(new Date()))
                .putString("contact", o.optString("contact")).putString("business", o.optString("business")).putString("reminder", o.optString("reminder"))
                .putString("lock_message", o.optString("lock_message")).putString("balance", o.isNull("balance") ? "" : o.optString("balance"))
                .putString("next_due", o.isNull("next_due") ? "" : o.optString("next_due")).putBoolean("loc_enabled", o.optBoolean("location_enabled"))
                .putString("sig", o.optString("sig")).putInt("latest", o.optInt("latest_version")).putString("apk_url", o.optString("apk_url")).apply();
            if (o.optBoolean("release")) {
                try { Api.post("released", new JSONObject().put("device_id", sp.getInt("device_id", 0)).put("token", sp.getString("token", ""))); } catch (Exception ignored) {}
                Policy.release(c); return;
            }
            Policy.enforce(c); Policy.perms(c, o.optBoolean("location_enabled"));
            Policy.setLocked(c, o.optBoolean("lock"));
            JSONArray cmds = o.optJSONArray("commands");
            if (cmds != null && cmds.length() > 0) {
                JSONArray ids = new JSONArray();
                for (int i = 0; i < cmds.length(); i++) { JSONObject cm = cmds.getJSONObject(i); ids.put(cm.getInt("id"));
                    if ("message".equals(cm.optString("command")) && !cm.optString("payload").isEmpty()) notify(c, cm.optString("payload")); }
                Api.post("ack", new JSONObject().put("device_id", sp.getInt("device_id", 0)).put("token", sp.getString("token", "")).put("ids", ids));
            }
            String rem = o.optString("reminder"); if (!rem.isEmpty() && !rem.equals(old)) notify(c, rem);
        } catch (Exception e) { sp.edit().putString("conn", "Offline (" + e.getMessage() + ")").apply(); }
    }
    static Location loc(Context c) {
        if (c.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return null;
        try { LocationManager lm = (LocationManager) c.getSystemService(Context.LOCATION_SERVICE); Location best = null;
              for (String p : lm.getProviders(true)) { Location l = lm.getLastKnownLocation(p); if (l != null && (best == null || l.getTime() > best.getTime())) best = l; }
              return best; } catch (Exception e) { return null; }
    }
    static void notify(Context c, String text) {
        NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.createNotificationChannel(new NotificationChannel("pay", "Payment notices", NotificationManager.IMPORTANCE_HIGH));
        nm.notify((int) (System.currentTimeMillis() / 1000), new Notification.Builder(c, "pay").setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(Policy.sp(c).getString("business", "Payment notice")).setContentText(text).setStyle(new Notification.BigTextStyle().bigText(text)).build());
    }
}
