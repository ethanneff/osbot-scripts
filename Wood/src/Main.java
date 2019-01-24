import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import org.osbot.rs07.api.model.Player;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

@ScriptManifest(author = "Me", name = "Wood", version = 1, logo = "", info = "")
public class Main extends Script {
	// properties
	private Skill skill = Skill.WOODCUTTING;
	private long lastMovement;
	private long maxIdleTime = 20;

	// timing
	private int tick() {
		return random(100, 300);
	}

	private void interact() throws InterruptedException {
		sleep(random(1000, 1500));
	}

	private void kill() throws InterruptedException {
		stop();
		interact();
	}

	// interface
	private String formatTime(long ms) {
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

	// load
	@Override
	public void onStart() throws InterruptedException {
		lastMovement = System.nanoTime();
		experienceTracker.start(skill);
		tabs.open(Tab.INVENTORY);
		interact();
	}

	// loop
	@Override
	@SuppressWarnings("unchecked")
	public int onLoop() throws InterruptedException {

		// environment
		Player mod = players.closest(n -> n != null && n.getName().startsWith("Mod "));
		long currentTime = System.nanoTime();
		long secondsSinceLastMovement = (currentTime - lastMovement) / 1000000000;
		boolean lowHp = skills.getDynamic(Skill.HITPOINTS) <= skills.getStatic(Skill.HITPOINTS) * 0.2;

		// early exit
		if (mod != null || secondsSinceLastMovement > maxIdleTime || lowHp) {
			kill();
		}

		// moving
		if (myPlayer().isMoving() || myPlayer().isAnimating()) {
			lastMovement = currentTime;
			return tick();
		}

		// action
		if (!settings.isRunning() && settings.getRunEnergy() > random(10, 20)) {
			settings.setRunning(true);
		} else if (inventory.isFull()) {
			// drop
			inventory.dropAll(n -> n != null && n.getName().toLowerCase().contains("log"));
		} else if (skills.getSkills().getDynamic(Skill.WOODCUTTING) >= 15) {
			// cut
			objects.closest(n -> n != null && n.getName().toLowerCase().contains("oak")).interact("Chop down");
		} else if (skills.getSkills().getDynamic(Skill.WOODCUTTING) >= 30) {
			// cut
			objects.closest(n -> n != null && n.getName().toLowerCase().contains("willow")).interact("Chop down");
		} else if (skills.getSkills().getDynamic(Skill.WOODCUTTING) >= 45) {
			// cut
			objects.closest(n -> n != null && n.getName().toLowerCase().contains("maple")).interact("Chop down");
		}

		// next
		camera.toTop();
		interact();
		return tick();
	}
}