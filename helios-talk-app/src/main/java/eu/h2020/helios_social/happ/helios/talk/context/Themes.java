package eu.h2020.helios_social.happ.helios.talk.context;

import android.app.Activity;

import java.util.HashMap;

import eu.h2020.helios_social.happ.helios.talk.R;


public class Themes {

	HashMap<Integer, Integer> themes;

	public Themes(Activity activity) {
		themes = new HashMap();
		themes.put(activity.getColor(R.color.helios_default_context_color),
				R.style.HeliosTheme);
		themes.put(activity.getColor(R.color.helios_context_deepspace_color),
				R.style.HeliosTheme_DeepSpace);
		themes.put(
				activity.getColor(R.color.helios_context_midnightgreen_color),
				R.style.HeliosTheme_MidnightGreen);
		themes.put(activity.getColor(R.color.helios_context_asparagus_color),
				R.style.HeliosTheme_Asparagus);
		themes.put(activity.getColor(R.color.helios_context_forestgreen_color),
				R.style.HeliosTheme_ForestGreen);
		themes.put(activity.getColor(R.color.helios_context_skobeloff_color),
				R.style.HeliosTheme_Skobeloff);
		themes.put(
				activity.getColor(R.color.helios_context_skobelofflight_color),
				R.style.HeliosTheme_Skobelofflight);
		themes.put(activity.getColor(R.color.helios_context_queenblue_color),
				R.style.HeliosTheme_Queenblue);
		themes.put(activity.getColor(R.color.helios_context_darkpurple_color),
				R.style.HeliosTheme_darkblue);
		themes.put(activity.getColor(R.color.helios_context_darkred_color),
				R.style.HeliosTheme_Darkred);
		themes.put(activity.getColor(R.color.helios_context_brownish_color),
				R.style.HeliosTheme_Brownish);
		themes.put(activity.getColor(R.color.helios_context_candypink_color),
				R.style.HeliosTheme_Candypink);
		themes.put(
				activity.getColor(R.color.helios_context_chineseviolet_color),
				R.style.HeliosTheme_Chineseviolet);
		themes.put(activity.getColor(R.color.helios_context_blackshadows_color),
				R.style.HeliosTheme_Blackshadows);
		themes.put(activity.getColor(R.color.helios_context_davysgrey_color),
				R.style.HeliosTheme_Davysgrey);
		themes.put(activity.getColor(R.color.helios_context_purple_color),
				R.style.HeliosTheme_Purple);
		themes.put(
				activity.getColor(R.color.helios_context_intensepurple_color),
				R.style.HeliosTheme_Intensepurple);
		themes.put(activity.getColor(R.color.helios_context_lightpurple_color),
				R.style.HeliosTheme_Lightpurple);
		themes.put(
				activity.getColor(R.color.helios_context_lighterpurple_color),
				R.style.HeliosTheme_Lighterpurple);
		themes.put(activity.getColor(R.color.helios_context_darkblue_color),
				R.style.HeliosTheme_darkblue);
		themes.put(activity.getColor(R.color.helios_context_orange_color),
				R.style.HeliosTheme_Orange);
		themes.put(activity.getColor(R.color.helios_context_smoothorange_color),
				R.style.HeliosTheme_Smoothorange);

	}

	public int[] getColors() {
		int[] colors = new int[themes.size()];
		int i = 0;
		for (Integer c : themes.keySet()) {
			colors[i] = c;
			i++;
		}
		return colors;
	}

	public int getTheme(int color) {
		return themes.get(color);
	}
}
