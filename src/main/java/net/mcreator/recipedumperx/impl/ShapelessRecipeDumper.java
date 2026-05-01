package net.mcreator.recipedumperx.impl;

import net.mcreator.recipedumperx.api.IRecipeDumper;
import net.mcreator.recipedumperx.api.IRecipeInputs;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;

import java.util.List;

@IRecipeDumper.For(ShapelessRecipe.class)
public class ShapelessRecipeDumper implements IRecipeDumper<ShapelessRecipe> {

    @Override
    public void setInputs(ShapelessRecipe recipe, IRecipeInputs inputs) {
        List<Ingredient> ingredients = recipe.getIngredients();
        for (int i = 0; i < ingredients.size(); i++) {
            inputs.addInput(i + 1, ingredients.get(i));
        }
    }

    @Override
    public String getRecipeTypeName(ShapelessRecipe recipe) {
        return "crafting_shapeless";
    }
}