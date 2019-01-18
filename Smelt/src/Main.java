import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.map.constants.Banks;
import org.osbot.rs07.api.model.Entity;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

@ScriptManifest(author = "Noob", name = "Smelt", version = -15.0, logo = "", info = "Does some noob shit")
public class Main extends Script {

	private long lastMovement;
	private Skill skill = Skill.CRAFTING;

	@Override
	public void onStart() throws InterruptedException {
		// open inventory tab
		lastMovement = System.nanoTime();
		experienceTracker.start(skill);
		tabs.open(Tab.INVENTORY);
		sleep(1000);
	}

	public int tick() {
		return random(100, 300);
	}

	public void interact() throws InterruptedException {
		sleep(random(1000, 1500));
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
		Item goldBar = inventory.getItem(n -> n != null && n.getName().toLowerCase().contains("gold bar"));
		Entity furnace = objects.closest("Furnace");
		Position furnacePosition = new Position(3274, 3186, 0);

		// enable running
		if (!settings.isRunning() && settings.getRunEnergy() > random(10, 20)) {
			settings.setRunning(true);
		}

		// action
		long currentTime = System.nanoTime();
		if (myPlayer().isMoving() || myPlayer().isAnimating()) {
			lastMovement = currentTime;
			return tick();
		}

		long seconds = (currentTime - lastMovement) / 1000000000;

		// exit if nothing happens
		if (seconds > 20) {
			stop();
		}

		// prevent spam click
		if (seconds < 1.5) {
			return tick();
		}

		// near furnace
		if (furnace.getPosition().distance(myPlayer()) < 8) {
			// go to bank
			if (goldBar == null) {
				walking.webWalk(Banks.AL_KHARID);
				return tick();
			}

			// need to smelt
			if (goldBar != null) {
				// interact
				furnace.interact("Smelt");
				interact();
				widgets.get(446, 47).interact("Make-All");
				interact();
				return tick();

			}
		} else if (Banks.AL_KHARID.contains(myPlayer())) {
			// go to furnace
			if (inventory.contains("Gold bar")) {
				walking.webWalk(furnacePosition);
				return tick();
			}

			if (!bank.isOpen()) {
				bank.open();
				return tick();
			}

			if (bank.isOpen()) {
				if (bank.getItem("Gold bar").getAmount() <= 0) {
					stop();
				}
				bank.depositAllExcept(n -> n != null && n.getName().toLowerCase().contains("mould"));
				bank.withdraw("Gold bar", random(55, 5555));
				return tick();
			}
		} else {
			// go somewhere if in middle
			walking.webWalk(Banks.AL_KHARID);
		}

		return tick();
	}

}