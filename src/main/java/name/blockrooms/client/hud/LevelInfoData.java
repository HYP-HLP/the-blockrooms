package name.blockrooms.client.hud;

import java.util.List;
public record LevelInfoData(
        String title,
        int titleColor,
        List<Line> lines,
        int typeSpeed,
        int lineDelay,
        int holdTicks,
        Difficulty difficulty) {

    public record Line(String text, int color) {
    }
    public record Difficulty(
            String title,
            int titleColor,
            String safe,
            String security,
            String entity,
            int safeColor,
            int securityColor,
            int entityColor) {

        public boolean isEmpty() {
            return (title == null || title.isBlank())
                    && safe == null && security == null && entity == null;
        }
    }
    public static final int DEFAULT_TITLE_COLOR = 0xFFFFFFFF;
    public static final int DEFAULT_LINE_COLOR = 0xFFA8A8A8;

    public static final int DEFAULT_SAFE_COLOR = 0xFF7ECB20;
    public static final int DEFAULT_SECURITY_COLOR = 0xFFFFC107;
    public static final int DEFAULT_ENTITY_COLOR = 0xFFF44336;

    public boolean hasTitle() {
        return title != null && !title.isBlank();
    }

    public boolean isEmpty() {
        return !hasTitle() && lines.isEmpty() && (difficulty == null || difficulty.isEmpty());
    }
}
