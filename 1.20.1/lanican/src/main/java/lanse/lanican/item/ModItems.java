package lanse.lanican.item;

import lanse.lanican.Lanican;
import lanse.lanican.custom.item.HollowJudgementItem;
import lanse.lanican.custom.item.HollowPurpleItem;
import lanse.lanican.custom.item.KirbyWandItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {
    public static final Item RUBY = registerItem("ruby", new Item(new FabricItemSettings()));
    public static final Item RAW_RUBY = registerItem("raw_ruby", new Item(new FabricItemSettings()));
    public static final Item HOLLOW_PURPLE = registerItem("hollow_purple",
            new HollowPurpleItem(new FabricItemSettings().maxDamage(1)));
    public static final Item HOLLOW_JUDGEMENT = registerItem("hollow_judgement",
            new HollowJudgementItem(new FabricItemSettings().maxDamage(156))); //TODO - add cool down
    public static final Item KIRBY_WAND = registerItem("kirby_wand",
            new KirbyWandItem(new FabricItemSettings().maxDamage(1)));

    private static void addItemsToIngredientItemGroup(FabricItemGroupEntries entries) {
        entries.add(RUBY);
        entries.add(RAW_RUBY);
    }

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(Lanican.MOD_ID, name), item);
    }

    public static void registerModItems() {
        Lanican.LOGGER.info("Registering Mod Items for " + Lanican.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(ModItems::addItemsToIngredientItemGroup);
    }
}