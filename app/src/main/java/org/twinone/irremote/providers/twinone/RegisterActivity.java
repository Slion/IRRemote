package org.twinone.irremote.providers.twinone;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import org.twinone.irremote.R;
import org.twinone.irremote.compat.ToolbarActivity;
import org.twinone.irremote.ui.SettingsActivity;

public class RegisterActivity extends ToolbarActivity {

    /**
     * This boolean preference contains true if the user is registered and
     * verified
     */
    private static final String PREF_KEY_REGISTERED = "org.twinone.irremote.registered_user";

    /**
     * Returns true if this user is registered and verified
     */
    public static boolean isRegistered(Context c) {
        return SettingsActivity.getPreferences(c).getBoolean(
                PREF_KEY_REGISTERED, false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empty);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (isVerifyIntent()) {
            addFragment(new VerifyFragment());
        } else {
            addFragment(new RegisterFragment());
        }
    }

    private boolean isVerifyIntent() {
        Uri data = getIntent().getData();
        return data != null && data.getQueryParameter("a").equals("verify");
    }

    void addFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment).commit();
    }

    @Override
    public boolean onNavigateUp() {
        return onSupportNavigateUp();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

}
