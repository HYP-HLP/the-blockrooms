package name.blockrooms;

import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Client-side configuration for The Blockrooms.
 * Registered in {@link BlockroomsClient} and shown in the auto-generated
 * in-game config screen (Mods -> Config -> The Blockrooms -> Client).
 */
public class ClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    /** Master switch for the bottom-right level info panel. */
    public static final ModConfigSpec.BooleanValue LEVEL_INFO_ENABLED = BUILDER
            .comment(Component.translatable("blockrooms.configuration.level_info_enabled_comment").getString())
            .define("level_info_enabled", true);

    /** Master switch for the top-right survival difficulty panel. */
    public static final ModConfigSpec.BooleanValue LEVEL_DIFFICULTY_ENABLED = BUILDER
            .comment(Component.translatable("blockrooms.configuration.level_difficulty_enabled_comment").getString())
            .define("level_difficulty_enabled", true);

    /** Global default: ticks between each character of the typewriter effect. */
    public static final ModConfigSpec.IntValue LEVEL_INFO_TYPE_SPEED = BUILDER
            .comment(Component.translatable("blockrooms.configuration.level_info_type_speed_comment").getString())
            .defineInRange("level_info_type_speed", 2, 1, 20);

    /** Global default: ticks to wait between lines. */
    public static final ModConfigSpec.IntValue LEVEL_INFO_LINE_DELAY = BUILDER
            .comment(Component.translatable("blockrooms.configuration.level_info_line_delay_comment").getString())
            .defineInRange("level_info_line_delay", 12, 0, 200);

    /** Global default: ticks the text stays visible after the last line. */
    public static final ModConfigSpec.IntValue LEVEL_INFO_HOLD_TICKS = BUILDER
            .comment(Component.translatable("blockrooms.configuration.level_info_hold_ticks_comment").getString())
            .defineInRange("level_info_hold_ticks", 80, 0, 600);

    /** Maximum width of the level info panel, in GUI pixels; the panel shrinks to fit its content. */
    public static final ModConfigSpec.IntValue LEVEL_INFO_PANEL_WIDTH = BUILDER
            .comment(Component.translatable("blockrooms.configuration.level_info_panel_width_comment").getString())
            .defineInRange("level_info_panel_width", 200, 100, 400);

    /** Maximum rows visible in the level info panel; content up to this size fits dynamically, longer content scrolls. */
    public static final ModConfigSpec.IntValue LEVEL_INFO_PANEL_ROWS = BUILDER
            .comment(Component.translatable("blockrooms.configuration.level_info_panel_rows_comment").getString())
            .defineInRange("level_info_panel_rows", 6, 2, 12);

    static final ModConfigSpec SPEC = BUILDER.build();
}
