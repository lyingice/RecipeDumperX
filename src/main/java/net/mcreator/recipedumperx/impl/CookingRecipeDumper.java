package net.mcreator.recipedumperx.impl;

import com.google.gson.JsonObject;
import net.mcreator.recipedumperx.api.IRecipeDumper;
import net.mcreator.recipedumperx.api.IRecipeInputs;
import net.mcreator.recipedumperx.api.IRecipeOutputs;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.crafting.*;

@IRecipeDumper.For(AbstractCookingRecipe.class)
public class CookingRecipeDumper implements IRecipeDumper<AbstractCookingRecipe> {

    @Override
    public void setInputs(AbstractCookingRecipe recipe, IRecipeInputs inputs) {
        inputs.addInput(1, recipe.getIngredients().get(0));
    }

    @Override
    public void setOutputs(AbstractCookingRecipe recipe, IRecipeOutputs outputs) {
        // 安全地获取输出物品：1.20.6 使用 STATIC_ACCESS，1.21.1 使用 null
        outputs.addOutput(1, recipe.getResultItem(RegistryAccess.EMPTY));
    }

    @Override
    public String getRecipeTypeName(AbstractCookingRecipe recipe) {
        if (recipe instanceof BlastingRecipe) {
            return "blasting";
        } else if (recipe instanceof CampfireCookingRecipe) {
            return "campfire_cooking";
        } else if (recipe instanceof SmokingRecipe) {
            return "smoking";
        }
        return "smelting";
    }

    @Override
    public void writeExtraInformation(AbstractCookingRecipe recipe, JsonObject jsonObject) {
        // 使用反射获取 experience，兼容 1.20.6 和 1.21.1
        try {
            float experience = (float) recipe.getClass().getMethod("getExperience").invoke(recipe);
            jsonObject.addProperty("experience", experience);
        } catch (Exception e) {
            jsonObject.addProperty("experience", 0);
        }

        // 使用反射获取 cookingTime，兼容 1.20.6 和 1.21.1
        try {
            int cookingTime = (int) recipe.getClass().getMethod("getCookingTime").invoke(recipe);
            jsonObject.addProperty("cookTime", cookingTime);
        } catch (Exception e) {
            jsonObject.addProperty("cookTime", 200);
        }
    }
}