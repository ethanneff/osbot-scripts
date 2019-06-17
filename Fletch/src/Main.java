import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

@ScriptManifest(author = "Me", name = "Fletch", version = 0, logo = "", info = "")
public class Main extends Script {
	// properties
	private Skill skill = Skill.FLETCHING;
	private String toolType = "Knife";
	private long lastMovement;
	private int maxIdle = 60;
	private long duration = 0;

	@Override
	public void onStart() throws InterruptedException {
		lastMovement = System.nanoTime();
		experienceTracker.start(skill);
		tabs.open(Tab.INVENTORY);
		sleep(500);
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
		g.drawString("Elapsed -> " + String.valueOf(formatTime(experienceTracker.getElapsed(skill))), 2, 50);
		g.drawString(String.valueOf(skill) + " -> " + String.valueOf(skills.getStatic(skill)) + " "
				+ String.valueOf(formatTime(experienceTracker.getTimeToLevel(skill))), 2, 65);
		g.drawString("Gained -> " + String.valueOf(experienceTracker.getGainedLevels(skill)) + " "
				+ String.valueOf(experienceTracker.getGainedXP(skill)) + " "
				+ String.valueOf(experienceTracker.getGainedXPPerHour(skill)), 2, 80);
		g.drawRect(mouse.getPosition().x - 3, mouse.getPosition().y - 3, 6, 6);
	}

	private void interact() throws InterruptedException {
		sleep(random(1000, 1500));
	}

	private String getItemType() {
		int level = skills.getStatic(skill);
		if (level < 20) {
			return "Logs";
		} else if (level < 35) {
			return "Oak logs";
		} else if (level < 50) {
			return "Willow logs";
		} else if (level < 65) {
			return "Maple logs";
		} else if (level < 80) {
			return "Yew logs";
		}
		return "Magic logs";
	}

	private int getWidgetIndex() {
		int level = skills.getStatic(skill);
		if (level < 10) {
			return 16;
		} else if (level < 20) {
			return 17;
		} else if (level < 25) {
			return 15;
		} else if (level < 35) {
			return 16;
		} else if (level < 40) {
			return 15;
		} else if (level < 50) {
			return 16;
		} else if (level < 55) {
			return 15;
		} else if (level < 65) {
			return 16;
		} else if (level < 70) {
			return 15;
		} else if (level < 80) {
			return 16;
		} else if (level < 85) {
			return 15;
		} else if (level < 100) {
			return 16;
		}
		return 16;
	}

	@SuppressWarnings("unchecked")
	private Item getItem(String itemType) {
		Item item = inventory.getItem(n -> n != null && n.getName().contains(itemType));
		if (item != null)
			return item;
		return inventory.getItem(n -> n != null && n.getName().toLowerCase().contains("logs"));
	}

	@Override
	@SuppressWarnings("unchecked")
	public int onLoop() throws InterruptedException {
		// properties
		Item tool = inventory.getItem(n -> n != null && n.getName().contains(toolType));
		String itemType = getItemType();
		Item item = getItem(itemType);
		long currentTime = System.nanoTime();
		int widgetIndex = getWidgetIndex();
		long seconds = (currentTime - lastMovement) / 1000000000;
		duration = (currentTime - duration) / 1000000000;

		// action
		if (myPlayer().isMoving() || myPlayer().isAnimating() || combat.isFighting()) {
			lastMovement = currentTime;
		} else if (seconds > maxIdle) {
			stop();
			interact();
		} else if (item == null) {
			bank.open();
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
			bank.withdrawAll(itemType);
			bank.close();
		} else if (item != null && tool != null) {
			tool.interact();
			item.interact();
			interact();
			widgets.get(270, widgetIndex).interact("Make");
			interact();
			mouse.moveOutsideScreen();
		}

		// next
		return random(200, 500);
	}
}