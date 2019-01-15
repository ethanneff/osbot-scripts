import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.MagicSpell;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.ui.Spells;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

@ScriptManifest(author = "Me", info = "Curse cows", name = "Curse", version = 0, logo = "")
public class Main extends Script {
	// config
	String inventory = "Body rune";
	Skill skill = Skill.MAGIC;
	long startAmount = 0;
	long experiencePerSpell = 29;
	String npcName = "Cow";
	String nonNpcName = "Dairy";
	MagicSpell spell = Spells.NormalSpells.CURSE;

	@Override
	public void onStart() {
		startAmount = getInventory().getAmount(inventory);
		experienceTracker.start(skill);
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
		long currentAmount = getInventory().getAmount(inventory);
		g.setColor(new Color(255, 255, 255));
		g.setFont(new Font("Open Sans", Font.PLAIN, 12));
		g.drawString("Curse everything", 2, 230);
		g.drawString("Elapsed time:         \t" + String.valueOf(formatTime(experienceTracker.getElapsed(skill))), 2,
				250);
		g.drawString("Current level:         \t" + String.valueOf(skills.getStatic(skill)) + "\t"
				+ String.valueOf(skills.getExperience(skill)), 2, 270);
		g.drawString("Next level:             \t" + String.valueOf(skills.getStatic(skill) + 1) + "\t"
				+ String.valueOf(skills.experienceToLevel(skill)) + "\t"
				+ String.valueOf(formatTime(experienceTracker.getTimeToLevel(skill))), 2, 290);
		g.drawString("Experience gained: \t" + String.valueOf(experienceTracker.getGainedLevels(skill)) + "\t"
				+ String.valueOf(experienceTracker.getGainedXP(skill)) + "\t"
				+ String.valueOf(experienceTracker.getGainedXPPerHour(skill)), 2, 310);
		g.drawString("Spells used:           \t" + String.valueOf(startAmount - currentAmount) + "\t"
				+ String.valueOf(currentAmount) + "\t"
				+ String.valueOf(currentAmount - skills.experienceToLevel(skill) / experiencePerSpell), 2, 330);
	}

	public int nextLoop() {
		return random(200, 300);
	}

	@Override
	public int onLoop() throws InterruptedException {
		// properties
		@SuppressWarnings("unchecked")
		NPC npc = npcs.closest(n -> n != null && n.getName().contains(npcName) && !n.getName().contains(nonNpcName)
				&& !n.isHitBarVisible() && n.getHealthPercent() > 0 && !n.isUnderAttack() && n.isAttackable());
		boolean ableToCast = magic.canCast(spell);

		// no runes in backpack
		if (!ableToCast) {
			stop();
		}

		// player busy
		if (myPlayer().isAnimating() || myPlayer().isMoving() || myPlayer().isUnderAttack()) {
			return nextLoop();
		}

		// npc busy
		if (npc == null) {
			camera.toEntity(npc);
			return nextLoop();
		}

		// attack
		magic.castSpellOnEntity(spell, npc);
		sleep(random(600, 800));

		return nextLoop();
	}
}

//// bones
//GroundItem bones = getGroundItems().closest("Bones");
//if (bones != null && (bones.getPosition().distance(myPlayer().getPosition()) > 1) && (bones.getPosition().distance(myPlayer().getPosition()) < 5)) {
//	bones.interact("Take");
//	sleep(random(450, 750));
//	if (getInventory().contains("Bones")) {
//		getInventory().interact("Bury","Bones");
//		return tick();
//	}
//}
//
//// feathers
//GroundItem feathers = getGroundItems().closest("Feathers");
//if (feathers != null && (bones.getPosition().distance(myPlayer().getPosition()) > 1) && (feathers.getPosition().distance(myPlayer().getPosition()) < 2)) {
//	feathers.interact("Take");
//}
