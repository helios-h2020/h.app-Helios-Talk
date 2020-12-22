package eu.h2020.helios_social.happ.helios.talk.activity;

import android.app.Activity;

import eu.h2020.helios_social.happ.helios.talk.AndroidComponent;
import eu.h2020.helios_social.happ.helios.talk.StartupFailureActivity;
import eu.h2020.helios_social.happ.helios.talk.account.AuthorNameFragment;
import eu.h2020.helios_social.happ.helios.talk.account.DozeFragment;
import eu.h2020.helios_social.happ.helios.talk.account.SetPasswordFragment;
import eu.h2020.helios_social.happ.helios.talk.account.SetupActivity;
import eu.h2020.helios_social.happ.helios.talk.account.UnlockActivity;
import eu.h2020.helios_social.happ.helios.talk.chat.ChatListFragment;
import eu.h2020.helios_social.happ.helios.talk.contact.ContactListFragment;
import eu.h2020.helios_social.happ.helios.talk.contact.connection.AddContactActivity;
import eu.h2020.helios_social.happ.helios.talk.contact.connection.LinkExchangeFragment;
import eu.h2020.helios_social.happ.helios.talk.contact.connection.NicknameFragment;
import eu.h2020.helios_social.happ.helios.talk.contact.connection.PendingContactListActivity;
import eu.h2020.helios_social.happ.helios.talk.context.CreateContextActivity;
import eu.h2020.helios_social.happ.helios.talk.context.CreateContextModule;
import eu.h2020.helios_social.happ.helios.talk.context.invites.InvitationListActivity;
import eu.h2020.helios_social.happ.helios.talk.context.sharing.InviteContactActivity;
import eu.h2020.helios_social.happ.helios.talk.context.sharing.InvitingContactsFragment;
import eu.h2020.helios_social.happ.helios.talk.conversation.ConversationActivity;
import eu.h2020.helios_social.happ.helios.talk.favourites.FavouritesFragment;
import eu.h2020.helios_social.happ.helios.talk.login.ChangePasswordActivity;
import eu.h2020.helios_social.happ.helios.talk.login.OpenDatabaseFragment;
import eu.h2020.helios_social.happ.helios.talk.login.PasswordFragment;
import eu.h2020.helios_social.happ.helios.talk.login.StartupActivity;
import eu.h2020.helios_social.happ.helios.talk.navdrawer.NavDrawerActivity;
import eu.h2020.helios_social.happ.helios.talk.privategroup.conversation.GroupConversationActivity;
import eu.h2020.helios_social.happ.helios.talk.privategroup.creation.CreateGroupActivity;
import eu.h2020.helios_social.happ.helios.talk.privategroup.creation.CreateGroupFragment;
import eu.h2020.helios_social.happ.helios.talk.privategroup.creation.CreateGroupModule;
import eu.h2020.helios_social.happ.helios.talk.privategroup.creation.GroupInviteActivity;
import eu.h2020.helios_social.happ.helios.talk.privategroup.creation.GroupInviteFragment;
import eu.h2020.helios_social.happ.helios.talk.profile.ProfileActivity;
import eu.h2020.helios_social.happ.helios.talk.settings.SettingsActivity;
import eu.h2020.helios_social.happ.helios.talk.settings.SettingsFragment;
import eu.h2020.helios_social.happ.helios.talk.splash.SplashScreenActivity;

import dagger.Component;

@ActivityScope
@Component(
		modules = {ActivityModule.class,
				CreateContextModule.class,
				CreateGroupModule.class,},
		dependencies = AndroidComponent.class)
public interface ActivityComponent {

	Activity activity();

	void inject(SplashScreenActivity activity);

	void inject(StartupActivity activity);

	void inject(SetupActivity activity);

	void inject(NavDrawerActivity activity);

/*	void inject(ContactExchangeActivity activity);

	void inject(KeyAgreementActivity activity);*/

	void inject(ConversationActivity activity);

	/*void inject(ImageActivity activity);

	void inject(ForumInvitationActivity activity);*/

	void inject(CreateGroupActivity activity);

	void inject(GroupConversationActivity activity);

	void inject(GroupInviteActivity activity);

	/*void inject(GroupInvitationActivity activity);

	void inject(GroupMemberListActivity activity);

	void inject(RevealContactsActivity activity);

	void inject(CreateForumActivity activity);

	void inject(ShareForumActivity activity);

	void inject(ForumSharingStatusActivity activity);

	void inject(ForumActivity activity);*/

	void inject(SettingsActivity activity);

	void inject(ChangePasswordActivity activity);

	void inject(StartupFailureActivity activity);

	void inject(UnlockActivity activity);

	void inject(AddContactActivity activity);

	void inject(PendingContactListActivity activity);

	// Fragments

	void inject(AuthorNameFragment fragment);

	void inject(SetPasswordFragment fragment);

	void inject(DozeFragment fragment);

	void inject(PasswordFragment imageFragment);

	void inject(OpenDatabaseFragment activity);

	void inject(ContactListFragment fragment);

	void inject(ChatListFragment fragment);

	void inject(FavouritesFragment fragment);

	void inject(CreateGroupFragment fragment);

	void inject(GroupInviteFragment fragment);

	/*void inject(RevealContactsFragment activity);

	void inject(KeyAgreementFragment fragment);*/

	void inject(LinkExchangeFragment fragment);

	void inject(NicknameFragment fragment);

	//void inject(ShareForumFragment fragment);

	void inject(SettingsFragment fragment);

	/*void inject(ContactExchangeErrorFragment fragment);

	void inject(AliasDialogFragment aliasDialogFragment);

	void inject(ImageFragment imageFragment);*/

	void inject(CreateContextActivity createContextActivity);

	//void inject(StatsActivity statsActivity);

	void inject(InviteContactActivity inviteContactActivity);

	void inject(InvitingContactsFragment invitingContactsFragment);

	void inject(InvitationListActivity contextInviteListActivity);

	void inject(ProfileActivity profileActivity);

	/*void inject(ContactProfileActivity contactProfileActivity);*/
}
