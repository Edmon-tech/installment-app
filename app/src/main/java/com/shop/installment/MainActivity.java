package com.shop.installment;
import android.app.Activity; import android.content.*; import android.net.Uri; import android.os.Bundle; import android.widget.*;
public class MainActivity extends Activity {
    TextView info; EditText code; Button reg, upd;
    Button btn(LinearLayout l, String s, android.view.View.OnClickListener c) { Button b = new Button(this); b.setText(s); b.setOnClickListener(c); l.addView(b); return b; }
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        ScrollView sv = new ScrollView(this); LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); l.setPadding(32, 32, 32, 32); sv.addView(l);
        info = new TextView(this); info.setTextSize(15); l.addView(info);
        code = new EditText(this); code.setHint("Enrollment code (from the shop)"); l.addView(code);
        reg = btn(l, "Register this phone", v -> { String c = code.getText().toString().trim(); if (!c.isEmpty()) { Policy.sp(this).edit().putString("enroll_code", c).apply(); syncNow(); } });
        btn(l, "Sync now", v -> syncNow());
        upd = btn(l, "Download update", v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Policy.sp(this).getString("apk_url", "")))));
        btn(l, "Allow location (if in your agreement)", v -> requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1));
        setContentView(sv); Sync.schedule(this);
    }
    void syncNow() { info.setText("Syncing..."); new Thread(() -> { Sync.run(this); runOnUiThread(this::render); }).start(); }
    @Override protected void onResume() { super.onResume(); render(); }
    void render() {
        SharedPreferences sp = Policy.sp(this); boolean registered = sp.contains("token"); boolean rel = sp.getBoolean("released", false);
        code.setVisibility(registered ? android.view.View.GONE : android.view.View.VISIBLE); reg.setVisibility(registered ? android.view.View.GONE : android.view.View.VISIBLE);
        boolean newer = sp.getInt("latest", 0) > Policy.verCode(this); upd.setVisibility(newer ? android.view.View.VISIBLE : android.view.View.GONE);
        StringBuilder s = new StringBuilder();
        s.append(sp.getString("business", "Phone Installment")).append("\nContact: ").append(sp.getString("contact", "-")).append("\n\n");
        s.append("Status: ").append(rel ? "Account fully paid - restrictions removed" : registered ? "Registered" : "Not registered").append('\n');
        s.append("Connection: ").append(sp.getString("conn", "-")).append('\n').append("Last sync: ").append(sp.getString("last_sync", "-")).append('\n');
        s.append("App version: ").append(Policy.verName(this)).append(newer ? "  (UPDATE AVAILABLE)" : "").append('\n');
        s.append("Device management: ").append(Policy.owner(this) ? "Active" : "Not active").append('\n');
        s.append("Location reporting: ").append(sp.getBoolean("loc_enabled", false) ? "ON (per your agreement)" : "OFF").append("\n\n");
        if (!sp.getString("balance", "").isEmpty()) s.append("Balance: PHP ").append(sp.getString("balance", "")).append("\nNext due: ").append(sp.getString("next_due", "")).append('\n');
        if (!sp.getString("reminder", "").isEmpty()) s.append('\n').append(sp.getString("reminder", "")).append('\n');
        s.append("\nPRIVACY & DEVICE MANAGEMENT NOTICE\n").append(Cfg.NOTICE);
        info.setText(s);
    }
}
