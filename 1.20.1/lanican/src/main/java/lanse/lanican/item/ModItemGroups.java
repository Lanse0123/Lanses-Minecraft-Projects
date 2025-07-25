package lanse.lanican.item;

import lanse.lanican.Lanican;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemGroups {
    public static final ItemGroup LANSE_GROUP = Registry.register(Registries.ITEM_GROUP,
            new Identifier(Lanican.MOD_ID, "lanse_items"),
            FabricItemGroup.builder().displayName(Text.translatable("lanse_items"))
                    .icon(() -> new ItemStack(ModItems.RUBY)).entries((displayContext, entries) -> {

                        entries.add(ModItems.HOLLOW_PURPLE);
                        entries.add(ModItems.HOLLOW_JUDGEMENT);
                        entries.add(ModItems.KIRBY_WAND);

                        entries.add(ModItems.RUBY);
                        entries.add(ModItems.RAW_RUBY);

                        entries.add(Items.DIAMOND);

                    }).build());


    public static void registerItemGroups() {
        Lanican.LOGGER.info("Registering Item Groups for " + Lanican.MOD_ID);
    }
}