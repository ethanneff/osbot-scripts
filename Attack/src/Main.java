import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.model.GroundItem;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

@ScriptManifest(author = "Me", info = "Curse cows", name = "Attack", version = 0, logo = "")
public class Main extends Script {
	// config
	Skill skill = Skill.DEFENCE;
	long startAmount = 0;
	long experiencePerSpell = 29;
	String npcName = "Cow";
	boolean enableBones = false;
	boolean enableCoin = false;
	boolean enablePickup = true;
	Area combatArea;
	NPC target;

	@Override
	public void onStart() throws InterruptedException {
		experienceTracker.start(skill);
		getTabs().open(Tab.INVENTORY);
		sleep(1000);
	}

	@Override
	public void onExit() {
		log("Stopped");
	}

	public final String formatTime(final long ms) {
		long s = ms / 1000, m = s / 60, h = m / 60;
		s %= 60;
		m %= 60;
		h %= 24;
		return String.format("%02d:%02d:%02d", h, m, s);
	}

	@Override
	public void onPaint(Graphics2D g) {
		g.setColor(new Color(255, 255, 255));
		g.setFont(new Font("Open Sans", Font.PLAIN, 12));
		g.drawString("Curse everything", 2, 230);
		g.drawString("Elapsed time:         \t" + String.valueOf(formatTime(experienceTracker.getElapsed(skill))), 2,
				250);
		g.drawString("Current level:         \t" + String.valueOf(skills.getStatic(skill)) + "\t"
				+ String.valueOf(skills.getExperience(skill)), 2, 270);
		g.drawString("Next level:             \t" + String.valueOf(skills.getStatic(skill) + 1) + "\t"
				+ String.valueOf(skills.experienceToLevel(skill)) + "\t"
				+ String.valueOf(formatTime(experienceTracker.getTimeToLevel(skill))), 2, 290);
		g.drawString("Experience gained: \t" + String.valueOf(experienceTracker.getGainedLevels(skill)) + "\t"
				+ String.valueOf(experienceTracker.getGainedXP(skill)) + "\t"
				+ String.valueOf(experienceTracker.getGainedXPPerHour(skill)), 2, 310);
		g.drawRect(mouse.getPosition().x - 3, mouse.getPosition().y - 3, 6, 6);
	}

	public int nextLoop() {
		return random(100, 300);
	}

	public void preventDoubleClick() throws InterruptedException {
		sleep(random(1000, 1200));
	}

	public void waitForNpcToRespond() throws InterruptedException {
		sleep(random(1750, 2250));
	}

	@Override
	@SuppressWarnings("unchecked")
	public int onLoop() throws InterruptedException {
		// state
		boolean lowHp = skills.getDynamic(Skill.HITPOINTS) < skills.getStatic(Skill.HITPOINTS) / 2;
		Item food = inventory.getItem(n -> n != null && n.hasAction("Drink") || n.hasAction("Eat"));
		Item bone = inventory.getItem(n -> n != null && n.hasAction("Bury"));
		NPC npc = npcs.closest(n -> n != null && n.isAttackable() && !n.isHitBarVisible() && n.getHealthPercent() > 0
				&& !n.isUnderAttack() && n.isAttackable() && !n.getName().contains("Rat")
				&& n.getPosition().distance(myPlayer().getPosition()) <= 10);
		GroundItem ground = getGroundItems()
				.closest(n -> n != null && (n.getPosition().distance(myPlayer().getPosition()) > 1)
						&& (n.getPosition().distance(myPlayer().getPosition()) <= 10)
						&& ((enableCoin && n.getName().contains("Coin")) || n.getName().contains("Big bones")
								|| n.getName().contains("Rune") || n.getName().contains("Iron")
								|| n.getName().contains("Tin") || n.getName().contains("Copper")
								|| n.getName().contains("Cooked") || n.hasAction("Eat", "Drink")));

		// enable running
		if (!settings.isRunning() && settings.getRunEnergy() > random(10, 20)) {
			settings.setRunning(true);
		}

		// low hp logout
		if (lowHp && food == null) {
			stop();
		}

		// low hp eat
		if (lowHp && food != null) {
			food.interact("Eat", "Drink");
			preventDoubleClick();
			return nextLoop();
		}

		// fighting
		if (target != null && target.isInteracting(myPlayer())) {
			return nextLoop();
		}
		target = null;

		// bury
		if (bone != null) {
			bone.interact("Bury");
			preventDoubleClick();
			return nextLoop();
		}

		// pick up
		if (enablePickup && ground != null) {
			ground.interact("Take");
			preventDoubleClick();
			return nextLoop();
		}

		// find npc
		if (npc != null && !npc.isVisible()) {
			camera.toEntity(npc);
		}

		// player busy
		if (myPlayer().isAnimating() || myPlayer().isMoving() || myPlayer().isUnderAttack()) {
			return nextLoop();
		}

		// attack npc
		npc.interact("Attack");
		target = npc;
		waitForNpcToRespond();
		return nextLoop();
	}
}
