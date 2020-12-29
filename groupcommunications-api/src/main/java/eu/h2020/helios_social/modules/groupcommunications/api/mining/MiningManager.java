package eu.h2020.helios_social.modules.groupcommunications.api.mining;

import java.util.HashMap;

import eu.h2020.helios_social.core.contextualegonetwork.Node;
import eu.h2020.helios_social.modules.socialgraphmining.SocialGraphMiner;

public interface MiningManager {

    SocialGraphMiner getSocialGraphMiner();

    HashMap<Node, Double> getNextInteractionRecommendations(String egoContextId);
}
