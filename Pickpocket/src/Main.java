import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

@ScriptManifest(author = "Me", name = "Pickpocket", version = 1, logo = "", info = "")
public class Main extends Script {
	private String target = "farmer";
	private Skill skill = Skill.THIEVING;
	private int distance = 7;

	@Override
	public void onStart() throws InterruptedException {
		experienceTracker.start(skill);
		tabs.open(Tab.INVENTORY);
		sleep(1000);
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
		g.drawString("Elapsed -> " + String.valueOf(formatTime(experienceTracker.getElapsed(skill))), 2, 30);
		g.drawString(String.valueOf(skill) + " -> " + String.valueOf(skills.getStatic(skill)) + " ("
				+ String.valueOf(combat.getCombatLevel()) + ") "
				+ String.valueOf(formatTime(experienceTracker.getTimeToLevel(skill))), 2, 45);
		g.drawString("Gained -> " + String.valueOf(experienceTracker.getGainedLevels(skill)) + " "
				+ String.valueOf(experienceTracker.getGainedXP(skill)) + " "
				+ String.valueOf(experienceTracker.getGainedXPPerHour(skill)), 2, 60);
		g.drawRect(mouse.getPosition().x - 3, mouse.getPosition().y - 3, 6, 6);
	}

	@Override
	@SuppressWarnings("unchecked")
	public int onLoop() throws InterruptedException {
		// properties
		NPC npc = npcs.closest(n -> n != null && n.isAttackable() && !n.isHitBarVisible() && n.getHealthPercent() > 0
				&& !n.isUnderAttack() && n.getName().toLowerCase().contains(target) && map.canReach(n)
				&& n.getPosition().distance(myPlayer().getPosition()) <= distance);
		boolean lowHp = skills.getDynamic(Skill.HITPOINTS) <= skills.getStatic(Skill.HITPOINTS) / 2;
		Item coinPouch = inventory.getItem(n -> n != null && n.getName().toLowerCase().contains("coin pouch"));
		Item food = inventory.getItem(n -> n != null && n.hasAction("Drink") || n.hasAction("Eat"));

		// quit
		if (npc == null || skills.getDynamic(Skill.HITPOINTS) <= skills.getStatic(Skill.HITPOINTS) * 0.2) {
			stop();
			sleep(random(1000, 1500));
			return random(100, 300);
		}

		// enable running
		if (!settings.isRunning() && settings.getRunEnergy() > random(10, 20)) {
			settings.setRunning(true);
		}

		// low hp eat
		if (lowHp && food != null) {
			food.interact("Eat", "Drink");
			sleep(random(1000, 1500));
			return random(100, 300);
		}

		// stun
		if (myPlayer().isAnimating() || myPlayer().isMoving() || myPlayer().isHitBarVisible()
				|| npc.isInteracting(myPlayer())) {
			return random(100, 300);
		}

		// empty inventory
		if (coinPouch != null && coinPouch.getAmount() > random(10, 20)) {
			coinPouch.interact("Open-all");
			sleep(random(1000, 1500));
			return random(100, 300);
		}

		// steal
		npc.interact("Pickpocket");
		sleep(random(1000, 1250));
		return random(100, 300);

	}

}