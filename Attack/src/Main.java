
// TODO: still click on doors
// TODO: if no healthbar in 1 minute, logout
// TODO: need to figure out best way to do myPlayer().isUnderAttack() to keep hitting target
// TODO: need to figure out how to make script work when npc attack first
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
	private boolean enableCoin = true;
	private boolean enablePickup = true;
	private NPC target;
	private Item ammo;
	private boolean isRanged;
	private int distance = 7;
	private MagicSpell teleport = Spells.NormalSpells.VARROCK_TELEPORT;
	private long lastAttack;

	@Override
	public void onStart() throws InterruptedException {
		isRanged = equipment.getItemInSlot(EquipmentSlot.WEAPON.slot).getName().toLowerCase().contains("bow");
		ammo = equipment.getItemInSlot(EquipmentSlot.ARROWS.slot);
		int mode = configs.get(43);
		skill = isRanged ? Skill.RANGED : mode == 1 ? Skill.STRENGTH : mode == 3 ? Skill.DEFENCE : Skill.ATTACK;
		experienceTracker.start(skill);
		lastAttack = System.nanoTime();
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
		boolean lowHp = skills.getDynamic(Skill.HITPOINTS) <= skills.getStatic(Skill.HITPOINTS) / 2;
		Item food = inventory.getItem(n -> n != null && n.hasAction("Drink") || n.hasAction("Eat"));
		Item bone = inventory.getItem(n -> n != null && n.hasAction("Bury"));
		NPC npc = npcs.closest(n -> n != null && n.isAttackable() && !n.isHitBarVisible() && n.getHealthPercent() > 0
				&& !n.isUnderAttack() && !n.getName().toLowerCase().contains("rat") && map.canReach(n)
				&& n.getPosition().distance(myPlayer().getPosition()) <= distance);
		GroundItem ground = getGroundItems().closest(n -> n != null
				&& n.getPosition().distance(myPlayer().getPosition()) <= distance && map.canReach(n)
				&& ((enableCoin && n.getName().toLowerCase().contains("coin"))
						|| n.getName().toLowerCase().contains("big bones")
						|| n.getName().toLowerCase().contains("snapdragon")
						|| n.getName().toLowerCase().contains("half") || n.getName().toLowerCase().contains("ranarr")
						|| n.getName().toLowerCase().contains("torstol") || n.getName().toLowerCase().contains("key")
						|| n.getName().toLowerCase().contains("rune") || n.getName().toLowerCase().contains("dragon")
						|| n.getName().toLowerCase().contains("arrow") || n.getDefinition().isNoted()
						|| n.hasAction("Eat", "Drink")));

		// enable running
		if (!settings.isRunning() && settings.getRunEnergy() > random(10, 20)) {
			settings.setRunning(true);
		}

		// stop if full
		if (inventory.isFull()) {
			stop();
			preventDoubleClick();
			return nextLoop();

		// stop if full
//		if (inventory.isFull()) {
//			stop();
//			preventDoubleClick();
//			return nextLoop();
//		}
		}

		// record last attack
//		long currentTime = System.nanoTime();
//		if (combat.getFighting().isHitBarVisible()) {
//			lastAttack = currentTime;
//		}

//		// logout if no attack
//		long seconds = (currentTime - lastAttack) / 1000000000;
//		if (seconds > 30) {
//			stop();
//		}

		// low hp logout
		if (lowHp && food == null) {
			if (magic.canCast(teleport)) {
				magic.castSpell(teleport);
				preventDoubleClick();
			}
			stop();
			preventDoubleClick();
			return nextLoop();
		}
		

		// low hp eat
		if (lowHp && food != null) {
			food.interact("Eat", "Drink");
			preventDoubleClick();
			return nextLoop();
		}

		// bury
		if (enableBones && bone != null) {
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
			camera.toTop();
		}

		// enable special attack
		if (combat.getSpecialPercentage() >= random(80, 100) && !combat.isSpecialActivated()) {
			combat.toggleSpecialAttack(true);
		}

		// reset
		if (target != null && !target.isInteracting(myPlayer())) {
			target = null;
		}

		// re-attack
		if (target != null && !myPlayer().isInteracting(target) && target.isInteracting(myPlayer())) {
			target.interact("Attack");
			waitForNpcToRespond();
			return nextLoop();
		}

		// player busy
		if (myPlayer().isAnimating() || myPlayer().isMoving() || combat.isFighting()) {// myPlayer().isUnderAttack()) {
			return nextLoop();
		}

		// attack npc
		npc.interact("Attack");
		target = npc;
		waitForNpcToRespond();
		return nextLoop();
	}
}
