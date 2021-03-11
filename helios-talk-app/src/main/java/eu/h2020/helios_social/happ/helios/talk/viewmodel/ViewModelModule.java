package eu.h2020.helios_social.happ.helios.talk.viewmodel;

import javax.inject.Singleton;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;
import eu.h2020.helios_social.happ.helios.talk.contact.connection.AddContactViewModel;
import eu.h2020.helios_social.happ.helios.talk.contact.connection.PendingContactListViewModel;
import eu.h2020.helios_social.happ.helios.talk.context.invites.InvitationListViewModel;
import eu.h2020.helios_social.happ.helios.talk.conversation.ConversationViewModel;
import eu.h2020.helios_social.happ.helios.talk.favourites.FavouritesViewModel;
import eu.h2020.helios_social.happ.helios.talk.forum.conversation.ForumConversationViewModel;
import eu.h2020.helios_social.happ.helios.talk.privategroup.conversation.PrivateGroupConversationViewModel;

@Module
public abstract class ViewModelModule {

    @Binds
    @Singleton
    abstract ViewModelProvider.Factory bindViewModelFactory(
            ViewModelFactory viewModelFactory);

    @Binds
    @IntoMap
    @ViewModelKey(AddContactViewModel.class)
    abstract ViewModel bindAddContactViewModel(
            AddContactViewModel addContactViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(PendingContactListViewModel.class)
    abstract ViewModel bindPendingContactListViewModel(
            PendingContactListViewModel pendingContactListViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(ConversationViewModel.class)
    abstract ViewModel bindConversationViewModel(
            ConversationViewModel conversationViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(PrivateGroupConversationViewModel.class)
    abstract ViewModel bindGroupConversationViewModel(
            PrivateGroupConversationViewModel groupConversationViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(ForumConversationViewModel.class)
    abstract ViewModel bindForumConversationViewModel(
            ForumConversationViewModel forumConversationViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(InvitationListViewModel.class)
    abstract ViewModel bindContextInviteListViewModel(
            InvitationListViewModel invitationListViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(FavouritesViewModel.class)
    abstract ViewModel bindFavouritesViewModel(
            FavouritesViewModel favouritesViewModel);

}
