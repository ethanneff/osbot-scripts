import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.Entity;
import org.osbot.rs07.api.model.GroundItem;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.model.Player;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

@ScriptManifest(author = "Me", name = "Agility", version = 1, logo = "", info = "")
public class Main extends Script {
	// properties
	private Skill skill = Skill.AGILITY;
	private long lastMovement;
	private long maxIdleTime = 30;

	// timing
	private int tick() {
		return random(100, 300);
	}

	private void interact() throws InterruptedException {
		sleep(random(1000, 1500));
	}

	private void kill() throws InterruptedException {
		stop();
		interact();
	}

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
		g.drawRect(mouse.getPosition().x - 3, mouse.getPosition().y - 3, 6, 6);
	}

	// load
	@Override
	public void onStart() throws InterruptedException {
		lastMovement = System.nanoTime();
		experienceTracker.start(skill);
		tabs.open(Tab.INVENTORY);
		interact();
	}

	// loop
	@Override
	@SuppressWarnings("unchecked")
	public int onLoop() throws InterruptedException {

		// obstacles
		Entity o01 = objects.closest("Rough wall");
		Entity o02 = objects.closest("Tightrope");
		Entity o03 = objects.closest("Narrow wall");
		Entity o04 = objects.closest("Wall");
		Entity o05 = objects.closest("Gap");
		Entity o06 = objects.closest("Crate");
		Entity o07 = objects.closest("Clothes line");
		Entity o08 = objects.closest("Ledge");
		Entity o09 = objects.closest("Edge");
		Entity o10 = objects.closest("Tall tree");
		Entity o11 = objects.closest("Pole-vault");

		// varrock
		Position vp00 = new Position(3221, 3414, 0); // start
		Position vp01 = new Position(3208, 3396, 3); // walk
		Position vp02 = new Position(3232, 3402, 3); // walk
		Area va00 = new Area(3183, 3380, 3250, 3435); // varrock
		Area va01 = new Area(3212, 3409, 3220, 3420).setPlane(3); // cross
		Area va02 = new Area(3201, 3412, 3209, 3420).setPlane(3); // leap
		Area va03 = new Area(3191, 3414, 3198, 3417).setPlane(1); // balance
		Area va04 = new Area(3190, 3407, 3199, 3400).setPlane(3); // leap
		Area va05 = new Area(3179, 3380, 3199, 3398).setPlane(3); // move vp01
		Area va06 = new Area(3200, 3393, 3204, 3404).setPlane(3); // move vp01
		Area va07 = new Area(3205, 3393, 3208, 3404).setPlane(3); // leap
		Area va08 = new Area(3214, 3391, 3230, 3404).setPlane(3); // move vp02
		Area va09 = new Area(3231, 3391, 3232, 3404).setPlane(3); // leap
		Area va10 = new Area(3235, 3402, 3241, 3408).setPlane(3); // hurdle
		Area va11 = new Area(3235, 3410, 3241, 3416).setPlane(3); // jump off

		// canifis
		Position cp00 = new Position(3505, 3488, 0);
		Area ca00 = new Area(3470, 3465, 3519, 3510);
		Area ca01 = new Area(3503, 3490, 3511, 3498).setPlane(2);
		Area ca02 = new Area(3495, 3501, 3504, 3508).setPlane(2);
		Area ca03 = new Area(3484, 3497, 3493, 3505).setPlane(2);
		Area ca04 = new Area(3472, 3490, 3481, 3501).setPlane(3);
		Area ca05 = new Area(3475, 3479, 3485, 3488).setPlane(2);
		Area ca06 = new Area(3486, 3467, 3504, 3479).setPlane(3);
		Area ca07 = new Area(3507, 3473, 3516, 3483).setPlane(2);

		// seers
		Position sp00 = new Position(2729, 3489, 0);
		Area sa00 = new Area(2679, 3442, 2787, 3521);
		Area sa01 = new Area(2719, 3488, 2731, 3498).setPlane(3);
		Area sa02 = new Area(2702, 3485, 2714, 3500).setPlane(2);
		Area sa03 = new Area(2705, 3474, 2717, 3484).setPlane(2);
		Area sa04 = new Area(2697, 3468, 2717, 3477).setPlane(3);
		Area sa05 = new Area(2688, 3458, 2704, 3466).setPlane(2);

		// environment
		Player mod = players.closest(n -> n != null && n.getName().startsWith("Mod "));
		GroundItem ground = getGroundItems()
				.closest(n -> n != null && map.canReach(n) && n.getName().toLowerCase().contains("mark of grace"));
		long currentTime = System.nanoTime();
		long secondsSinceLastMovement = (currentTime - lastMovement) / 1000000000;
		boolean lowHp = skills.getDynamic(Skill.HITPOINTS) <= skills.getStatic(Skill.HITPOINTS) * 0.3;
		Item inventoryFood = inventory.getItem(n -> n != null && n.hasAction("Drink") || n.hasAction("Eat"));

		// early exit
		if (mod != null || (lowHp && inventoryFood == null) || secondsSinceLastMovement > maxIdleTime) {
			kill();
		}

		// eat food
		if (lowHp) {
			inventoryFood.interact("Eat", "Drink");
			interact();
			return tick();
		}

		// enable running
		if (!settings.isRunning() && settings.getRunEnergy() > random(10, 20)) {
			settings.setRunning(true);
			interact();
		}

		// moving
		if (myPlayer().isMoving() || myPlayer().isAnimating()) {
			lastMovement = currentTime;
			return tick();
		}

		// wait for action after movement
		if (secondsSinceLastMovement < random(1500, 2000) / 1000) {
			return tick();
		}

		// pick up
		if (ground != null) {
			ground.interact("Take");
			interact();
			return tick();
		}

		// varrock
		if (va00.contains(myPlayer()) && vp00.distance(myPlayer()) > 10) {
			walking.webWalk(vp00);
		} else if (va00.contains(myPlayer()) && o01 != null) {
			o01.interact("Climb");
		} else if (va01.contains(myPlayer()) && o07 != null) {
			o07.interact("Cross");
		} else if (va02.contains(myPlayer()) && o05 != null) {
			o05.interact("Leap");
		} else if (va03.contains(myPlayer()) && o04 != null) {
			o04.interact("Balance");
		} else if (va04.contains(myPlayer()) && o05 != null) {
			o05.interact("Leap");
		} else if ((va05.contains(myPlayer()) || va06.contains(myPlayer()))) {
			walking.webWalk(vp01);
		} else if (va07.contains(myPlayer()) && o05 != null) {
			o05.interact("Leap");
		} else if (va08.contains(myPlayer())) {
			walking.webWalk(vp02);
		} else if (va09.contains(myPlayer()) && o05 != null) {
			o05.interact("Leap");
		} else if (va10.contains(myPlayer()) && o08 != null) {
			o08.interact("Hurdle");
		} else if (va11.contains(myPlayer()) && o09 != null) {
			o09.interact("Jump-off");
		}

		// canifis
		else if (ca00.contains(myPlayer()) && cp00.distance(myPlayer()) > 10) {
			walking.webWalk(cp00);
		} else if (ca00.contains(myPlayer())) {
			objects.closest(n -> n.getId() == 10819).interact("Climb");
		} else if (ca01.contains(myPlayer())) {
			objects.closest(n -> n.getId() == 10820).interact("Jump");
		} else if (ca02.contains(myPlayer())) {
			objects.closest(n -> n.getId() == 10821).interact("Jump");
		} else if (ca03.contains(myPlayer())) {
			objects.closest(n -> n.getId() == 10828).interact("Jump");
		} else if (ca04.contains(myPlayer())) {
			objects.closest(n -> n.getId() == 10822).interact("Jump");
		} else if (ca05.contains(myPlayer())) {
			objects.closest(n -> n.getId() == 10831).interact("Vault");
		} else if (ca06.contains(myPlayer())) {
			objects.closest(n -> n.getId() == 10823).interact("Jump");
		} else if (ca07.contains(myPlayer())) {
			objects.closest(n -> n.getId() == 10832).interact("Jump");
		}

		// seers
		else if (sa00.contains(myPlayer()) && sp00.distance(myPlayer()) > 15) {
			walking.webWalk(sp00);
			camera.moveYaw(0);
		} else if (sa00.contains(myPlayer())) {
			objects.closest(n -> n.getId() == 11373).interact("Climb-up");
		} else if (sa01.contains(myPlayer())) {
			objects.closest(n -> n.getId() == 11374).interact("Jump");
		} else if (sa02.contains(myPlayer())) {
			objects.closest(n -> n.getId() == 11378).interact("Cross");
		} else if (sa03.contains(myPlayer())) {
			objects.closest(n -> n.getId() == 11375).interact("Jump");
		} else if (sa04.contains(myPlayer())) {
			objects.closest(n -> n.getId() == 11376).interact("Jump");
		} else if (sa05.contains(myPlayer())) {
			objects.closest(n -> n.getId() == 11377).interact("Jump");
		}

		else {
			kill();
		}

		// action
		interact();
		return tick();
	}
}