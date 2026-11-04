package com.shop.installment;
import android.app.admin.DeviceAdminReceiver; import android.content.*;
public class AdminReceiver extends DeviceAdminReceiver {
    @Override public void onProfileProvisioningComplete(Context c, Intent i) { Policy.saveExtras(c, i); Policy.enforce(c); Sync.schedule(c); RealtimeService.start(c); }
    @Override public void onEnabled(Context c, Intent i) { Policy.enforce(c); }
}
