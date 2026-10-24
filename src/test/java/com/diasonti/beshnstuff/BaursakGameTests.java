package com.diasonti.beshnstuff;

import java.util.List;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder
@PrefixGameTestTemplate(false)
public final class BaursakGameTests {
    private static final ResourceLocation RECIPE_ID = ResourceLocation.fromNamespaceAndPath(BeshNStuffMod.MODID, "baursak");

    private BaursakGameTests() {
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty")
    public static void baursakHasFastSnackProperties(GameTestHelper helper) {
        ItemStack baursak = new ItemStack(BeshNStuffMod.BAURSAK.get());
        var food = baursak.getFoodProperties(null);

        helper.assertTrue(food != null, "Baursak should be edible");
        helper.assertTrue(food.nutrition() == 1, "Baursak should restore one hunger point");
        helper.assertTrue(Math.abs(food.saturation() - 1.2f) < 0.0001f, "Baursak should restore 1.2 saturation");
        helper.assertTrue(baursak.getUseDuration(null) == 16, "Baursak should take 16 ticks (0.8 seconds) to eat");
        helper.assertTrue(!food.canAlwaysEat(), "Baursak should only be edible when hungry");
        helper.assertTrue(baursak.getMaxStackSize() == 64, "Baursak should stack to 64");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty")
    public static void eatingBaursakRestoresSnackNutrition(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.getFoodData().setFoodLevel(10);
        player.getFoodData().setSaturation(0.0f);
        ItemStack baursak = new ItemStack(BeshNStuffMod.BAURSAK.get(), 2);

        ItemStack remaining = baursak.finishUsingItem(helper.getLevel(), player);

        helper.assertTrue(player.getFoodData().getFoodLevel() == 11, "Eating baursak should restore one hunger point");
        helper.assertTrue(Math.abs(player.getFoodData().getSaturationLevel() - 1.2f) < 0.0001f,
                "Eating baursak should restore 1.2 saturation");
        helper.assertTrue(remaining.is(BeshNStuffMod.BAURSAK.get()) && remaining.getCount() == 1,
                "Eating baursak should consume exactly one item");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty")
    public static void inventoryGridCraftsThreeBaursakFromTwoWheat(GameTestHelper helper) {
        CraftingInput input = CraftingInput.of(2, 2, List.of(
                new ItemStack(Items.WHEAT), ItemStack.EMPTY,
                ItemStack.EMPTY, new ItemStack(Items.WHEAT)));

        assertCraftsThreeBaursak(helper, input);
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty")
    public static void craftingTableCraftsThreeBaursakFromTwoWheat(GameTestHelper helper) {
        CraftingInput input = CraftingInput.of(3, 3, List.of(
                new ItemStack(Items.WHEAT), ItemStack.EMPTY, ItemStack.EMPTY,
                ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY,
                ItemStack.EMPTY, ItemStack.EMPTY, new ItemStack(Items.WHEAT)));

        assertCraftsThreeBaursak(helper, input);
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty")
    public static void baursakRequiresExactlyTwoWheatInSeparateSlots(GameTestHelper helper) {
        CraftingRecipe recipe = (CraftingRecipe) helper.getLevel().getRecipeManager().byKey(RECIPE_ID).orElseThrow().value();

        helper.assertTrue(!recipe.matches(CraftingInput.of(1, 1, List.of(new ItemStack(Items.WHEAT))), helper.getLevel()),
                "One wheat should not craft baursak");
        helper.assertTrue(!recipe.matches(CraftingInput.of(1, 1, List.of(new ItemStack(Items.WHEAT, 2))), helper.getLevel()),
                "Two wheat stacked in one slot should not craft baursak");
        helper.assertTrue(!recipe.matches(CraftingInput.of(3, 1, List.of(
                new ItemStack(Items.WHEAT), new ItemStack(Items.WHEAT), new ItemStack(Items.WHEAT))), helper.getLevel()),
                "Three wheat should not match the baursak recipe");
        helper.assertTrue(!recipe.matches(CraftingInput.of(2, 1, List.of(
                new ItemStack(Items.WHEAT), new ItemStack(Items.WHEAT_SEEDS))), helper.getLevel()),
                "Wheat seeds should not replace wheat in the baursak recipe");
        helper.succeed();
    }

    private static void assertCraftsThreeBaursak(GameTestHelper helper, CraftingInput input) {
        var recipe = helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel()).orElseThrow();
        helper.assertTrue(recipe.id().equals(RECIPE_ID), "Two wheat should match the baursak recipe");

        ItemStack result = recipe.value().assemble(input, helper.getLevel().registryAccess());
        helper.assertTrue(result.is(BeshNStuffMod.BAURSAK.get()), "The recipe should produce baursak");
        helper.assertTrue(result.getCount() == 3, "Two wheat should produce three baursak");
    }
}
