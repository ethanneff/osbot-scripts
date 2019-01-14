import org.osbot.rs07.api.model.GroundItem;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.api.filter.Filter;
import org.osbot.rs07.api.model.Item;

@ScriptManifest(author = "Noob", name = "Noob Script", version = -15.0, logo = "", info = "Does some noob shit")
public class Main extends Script {

    private enum STATE{ PICKING, BURYING }

    private STATE getState(){
        if(!getInventory().isFull()) {
        	return STATE.PICKING;
        
        } else { 
        	return STATE.BURYING;
        }
    }
    
    @Override
    public int onLoop() throws InterruptedException {
        switch (getState()){
            case PICKING: pick();
                    break;
            case BURYING: bury();
                break;
        }
        return 0;
    }


	private void pick(){
        getGroundItems().closest(new Filter<GroundItem>() {
            @Override
            public boolean match(GroundItem groundItem) {

                if(groundItem.hasAction("Bury")) return true;
                return false;
            }
        }).interact();

        try {
            sleep(random(1000, 1200));
        } catch(InterruptedException e){
            log(e);
        }
    }

    private void bury(){
        for(Item item : getInventory().getItems()){
            if(item.hasAction("Bury")) item.interact("Bury");
        }

        try{
            sleep(random(1000, 1200));
        } catch(InterruptedException e){
            log(e);
        }
    }
}