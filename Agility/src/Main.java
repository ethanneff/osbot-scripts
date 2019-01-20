import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.Entity;
import org.osbot.rs07.api.model.GroundItem;
import org.osbot.rs07.api.model.Player;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

@ScriptManifest(author = "Me", name = "Agility", version = -15.0, logo = "", info = "Does some noob shit")
public class Main extends Script {
	// properties
	private Skill skill = Skill.AGILITY;
	private long lastMovement;

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

	private final String formatTime(final long ms) {
		long s = ms / 1000, m = s / 60, h = m / 60;
		s %= 60;
		m %= 60;
		h %= 24;
		return String.format("%02d:%02d:%02d", h, m, s);
	}

	private boolean nearPosition(Position to, Position from) {
		return to.distance(from) < 6;
	}

	@Override
	public void onStart() throws InterruptedException {
		lastMovement = System.nanoTime();
		experienceTracker.start(skill);
		tabs.open(Tab.INVENTORY);
		interact();
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

		// draynor spots
		Position p0 = new Position(3105, 3278, 0);
		Position p1 = new Position(3102, 3279, 3);
		Position p2 = new Position(3090, 3276, 3);
		Position p3 = new Position(3092, 3266, 3);
		Position p4 = new Position(3088, 3261, 3);
		Position p5 = new Position(3088, 3255, 3);
		Position p6 = new Position(3096, 3256, 3);
		Position current = myPlayer().getPosition();
		Entity o1 = objects.closest("Rough wall");
		Entity o2 = objects.closest("Tightrope");
		Entity o3 = objects.closest("Narrow wall");
		Entity o4 = objects.closest("Wall");
		Entity o5 = objects.closest("Gap");
		Entity o6 = objects.closest("Crate");

		long currentTime = System.nanoTime();
		Player mod = players.closest(n -> n != null && n.getName().startsWith("Mod "));
		long seconds = (currentTime - lastMovement) / 1000000000;
		boolean playerFell = current.getZ() == 0;
		GroundItem ground = getGroundItems()
				.closest(n -> n != null && map.canReach(n) && n.getName().toLowerCase().contains("mark of grace"));

		// exit if mod nearby
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

		// agility
		if (!settings.isRunning() && settings.getRunEnergy() > random(10, 20)) {
			// enable running
			settings.setRunning(true);
		} else if (ground != null) {
			// mark of grace
			ground.interact("Take");
		} else if (myPlayer().isMoving() || myPlayer().isAnimating()) {
			// moving
			lastMovement = currentTime;
		} else if (playerFell && o1 != null) {
			o1.interact("Climb");
		} else if (playerFell && o1 == null) {
			walking.webWalk(p0);
		} else if (nearPosition(current, p1) && o2 != null) {
			o2.interact("Cross");
		} else if (nearPosition(current, p2) && o2 != null) {
			o2.interact("Cross");
		} else if (nearPosition(current, p3) && o3 != null) {
			o3.interact("Balance");
		} else if (nearPosition(current, p4) && o4 != null) {
			o4.interact("Jump-up");
		} else if (nearPosition(current, p5) && o5 != null) {
			o5.interact("Jump");
		} else if (nearPosition(current, p6) && o6 != null) {
			o6.interact("Climb-down");
		}

		interact();
		return tick();
	}

}