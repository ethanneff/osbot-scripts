import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.model.Player;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

@ScriptManifest(author = "Me", name = "Fletch", version = -15.0, logo = "", info = "Does some noob shit")
public class Main extends Script {
	// properties
	private Skill skill = Skill.FLETCHING;
	private String logType = "Willow logs";
	private String knifeType = "Knife";
	private long lastMovement;

	@Override
	public void onStart() throws InterruptedException {
		lastMovement = System.nanoTime();
		experienceTracker.start(skill);
		tabs.open(Tab.INVENTORY);
		interact();
	}

	private int tick() {
		return random(100, 300);
	}

	private void interact() throws InterruptedException {
		sleep(random(1000, 1500));
	}

	private void selectMenu(Item knife, Item log, int index) throws InterruptedException {
		knife.interact("Use");
		log.interact();
		interact();
		widgets.get(270, index).interact("Make");
		interact();
	}

	private void kill() throws InterruptedException {
		stop();
		interact();
	}

	private final String formatTime(final long ms) {
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
		g.drawString(String.valueOf(skill) + " -> " + String.valueOf(skills.getStatic(skill)) + " "
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
		Item knife = inventory.getItem(n -> n != null && n.getName().contains(knifeType));
		Item log = inventory.getItem(n -> n != null && n.getName().contains(logType));
		long currentTime = System.nanoTime();
		Player mod = players.closest(n -> n != null && n.getName().startsWith("Mod "));
		long seconds = (currentTime - lastMovement) / 1000000000;

		// action
		if (myPlayer().isMoving() || myPlayer().isAnimating()) {
			lastMovement = currentTime;
			return tick();
		}

		// exit if mod
		if (mod != null) {
			kill();
		}

		// exit if nothing happens
		if (seconds > 20) {
			kill();
		}

		// prevent multiple clicks
		if (seconds < 1) {
			return tick();
		}

		// make
		if (log != null && knife != null) {
			selectMenu(knife, log, 16);
			if (!myPlayer().isAnimating()) {
				selectMenu(knife, log, 15);
			}
			return tick();
		}

		// bank
		bank.open();
		if (bank.getItem(logType).getAmount() <= 0) {
			kill();
		}
		bank.depositAllExcept(n -> n != null && n.getName().contains(knifeType));
		if (!inventory.contains(knifeType)) {
			bank.withdraw(knifeType, 1);
		}
		bank.withdrawAll(logType);
		bank.close();
		interact();
		return tick();
	}

}