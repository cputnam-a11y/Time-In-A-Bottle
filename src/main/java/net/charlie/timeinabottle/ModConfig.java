package net.charlie.timeinabottle;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;
public class ModConfig {
    public static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("time-in-a-bottle.json");
    public static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    private int[] speedLevels;
    private int duration;
    private int timeSecond;
    private long maxTime;
    public ModConfig() {
        try {
            this.load();
        } catch (IOException fileNotFound) {
            try {
                JsonWriter writer = new JsonWriter(new FileWriter(PATH.toString()));
                writer.setIndent("  ");
                GSON.toJson(this.addDefault(new JsonObject()), writer);
                writer.close();
                this.load();
            } catch (IOException failedToOpenFile) {
                TimeInABottle.LOGGER.error("Something went wrong while creating the config!", failedToOpenFile);
            }
        }

    }
    public void load() throws IOException {
        JsonObject object = JsonParser.parseString(new String(Files.readAllBytes(PATH))).getAsJsonObject();
        this.load(object);
    }
    private void load(JsonObject obj) {
        JsonArray speedLevelElement = obj.get("speed-levels").getAsJsonArray();
        this.speedLevels = new int[speedLevelElement.size()];

        for(int i = 0; i < speedLevelElement.size(); ++i) {
            this.speedLevels[i] = speedLevelElement.get(i).getAsInt();
        }

        this.duration = obj.get("duration").getAsInt();
        this.timeSecond = obj.get("time-second").getAsInt();
        this.maxTime = obj.get("max-time").getAsLong();
    }
    private JsonObject addDefault(JsonObject obj) {
        JsonArray arr = new JsonArray();

        for(int i = 1; i <= 32; i *= 2) {
            arr.add(i);
        }

        obj.add("speed-levels", arr);
        obj.addProperty("duration", 30);
        obj.addProperty("time-second", 20);
        obj.addProperty("max-time", 622080000);
        return obj;
    }
    public int[] getSpeedLevels() {
        return this.speedLevels;
    }

    public int getDuration() {
        return this.duration;
    }

    public int getTimeSecond() {
        return this.timeSecond;
    }

    public long getMaxTime() {
        return this.maxTime;
    }
}
