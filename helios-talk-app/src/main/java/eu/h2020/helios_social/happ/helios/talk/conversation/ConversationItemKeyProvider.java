package eu.h2020.helios_social.happ.helios.talk.conversation;

import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemKeyProvider;
import eu.h2020.helios_social.happ.helios.talk.api.nullsafety.NotNullByDefault;

@NotNullByDefault
class ConversationItemKeyProvider extends ItemKeyProvider<String> {

	private final ConversationAdapter adapter;

	protected ConversationItemKeyProvider(ConversationAdapter adapter) {
		super(SCOPE_MAPPED);
		this.adapter = adapter;
	}

	@Nullable
	@Override
	public String getKey(int position) {
		return adapter.getItemKey(position);
	}

	@Override
	public int getPosition(String key) {
		return adapter.getPositionOfKey(key);
	}

}
