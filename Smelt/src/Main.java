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

@ScriptManifest(author = "Me", name = "Smelt", version = -15.0, logo = "", info = "Does some noob shit")
public class Main extends Script {
	// properties
	private Skill skill = Skill.CRAFTING;
	private String itemType = "Gold bar";
	private String toolType = "Bracelet mould";
	private int widgetLocation = 446;
	private int widgetIndex = 46;
	private long lastMovement;
	private int maxIdle = 60;

	@Override
	public void onStart() throws InterruptedException {
		// open inventory tab
		lastMovement = System.nanoTime();
		experienceTracker.start(skill);
		tabs.open(Tab.INVENTORY);
	}

	private void interact() throws InterruptedException {
		sleep(random(500, 1500));
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
		Item item = inventory.getItem(n -> n != null && n.getName().contains(itemType));
		Item tool = inventory.getItem(n -> n != null && n.getName().contains(toolType));
		Entity furnace = objects.closest("Furnace");
		Position furnacePosition = new Position(3274, 3186, 0);
		long currentTime = System.nanoTime();
		long seconds = (currentTime - lastMovement) / 1000000000;

		// action
		if (myPlayer().isMoving() || myPlayer().isAnimating() || combat.isFighting()) {
			lastMovement = currentTime;
		} else if (seconds > maxIdle) {
			stop();
			interact();
		} else if (!settings.isRunning() && settings.getRunEnergy() > random(20, 50)) {
			settings.setRunning(true);
			interact();
		} else if (furnace.getPosition().distance(myPlayer()) < 8) {
			if (seconds < 1.5) {
				interact();
			} else if (item == null || tool == null) {
				walking.webWalk(Banks.AL_KHARID);
			} else if (item != null) {
				furnace.interact("Smelt");
				interact();
				widgets.get(widgetLocation, widgetIndex).interact();
				interact();
				mouse.moveOutsideScreen();
			}
		} else if (Banks.AL_KHARID.contains(myPlayer())) {
			bank.open();
			interact();
			bank.depositAllExcept(n -> n != null && n.getName().contains(toolType));
			Item itemBank = bank.getItem(itemType);
			Item toolBank = bank.getItem(toolType);
			if (itemBank == null || (!inventory.contains(toolType) && toolBank == null)) {
				stop();
				interact();
			}
			if (!inventory.contains(toolType)) {
				bank.withdraw(toolType, 1);
			}
			interact();
			bank.withdrawAll(itemType);
			interact();
			walking.webWalk(furnacePosition);
		} else {
			walking.webWalk(Banks.AL_KHARID);
		}

		// next
		return random(200, 500);
	}

}