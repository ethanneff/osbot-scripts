
// TODO: still click on doors
// TODO: does not capture change in attack
// TODO: still attacks multiple enemies if already attacked
// TODO: lowhp causes infinite loop
// TODO: needs escape if not action after time period (stuck)
// TODO: needs to not pick up items if inventory full
// TODO: world hop

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import org.osbot.rs07.api.model.GroundItem;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.EquipmentSlot;
import org.osbot.rs07.api.ui.MagicSpell;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.ui.Spells;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

@ScriptManifest(author = "Me", info = "Curse cows", name = "Attack", version = 0, logo = "")
public class Main extends Script {
	// config
	private Skill skill = Skill.STRENGTH;
	private boolean enableCoin = false;
	private boolean enablePickup = true;
	private NPC target;
	private Item ammo;
	private boolean isRanged;
	private int distance = 7;
	private MagicSpell teleport = Spells.NormalSpells.VARROCK_TELEPORT;

	@Override
	public void onStart() throws InterruptedException {
		isRanged = equipment.getItemInSlot(EquipmentSlot.WEAPON.slot).getName().contains("bow");
		ammo = equipment.getItemInSlot(EquipmentSlot.ARROWS.slot);
		int mode = configs.get(43);
		skill = isRanged ? Skill.RANGED : mode == 1 ? Skill.STRENGTH : mode == 3 ? Skill.DEFENCE : Skill.ATTACK;
		experienceTracker.start(skill);
		tabs.open(Tab.INVENTORY);
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
		g.drawString("Elapsed -> " + String.valueOf(formatTime(experienceTracker.getElapsed(skill))), 2, 30);
		g.drawString(String.valueOf(skill) + " -> " + String.valueOf(skills.getStatic(skill)) + " ("
				+ String.valueOf(combat.getCombatLevel()) + ") "
				+ String.valueOf(formatTime(experienceTracker.getTimeToLevel(skill))), 2, 45);
		g.drawString("Gained -> " + String.valueOf(experienceTracker.getGainedLevels(skill)) + " "
				+ String.valueOf(experienceTracker.getGainedXP(skill)) + " "
				+ String.valueOf(experienceTracker.getGainedXPPerHour(skill)), 2, 60);
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
				&& !n.isUnderAttack() && n.isAttackable() && !n.getName().contains("Rat") && map.canReach(n)
				&& n.getPosition().distance(myPlayer().getPosition()) <= distance);
		GroundItem ground = getGroundItems().closest(
				n -> n != null && n.getPosition().distance(myPlayer().getPosition()) <= distance && map.canReach(n)
						&& ((enableCoin && n.getName().contains("Coin")) || n.getName().contains("Big bones")
								|| n.getName().contains("Rune") || n.getName().contains("arrow")
								|| n.getDefinition().isNoted())); // || n.hasAction("Eat", "Drink")));

		// enable running
		if (!settings.isRunning() && settings.getRunEnergy() > random(10, 20)) {
			settings.setRunning(true);
		}
//
//		// low hp logout
//		if (lowHp && food == null) {
//			if (magic.canCast(teleport)) {
//				magic.castSpell(teleport);
//				preventDoubleClick();
//			}
//			stop();
//		}
//
//		// low hp eat
//		if (lowHp && food != null) {
//			food.interact("Eat", "Drink");
//			preventDoubleClick();
//			return nextLoop();
//		}
//
//		// fighting
//		if (target != null && target.isInteracting(myPlayer())) {
//			return nextLoop();
//		}
//		target = null;
//
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
//			camera.toEntity(npc);
			camera.toTop();
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
