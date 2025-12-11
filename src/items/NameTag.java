package items;

import java.security.cert.PolicyQualifierInfo;

import characters.Player;

public class NameTag extends Item {
    public NameTag() {
        super("name tag", 5.00);
    }

    @Override
    public boolean use(Player player) {
        if (player.removeItem(name, 1)) {
            player.changeReputation(10);
            return true;
        }
        return false;
    }
    
}
