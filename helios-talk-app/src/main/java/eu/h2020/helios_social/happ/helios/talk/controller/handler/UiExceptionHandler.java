package eu.h2020.helios_social.happ.helios.talk.controller.handler;

import eu.h2020.helios_social.happ.helios.talk.api.nullsafety.NotNullByDefault;
import eu.h2020.helios_social.happ.helios.talk.DestroyableContext;

import javax.annotation.concurrent.Immutable;

import androidx.annotation.UiThread;

@Immutable
@NotNullByDefault
public abstract class UiExceptionHandler<E extends Exception>
		implements ExceptionHandler<E> {

	protected final DestroyableContext listener;

	protected UiExceptionHandler(DestroyableContext listener) {
		this.listener = listener;
	}

	@Override
	public void onException(E exception) {
		listener.runOnUiThreadUnlessDestroyed(() -> onExceptionUi(exception));
	}

	@UiThread
	public abstract void onExceptionUi(E exception);

}
