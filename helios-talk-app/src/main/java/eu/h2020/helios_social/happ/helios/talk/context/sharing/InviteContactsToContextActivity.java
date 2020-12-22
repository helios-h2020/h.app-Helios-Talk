package eu.h2020.helios_social.happ.helios.talk.context.sharing;

import android.os.Bundle;

import java.util.Collection;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import eu.h2020.helios_social.happ.helios.talk.activity.ActivityComponent;
import eu.h2020.helios_social.happ.helios.talk.api.nullsafety.MethodsNotNullByDefault;
import eu.h2020.helios_social.happ.helios.talk.api.nullsafety.ParametersNotNullByDefault;
import eu.h2020.helios_social.happ.helios.talk.controller.handler.UiExceptionHandler;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactId;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class InviteContactsToContextActivity
		extends InviteContactActivity {

	private static final Logger LOG =
			Logger.getLogger(InviteContactsToContextActivity.class.getName());

	@Override
	public void injectActivity(ActivityComponent component) {
		component.inject(this);
	}

	@Override
	public void onCreate(@Nullable Bundle bundle) {
		super.onCreate(bundle);

		if (bundle == null) {
			LOG.info("Current Context: " + contextId);
			showInitialFragment(InvitingContactsFragment.newInstance(
					contextId
			));
		}
	}

	@Override
	void invite(Collection<ContactId> contacts) {
		controller.invite(contextId,
				contacts,
				new UiExceptionHandler<DbException>(this) {
					@Override
					public void onExceptionUi(DbException exception) {
						handleDbException(exception);
					}
				});
		onBackPressed();
	}

}
