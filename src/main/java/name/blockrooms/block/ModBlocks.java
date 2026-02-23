package name.blockrooms.block;

import name.blockrooms.Blockrooms;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Blockrooms.MODID);
    public static final DeferredBlock<Block> HEATED_IRON_BLOCK = BLOCKS.registerSimpleBlock(
            "heated_iron_block",
            properties -> properties.instrument(NoteBlockInstrument.IRON_XYLOPHONE)
                    .mapColor(MapColor.FIRE)
                    .requiresCorrectToolForDrops()
                    .strength(5.0F, 6.0F)
                    .sound(SoundType.IRON)
    );

    public static void register(IEventBus eventBus) { BLOCKS.register(eventBus); }
}
