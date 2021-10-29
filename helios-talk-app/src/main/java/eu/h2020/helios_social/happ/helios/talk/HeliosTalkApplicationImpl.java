package eu.h2020.helios_social.happ.helios.talk;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.os.StrictMode.VmPolicy;
import android.preference.PreferenceManager;

import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.google.GoogleEmojiProvider;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.annotation.AcraCore;
import org.acra.annotation.AcraDialog;
import org.acra.annotation.AcraMailSender;
import org.acra.data.StringFormat;

import eu.h2020.helios_social.happ.helios.talk.android.HeliosTalkAndroidEagerSingletons;
import eu.h2020.helios_social.modules.groupcommunications.db.GroupCommunicationsDBEagerSingletons;
import eu.h2020.helios_social.happ.helios.talk.logging.CachingLogHandler;
import eu.h2020.helios_social.happ.helios.talk.util.UiUtils;
import eu.h2020.helios_social.modules.groupcommunications.GroupCommunicationsEagerSingletons;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
import static eu.h2020.helios_social.happ.helios.talk.TestingConstants.IS_DEBUG_BUILD;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.INFO;
import static java.util.logging.Logger.getLogger;

@AcraCore(reportContent = {ReportField.APP_VERSION_CODE,
        ReportField.APP_VERSION_NAME,
        ReportField.USER_COMMENT,
        ReportField.SHARED_PREFERENCES,
        ReportField.ANDROID_VERSION,
        ReportField.CUSTOM_DATA,
        ReportField.STACK_TRACE,
        ReportField.BUILD,
        ReportField.BUILD_CONFIG,
        ReportField.CRASH_CONFIGURATION,
        ReportField.DISPLAY,
        ReportField.LOGCAT
},
        logcatArguments = {"-t", "1000", "-v", "time"},
        reportFormat = StringFormat.JSON
)
@AcraMailSender(mailTo = "happ.helios.talk@gmail.com")
@AcraDialog(resCommentPrompt = R.string.crash_dialog_comment_prompt,
        resText = R.string.crash_dialog_text)
public class HeliosTalkApplicationImpl extends Application
        implements HeliosTalkApplication {

    private static final Logger LOG =
            getLogger(HeliosTalkApplicationImpl.class.getName());

    private final CachingLogHandler logHandler = new CachingLogHandler();
    private FileHandler fileHandler;

    private AndroidComponent applicationComponent;
    private volatile SharedPreferences prefs;

    @Override
    protected void attachBaseContext(Context base) {
        if (prefs == null)
            prefs = PreferenceManager.getDefaultSharedPreferences(base);
        Localizer.initialize(prefs);

        super.attachBaseContext(
                Localizer.getInstance().setLocale(base));
        setTheme(base, prefs);
        ACRA.init(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //if (IS_DEBUG_BUILD) enableStrictMode();

        Logger rootLogger = getLogger("");

        Handler[] handlers = rootLogger.getHandlers();
        // Disable the Android logger for release builds
        for (Handler handler : handlers) rootLogger.removeHandler(handler);
        if (IS_DEBUG_BUILD) {
            // We can't set the level of the Android logger at runtime, so
            // raise records to the logger's default level
            rootLogger.addHandler(new LevelRaisingHandler(FINE, INFO));
            // Restore the default handlers after the level raising handler
            for (Handler handler : handlers) rootLogger.addHandler(handler);
        }
        rootLogger.addHandler(logHandler);
        try {
            SimpleDateFormat format = new SimpleDateFormat("YYYY-M-d_HHmmss");
            String LOG_FILE = "helios-talk-" + format.format(Calendar.getInstance().getTime()) + ".log";
            fileHandler = new FileHandler(getApplicationContext()
                                                  .getExternalFilesDir("/logs") + "/" + LOG_FILE);
            fileHandler.setFormatter(new SimpleFormatter());
            rootLogger.addHandler(fileHandler);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        rootLogger.setLevel(IS_DEBUG_BUILD ? FINE : INFO);

        LOG.info("Created");

        applicationComponent = createApplicationComponent();
        EmojiManager.install(new GoogleEmojiProvider());
    }

    protected AndroidComponent createApplicationComponent() {
        AndroidComponent androidComponent = DaggerAndroidComponent.builder()
                .appModule(new AppModule(this))
                .build();

        // We need to load the eager singletons directly after making the
        // dependency graphs
        GroupCommunicationsDBEagerSingletons.Helper
                .injectEagerSingletons(androidComponent);
        HeliosTalkAndroidEagerSingletons.Helper
                .injectEagerSingletons(androidComponent);
        GroupCommunicationsEagerSingletons.Helper
                .injectEagerSingletons(androidComponent);
        AndroidEagerSingletons.Helper.injectEagerSingletons(androidComponent);
        LOG.info("created new app component");
        return androidComponent;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Localizer.getInstance().setLocale(this);
    }

    private void setTheme(Context ctx, SharedPreferences prefs) {
        String theme = prefs.getString("pref_key_theme", null);
        if (theme == null) {
            // set default value
            theme = getString(R.string.pref_theme_dark_value);
            prefs.edit().putString("pref_key_theme", theme).apply();
        }
        // set theme
        UiUtils.setTheme(ctx, theme);
    }

    private void enableStrictMode() {
        ThreadPolicy.Builder threadPolicy = new ThreadPolicy.Builder();
        threadPolicy.detectAll();
        threadPolicy.permitDiskReads();
        threadPolicy.permitDiskWrites();
        threadPolicy.penaltyLog();
        StrictMode.setThreadPolicy(threadPolicy.build());
        VmPolicy.Builder vmPolicy = new VmPolicy.Builder();
        vmPolicy.detectAll();
        vmPolicy.penaltyLog();
        StrictMode.setVmPolicy(vmPolicy.build());
    }

    @Override
    public Collection<LogRecord> getRecentLogRecords() {
        return logHandler.getRecentLogRecords();
    }

    @Override
    public AndroidComponent getApplicationComponent() {
        return applicationComponent;
    }

    @Override
    public SharedPreferences getDefaultSharedPreferences() {
        return prefs;
    }

    @Override
    public boolean isRunningInBackground() {
        RunningAppProcessInfo info = new RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(info);
        return (info.importance != IMPORTANCE_FOREGROUND);
    }

/*    public void clearComponent() {
        applicationComponent = null;
    }

    public void createComponent() {
        this.applicationComponent = createApplicationComponent();
    }*/
}
