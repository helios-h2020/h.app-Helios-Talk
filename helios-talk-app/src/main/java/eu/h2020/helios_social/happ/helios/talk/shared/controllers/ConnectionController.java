package eu.h2020.helios_social.happ.helios.talk.shared.controllers;

import eu.h2020.helios_social.happ.helios.talk.controller.handler.UiResultExceptionHandler;
import eu.h2020.helios_social.modules.groupcommunications.api.peer.PeerInfo;

public interface ConnectionController {

    void sendConnectionRequest(PeerInfo peerInfo, UiResultExceptionHandler<Void, Exception> handler);

    void sendConnectionRequest(String peerId, String alias, UiResultExceptionHandler<Void, Exception> handler);

}
