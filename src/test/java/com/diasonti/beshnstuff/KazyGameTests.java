package com.diasonti.beshnstuff;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder
@PrefixGameTestTemplate(false)
public final class KazyGameTests {
    private static final ResourceLocation RECIPE_ID = ResourceLocation.fromNamespaceAndPath(BeshNStuffMod.MODID, "kazy");

    private KazyGameTests() {
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty")
    public static void kazyIsWorthLessThanThreeRawHorseMeats(GameTestHelper helper) {
        var horseMeat = new ItemStack(BeshNStuffMod.HORSE_MEAT.get()).getFoodProperties(null);
        var kazy = new ItemStack(BeshNStuffMod.KAZY.get()).getFoodProperties(null);

        helper.assertTrue(horseMeat != null && kazy != null, "Horse meat and kazy should be edible");
        helper.assertTrue(kazy.nutrition() < horseMeat.nutrition() * 3,
                "Kazy should restore less hunger than its three raw horse meat inputs");
        helper.assertTrue(kazy.saturation() < horseMeat.saturation() * 3,
                "Kazy should restore less saturation than its three raw horse meat inputs");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty")
    public static void kazySliceIsWorthOneAndAHalfCookedHorseMeats(GameTestHelper helper) {
        var horseMeat = new ItemStack(BeshNStuffMod.COOKED_HORSE_MEAT.get()).getFoodProperties(null);
        var slice = new ItemStack(BeshNStuffMod.KAZY_SLICE.get()).getFoodProperties(null);

        helper.assertTrue(horseMeat != null && slice != null, "Cooked horse meat and kazy slices should be edible");
        helper.assertTrue(slice.nutrition() == horseMeat.nutrition() * 1.5f,
                "A kazy slice should restore 1.5 times the hunger of cooked horse meat");
        helper.assertTrue(Math.abs(slice.saturation() - horseMeat.saturation() * 1.5f) < 0.0001f,
                "A kazy slice should restore 1.5 times the saturation of cooked horse meat");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty")
    public static void eatingKazyRestoresItsFoodValues(GameTestHelper helper) {
        assertEatingRestoresFood(helper, new ItemStack(BeshNStuffMod.KAZY.get(), 2), 16, 4.8f);
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty")
    public static void eatingKazySliceRestoresItsFoodValues(GameTestHelper helper) {
        assertEatingRestoresFood(helper, new ItemStack(BeshNStuffMod.KAZY_SLICE.get(), 2), 20, 19.2f);
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty")
    public static void anyCraftingTableRowCraftsOneKazy(GameTestHelper helper) {
        for (int row = 0; row < 3; row++) {
            List<ItemStack> items = new ArrayList<>();
            for (int slot = 0; slot < 9; slot++) {
                items.add(slot / 3 == row ? new ItemStack(BeshNStuffMod.HORSE_MEAT.get()) : ItemStack.EMPTY);
            }
            CraftingInput input = CraftingInput.of(3, 3, items);
            var recipe = helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel()).orElseThrow();
            helper.assertTrue(recipe.id().equals(RECIPE_ID), "Three horse meats in a horizontal row should match the kazy recipe");

            ItemStack result = recipe.value().assemble(input, helper.getLevel().registryAccess());
            helper.assertTrue(result.is(BeshNStuffMod.KAZY.get()) && result.getCount() == 1,
                    "Three horse meats should produce exactly one kazy");
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty")
    public static void kazyRequiresExactlyThreeRawHorseMeatsInARow(GameTestHelper helper) {
        CraftingRecipe recipe = (CraftingRecipe) helper.getLevel().getRecipeManager().byKey(RECIPE_ID).orElseThrow().value();

        helper.assertTrue(!recipe.matches(CraftingInput.of(1, 1, List.of(
                new ItemStack(BeshNStuffMod.HORSE_MEAT.get(), 3))), helper.getLevel()),
                "Three horse meats stacked in one slot should not craft kazy");
        helper.assertTrue(!recipe.matches(CraftingInput.of(2, 1, List.of(
                new ItemStack(BeshNStuffMod.HORSE_MEAT.get()), new ItemStack(BeshNStuffMod.HORSE_MEAT.get()))), helper.getLevel()),
                "Two horse meats should not craft kazy");
        helper.assertTrue(!recipe.matches(CraftingInput.of(1, 3, List.of(
                new ItemStack(BeshNStuffMod.HORSE_MEAT.get()), new ItemStack(BeshNStuffMod.HORSE_MEAT.get()),
                new ItemStack(BeshNStuffMod.HORSE_MEAT.get()))), helper.getLevel()),
                "A vertical row should not craft kazy");
        helper.assertTrue(!recipe.matches(CraftingInput.of(2, 2, List.of(
                new ItemStack(BeshNStuffMod.HORSE_MEAT.get()), new ItemStack(BeshNStuffMod.HORSE_MEAT.get()),
                new ItemStack(BeshNStuffMod.HORSE_MEAT.get()), ItemStack.EMPTY)), helper.getLevel()),
                "The inventory grid should not craft kazy");
        helper.assertTrue(!recipe.matches(CraftingInput.of(3, 1, List.of(
                new ItemStack(BeshNStuffMod.HORSE_MEAT.get()), new ItemStack(BeshNStuffMod.COOKED_HORSE_MEAT.get()),
                new ItemStack(BeshNStuffMod.HORSE_MEAT.get()))), helper.getLevel()),
                "Cooked horse meat should not replace raw horse meat");
        helper.assertTrue(!recipe.matches(CraftingInput.of(3, 1, List.of(
                new ItemStack(Items.BEEF), new ItemStack(Items.BEEF), new ItemStack(Items.BEEF))), helper.getLevel()),
                "Beef should not replace horse meat");
        helper.assertTrue(!recipe.matches(CraftingInput.of(3, 2, List.of(
                new ItemStack(BeshNStuffMod.HORSE_MEAT.get()), new ItemStack(BeshNStuffMod.HORSE_MEAT.get()),
                new ItemStack(BeshNStuffMod.HORSE_MEAT.get()), new ItemStack(BeshNStuffMod.HORSE_MEAT.get()),
                ItemStack.EMPTY, ItemStack.EMPTY)), helper.getLevel()),
                "Extra ingredients should not match the kazy recipe");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 240)
    public static void furnaceCooksKazyIntoThreeSlices(GameTestHelper helper) {
        assertCookingProducesSlices(helper, Blocks.FURNACE, 1, 0, 200);
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 140)
    public static void smokerCooksKazyIntoThreeSlices(GameTestHelper helper) {
        assertCookingProducesSlices(helper, Blocks.SMOKER, 1, 0, 100);
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 440)
    public static void furnaceCooksTwoKazyIntoSixSlices(GameTestHelper helper) {
        assertCookingProducesSlices(helper, Blocks.FURNACE, 2, 0, 400);
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 240)
    public static void smokerCooksTwoKazyIntoSixSlices(GameTestHelper helper) {
        assertCookingProducesSlices(helper, Blocks.SMOKER, 2, 0, 200);
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 240)
    public static void furnaceCanFillASliceStackExactly(GameTestHelper helper) {
        assertCookingProducesSlices(helper, Blocks.FURNACE, 1, 61, 200);
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 140)
    public static void smokerCanFillASliceStackExactly(GameTestHelper helper) {
        assertCookingProducesSlices(helper, Blocks.SMOKER, 1, 61, 100);
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 260)
    public static void furnaceRequiresSpaceForAllThreeSlices(GameTestHelper helper) {
        assertCookingWaitsForOutputSpace(helper, Blocks.FURNACE, 220);
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 160)
    public static void smokerRequiresSpaceForAllThreeSlices(GameTestHelper helper) {
        assertCookingWaitsForOutputSpace(helper, Blocks.SMOKER, 120);
    }

    private static void assertEatingRestoresFood(GameTestHelper helper, ItemStack kazy, int expectedHunger, float expectedSaturation) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.getFoodData().setFoodLevel(8);
        player.getFoodData().setSaturation(0.0f);

        ItemStack remaining = kazy.finishUsingItem(helper.getLevel(), player);

        helper.assertTrue(player.getFoodData().getFoodLevel() == expectedHunger, "Eating kazy should restore its hunger value");
        helper.assertTrue(Math.abs(player.getFoodData().getSaturationLevel() - expectedSaturation) < 0.0001f,
                "Eating kazy should restore its saturation value");
        helper.assertTrue(remaining.is(kazy.getItem()) && remaining.getCount() == 1, "Eating kazy should consume exactly one item");
        helper.succeed();
    }

    private static AbstractFurnaceBlockEntity prepareCooker(GameTestHelper helper, Block block, int inputCount, int outputCount) {
        BlockPos pos = new BlockPos(0, 1, 0);
        helper.setBlock(pos, block);
        AbstractFurnaceBlockEntity cooker = helper.getBlockEntity(pos);
        cooker.setItem(0, new ItemStack(BeshNStuffMod.KAZY.get(), inputCount));
        cooker.setItem(1, new ItemStack(Items.COAL));
        if (outputCount > 0) {
            cooker.setItem(2, new ItemStack(BeshNStuffMod.KAZY_SLICE.get(), outputCount));
        }
        return cooker;
    }

    private static void assertCookingProducesSlices(GameTestHelper helper, Block block, int inputCount, int outputCount, int ticks) {
        AbstractFurnaceBlockEntity cooker = prepareCooker(helper, block, inputCount, outputCount);

        helper.succeedOnTickWhen(ticks, () -> {
            ItemStack output = cooker.getItem(2);
            helper.assertTrue(output.is(BeshNStuffMod.KAZY_SLICE.get()) && output.getCount() == outputCount + inputCount * 3,
                    "Cooking should add exactly three slices per sausage");
            helper.assertTrue(cooker.getItem(0).isEmpty(), "Cooking should consume the kazy");
        });
    }

    private static void assertCookingWaitsForOutputSpace(GameTestHelper helper, Block block, int ticks) {
        AbstractFurnaceBlockEntity cooker = prepareCooker(helper, block, 1, 62);

        helper.runAtTickTime(ticks, () -> {
            ItemStack input = cooker.getItem(0);
            ItemStack output = cooker.getItem(2);
            helper.assertTrue(input.is(BeshNStuffMod.KAZY.get()) && input.getCount() == 1,
                    "Cooking should not consume a sausage without space for all three slices");
            helper.assertTrue(output.is(BeshNStuffMod.KAZY_SLICE.get()) && output.getCount() == 62,
                    "Cooking should not overfill the output stack");
            helper.succeed();
        });
    }
}
