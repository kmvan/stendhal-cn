package games.stendhal.server.entity.player;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import marauroa.common.Log4J;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;
import marauroa.server.game.db.DatabaseFactory;

import org.junit.BeforeClass;
import org.junit.Test;

import utilities.PlayerTestHelper;

/**
 * Test the UpdateConverter class.
 *
 * @author Martin Fuchs
 */
public class UpdateConverterTest {

	@BeforeClass
	public static void setupClass() {
		Log4J.init();
		new DatabaseFactory().initializeDatabase();
	}

	/**
	 * Tests for transformString.
	 */
	@Test
	public void testTransformString() {
		assertEquals(null, UpdateConverter.updateItemName(null));
		assertEquals("", UpdateConverter.updateItemName(""));
		assertEquals("chicken", UpdateConverter.updateItemName("chicken"));
		assertEquals("enhanced chainmail", UpdateConverter.updateItemName("chain_armor_+1"));
		assertEquals("enhanced lion shield", UpdateConverter.updateItemName("lion_shield_+1"));
		assertEquals("enhanced lion shield", UpdateConverter.updateItemName("enhanced_lion_shield"));
		assertEquals("black book", UpdateConverter.updateItemName("black_book"));
		assertEquals("black book", UpdateConverter.updateItemName("book_black"));
		assertEquals("black book", UpdateConverter.updateItemName("book black"));
	}

	/**
	 * Tests the killing slot upgrade transformation.
	 */
	@Test
	public void testTransformKillSlot() {
		final Player player = PlayerTestHelper.createPlayer("player");

		RPSlot killSlot = player.getSlot("!kills");
		RPObject killStore = killSlot.getFirst();

		killStore.put("name", "solo");
		killStore.put("monster", "shared");
		killStore.put("cave_rat", "solo");

		final String oldID = killStore.get("id");

		UpdateConverter.updatePlayerRPObject(player);

		killSlot = player.getSlot("!kills");
		killStore = killSlot.getFirst();

		final String idDot = killStore.get(oldID + ".id");
		assertEquals(null, idDot);

		assertTrue(player.hasKilled("name"));
		assertTrue(player.hasKilled("monster"));
		assertFalse(player.hasKilled("cave_rat"));
		assertTrue(player.hasKilled("cave rat"));
	}

	/**
	 * Tests the new killings slot functionality in conjunction with updatePlayerRPObject().
	 */ 
	@Test
	public void testKillingRecords() {
		final Player player = PlayerTestHelper.createPlayer("player");

		player.setSoloKill("name");
		player.setSharedKill("monster");
		player.setSoloKill("cave rat");

		RPSlot killSlot = player.getSlot("!kills");
		RPObject killStore = killSlot.getFirst();
		final String oldID = killStore.get("id");

		UpdateConverter.updatePlayerRPObject(player);

		killSlot = player.getSlot("!kills");
		killStore = killSlot.getFirst();

		final String idDot = killStore.get(oldID + ".id");
		assertEquals(null, idDot);

		assertTrue(player.hasKilled("name"));
		assertTrue(player.hasKilled("monster"));
		assertTrue(player.hasKilled("cave rat"));
	}

	/**
	 * Tests for renameQuest.
	 */
	@Test
	public void testRenameQuest() {
		final Player player = PlayerTestHelper.createPlayer("player");

		// First we use only the old quest slot name.
		player.setQuest("Valo_concoct_potion", "3;mega potion;1200000000000");
		UpdateConverter.updateQuests(player);
		assertNull(player.getQuest("Valo_concoct_potion"));
		assertEquals("3;mega potion;1200000000000", player.getQuest("valo_concoct_potion"));

		// Now add the old name to the existing new one and see if they are accumulated correct.
		player.setQuest("Valo_concoct_potion", "8;mega potion;1300000000000");
		UpdateConverter.updateQuests(player);
		assertNull(player.getQuest("Valo_concoct_potion"));
		assertEquals("11;mega potion;1200000000000", player.getQuest("valo_concoct_potion"));
	}
	
	/**
	 * Tests for renameQuest.
	 */
	@Test
	public void testfixKillQuestsSlots() {
		final Player player = PlayerTestHelper.createPlayer("player");
		player.setQuest("kill_gnomes", "start");
		player.setQuest("clean_storage", "start");
		player.setQuest("kill_dhohr_nuggetcutter", "start");
		UpdateConverter.updateQuests(player);
		assertEquals(player.getQuest("clean_storage"), "start;rat,0,1,0,0,caverat,0,1,0,0,snake,0,1,0,0");
		assertEquals(player.getQuest("kill_gnomes"), "start;gnome,0,1,0,0,infantry gnome,0,1,0,0,cavalryman gnome,0,1,0,0");
		assertEquals(player.getQuest("kill_dhohr_nuggetcutter"), "start;Dhohr Nuggetcutter,0,1,0,0,mountain dwarf,0,1,0,0,mountain elder dwarf,0,1,0,0,mountain hero dwarf,0,1,0,0,mountain leader dwarf,0,1,0,0");		
	}
	
}
