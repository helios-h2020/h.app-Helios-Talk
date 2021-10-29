package eu.h2020.helios_social.happ.helios.talk.context;

import java.util.Collection;

import eu.h2020.helios_social.core.context.Context;
import eu.h2020.helios_social.happ.helios.talk.controller.handler.ExceptionHandler;
import eu.h2020.helios_social.happ.helios.talk.controller.handler.ResultExceptionHandler;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactId;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.FormatException;
import eu.h2020.helios_social.modules.groupcommunications.context.proxy.GeneralContextProxy;
import eu.h2020.helios_social.modules.groupcommunications.context.proxy.LocationContextProxy;
import eu.h2020.helios_social.modules.groupcommunications.context.proxy.SpatioTemporalContext;
public interface ContextController {

	void invite(String contextId, Collection<ContactId> contacts,
			ExceptionHandler<DbException> handler);

	String getCurrentContext();

	Context getContext(String contextId)
			throws DbException, FormatException;

	int getPendingIncomingContextInvitations()
			throws DbException;

	void storeLocationContext(LocationContextProxy locationContext,
							  ResultExceptionHandler<String, DbException> handler);

	void storeSpatioTemporalContext(SpatioTemporalContext spatioTemporalContext,
									ResultExceptionHandler<String, DbException> handler);

	void storeGeneralContext(GeneralContextProxy generalContext,
			ResultExceptionHandler<String, DbException> handler);

	void deleteContext(String contextId,
			ResultExceptionHandler<Void, DbException> handler);

	void setContextPrivateName(String contextId, String name,
							   ResultExceptionHandler<Void, DbException> handler);

	Integer getContextColor(String contextId) throws DbException;

	String getContextPrivateName(String contextId) throws DbException;

	String getContextName(String contextId) throws DbException;

	void setContextName(String contextId, String name,
						  ResultExceptionHandler<Void, DbException> handler) throws DbException;
}
