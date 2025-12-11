package characters;

import java.util.HashMap;
import java.util.Map;

public class Player extends Character {
    private int health;
    private int stamina; // decreases when working, may be refilled by energy drinks
    private double money;
    private Map<String, Integer> inventory; // item & count
    int reputation;
    private int shiftsWorked;

    public Player(String name) {
        super(name, "player", 20);
        this.health = 100;
        this.stamina = 100;
        this.money = 200.0; // starting wages/savings
        this.inventory = new HashMap<>();
        this.reputation = 80;
        this.shiftsWorked = 0;
    }

    // Construct a Player with explicit state (used for loading from save)
    public static Player fromJson(String name, int health, int stamina, double money, int shiftsWorked) {
        Player p = new Player(name);
        p.health = health;
        p.stamina = stamina;
        p.money = money;
        p.shiftsWorked = shiftsWorked;
        return p;
    }

    // Basic getters
    public int getHealth() { return health; }
    public int getStamina() { return stamina; }
    public double getMoney() { return money; }
    public int getShiftsWorked() { return shiftsWorked; }

    // Inventory & economy
    public void addItem(String item, int count) {
        inventory.put(item, inventory.getOrDefault(item, 0) + count);
    }

    public boolean removeItem(String item, int count) {
        int have = inventory.getOrDefault(item, 0);
        if (have < count) return false;
        if (have == count) inventory.remove(item);
        else inventory.put(item, have - count);
        return true;
    }

    public Map<String, Integer> getInventory() { return new HashMap<>(inventory); }

    public void changeMoney(double delta) { this.money = Math.max(0.0, this.money + delta); }
    
    // Clamps stamina to [0-100] range; prevents overcharging or negative values
    public void addStamina(int amount) { this.stamina = Math.min(100, Math.max(0, this.stamina + amount)); }

    // Reputation mechanics
    public void setReputation(int value) {
        reputation = Math.max(0, Math.min(100, value));
    }

    public int getReputation() {
        return reputation;
    }

    public void changeReputation(int delta) {
        setReputation(reputation + delta);
    }

    // Handles player changes of working a shift while the Game class manages other game logic
    public double workShift(Manager managerOnDuty, double demandFactor) {
        // Calculate earnings based on reputation and stamina
        if (stamina < 10) {
            // Exhausted workers earn less; penalty is reputation/1.85 instead of full reputation
            double earned = (reputation / 1.85 /* lower earnings due to exhaustion */) * demandFactor;
            changeMoney(earned);
            stamina = Math.max(0, stamina - 5); // use max to avoid negative stamina
            shiftsWorked++;
            return earned;
        }

        double base = reputation; // rep is a nice round number that works well as base pay














        double earned = base * demandFactor;
        changeMoney(earned);
    
        stamina = Math.max(0, stamina - 5); // Base stamina loss per shift
        if (demandFactor > 1.2) stamina = Math.max(0, stamina - 8); // High demand = harder work, extra stamina drain
        if (demandFactor < 0.8) stamina = Math.min(100, stamina + 12); // Low demand = easier work, stamina recovery

        // if demand is low, reputation drops due to poor performance
        if (demandFactor < 0.9) {
            changeReputation(-2);
            // very low demand hurts reputation more
            if (demandFactor < 0.7) changeReputation(-3);
        } 
        else if (demandFactor > 1.1) changeReputation(2);

        // Good performance increases reputation with coworkers
        changeReputation(1);
        shiftsWorked++;
        return earned;
    }

    @Override
    public String toString() {
        return  "Name: " + name + '\n' +
                "Health: " + health + '\n' +
                "Stamina: " + stamina + '\n' +
                "Money: $" + String.format("%.2f", money) + '\n' +
                "Shifts Worked: " + shiftsWorked + '\n' +
                "Inventory: " + inventory + '\n' +
                "Reputation: " + reputation + '\n';
    }
}
