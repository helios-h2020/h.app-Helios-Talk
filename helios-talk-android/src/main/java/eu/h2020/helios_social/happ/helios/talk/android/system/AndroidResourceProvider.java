package eu.h2020.helios_social.happ.helios.talk.android.system;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;

import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.NotNullByDefault;
import eu.h2020.helios_social.modules.groupcommunications_utils.system.ResourceProvider;

import java.io.InputStream;

import javax.inject.Inject;

@NotNullByDefault
class AndroidResourceProvider implements ResourceProvider {

	private final Context appContext;

	@Inject
	AndroidResourceProvider(Application app) {
		this.appContext = app.getApplicationContext();
	}

	@Override
	public InputStream getResourceInputStream(String name, String extension) {
		Resources res = appContext.getResources();
		// extension is ignored on Android, resources are retrieved without it
		int resId =
				res.getIdentifier(name, "raw", appContext.getPackageName());
		return res.openRawResource(resId);
	}
}
