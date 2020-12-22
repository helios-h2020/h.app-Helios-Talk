package eu.h2020.helios_social.happ.helios.talk.contact.connection;

interface PendingContactListener {

	void onPendingContactItemDelete(PendingContactItem item);

	void onPendingContactItemConfirm(PendingContactItem item);

}
