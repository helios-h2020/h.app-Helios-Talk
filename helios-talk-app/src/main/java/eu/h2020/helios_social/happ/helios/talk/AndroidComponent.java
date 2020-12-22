package eu.h2020.helios_social.happ.helios.talk;

import eu.h2020.helios_social.core.contextualegonetwork.ContextualEgoNetwork;
import eu.h2020.helios_social.happ.account.HeliosTalkAccountModule;

import eu.h2020.helios_social.happ.helios.talk.api.account.AccountManager;
import eu.h2020.helios_social.happ.helios.talk.api.crypto.CryptoExecutor;
import eu.h2020.helios_social.happ.helios.talk.api.crypto.PasswordStrengthEstimator;
import eu.h2020.helios_social.happ.helios.talk.api.db.DatabaseExecutor;
import eu.h2020.helios_social.happ.helios.talk.api.event.EventBus;
import eu.h2020.helios_social.happ.helios.talk.api.identity.IdentityManager;
import eu.h2020.helios_social.happ.helios.talk.api.lifecycle.IoExecutor;
import eu.h2020.helios_social.happ.helios.talk.api.lifecycle.LifecycleManager;
import eu.h2020.helios_social.happ.helios.talk.api.settings.SettingsManager;
import eu.h2020.helios_social.happ.helios.talk.api.system.Clock;
import eu.h2020.helios_social.happ.helios.talk.api.system.LocationUtils;

import eu.h2020.helios_social.happ.android.AndroidNotificationManager;
import eu.h2020.helios_social.happ.android.DozeWatchdog;
import eu.h2020.helios_social.happ.android.LockManager;
import eu.h2020.helios_social.happ.helios.talk.android.HeliosTalkAndroidEagerSingletons;
import eu.h2020.helios_social.happ.helios.talk.android.HeliosTalkAndroidModule;
import eu.h2020.helios_social.happ.helios.talk.android.system.AndroidExecutor;
import eu.h2020.helios_social.happ.helios.talk.db.HeliosTalkDbEagerSingletons;
import eu.h2020.helios_social.happ.helios.talk.db.HeliosTalkDbModule;
import eu.h2020.helios_social.happ.helios.talk.login.SignInReminderReceiver;
import eu.h2020.helios_social.happ.helios.talk.view.EmojiTextInputView;

import java.util.concurrent.Executor;

import javax.inject.Singleton;

import androidx.lifecycle.ViewModelProvider;
import dagger.Component;
import eu.h2020.helios_social.modules.groupcommunications.GroupCommunicationsEagerSingletons;
import eu.h2020.helios_social.modules.groupcommunications.GroupCommunicationsModule;
import eu.h2020.helios_social.modules.groupcommunications.api.CommunicationManager;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.PendingContactFactory;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.connection.ConnectionManager;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactManager;
import eu.h2020.helios_social.modules.groupcommunications.api.context.sharing.ContextInvitationFactory;
import eu.h2020.helios_social.modules.groupcommunications.api.context.sharing.SharingContextManager;
import eu.h2020.helios_social.modules.groupcommunications.api.group.GroupFactory;
import eu.h2020.helios_social.modules.groupcommunications.api.group.GroupManager;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.MessageTracker;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.MessagingManager;
import eu.h2020.helios_social.modules.groupcommunications.api.conversation.ConversationManager;
import eu.h2020.helios_social.modules.groupcommunications.api.privateconversation.PrivateMessageFactory;
import eu.h2020.helios_social.modules.groupcommunications.api.privategroup.GroupMessageFactory;
import eu.h2020.helios_social.modules.groupcommunications.api.privategroup.sharing.GroupInvitationFactory;
import eu.h2020.helios_social.modules.groupcommunications.api.group.sharing.SharingGroupManager;
import eu.h2020.helios_social.modules.groupcommunications.api.profile.ProfileManager;
import eu.h2020.helios_social.modules.groupcommunications.context.ContextFactory;
import eu.h2020.helios_social.modules.groupcommunications.context.ContextManager;

@Singleton
@Component(modules = {
		GroupCommunicationsModule.class,
		HeliosTalkDbModule.class,
		HeliosTalkAndroidModule.class,
		HeliosTalkAccountModule.class,
		AppModule.class,
		//AttachmentModule.class,
		//ProfileModule.class
})
public interface AndroidComponent
		extends HeliosTalkDbEagerSingletons, HeliosTalkAndroidEagerSingletons,
		GroupCommunicationsEagerSingletons, AndroidEagerSingletons {

	// Exposed objects
	@CryptoExecutor
	Executor cryptoExecutor();

	PasswordStrengthEstimator passwordStrengthIndicator();

	@DatabaseExecutor
	Executor databaseExecutor();

	MessageTracker messageTracker();

	LifecycleManager lifecycleManager();

	IdentityManager identityManager();

	EventBus eventBus();

	AndroidNotificationManager androidNotificationManager();

	ContextFactory contextFactory();

	ConnectionManager connectionManager();

	PendingContactFactory pendingContactFactory();

	ContactManager contactManager();

	ConversationManager conversationManager();

	MessagingManager messagingManager();

	PrivateMessageFactory privateMessageFactory();

	GroupManager groupManager();

	GroupInvitationFactory groupInviteFactory();

	/*GroupInvitationManager groupInvitationManager();*/

	GroupFactory groupFactory();

	GroupMessageFactory groupMessageFactory();

	ContextManager contextManager();

	SharingContextManager sharingContextManager();

	ContextInvitationFactory contextInviteFactory();

	ProfileManager profileManager();

	//ForumManager forumManager();

	SharingGroupManager sharingGroupManager();

	SettingsManager settingsManager();

	AndroidExecutor androidExecutor();

	Clock clock();

	DozeWatchdog dozeWatchdog();

	@IoExecutor
	Executor ioExecutor();

	AccountManager accountManager();

	LockManager lockManager();

	LocationUtils locationUtils();

	ViewModelProvider.Factory viewModelFactory();

	ContextualEgoNetwork contextualEgoNetwork();

	//MiningManager miningManager();

	CommunicationManager communicationManager();

	void inject(SignInReminderReceiver heliosTalkService);

	void inject(HeliosTalkService heliosTalkService);

	void inject(NotificationCleanupService notificationCleanupService);

	void inject(EmojiTextInputView textInputView);

}
