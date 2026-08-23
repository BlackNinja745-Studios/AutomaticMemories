package com.dinoslice.automaticmemories.client.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import com.dinoslice.automaticmemories.client.ScreenshotTimerSingleton;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            ConfigBuilder builder = ConfigBuilder.create()
                    .setTitle(Component.translatable("automaticmemories.config.title"))
                    .setSavingRunnable(() -> Configuration.saveToFile(Configuration.CONFIG_PATH))
                    .setParentScreen(parent);

            ConfigEntryBuilder entryBuilder = builder.entryBuilder();

            ConfigCategory category = builder.getOrCreateCategory(Component.translatable("automaticmemories.config.category"));

            category.addEntry(
                    entryBuilder.startBooleanToggle(Component.translatable("automaticmemories.config.enabled"), Configuration.ENABLED)
                            .setTooltip(Component.translatable("automaticmemories.config.enabled.tooltip"))
                            .setDefaultValue(true)
                            .setSaveConsumer((enabled) -> {
                                Configuration.ENABLED = enabled;

                                if (enabled)
                                    ScreenshotTimerSingleton.restartOrStartTimer(Configuration.LEFTOVER_INTERVAL_MS, Configuration.INTERVAL_MS);
                                else {
                                    ScreenshotTimerSingleton.cancelTimer();
                                    Configuration.LEFTOVER_INTERVAL_MS = ScreenshotTimerSingleton.timeSinceLastScreenshot();
                                }
                            })
                            .build()
            );

            category.addEntry(
                entryBuilder.startSubCategory(Component.translatable("automaticmemories.config.interval.subcategory"), List.of(
                    entryBuilder.startLongField(Component.translatable("automaticmemories.config.interval.interval_ms"), Configuration.INTERVAL_MS)
                        .setDefaultValue(3600 * 1000 * 3)
                        .setMin(5 * 1000)
                        .setSaveConsumer(l -> {
                            Configuration.INTERVAL_MS = l;
                            ScreenshotTimerSingleton.restartOrStartTimer(0, Configuration.INTERVAL_MS);
                        })
                        .setTooltipSupplier(l -> {
                            Component main = Component.translatable("automaticmemories.config.interval.interval_ms.tooltip.main");

                            Component current = Component.translatable("automaticmemories.config.interval.interval_ms.tooltip.editing", ScreenshotTimerSingleton.formatTime(l))
                                    .withStyle(ChatFormatting.GOLD);

                            Component remaining = Configuration.ENABLED ? Component.translatable("automaticmemories.config.interval.interval_ms.tooltip.remaining",
                                    ScreenshotTimerSingleton.formatTime(Configuration.INTERVAL_MS - ScreenshotTimerSingleton.timeSinceLastScreenshot()),
                                    ScreenshotTimerSingleton.formatTime(Configuration.INTERVAL_MS)
                            ).withStyle(ChatFormatting.GRAY) : Component.translatable("automaticmemories.config.interval.interval_ms.tooltip.disabled")
                                    .withStyle(ChatFormatting.GRAY);

                            return l == Configuration.INTERVAL_MS ?
                                    Optional.of(new Component[] { main, remaining }) :
                                    Optional.of(new Component[] { main, current, remaining });
                        })
                        .build(),

                    entryBuilder.startBooleanToggle(Component.translatable("automaticmemories.config.interval.restart_timer_each_session"), Configuration.RESTART_TIMER_EACH_SESSION)
                        .setDefaultValue(false)
                        .setSaveConsumer(b -> Configuration.RESTART_TIMER_EACH_SESSION = b)
                        .setTooltip(Optional.of(new Component[] {
                                Component.translatable("automaticmemories.config.interval.restart_timer_each_session.tooltip")
                        }))
                        .build(),
                    entryBuilder.startBooleanToggle(Component.translatable("automaticmemories.config.interval.require_in_world"), Configuration.REQUIRE_IN_WORLD)
                        .setDefaultValue(true)
                        .setSaveConsumer(b -> Configuration.REQUIRE_IN_WORLD = b)
                        .setTooltip(Optional.of(new Component[] {
                                Component.translatable("automaticmemories.config.interval.require_in_world.tooltip")
                        }))
                        .build(),
                    entryBuilder.startBooleanToggle(Component.translatable("automaticmemories.config.interval.require_unpaused"), Configuration.REQUIRE_UNPAUSED)
                        .setDefaultValue(false)
                        .setSaveConsumer(b -> Configuration.REQUIRE_UNPAUSED = b)
                        .setTooltip(Optional.of(new Component[] {
                                Component.translatable("automaticmemories.config.interval.require_unpaused.tooltip")
                        }))
                        .build()
                ))
                .setExpanded(true)
                .build()
            );

            category.addEntry(
                entryBuilder.startSubCategory(Component.translatable("automaticmemories.config.save.subcategory"), List.of(
                    entryBuilder.startTextField(Component.translatable("automaticmemories.config.save.save_directory"), Configuration.SAVE_DIRECTORY)
                        .setDefaultValue("screenshots")
                        .setSaveConsumer(s -> Configuration.SAVE_DIRECTORY = s)
                        .setErrorSupplier(s -> {
                            try {
                                Paths.get(s);
                            } catch (Exception e) {
                                return Optional.of(Component.translatable("automaticmemories.config.save.save_directory.error", e.getMessage()));
                            }
                            return Optional.empty();
                        })
                        .setTooltipSupplier(s -> {
                            File runDir = Minecraft.getInstance().gameDirectory;

                            Component main = Component.translatable("automaticmemories.config.save.save_directory.tooltip.main");

                            Component current = Component.translatable("automaticmemories.config.save.save_directory.tooltip.editing", Configuration.getFullDirectory(runDir, s))
                                .withStyle(ChatFormatting.GOLD);

                            Component remaining = Component.translatable("automaticmemories.config.save.save_directory.tooltip.current",
                                Configuration.getFullDirectory(runDir, Configuration.SAVE_DIRECTORY)
                            ).withStyle(ChatFormatting.GRAY);

                            return Configuration.SAVE_DIRECTORY.equals(s) ?
                                Optional.of(new Component[] { main, remaining }) :
                                Optional.of(new Component[] { main, current, remaining });
                        })
                        .build(),

                        entryBuilder.startTextField(Component.translatable("automaticmemories.config.save.screenshot_prefix"), Configuration.SCREENSHOT_PREFIX)
                            .setDefaultValue("auto_")
                            .setSaveConsumer(s -> Configuration.SCREENSHOT_PREFIX = s)
                            .setTooltip(Optional.of(new Component[] {
                                Component.translatable("automaticmemories.config.save.screenshot_prefix.tooltip")
                            }))
                            .build()
                ))
                .setExpanded(true)
                .build()
            );

            category.addEntry(
                    entryBuilder.startSubCategory(Component.translatable("automaticmemories.config.special_screenshots.subcategory"), List.of(
                            entryBuilder.startBooleanToggle(Component.translatable("automaticmemories.config.special_screenshots.death"), Configuration.SCREENSHOT_DEATH)
                                    .setDefaultValue(true)
                                    .setTooltip(Component.translatable("automaticmemories.config.special_screenshots.death.tooltip"))
                                    .setSaveConsumer(enabled -> Configuration.SCREENSHOT_DEATH = enabled)
                                    .build(),

                            entryBuilder.startBooleanToggle(Component.translatable("automaticmemories.config.special_screenshots.advancement"), Configuration.SCREENSHOT_ADVANCEMENT)
                                    .setDefaultValue(true)
                                    .setTooltip(Component.translatable("automaticmemories.config.special_screenshots.advancement.tooltip"))
                                    .setSaveConsumer(enabled -> Configuration.SCREENSHOT_ADVANCEMENT = enabled)
                                    .build()
                    ))
                    .setExpanded(true)
                    .build()
            );

            category.addEntry(
                entryBuilder.startSubCategory(Component.translatable("automaticmemories.config.miscellaneous.subcategory"), List.of(
                    entryBuilder.startBooleanToggle(Component.translatable("automaticmemories.config.miscellaneous.notify_player"), Configuration.NOTIFY_PLAYER)
                        .setSaveConsumer(b -> Configuration.NOTIFY_PLAYER = b)
                        .setDefaultValue(false)
                        .setTooltip(Optional.of(new Component[] {
                            Component.translatable("automaticmemories.config.miscellaneous.notify_player.tooltip.main"),
                            Component.translatable("automaticmemories.config.miscellaneous.notify_player.tooltip.disabled_warnings")
                        }))
                        .build()
                ))
                .setExpanded(true)
                .build()
            );

            return builder.build();
        };
    }
}
