package game;

import java.util.HashMap;
import java.util.ArrayList;

import characters.*;

// GameData is a comprehensive data transfer object that captures the complete state
// of a Game instance for persistence to disk. It includes all game fields and their
// current state.

public class GameData {
    // Player state
    public Player playerData;
    
    // Manager state
    public String managerName;
    public int managerAge;
    
    // NPCs collection (map of NPC name to NPC data)
    public HashMap<String, NPCData> npcsData;
    
    // Game configuration and state
    public double baseDemandFactor;
    public long gameTimestamp;  // When the game was saved

    // Daily demand factor history (one entry per day/shift)
    public ArrayList<Double> demandHistory;
    
    // Constructor - initializes empty collections
    public GameData() {
        this.playerData = null;
        this.npcsData = new HashMap<>();
        this.baseDemandFactor = 1.0;
        this.gameTimestamp = System.currentTimeMillis();
        this.demandHistory = new java.util.ArrayList<>();
        this.managerName = null;
        this.managerAge = 0;
    }
    
    // Construct GameData from current game state
    public GameData(Player player, Manager manager, HashMap<String, NPC> npcs, double baseDemandFactor, ArrayList<Double> demandHistory) {
        this.playerData = player;
        
        if (manager != null) {
            this.managerName = manager.getName();
            this.managerAge = manager.getAge();
        }
        
        this.npcsData = new HashMap<>();
        if (npcs != null) {
            for (HashMap.Entry<String, NPC> entry : npcs.entrySet()) {
                this.npcsData.put(entry.getKey(), new NPCData(entry.getValue()));
            }
        }
        
        this.baseDemandFactor = baseDemandFactor;
        this.gameTimestamp = System.currentTimeMillis();
        this.demandHistory = new java.util.ArrayList<>();
        if (demandHistory != null) this.demandHistory.addAll(demandHistory);
    }
    
    // Nested data class for NPC state
    public static class NPCData {
        public String name;
        public String role;
        public int age;
        
        public NPCData() {}
        
        public NPCData(NPC npc) {
            this.name = npc.getName();
            this.role = npc.getRole();
            this.age = npc.getAge();
        }
    }
}
