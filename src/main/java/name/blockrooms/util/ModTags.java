package name.blockrooms.util;

import name.blockrooms.Blockrooms;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;


public class ModTags {
    public static final TagKey<Level> IN_BLOCKROOMS = TagKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath(Blockrooms.MODID, "in_blockrooms"));
}
