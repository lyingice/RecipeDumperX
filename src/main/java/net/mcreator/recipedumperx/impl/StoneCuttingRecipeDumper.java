package net.mcreator.recipedumperx.impl;

import net.mcreator.recipedumperx.api.IRecipeDumper;
import net.mcreator.recipedumperx.api.IRecipeInputs;
import net.minecraft.world.item.crafting.StonecutterRecipe; // 注意类名变更

@IRecipeDumper.For(StonecutterRecipe.class)
public class StoneCuttingRecipeDumper implements IRecipeDumper<StonecutterRecipe> {
    @Override
    public void setInputs(StonecutterRecipe recipe, IRecipeInputs inputs) {
        inputs.addInput(1, recipe.getIngredients().get(0));
    }
    @Override
    public String getRecipeTypeName(StonecutterRecipe recipe) {
        // 返回旧版兼容的类型名，不带命名空间前缀
        return "stonecutting";
    }
}