package net.hamster332.schematics;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

public class SchematicsLoader {
    public static boolean placeStructure(ServerWorld world, BlockPos pos, Identifier structureId, BlockMirror mirror, BlockRotation rotation) {
        StructureTemplateManager manager = world.getStructureTemplateManager();
        Optional<StructureTemplate> optionalTemplate = manager.getTemplate(structureId);

        if (optionalTemplate.isPresent()) {
            StructureTemplate template = optionalTemplate.get();

            StructurePlacementData placementData = new StructurePlacementData()
                    .setIgnoreEntities(false)
                    .setMirror(mirror)
                    .setRotation(rotation)
                    .setPosition(pos);
            template.place(world, pos, pos, placementData, world.getRandom(), 2);

            return true;
        }
        return false;
    }
}
