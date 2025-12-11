package game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import characters.*;
import namegenerator.*;
import weather.*;
import items.*;

public class Game {
    private Save save;
    private Weather weatherAPI;
    private NameGenerator nameGenerator;
    private Player player;
    private Manager manager;
    private HashMap<String, NPC> npcs;
    private double baseDemandFactor = 1.0;
    private ArrayList<Double> demandHistory = new ArrayList<>();

    private void initWeatherAPI() {
        try {
            weatherAPI = new Weather(40.7128, -74.0060); // Start with NY city coords
            OpenMeteoResponse resp = weatherAPI.getLatestResponse();
            
            // debug info
            // System.out.println("Current weather: " + resp);
        } catch (Exception e) {
            System.out.println("Failed to initialize weather API: " + e.getMessage());
        }
    }

    private void initNameGeneratorAPI() {
        try {
            nameGenerator = new NameGenerator();
            nameGenerator.getNewResponse();
        } catch (Exception e) {
            System.out.println("Failed to initialize name generator API: " + e.getMessage());
        }

        // for debugging, print all generated names
        // for (NameAPIResponse.NameData nd : nameGenerator.getLatestResponse().getGeneratedNames()) { System.out.println(nd); }
    }

    // Randomize weather using random coordinates
    private void randomizeWeather() {
        double lat = -45 + Math.random() * 90;
        double lon = -90 + Math.random() * 180;
        weatherAPI.setCoordinates(lat, lon);

        try {
            OpenMeteoResponse resp = weatherAPI.getNewResponse();
            // debug info
            // System.out.println("New location: (" + String.format("%.4f", lat) + ", " + String.format("%.4f", lon) + ")");
            // System.out.println("Current weather: " + resp);
        } catch (Exception e) {
            System.out.println("Failed to randomize weather: " + e.getMessage());
        }
    }

    private double calcDemandFactor() {
        // Multiplies base demand by weather modifiers; temperature affects customer traffic, pressure indicates weather quality
        double demandFactor = baseDemandFactor;
        OpenMeteoResponse resp = weatherAPI.getLatestResponse();
        if (resp != null && resp.getHourly() != null && resp.getHourly().length > 0) {
            double temp = resp.getHourly()[0].temperature;
            double pressure = resp.getHourly()[0].pressure;

            // hot increases demand slightly
            if (temp > 80) demandFactor *= 1.3;
            else if (temp < 40) demandFactor *= 1.15;

            // low pressure = bad weather = less customers
            if (pressure < 1004) demandFactor *= 0.9;

            // high pressure = good weather = more customers
            if (pressure > 1010 && pressure < 1013) demandFactor *= 1.1;

            // super good weather = lots of customers
            if (pressure >= 1013) demandFactor *= 1.3;
        }

        return demandFactor;
    }

    // Load previous game state or create fresh player/manager/NPCs with defaults
    private void initializeGameState() {
        GameData gameData = save.loadLatestGame();
        
        if (gameData != null) {
            // Load from save file
            this.player = gameData.playerData;
            this.baseDemandFactor = gameData.baseDemandFactor;
            this.demandHistory = new ArrayList<>(gameData.demandHistory);
            
            // Restore manager
            if (gameData.managerName != null) {
                this.manager = new Manager(gameData.managerName, gameData.managerAge);
            } else {
                this.manager = initManagerNPC();
            }
            
            // Restore NPCs
            this.npcs = new HashMap<>();
            if (!gameData.npcsData.isEmpty())
            for (var entry : gameData.npcsData.entrySet()) {
                NPC npc = new NPC(entry.getValue().name, entry.getValue().role, entry.getValue().age);
                npcs.put(entry.getKey(), npc);
            }
            
            System.out.println("Game state loaded from save.");
        } else {
            // Initialize with defaults
            this.player = new Player("Charlie Dotter");
            this.manager = initManagerNPC();
            this.npcs = new HashMap<>();
            this.baseDemandFactor = 1.0;
            this.demandHistory = new ArrayList<>();
            
            System.out.println("No save found. Starting fresh game.");
        }
    }

    // Generate manager NPC from name API; falls back to 'Boss' if API unavailable
    private Manager initManagerNPC() {
        try {
            if (nameGenerator == null || nameGenerator.getLatestResponse() == null) {
                nameGenerator.getNewResponse();
            }
            
            NameAPIResponse.NameData[] generatedName = nameGenerator.getLatestResponse().getGeneratedNames();
            NameAPIResponse.NameData mgrBio = generatedName[generatedName.length - 1];
            return new Manager(mgrBio.getFullName(), mgrBio.age);
        } catch (Exception e) {
            System.out.println("Failed to generate manager: " + e.getMessage());
            return new Manager("Boss", 40); // Fallback manager
        }
    }

    // Initializes APIs, loads/creates game state, starts autosave (5-min interval), and runs main game loop
    public Game(String[] args) {

        // Initialize
        Scanner in = new Scanner(System.in);

        initWeatherAPI();
        initNameGeneratorAPI();

        save = new Save();
        
        // Load game state from save or initialize defaults
        initializeGameState();
        
        // Start autosave thread
        GameData initialGameData = new GameData(player, manager, npcs, baseDemandFactor, demandHistory);
        save.startAutosave(initialGameData);

        Shop shop = new Shop();


        /* ---------------------------------------- */
        System.out.println("Welcome to the supermarket — you are Charlie Dotter, a bagger.");

        // main loop
        boolean running = true;
        while (running) {
            System.out.println("\nChoose an action: work / shop / use / interact / stats / quit");
            System.out.print("> ");
            String cmd = in.nextLine().trim().toLowerCase();
            System.out.println();

            switch (cmd) {
                case "stats": {
                    System.out.println(player);
                    break;
                }



                case "":
                case "work": {
                    double demandFactor = calcDemandFactor();

                    System.out.println("Day " + (player.getShiftsWorked() + 1) + "...");
                    System.out.println("Working a shift under manager " + manager.getName() + "...");
                    System.out.println("Current temperature and pressure affect demand. Temperature: " + 
                        String.format("%.1f", weatherAPI.getLatestResponse().getHourly()[0].temperature) + "°F, " +
                        "Pressure: " + String.format("%.1f", weatherAPI.getLatestResponse().getHourly()[0].pressure) + " hPa"
                    );

                    // If tired, warn player
                    if (player.getStamina() <= 35) System.out.println("Warning: You are very tired (stamina: " + player.getStamina() + "). Consider using an Energy Drink before working.");
                    else if (player.getStamina() <= 65) System.out.println("You feel somewhat tired (stamina: " + player.getStamina() + "). Working may be less effective.");
                    else if (player.getStamina() < 80) System.out.println("You feel a bit groggy (stamina: " + player.getStamina() + ").");
                    else if (player.getStamina() > 90) System.out.println("You feel energetic and ready to work! (stamina: " + player.getStamina() + ")");

                    // base case
                    else System.out.println("Time to work. (stamina: " + player.getStamina() + ")");

                    System.out.println("Current demand factor: " + String.format("%.2f", demandFactor));
                    System.out.println("Current reputation: " + player.getReputation());
                    
                    
                    double earned = player.workShift(manager, demandFactor);
                    System.out.println("You worked a shift and earned $" + String.format("%.2f", earned));

                    demandHistory.add(demandFactor);    // log demand factor for the day for performance tracking
                    
                    int daysWorked = player.getShiftsWorked();
                    if (daysWorked % 10 == 0) {
                        // perfomance review every 30 days
                        int rep = player.getReputation();
                        System.out.println("\n--- Performance Review ---");
                        if (rep >= 80) {
                            System.out.println("Excellent work! Your reputation with " + manager.getName() + " has earned you a $50 bonus!");
                            player.changeMoney(50.0);
                        } else if (rep >= 60) {
                            System.out.println("Good job! Your reputation with " + manager.getName() + " remains solid.");
                        } else if (rep >= 20) {
                            System.out.println("You need to improve your performance. Your reputation with " + manager.getName() + " is slipping. " + manager.getName() + 
                                " suggests using Energy Drinks to maintain stamina during shifts.");
                        } else {
                            System.out.println("Your performance is unsatisfactory. " + manager.getName() + " is considering termination.");

                            if (demandFactor > 1.2) {
                                System.out.println("However, due to high demand recently, " + manager.getName() + " is willing to give you another chance. +20 rep");
                                player.changeReputation(20); // small rep boost
                                continue;
                            }

                            if (player.getMoney() >= 20.0 && demandFactor <= 1.0) {
                                System.out.println("Low demand. You pay a $20 bribe to " + manager.getName() + " to keep your job.");
                                player.changeMoney(-20.0);
                                player.changeReputation(10); // small rep boost
                            } else {
                                System.out.println("You cannot afford to bribe " + manager.getName() + ". You have been terminated.");
                                running = false;
                                break;
                            }
                        }
                    }
                    System.out.println("Day " + player.getShiftsWorked() + " is now complete.");

                    randomizeWeather();                 // new weather for next shift
                    break;
                }



                case "shop": {
                    // Enumerate shop items
                    System.out.println("Shop items:");
                    shop.getPriceList().forEach((k,v) -> System.out.println(k + " - $" + String.format("%.2f", v)));

                    // Prompt for item to buy
                    System.out.print("Enter item name to buy: ");
                    String item = in.nextLine().trim().toLowerCase();

                    // Attempt purchase
                    if (shop.buy(player, item)) System.out.println("Bought " + item);
                    else System.out.println("Can't buy " + item + ". Check funds or spelling.");

                    break;
                }



                case "use": {
                    System.out.println("Use what? (e.g. ");
                    for (String itemName : shop.getItemNames()) {
                        System.out.print(itemName + ", ");
                    }
                    System.out.print(")\n> "); // end parentheses and prompt caret

                    String item = in.nextLine().trim().toLowerCase();

                    if (item.equals("energy drink")) {
                        if (new EnergyDrink().use(player)) {
                            System.out.println("You used an Energy Drink. Stamina restored to " + player.getStamina() + ".");
                        } else {
                            System.out.println("You don't have an Energy Drink.");
                        }
                    } else if (item.equals("meth")) {
                        if (new Meth().use(player)) {
                            System.out.println("You used some Meth. Reputation decreased to " + player.getReputation() + ".");
                            System.out.println("Be careful, using Meth can have consequences.");
                            System.out.println("Your stamina is now " + player.getStamina() + ".");
                        } else {
                            System.out.println("You don't have any Meth.");
                        }
                    } else if (item.equals("name tag")) {
                        if (new NameTag().use(player)) {
                            System.out.println("You used a Name Tag. Your name is now: " + player.getName() + ".");
                            System.out.println("Your coworkers recognize you better now. Reputation increased to " + player.getReputation() + ".");
                        } else {
                            System.out.println("You don't have a Name Tag.");
                        }
                    } else { 
                        System.out.println("Unknown item.");
                    }

                    break;
                }
                


                case "quit": {
                    // Kill the background thread and save final state
                    GameData gameData = new GameData(player, manager, npcs, baseDemandFactor, demandHistory);
                    save.updateGameData(gameData);
                    save.stopAutosave();
                    
                    // Final save
                    save.saveGame(gameData);

                    // Exit main loop
                    running = false;
                    break;
                }

                // if no recognized command, fail gracefully and try again
                default: { System.out.println("Unknown command."); }
            }
        }

        System.out.println("Goodbye — final status:\n" + player);
        in.close();
    }
}
