package eu.h2020.helios_social.happ.helios.talk.conversation;

import android.app.Application;

import java.util.concurrent.Executor;
import java.util.logging.Logger;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import eu.h2020.helios_social.happ.helios.talk.api.db.DatabaseExecutor;
import eu.h2020.helios_social.happ.helios.talk.api.db.NoSuchContactException;
import eu.h2020.helios_social.happ.helios.talk.api.nullsafety.NotNullByDefault;
import eu.h2020.helios_social.happ.helios.talk.api.settings.Settings;
import eu.h2020.helios_social.happ.helios.talk.api.settings.SettingsManager;
import eu.h2020.helios_social.happ.helios.talk.settings.SettingsFragment;
import eu.h2020.helios_social.happ.helios.talk.util.UiUtils;
import eu.h2020.helios_social.happ.helios.talk.viewmodel.LiveEvent;
import eu.h2020.helios_social.happ.helios.talk.viewmodel.MutableLiveEvent;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.Contact;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactId;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactManager;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.Message;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.MessageHeader;
import eu.h2020.helios_social.modules.groupcommunications.api.messaging.MessagingManager;
import eu.h2020.helios_social.modules.groupcommunications.api.conversation.ConversationManager;
import eu.h2020.helios_social.modules.groupcommunications.api.privateconversation.PrivateMessageFactory;

import static eu.h2020.helios_social.happ.helios.talk.api.util.LogUtils.logDuration;
import static eu.h2020.helios_social.happ.helios.talk.api.util.LogUtils.logException;
import static eu.h2020.helios_social.happ.helios.talk.api.util.LogUtils.now;
import static java.util.Objects.requireNonNull;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;

@NotNullByDefault
public class ConversationViewModel extends AndroidViewModel {

	private static Logger LOG =
			getLogger(ConversationViewModel.class.getName());

	private static final String SHOW_ONBOARDING_IMAGE =
			"showOnboardingImage";

	@DatabaseExecutor
	private final Executor dbExecutor;
	private final MessagingManager messagingManager;
	private final ConversationManager conversationManager;
	private final ContactManager contactManager;
	private final SettingsManager settingsManager;
	private final PrivateMessageFactory privateMessageFactory;

	@Nullable
	private ContactId contactId = null;
	private String contextId = null;
	private String messagingGroupId = null;
	private final MutableLiveData<Contact> contact = new MutableLiveData<>();
	/*private final LiveData<AuthorId> contactAuthorId =
			Transformations.map(contact, c -> c.getAuthor().getId());*/
	private final LiveData<String> contactName =
			Transformations.map(contact, UiUtils::getContactDisplayName);
	private final MutableLiveData<Boolean> imageSupport =
			new MutableLiveData<>();
	private final MutableLiveEvent<Boolean> showImageOnboarding =
			new MutableLiveEvent<>();
	private final MutableLiveEvent<Boolean> showIntroductionOnboarding =
			new MutableLiveEvent<>();
	private final MutableLiveData<Boolean> showIntroductionAction =
			new MutableLiveData<>();
	private final MutableLiveData<Boolean> contactDeleted =
			new MutableLiveData<>();
	private final MutableLiveEvent<MessageHeader> addedHeader =
			new MutableLiveEvent<>();

	@Inject
	ConversationViewModel(Application application,
			@DatabaseExecutor Executor dbExecutor,
			MessagingManager messagingManager,
			ConversationManager conversationManager,
			ContactManager contactManager,
			SettingsManager settingsManager,
			PrivateMessageFactory privateMessageFactory) {
		super(application);
		this.dbExecutor = dbExecutor;
		this.messagingManager = messagingManager;
		this.contactManager = contactManager;
		this.settingsManager = settingsManager;
		this.privateMessageFactory = privateMessageFactory;
		this.conversationManager = conversationManager;
		contactDeleted.setValue(false);
	}

	/**
	 * Setting the {@link ContactId} automatically triggers loading of other
	 * data.
	 */
	void setContactId(ContactId contactId) {
		if (this.contactId == null) {
			this.contactId = contactId;
			loadContact(contactId);
		} else if (!contactId.equals(this.contactId)) {
			throw new IllegalStateException();
		}
	}


	void setContextId(String contextId) {
		if (this.contextId == null) {
			this.contextId = contextId;
		}
	}

	void setGroupId(String groupId) {
		if (this.messagingGroupId == null) {
			this.messagingGroupId = groupId;
		}
	}

	private void loadContact(ContactId contactId) {
		dbExecutor.execute(() -> {
			try {
				long start = now();
				Contact c = contactManager.getContact(contactId);
				contact.postValue(c);
				logDuration(LOG, "Loading contact", start);
				start = now();
				checkFeaturesAndOnboarding(contactId);
				logDuration(LOG, "Checking for image support", start);
			} catch (NoSuchContactException e) {
				contactDeleted.postValue(true);
			} catch (DbException e) {
				logException(LOG, WARNING, e);
			}
		});
	}

	void markMessageRead(String groupId, String messageId) {
		dbExecutor.execute(() -> {
			try {
				long start = now();
				conversationManager.setReadFlag(groupId, messageId, true);
				logDuration(LOG, "Marking read", start);
			} catch (DbException e) {
				logException(LOG, WARNING, e);
			}
		});
	}

	/*void setContactAlias(String alias) {
		dbExecutor.execute(() -> {
			try {
				contactManager.setContactAlias(requireNonNull(contactId),
						alias.isEmpty() ? null : alias);
				loadContact(contactId);
			} catch (DbException e) {
				logException(LOG, WARNING, e);
			}
		});
	}*/

	@UiThread
	void sendMessage(@Nullable String text,
			/*List<AttachmentHeader> headers,*/ long timestamp) {
		// messagingGroupId is loaded with the contact
		requireNonNull(messagingGroupId);
		createMessage(messagingGroupId, text, /*headers, */timestamp
				/*,hasImageSupport*/);
	}

	@UiThread
	void sendVideoCallRequest(long timestamp, String room_id) {
		// messagingGroupId is loaded with the contact

		requireNonNull(messagingGroupId);
		createVideoCallMessage(messagingGroupId, timestamp, room_id);

	}

	@DatabaseExecutor
	private void checkFeaturesAndOnboarding(ContactId c) throws DbException {
		// check if images are supported
		boolean imagesSupported = false;
		imageSupport.postValue(imagesSupported);

		// we only show one onboarding dialog at a time
		Settings settings = settingsManager.getSettings(
				SettingsFragment.SETTINGS_NAMESPACE);
		if (imagesSupported &&
				settings.getBoolean(SHOW_ONBOARDING_IMAGE, true)) {
			onOnboardingShown(SHOW_ONBOARDING_IMAGE);
			showImageOnboarding.postEvent(true);
		}
	}

	@DatabaseExecutor
	private void onOnboardingShown(String key) throws DbException {
		Settings settings = new Settings();
		settings.putBoolean(key, false);
		settingsManager
				.mergeSettings(settings, SettingsFragment.SETTINGS_NAMESPACE);
	}

	private void createMessage(String groupId, @Nullable String text,
			/*List<AttachmentHeader> headers, */long timestamp/*,
			boolean hasImageSupport*/) {
		try {
			Message pm;
			/*if (hasImageSupport) {
				pm = privateMessageFactory.createPrivateMessage(groupId,
						timestamp, text, headers);
			} else {*/
			pm = privateMessageFactory.createTextMessage(
					groupId, timestamp, requireNonNull(text));
			//}
			MessageHeader h = messagingManager.sendPrivateMessage(
					contactId,
					contextId,
					pm
			);
			addedHeader.postEvent(h);
		} catch (DbException e) {
			throw new AssertionError(e);
		}
	}

	private void createVideoCallMessage(String groupId, long timestamp,
			String room_id) {
		try {
			Message pm;
			pm = privateMessageFactory.createVideoCallMessage(groupId,
					timestamp, room_id);

			MessageHeader h = messagingManager.sendPrivateMessage(
					contactId,
					contextId,
					pm
			);
			addedHeader.postEvent(h);
		} catch (DbException e) {
			throw new AssertionError(e);
		}
	}

	/*private void storeVideoCallMessage(Message m) {
		//attachmentCreator.onAttachmentsSent(m.getMessage().getId());
		dbExecutor.execute(() -> {
			try {
				long start = now();
				messagingManager.addVideoCallLocalMessage(m);
				logDuration(LOG, "Storing message", start);
				Message message = m.getMessage();
				PrivateMessageHeader h = new PrivateMessageHeader(
						message.getId(), message.getGroupId(),
						message.getTimestamp(), true, true, false, false, false,
						m.hasText(), m.getAttachmentHeaders(), VIDEOCALL);
				// TODO add text to cache when available here
				addedHeader.postEvent(h);
			} catch (DbException e) {
				logException(LOG, WARNING, e);
			}
		});
	}

	private void storeMessage(PrivateMessage m) {
		attachmentCreator.onAttachmentsSent(m.getMessage().getId());
		dbExecutor.execute(() -> {
			try {
				long start = now();
				messagingManager.addLocalMessage(m);
				logDuration(LOG, "Storing message", start);
				Message message = m.getMessage();
				long msgType =
						m.getAttachmentHeaders().size() == 0 ? PRIVATE_MESSAGE :
								ATTACHMENT;
				PrivateMessageHeader h = new PrivateMessageHeader(
						message.getId(), message.getGroupId(),
						message.getTimestamp(), true, true, false, false, false,
						m.hasText(), m.getAttachmentHeaders(), msgType);
				// TODO add text to cache when available here
				addedHeader.postEvent(h);
			} catch (DbException e) {
				logException(LOG, WARNING, e);
			}
		});
	}*/

	/*public AttachmentRetriever getAttachmentRetriever() {
		return attachmentRetriever;
	}*/

	LiveData<Contact> getContact() {
		return contact;
	}

	/*LiveData<AuthorId> getContactAuthorId() {
		return contactAuthorId;
	}*/

	LiveData<String> getContactDisplayName() {
		return contactName;
	}

	LiveData<Boolean> hasImageSupport() {
		return imageSupport;
	}

	LiveEvent<Boolean> showImageOnboarding() {
		return showImageOnboarding;
	}

	LiveEvent<Boolean> showIntroductionOnboarding() {
		return showIntroductionOnboarding;
	}

	LiveData<Boolean> showIntroductionAction() {
		return showIntroductionAction;
	}

	LiveData<Boolean> isContactDeleted() {
		return contactDeleted;
	}

	LiveEvent<MessageHeader> getAddedPrivateMessage() {
		return addedHeader;
	}

	@UiThread
	void recheckFeaturesAndOnboarding(ContactId contactId) {
		dbExecutor.execute(() -> {
			try {
				checkFeaturesAndOnboarding(contactId);
			} catch (DbException e) {
				logException(LOG, WARNING, e);
			}
		});
	}
}
