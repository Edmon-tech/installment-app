package com.shop.installment;
import android.content.*;
public class BootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context c, Intent i) {
        if (Policy.sp(c).getBoolean("released", false)) return;
        Sync.schedule(c);
        if (Policy.sp(c).getBoolean("locked", false)) Policy.setLocked(c, true);
    }
}
