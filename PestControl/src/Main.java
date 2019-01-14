import org.osbot.rs07.api.map.Position;

import org.osbot.rs07.api.model.Entity;

import org.osbot.rs07.api.model.NPC;

import org.osbot.rs07.api.model.RS2Object;

import org.osbot.rs07.api.ui.Message;

import org.osbot.rs07.api.ui.Message.MessageType;

import org.osbot.rs07.script.MethodProvider;

import org.osbot.rs07.script.Script;

import org.osbot.rs07.script.ScriptManifest;

import java.awt.*;

import java.util.Random;

@ScriptManifest(

 author = "54b3ew5vw",

 info = "PestControl test herp derp",

 name = "PestControl Afker B",

 version = 0,

 logo = "")

public class Main extends Script {

 // Npc names

 String[] pestControlMonsters = {
  "Brawler",
  "Defiler",
  "Ravager",
  "Shifter",
  "Spinner",
  "Torcher"
 };

 boolean areWeInBoat, didWeArrive, didWeFinish = false;

 int okset = 0;

 String status = "Nothing";

 //int[] pestControlStarter = {1,2,3,4};

 //int[] pestControlMiddle = {10339,1630};

 //int[] pestControlBoat1 = {2260,2643,2263,2638};

 //int[] pestControlBoat2 = {2637,2647,2641,2642};

 //int[] pestControlBoat3 = {2632,2654,2635,2649};

 @Override

 public void onStart() {

  log("=============================");

  log("= Starting pest control bot =");

  log("=============================");

  getBot().addMessageListener(this);

 }

 private enum State {

  BOATING,
  WAITING,
  MOVING,
  FINDINGTARGET,
  KILLING,
  CAMERAMOVE,
  MICROWAIT;

 };

 private State getState() {

  NPC findEnemy = npcs.closest(pestControlMonsters);

  RS2Object findCauldron = objects.closest("Cauldron");

  RS2Object findPlank = objects.closest("Gangplank");

  //RS2Object findPlank = objects.closest(9999).getX;

  if (areWeInBoat == true && findCauldron != null && findPlank != null) {

   status = "Waiting";

   return State.WAITING;

  }

  if (findCauldron == null && findPlank == null) {

   areWeInBoat = false;

   okset = objects.closest("Lander boat").getY() - myPlayer().getY();

   //log (objects.objects.closest("Lander boat").getY());

   //log (myPlayer().getY());

   if (objects.closest("Lander boat").getY() - myPlayer().getY() < 10) {

    status = "Moving";

    return State.MOVING;

   }

  }

  if (areWeInBoat == false && myPlayer().getY() < 3000 && findPlank != null) {

   status = "Entering boat";

   return State.BOATING;

  }

  if (findEnemy != null && !myPlayer().isAnimating() && !myPlayer().isMoving() && !myPlayer().isUnderAttack() && myPlayer().getInteracting() == null) {

   status = "Finding Target";

   return State.FINDINGTARGET;

  }

  if (myPlayer().isAnimating() || myPlayer().isUnderAttack() || myPlayer().isMoving() || myPlayer().getInteracting() != null) {

   status = "Killing";

   return State.KILLING;

  }

  status = "MicroWaiting";

  return State.MICROWAIT;

 }

 //@Override

 //public int onLoop() throws InterruptedException {

 //	NPC findEnemy = npcs.closest(pestControlMonsters);

 //	if (findEnemy != null){

 // if (!myPlayer().isAnimating() && !myPlayer().isMoving() && !myPlayer().isUnderAttack() && myPlayer().getInteracting() == null){

 // findEnemy.interact("Attack");

 // sleep(random(1000, 5000));

 // }

 //	}

 // return random(100, 1000);

 //}

 public void onMessage(Message message) {

  if (message.getType() == MessageType.GAME) {

   try {

    if (message.getMessage().contains("You board the lander") || message.getMessage().contains("can't reach that!")) {

     areWeInBoat = true;

    } else {

     // Do nothing

    }

   } catch (Exception e) {

    e.printStackTrace();

   }

  }

 }

 @Override

 public int onLoop() throws InterruptedException {

  switch (getState()) {

   case WAITING:

    sleep(random(300, 750));

    //log ("Waiting");

    break;

   case MICROWAIT:

    sleep(random(10, 100));

    break;

   case BOATING:

    objects.closest("Gangplank").interact("Cross");

    sleep(random(750, 1250));

    //log ("Entering boat");

    break;

   case MOVING:

    int newX = myPlayer().getX() + random(0, 18) - 9;

    int newY = myPlayer().getY() - random(15, 25);

    //Position baseObject = objects.closest(9999).getPosition();

    //Position towerLeft = new Position((baseObject.getX() - 14), (baseObject.getY() + 14), 0);

    //Position towerRight = new Position((baseObject.getX() + 11), (baseObject.getY() + 14), 0);

    //Position towerTopLeft = new Position((baseObject.getX() - 11), (baseObject.getY() + 29), 0);

    //Position towerTopRight = new Position((baseObject.getX() + 8), (baseObject.getY() + 29), 0);

    //Position[] newLocation = {new Position(newX, newY, 0)};

    if (map.canReach(new Position(newX, newY, 0)) && myPlayer().isMoving() == false) {

     log("Location; " + newX + "," + newY + ",0");

     localWalker.map.walk(newX, newY);

     log("attempt click");

     sleep(random(1000, 1750));

    } else {

     sleep(random(100, 1000));

    }

    break;

   case FINDINGTARGET:

    NPC findEnemy = npcs.closest(pestControlMonsters);

    NPC findPortal = npcs.closest("Portal");

    if (findPortal != null && findPortal.isAttackable()) {

     findEnemy.interact("Attack");

     sleep(random(300, 2500));

     break;

    }

    if (findEnemy != null && findEnemy.isAttackable()) {

     findEnemy.interact("Attack");

     sleep(random(300, 2500));

    } else {

     sleep(random(350, 1250));

    }

    //log("Finding target");

    break;

   case KILLING:

    sleep(random(500, 3000));

    //log("Killing");

    break;

  }

  return random(200, 800);

 }

 @Override

 public void onExit() {

  log("YoloSwagAFk");

 }

 @Override

 public void onPaint(Graphics2D g) {

  g.drawString("Status: " + status, 200, 328);

  g.drawString("y difference " + okset, 200, 300);

 }

}