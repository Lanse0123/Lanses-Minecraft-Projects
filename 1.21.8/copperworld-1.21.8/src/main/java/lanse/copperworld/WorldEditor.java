package lanse.copperworld;

import lanse.copperworld.worldtype.CopperPreset;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.IceBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class WorldEditor {

    public enum WorldPreset {
        COPPER
    }
    //TODO - once I add more of these, save it
    public static WorldPreset worldPreset = WorldPreset.COPPER;

    public static final Set<Block> EXCLUDED_BLOCKS = Set.of(
            Blocks.END_PORTAL, Blocks.END_PORTAL_FRAME, Blocks.END_GATEWAY, Blocks.NETHER_PORTAL, Blocks.OBSIDIAN, Blocks.BEDROCK,
            Blocks.AIR, Blocks.CAVE_AIR, Blocks.VOID_AIR
    );

    public static void adjustColumn(ServerWorld world, int x, int z, List<CopperWorld.BlockEdit> edits) {

        int y = world.getTopY(Heightmap.Type.WORLD_SURFACE, new BlockPos(x, 0, z));

        switch (worldPreset){
            case COPPER -> CopperPreset.adjustColumn(world, x, y, z, edits);
        }
    }
}