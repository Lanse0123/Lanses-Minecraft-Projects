package lanse.copperworld.worldtype;

import lanse.copperworld.BlockCategories;
import lanse.copperworld.WorldEditor;
import net.minecraft.block.*;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

import java.util.Set;

public class ExamplePreset {

    public static final Set<Block> PRESET_SPECIFFIC_EXCLUDED_BLOCKS = Set.of(
            Blocks.COPPER_BLOCK,
            Blocks.EXPOSED_COPPER,
            Blocks.WEATHERED_COPPER
            //etc
    );

    public static void adjustColumn(ServerWorld world, int x, int y, int z) {

        BlockPos pos = new BlockPos(x, y, z);
        Chunk chunk = world.getChunk(pos);
        pos = pos.up();

        for (int i = y; i >= world.getBottomY(); i--){

            pos = pos.down();
            Block block = world.getBlockState(pos).getBlock();

            if (WorldEditor.EXCLUDED_BLOCKS.contains(block)) continue;
            if (PRESET_SPECIFFIC_EXCLUDED_BLOCKS.contains(block)) continue;

            if (BlockCategories.commonWorldNaturalMaterial.contains(block)) {
                chunk.setBlockState(pos, Blocks.WAXED_CUT_COPPER.getDefaultState());

            } else if (BlockCategories.commonWorldNaturalSurface.contains(block)) {
                chunk.setBlockState(pos, Blocks.OXIDIZED_CUT_COPPER.getDefaultState());

            } else if (BlockCategories.liquids.contains(block)) {
                continue;

            } else if (BlockCategories.uncommon1.contains(block)) {
                chunk.setBlockState(pos, Blocks.WAXED_EXPOSED_CUT_COPPER.getDefaultState());

            } else if (BlockCategories.uncommon2.contains(block)) {
                chunk.setBlockState(pos, Blocks.WAXED_WEATHERED_CUT_COPPER.getDefaultState());

            } else if (BlockCategories.uncommon3.contains(block)) {
                chunk.setBlockState(pos, Blocks.WAXED_OXIDIZED_CUT_COPPER.getDefaultState());

            } else if (BlockCategories.plants.contains(block)) {
                chunk.setBlockState(pos, Blocks.LIGHTNING_ROD.getDefaultState());

            } else if (BlockCategories.leaves.contains(block)) {
                if (block instanceof LeavesBlock){
                    chunk.setBlockState(pos, Blocks.WAXED_OXIDIZED_COPPER_GRATE.getDefaultState());
                } else if (block instanceof NetherWartBlock){
                    chunk.setBlockState(pos, Blocks.WAXED_COPPER_GRATE.getDefaultState());
                } else {
                    chunk.setBlockState(pos, Blocks.WAXED_EXPOSED_COPPER_GRATE.getDefaultState());
                }

            } else if (BlockCategories.wood.contains(block)) {
                chunk.setBlockState(pos, Blocks.WAXED_CHISELED_COPPER.getDefaultState());

            } else if (BlockCategories.ores.contains(block)) {
                chunk.setBlockState(pos, Blocks.RAW_COPPER_BLOCK.getDefaultState());

            } else if (BlockCategories.colorSolidBlocks.contains(block)) {
                chunk.setBlockState(pos, Blocks.WAXED_WEATHERED_CHISELED_COPPER.getDefaultState());

            } else if (BlockCategories.slabs.contains(block)) {
                BlockState state = block.getDefaultState();
                Block tempBlock = getBaseBlockFromVariant(block, "_SLAB");

                if (BlockCategories.uncommon1.contains(tempBlock)) {
                    chunk.setBlockState(pos, Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB.getStateWithProperties(state));
                } else if (BlockCategories.uncommon2.contains(tempBlock)) {
                    chunk.setBlockState(pos, Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB.getStateWithProperties(state));
                } else if (BlockCategories.uncommon3.contains(tempBlock)) {
                    chunk.setBlockState(pos, Blocks.WAXED_OXIDIZED_CUT_COPPER_SLAB.getStateWithProperties(state));
                } else {
                    chunk.setBlockState(pos, Blocks.WAXED_CUT_COPPER_SLAB.getStateWithProperties(state));
                }

            } else if (BlockCategories.walls.contains(block)) {
                BlockState state = block.getDefaultState();
                chunk.setBlockState(pos, Blocks.TUFF_BRICK_WALL.getStateWithProperties(state));

            } else if (BlockCategories.carpets.contains(block)) {
                chunk.setBlockState(pos, Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB.getDefaultState());

            } else if (BlockCategories.glass.contains(block)) {
                if (block instanceof StainedGlassPaneBlock){
                    chunk.setBlockState(pos, Blocks.IRON_BARS.getDefaultState());
                } else {
                    chunk.setBlockState(pos, Blocks.WAXED_EXPOSED_COPPER_GRATE.getDefaultState());
                }

            } else if (BlockCategories.beds.contains(block)) {
                chunk.setBlockState(pos, Blocks.OXIDIZED_COPPER.getDefaultState());

            } else if (BlockCategories.redstone.contains(block)) {
                continue;

            } else if (BlockCategories.solidPlants.contains(block)) {
                chunk.setBlockState(pos, Blocks.WAXED_WEATHERED_COPPER.getDefaultState());

            } else if (BlockCategories.pots.contains(block)) {
                continue;

            } else if (BlockCategories.signs.contains(block)) {
                chunk.setBlockState(pos, Blocks.AIR.getDefaultState());

            } else if (BlockCategories.fences.contains(block)) {
                if (block instanceof FenceBlock){
                    chunk.setBlockState(pos, Blocks.ACACIA_FENCE.getDefaultState());
                } else {
                    chunk.setBlockState(pos, Blocks.ACACIA_FENCE_GATE.getDefaultState());
                }

            } else if (BlockCategories.trapdoors.contains(block)) {
                chunk.setBlockState(pos, Blocks.ACACIA_TRAPDOOR.getDefaultState());

            } else if (BlockCategories.doors.contains(block)) {
                chunk.setBlockState(pos, Blocks.COPPER_DOOR.getDefaultState());

            } else if (BlockCategories.storage.contains(block)) {
                continue;

            } else if (BlockCategories.tables.contains(block)) {
                chunk.setBlockState(pos, Blocks.WAXED_CHISELED_COPPER.getDefaultState());

            } else if (BlockCategories.lights1.contains(block)) {
                chunk.setBlockState(pos, Blocks.WAXED_COPPER_BULB.getDefaultState());

            } else if (BlockCategories.lights2.contains(block)) {
                continue;

            } else if (BlockCategories.traps.contains(block)) {
                continue;

            } else if (BlockCategories.clusters.contains(block)) {
                chunk.setBlockState(pos, Blocks.LIGHTNING_ROD.getDefaultState());

            } else if (BlockCategories.structural.contains(block)) {
                continue;

            } else if (BlockCategories.coralBlocks.contains(block)) {
                BlockState state = block.getDefaultState();
                if (state.isSolidBlock(world, pos)){
                    chunk.setBlockState(pos, Blocks.WAXED_COPPER_BLOCK.getDefaultState());
                } else {
                    chunk.setBlockState(pos, Blocks.LIGHTNING_ROD.getStateWithProperties(state));
                }

            } else if (BlockCategories.stairs.contains(block)) {
                BlockState state = block.getDefaultState();
                Block tempBlock = getBaseBlockFromVariant(block, "_STAIRS");

                if (BlockCategories.uncommon1.contains(tempBlock)) {
                    chunk.setBlockState(pos, Blocks.WAXED_EXPOSED_CUT_COPPER_STAIRS.getStateWithProperties(state));
                } else if (BlockCategories.uncommon2.contains(tempBlock)) {
                    chunk.setBlockState(pos, Blocks.WAXED_WEATHERED_CUT_COPPER_STAIRS.getStateWithProperties(state));
                } else if (BlockCategories.uncommon3.contains(tempBlock)) {
                    chunk.setBlockState(pos, Blocks.WAXED_OXIDIZED_CUT_COPPER_STAIRS.getStateWithProperties(state));
                } else {
                    chunk.setBlockState(pos, Blocks.WAXED_CUT_COPPER_STAIRS.getStateWithProperties(state));
                }

            } else if (BlockCategories.birthday.contains(block)) {
                if (block instanceof CakeBlock){
                    chunk.setBlockState(pos, Blocks.OXIDIZED_CUT_COPPER_SLAB.getDefaultState());
                }

            } else if (BlockCategories.pressurePlates.contains(block)) {
                chunk.setBlockState(pos, Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE.getDefaultState());

            } else if (BlockCategories.buttons.contains(block)) {
                BlockState state = block.getDefaultState();
                chunk.setBlockState(pos, Blocks.ACACIA_BUTTON.getStateWithProperties(state));

            } else if (BlockCategories.valuables.contains(block)) {
                continue;

            } else if (BlockCategories.heads.contains(block)) {
                continue;

            }
            world.getChunkManager().markForUpdate(pos); // Triggers client resend
        }
    }

    public static Block getBaseBlockFromVariant(Block originalBlock, String variantToRemove) {
        //converts a slab or stair or something into full block
        Identifier originalId = Registries.BLOCK.getId(originalBlock);
        String namespace = originalId.getNamespace();
        String path = originalId.getPath();

        if (!path.contains(variantToRemove)) return originalBlock;

        String basePath = path.replace(variantToRemove, "");
        Identifier baseId = Identifier.of(namespace, basePath);

        return Registries.BLOCK.get(baseId);
    }
}
