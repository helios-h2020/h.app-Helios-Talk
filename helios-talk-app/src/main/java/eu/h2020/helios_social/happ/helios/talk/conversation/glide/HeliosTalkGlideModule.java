package eu.h2020.helios_social.happ.helios.talk.conversation.glide;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;


import eu.h2020.helios_social.happ.helios.talk.TestingConstants;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.NotNullByDefault;

import static android.util.Log.DEBUG;
import static android.util.Log.WARN;

@GlideModule
@NotNullByDefault
public final class HeliosTalkGlideModule extends AppGlideModule {

	@Override
	public void applyOptions(Context context, GlideBuilder builder) {
		builder.setLogLevel(TestingConstants.IS_DEBUG_BUILD ? DEBUG : WARN);
	}

	@Override
	public boolean isManifestParsingEnabled() {
		return false;
	}

}