package eu.h2020.helios_social.happ.helios.talk.profile;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Inject;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import eu.h2020.helios_social.core.contextualegonetwork.ContextualEgoNetwork;
import eu.h2020.helios_social.happ.helios.talk.R;
import eu.h2020.helios_social.happ.helios.talk.activity.ActivityComponent;
import eu.h2020.helios_social.happ.helios.talk.activity.HeliosTalkActivity;
import eu.h2020.helios_social.happ.helios.talk.api.identity.IdentityManager;
import eu.h2020.helios_social.happ.helios.talk.api.nullsafety.MethodsNotNullByDefault;
import eu.h2020.helios_social.happ.helios.talk.api.nullsafety.ParametersNotNullByDefault;
import eu.h2020.helios_social.modules.groupcommunications.api.exception.DbException;
import eu.h2020.helios_social.modules.groupcommunications.api.profile.Profile;
import eu.h2020.helios_social.modules.groupcommunications.api.profile.ProfileBuilder;
import eu.h2020.helios_social.modules.groupcommunications.api.profile.ProfileManager;


@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class ProfileActivity extends HeliosTalkActivity {

    private static final Logger LOG =
            Logger.getLogger(ProfileActivity.class.getName());
    private static final int SELFIE = 0;
    private static final int PICK_IMAGE = 1;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Inject
    volatile ProfileManager profileManager;
    @Inject
    volatile IdentityManager identityManager;
    @Inject
    volatile ContextualEgoNetwork egoNetwork;

    private ImageView avatar;
    private EditText nickname;
    private EditText fullname;
    private Spinner gender;
    private Spinner country;
    private EditText work;
    private EditText uni;
    private TagEditText interests;
    private EditText quote;
    private FloatingActionButton button;
    private Uri uri;
    private Profile p;

    @Override
    public void injectActivity(ActivityComponent component) {
        component.inject(this);
    }

    @Override
    public void onCreate(@Nullable Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_profile);

        avatar = findViewById(R.id.avatarView);
        nickname = findViewById(R.id.user_nickaname);
        nickname.setText(identityManager.getIdentity().getAlias());
        fullname = findViewById(R.id.user_fullname);
        gender = findViewById(R.id.gender);
        country = findViewById(R.id.country);
        work = findViewById(R.id.work);
        uni = findViewById(R.id.university);
        interests = findViewById(R.id.interests);
        quote = findViewById(R.id.quote);
        button = findViewById(R.id.addNewProfilePicture);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verifyStoragePermissions(ProfileActivity.this);
                selectImage(ProfileActivity.this);
            }
        });
        loadProfile();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case 0:
                    if (resultCode == RESULT_OK && data != null) {
                        Bitmap selectedImage =
                                (Bitmap) data.getExtras().get("data");
                        ByteArrayOutputStream stream =
                                new ByteArrayOutputStream();
                        selectedImage.compress(Bitmap.CompressFormat.JPEG, 100,
                                stream);
                    }

                    break;
                case 1:
                    if (resultCode == RESULT_OK && data != null) {
                        uri = data.getData();
                    }
                    break;
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        loadProfile();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProfile();
    }

    public void loadProfile() {
        try {
            String contextId =
                    egoNetwork.getCurrentContext().getData().toString()
                            .split("%")[1];
            if (profileManager.containsProfile(contextId)) {
                p = profileManager.getProfile(contextId);
                fullname.setText(p.getFullname());
                gender.setSelection(p.getGender());
                country.setSelection(p.getCountry());
                uni.setText(p.getUniversity());
                work.setText(p.getWork());
                interests.setText(p.getInterests());
                if (interests.getText().length() > 0) {
                    interests.format();
                }
                quote.setText(p.getQuote());
                if (uri == null && p.getProfilePic() == null) {
                    avatar.setImageResource(R.drawable.ic_person_big);
                } else if (uri == null && p.getProfilePic() != null) {
                    avatar.setImageBitmap(BitmapFactory
                            .decodeByteArray(p.getProfilePic(), 0,
                                    p.getProfilePic().length));
                } else {
                    Bitmap bitmap = null;
                    if (android.os.Build.VERSION.SDK_INT >=
                            android.os.Build.VERSION_CODES.FROYO) {
                        bitmap = ThumbnailUtils
                                .extractThumbnail(BitmapFactory.decodeStream(
                                        getContentResolver()
                                                .openInputStream(uri)),
                                        200,
                                        200);
                        avatar.setImageBitmap(bitmap);
                    }
                }
            } else {
                avatar.setImageResource(R.drawable.ic_person_big);
            }
        } catch (DbException | FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.profile_actions, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.save_profile:
                try {
                    byte[] profilePic = null;
                    if (uri != null) {
                        Bitmap bitmap = ThumbnailUtils
                                .extractThumbnail(BitmapFactory.decodeStream(
                                        getContentResolver()
                                                .openInputStream(uri)),
                                        200, 200);
                        ByteArrayOutputStream stream =
                                new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 200,
                                stream);
                        profilePic = stream.toByteArray();
                    }
                    String contextId =
                            egoNetwork.getCurrentContext().getData().toString()
                                    .split("%")[1];
                    Profile p = new ProfileBuilder(contextId)
                            .setAlias(nickname.getText().toString())
                            .setFullname(fullname.getText().toString())
                            .setCountry(country.getSelectedItemPosition())
                            .setGender(gender.getSelectedItemPosition())
                            .setUniversity(uni.getText().toString())
                            .setWork(work.getText().toString())
                            .setInterests(interests.getText().toString())
                            .setQuote(quote.getText().toString())
                            .setProfilePicture(profilePic)
                            .build();

                    if (profileManager.containsProfile(p.getContextId())) {
                        profileManager.updateProfile(p);
                    } else {
                        profileManager.addProfile(p);
                    }
                } catch (DbException e) {
                    e.printStackTrace();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void selectImage(Context context) {
        final CharSequence[] options =
                {/*"Take Photo",*/ "Choose from Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose your profile picture");

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int item) {

				/*if (options[item].equals("Take Photo")) {
					Intent takePicture = new Intent(
							android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
					startActivityForResult(takePicture, 0);

				} else*/
                if (options[item].equals("Choose from Gallery")) {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    pickPhoto.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivityForResult(pickPhoto,
                            1);//one can be replaced with any action code

                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("profilePic", uri);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        LOG.info("onRestoreState");
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("profilePic")) {
                uri = savedInstanceState.getParcelable("profilePic");
            }
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

}
