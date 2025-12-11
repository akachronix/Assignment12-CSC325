package game;

import java.util.Map;
import java.util.HashMap;

import characters.Player;
import items.*;

// This class is AI generated, I forgot to save the response. Wrote an initial version of Shop.java
// and then asked the AI to complete it. This is the result. My prompt went along the lines of:
// "Create a simple Shop class that is modular and uses the Item base class to manage items for sale."
// Pretty decent result!
public class Shop {
    private Map<String, Item> inventory;

    public Shop() {
        inventory = new HashMap<>();
        inventory.put("energy drink", new EnergyDrink());
        inventory.put("meth", new Meth());
        inventory.put("name tag", new NameTag());
        // Add more items here as they are created as Item subclasses
    }

    public Map<String, Double> getPriceList() {
        // Returns a map of item names to prices for display/compatibility
        Map<String, Double> prices = new HashMap<>();
        for (String name : inventory.keySet())
            prices.put(name, inventory.get(name).getPrice());
        
        return prices;
    }

    public String[] getItemNames() {
        return inventory.keySet().toArray(new String[0]);
    }

    public boolean buy(Player p, String item) {
        Item shopItem = inventory.get(item.trim().toLowerCase());

        if (shopItem == null) 
            return false;
        if (p.getMoney() < shopItem.getPrice()) 
            return false;

        p.changeMoney(-shopItem.getPrice());
        p.addItem(item, 1);

        return true;
    }
}
