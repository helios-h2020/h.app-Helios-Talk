package eu.h2020.helios_social.happ.helios.talk.context;

import java.util.Collection;

import eu.h2020.helios_social.core.context.Context;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;
import eu.h2020.helios_social.happ.helios.talk.controller.handler.ExceptionHandler;
import eu.h2020.helios_social.happ.helios.talk.controller.handler.ResultExceptionHandler;
import eu.h2020.helios_social.modules.groupcommunications.api.contact.ContactId;
import eu.h2020.helios_social.modules.groupcommunications.api.context.DBContext;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.FormatException;
import eu.h2020.helios_social.modules.groupcommunications.context.proxy.GeneralContextProxy;
import eu.h2020.helios_social.modules.groupcommunications.context.proxy.LocationContextProxy;

public interface ContextController {

	void invite(String contextId, Collection<ContactId> contacts,
			ExceptionHandler<DbException> handler);

	String getCurrentContext();

	Context getContext(String contextId)
			throws DbException, FormatException;

	void storeLocationContext(LocationContextProxy locationContext,
			ResultExceptionHandler<String, DbException> handler);

	void storeGeneralContext(GeneralContextProxy generalContext,
			ResultExceptionHandler<String, DbException> handler);

	void deleteContext(String contextId,
			ResultExceptionHandler<String, DbException> handler);

	Integer getContextColor(String contextId) throws DbException;
}
