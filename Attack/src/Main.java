
// TODO: re-attack old target
// TODO: capture change in attack
// TODO: add world hop
// TODO: fix logout does not work if in combat
// TODO: turn off auto retaliate and add logic to attack target 

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import org.osbot.rs07.api.model.GroundItem;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.model.Player;
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
	private long idleTime = 30;
	private boolean isRanged = false;
	private int distance = 7;
	private long lastMovement = System.nanoTime();
	private MagicSpell teleport = Spells.NormalSpells.VARROCK_TELEPORT;
	private NpcType currentNpcType = NpcType.FleshCrawler;

	private enum NpcType {
		FleshCrawler, Cyclops, ChaosDruids
	}

	@Override
	public void onStart() throws InterruptedException {
		isRanged = equipment.getItemInSlot(EquipmentSlot.WEAPON.slot).getName().toLowerCase().contains("bow")
				|| equipment.getItemInSlot(EquipmentSlot.CAPE.slot).getName().toLowerCase().contains("ava");
		int mode = configs.get(43);
		skill = isRanged ? Skill.RANGED : mode == 1 ? Skill.STRENGTH : mode == 3 ? Skill.DEFENCE : Skill.ATTACK;
		experienceTracker.start(skill);
		tabs.open(Tab.INVENTORY);
		sleep(500);
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
		// environment
		Item food = inventory.getItem(n -> n != null && n.hasAction("Drink") || n.hasAction("Eat"));
		Item bone = inventory.getItem(n -> n != null && n.hasAction("Bury"));
		Player mod = players.closest(n -> n != null && n.getName().startsWith("Mod "));
		NPC potentialNpc = npcs.closest(n -> n != null && n.isAttackable() && !n.isHitBarVisible()
				&& n.getHealthPercent() > 0 && !n.isUnderAttack() && !n.getName().toLowerCase().contains("rat")
				&& map.canReach(n) && n.getPosition().distance(myPlayer().getPosition()) <= distance);
		NPC attackingNpc = npcs.closest(n -> n != null && n.isAttackable() && !n.isUnderAttack()
				&& !n.getName().toLowerCase().contains("rat") && map.canReach(n)
				&& n.getPosition().distance(myPlayer().getPosition()) <= distance && n.isInteracting(myPlayer()));
		NPC nextTarget = attackingNpc != null ? attackingNpc : potentialNpc;
		GroundItem ground = getGroundItems()
				.closest(n -> n != null && n.getPosition().distance(myPlayer().getPosition()) <= distance
						&& map.canReach(n) && n.getName().toLowerCase().contains("shield left half")
						|| n.getName().toLowerCase().contains("dragon spear")
						|| n.getName().toLowerCase().contains("dragon med helm")
						|| n.getName().toLowerCase().contains("rune 2h sword")
						|| n.getName().toLowerCase().contains("rune sq shield")
						|| n.getName().toLowerCase().contains("rune kitesheld")
						|| n.getName().toLowerCase().contains("diamond")
						|| n.getName().toLowerCase().contains("dragonstone")
						|| n.getName().toLowerCase().contains("runite bar")
						|| n.getName().toLowerCase().contains("half of key")
						|| (isRanged && n.getAmount() >= 3
								&& (n.getName().toLowerCase().contains("arrow")
										|| n.getName().toLowerCase().contains("dart")))
						|| (currentNpcType == NpcType.FleshCrawler
								&& (n.getDefinition().isNoted() || n.getName().toLowerCase().contains("coin")
										|| n.getName().toLowerCase().contains("ranarr")
										|| n.getName().toLowerCase().contains("nature rune")
										|| n.getName().toLowerCase().contains("dust rune")
										|| n.getName().toLowerCase().contains("fire rune")))
						|| (currentNpcType == NpcType.Cyclops
								&& (n.getDefinition().isNoted() || n.getName().toLowerCase().contains("coin")
										|| n.hasAction("Eat", "Drink") || n.getName().toLowerCase().contains("rune")
										|| n.getName().toLowerCase().contains("ranarr")
										|| n.getName().toLowerCase().contains("adamant")
										|| n.getName().toLowerCase().contains("mithril")
										|| n.getName().toLowerCase().contains("defender")
										|| n.getName().toLowerCase().contains("black knife")
										|| n.getName().toLowerCase().contains("black longsword")
										|| n.getName().toLowerCase().contains("long bone")
										|| n.getName().toLowerCase().contains("curved bone")
										|| n.getName().toLowerCase().contains("snapdragon")
										|| n.getName().toLowerCase().contains("torstol")
										|| n.getName().toLowerCase().contains("steel chainbody")
										|| n.getName().toLowerCase().contains("2h")))
						|| (currentNpcType == NpcType.ChaosDruids
								&& (n.getDefinition().isNoted() || n.getName().toLowerCase().contains("ranarr")
										|| n.getName().toLowerCase().contains("harralander")
										|| n.getName().toLowerCase().contains("avantoe")
										|| n.getName().toLowerCase().contains("irit leaf")
										|| n.getName().toLowerCase().contains("lantadyme")
										|| n.getName().toLowerCase().contains("cadantine")
										|| n.getName().toLowerCase().contains("dwarf weed")
										|| n.getName().toLowerCase().contains("law rune")
										|| n.getName().toLowerCase().contains("air rune")
										|| n.getName().toLowerCase().contains("nature rune"))));

		// state
		long currentTime = System.nanoTime();
		boolean lowHp = skills.getDynamic(Skill.HITPOINTS) <= skills.getStatic(Skill.HITPOINTS) / 2;
		boolean modNearby = mod != null;
		boolean playerBusy = myPlayer().isAnimating() || myPlayer().isMoving() || combat.isFighting();
		boolean inventoryIsFull = inventory.isFull() && currentNpcType == NpcType.FleshCrawler;
		boolean hasNotAttackedInAwhile = ((currentTime - lastMovement) / 1000000000) > idleTime;
		boolean aboutToDie = lowHp && food == null && !magic.canCast(teleport);
		boolean shouldTeleport = lowHp && food == null && magic.canCast(teleport);
		boolean shouldEat = lowHp && food != null;
		boolean shouldRun = !settings.isRunning() && settings.getRunEnergy() > random(10, 20);
		boolean shouldBury = bone != null;
		boolean shouldPickUp = ground != null && !inventory.isFull();
		boolean shouldSpecial = !isRanged && combat.getSpecialPercentage() >= random(80, 100)
				&& !combat.isSpecialActivated();
		boolean shouldAttack = nextTarget != null && !playerBusy;

		// already busy
		if (playerBusy) {
			lastMovement = currentTime;
		}

		// find next action
		if (modNearby || inventoryIsFull || hasNotAttackedInAwhile || aboutToDie) {
			// early exit
			stop();
			preventDoubleClick();
		} else if (shouldTeleport) {
			// teleport
			magic.castSpell(teleport);
			preventDoubleClick();
		} else if (shouldEat) {
			// eat
			food.interact("Eat", "Drink");
			preventDoubleClick();
		} else if (shouldRun) {
			// run
			settings.setRunning(true);
			preventDoubleClick();
		} else if (shouldBury) {
			// bury
			bone.interact("Bury");
			preventDoubleClick();
		} else if (shouldPickUp) {
			// pick up
			ground.interact("Take");
			preventDoubleClick();
		} else if (shouldSpecial) {
			// enable special
			combat.toggleSpecialAttack(true);
			preventDoubleClick();
		} else if (shouldAttack) {
			// attack
			nextTarget.interact("Attack");
			camera.toTop();
			waitForNpcToRespond();
		}

		// // warriors guild
		// if (inventory.contains("Black full helm") && inventory.contains("Black
		// platebody")
		// && inventory.contains("Black platelegs")) {
		// Entity animator = objects.closest("Magical Animator");
		// if (animator != null) {
		// animator.interact("Animate");
		// preventDoubleClick();
		// return nextLoop();
		// }
		// }

		// high alch

		// loop
		return nextLoop();
	}
}
