package items;

import characters.Player;

/**
 * Energy Drink item - restores stamina when used.
 */
public class EnergyDrink extends Item {
    public EnergyDrink() {
        super("energy drink", 3.50);
    }

    @Override
    public boolean use(Player player) {
        if (player.removeItem(name, 1)) {
            player.addStamina(40);
            return true;
        }
        return false;
    }
}
