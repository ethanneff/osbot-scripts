import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.model.GroundItem;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.EquipmentSlot;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;

import java.awt.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;


/**
 * Created by Alek on 6/26/2016.
 */
@ScriptManifest(name = "Macro Killer", author = "Alek", version = 1.4, info = "Macro Killer", logo = "")

public class MacroKiller extends Script {

    private boolean isUsingRanged = false;
    private Area combatArea;
    private String state = "State: Idle";
    private Font titleFont = new Fon﻿t("Sans-Serif", Font.BOLD, 10);
    private String ammoType;
    private int ammoRemaining = 0;
    private boolean isAvasEquipped = false;
    private int collectAmmo = 0;

    private Predicate<NPC> suitableNPC = n ->
            getMap().canReach(n) &&
            n.getHealthPercent() > 0 &&
            n.hasAction("Attack") &&
            combatArea.contains(n) &&
            !n.isUnderAttack() &&
            getMap().realDistance(n) < 7;

    private Predicate<GroundItem> suitableArrowStack = g ->
            g.getName().contains(ammoType) &&
            getMap().realDistance(g) < 4;


    @Override
    public void onPaint(Graphics2D g) {
        g.setFont(titleFont);
        g.setColor(Color.WHITE);
        g.drawRect(mouse.getPosition().x - 3, mouse.getPosition().y - 3, 6, 6);
        g.drawString("Macro Killer v1.5 - Written by Alek", 10, 250);
        g.drawString(state, 10, 265);
        if (isUsingRanged)
            g.drawString("Remaining " + ammoType + "s: " + ammoRemaining, 10, 280);
    }

    @Override
    public void onStart() {
        if (getEquipment().getItemInSlot(EquipmentSlot.WEAPON.slot).getName().contains("bow")) {
            Item ammo = getEquipment().getItemInSlot(EquipmentSlot.ARROWS.slot);
            if (ammo != null && ammo.getAmount() > 1) {
                ammoType = ammo.getName().toLowerCase().split(" ")[1];
                isUsingRanged = true;
            }
            if (isUsingRanged) {
                Item item = getEquipment().getItemInSlot(EquipmentSlot.CAPE.slot);
                isAvasEquipped = item != null && (item.getName().contains("Ava's"));
            }
        }
        combatArea = myPlayer().getArea(6);
    }

    @Override
    public int onLoop() {

        if (getSkills().getDynamic(Skill.HITPOINTS) < (getSkills().getStatic(Skill.HITPOINTS) / 2)) {
            state = "State: Looking for food to eat";
            Optional<Item> foodItem = Arrays.stream(getInventory().getItems()).filter(i -> i != null && (i.hasAction("Eat") || i.hasAction("Drink"))).findFirst();
            if (foodItem.isPresent()) {
                state = "State: Eating food " + foodItem.get().getName();
                foodItem.get().interact("Eat", "Drink");
            } else {
                state = "State: No food remaining, logging out";
                stop(true);
            }
        } else if (!getCombat().isFighting() || myPlayer().getInteracting() == null) {
            if (isUsingRanged) {
                state = "State: Checking equipment for " + ammoType + "s";
                Item arrows = getEquipment().getItemInSlot(EquipmentSlot.ARROWS.slot);
                if (arrows == null || arrows.getAmount() < 10) {
                    state = "State: Not enough arrows, logging out";
                    stop(true);
                    return 0;
                }
                ammoRemaining = arrows.getAmount();
                state = "State: Scanning ground for " + ammoType + "s";
                java.util.List<GroundItem> arrowItems = groundItems.getAll().stream().filter(suitableArrowStack).collect(Collectors.toList());
                arrowItems.sort(Comparator.comparingInt(GroundItem::getAmount).thenComparingInt(GroundItem::getAmount).reversed());
                if (!isAvasEquipped && !arrowItems.isEmpty()) {
                    if (arrowItems.get(0).getAmount() > 1 || ((collectAmmo = ~collectAmmo & 1) == 1)) {
                        state = "State: Looting " + arrowItems.get(0).getName() + "(s) with a stack size of " + arrowItems.get(0).getAmount();
                        if (arrowItems.get(0).interact("Take")) {
                            ConditionalSleep pickup = new ConditionalSleep(4000, 500) {
                                @Override
                                public boolean condition() throws InterruptedException {
                                    return !arrowItems.get(0).exists();
                                }
                            };
                            if (pickup.sleep()) {
                                if (arrowItems.get(0).getName().equals(getEquipment().getItemInSlot(EquipmentSlot.ARROWS.slot).getName()))
                                    getInventory().interact("Wield", arrowItems.get(0).getId());
                            }
                        }
                    }
                }
            }
            state = "State: Searching for monsters to kill";
            java.util.List<NPC> npcs = getNpcs().getAll().stream().filter(suitableNPC).collect(Collectors.toList());
            if (!npcs.isEmpty()) {
                npcs.sort(Comparator.<NPC>comparingInt(a -> getMap().realDistance(a)).thenComparingInt(b -> getMap().realDistance(b)));
                if (npcs.get(0).interact("Attack")) {
                    state = "State: Attacking " + npcs.get(0).getName();
                    new ConditionalSleep(3000, 500) {
                        @Override
                        public boolean condition() throws InterruptedException {
                            return !npcs.get(0).exists() || npcs.get(0).isUnderAttack();
                        }
                    }.sleep();
                }
            }
        }
        return 500;
    }

}