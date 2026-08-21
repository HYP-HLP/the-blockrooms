package name.blockrooms.entity.secret;

import com.mojang.authlib.GameProfile;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public class PlayerPuppet extends Player {

    public PlayerPuppet(Level level, GameProfile gameProfile) {
        super(level, gameProfile);
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public @Nullable GameType gameMode() {
        return GameType.SURVIVAL;
    }
}
