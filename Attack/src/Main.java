
// TODO: re-attack old target
// TODO: capture change in attack
// TODO: fix logout does not work if in combat
// TODO: turn off auto retaliate and add logic to attack target 

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.Entity;
import org.osbot.rs07.api.model.GroundItem;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.model.Player;
import org.osbot.rs07.api.ui.EquipmentSlot;
import org.osbot.rs07.api.ui.PrayerButton;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

@ScriptManifest(author = "Me", info = "kill things", name = "Attack", version = 0, logo = "")
public class Main extends Script {
	// config
	private Skill skill = Skill.STRENGTH;
	private long idleTime = 60;
	private boolean isRanged = false;
	private int distance = 7;
	private long minPeople = 3;
	private long lastMovement = System.nanoTime();
	private String teleport = "Edgeville";
	private PrayerButton prayerSkill = PrayerButton.EAGLE_EYE;
	private long hudBase = 35;
	private long pestZoneX = 6700;
	private NpcType currentNpcType = NpcType.ElderChaosDruids;

	private enum NpcType {
		FleshCrawler, Cyclops, ChaosDruids, Banchee, GreaterDemon, MossGiant, ElderChaosDruids
	}

	@Override
	public void onStart() throws InterruptedException {
		Item weapon = equipment.getItemInSlot(EquipmentSlot.WEAPON.slot);
		Item cape = equipment.getItemInSlot(EquipmentSlot.CAPE.slot);
		isRanged = (weapon != null && weapon.getName().toLowerCase().contains("bow"))
				|| (cape != null && cape.getName().toLowerCase().contains("ava"));
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
		g.drawString("Elapsed -> " + String.valueOf(formatTime(experienceTracker.getElapsed(skill))), 2, hudBase);
		g.drawString(String.valueOf(skill) + " -> " + String.valueOf(skills.getStatic(skill)) + " ("
				+ String.valueOf(combat.getCombatLevel()) + ") "
				+ String.valueOf(formatTime(experienceTracker.getTimeToLevel(skill))), 2, hudBase + 15);
		g.drawString("Gained -> " + String.valueOf(experienceTracker.getGainedLevels(skill)) + " "
				+ String.valueOf(experienceTracker.getGainedXP(skill)) + " "
				+ String.valueOf(experienceTracker.getGainedXPPerHour(skill)), 2, hudBase + 30);
		g.drawString("Inventory -> " + String.valueOf(inventory.getEmptySlotCount()) + "/"
				+ String.valueOf(inventory.getCapacity()), 2, hudBase + 45);
		g.drawRect(mouse.getPosition().x - 3, mouse.getPosition().y - 3, 6, 6);
	}

	@Override
	@SuppressWarnings("unchecked")
	public int onLoop() throws InterruptedException {
		// environment
		Position playerPosition = myPlayer().getPosition();
		Item food = inventory.getItem(n -> n != null && n.hasAction("Drink") || n.hasAction("Eat"));
		Item bone = inventory.getItem(n -> n != null && n.hasAction("Bury") && n.getName().contains("Big bone"));
		Item necklace = equipment.getItemInSlot(EquipmentSlot.AMULET.slot);
		Entity chaosAlter = objects.closest(n -> n != null && n.getId() == 411);
		Player mod = players.closest(n -> n != null && n.getName().startsWith("Mod "));
		Player nearbyMovingPlayer = players
				.closest(p -> p != null && p.isMoving() && p.getPosition().distance(myPlayer()) > distance);
		NPC potentialNpc = npcs.closest(n -> n != null && n.isAttackable() && !n.isUnderAttack()
				&& n.getHealthPercent() > 0 && !n.getName().toLowerCase().contains("rat") && map.canReach(n)
				&& n.getPosition().distance(myPlayer().getPosition()) <= distance);
		NPC attackingNpc = npcs.closest(n -> n != null && n.isAttackable() && !n.isUnderAttack()
				&& n.getHealthPercent() > 0 && !n.getName().toLowerCase().contains("rat") && map.canReach(n)
				&& n.getPosition().distance(myPlayer().getPosition()) <= distance && n.isInteracting(myPlayer()));
		NPC nextTarget = attackingNpc != null ? attackingNpc : potentialNpc;
		GroundItem ground = groundItems
				.closest(n -> n != null && n.getPosition().distance(myPlayer().getPosition()) <= distance
						&& map.canReach(n) && n.getName().toLowerCase().contains("shield left half")
						|| n.getName().toLowerCase().contains("dragon spear")
						|| n.getName().toLowerCase().contains("dragon med helm")
						|| n.getName().toLowerCase().contains("rune 2h sword")
						|| n.getName().toLowerCase().contains("rune sq shield")
						|| n.getName().toLowerCase().contains("rune kitesheld")
//						|| n.getName().toLowerCase().contains("diamond")
						|| n.getName().toLowerCase().contains("looting bag")
						|| n.getName().toLowerCase().contains("dragonstone")
						|| n.getName().toLowerCase().contains("runite bar")
						|| n.getName().toLowerCase().contains("emblem")
						|| n.getName().toLowerCase().contains("half of key")
						|| (isRanged && n.getAmount() >= 3
								&& (n.getName().toLowerCase().contains("arrow")
										|| n.getName().toLowerCase().contains("dart")))
						|| (currentNpcType == NpcType.FleshCrawler
								&& (n.getDefinition().isNoted() || n.getName().toLowerCase().contains("ranarr")
										|| n.getName().toLowerCase().contains("nature rune")))
						|| (currentNpcType == NpcType.GreaterDemon
								&& (n.getDefinition().isNoted() || n.getName().toLowerCase().contains("ranarr")
										|| n.getName().toLowerCase().contains("base")
										|| n.getName().toLowerCase().contains("rune")
										|| n.getName().toLowerCase().contains("mithril")
										|| n.getName().toLowerCase().contains("adamant")
										|| n.getName().toLowerCase().contains("demon head")
										|| n.getName().toLowerCase().contains("slayer")
										|| n.getName().toLowerCase().contains("top")))
						|| (currentNpcType == NpcType.Cyclops && (n.getDefinition().isNoted()
								|| n.hasAction("Eat", "Drink") || n.getName().toLowerCase().contains("rune")
								|| n.getName().toLowerCase().contains("gloves")
								|| n.getName().toLowerCase().contains("essence")
								|| n.getName().toLowerCase().contains("ranarr")))
						|| (currentNpcType == NpcType.Banchee
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
						|| (currentNpcType == NpcType.MossGiant
								&& (n.getDefinition().isNoted() || n.getName().toLowerCase().contains("rune")
										|| n.getName().toLowerCase().contains("air rune")
										|| n.getName().toLowerCase().contains("law rune")
										|| n.getName().toLowerCase().contains("scroll")
										|| n.getName().toLowerCase().contains("mossy key")
										|| n.getName().toLowerCase().contains("cosmic rune")
										|| n.getName().toLowerCase().contains("chaos rune")
										|| n.getName().toLowerCase().contains("death rune")
										|| n.getName().toLowerCase().contains("nature rune")
										|| n.getName().toLowerCase().contains("blood rune")
										|| n.getName().toLowerCase().contains("long bone")
										|| n.getName().toLowerCase().contains("curved bone")
										|| n.getName().toLowerCase().contains("ranarr")
										|| n.getName().toLowerCase().contains("snapdragon")
										|| n.getName().toLowerCase().contains("torstol")))
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

		// pest control
		Entity pestGangplank = objects.closest(n -> n.getId() == 25631);
		Area pestBeforeEntry = new Area(2643, 2647, 2646, 2640);
		Area pestWaitingInBoat = new Area(2637, 2640, 2641, 2648);
		NPC pest = npcs.closest(n -> n != null && n.isAttackable() && n.getHealthPercent() > 0
				&& (n.getName().toLowerCase().contains("defiler") || n.getName().toLowerCase().contains("defiler")
						|| n.getName().toLowerCase().contains("brawler")
						|| n.getName().toLowerCase().contains("defiler")
						|| n.getName().toLowerCase().contains("ravager")
						|| n.getName().toLowerCase().contains("shifter")
						|| n.getName().toLowerCase().contains("spinner")
						|| n.getName().toLowerCase().contains("splatter")
						|| n.getName().toLowerCase().contains("torcher"))
				&& map.canReach(n));

		// state
		long currentWorld = worlds.getCurrentWorld();
		long currentTime = System.nanoTime();
		boolean necklaceCanTeleport = necklace != null && necklace.hasAction(teleport);
		boolean playerBeforePestEntry = pestBeforeEntry.contains(myPlayer()) && pestGangplank != null;
		boolean playerWaitingInPestBoat = pestWaitingInBoat.contains(myPlayer());
		boolean playerInPestControl = playerPosition.getX() > pestZoneX || playerBeforePestEntry
				|| playerWaitingInPestBoat;
		boolean lowHp = skills.getDynamic(Skill.HITPOINTS) <= skills.getStatic(Skill.HITPOINTS) / 2;
		boolean modNearby = mod != null;
		boolean playerBusy = myPlayer().isAnimating() || myPlayer().isMoving() || combat.isFighting();
		boolean playerOutOfCombat = ((currentTime - lastMovement) / 1000000000) > 10;
		boolean inventoryIsFull = inventory.isFull();
		boolean inventoryIsFullWithoutFood = inventoryIsFull && food == null;
		boolean hasNotMovedInALongTime = ((currentTime - lastMovement) / 1000000000) > idleTime;
		boolean cannotHeal = lowHp && food == null && !playerInPestControl;
		boolean shouldTeleport = lowHp && food == null && !playerInPestControl && necklaceCanTeleport;
		boolean shouldEat = food != null && (lowHp || inventoryIsFull);
		boolean shouldRun = !settings.isRunning() && settings.getRunEnergy() > random(10, 20);
		boolean shouldBury = bone != null;
		boolean shouldPickUp = ground != null && !inventory.isFull();
		boolean shouldSpecial = !isRanged && combat.getSpecialPercentage() >= random(50, 100)
				&& !combat.isSpecialActivated();
		boolean shouldAttack = nextTarget != null && !playerBusy;
		boolean shouldWorldHop = players.filter(p -> p != null && myPlayer().getArea(distance).contains(p))
				.size() >= minPeople && !playerBusy && playerOutOfCombat;
		boolean shouldPestPrayer = !prayer.isActivated(prayerSkill);
		boolean shouldHopToPestControl = currentWorld != 344 && (playerBeforePestEntry || playerWaitingInPestBoat);
		boolean shouldLogout = modNearby || inventoryIsFullWithoutFood || hasNotMovedInALongTime || cannotHeal;
		boolean shouldPray = skills.getDynamic(Skill.PRAYER) <= 20 && currentNpcType == NpcType.ElderChaosDruids;

		// update
		lastMovement = playerBusy ? currentTime : lastMovement;
		hudBase = playerInPestControl ? 150 : hudBase;

		// pest control
		if (playerInPestControl) {
			if (playerBusy) {
				log("player busy");
			} else if (shouldHopToPestControl) {
				log("hop to pest control");
				worlds.hop(344);
			} else if (playerBeforePestEntry) {
				log("enter boat");
				camera.toTop();
				pestGangplank.interact("Cross");
			} else if (playerWaitingInPestBoat) {
				log("waitingInBoat");
			} else if (pest != null) {
				log("attack pest");
				pest.interact("Attack");
				camera.toTop();
			} else if (shouldRun) {
				log("enable run");
				settings.setRunning(true);
			} else if (shouldPestPrayer) {
				log("enable prayer");
				prayer.set(prayerSkill, true);
			} else if (nearbyMovingPlayer != null) {
				log("follow player");
				nearbyMovingPlayer.interact("Follow");
			}
			return random(800, 1200);
		}

		// attack
		if (shouldLogout) {
			log("early exit");
			stop();
		} else if (shouldWorldHop) {
			log("world hop");
			worlds.hopToP2PWorld();
		} else if (shouldTeleport) {
			log("teleport");
			equipment.openTab();
			necklace.interact(teleport);
		} else if (shouldEat) {
			log("eat");
			food.interact("Eat", "Drink");
		} else if (shouldRun) {
			log("run");
			settings.setRunning(true);
		} else if (shouldPray) {
			log("pray");
			walking.webWalk(new Position(3240, 3609, 0));
			chaosAlter.interact();
		} else if (shouldBury) {
			log("bury");
			bone.interact("Bury");
		} else if (shouldPickUp) {
			log("pick up");
			ground.interact("Take");
		} else if (shouldSpecial) {
			log("enable special");
			combat.toggleSpecialAttack(true);
		} else if (shouldAttack) {
			log("attack");
			nextTarget.interact("Attack");
			camera.toTop();
		}

		// high alch

		// loop
		return random(800, 1200);
	}
}
