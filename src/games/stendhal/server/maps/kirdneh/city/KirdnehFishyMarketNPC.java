package games.stendhal.server.maps.kirdneh.city;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.BuyerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.BuyerBehaviour;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * In Kirdneh open market .
 */
public class KirdnehFishyMarketNPC implements ZoneConfigurator {
    private ShopList shops = SingletonRepository.getShopList();

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	public void configureZone(StendhalRPZone zone, Map<String, String> attributes) {
		buildfishyguy(zone);
	}

	private void buildfishyguy(StendhalRPZone zone) {
		SpeakerNPC fishyguy = new SpeakerNPC("Fishmonger") {

			@Override
			protected void createPath() {
				List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(63, 89));
				nodes.add(new Node(63, 88));				
				nodes.add(new Node(64, 88));
				nodes.add(new Node(64, 87));
				nodes.add(new Node(68, 87));
				nodes.add(new Node(68, 89));
				setPath(new FixedPath(nodes, true));

			}

			@Override
			protected void createDialog() {
				addGreeting("Ahoy, me hearty! Back from yer swashbucklin, ah see.");
				addJob("By the Powers! I be buyin. You be sellin?");
				addReply("yes", "Well, shiver me timbers! Check out that blackboard o'er thar fer me prices an' what i be buyin");
				addReply("aye", "Well, shiver me timbers! Check out that blackboard o'er thar fer me prices an' what i be buyin");
				addReply("no", "You lily-livered scallywag! Why ye be wastin me time?");
				addHelp("An' just what do ya think a buccanneer such as meself could possibly help ye with?");
				new BuyerAdder().add(this, new BuyerBehaviour(shops.get("buyfishes")), true);
				addOffer("Check out that thar blackboard fer how many dubloons I be givin.");
				addQuest("Ye don't ha'e the guts ta do whut I need done.");
				addGoodbye("Arrgh, avast an' be gone with ye!");

			}
		};

		fishyguy.setEntityClass("sailor1npc");
		fishyguy.setPosition(63, 89);
		fishyguy.initHP(100);
		zone.add(fishyguy);
	}
}
