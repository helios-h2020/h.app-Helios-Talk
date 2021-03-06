package eu.h2020.helios_social.happ.helios.talk.util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.PowerManager;
import android.text.SpannableStringBuilder;

import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.transition.Transition;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;

import org.acra.ACRA;

import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.MethodsNotNullByDefault;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.ParametersNotNullByDefault;

import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.android.system.AndroidExecutor;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.hardware.fingerprint.FingerprintManagerCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.Contact;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactId;

import static android.content.Context.KEYGUARD_SERVICE;
import static android.content.Context.POWER_SERVICE;
import static android.content.Intent.CATEGORY_DEFAULT;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.os.Build.MANUFACTURER;
import static android.os.Build.VERSION.SDK_INT;
import static android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS;
import static android.text.format.DateUtils.DAY_IN_MILLIS;
import static android.text.format.DateUtils.FORMAT_ABBREV_ALL;
import static android.text.format.DateUtils.FORMAT_ABBREV_MONTH;
import static android.text.format.DateUtils.FORMAT_ABBREV_RELATIVE;
import static android.text.format.DateUtils.FORMAT_ABBREV_TIME;
import static android.text.format.DateUtils.FORMAT_SHOW_DATE;
import static android.text.format.DateUtils.FORMAT_SHOW_TIME;
import static android.text.format.DateUtils.FORMAT_SHOW_YEAR;
import static android.text.format.DateUtils.MINUTE_IN_MILLIS;
import static android.text.format.DateUtils.WEEK_IN_MILLIS;
import static android.text.format.DateUtils.YEAR_IN_MILLIS;
import static android.view.KeyEvent.ACTION_DOWN;
import static android.view.KeyEvent.KEYCODE_ENTER;
import static android.view.inputmethod.EditorInfo.IME_NULL;
import static android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_AUTO_TIME;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;
import static androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode;
import static androidx.core.content.ContextCompat.getSystemService;
import static androidx.core.view.ViewCompat.LAYOUT_DIRECTION_RTL;
import static eu.h2020.helios_social.happ.helios.talk.BuildConfig.APPLICATION_ID;
import static java.util.Objects.requireNonNull;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class UiUtils {

	public static final long MIN_DATE_RESOLUTION = MINUTE_IN_MILLIS;
	public static final float GREY_OUT = 0.5f;

	public static void showSoftKeyboard(View view) {
		if (view.requestFocus()) {
			InputMethodManager imm = requireNonNull(getSystemService(
					view.getContext(), InputMethodManager.class));
			imm.showSoftInput(view, SHOW_IMPLICIT);
		}
	}

	public static void hideSoftKeyboard(View view) {
		InputMethodManager imm = requireNonNull(
				getSystemService(view.getContext(), InputMethodManager.class));
		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}

	public static String getContactDisplayName(Contact c) {
		return c.getAlias();
	}

	public static void setError(TextInputLayout til, @Nullable String error,
			boolean set) {
		if (set) {
			if (til.getError() == null) til.setError(error);
		} else {
			til.setError(null);
		}
	}

	public static String formatDate(Context ctx, long time) {
		int flags = FORMAT_ABBREV_RELATIVE |
				FORMAT_SHOW_DATE | FORMAT_ABBREV_TIME | FORMAT_ABBREV_MONTH;

		long diff = System.currentTimeMillis() - time;
		if (diff < MIN_DATE_RESOLUTION) return ctx.getString(R.string.now);
		if (diff >= DAY_IN_MILLIS && diff < WEEK_IN_MILLIS) {
			// also show time when older than a day, but newer than a week
			return DateUtils.getRelativeDateTimeString(ctx, time,
					MIN_DATE_RESOLUTION, WEEK_IN_MILLIS, flags).toString();
		}
		// otherwise just show "...ago" or date string
		return DateUtils.getRelativeTimeSpanString(time,
				System.currentTimeMillis(),
				MIN_DATE_RESOLUTION, flags).toString();
	}

	public static String formatDateAbsolute(Context ctx, long time) {
		int flags = FORMAT_SHOW_TIME | FORMAT_SHOW_DATE | FORMAT_ABBREV_ALL;
		long diff = System.currentTimeMillis() - time;
		if (diff >= YEAR_IN_MILLIS) flags |= FORMAT_SHOW_YEAR;
		return DateUtils.formatDateTime(ctx, time, flags);
	}

	/**
	 * Executes the runnable when clicking the link in the textView's text.
	 * <p>
	 * Attention: This assumes that there's only <b>one</b> link in the text.
	 */
	public static void onSingleLinkClick(TextView textView, Runnable runnable) {
		SpannableStringBuilder ssb =
				new SpannableStringBuilder(textView.getText());
		ClickableSpan[] spans =
				ssb.getSpans(0, ssb.length(), ClickableSpan.class);
		if (spans.length != 1) throw new AssertionError();
		ClickableSpan span = spans[0];
		int start = ssb.getSpanStart(span);
		int end = ssb.getSpanEnd(span);
		ssb.removeSpan(span);
		ClickableSpan cSpan = new ClickableSpan() {
			@Override
			public void onClick(View v) {
				runnable.run();
			}
		};
		ssb.setSpan(cSpan, start, end, 0);
		textView.setText(ssb);
		textView.setMovementMethod(new LinkMovementMethod());
	}

	public static String getAvatarTransitionName(ContactId c) {
		return "avatar" + c.getId();
	}

	public static String getBulbTransitionName(ContactId c) {
		return "bulb" + c.getId();
	}

	public static OnClickListener getGoToSettingsListener(Context context) {
		return (dialog, which) -> {
			Intent i = new Intent();
			i.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
			i.addCategory(CATEGORY_DEFAULT);
			i.setData(Uri.parse("package:" + APPLICATION_ID));
			i.addFlags(FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(i);
		};
	}

	public static void showOnboardingDialog(Context ctx, String text) {
		new AlertDialog.Builder(ctx, R.style.OnboardingDialogTheme)
				.setMessage(text)
				.setNeutralButton(R.string.got_it,
						(dialog, which) -> dialog.cancel())
				.show();
	}

	public static boolean needsDozeWhitelisting(Context ctx) {
		if (SDK_INT < 23) return false;
		PowerManager pm = (PowerManager) ctx.getSystemService(POWER_SERVICE);
		String packageName = ctx.getPackageName();
		if (pm == null) throw new AssertionError();
		return !pm.isIgnoringBatteryOptimizations(packageName);
	}

	@TargetApi(23)
	@SuppressLint("BatteryLife")
	public static Intent getDozeWhitelistingIntent(Context ctx) {
		Intent i = new Intent();
		i.setAction(ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
		i.setData(Uri.parse("package:" + ctx.getPackageName()));
		return i;
	}

	public static boolean isSamsung7() {
		return (SDK_INT == 24 || SDK_INT == 25) &&
				MANUFACTURER.equalsIgnoreCase("Samsung");
	}

	public static void setFilterTouchesWhenObscured(View v, boolean filter) {
		v.setFilterTouchesWhenObscured(filter);
		// Workaround for Android bug #13530806, see
		// https://android.googlesource.com/platform/frameworks/base/+/aba566589e0011c4b973c0d4f77be4e9ee176089%5E%21/core/java/android/view/View.java
		if (v.getFilterTouchesWhenObscured() != filter)
			v.setFilterTouchesWhenObscured(!filter);
	}

	public static void setTheme(Context ctx, String theme) {
		if (theme.equals(ctx.getString(R.string.pref_theme_light_value))) {
			setDefaultNightMode(MODE_NIGHT_NO);
		} else if (theme
				.equals(ctx.getString(R.string.pref_theme_dark_value))) {
			setDefaultNightMode(MODE_NIGHT_YES);
		} else if (theme
				.equals(ctx.getString(R.string.pref_theme_auto_value))) {
			// TODO remove AUTO-setting as it is deprecated
			setDefaultNightMode(MODE_NIGHT_AUTO_TIME);
		} else if (theme
				.equals(ctx.getString(R.string.pref_theme_system_value))) {
			setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM);
		}
	}

	public static int resolveAttribute(Context ctx, @AttrRes int attr) {
		TypedValue outValue = new TypedValue();
		ctx.getTheme().resolveAttribute(attr, outValue, true);
		return outValue.resourceId;
	}

	@ColorInt
	public static int resolveColorAttribute(Context ctx, @AttrRes int res) {
		@ColorRes
		int color = resolveAttribute(ctx, res);
		return ContextCompat.getColor(ctx, color);
	}

	public static boolean hasScreenLock(Context ctx) {
		return hasKeyguardLock(ctx) || hasUsableFingerprint(ctx);
	}

	public static boolean hasKeyguardLock(Context ctx) {
		if (SDK_INT < 21) return false;
		KeyguardManager keyguardManager =
				(KeyguardManager) ctx.getSystemService(KEYGUARD_SERVICE);
		if (keyguardManager == null) return false;
		// check if there's a lock mechanism we can use
		// first one is true if SIM card is locked, so use second if available
		return (SDK_INT < 23 && keyguardManager.isKeyguardSecure()) ||
				(SDK_INT >= 23 && keyguardManager.isDeviceSecure());
	}

	public static boolean hasUsableFingerprint(Context ctx) {
		if (SDK_INT < 28) return false;
		FingerprintManagerCompat fm = FingerprintManagerCompat.from(ctx);
		return fm.hasEnrolledFingerprints() && fm.isHardwareDetected();
	}

	public static void triggerFeedback(AndroidExecutor androidExecutor) {
		androidExecutor.runOnBackgroundThread(
				() -> ACRA.getErrorReporter()
						.handleException(new UserFeedback(), false));
	}

	public static boolean enterPressed(int actionId,
			@Nullable KeyEvent keyEvent) {
		return actionId == IME_NULL &&
				keyEvent != null &&
				keyEvent.getAction() == ACTION_DOWN &&
				keyEvent.getKeyCode() == KEYCODE_ENTER;
	}

	@RequiresApi(api = 21)
	public static void excludeSystemUi(Transition transition) {
		transition.excludeTarget(android.R.id.statusBarBackground, true);
		transition.excludeTarget(android.R.id.navigationBarBackground, true);
	}

	/**
	 * Observes the given {@link LiveData} until the first change.
	 * If the LiveData's value is available, the {@link Observer} will be
	 * called right away.
	 */
	@UiThread
	public static <T> void observeOnce(LiveData<T> liveData,
			LifecycleOwner owner, Observer<T> observer) {
		liveData.observe(owner, new Observer<T>() {
			@Override
			public void onChanged(@Nullable T t) {
				observer.onChanged(t);
				liveData.removeObserver(this);
			}
		});
	}

	/**
	 * Same as {@link #observeOnce(LiveData, LifecycleOwner, Observer)},
	 * but without a {@link LifecycleOwner}.
	 * <p>
	 * Warning: Do NOT call from objects that have a lifecycle.
	 */
	@UiThread
	public static <T> void observeForeverOnce(LiveData<T> liveData,
			Observer<T> observer) {
		liveData.observeForever(new Observer<T>() {
			@Override
			public void onChanged(@Nullable T t) {
				observer.onChanged(t);
				liveData.removeObserver(this);
			}
		});
	}

	public static boolean isRtl(Context ctx) {
		if (SDK_INT < 17) return false;
		return ctx.getResources().getConfiguration().getLayoutDirection() ==
				LAYOUT_DIRECTION_RTL;
	}
}
