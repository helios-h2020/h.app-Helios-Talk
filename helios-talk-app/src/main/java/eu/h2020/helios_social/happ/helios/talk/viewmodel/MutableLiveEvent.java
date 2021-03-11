package eu.h2020.helios_social.happ.helios.talk.viewmodel;

import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.NotNullByDefault;

@NotNullByDefault
public class MutableLiveEvent<T> extends LiveEvent<T> {

	public void postEvent(T value) {
		super.postValue(new ConsumableEvent<>(value));
	}

	public void setEvent(T value) {
		super.setValue(new ConsumableEvent<>(value));
	}
}
