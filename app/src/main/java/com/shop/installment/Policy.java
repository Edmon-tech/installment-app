package com.shop.installment;
import android.app.ActivityOptions; import android.app.admin.DevicePolicyManager; import android.content.*;
import android.content.pm.PackageInfo; import android.os.*; import android.telecom.TelecomManager;
final class Policy {
    static SharedPreferences sp(Context c) { return c.getSharedPreferences("inst", Context.MODE_PRIVATE); }
    static ComponentName cn(Context c) { return new ComponentName(c, AdminReceiver.class); }
    static DevicePolicyManager dpm(Context c) { return (DevicePolicyManager) c.getSystemService(Context.DEVICE_POLICY_SERVICE); }
    static boolean owner(Context c) { return dpm(c).isDeviceOwnerApp(c.getPackageName()); }
    static int verCode(Context c) { try { PackageInfo p = c.getPackageManager().getPackageInfo(c.getPackageName(), 0); return (int) p.getLongVersionCode(); } catch (Exception e) { return 0; } }
    static String verName(Context c) { try { return c.getPackageManager().getPackageInfo(c.getPackageName(), 0).versionName; } catch (Exception e) { return "?"; } }

    static void saveExtras(Context c, Intent i) {
        try { PersistableBundle b = i.getParcelableExtra(DevicePolicyManager.EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE);
              if (b != null && b.getString("enroll_code") != null) sp(c).edit().putString("enroll_code", b.getString("enroll_code")).apply(); } catch (Exception ignored) {}
    }
    // Restrictions applied while the account is active
    static void enforce(Context c) {
        if (!owner(c)) return; DevicePolicyManager d = dpm(c); ComponentName a = cn(c);
        try {
            d.addUserRestriction(a, UserManager.DISALLOW_FACTORY_RESET);
            d.addUserRestriction(a, UserManager.DISALLOW_SAFE_BOOT);
            d.addUserRestriction(a, UserManager.DISALLOW_ADD_USER);
            if (Build.VERSION.SDK_INT >= 30) d.addUserRestriction(a, UserManager.DISALLOW_APPS_CONTROL);
            d.setUninstallBlocked(a, c.getPackageName(), true);
        } catch (Exception ignored) {}
    }
    static void perms(Context c, boolean loc) {
        if (!owner(c)) return; DevicePolicyManager d = dpm(c); ComponentName a = cn(c); String p = c.getPackageName();
        try {
            if (Build.VERSION.SDK_INT >= 33) d.setPermissionGrantState(a, p, "android.permission.POST_NOTIFICATIONS", DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED);
            if (loc) { d.setPermissionGrantState(a, p, "android.permission.ACCESS_FINE_LOCATION", DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED);
                       d.setPermissionGrantState(a, p, "android.permission.ACCESS_BACKGROUND_LOCATION", DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED); }
        } catch (Exception ignored) {}
    }
    static void setLocked(Context c, boolean lock) {
        boolean was = sp(c).getBoolean("locked", false); sp(c).edit().putBoolean("locked", lock).apply();
        if (!owner(c)) return; DevicePolicyManager d = dpm(c);
        if (lock) {
            if (!was) { try { d.lockNow(); } catch (Exception ignored) {} }
            String dialer = null; try { dialer = ((TelecomManager) c.getSystemService(Context.TELECOM_SERVICE)).getDefaultDialerPackage(); } catch (Exception ignored) {}
            d.setLockTaskPackages(cn(c), dialer != null ? new String[]{c.getPackageName(), dialer} : new String[]{c.getPackageName()});
            d.setLockTaskFeatures(cn(c), DevicePolicyManager.LOCK_TASK_FEATURE_NONE);
            if (!LockActivity.isOpen()) {
                Intent i = new Intent(c, LockActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ActivityOptions o = ActivityOptions.makeBasic(); o.setLockTaskEnabled(true);
                try { c.startActivity(i, o.toBundle()); } catch (Exception ignored) {}
            }
        } else { LockActivity.closeIfOpen(); try { d.setLockTaskPackages(cn(c), new String[]{}); } catch (Exception ignored) {} }
    }
    // Account fully paid: remove ALL restrictions and Device Owner status
    static void release(Context c) {
        sp(c).edit().putBoolean("released", true).putBoolean("locked", false).apply();
        androidx.work.WorkManager.getInstance(c).cancelUniqueWork("sync");
        if (!owner(c)) return; DevicePolicyManager d = dpm(c); ComponentName a = cn(c);
        LockActivity.closeIfOpen();
        try { d.setLockTaskPackages(a, new String[]{}); d.setUninstallBlocked(a, c.getPackageName(), false);
              for (String r : new String[]{UserManager.DISALLOW_FACTORY_RESET, UserManager.DISALLOW_SAFE_BOOT, UserManager.DISALLOW_ADD_USER, "no_control_apps"}) d.clearUserRestriction(a, r);
              d.clearDeviceOwnerApp(c.getPackageName()); } catch (Exception ignored) {}
    }
}
