package eu.h2020.helios_social.happ.helios.talk.fragment;

import javax.inject.Inject;

import eu.h2020.helios_social.happ.helios.talk.api.event.EventBus;
import eu.h2020.helios_social.happ.helios.talk.api.event.EventListener;

public abstract class BaseEventFragment extends BaseFragment implements
		EventListener {

	@Inject
	protected volatile EventBus eventBus;

	@Override
	public void onStart() {
		super.onStart();
		eventBus.addListener(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		eventBus.removeListener(this);
	}
}
