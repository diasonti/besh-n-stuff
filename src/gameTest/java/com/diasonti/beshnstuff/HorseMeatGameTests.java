package com.diasonti.beshnstuff;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder
@PrefixGameTestTemplate(false)
public final class HorseMeatGameTests {
    private HorseMeatGameTests() {
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty")
    public static void horseMeatUsesCorrectCommonFoodTags(GameTestHelper helper) {
        var rawMeatTag = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "foods/raw_meat"));
        var cookedMeatTag = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "foods/cooked_meat"));
        var rawBeefTag = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "foods/raw_beef"));
        var cookedBeefTag = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "foods/cooked_beef"));
        var rawHorseMeat = new ItemStack(BeshNStuffMod.HORSE_MEAT.get());
        var cookedHorseMeat = new ItemStack(BeshNStuffMod.COOKED_HORSE_MEAT.get());

        helper.assertTrue(rawHorseMeat.is(rawMeatTag), "Raw horse meat should be recognized as generic raw meat without Farmer's Delight");
        helper.assertTrue(!rawHorseMeat.is(cookedMeatTag), "Raw horse meat should not be recognized as cooked meat");
        helper.assertTrue(cookedHorseMeat.is(cookedMeatTag), "Cooked horse meat should be recognized as generic cooked meat without Farmer's Delight");
        helper.assertTrue(!cookedHorseMeat.is(rawMeatTag), "Cooked horse meat should not be recognized as raw meat");
        helper.assertTrue(!rawHorseMeat.is(rawBeefTag) && !cookedHorseMeat.is(cookedBeefTag),
                "Horse meat should not be recognized as beef");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty")
    public static void horseDropsHorseMeat(GameTestHelper helper) {
        Horse horse = helper.spawn(EntityType.HORSE, new BlockPos(0, 1, 0));
        horse.hurt(helper.getLevel().damageSources().generic(), Float.MAX_VALUE);
        helper.succeedWhen(() -> helper.assertItemEntityPresent(BeshNStuffMod.HORSE_MEAT.get(), new BlockPos(0, 1, 0), 4.0));
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 240)
    public static void furnaceCooksHorseMeat(GameTestHelper helper) {
        BlockPos furnacePos = new BlockPos(0, 1, 0);
        helper.setBlock(furnacePos, Blocks.FURNACE);
        AbstractFurnaceBlockEntity furnace = helper.getBlockEntity(furnacePos);
        furnace.setItem(0, new ItemStack(BeshNStuffMod.HORSE_MEAT.get()));
        furnace.setItem(1, new ItemStack(Items.COAL));

        helper.succeedOnTickWhen(200, () -> helper.assertContainerContains(furnacePos, BeshNStuffMod.COOKED_HORSE_MEAT.get()));
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 140)
    public static void smokerCooksHorseMeat(GameTestHelper helper) {
        BlockPos smokerPos = new BlockPos(0, 1, 0);
        helper.setBlock(smokerPos, Blocks.SMOKER);
        AbstractFurnaceBlockEntity smoker = helper.getBlockEntity(smokerPos);
        smoker.setItem(0, new ItemStack(BeshNStuffMod.HORSE_MEAT.get()));
        smoker.setItem(1, new ItemStack(Items.COAL));

        helper.succeedOnTickWhen(100, () -> helper.assertContainerContains(smokerPos, BeshNStuffMod.COOKED_HORSE_MEAT.get()));
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty")
    public static void campfireRecipeProducesOneCookedHorseMeat(GameTestHelper helper) {
        var input = new SingleRecipeInput(new ItemStack(BeshNStuffMod.HORSE_MEAT.get()));
        var recipe = helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.CAMPFIRE_COOKING, input, helper.getLevel()).orElseThrow();
        var recipeId = ResourceLocation.fromNamespaceAndPath(BeshNStuffMod.MODID, "cooked_horse_meat_from_campfire_cooking");
        helper.assertTrue(recipe.id().equals(recipeId), "Horse meat should match its campfire cooking recipe without Farmer's Delight");
        ItemStack result = recipe.value().assemble(input, helper.getLevel().registryAccess());
        helper.assertTrue(result.is(BeshNStuffMod.COOKED_HORSE_MEAT.get()) && result.getCount() == 1,
                "One raw horse meat should produce exactly one cooked horse meat");
        helper.assertTrue(recipe.value().getCookingTime() == 600, "Campfire cooking should take 600 ticks");
        helper.assertTrue(!recipe.value().matches(new SingleRecipeInput(new ItemStack(BeshNStuffMod.COOKED_HORSE_MEAT.get())), helper.getLevel()),
                "Cooked horse meat should not match the campfire recipe");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 640)
    public static void campfireCooksHorseMeat(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 2, 1);
        CampfireBlockEntity campfire = prepareCampfire(helper, pos, true);

        // Check immediately after cooking, before the randomly ejected item can leave the search area.
        helper.succeedWhen(() -> {
            helper.assertTrue(campfire.getItems().stream().allMatch(ItemStack::isEmpty), "Cooking should consume the raw horse meat");
            helper.assertItemEntityCountIs(BeshNStuffMod.COOKED_HORSE_MEAT.get(), pos, 3.0, 1);
        });
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 640)
    public static void unlitCampfireDoesNotCookHorseMeat(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 2, 1);
        CampfireBlockEntity campfire = prepareCampfire(helper, pos, false);

        helper.runAtTickTime(620, () -> {
            helper.assertItemEntityNotPresent(BeshNStuffMod.COOKED_HORSE_MEAT.get(), pos, 3.0);
            ItemStack remaining = campfire.getItems().get(0);
            helper.assertTrue(remaining.is(BeshNStuffMod.HORSE_MEAT.get()) && remaining.getCount() == 1,
                    "An unlit campfire should retain the raw horse meat");
            helper.succeed();
        });
    }

    private static CampfireBlockEntity prepareCampfire(GameTestHelper helper, BlockPos pos, boolean lit) {
        helper.setBlock(pos, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, lit));
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(BeshNStuffMod.HORSE_MEAT.get(), 2));
        helper.useBlock(pos, player);
        ItemStack remaining = player.getMainHandItem();
        helper.assertTrue(remaining.is(BeshNStuffMod.HORSE_MEAT.get()) && remaining.getCount() == 1,
                "Placing horse meat on a campfire should take exactly one item");
        return helper.getBlockEntity(pos);
    }
}
