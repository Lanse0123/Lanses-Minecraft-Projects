package lanse.copperworld.worldtype;

import lanse.copperworld.BlockCategories;
import lanse.copperworld.CopperWorld.BlockEdit;
import lanse.copperworld.WorldEditor;
import net.minecraft.block.*;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

import java.util.List;
import java.util.Set;

public class CopperPreset {

    public static final Set<Block> COPPER_BLOCKS = Set.of(
            Blocks.COPPER_BLOCK,
            Blocks.EXPOSED_COPPER,
            Blocks.WEATHERED_COPPER,
            Blocks.OXIDIZED_COPPER,
            Blocks.WAXED_COPPER_BLOCK,
            Blocks.WAXED_EXPOSED_COPPER,
            Blocks.WAXED_WEATHERED_COPPER,
            Blocks.WAXED_OXIDIZED_COPPER,

            Blocks.CUT_COPPER,
            Blocks.CUT_COPPER_SLAB,
            Blocks.CUT_COPPER_STAIRS,
            Blocks.EXPOSED_CUT_COPPER,
            Blocks.EXPOSED_CUT_COPPER_SLAB,
            Blocks.EXPOSED_CUT_COPPER_STAIRS,
            Blocks.WEATHERED_CUT_COPPER,
            Blocks.WEATHERED_CUT_COPPER_SLAB,
            Blocks.WEATHERED_CUT_COPPER_STAIRS,
            Blocks.OXIDIZED_CUT_COPPER,
            Blocks.OXIDIZED_CUT_COPPER_SLAB,
            Blocks.OXIDIZED_CUT_COPPER_STAIRS,
            Blocks.WAXED_CUT_COPPER,
            Blocks.WAXED_CUT_COPPER_SLAB,
            Blocks.WAXED_CUT_COPPER_STAIRS,
            Blocks.WAXED_EXPOSED_CUT_COPPER,
            Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB,
            Blocks.WAXED_EXPOSED_CUT_COPPER_STAIRS,
            Blocks.WAXED_WEATHERED_CUT_COPPER,
            Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB,
            Blocks.WAXED_WEATHERED_CUT_COPPER_STAIRS,
            Blocks.WAXED_OXIDIZED_CUT_COPPER,
            Blocks.WAXED_OXIDIZED_CUT_COPPER_SLAB,
            Blocks.WAXED_OXIDIZED_CUT_COPPER_STAIRS,

            Blocks.CHISELED_COPPER,
            Blocks.WAXED_CHISELED_COPPER,
            Blocks.EXPOSED_CHISELED_COPPER,
            Blocks.WAXED_EXPOSED_CHISELED_COPPER,
            Blocks.WEATHERED_CHISELED_COPPER,
            Blocks.WAXED_WEATHERED_CHISELED_COPPER,
            Blocks.OXIDIZED_CHISELED_COPPER,
            Blocks.WAXED_OXIDIZED_CHISELED_COPPER,

            Blocks.COPPER_GRATE,
            Blocks.WAXED_COPPER_GRATE,
            Blocks.EXPOSED_COPPER_GRATE,
            Blocks.WAXED_EXPOSED_COPPER_GRATE,
            Blocks.WEATHERED_COPPER_GRATE,
            Blocks.WAXED_WEATHERED_COPPER_GRATE,
            Blocks.OXIDIZED_COPPER_GRATE,
            Blocks.WAXED_OXIDIZED_COPPER_GRATE,

            Blocks.COPPER_BULB,
            Blocks.WAXED_COPPER_BULB,
            Blocks.EXPOSED_COPPER_BULB,
            Blocks.WAXED_EXPOSED_COPPER_BULB,
            Blocks.WEATHERED_COPPER_BULB,
            Blocks.WAXED_WEATHERED_COPPER_BULB,
            Blocks.OXIDIZED_COPPER_BULB,
            Blocks.WAXED_OXIDIZED_COPPER_BULB,

            Blocks.COPPER_DOOR,
            Blocks.WAXED_COPPER_DOOR,
            Blocks.EXPOSED_COPPER_DOOR,
            Blocks.WAXED_EXPOSED_COPPER_DOOR,
            Blocks.WEATHERED_COPPER_DOOR,
            Blocks.WAXED_WEATHERED_COPPER_DOOR,
            Blocks.OXIDIZED_COPPER_DOOR,
            Blocks.WAXED_OXIDIZED_COPPER_DOOR,

            Blocks.COPPER_TRAPDOOR,
            Blocks.WAXED_COPPER_TRAPDOOR,
            Blocks.EXPOSED_COPPER_TRAPDOOR,
            Blocks.WAXED_EXPOSED_COPPER_TRAPDOOR,
            Blocks.WEATHERED_COPPER_TRAPDOOR,
            Blocks.WAXED_WEATHERED_COPPER_TRAPDOOR,
            Blocks.OXIDIZED_COPPER_TRAPDOOR,
            Blocks.WAXED_OXIDIZED_COPPER_TRAPDOOR,

            Blocks.LIGHTNING_ROD,

            Blocks.TUFF,
            Blocks.TUFF_STAIRS,
            Blocks.TUFF_SLAB,
            Blocks.TUFF_WALL,
            Blocks.POLISHED_TUFF,
            Blocks.POLISHED_TUFF_STAIRS,
            Blocks.POLISHED_TUFF_SLAB,
            Blocks.POLISHED_TUFF_WALL,
            Blocks.TUFF_BRICKS,
            Blocks.TUFF_BRICK_STAIRS,
            Blocks.TUFF_BRICK_SLAB,
            Blocks.TUFF_BRICK_WALL,
            Blocks.CHISELED_TUFF,

            Blocks.COBWEB,
            Blocks.CHEST,
            Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE,
            Blocks.CHAIN,
            Blocks.SPAWNER,
            Blocks.TRIAL_SPAWNER,
            Blocks.VAULT
    );

    //TODO - marker to separate field

    public static void adjustColumn(ServerWorld world, int x, int y, int z, List<BlockEdit> edits) {

        BlockPos pos = new BlockPos(x, y, z);
        Chunk chunk = world.getChunk(pos);
        pos = pos.up();

        for (int i = y; i >= world.getBottomY(); i--){

            pos = pos.down();
            Block block = world.getBlockState(pos).getBlock();

            if (WorldEditor.EXCLUDED_BLOCKS.contains(block)) continue;
            if (COPPER_BLOCKS.contains(block)) continue;

            if (BlockCategories.commonWorldNaturalMaterial.contains(block)) {
                edits.add(new BlockEdit(pos, Blocks.WAXED_CUT_COPPER.getDefaultState()));

            } else if (BlockCategories.commonWorldNaturalSurface.contains(block)) {
                edits.add(new BlockEdit(pos, Blocks.OXIDIZED_CUT_COPPER.getDefaultState()));

            } else if (BlockCategories.liquids.contains(block)) {
                continue;

            } else if (BlockCategories.uncommon1.contains(block)) {
                edits.add(new BlockEdit(pos, Blocks.WAXED_EXPOSED_CUT_COPPER.getDefaultState()));

            } else if (BlockCategories.uncommon2.contains(block)) {
                edits.add(new BlockEdit(pos, Blocks.WAXED_WEATHERED_CUT_COPPER.getDefaultState()));

            } else if (BlockCategories.uncommon3.contains(block)) {
                edits.add(new BlockEdit(pos, Blocks.WAXED_OXIDIZED_CUT_COPPER.getDefaultState()));

            } else if (BlockCategories.plants.contains(block)) {
                edits.add(new BlockEdit(pos, Blocks.LIGHTNING_ROD.getDefaultState()));

            } else if (BlockCategories.leaves.contains(block)) {
                if (block instanceof LeavesBlock){
                    edits.add(new BlockEdit(pos, Blocks.WAXED_OXIDIZED_COPPER_GRATE.getDefaultState()));
                } else if (block instanceof NetherWartBlock){
                    edits.add(new BlockEdit(pos, Blocks.WAXED_COPPER_GRATE.getDefaultState()));
                } else {
                    edits.add(new BlockEdit(pos, Blocks.WAXED_EXPOSED_COPPER_GRATE.getDefaultState()));
                }

            } else if (BlockCategories.wood.contains(block)) {
                edits.add(new BlockEdit(pos, Blocks.WAXED_COPPER_BLOCK.getDefaultState()));

            } else if (BlockCategories.ores.contains(block)) {
                edits.add(new BlockEdit(pos, Blocks.RAW_COPPER_BLOCK.getDefaultState()));

            } else if (BlockCategories.colorSolidBlocks.contains(block)) {
                edits.add(new BlockEdit(pos, Blocks.WAXED_WEATHERED_CHISELED_COPPER.getDefaultState()));

            } else if (BlockCategories.slabs.contains(block)) {
                BlockState state = chunk.getBlockState(pos);
                Block tempBlock = getBaseBlockFromVariant(block, "_SLAB");

                if (BlockCategories.uncommon1.contains(tempBlock)) {
                    edits.add(new BlockEdit(pos, Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB.getStateWithProperties(state)));
                } else if (BlockCategories.uncommon2.contains(tempBlock)) {
                    edits.add(new BlockEdit(pos, Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB.getStateWithProperties(state)));
                } else if (BlockCategories.uncommon3.contains(tempBlock)) {
                    edits.add(new BlockEdit(pos, Blocks.WAXED_OXIDIZED_CUT_COPPER_SLAB.getStateWithProperties(state)));
                } else {
                    edits.add(new BlockEdit(pos, Blocks.WAXED_CUT_COPPER_SLAB.getStateWithProperties(state)));
                }

            } else if (BlockCategories.walls.contains(block)) {
                BlockState state = block.getDefaultState();
                edits.add(new BlockEdit(pos, Blocks.TUFF_BRICK_WALL.getStateWithProperties(state)));

            } else if (BlockCategories.carpets.contains(block)) {
                edits.add(new BlockEdit(pos, Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB.getDefaultState()));

            } else if (BlockCategories.glass.contains(block)) {
                if (block instanceof StainedGlassPaneBlock){
                    edits.add(new BlockEdit(pos, Blocks.IRON_BARS.getDefaultState()));
                } else {
                    edits.add(new BlockEdit(pos, Blocks.WAXED_EXPOSED_COPPER_GRATE.getDefaultState()));
                }

            } else if (BlockCategories.beds.contains(block)) {
                edits.add(new BlockEdit(pos, Blocks.OXIDIZED_COPPER.getDefaultState()));

            } else if (BlockCategories.redstone.contains(block)) {
                continue;

            } else if (BlockCategories.solidPlants.contains(block)) {
                edits.add(new BlockEdit(pos, Blocks.WAXED_WEATHERED_COPPER.getDefaultState()));

            } else if (BlockCategories.pots.contains(block)) {
                continue;

            } else if (BlockCategories.signs.contains(block)) {
                edits.add(new BlockEdit(pos, Blocks.AIR.getDefaultState()));

            } else if (BlockCategories.fences.contains(block)) {
                BlockState state = chunk.getBlockState(pos);

                if (block instanceof FenceBlock){
                    edits.add(new BlockEdit(pos, Blocks.ACACIA_FENCE.getStateWithProperties(state)));
                } else {
                    edits.add(new BlockEdit(pos, Blocks.ACACIA_FENCE_GATE.getStateWithProperties(state)));
                }

            } else if (BlockCategories.trapdoors.contains(block)) {
                edits.add(new BlockEdit(pos, Blocks.ACACIA_TRAPDOOR.getDefaultState()));

            } else if (BlockCategories.doors.contains(block)) {
                edits.add(new BlockEdit(pos, Blocks.COPPER_DOOR.getDefaultState()));

            } else if (BlockCategories.storage.contains(block)) {
                continue;

            } else if (BlockCategories.tables.contains(block)) {
                edits.add(new BlockEdit(pos, Blocks.WAXED_CHISELED_COPPER.getDefaultState()));

            } else if (BlockCategories.lights1.contains(block)) {
                edits.add(new BlockEdit(pos, Blocks.WAXED_COPPER_BULB.getDefaultState()));

            } else if (BlockCategories.lights2.contains(block)) {
                continue;

            } else if (BlockCategories.traps.contains(block)) {
                continue;

            } else if (BlockCategories.clusters.contains(block)) {
                edits.add(new BlockEdit(pos, Blocks.LIGHTNING_ROD.getDefaultState()));

            } else if (BlockCategories.structural.contains(block)) {
                continue;

            } else if (BlockCategories.coralBlocks.contains(block)) {
                BlockState state = chunk.getBlockState(pos);
                if (state.isSolidBlock(world, pos)){
                    edits.add(new BlockEdit(pos, Blocks.WAXED_COPPER_BLOCK.getDefaultState()));
                } else {
                    edits.add(new BlockEdit(pos, Blocks.LIGHTNING_ROD.getStateWithProperties(state)));
                }

            } else if (BlockCategories.stairs.contains(block)) {
                BlockState state = chunk.getBlockState(pos);
                Block tempBlock = getBaseBlockFromVariant(block, "_STAIRS");

                if (BlockCategories.uncommon1.contains(tempBlock)) {
                    edits.add(new BlockEdit(pos, Blocks.WAXED_EXPOSED_CUT_COPPER_STAIRS.getStateWithProperties(state)));
                } else if (BlockCategories.uncommon2.contains(tempBlock)) {
                    edits.add(new BlockEdit(pos, Blocks.WAXED_WEATHERED_CUT_COPPER_STAIRS.getStateWithProperties(state)));
                } else if (BlockCategories.uncommon3.contains(tempBlock)) {
                    edits.add(new BlockEdit(pos, Blocks.WAXED_OXIDIZED_CUT_COPPER_STAIRS.getStateWithProperties(state)));
                } else {
                    edits.add(new BlockEdit(pos, Blocks.WAXED_CUT_COPPER_STAIRS.getStateWithProperties(state)));
                }

            } else if (BlockCategories.birthday.contains(block)) {
                if (block instanceof CakeBlock){
                    edits.add(new BlockEdit(pos, Blocks.OXIDIZED_CUT_COPPER_SLAB.getDefaultState()));
                }

            } else if (BlockCategories.pressurePlates.contains(block)) {
                edits.add(new BlockEdit(pos, Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE.getDefaultState()));

            } else if (BlockCategories.buttons.contains(block)) {
                BlockState state = chunk.getBlockState(pos);
                edits.add(new BlockEdit(pos, Blocks.ACACIA_BUTTON.getStateWithProperties(state)));

            } else if (BlockCategories.valuables.contains(block)) {
                continue;

            } else if (BlockCategories.heads.contains(block)) {
                continue;

            }
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
