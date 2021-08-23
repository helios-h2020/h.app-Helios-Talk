package eu.h2020.helios_social.happ.helios.talk.activity;

import android.app.Activity;

import eu.h2020.helios_social.happ.helios.talk.AndroidComponent;
import eu.h2020.helios_social.happ.helios.talk.StartupFailureActivity;
import eu.h2020.helios_social.happ.helios.talk.account.AuthorNameFragment;
import eu.h2020.helios_social.happ.helios.talk.account.DozeFragment;
import eu.h2020.helios_social.happ.helios.talk.account.SetPasswordFragment;
import eu.h2020.helios_social.happ.helios.talk.account.SetupActivity;
import eu.h2020.helios_social.happ.helios.talk.account.UnlockActivity;
import eu.h2020.helios_social.happ.helios.talk.account.onboarding.OnBoardingFragmentStepFive;
import eu.h2020.helios_social.happ.helios.talk.account.onboarding.OnBoardingFragmentStepFour;
import eu.h2020.helios_social.happ.helios.talk.account.onboarding.OnBoardingFragmentStepOne;
import eu.h2020.helios_social.happ.helios.talk.account.onboarding.OnBoardingFragmentStepThree;
import eu.h2020.helios_social.happ.helios.talk.account.onboarding.OnBoardingFragmentStepTwo;
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
import eu.h2020.helios_social.happ.helios.talk.conversation.ConversationModule;
import eu.h2020.helios_social.happ.helios.talk.conversation.ImageActivity;
import eu.h2020.helios_social.happ.helios.talk.conversation.sharecontacts.ShareContactActivity;
import eu.h2020.helios_social.happ.helios.talk.conversation.sharecontacts.ShareContactFragment;
import eu.h2020.helios_social.happ.helios.talk.favourites.FavouritesFragment;
import eu.h2020.helios_social.happ.helios.talk.forum.conversation.ForumConversationActivity;
import eu.h2020.helios_social.happ.helios.talk.forum.conversation.ForumModule;
import eu.h2020.helios_social.happ.helios.talk.forum.creation.CreateForumActivity;
import eu.h2020.helios_social.happ.helios.talk.forum.creation.CreateForumFragment;
import eu.h2020.helios_social.happ.helios.talk.forum.creation.CreateForumModule;
import eu.h2020.helios_social.happ.helios.talk.forum.creation.ForumInviteActivity;
import eu.h2020.helios_social.happ.helios.talk.forum.creation.ForumInviteFragment;
import eu.h2020.helios_social.happ.helios.talk.forum.membership.ForumMembershipListActivity;
import eu.h2020.helios_social.happ.helios.talk.forum.membership.ForumMembershipModule;
import eu.h2020.helios_social.happ.helios.talk.login.ChangePasswordActivity;
import eu.h2020.helios_social.happ.helios.talk.login.OpenDatabaseFragment;
import eu.h2020.helios_social.happ.helios.talk.login.PasswordFragment;
import eu.h2020.helios_social.happ.helios.talk.login.StartupActivity;
import eu.h2020.helios_social.happ.helios.talk.navdrawer.NavDrawerActivity;
import eu.h2020.helios_social.happ.helios.talk.privategroup.conversation.PrivateGroupConversationActivity;
import eu.h2020.helios_social.happ.helios.talk.privategroup.creation.CreateGroupActivity;
import eu.h2020.helios_social.happ.helios.talk.privategroup.creation.CreateGroupFragment;
import eu.h2020.helios_social.happ.helios.talk.privategroup.creation.CreateGroupModule;
import eu.h2020.helios_social.happ.helios.talk.privategroup.creation.GroupInviteActivity;
import eu.h2020.helios_social.happ.helios.talk.privategroup.creation.GroupInviteFragment;
import eu.h2020.helios_social.happ.helios.talk.privategroup.membership.GroupMembershipListActivity;
import eu.h2020.helios_social.happ.helios.talk.privategroup.membership.GroupMembershipModule;
import eu.h2020.helios_social.happ.helios.talk.profile.ContactProfileActivity;
import eu.h2020.helios_social.happ.helios.talk.profile.ProfileActivity;
import eu.h2020.helios_social.happ.helios.talk.search.SearchActivity;
import eu.h2020.helios_social.happ.helios.talk.search.SearchModule;
import eu.h2020.helios_social.happ.helios.talk.settings.SettingsActivity;
import eu.h2020.helios_social.happ.helios.talk.settings.SettingsFragment;
import eu.h2020.helios_social.happ.helios.talk.share.ShareContentActivity;
import eu.h2020.helios_social.happ.helios.talk.share.ShareContentModule;
import eu.h2020.helios_social.happ.helios.talk.shared.SharedModule;
import eu.h2020.helios_social.happ.helios.talk.splash.SplashScreenActivity;

import dagger.Component;

@ActivityScope
@Component(
        modules = {ActivityModule.class,
                CreateContextModule.class,
                CreateGroupModule.class,
                CreateForumModule.class,
                SearchModule.class,
                ForumModule.class,
                ConversationModule.class,
                SharedModule.class,
                ForumMembershipModule.class,
                ShareContentModule.class,
                GroupMembershipModule.class
        },
        dependencies = AndroidComponent.class)
public interface ActivityComponent {

    Activity activity();

    void inject(SplashScreenActivity activity);

    void inject(StartupActivity activity);

    void inject(SetupActivity activity);

    void inject(NavDrawerActivity activity);

    void inject(ConversationActivity activity);

    void inject(ShareContactActivity activity);

    void inject(ImageActivity activity);

    void inject(CreateGroupActivity activity);

    void inject(PrivateGroupConversationActivity activity);

    void inject(GroupInviteActivity activity);

    void inject(CreateForumActivity activity);

    void inject(ForumInviteActivity activity);

    void inject(ForumConversationActivity activity);

    void inject(ForumMembershipListActivity activity);

    void inject(SettingsActivity activity);

    void inject(SearchActivity activity);

    void inject(ChangePasswordActivity activity);

    void inject(StartupFailureActivity activity);

    void inject(UnlockActivity activity);

    void inject(AddContactActivity activity);

    void inject(PendingContactListActivity activity);

    void inject(ShareContentActivity activity);

    // Fragments

    void inject(OnBoardingFragmentStepOne fragment);

    void inject(OnBoardingFragmentStepTwo fragment);

    void inject(OnBoardingFragmentStepThree fragment);

    void inject(OnBoardingFragmentStepFour fragment);

    void inject(OnBoardingFragmentStepFive fragment);

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

    void inject(CreateForumFragment fragment);

    void inject(ForumInviteFragment fragment);

    void inject(ShareContactFragment fragment);

    void inject(LinkExchangeFragment fragment);

    void inject(NicknameFragment fragment);

    void inject(SettingsFragment fragment);

    void inject(CreateContextActivity createContextActivity);

    //void inject(StatsActivity statsActivity);

    void inject(InviteContactActivity inviteContactActivity);

    void inject(InvitingContactsFragment invitingContactsFragment);

    void inject(InvitationListActivity contextInviteListActivity);

    void inject(ProfileActivity profileActivity);

    void inject(ContactProfileActivity contactProfileActivity);

    void inject(GroupMembershipListActivity groupMembershipListActivity);
}
