package eu.h2020.helios_social.happ.helios.talk.favourites;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;

import java.util.concurrent.Executor;
import java.util.logging.Logger;

import javax.inject.Inject;

import eu.h2020.helios_social.happ.helios.talk.attachment.AttachmentRetriever;
import eu.h2020.helios_social.happ.helios.talk.conversation.ConversationViewModel;
import eu.h2020.helios_social.modules.groupcommunications.api.conversation.ConversationManager;
import eu.h2020.helios_social.modules.groupcommunications_utils.db.DatabaseExecutor;

import static java.util.logging.Logger.getLogger;

public class FavouritesViewModel extends AndroidViewModel {

    private static Logger LOG =
            getLogger(ConversationViewModel.class.getName());

    @DatabaseExecutor
    private final Executor dbExecutor;
    private final ConversationManager conversationManager;
    private final AttachmentRetriever attachmentRetriever;
    private String contextId = null;

    @Inject
    FavouritesViewModel(Application application,
                        @DatabaseExecutor Executor dbExecutor,
                        ConversationManager conversationManager,
                        AttachmentRetriever attachmentRetriever) {
        super(application);
        this.dbExecutor = dbExecutor;
        this.conversationManager = conversationManager;
        this.attachmentRetriever = attachmentRetriever;
    }


    void setContextId(String contextId) {
        if (this.contextId == null) {
            this.contextId = contextId;
        }
    }

    public AttachmentRetriever getAttachmentRetriever() {
        return attachmentRetriever;
    }

}
