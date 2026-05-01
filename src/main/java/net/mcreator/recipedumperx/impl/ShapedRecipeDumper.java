package net.mcreator.recipedumperx.impl;

import net.mcreator.recipedumperx.api.IRecipeDumper;
import net.mcreator.recipedumperx.api.IRecipeInputs;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;

import java.util.List;

@IRecipeDumper.For(ShapedRecipe.class)
public class ShapedRecipeDumper implements IRecipeDumper<ShapedRecipe> {

    @Override
    public void setInputs(ShapedRecipe recipe, IRecipeInputs inputs) {
        // 1.21.1: 使用 ShapedRecipe 的公开方法 getWidth() 和 getIngredients()
        int width = recipe.getWidth();
        List<Ingredient> ingredients = recipe.getIngredients();

        for (int i = 0; i < ingredients.size(); i++) {
            Ingredient ingredient = ingredients.get(i);
            // 跳过空槽位
            if (ingredient.isEmpty()) {
                continue;
            }
            // 计算工作台网格中的真实槽位编号
            int x = i % width;
            int y = i / width;
            int slotId = y * 3 + x + 1;
            inputs.addInput(slotId, ingredient);
        }
    }

    @Override
    public String getRecipeTypeName(ShapedRecipe recipe) {
        return "crafting_shaped";
    }
}