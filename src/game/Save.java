package game;

import java.io.File;
import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import characters.Player;

public class Save {
    private final File saveDir = new File("saves/");
    private final ObjectMapper mapper = new ObjectMapper();
    
    // Autosave thread management
    private Thread autosaveThread;
    private volatile boolean autosaveRunning = false;
    private GameData currentGameData;

    public Save() {
        if (!saveDir.exists()) {
            saveDir.mkdirs();
        }
        this.currentGameData = null;
    }

    // Save GameData to a JSON file with timestamp
    public boolean saveGame(GameData gameData) {
        long timestamp = System.currentTimeMillis() / 1000;
        String filename = "game_" + timestamp + ".save";
        return saveGame(gameData, filename);
    }

   
    // Save GameData to a JSON file with specified filename
    public boolean saveGame(GameData gameData, String filename) {
        try {
            ObjectNode root = serializeGameData(gameData);
            File out = new File(saveDir, filename);
            mapper.writerWithDefaultPrettyPrinter().writeValue(out, root);
            System.out.println("Saved game to " + out.getPath());
            return true;
        } catch (IOException ex) {
            System.err.println("Failed to save game: " + ex.getMessage());
            return false;
        }
    }

    public GameData loadLatestGame() {
        File[] files = saveDir.listFiles((d, name) -> name.endsWith(".save"));
        if (files == null || files.length == 0) {
            return null;
        }

        // Find newest by lastModified
        File newest = files[0];
        for (File f : files) {
            if (f.lastModified() > newest.lastModified()) newest = f;
        }

        try {
            ObjectNode root = (ObjectNode) mapper.readTree(newest);
            GameData gameData = deserializeGameData(root);
            System.out.println("Loaded game from " + newest.getPath());
            return gameData;
        } catch (IOException ex) {
            System.err.println("Failed to load game: " + ex.getMessage());
            return null;
        }
    }

    public boolean hasSaves() {
        File[] files = saveDir.listFiles((d, name) -> name.endsWith(".save"));
        return files != null && files.length > 0;
    }

    public synchronized void startAutosave(GameData gameData) {
        stopAutosave();
        this.currentGameData = gameData;
        autosaveRunning = true;
        autosaveThread = new Thread(() -> {
            while (autosaveRunning) {
                try {
                    Thread.sleep(5 * 60 * 1000); // 5 minutes
                    if (autosaveRunning && currentGameData != null) {
                        saveGame(currentGameData);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.out.println("Autosave error: " + e.getMessage());
                }
            }
        }, "AutosaveThread");
        autosaveThread.setDaemon(true);
        autosaveThread.start();
        // System.out.println("Autosave started (5-minute interval)");
    }

    public synchronized void stopAutosave() {
        autosaveRunning = false;
        if (autosaveThread != null && autosaveThread.isAlive()) {
            autosaveThread.interrupt();
            try {
                autosaveThread.join(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        autosaveThread = null;
        // System.out.println("Autosave stopped");
    }

    public synchronized void updateGameData(GameData gameData) {
        this.currentGameData = gameData;
    }

    private ObjectNode serializeGameData(GameData data) {
        ObjectNode root = mapper.createObjectNode();

        // Game state
        root.put("gameTimestamp", data.gameTimestamp);
        root.put("baseDemandFactor", data.baseDemandFactor);

        // Player
        if (data.playerData != null) {
            root.set("player", serializePlayer(data.playerData));
        }

        // Manager
        if (data.managerName != null) {
            ObjectNode managerNode = mapper.createObjectNode();
            managerNode.put("name", data.managerName);
            managerNode.put("age", data.managerAge);
            root.set("manager", managerNode);
        }

        // NPCs
        ObjectNode npcsNode = mapper.createObjectNode();
        for (var entry : data.npcsData.entrySet()) {
            ObjectNode npcNode = mapper.createObjectNode();
            npcNode.put("name", entry.getValue().name);
            npcNode.put("role", entry.getValue().role);
            npcNode.put("age", entry.getValue().age);
            npcsNode.set(entry.getKey(), npcNode);
        }
        root.set("npcs", npcsNode);

        // Demand history
        if (data.demandHistory != null) {
            var arr = mapper.createArrayNode();
            for (Double d : data.demandHistory) {
                arr.add(d);
            }
            root.set("demandHistory", arr);
        }

        return root;
    }

    /**
     * Serialize Player to JSON ObjectNode
     */
    private ObjectNode serializePlayer(Player p) {
        ObjectNode playerNode = mapper.createObjectNode();
        playerNode.put("name", p.getName());
        playerNode.put("health", p.getHealth());
        playerNode.put("stamina", p.getStamina());
        playerNode.put("money", p.getMoney());
        playerNode.put("shiftsWorked", p.getShiftsWorked());
        playerNode.put("reputation", p.getReputation());

        // Inventory
        ObjectNode inventoryNode = mapper.createObjectNode();
        for (var entry : p.getInventory().entrySet()) {
            inventoryNode.put(entry.getKey(), entry.getValue());
        }
        playerNode.set("inventory", inventoryNode);

        return playerNode;
    }

    /**
     * Deserialize JSON ObjectNode to GameData
     */
    private GameData deserializeGameData(ObjectNode root) {
        GameData gameData = new GameData();

        gameData.gameTimestamp = root.path("gameTimestamp").asLong(System.currentTimeMillis());
        gameData.baseDemandFactor = root.path("baseDemandFactor").asDouble(1.0);

        // Player
        ObjectNode playerNode = (ObjectNode) root.path("player");
        if (playerNode != null) {
            gameData.playerData = deserializePlayer(playerNode);
        }

        // Manager
        ObjectNode managerNode = (ObjectNode) root.path("manager");
        if (managerNode != null && managerNode.has("name")) {
            gameData.managerName = managerNode.path("name").asText();
            gameData.managerAge = managerNode.path("age").asInt(30);
        }

        // NPCs
        ObjectNode npcsNode = (ObjectNode) root.path("npcs");
        if (npcsNode != null) {
            npcsNode.fieldNames().forEachRemaining(key -> {
                ObjectNode npcNode = (ObjectNode) npcsNode.path(key);
                GameData.NPCData npcData = new GameData.NPCData();
                npcData.name = npcNode.path("name").asText();
                npcData.role = npcNode.path("role").asText();
                npcData.age = npcNode.path("age").asInt(30);
                gameData.npcsData.put(key, npcData);
            });
        }

        // Demand history
        if (root.has("demandHistory") && root.path("demandHistory").isArray()) {
            var arr = root.withArray("demandHistory");
            gameData.demandHistory = new java.util.ArrayList<>();
            for (int i = 0; i < arr.size(); i++) {
                gameData.demandHistory.add(arr.get(i).asDouble(0.0));
            }
        }

        return gameData;
    }

    /**
     * Deserialize JSON ObjectNode to Player
     */
    private Player deserializePlayer(ObjectNode root) {
        String name = root.path("name").asText("Player");
        int health = root.path("health").asInt(100);
        int stamina = root.path("stamina").asInt(100);
        double money = root.path("money").asDouble(0.0);
        int shiftsWorked = root.path("shiftsWorked").asInt(0);

        Player p = Player.fromJson(name, health, stamina, money, shiftsWorked);

        // Reputation
        int reputation = root.path("reputation").asInt(80);
        p.setReputation(reputation);

        // Inventory
        ObjectNode inventoryNode = (ObjectNode) root.path("inventory");
        if (inventoryNode != null) {
            inventoryNode.fieldNames().forEachRemaining(key -> {
                int count = inventoryNode.path(key).asInt(0);
                p.addItem(key, count);
            });
        }

        return p;
    }
}
