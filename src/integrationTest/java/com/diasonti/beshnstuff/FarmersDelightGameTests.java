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

@GameTestHolder
@PrefixGameTestTemplate(false)
public final class FarmersDelightGameTests {
    private static final ResourceLocation RECIPE_ID = ResourceLocation.fromNamespaceAndPath(
            BeshNStuffMod.MODID, "compat/farmersdelight/baursak_from_dough");
    private static final ResourceLocation DOUGH_ID = ResourceLocation.fromNamespaceAndPath("farmersdelight", "wheat_dough");
    private static final ResourceLocation SKILLET_ID = ResourceLocation.fromNamespaceAndPath("farmersdelight", "skillet");

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

    @GameTestGenerator
    public static List<TestFunction> skilletTests() {
        return List.of(
                new TestFunction("defaultBatch", "farmersdelightgametests.skilletCooksDoughIntoTwoBaursak",
                        "minecraft:bastion/mobs/empty", 160, 0, true, FarmersDelightGameTests::skilletCooksDoughIntoTwoBaursak),
                new TestFunction("defaultBatch", "farmersdelightgametests.unheatedSkilletDoesNotCookDough",
                        "minecraft:bastion/mobs/empty", 160, 0, true, FarmersDelightGameTests::unheatedSkilletDoesNotCookDough));
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
}
