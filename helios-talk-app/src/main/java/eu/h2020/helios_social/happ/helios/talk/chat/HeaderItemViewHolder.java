package eu.h2020.helios_social.happ.helios.talk.chat;

import android.content.Context;
import android.view.View;

import com.vanniktech.emoji.EmojiTextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import eu.h2020.helios_social.happ.helios.talk.R;

public class HeaderItemViewHolder extends RecyclerView.ViewHolder {

	private EmojiTextView header;

	public HeaderItemViewHolder(
			@NonNull View v) {
		super(v);
		header = v.findViewById(R.id.title);
	}

	protected void bind(Context ctx, ChatItem item) {
		header.setText(item.getName());
	}
}
