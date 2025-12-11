package characters;

public class Manager extends NPC {
    public Manager(String name, int age) {
        super(name, "Manager", age);
    }

    public void evaluatePerformance(Player p) {
        int reputation = p.getReputation();
        if (reputation > 80) {
            System.out.println(getName() + " praises " + p.getName() + " for excellent performance!");
        } else if (reputation > 50) {
            System.out.println(getName() + " gives " + p.getName() + " some constructive feedback.");
        } else {
            System.out.println(getName() + " is disappointed with " + p.getName() + "'s performance.");
        }
    }

    public void approveRaise(Player p, double amount) {
        int reputation = p.getReputation();
        if (reputation > 70) {
            p.changeMoney(amount);
            System.out.println(getName() + " approves a raise of $" + amount + " for " + p.getName() + "!");
        } else {
            System.out.println(getName() + " denies the raise request from " + p.getName() + ".");
        }
    }
}
