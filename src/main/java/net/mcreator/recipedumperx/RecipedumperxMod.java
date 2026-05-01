package net.mcreator.recipedumperx;

import net.mcreator.recipedumperx.api.IRecipeDumper;
import net.mcreator.recipedumperx.impl.DumpRecipeCommand;
import net.mcreator.recipedumperx.impl.ModArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforgespi.language.ModFileScanData;
import net.minecraft.resources.ResourceLocation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.objectweb.asm.Type;

@Mod(RecipedumperxMod.MODID)
public class RecipedumperxMod {
    public static final String MODID = "recipedumperx";
    private static boolean initialized = false;
    private static boolean argumentRegistered = false;

    public RecipedumperxMod(IEventBus modEventBus, ModContainer container) {
        // 1. 使用自定义的强制注册方法
        forceRegisterArgumentType();

        // 2. 注册游戏事件监听器
        NeoForge.EVENT_BUS.register(this);
    }

    /**
     * 在构造方法中，通过反射直接向 ArgumentTypeInfos 的底层 Map 注入新的参数类型。
     * 这确保注册时机早于任何玩家登录事件。
     */
    @SuppressWarnings("unchecked")
    private void forceRegisterArgumentType() {
        if (!argumentRegistered) {
            argumentRegistered = true;
            try {
                // 创建参数类型的序列化器
                ArgumentTypeInfo<?, ?> info = SingletonArgumentInfo.contextFree(() -> ModArgumentType.INSTANCE);

                // 通过反射获取 ArgumentTypeInfos 中私有的 BY_CLASS Map 字段
                Field field = ArgumentTypeInfos.class.getDeclaredField("BY_CLASS");
                field.setAccessible(true);
                Map<Class<?>, ArgumentTypeInfo<?, ?>> byClassMap = (Map<Class<?>, ArgumentTypeInfo<?, ?>>) field.get(null);

                // 注册到原版映射中
                byClassMap.put(ModArgumentType.class, info);

                System.out.println("[RecipeDumperX] Successfully force-registered custom argument type via BY_CLASS map.");
            } catch (Exception e) {
                System.err.println("[RecipeDumperX] Failed to force-register argument type:");
                e.printStackTrace();
            }
        }
    }

    // 注册命令
    @SubscribeEvent
    public void onRegisterCommand(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("dumprecipe")
                .then(Commands.argument("mod", ModArgumentType.INSTANCE)
                        .executes(DumpRecipeCommand::executeCommand)
                )
        );
    }

    // 在玩家登录成功后初始化配方导出器
    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!initialized) {
            initialized = true;
            registerAllDumpers();
        }
    }

    private void registerAllDumpers() {
        Type dumperForType = Type.getType(IRecipeDumper.For.class);
        for (ModFileScanData scanData : ModList.get().getAllScanData()) {
            scanData.getAnnotations().stream()
                    .filter(annotationData -> annotationData.annotationType().equals(dumperForType))
                    .filter(annotationData -> {
                        Object modDeps = annotationData.annotationData().get("modDeps");
                        return modDeps == null || ModList.get().isLoaded(((String) modDeps));
                    })
                    .map(ModFileScanData.AnnotationData::clazz)
                    .distinct()
                    .forEach(this::registerThisDumper);
        }
    }

    private void registerThisDumper(Type dumperType) {
        String className = dumperType.getClassName();
        try {
            Class<?> clazz = Class.forName(className);
            if (IRecipeDumper.class.isAssignableFrom(clazz)) {
                IRecipeDumper<?> recipeDumper = (IRecipeDumper<?>) clazz.getDeclaredConstructor().newInstance();
                IRecipeDumper.For annotation = clazz.getAnnotation(IRecipeDumper.For.class);
                if (annotation != null) {
                    DumpRecipeCommand.addRecipeDumper(annotation.value(), recipeDumper);
                    return;
                }
                for (IRecipeDumper.For aFor : clazz.getAnnotation(IRecipeDumper.Container.class).value()) {
                    DumpRecipeCommand.addRecipeDumper(aFor.value(), recipeDumper);
                }
            }
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }
    // ... 在你现有的主类中添加以下两个方法 ...

    // 添加一个静态的注册表实例
    private static final DumperRegistry DUMPER_REGISTRY = new DumperRegistry();

    // 获取注册表的公共方法
    public static DumperRegistry getDumperRegistry() {
        return DUMPER_REGISTRY;
    }

    // 简单注册表类，你可以放在主类内部或者单独建文件
    public static class DumperRegistry {
        private final Map<Class<? extends Recipe<?>>, IRecipeDumper<Recipe<?>>> dumpers = new HashMap<>();

        public void addDumper(Class<? extends Recipe<?>> recipeClass, IRecipeDumper<?> dumper) {
            dumpers.put(recipeClass, (IRecipeDumper<Recipe<?>>) dumper);
        }

        public boolean hasDumper(Class<?> recipeClass) {
            return dumpers.containsKey(recipeClass);
        }

        public IRecipeDumper<Recipe<?>> getDumper(Class<?> recipeClass) {
            return dumpers.get(recipeClass);
        }
    }
}