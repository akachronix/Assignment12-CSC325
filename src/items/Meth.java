package items;

import characters.Player;

public class Meth extends Item {
    public Meth() {
        super("meth", 50.0);
    }

    @Override
    public boolean use(Player player) {
        if (player.removeItem(name, 1)) {
            player.changeReputation(-30);
            player.addStamina(100);
            return true;
        }

        return false;
    }
    
}
