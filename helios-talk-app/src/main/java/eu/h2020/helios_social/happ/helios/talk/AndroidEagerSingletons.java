package eu.h2020.helios_social.happ.helios.talk;

public interface AndroidEagerSingletons {

	void inject(AppModule.EagerSingletons init);

	class Helper {

		public static void injectEagerSingletons(AndroidEagerSingletons c) {
			c.inject(new AppModule.EagerSingletons());
		}
	}
}
