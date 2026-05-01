package net.mcreator.recipedumperx.api;

import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.Recipe;

import java.lang.annotation.*;

public interface IRecipeDumper<T extends Recipe<?>> {
    // 1.21: 提供一个静态的 RegistryAccess 实例，供 setOutputs 使用
    RegistryAccess STATIC_ACCESS = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);

    void setInputs(T recipe, IRecipeInputs inputs);

    default void setOutputs(T recipe, IRecipeOutputs outputs) {
        // 使用 STATIC_ACCESS 代替 RegistryAccess.EMPTY，更规范
        outputs.addOutput(1, recipe.getResultItem(STATIC_ACCESS));
    }

    default void writeExtraInformation(T recipe, JsonObject jsonObject) {
    }

    default String getRecipeTypeName(T recipe) {
        // 简化：直接返回配方类型的字符串表示，不再需要 BuiltInRegistries
        return recipe.getType().toString();
    }

    // 注解，无改动
    @Retention(RetentionPolicy.RUNTIME)
    @Repeatable(Container.class)
    @Target(ElementType.TYPE)
    @interface For {
        Class<? extends Recipe<?>> value();

        @SuppressWarnings("unused")
        String modDeps() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface Container {
        For[] value();
    }
}