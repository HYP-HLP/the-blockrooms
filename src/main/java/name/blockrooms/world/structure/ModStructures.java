package name.blockrooms.world.structure;

import com.mojang.serialization.MapCodec;
import name.blockrooms.Blockrooms;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModStructures {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, Blockrooms.MODID);
    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_PIECE, Blockrooms.MODID);

    public static final DeferredHolder<StructureType<?>, StructureType<BlockLevel0OutpostStructure>> BMEG_OUTPOST_TYPE =
            STRUCTURE_TYPES.register("bmeg_outpost", () -> structureType(BlockLevel0OutpostStructure.CODEC));
    public static final DeferredHolder<StructurePieceType, StructurePieceType> BMEG_OUTPOST_PIECE_TYPE =
            STRUCTURE_PIECE_TYPES.register("bmeg_outpost_piece", () -> (StructurePieceType.ContextlessType) BlockLevel0OutpostPiece::new);

    private static <S extends Structure> StructureType<S> structureType(MapCodec<S> codec) {
        return () -> codec;
    }

    public static void register(IEventBus eventBus) {
        STRUCTURE_TYPES.register(eventBus);
        STRUCTURE_PIECE_TYPES.register(eventBus);
    }
}
