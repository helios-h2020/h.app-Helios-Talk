package eu.h2020.helios_social.happ.helios.talk;

import android.app.Activity;
import android.content.SharedPreferences;

import eu.h2020.helios_social.happ.helios.talk.navdrawer.NavDrawerActivity;

import java.util.Collection;
import java.util.logging.LogRecord;

/**
 * This exists so that the Application object will not necessarily be cast
 * directly to the HeliosTalk application object.
 */
public interface HeliosTalkApplication {

	Class<? extends Activity> ENTRY_ACTIVITY = NavDrawerActivity.class;

	Collection<LogRecord> getRecentLogRecords();

	AndroidComponent getApplicationComponent();

	SharedPreferences getDefaultSharedPreferences();

	boolean isRunningInBackground();
}
