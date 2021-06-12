package eu.h2020.helios_social.happ.helios.talk.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;


import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.NotNullByDefault;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static androidx.core.content.ContextCompat.getColor;
import static java.util.Objects.requireNonNull;

@NotNullByDefault
public class FileAttachmentPreview extends ConstraintLayout {

    private final TextView filename;

    @Nullable
    private FileAttachmentPreviewListener listener;

    public FileAttachmentPreview(Context context) {
        this(context, null);
    }

    public FileAttachmentPreview(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FileAttachmentPreview(Context context, @Nullable AttributeSet attrs,
                                 int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // inflate layout
        LayoutInflater inflater = (LayoutInflater) requireNonNull(
                context.getSystemService(LAYOUT_INFLATER_SERVICE));
        inflater.inflate(R.layout.file_preview, this, true);

        // set background color
        setBackgroundColor(getColor(context, R.color.card_background));

        // find filename
        filename = findViewById(R.id.fileName);

        // set cancel listener
        findViewById(R.id.fileAttachmentCancelButton).setOnClickListener(view -> {
            if (listener != null) listener.onCancel();
        });
    }

    void setFileAttachmentPreviewListener(FileAttachmentPreviewListener listener) {
        this.listener = listener;
    }

    void showPreview(FileAttachmentPreviewItem item) {
        if (listener == null) throw new IllegalStateException();
        filename.setText(item.getFileName());
        setVisibility(VISIBLE);
    }

    interface FileAttachmentPreviewListener {
        void onCancel();
    }

}
