package eu.h2020.helios_social.happ.helios.talk.contact.connection;

import android.animation.ObjectAnimator;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.regex.Matcher;

import javax.annotation.Nullable;
import javax.inject.Inject;

import androidx.core.app.ShareCompat.IntentBuilder;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.activity.ActivityComponent;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.MethodsNotNullByDefault;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.ParametersNotNullByDefault;
import eu.h2020.helios_social.happ.helios.talk.fragment.BaseFragment;
import eu.h2020.helios_social.happ.helios.talk.util.UiUtils;

import static android.content.Context.CLIPBOARD_SERVICE;
import static android.widget.Toast.LENGTH_SHORT;
import static eu.h2020.helios_social.modules.groupcommunications_utils.contact.HeliosLinkConstants.LINK_REGEX;
import static java.util.Objects.requireNonNull;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class LinkExchangeFragment extends BaseFragment
		implements OnGlobalLayoutListener {

	private static final String TAG = LinkExchangeFragment.class.getName();

	@Inject
	ViewModelProvider.Factory viewModelFactory;

	private AddContactViewModel viewModel;

	private ClipboardManager clipboard;
	private TextInputLayout linkInputLayout;
	private TextInputEditText linkInput;

	@Override
	public String getUniqueTag() {
		return TAG;
	}

	@Override
	public void injectFragment(ActivityComponent component) {
		component.inject(this);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		if (getActivity() == null || getContext() == null) return null;

		viewModel = ViewModelProviders.of(getActivity(), viewModelFactory)
				.get(AddContactViewModel.class);

		View v = inflater.inflate(R.layout.fragment_link_exchange,
				container, false);

		linkInputLayout = v.findViewById(R.id.linkInputLayout);
		linkInput = v.findViewById(R.id.linkInput);
		if (viewModel.getRemoteHeliosLink() != null) {
			// This can happen if the link was set via an incoming Intent
			linkInput.setText(viewModel.getRemoteHeliosLink());
		}

		clipboard = (ClipboardManager) requireContext()
				.getSystemService(CLIPBOARD_SERVICE);

		Button pasteButton = v.findViewById(R.id.pasteButton);
		pasteButton.setOnClickListener(view -> {
			ClipData clipData = clipboard.getPrimaryClip();
			if (clipData != null && clipData.getItemCount() > 0)
				linkInput.setText(clipData.getItemAt(0).getText());
		});

		UiUtils.observeOnce(viewModel.getHeliosLink(), this,
				this::onHeliosLinkLoaded);

		if (savedInstanceState == null) {
			ScrollView scrollView = (ScrollView) v;
			// we need to wait for views to be laid out to get the heights
			scrollView.getViewTreeObserver().addOnGlobalLayoutListener(this);
		}

		return v;
	}

	@Override
	public void onGlobalLayout() {
		ScrollView scrollView = (ScrollView) requireView();
		View layout = scrollView.getChildAt(0);
		int scrollBy = layout.getHeight() - scrollView.getHeight();
		if (scrollBy > 0) {
			// smoothScrollTo() is too fast due to the transition animation
			ObjectAnimator animator = ObjectAnimator
					.ofInt(scrollView, "scrollY", scrollBy);
			animator.setDuration(1000);
			animator.start();
		}
		layout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
	}

	private void onHeliosLinkLoaded(String link) {
		View v = requireView();

		TextView linkView = v.findViewById(R.id.linkView);
		linkView.setText(link);

		Button copyButton = v.findViewById(R.id.copyButton);
		ClipData clip = ClipData.newPlainText(
				getString(R.string.link_clip_label), link);
		copyButton.setOnClickListener(view -> {
			clipboard.setPrimaryClip(clip);
			Toast.makeText(getContext(), R.string.link_copied_toast,
					LENGTH_SHORT).show();
		});
		copyButton.setEnabled(true);

		Button shareButton = v.findViewById(R.id.shareButton);
		shareButton.setOnClickListener(view ->
				IntentBuilder.from(requireActivity())
						.setText(link)
						.setType("text/plain")
						.startChooser());
		shareButton.setEnabled(true);

		Button continueButton = v.findViewById(R.id.addButton);
		continueButton.setOnClickListener(view -> onContinueButtonClicked());
		continueButton.setEnabled(true);
	}

	/**
	 * Requires {@link AddContactViewModel#getHeliosLink()} to be loaded.
	 */
	@Nullable
	private String getRemoteHeliosLinkOrNull() {
		CharSequence link = linkInput.getText();
		if (link == null || link.length() == 0) {
			linkInputLayout.setError(getString(R.string.missing_link));
			linkInput.requestFocus();
			return null;
		}

		Matcher matcher = LINK_REGEX.matcher(link);
		if (matcher.find()) {
			String linkWithoutSchema = matcher.group(2);
			// Check also if this is our own link. This was loaded already,
			// because it enables the Continue button which is the only caller.
			if (("helios://" + linkWithoutSchema)
					.equals(viewModel.getHeliosLink().getValue())) {
				linkInputLayout.setError(getString(R.string.own_link_error));
				linkInput.requestFocus();
				return null;
			}
			linkInputLayout.setError(null);
			return link.toString();
		}
		linkInputLayout.setError(getString(R.string.invalid_link));
		linkInput.requestFocus();
		return null;
	}

	private void onContinueButtonClicked() {
		String link = getRemoteHeliosLinkOrNull();
		if (link == null) return;  // invalid link

		viewModel.setRemoteHeliosLink(link);
		viewModel.onRemoteLinkEntered();
	}

}
