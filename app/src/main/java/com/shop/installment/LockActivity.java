package com.shop.installment;
import android.app.Activity; import android.content.Intent; import android.net.Uri; import android.os.Bundle; import android.view.Gravity; import android.widget.*;
public class LockActivity extends Activity {
    static LockActivity cur;
    static boolean isOpen() { return cur != null; }
    static void closeIfOpen() { final LockActivity a = cur; if (a != null) a.runOnUiThread(() -> { try { a.stopLockTask(); } catch (Exception ignored) {} a.finish(); }); }
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b); cur = this;
        android.content.SharedPreferences sp = Policy.sp(this);
        LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); l.setGravity(Gravity.CENTER); l.setPadding(48, 48, 48, 48);
        TextView t = new TextView(this); t.setTextSize(26); t.setText("Payment Required"); t.setGravity(Gravity.CENTER); l.addView(t);
        TextView m = new TextView(this); m.setTextSize(17); m.setGravity(Gravity.CENTER); m.setPadding(0, 32, 0, 32);
        m.setText(sp.getString("lock_message", "") + "\n\n" + sp.getString("business", "") + "\n" + sp.getString("contact", "")); l.addView(m);
        final String contact = sp.getString("contact", "");
        Button call = new Button(this); call.setText("Call the shop"); call.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + contact)))); l.addView(call);
        Button em = new Button(this); em.setText("Emergency call (911)"); em.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:911")))); l.addView(em);
        setContentView(l);
    }
    @Override protected void onResume() { super.onResume(); try { startLockTask(); } catch (Exception ignored) {} }
    @Override public void onBackPressed() { /* blocked while locked */ }
    @Override protected void onDestroy() { if (cur == this) cur = null; super.onDestroy(); }
}
