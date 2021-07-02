package eu.h2020.helios_social.happ.helios.talk.settings;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.Event;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.EventBus;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.EventListener;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.MethodsNotNullByDefault;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.ParametersNotNullByDefault;
import eu.h2020.helios_social.modules.groupcommunications_utils.settings.Settings;
import eu.h2020.helios_social.modules.groupcommunications_utils.settings.SettingsManager;
import eu.h2020.helios_social.modules.groupcommunications_utils.settings.event.SettingsUpdatedEvent;
import eu.h2020.helios_social.modules.groupcommunications_utils.system.LocationUtils;
import eu.h2020.helios_social.modules.groupcommunications_utils.util.StringUtils;

import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.Localizer;
import eu.h2020.helios_social.happ.helios.talk.android.system.AndroidExecutor;
import eu.h2020.helios_social.happ.helios.talk.login.ChangePasswordActivity;
import eu.h2020.helios_social.happ.helios.talk.util.UiUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.text.TextUtilsCompat;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import eu.h2020.helios_social.happ.helios.talk.HeliosTalkApplication;
import eu.h2020.helios_social.happ.helios.talk.activity.RequestCodes;
import eu.h2020.helios_social.happ.android.AndroidNotificationManager;

import static android.app.Activity.RESULT_OK;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.media.RingtoneManager.ACTION_RINGTONE_PICKER;
import static android.media.RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI;
import static android.media.RingtoneManager.EXTRA_RINGTONE_EXISTING_URI;
import static android.media.RingtoneManager.EXTRA_RINGTONE_PICKED_URI;
import static android.media.RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT;
import static android.media.RingtoneManager.EXTRA_RINGTONE_TITLE;
import static android.media.RingtoneManager.EXTRA_RINGTONE_TYPE;
import static android.media.RingtoneManager.TYPE_NOTIFICATION;
import static android.os.Build.VERSION.SDK_INT;
import static android.provider.Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS;
import static android.provider.Settings.EXTRA_APP_PACKAGE;
import static android.provider.Settings.EXTRA_CHANNEL_ID;
import static android.provider.Settings.System.DEFAULT_NOTIFICATION_URI;
import static android.widget.Toast.LENGTH_SHORT;
import static androidx.core.view.ViewCompat.LAYOUT_DIRECTION_LTR;
import static eu.h2020.helios_social.modules.groupcommunications_utils.settings.SettingsConsts.PREF_CONTENT_PROFILING;
import static eu.h2020.helios_social.modules.groupcommunications_utils.settings.SettingsConsts.PREF_RECOMMENDATION_MINER;
import static eu.h2020.helios_social.modules.groupcommunications_utils.settings.SettingsConsts.PREF_SHARE_PREFS;
import static eu.h2020.helios_social.modules.groupcommunications_utils.settings.SettingsConsts.SETTINGS_NAMESPACE;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static eu.h2020.helios_social.modules.groupcommunications_utils.util.LogUtils.logDuration;
import static eu.h2020.helios_social.modules.groupcommunications_utils.util.LogUtils.logException;
import static eu.h2020.helios_social.modules.groupcommunications_utils.util.LogUtils.now;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class SettingsFragment extends PreferenceFragmentCompat
        implements EventListener, OnPreferenceChangeListener {

    public static final String LANGUAGE = "pref_key_language";
    public static final String PREF_SCREEN_LOCK = "pref_key_lock";
    public static final String PREF_SCREEN_LOCK_TIMEOUT =
            "pref_key_lock_timeout";
    public static final String NOTIFY_SIGN_IN = "pref_key_notify_sign_in";

    private static final Logger LOG =
            Logger.getLogger(SettingsFragment.class.getName());

    private static final int REQUEST_ACCESS_MEDIA_METADATA = 0;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static String[] MEDIA_LOCATION_PERMISSION = {
            Manifest.permission.ACCESS_MEDIA_LOCATION
    };

    private SettingsActivity listener;
    private ListPreference language;
    private ListPreference enableProfiling;
    private ListPreference minerPref;
    private SwitchPreference enableSharePreferences;
    private SwitchPreference screenLock;
    private ListPreference screenLockTimeout;
    private SwitchPreference notifyPrivateMessages;
    private SwitchPreference notifyGroupMessages;
    private SwitchPreference notifyForumPosts;
    private SwitchPreference notifyVibration;

    private Preference notifySound;

    // Fields that are accessed from background threads must be volatile
    private volatile Settings settings;
    private volatile boolean settingsLoaded = false;

    @Inject
    volatile SettingsManager settingsManager;
    @Inject
    volatile EventBus eventBus;
    @Inject
    LocationUtils locationUtils;

    @Inject
    AndroidExecutor androidExecutor;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (SettingsActivity) context;
        listener.getActivityComponent().inject(this);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.settings);

        language = (ListPreference) findPreference(LANGUAGE);
        setLanguageEntries();
        ListPreference theme =
                (ListPreference) findPreference("pref_key_theme");
        enableProfiling = (ListPreference) findPreference("pref_key_profiling");
        enableSharePreferences = (SwitchPreference) findPreference("pref_key_preferences");
        minerPref = (ListPreference) findPreference("pref_key_miner");
        screenLock = (SwitchPreference) findPreference(PREF_SCREEN_LOCK);
        screenLockTimeout =
                (ListPreference) findPreference(PREF_SCREEN_LOCK_TIMEOUT);
        notifyPrivateMessages = (SwitchPreference) findPreference(
                "pref_key_notify_private_messages");
        notifyGroupMessages = (SwitchPreference) findPreference(
                "pref_key_notify_group_messages");
        notifyForumPosts = (SwitchPreference) findPreference(
                "pref_key_notify_forum_posts");
		/*notifyBlogPosts = (SwitchPreference) findPreference(
				"pref_key_notify_blog_posts");*/
        notifyVibration = (SwitchPreference) findPreference(
                "pref_key_notify_vibration");
        notifySound = findPreference("pref_key_notify_sound");

        language.setOnPreferenceChangeListener(this);
        theme.setOnPreferenceChangeListener((preference, newValue) -> {
            if (getActivity() != null) {
                // activate new theme
                UiUtils.setTheme(getActivity(), (String) newValue);
                // bring up parent activity, so it can change its theme as well
                // upstream bug: https://issuetracker.google.com/issues/38352704
                Intent intent =
                        new Intent(getActivity(),
                                   HeliosTalkApplication.ENTRY_ACTIVITY);
                intent.setFlags(
                        FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                // bring this activity back to the foreground
                intent = new Intent(getActivity(), getActivity().getClass());
                startActivity(intent);
                getActivity().finish();
            }
            return true;
        });
        enableProfiling.setOnPreferenceChangeListener(this);
        enableSharePreferences.setOnPreferenceChangeListener(this);
        minerPref.setOnPreferenceChangeListener(this);
        screenLock.setOnPreferenceChangeListener(this);
        screenLockTimeout.setOnPreferenceChangeListener(this);

        if (SDK_INT < 27) {
            // remove System Default Theme option from preference entries
            // as it is not functional on this API anyway
            List<CharSequence> entries =
                    new ArrayList<>(Arrays.asList(theme.getEntries()));
            entries.remove(getString(R.string.pref_theme_system));
            theme.setEntries(entries.toArray(new CharSequence[0]));
            // also remove corresponding value
            List<CharSequence> values =
                    new ArrayList<>(Arrays.asList(theme.getEntryValues()));
            values.remove(getString(R.string.pref_theme_system_value));
            theme.setEntryValues(values.toArray(new CharSequence[0]));
        }

        findPreference("pref_key_change_password")
                .setOnPreferenceClickListener(v -> {
                    startActivity(new Intent(getActivity(),
                                             ChangePasswordActivity.class));
                    return true;
                });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        ColorDrawable divider = new ColorDrawable(
                ContextCompat.getColor(requireContext(), R.color.divider));
        setDivider(divider);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        eventBus.addListener(this);
        setSettingsEnabled(false);
        loadSettings();
    }

    @Override
    public void onStop() {
        super.onStop();
        eventBus.removeListener(this);
    }

    private void setLanguageEntries() {
        CharSequence[] tags = language.getEntryValues();
        List<CharSequence> entries = new ArrayList<>(tags.length);
        List<CharSequence> entryValues = new ArrayList<>(tags.length);
        for (CharSequence cs : tags) {
            String tag = cs.toString();
            if (tag.equals("default")) {
                entries.add(getString(R.string.pref_language_default));
                entryValues.add(tag);
                continue;
            }
            Locale locale = Localizer.getLocaleFromTag(tag);
            if (locale == null)
                throw new IllegalStateException();
            // Exclude RTL locales on API < 17, they won't be laid out correctly
            if (SDK_INT < 17 && !isLeftToRight(locale)) {
                if (LOG.isLoggable(INFO))
                    LOG.info("Skipping RTL locale " + tag);
                continue;
            }
            String nativeName = locale.getDisplayName(locale);
            // Fallback to English if the name is unknown in both native and
            // current locale.
            if (nativeName.equals(tag)) {
                String tmp = locale.getDisplayLanguage(Locale.ENGLISH);
                if (!tmp.isEmpty() && !tmp.equals(nativeName))
                    nativeName = tmp;
            }
            // Prefix with LRM marker to prevent any RTL direction
            entries.add("\u200E" + nativeName.substring(0, 1).toUpperCase()
                                + nativeName.substring(1));
            entryValues.add(tag);
        }
        language.setEntries(entries.toArray(new CharSequence[0]));
        language.setEntryValues(entryValues.toArray(new CharSequence[0]));
    }

    private boolean isLeftToRight(Locale locale) {
        // TextUtilsCompat returns the wrong direction for Hebrew on some phones
        String language = locale.getLanguage();
        if (language.equals("iw") || language.equals("he")) return false;
        int direction = TextUtilsCompat.getLayoutDirectionFromLocale(locale);
        return direction == LAYOUT_DIRECTION_LTR;
    }

    private void loadSettings() {
        listener.runOnDbThread(() -> {
            try {
                long start = now();
                settings = settingsManager.getSettings(SETTINGS_NAMESPACE);
                settingsLoaded = true;
                logDuration(LOG, "Loading settings", start);
                displaySettings();
            } catch (DbException e) {
                logException(LOG, WARNING, e);
            }
        });
    }

    private void displaySettings() {
        listener.runOnUiThreadUnlessDestroyed(() -> {
            // due to events, we might try to display before a load completed
            if (!settingsLoaded) return;

            displayScreenLockSetting();

            enableProfiling.setValueIndex(settings.getInt(PREF_CONTENT_PROFILING, 0));
            enableSharePreferences.setChecked(settings.getBoolean(PREF_SHARE_PREFS, true));
            minerPref.setValueIndex(settings.getInt(PREF_RECOMMENDATION_MINER, 0));

            if (SDK_INT < 26) {
                notifyPrivateMessages.setChecked(settings.getBoolean(
                        AndroidNotificationManager.PREF_NOTIFY_PRIVATE, true));
                notifyGroupMessages.setChecked(settings.getBoolean(
                        AndroidNotificationManager.PREF_NOTIFY_GROUP, true));
                notifyForumPosts.setChecked(settings.getBoolean(
                        AndroidNotificationManager.PREF_NOTIFY_FORUM, true));
                notifyVibration.setChecked(settings.getBoolean(
                        AndroidNotificationManager.PREF_NOTIFY_VIBRATION,
                        true));
                notifyPrivateMessages.setOnPreferenceChangeListener(this);
                notifyGroupMessages.setOnPreferenceChangeListener(this);
                notifyForumPosts.setOnPreferenceChangeListener(this);
                notifyVibration.setOnPreferenceChangeListener(this);
                notifySound.setOnPreferenceClickListener(
                        pref -> onNotificationSoundClicked());
                String text;
                if (settings.getBoolean(
                        AndroidNotificationManager.PREF_NOTIFY_SOUND, true)) {
                    String ringtoneName =
                            settings.get(
                                    AndroidNotificationManager.PREF_NOTIFY_RINGTONE_NAME);
                    if (StringUtils.isNullOrEmpty(ringtoneName)) {
                        text = getString(R.string.notify_sound_setting_default);
                    } else {
                        text = ringtoneName;
                    }
                } else {
                    text = getString(R.string.notify_sound_setting_disabled);
                }
                notifySound.setSummary(text);
            } else {
                setupNotificationPreference(notifyPrivateMessages,
                                            AndroidNotificationManager.CONTACT_CHANNEL_ID,
                                            R.string.notify_private_messages_setting_summary_26);
                setupNotificationPreference(notifyGroupMessages,
                                            AndroidNotificationManager.GROUP_CHANNEL_ID,
                                            R.string.notify_group_messages_setting_summary_26);
                setupNotificationPreference(notifyForumPosts,
                                            AndroidNotificationManager.FORUM_CHANNEL_ID,
                                            R.string.notify_forum_posts_setting_summary_26);

                notifyVibration.setVisible(false);
                notifySound.setVisible(false);
            }
            setSettingsEnabled(true);
        });
    }

    private void setSettingsEnabled(boolean enabled) {
        if (!enabled) screenLock.setEnabled(false);
        notifyPrivateMessages.setEnabled(enabled);
        notifyGroupMessages.setEnabled(enabled);
        notifyForumPosts.setEnabled(enabled);
        notifyVibration.setEnabled(enabled);
        notifySound.setEnabled(enabled);
    }

    private void displayScreenLockSetting() {
        if (SDK_INT < 21) {
            screenLock.setVisible(false);
            screenLockTimeout.setVisible(false);
        } else {
            if (getActivity() != null && UiUtils.hasScreenLock(getActivity())) {
                screenLock.setEnabled(true);
                screenLock.setChecked(
                        settings.getBoolean(PREF_SCREEN_LOCK, false));
                screenLock.setSummary(R.string.pref_lock_summary);
            } else {
                screenLock.setEnabled(false);
                screenLock.setChecked(false);
                screenLock.setSummary(R.string.pref_lock_disabled_summary);
            }
            // timeout depends on screenLock and gets disabled automatically
            int timeout = settings.getInt(PREF_SCREEN_LOCK_TIMEOUT,
                                          Integer.valueOf(getString(
                                                  R.string.pref_lock_timeout_value_default)));
            String newValue = String.valueOf(timeout);
            screenLockTimeout.setValue(newValue);
            setScreenLockTimeoutSummary(newValue);
        }
    }

    private void setScreenLockTimeoutSummary(String timeout) {
        String never = getString(R.string.pref_lock_timeout_value_never);
        if (timeout.equals(never)) {
            screenLockTimeout
                    .setSummary(R.string.pref_lock_timeout_never_summary);
        } else {
            screenLockTimeout
                    .setSummary(R.string.pref_lock_timeout_summary);
        }
    }

    @TargetApi(26)
    private void setupNotificationPreference(SwitchPreference pref,
                                             String channelId, @StringRes int summary) {
        pref.setWidgetLayoutResource(0);
        pref.setSummary(summary);
        pref.setOnPreferenceClickListener(clickedPref -> {
            String packageName = requireContext().getPackageName();
            Intent intent = new Intent(ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                    .putExtra(EXTRA_APP_PACKAGE, packageName)
                    .putExtra(EXTRA_CHANNEL_ID, channelId);
            Context ctx = requireContext();
            if (intent.resolveActivity(ctx.getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(ctx, R.string.error_start_activity, LENGTH_SHORT)
                        .show();
            }
            return true;
        });
    }

    private boolean onNotificationSoundClicked() {
        String title = getString(R.string.choose_ringtone_title);
        Intent i = new Intent(ACTION_RINGTONE_PICKER);
        i.putExtra(EXTRA_RINGTONE_TYPE, TYPE_NOTIFICATION);
        i.putExtra(EXTRA_RINGTONE_TITLE, title);
        i.putExtra(EXTRA_RINGTONE_DEFAULT_URI,
                   DEFAULT_NOTIFICATION_URI);
        i.putExtra(EXTRA_RINGTONE_SHOW_SILENT, true);
        if (settings.getBoolean(AndroidNotificationManager.PREF_NOTIFY_SOUND,
                                true)) {
            Uri uri;
            String ringtoneUri =
                    settings.get(
                            AndroidNotificationManager.PREF_NOTIFY_RINGTONE_URI);
            if (StringUtils.isNullOrEmpty(ringtoneUri))
                uri = DEFAULT_NOTIFICATION_URI;
            else uri = Uri.parse(ringtoneUri);
            i.putExtra(EXTRA_RINGTONE_EXISTING_URI, uri);
        }
        if (i.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivityForResult(i, RequestCodes.REQUEST_RINGTONE);
        } else {
            Toast.makeText(getContext(), R.string.cannot_load_ringtone,
                           LENGTH_SHORT).show();
        }
        return true;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == language) {
            if (!language.getValue().equals(newValue))
                languageChanged((String) newValue);
            return false;
        } else if (preference == enableProfiling) {
            int profilingSetting = Integer.valueOf((String) newValue);
            LOG.info("Profiling Setting: " + profilingSetting);
            if (profilingSetting != 0) verifyStoragePermissions();
            Settings s = new Settings();
            s.putInt(PREF_CONTENT_PROFILING, profilingSetting);
            storeSettings(s);
        } else if (preference == enableSharePreferences) {
            Settings s = new Settings();
            s.putBoolean(PREF_SHARE_PREFS, (Boolean) newValue);
            storeSettings(s);
        } else if (preference == minerPref) {
            Settings s = new Settings();
            String value = (String) newValue;
            s.putInt(PREF_RECOMMENDATION_MINER, Integer.valueOf(value));
            storeSettings(s);
        } else if (preference == screenLock) {
            Settings s = new Settings();
            s.putBoolean(PREF_SCREEN_LOCK, (Boolean) newValue);
            storeSettings(s);
        } else if (preference == screenLockTimeout) {
            Settings s = new Settings();
            String value = (String) newValue;
            s.putInt(PREF_SCREEN_LOCK_TIMEOUT, Integer.valueOf(value));
            storeSettings(s);
            setScreenLockTimeoutSummary(value);
        } else if (preference == notifyPrivateMessages) {
            Settings s = new Settings();
            s.putBoolean(AndroidNotificationManager.PREF_NOTIFY_PRIVATE,
                         (Boolean) newValue);
            storeSettings(s);
        } else if (preference == notifyGroupMessages) {
            Settings s = new Settings();
            s.putBoolean(AndroidNotificationManager.PREF_NOTIFY_GROUP,
                         (Boolean) newValue);
            storeSettings(s);
        } else if (preference == notifyForumPosts) {
            Settings s = new Settings();
            s.putBoolean(AndroidNotificationManager.PREF_NOTIFY_FORUM,
                         (Boolean) newValue);
            storeSettings(s);
        } else if (preference == notifyVibration) {
            Settings s = new Settings();
            s.putBoolean(AndroidNotificationManager.PREF_NOTIFY_VIBRATION,
                         (Boolean) newValue);
            storeSettings(s);
        }
        return true;
    }

    private void languageChanged(String newValue) {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.pref_language_title);
        builder.setMessage(R.string.pref_language_changed);
        builder.setPositiveButton(R.string.sign_out_button,
                                  (dialogInterface, i) -> {
                                      language.setValue(newValue);
                                      Intent intent = new Intent(getContext(),
                                                                 HeliosTalkApplication.ENTRY_ACTIVITY);
                                      intent.setFlags(FLAG_ACTIVITY_CLEAR_TOP);
                                      //intent.setData(SIGN_OUT_URI);
                                      requireActivity().startActivity(intent);
                                      requireActivity().finish();
                                  });
        builder.setNegativeButton(R.string.cancel, null);
        builder.setCancelable(false);
        builder.show();
    }

	/*private void storeProfilingSetting(int profilingSetting) {
		Settings s = new Settings();
		s.putInt(PREF_PROF_ALG, profilingSetting);
		mergeSettings(s, PROFILING_NAMESPACE);
	}*/

    private void storeSettings(Settings s) {
        mergeSettings(s, SETTINGS_NAMESPACE);
    }

    private void mergeSettings(Settings s, String namespace) {
        listener.runOnDbThread(() -> {
            try {
                long start = now();
                settingsManager.mergeSettings(s, namespace);
                logDuration(LOG, "Merging settings", start);
            } catch (DbException e) {
                logException(LOG, WARNING, e);
            }
        });
    }

    @Override
    public void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (request == RequestCodes.REQUEST_RINGTONE && result == RESULT_OK) {
            Settings s = new Settings();
            Uri uri = data.getParcelableExtra(EXTRA_RINGTONE_PICKED_URI);
            if (uri == null) {
                // The user chose silence
                s.putBoolean(AndroidNotificationManager.PREF_NOTIFY_SOUND,
                             false);
                s.put(AndroidNotificationManager.PREF_NOTIFY_RINGTONE_NAME, "");
                s.put(AndroidNotificationManager.PREF_NOTIFY_RINGTONE_URI, "");
            } else if (RingtoneManager.isDefault(uri)) {
                // The user chose the default
                s.putBoolean(AndroidNotificationManager.PREF_NOTIFY_SOUND,
                             true);
                s.put(AndroidNotificationManager.PREF_NOTIFY_RINGTONE_NAME, "");
                s.put(AndroidNotificationManager.PREF_NOTIFY_RINGTONE_URI, "");
            } else {
                // The user chose a ringtone other than the default
                Ringtone r = RingtoneManager.getRingtone(getContext(), uri);
                if (r == null || "file".equals(uri.getScheme())) {
                    Toast.makeText(getContext(), R.string.cannot_load_ringtone,
                                   LENGTH_SHORT).show();
                } else {
                    String name = r.getTitle(getContext());
                    s.putBoolean(AndroidNotificationManager.PREF_NOTIFY_SOUND,
                                 true);
                    s.put(AndroidNotificationManager.PREF_NOTIFY_RINGTONE_NAME,
                          name);
                    s.put(AndroidNotificationManager.PREF_NOTIFY_RINGTONE_URI,
                          uri.toString());
                }
            }
            storeSettings(s);
        }
    }

    @Override
    public void eventOccurred(Event e) {
        if (e instanceof SettingsUpdatedEvent) {
            SettingsUpdatedEvent s = (SettingsUpdatedEvent) e;
            String namespace = s.getNamespace();
            if (namespace.equals(SETTINGS_NAMESPACE)) {
                LOG.info("Settings updated");
                settings = s.getSettings();
                displaySettings();
            }
        }
    }

    /**
     * request storage permissions from user to access image collection
     */
    public void verifyStoragePermissions() {
        // Check if we have write permission
        if (ContextCompat.checkSelfPermission(getActivity(),
                                              Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getActivity(),
                                                  Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            verifyMetadataPermissions();
            return;
        }

        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                                                                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(getActivity())
                    .setTitle("Access Storage Permission!")
                    .setMessage(R.string.profiling_storage_permissions)
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(
                                    getActivity(),
                                    PERMISSIONS_STORAGE,
                                    REQUEST_EXTERNAL_STORAGE);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            dialog.dismiss();
                        }
                    }).create().show();

        } else {
            ActivityCompat.requestPermissions(
                    getActivity(),
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
        }
    }

    public void verifyMetadataPermissions() {
        //check if access to metadata has been granted.
        if (ContextCompat.checkSelfPermission(getActivity(),
                                              Manifest.permission.ACCESS_MEDIA_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return;
        }

        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                                                                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(getActivity())
                    .setTitle("Access to Media Metadata!")
                    .setMessage(R.string.profiling_metadata_permissions)
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(
                                    getActivity(),
                                    MEDIA_LOCATION_PERMISSION,
                                    REQUEST_ACCESS_MEDIA_METADATA);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            dialog.dismiss();
                        }
                    }).create().show();

        } else {
            ActivityCompat.requestPermissions(
                    getActivity(),
                    MEDIA_LOCATION_PERMISSION,
                    REQUEST_ACCESS_MEDIA_METADATA);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                verifyMetadataPermissions();
            }
        } else if (requestCode == REQUEST_ACCESS_MEDIA_METADATA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                return;
            }
        } else {
            Toast.makeText(getActivity(), "Permissions did not grant!", Toast.LENGTH_LONG).show();
        }
    }
}
