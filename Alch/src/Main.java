import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.ui.EquipmentSlot;
import org.osbot.rs07.api.ui.MagicSpell;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.ui.Spells;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

@ScriptManifest(author = "Me", name = "Alch", version = 1, logo = "", info = "65k xp/hr")
public class Main extends Script {
	// properties
	private Skill skill = Skill.MAGIC;
	private long lastMovement = 0;
	private long maxIdleTime = 60;
	private MagicSpell spell = Spells.NormalSpells.HIGH_LEVEL_ALCHEMY;
	private String itemName = "bow";
	private String runeName = "nature";
	private String staffName = "Bryophyta's staff";
	private int alchCount = 0;
	private int reloadCount = random(500, 520);

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
		g.drawString("Count -> " + String.valueOf(alchCount), 2, 75);
		g.drawRect(mouse.getPosition().x - 3, mouse.getPosition().y - 3, 6, 6);
	}

	// load
	@Override
	public void onStart() throws InterruptedException {
		lastMovement = System.nanoTime();
		experienceTracker.start(skill);
		tabs.open(Tab.INVENTORY);
		sleep(500);
	}

	// helper
	private void kill() throws InterruptedException {
		stop();
		sleep(2000);
	}

	// loop
	@Override
	@SuppressWarnings("unchecked")
	public int onLoop() throws InterruptedException {
		// environment
		long currentTime = System.nanoTime();
		long secondsSinceLastMovement = (currentTime - lastMovement) / 1000000000;
		boolean lowHp = skills.getDynamic(Skill.HITPOINTS) <= skills.getStatic(Skill.HITPOINTS) * 0.2;
		Item item = inventory.getItem(n -> n != null && n.getName().toLowerCase().contains(itemName));
		Item rune = inventory.getItem(n -> n != null && n.getName().toLowerCase().contains(runeName));
		Item staff = inventory.getItem(n -> n != null && n.getName().contains(staffName));
		Item weapon = equipment.getItemInSlot(EquipmentSlot.WEAPON.slot);

		// state
		boolean shouldAlch = magic.isSpellSelected();
		boolean shouldSelectSpell = !magic.isSpellSelected();
		boolean shouldStop = secondsSinceLastMovement > maxIdleTime || lowHp || item == null;
		boolean shouldUnequipStaff = staff == null && weapon != null && rune != null && alchCount % reloadCount == 0;
		boolean shouldEquipStaff = staff != null && rune != null && weapon == null;

		// action
		if (shouldStop) {
			kill();
		} else if (shouldUnequipStaff) {
			equipment.unequip(EquipmentSlot.WEAPON);
			sleep(random(1000, 1500));
		} else if (shouldEquipStaff) {
			rune.interact();
			staff.interact();
			equipment.equip(EquipmentSlot.WEAPON, staffName);
			alchCount++;
		} else if (shouldAlch) {
			item.interact();
			alchCount++;
			lastMovement = currentTime;
			sleep(2750); // 2600 = 69k
		} else if (shouldSelectSpell) {
			if (!magic.canCast(spell)) {
				kill();
			}
			magic.castSpell(spell);
		}

		// next
		return random(200, 400);
	}
}