package com.shop.installment;
import android.app.Activity; import android.app.admin.DevicePolicyManager; import android.content.Intent; import android.os.Bundle;
// Required on Android 9/10+ for QR provisioning of a fully managed device
public class ProvisionActivity extends Activity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b); Intent i = getIntent();
        if (DevicePolicyManager.ACTION_GET_PROVISIONING_MODE.equals(i.getAction())) {
            Intent r = new Intent(); r.putExtra(DevicePolicyManager.EXTRA_PROVISIONING_MODE, DevicePolicyManager.PROVISIONING_MODE_FULLY_MANAGED_DEVICE);
            setResult(RESULT_OK, r);
        } else { Policy.saveExtras(this, i); Policy.enforce(this); Sync.schedule(this); setResult(RESULT_OK); }
        finish();
    }
}
