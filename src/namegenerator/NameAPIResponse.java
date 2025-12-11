package namegenerator;

import java.util.ArrayList;
import java.util.Arrays;

// Jackson JSON library (provided by maven)
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class NameAPIResponse {
    public static class NameData {
        public final String title;
        public final String first_name;
        public final String last_name;
        public final int age;
        public final String dob;

        public NameData(String title, String first_name, String last_name, int age, String dob) {
            this.title = title;
            this.first_name = first_name;
            this.last_name = last_name;
            this.age = age;
            this.dob = dob;
        }

        public String getFullName() {
            return String.format("%s %s %s", title, first_name, last_name);
        }

        public String getFirstAndLastName() {
            return String.format("%s %s", first_name, last_name);
        }

        public String getDOB() {
            return dob.substring(0, 10);
        }

        @Override
        public String toString() {
            return String.format("%s, age: %d, dob: %s", getFullName(), age, getDOB());
        }
    }

    private ArrayList<NameData> generatedNames;

    public NameAPIResponse(String json) {
        if (json == null) {
            return;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);
            JsonNode resultsNode = root.path("results");
            if (resultsNode.isArray()) { // validation
                generatedNames = new ArrayList<NameData>();
                JsonNode currentNameNode = resultsNode.get(0).path("name");
                JsonNode currentDobNode = resultsNode.get(0).path("dob");
                String json_Title = currentNameNode.path("title").asText();
                String json_First = currentNameNode.path("first").asText();
                String json_Last = currentNameNode.path("last").asText();
                int json_Age = currentDobNode.path("age").asInt();
                String json_Dob = currentDobNode.path("date").asText();

                generatedNames.add(new NameData(json_Title, json_First, json_Last, json_Age, json_Dob));
            }

        } catch (Exception e) {
            generatedNames = null;
        }
    }

    public NameData[] getGeneratedNames() {
        return generatedNames.toArray(new NameData[generatedNames.size()]);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (NameData data : generatedNames) {
            sb.append(data.toString()).append("\n");
        }
        return sb.toString();
    }
}