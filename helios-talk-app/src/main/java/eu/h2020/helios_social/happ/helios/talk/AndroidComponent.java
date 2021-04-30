package eu.h2020.helios_social.happ.helios.talk;

import eu.h2020.helios_social.core.contextualegonetwork.ContextualEgoNetwork;
import eu.h2020.helios_social.happ.account.HeliosTalkAccountModule;

import eu.h2020.helios_social.happ.helios.talk.attachment.AttachmentModule;
import eu.h2020.helios_social.modules.groupcommunications.db.GroupCommunicationsDBEagerSingletons;
import eu.h2020.helios_social.modules.groupcommunications.db.GroupCommunicationsDBModule;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.connection.ConnectionRegistry;
import eu.h2020.helios_social.modules.groupcommunications.api.resourcediscovery.queries.QueryManager;
import eu.h2020.helios_social.modules.groupcommunications_utils.account.AccountManager;
import eu.h2020.helios_social.modules.groupcommunications_utils.crypto.CryptoExecutor;
import eu.h2020.helios_social.modules.groupcommunications_utils.crypto.PasswordStrengthEstimator;
import eu.h2020.helios_social.modules.groupcommunications_utils.db.DatabaseExecutor;
import eu.h2020.helios_social.modules.groupcommunications_utils.sync.event.EventBus;
import eu.h2020.helios_social.modules.groupcommunications_utils.identity.IdentityManager;
import eu.h2020.helios_social.modules.groupcommunications_utils.lifecycle.IoExecutor;
import eu.h2020.helios_social.modules.groupcommunications_utils.lifecycle.LifecycleManager;
import eu.h2020.helios_social.modules.groupcommunications_utils.settings.SettingsManager;
import eu.h2020.helios_social.modules.groupcommunications_utils.system.Clock;
import eu.h2020.helios_social.modules.groupcommunications_utils.system.LocationUtils;

import eu.h2020.helios_social.happ.android.AndroidNotificationManager;
import eu.h2020.helios_social.happ.android.DozeWatchdog;
import eu.h2020.helios_social.happ.android.LockManager;
import eu.h2020.helios_social.happ.helios.talk.android.HeliosTalkAndroidEagerSingletons;
import eu.h2020.helios_social.happ.helios.talk.android.HeliosTalkAndroidModule;
import eu.h2020.helios_social.happ.helios.talk.android.system.AndroidExecutor;
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
import eu.h2020.helios_social.modules.groupcommunications.api.forum.ForumManager;
import eu.h2020.helios_social.modules.groupcommunications.api.forum.membership.ForumMembershipManager;
import eu.h2020.helios_social.modules.groupcommunications.api.group.GroupFactory;
import eu.h2020.helios_social.modules.groupcommunications.api.group.GroupManager;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.MessageTracker;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.MessagingManager;
import eu.h2020.helios_social.modules.groupcommunications.api.conversation.ConversationManager;
import eu.h2020.helios_social.modules.groupcommunications.api.mining.MiningManager;
import eu.h2020.helios_social.modules.groupcommunications.api.privateconversation.PrivateMessageFactory;
import eu.h2020.helios_social.modules.groupcommunications.api.privategroup.GroupMessageFactory;
import eu.h2020.helios_social.modules.groupcommunications.api.privategroup.sharing.GroupInvitationFactory;
import eu.h2020.helios_social.modules.groupcommunications.api.group.sharing.SharingGroupManager;
import eu.h2020.helios_social.modules.groupcommunications.api.profile.ProfileManager;
import eu.h2020.helios_social.modules.groupcommunications.api.profile.sharing.SharingProfileManager;
import eu.h2020.helios_social.modules.groupcommunications.context.ContextFactory;
import eu.h2020.helios_social.modules.groupcommunications.context.ContextManager;

@Singleton
@Component(modules = {
        GroupCommunicationsModule.class,
        GroupCommunicationsDBModule.class,
        HeliosTalkAndroidModule.class,
        HeliosTalkAccountModule.class,
        AppModule.class,
        AttachmentModule.class
})
public interface AndroidComponent
        extends GroupCommunicationsDBEagerSingletons, HeliosTalkAndroidEagerSingletons,
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

    ConnectionRegistry connectionRegistry();

    PendingContactFactory pendingContactFactory();

    ContactManager contactManager();

    ConversationManager conversationManager();

    MessagingManager messagingManager();

    PrivateMessageFactory privateMessageFactory();

    GroupManager groupManager();

    ForumMembershipManager forumMembershipManager();

    GroupInvitationFactory groupInviteFactory();

    /*GroupInvitationManager groupInvitationManager();*/

    GroupFactory groupFactory();

    GroupMessageFactory groupMessageFactory();

    ContextManager contextManager();

    SharingContextManager sharingContextManager();

    ContextInvitationFactory contextInviteFactory();

    ProfileManager profileManager();

    SharingProfileManager sharingProfileManager();

    ForumManager forumManager();

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

    MiningManager miningManager();

    CommunicationManager communicationManager();

    QueryManager queryManager();

    void inject(SignInReminderReceiver heliosTalkService);

    void inject(HeliosTalkService heliosTalkService);

    void inject(NotificationCleanupService notificationCleanupService);

    void inject(EmojiTextInputView textInputView);

}
