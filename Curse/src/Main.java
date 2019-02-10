import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.model.Player;
import org.osbot.rs07.api.ui.MagicSpell;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.ui.Spells;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

@ScriptManifest(author = "Me", name = "Curse", version = 0, logo = "", info = "")
public class Main extends Script {
	// properties
	private Skill skill = Skill.MAGIC;
	private MagicSpell spell = Spells.NormalSpells.CURSE;
	private long lastMovement;
	private int maxIdle = 60;

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

	private void kill() throws InterruptedException {
		stop();
		sleep(1000);
	}

	@Override
	@SuppressWarnings("unchecked")
	public int onLoop() throws InterruptedException {
		// properties
		Player mod = players.closest(n -> n != null && n.getName().startsWith("Mod "));
		NPC npc = npcs.closest(n -> n != null && !n.isHitBarVisible() && n.getHealthPercent() > 0 && !n.isUnderAttack()
				&& n.isAttackable() && map.canReach(n));
		boolean ableToCast = magic.canCast(spell);
		long currentTime = System.nanoTime();
		long seconds = (currentTime - lastMovement) / 1000000000;

		// action
		if (myPlayer().isMoving() || myPlayer().isAnimating() || combat.isFighting()) {
			lastMovement = currentTime;
		} else if (seconds > maxIdle || mod != null || npc == null || !ableToCast) {
			kill();
		} else {
			magic.castSpellOnEntity(spell, npc);
			sleep(random(1000, 1200));
		}

		// next
		return random(200, 400);
	}
}