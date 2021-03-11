package eu.h2020.helios_social.happ.helios.talk.conversation.glide;

import android.graphics.Bitmap;

import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;

public class HeliosTalkImageTransformation extends MultiTransformation<Bitmap> {

	public HeliosTalkImageTransformation(Radii r) {
		super(new CenterCrop(), new CustomCornersTransformation(r));
	}

}
