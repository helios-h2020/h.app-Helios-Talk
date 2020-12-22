package eu.h2020.helios_social.happ.helios.talk.controller.handler;

public interface ExceptionHandler<E extends Exception> {

	void onException(E exception);

}
