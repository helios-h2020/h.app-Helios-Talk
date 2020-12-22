package eu.h2020.helios_social.happ.helios.talk.favourites;


import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import eu.h2020.helios_social.happ.helios.talk.api.nullsafety.NotNullByDefault;


@NotThreadSafe
@NotNullByDefault
public class FavItem {

	@Nullable
	private String text;
	private final String messageId;
	private String author;
	private final long time;

	FavItem(String messageId, String author, long timestamp, String text) {
		this.messageId = messageId;
		this.author = author;
		this.time = timestamp;
		this.text = text;
	}

	@Nullable
	public String getText() {
		return text;
	}

	public String getMessageId() {
		return messageId;
	}

	public String getAuthor() {
		return author;
	}

	public long getTime() {
		return time;
	}
}
