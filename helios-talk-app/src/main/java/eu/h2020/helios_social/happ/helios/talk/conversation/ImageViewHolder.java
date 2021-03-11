package eu.h2020.helios_social.happ.helios.talk.conversation;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy;

import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.attachment.AttachmentItem;
import eu.h2020.helios_social.happ.helios.talk.conversation.glide.GlideApp;
import eu.h2020.helios_social.happ.helios.talk.conversation.glide.HeliosTalkImageTransformation;
import eu.h2020.helios_social.happ.helios.talk.conversation.glide.Radii;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.NotNullByDefault;

import static android.os.Build.VERSION.SDK_INT;
import static com.bumptech.glide.load.engine.DiskCacheStrategy.NONE;
import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

@NotNullByDefault
public
class ImageViewHolder extends RecyclerView.ViewHolder {

    @DrawableRes
    private static final int ERROR_RES = R.drawable.ic_image_broken;

    protected final ImageView imageView;
    private final int imageSize;

    public ImageViewHolder(View v, int imageSize) {
        super(v);
        imageView = v.findViewById(R.id.imageView);
        this.imageSize = imageSize;
    }

    public void bind(AttachmentItem attachment, Radii r, boolean single,
                     boolean needsStretch) {
        if (attachment == null) {
            GlideApp.with(imageView)
                    .clear(imageView);
            imageView.setImageResource(ERROR_RES);
        } else {
            setImageViewDimensions(attachment, single, needsStretch);
            loadImage(attachment, r);
            if (SDK_INT >= 21) {
                imageView.setTransitionName(attachment.getTransitionName());
            }
        }
    }

    private void setImageViewDimensions(AttachmentItem a, boolean single,
                                        boolean needsStretch) {
        StaggeredGridLayoutManager.LayoutParams params = (StaggeredGridLayoutManager.LayoutParams) imageView.getLayoutParams();
        int width = needsStretch ? imageSize * 2 : imageSize;
        params.width = width;
        params.height = imageSize;
        params.setFullSpan(needsStretch);
        imageView.setLayoutParams(params);
    }

    private void loadImage(AttachmentItem a, Radii r) {
        Transformation<Bitmap> transformation = new HeliosTalkImageTransformation(r);
        GlideApp.with(imageView)
                .load(a.getUri())
                .diskCacheStrategy(NONE)
                .error(ERROR_RES)
                .transform(transformation)
                .transition(withCrossFade())
                .into(imageView)
                .waitForLayout();
    }

}
