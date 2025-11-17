import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.PreparedStatement;

public class WeatherDatabase {
    private String dbPath;

    public WeatherDatabase(String dbPath) {
        this.dbPath = dbPath;
    }

    public void initDatabase() throws Exception {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath)) {
            String sql = "CREATE TABLE IF NOT EXISTS weather (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "time TEXT NOT NULL," +
                    "temperature REAL NOT NULL," +
                    "pressure REAL NOT NULL," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")";
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
            }
        }
    }

    public void insertWeatherData(OpenMeteoResponse response) throws Exception {
        if (response == null) {
            return;
        }

        OpenMeteoResponse.WeatherData[] hourly = response.getHourly();
        if (hourly == null || hourly.length == 0) {
            return;
        }

        String sql = "INSERT INTO weather (time, temperature, pressure) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (OpenMeteoResponse.WeatherData data : hourly) {
                pstmt.setString(1, data.time);
                pstmt.setDouble(2, data.temperature);
                pstmt.setDouble(3, data.pressure);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            System.out.println("Inserted " + hourly.length + " weather records into database.");
        }
    }

    public void printAllWeatherData() throws Exception {
        String sql = "SELECT time, temperature, pressure FROM weather ORDER BY time";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
             Statement stmt = conn.createStatement();
             var rs = stmt.executeQuery(sql)) {

            System.out.println("\n--- Weather Data in Database ---");
            while (rs.next()) {
                String time = rs.getString("time");
                double temp = rs.getDouble("temperature");
                double pressure = rs.getDouble("pressure");
                System.out.println(time + ": " + temp + "°C, " + pressure + " hPa");
            }
        }
    }

    public void clearWeatherData() throws Exception {
        String sql = "DELETE FROM weather";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Cleared all weather records from database.");
        }
    }
}
