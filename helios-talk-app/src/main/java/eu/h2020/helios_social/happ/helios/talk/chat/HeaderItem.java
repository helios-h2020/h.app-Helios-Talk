package eu.h2020.helios_social.happ.helios.talk.chat;

public class HeaderItem implements ChatItem {

	private String name;
	private int weight;

	public HeaderItem(String name) {
		this.name = name;
		this.weight = 0;
	}

	@Override
	public String getGroupId() {
		return null;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public int getMessageCount() {
		return 0;
	}

	@Override
	public long getTimestamp() {
		return 4117849822000l + weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}


	@Override
	public int getUnreadCount() {
		return 0;
	}

	public String getName() {
		return name;
	}

	@Override
	public String getLastMessageText() {
		return null;
	}

	@Override
	public void setLastMessageText(String lastMessage) {
	}


	@Override
	public boolean isFavourite() {
		return false;
	}

	@Override
	public void setFavourite(boolean isFavourite) {
	}
}
