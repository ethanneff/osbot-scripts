import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

@ScriptManifest(author = "Me", name = "Pickpocket", version = 1, logo = "", info = "")
public class Main extends Script {
	private String npc = "Man";

	@Override
	public void onStart() throws InterruptedException {
		// open inventory tab
		tabs.open(Tab.INVENTORY);
		sleep(1000);
	}

	@Override
	@SuppressWarnings("unchecked")
	public int onLoop() throws InterruptedException {
		// properties
		NPC man = npcs.closest(npc);
		Item coinPouch = inventory.getItem(n -> n != null && n.getName().toLowerCase().contains("coin pouch"));

		// quit
		if (man == null || skills.getDynamic(Skill.HITPOINTS) <= skills.getStatic(Skill.HITPOINTS) * 0.2) {
			stop();
		}

		// stun
		if (myPlayer().isAnimating() || myPlayer().isMoving() || myPlayer().isHitBarVisible()
				|| man.isInteracting(myPlayer())) {
			return random(100, 300);
		}

		// empty inventory
		if (coinPouch != null && coinPouch.getAmount() > random(10, 20)) {
			coinPouch.interact("Open-all");
			sleep(random(1000, 1500));
			return random(100, 300);
		}

		// steal
		man.interact("Pickpocket");
		sleep(random(1000, 1250));
		return random(100, 300);

	}

}