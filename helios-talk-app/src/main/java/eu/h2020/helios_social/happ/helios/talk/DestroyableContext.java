package eu.h2020.helios_social.happ.helios.talk;

public interface DestroyableContext {

	void runOnUiThreadUnlessDestroyed(Runnable runnable);
}
