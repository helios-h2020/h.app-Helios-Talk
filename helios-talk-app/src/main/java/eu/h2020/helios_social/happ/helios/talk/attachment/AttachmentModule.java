package eu.h2020.helios_social.happ.helios.talk.attachment;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AttachmentModule {

    @Provides
    @Singleton
    AttachmentRetriever provideAttachmentRetriever(
            AttachmentRetrieverImpl attachmentRetriever) {
        return attachmentRetriever;
    }
}
