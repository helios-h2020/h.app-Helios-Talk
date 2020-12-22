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
import eu.h2020.helios_social.happ.helios.talk.privategroup.conversation.GroupConversationViewModel;

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
	@ViewModelKey(GroupConversationViewModel.class)
	abstract ViewModel bindGroupConversationViewModel(
			GroupConversationViewModel groupConversationViewModel);

	@Binds
	@IntoMap
	@ViewModelKey(InvitationListViewModel.class)
	abstract ViewModel bindContextInviteListViewModel(
			InvitationListViewModel invitationListViewModel);

}
