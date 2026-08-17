package name.blockrooms.client.hud;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import name.blockrooms.Blockrooms;
import name.blockrooms.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.level.Level;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;

/**
 * Loads the level info panel content from data-driven, lang-style JSON files,
 * so that no level text is hardcoded in Java. Files live at
 * {@code assets/blockrooms/level_info/<language>.json} (e.g. {@code en_us.json},
 * {@code zh_cn.json}) and each contains one entry per level, keyed by the level
 * path (e.g. {@code blocklevel0}). The file matching the client's language is
 * used, falling back to {@code en_us.json} when missing. Players can override
 * or add entries with a resource pack.
 *
 * <p>Supported schema:</p>
 * <pre>
 * {
 *   "blocklevel0": {
 *     "title": "Level 0",              // optional heading, null/blank to skip
 *     "title_color": "#e8e8e8",        // optional, hex: #rrggbb / 0xrrggbb / rrggbb
 *     "lines": [                        // optional body lines
 *       { "text": "A line", "color": "#b0b0b0" },
 *       "a plain string line also works"
 *     ],
 *     "type_speed": 2,                 // optional, ticks per char (1-20), default from config
 *     "line_delay": 12,                // optional, ticks between lines (0-200), default from config
 *     "hold_ticks": 80,                // optional, ticks after last line (0-600), default from config
 *     "difficulty": {                   // optional survival difficulty (top-right panel)
 *       "title": "Survival Difficulty",
 *       "title_color": "#ffffff",      // optional title color
 *       "safe": "Safe",
 *       "security": "Secure",
 *       "entity": "Minimal entity count",
 *       "safe_color": "#4caf50",       // optional item colors
 *       "security_color": "#ffc107",
 *       "entity_color": "#f44336"
 *     }
 *   },
 *   "blocklevel1": { ... }
 * }
 * </pre>
 *
 * <p>All fields are optional. A missing or unparsable file simply means
 * nothing is shown for that dimension.</p>
 */
public final class LevelInfoManager {
    /** Fallback language file used when the client's language has no file. */
    public static final String DEFAULT_LANGUAGE = "en_us";

    private LevelInfoManager() {
    }

    /**
     * Returns the level info for the given dimension in the client's language
     * (falling back to {@value #DEFAULT_LANGUAGE}), if an entry exists and
     * parses successfully.
     */
    public static Optional<LevelInfoData> get(ResourceKey<Level> dimension) {
        String clientLang = Minecraft.getInstance().options.languageCode;
        Set<String> candidates = new LinkedHashSet<>();
        if (clientLang != null && !clientLang.isBlank()) {
            candidates.add(clientLang);
        }
        candidates.add(DEFAULT_LANGUAGE);

        String levelPath = dimension.identifier().getPath();
        for (String lang : candidates) {
            Identifier file = Identifier.fromNamespaceAndPath(Blockrooms.MODID, "level_info/" + lang + ".json");
            Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(file);
            if (resource.isEmpty()) {
                continue;
            }
            try (InputStreamReader reader = new InputStreamReader(resource.get().open(), StandardCharsets.UTF_8)) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                if (root.has(levelPath) && root.get(levelPath).isJsonObject()) {
                    return parse(root.getAsJsonObject(levelPath));
                }
            } catch (Exception e) {
                Blockrooms.LOGGER.warn("Failed to parse level info {}: {}", file, e.toString());
            }
        }
        return Optional.empty();
    }

    private static Optional<LevelInfoData> parse(JsonObject root) {
        String title = root.has("title") && root.get("title").isJsonPrimitive()
                ? root.get("title").getAsString() : null;
        int titleColor = root.has("title_color")
                ? parseColor(root.get("title_color").getAsString(), LevelInfoData.DEFAULT_TITLE_COLOR)
                : LevelInfoData.DEFAULT_TITLE_COLOR;

        List<LevelInfoData.Line> lines = new ArrayList<>();
        if (root.has("lines") && root.get("lines").isJsonArray()) {
            for (JsonElement element : root.getAsJsonArray("lines")) {
                if (element.isJsonPrimitive()) {
                    lines.add(new LevelInfoData.Line(element.getAsString(), LevelInfoData.DEFAULT_LINE_COLOR));
                } else if (element.isJsonObject()) {
                    JsonObject line = element.getAsJsonObject();
                    if (!line.has("text") || !line.get("text").isJsonPrimitive()) {
                        continue;
                    }
                    String text = line.get("text").getAsString();
                    int color = line.has("color")
                            ? parseColor(line.get("color").getAsString(), LevelInfoData.DEFAULT_LINE_COLOR)
                            : LevelInfoData.DEFAULT_LINE_COLOR;
                    lines.add(new LevelInfoData.Line(text, color));
                }
            }
        }

        LevelInfoData.Difficulty difficulty = parseDifficulty(
                root.has("difficulty") ? root.get("difficulty") : null);

        LevelInfoData data = new LevelInfoData(
                title,
                titleColor,
                List.copyOf(lines),
                root.has("type_speed") ? clamp(root.get("type_speed").getAsInt(), 1, 20)
                        : ClientConfig.LEVEL_INFO_TYPE_SPEED.get(),
                root.has("line_delay") ? clamp(root.get("line_delay").getAsInt(), 0, 200)
                        : ClientConfig.LEVEL_INFO_LINE_DELAY.get(),
                root.has("hold_ticks") ? clamp(root.get("hold_ticks").getAsInt(), 0, 600)
                        : ClientConfig.LEVEL_INFO_HOLD_TICKS.get(),
                difficulty);
        return data.isEmpty() ? Optional.empty() : Optional.of(data);
    }

    private static LevelInfoData.Difficulty parseDifficulty(JsonElement element) {
        if (element == null || !element.isJsonObject()) {
            return null;
        }
        JsonObject o = element.getAsJsonObject();
        String title = stringOrNull(o, "title");
        int titleColor = o.has("title_color")
                ? parseColor(o.get("title_color").getAsString(), LevelInfoData.DEFAULT_TITLE_COLOR)
                : LevelInfoData.DEFAULT_TITLE_COLOR;
        String safe = stringOrNull(o, "safe");
        String security = stringOrNull(o, "security");
        String entity = stringOrNull(o, "entity");
        LevelInfoData.Difficulty difficulty = new LevelInfoData.Difficulty(
                title, titleColor, safe, security, entity,
                o.has("safe_color") ? parseColor(o.get("safe_color").getAsString(), LevelInfoData.DEFAULT_SAFE_COLOR)
                        : LevelInfoData.DEFAULT_SAFE_COLOR,
                o.has("security_color") ? parseColor(o.get("security_color").getAsString(), LevelInfoData.DEFAULT_SECURITY_COLOR)
                        : LevelInfoData.DEFAULT_SECURITY_COLOR,
                o.has("entity_color") ? parseColor(o.get("entity_color").getAsString(), LevelInfoData.DEFAULT_ENTITY_COLOR)
                        : LevelInfoData.DEFAULT_ENTITY_COLOR);
        return difficulty.isEmpty() ? null : difficulty;
    }

    private static String stringOrNull(JsonObject o, String key) {
        return o.has(key) && o.get(key).isJsonPrimitive() ? o.get(key).getAsString() : null;
    }

    /** Parses "#rrggbb", "0xrrggbb" or "rrggbb" into an opaque ARGB int. */
    private static int parseColor(String value, int fallback) {
        try {
            String hex = value.trim();
            if (hex.startsWith("#")) {
                hex = hex.substring(1);
            } else if (hex.startsWith("0x") || hex.startsWith("0X")) {
                hex = hex.substring(2);
            }
            return (int) (0xFF000000L | (Long.parseLong(hex, 16) & 0xFFFFFFL));
        } catch (Exception e) {
            Blockrooms.LOGGER.warn("Invalid color '{}' in level info, using fallback", value);
            return fallback;
        }
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
