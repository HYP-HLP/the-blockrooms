package name.blockrooms.util;

import name.blockrooms.Blockrooms;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.Set;

public class ModLevels {
    private static final Set<ResourceKey<Level>> BLOCKLEVELS = new HashSet<>();
    public static boolean isInBlockrooms(ResourceKey<Level> levelResourceKey){
        return BLOCKLEVELS.stream().anyMatch(levelResourceKey::equals);
    }
    public static final ResourceKey<Level> BLOCKLEVEL_0 = level("blocklevel0");
    public static final ResourceKey<Level> BLOCKLEVEL_4 = level("blocklevel4");
    public static final ResourceKey<Level> GALLERY = level("the_gallery");
    private static ResourceKey<Level> level(String key) {
        var a = ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath(Blockrooms.MODID, key));
        BLOCKLEVELS.add(a);
        return a;
    }
}
