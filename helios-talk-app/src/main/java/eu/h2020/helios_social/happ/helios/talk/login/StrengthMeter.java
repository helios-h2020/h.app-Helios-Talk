package eu.h2020.helios_social.happ.helios.talk.login;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;

import static android.graphics.Color.BLACK;
import static android.graphics.Paint.Style.FILL;
import static android.graphics.Paint.Style.STROKE;
import static android.graphics.drawable.ClipDrawable.HORIZONTAL;
import static android.view.Gravity.LEFT;
import static android.view.Gravity.START;
import static eu.h2020.helios_social.happ.helios.talk.api.crypto.PasswordStrengthEstimator.QUITE_STRONG;
import static eu.h2020.helios_social.happ.helios.talk.api.crypto.PasswordStrengthEstimator.QUITE_WEAK;
import static eu.h2020.helios_social.happ.helios.talk.api.crypto.PasswordStrengthEstimator.STRONG;
import static eu.h2020.helios_social.happ.helios.talk.api.crypto.PasswordStrengthEstimator.WEAK;

public class StrengthMeter extends ProgressBar {

	private static final int MAX = 100;
	public static final int RED = Color.rgb(255, 76, 76);
	public static final int ORANGE = Color.rgb(255, 183, 50);
	public static final int YELLOW = Color.rgb(255, 255, 127);
	public static final int LIME = Color.rgb(153, 255, 153);
	public static final int GREEN = Color.rgb(102, 178, 102);

	private final ShapeDrawable bar;

	public StrengthMeter(Context context) {
		this(context, null);
	}

	public StrengthMeter(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs, android.R.attr.progressBarStyleHorizontal);
		RoundRectShape roundRectShape = new RoundRectShape(new float[] {
				10, 10, 10, 10,
				10, 10, 10, 10}, null, null);
		bar = new ShapeDrawable(roundRectShape);
		bar.getPaint().setColor(RED);
		ClipDrawable clip = new ClipDrawable(bar, LEFT & START, HORIZONTAL);
		ShapeDrawable background = new ShapeDrawable();
		Paint p = background.getPaint();
		p.setStyle(FILL);
		p.setColor(getResources().getColor(android.R.color.transparent));
		p.setStyle(STROKE);
		//p.setStrokeWidth(1);
		p.setColor(BLACK);
		Drawable[] layers = new Drawable[] {clip, background};
		setProgressDrawable(new LayerDrawable(layers));
		setIndeterminate(false);
		if (isInEditMode()) setStrength(STRONG);
	}

	@Override
	public int getMax() {
		return MAX;
	}

	public int getColor() {
		return bar.getPaint().getColor();
	}

	public void setStrength(float strength) {
		if (strength < 0 || strength > 1) throw new IllegalArgumentException();
		int colour;
		if (strength < WEAK) colour = RED;
		else if (strength < QUITE_WEAK) colour = ORANGE;
		else if (strength < QUITE_STRONG) colour = YELLOW;
		else if (strength < STRONG) colour = LIME;
		else colour = GREEN;
		bar.getPaint().setColor(colour);
		setProgress((int) (strength * MAX));
	}
}
