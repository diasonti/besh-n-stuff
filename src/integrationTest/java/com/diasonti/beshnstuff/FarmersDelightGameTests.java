package com.diasonti.beshnstuff;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;
import vectorwing.farmersdelight.common.registry.ModRecipeTypes;

@GameTestHolder
@PrefixGameTestTemplate(false)
public final class FarmersDelightGameTests {
    private static final ResourceLocation RECIPE_ID = ResourceLocation.fromNamespaceAndPath(
            BeshNStuffMod.MODID, "compat/farmersdelight/baursak_from_dough");
    private static final ResourceLocation KAZY_RECIPE_ID = ResourceLocation.fromNamespaceAndPath(
            BeshNStuffMod.MODID, "compat/farmersdelight/kazy_slice_from_cooking");
    private static final ResourceLocation HORSE_MEAT_RECIPE_ID = ResourceLocation.fromNamespaceAndPath(
            BeshNStuffMod.MODID, "cooked_horse_meat_from_campfire_cooking");
    private static final ResourceLocation DOUGH_ID = ResourceLocation.fromNamespaceAndPath("farmersdelight", "wheat_dough");
    private static final ResourceLocation SKILLET_ID = ResourceLocation.fromNamespaceAndPath("farmersdelight", "skillet");
    private static final ResourceLocation COOKING_POT_ID = ResourceLocation.fromNamespaceAndPath("farmersdelight", "cooking_pot");
    private static final ResourceLocation CABBAGE_ID = ResourceLocation.fromNamespaceAndPath("farmersdelight", "cabbage");
    private static final ResourceLocation CABBAGE_ROLL_ID = ResourceLocation.fromNamespaceAndPath("farmersdelight", "cabbage_rolls");

    private FarmersDelightGameTests() {
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty")
    public static void wheatDoughCooksIntoTwoBaursak(GameTestHelper helper) {
        var recipes = helper.getLevel().getRecipeManager();
        var recipe = recipes.byKey(RECIPE_ID).orElseThrow();
        helper.assertTrue(recipe.value() instanceof CampfireCookingRecipe,
                "The dough recipe should use campfire cooking for skillet support");
        var cooking = (CampfireCookingRecipe) recipe.value();
        var input = new SingleRecipeInput(new ItemStack(BuiltInRegistries.ITEM.getOptional(DOUGH_ID).orElseThrow()));
        var matched = recipes.getRecipeFor(RecipeType.CAMPFIRE_COOKING, input, helper.getLevel()).orElseThrow();
        helper.assertTrue(matched.id().equals(RECIPE_ID), "Wheat dough should match the baursak cooking recipe");
        ItemStack result = cooking.assemble(input, helper.getLevel().registryAccess());
        helper.assertTrue(result.is(BeshNStuffMod.BAURSAK.get()) && result.getCount() == 2,
                "One wheat dough should produce exactly two baursak");
        helper.assertTrue(cooking.getCookingTime() == 600, "The base campfire cooking time should be 600 ticks");
        helper.assertTrue(!cooking.matches(new SingleRecipeInput(new ItemStack(Items.WHEAT)), helper.getLevel()),
                "Raw wheat should not replace dough in the cooking recipe");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty")
    public static void horseMeatCooksIntoOneCookedHorseMeat(GameTestHelper helper) {
        var input = new SingleRecipeInput(new ItemStack(BeshNStuffMod.HORSE_MEAT.get()));
        var recipe = helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.CAMPFIRE_COOKING, input, helper.getLevel()).orElseThrow();
        helper.assertTrue(recipe.id().equals(HORSE_MEAT_RECIPE_ID), "Horse meat should match its campfire recipe for skillet support");
        ItemStack result = recipe.value().assemble(input, helper.getLevel().registryAccess());
        helper.assertTrue(result.is(BeshNStuffMod.COOKED_HORSE_MEAT.get()) && result.getCount() == 1,
                "One raw horse meat should produce exactly one cooked horse meat with Farmer's Delight installed");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 160)
    public static void cookingPotCooksHorseMeatIntoCabbageRoll(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 2, 1);
        helper.setBlock(pos.below(), Blocks.CAMPFIRE);
        helper.setBlock(pos, BuiltInRegistries.BLOCK.getOptional(COOKING_POT_ID).orElseThrow());
        CookingPotBlockEntity pot = helper.getBlockEntity(pos);
        pot.getInventory().setStackInSlot(0, new ItemStack(BeshNStuffMod.HORSE_MEAT.get()));
        pot.getInventory().setStackInSlot(1, new ItemStack(BuiltInRegistries.ITEM.getOptional(CABBAGE_ID).orElseThrow()));

        helper.runAtTickTime(140, () -> {
            ItemStack output = pot.getInventory().getStackInSlot(CookingPotBlockEntity.OUTPUT_SLOT);
            helper.assertTrue(output.is(BuiltInRegistries.ITEM.getOptional(CABBAGE_ROLL_ID).orElseThrow()) && output.getCount() == 1,
                    "Raw horse meat and cabbage should produce one Farmer's Delight cabbage roll");
            helper.assertTrue(pot.getInventory().getStackInSlot(0).isEmpty() && pot.getInventory().getStackInSlot(1).isEmpty(),
                    "Cooking cabbage rolls should consume the horse meat and cabbage");
            helper.succeed();
        });
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty")
    public static void kazyCookingRecipeProducesFourSlices(GameTestHelper helper) {
        var recipes = helper.getLevel().getRecipeManager();
        var recipe = recipes.byKey(KAZY_RECIPE_ID).orElseThrow();
        helper.assertTrue(recipe.value() instanceof CookingPotRecipe, "Kazy should use a cooking pot recipe");
        var cooking = (CookingPotRecipe) recipe.value();

        for (int slot = 0; slot < CookingPotRecipe.INPUT_SLOTS; slot++) {
            var inventory = new ItemStackHandler(CookingPotRecipe.INPUT_SLOTS);
            inventory.setStackInSlot(slot, new ItemStack(BeshNStuffMod.KAZY.get()));
            var input = new RecipeWrapper(inventory);
            var matched = recipes.getRecipeFor(ModRecipeTypes.COOKING.get(), input, helper.getLevel()).orElseThrow();
            helper.assertTrue(matched.id().equals(KAZY_RECIPE_ID), "Kazy in any ingredient slot should match the cooking pot recipe");
            ItemStack result = cooking.assemble(input, helper.getLevel().registryAccess());
            helper.assertTrue(result.is(BeshNStuffMod.KAZY_SLICE.get()) && result.getCount() == 4,
                    "One kazy should produce exactly four slices in a cooking pot");
        }

        helper.assertTrue(cooking.getCookTime() == 200, "Kazy should take 200 ticks to cook in a pot");
        helper.assertTrue(cooking.getOutputContainer().isEmpty(), "Kazy slices should not require a bowl or other container");
        var inventory = new ItemStackHandler(CookingPotRecipe.INPUT_SLOTS);
        inventory.setStackInSlot(0, new ItemStack(BeshNStuffMod.HORSE_MEAT.get()));
        helper.assertTrue(!cooking.matches(new RecipeWrapper(inventory), helper.getLevel()),
                "Raw horse meat should not replace kazy in the cooking pot recipe");
        inventory.setStackInSlot(0, new ItemStack(BeshNStuffMod.KAZY.get()));
        inventory.setStackInSlot(1, new ItemStack(BeshNStuffMod.KAZY.get()));
        helper.assertTrue(!cooking.matches(new RecipeWrapper(inventory), helper.getLevel()),
                "Extra ingredients should not match the kazy cooking pot recipe");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 240)
    public static void cookingPotCooksKazyIntoFourSlices(GameTestHelper helper) {
        assertCookingPotProducesSlices(helper, 1, 220);
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 440)
    public static void cookingPotCooksTwoKazyIntoEightSlices(GameTestHelper helper) {
        assertCookingPotProducesSlices(helper, 2, 420);
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 240)
    public static void unheatedCookingPotDoesNotCookKazy(GameTestHelper helper) {
        CookingPotBlockEntity pot = prepareCookingPot(helper, false, 1);

        helper.runAtTickTime(220, () -> {
            ItemStack input = pot.getInventory().getStackInSlot(0);
            helper.assertTrue(input.is(BeshNStuffMod.KAZY.get()) && input.getCount() == 1,
                    "An unheated cooking pot should retain the uncooked kazy");
            helper.assertTrue(pot.getMeal().isEmpty() && pot.getInventory().getStackInSlot(CookingPotBlockEntity.OUTPUT_SLOT).isEmpty(),
                    "An unheated cooking pot should not produce slices");
            helper.succeed();
        });
    }

    @GameTestGenerator
    public static List<TestFunction> skilletTests() {
        return List.of(
                new TestFunction("defaultBatch", "farmersdelightgametests.skilletCooksDoughIntoTwoBaursak",
                        "minecraft:bastion/mobs/empty", 160, 0, true, FarmersDelightGameTests::skilletCooksDoughIntoTwoBaursak),
                new TestFunction("defaultBatch", "farmersdelightgametests.unheatedSkilletDoesNotCookDough",
                        "minecraft:bastion/mobs/empty", 160, 0, true, FarmersDelightGameTests::unheatedSkilletDoesNotCookDough),
                new TestFunction("defaultBatch", "farmersdelightgametests.skilletCooksHorseMeat",
                        "minecraft:bastion/mobs/empty", 160, 0, true, FarmersDelightGameTests::skilletCooksHorseMeat),
                new TestFunction("defaultBatch", "farmersdelightgametests.unheatedSkilletDoesNotCookHorseMeat",
                        "minecraft:bastion/mobs/empty", 160, 0, true, FarmersDelightGameTests::unheatedSkilletDoesNotCookHorseMeat));
    }

    private static void skilletCooksDoughIntoTwoBaursak(GameTestHelper helper) {
        BlockPos skilletPos = new BlockPos(1, 2, 1);
        helper.setBlock(skilletPos.below(), Blocks.CAMPFIRE);
        placeDoughInSkillet(helper, skilletPos);

        helper.runAtTickTime(140, () -> {
            helper.assertItemEntityCountIs(BeshNStuffMod.BAURSAK.get(), skilletPos, 3.0, 2);
            var player = helper.makeMockPlayer(GameType.SURVIVAL);
            helper.useBlock(skilletPos, player);
            helper.assertTrue(player.getMainHandItem().isEmpty(), "Cooking should consume the dough");
            helper.succeed();
        });
    }

    private static void unheatedSkilletDoesNotCookDough(GameTestHelper helper) {
        BlockPos skilletPos = new BlockPos(1, 2, 1);
        helper.setBlock(skilletPos.below(), Blocks.STONE);
        placeDoughInSkillet(helper, skilletPos);

        helper.runAtTickTime(140, () -> {
            helper.assertItemEntityNotPresent(BeshNStuffMod.BAURSAK.get(), skilletPos, 3.0);
            var player = helper.makeMockPlayer(GameType.SURVIVAL);
            helper.useBlock(skilletPos, player);
            ItemStack remaining = player.getMainHandItem();
            helper.assertTrue(remaining.is(BuiltInRegistries.ITEM.getOptional(DOUGH_ID).orElseThrow()) && remaining.getCount() == 1,
                    "An unheated skillet should retain the uncooked dough");
            helper.succeed();
        });
    }

    private static void placeDoughInSkillet(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos, BuiltInRegistries.BLOCK.getOptional(SKILLET_ID).orElseThrow());
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(BuiltInRegistries.ITEM.getOptional(DOUGH_ID).orElseThrow()));
        helper.useBlock(pos, player);
        helper.assertTrue(player.getMainHandItem().isEmpty(), "The skillet should accept the wheat dough");
    }

    private static void skilletCooksHorseMeat(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 2, 1);
        helper.setBlock(pos.below(), Blocks.CAMPFIRE);
        placeHorseMeatInSkillet(helper, pos);

        helper.runAtTickTime(140, () -> {
            helper.assertItemEntityCountIs(BeshNStuffMod.COOKED_HORSE_MEAT.get(), pos, 3.0, 1);
            var player = helper.makeMockPlayer(GameType.SURVIVAL);
            helper.useBlock(pos, player);
            helper.assertTrue(player.getMainHandItem().isEmpty(), "Skillet cooking should consume the raw horse meat");
            helper.succeed();
        });
    }

    private static void unheatedSkilletDoesNotCookHorseMeat(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 2, 1);
        helper.setBlock(pos.below(), Blocks.STONE);
        placeHorseMeatInSkillet(helper, pos);

        helper.runAtTickTime(140, () -> {
            helper.assertItemEntityNotPresent(BeshNStuffMod.COOKED_HORSE_MEAT.get(), pos, 3.0);
            var player = helper.makeMockPlayer(GameType.SURVIVAL);
            helper.useBlock(pos, player);
            ItemStack remaining = player.getMainHandItem();
            helper.assertTrue(remaining.is(BeshNStuffMod.HORSE_MEAT.get()) && remaining.getCount() == 1,
                    "An unheated skillet should retain the raw horse meat");
            helper.succeed();
        });
    }

    private static void placeHorseMeatInSkillet(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos, BuiltInRegistries.BLOCK.getOptional(SKILLET_ID).orElseThrow());
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(BeshNStuffMod.HORSE_MEAT.get()));
        helper.useBlock(pos, player);
        helper.assertTrue(player.getMainHandItem().isEmpty(), "The skillet should accept raw horse meat");
    }

    private static CookingPotBlockEntity prepareCookingPot(GameTestHelper helper, boolean heated, int inputCount) {
        BlockPos pos = new BlockPos(1, 2, 1);
        helper.setBlock(pos.below(), heated ? Blocks.CAMPFIRE : Blocks.STONE);
        helper.setBlock(pos, BuiltInRegistries.BLOCK.getOptional(COOKING_POT_ID).orElseThrow());
        CookingPotBlockEntity pot = helper.getBlockEntity(pos);
        pot.getInventory().setStackInSlot(0, new ItemStack(BeshNStuffMod.KAZY.get(), inputCount));
        return pot;
    }

    private static void assertCookingPotProducesSlices(GameTestHelper helper, int inputCount, int ticks) {
        CookingPotBlockEntity pot = prepareCookingPot(helper, true, inputCount);

        helper.runAtTickTime(ticks, () -> {
            ItemStack output = pot.getInventory().getStackInSlot(CookingPotBlockEntity.OUTPUT_SLOT);
            helper.assertTrue(output.is(BeshNStuffMod.KAZY_SLICE.get()) && output.getCount() == inputCount * 4,
                    "A heated cooking pot should produce exactly four slices per kazy without containers");
            helper.assertTrue(pot.getInventory().getStackInSlot(0).isEmpty(), "Cooking should consume the kazy");
            helper.assertTrue(pot.getMeal().isEmpty(), "Slices should move directly to the output slot");
            helper.succeed();
        });
    }
}
