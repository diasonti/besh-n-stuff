package com.diasonti.beshnstuff;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(BeshNStuffMod.MODID)
public class BeshNStuffMod {
    public static final String MODID = "beshnstuff";
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    public static final DeferredItem<Item> HORSE_MEAT = ITEMS.registerSimpleItem(
            "horse_meat",
            new Item.Properties()
                    .food(new FoodProperties.Builder()
                            .nutrition(3)
                            .saturationModifier(0.3f)
                            .build()));
    public static final DeferredItem<Item> COOKED_HORSE_MEAT = ITEMS.registerSimpleItem(
            "cooked_horse_meat",
            new Item.Properties()
                    .food(new FoodProperties.Builder()
                            .nutrition(8)
                            .saturationModifier(0.8f)
                            .build()));
    public static final DeferredItem<Item> KAZY = ITEMS.registerSimpleItem(
            "kazy",
            new Item.Properties()
                    .food(new FoodProperties.Builder()
                            .nutrition(8)
                            .saturationModifier(0.3f)
                            .build()));
    public static final DeferredItem<Item> KAZY_SLICE = ITEMS.registerSimpleItem(
            "kazy_slice",
            new Item.Properties()
                    .food(new FoodProperties.Builder()
                            .nutrition(12)
                            .saturationModifier(0.8f)
                            .build()));
    public static final DeferredItem<Item> BAURSAK = ITEMS.registerSimpleItem(
            "baursak",
            new Item.Properties()
                    .food(new FoodProperties.Builder()
                            .nutrition(1)
                            .saturationModifier(0.6f)
                            .fast()
                            .build()));

    public BeshNStuffMod(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        modEventBus.addListener(this::addCreative);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
            event.accept(HORSE_MEAT);
            event.accept(COOKED_HORSE_MEAT);
            event.accept(KAZY);
            event.accept(KAZY_SLICE);
            event.accept(BAURSAK);
        }
    }
}
