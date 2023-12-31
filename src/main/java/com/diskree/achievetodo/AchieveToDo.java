package com.diskree.achievetodo;

import com.diskree.achievetodo.action.BlockedActionType;
import com.diskree.achievetodo.advancements.AdvancementsTab;
import com.diskree.achievetodo.advancements.RandomAdvancements;
import com.diskree.achievetodo.advancements.hints.*;
import com.diskree.achievetodo.client.SpyglassPanoramaDetails;
import com.diskree.achievetodo.datagen.AdvancementsGenerator;
import com.mojang.brigadier.Command;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.player.*;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.advancement.PlacedAdvancement;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.*;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.scoreboard.ReadableScoreboardScore;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Direction;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.World;

public class AchieveToDo implements ModInitializer {

    public static final String ID = BuildConfig.MOD_ID;

    public static final String BACAP_SCORE_OBJECTIVE = "bac_advancements";
    public static final String BACAP_DATA_PACK = "file/bacap.zip";
    public static final String BACAP_HARDCORE_DATA_PACK = "file/bacap_hardcore.zip";
    public static final String BACAP_TERRALITH_DATA_PACK = "file/bacap_terralith.zip";
    public static final String BACAP_AMPLIFIED_NETHER_DATA_PACK = "file/bacap_amplified_nether.zip";
    public static final String BACAP_NULLSCAPE_DATA_PACK = "file/bacap_nullscape.zip";

    public static final String TERRALITH_DATA_PACK = "file/terralith.zip";
    public static final String AMPLIFIED_NETHER_DATA_PACK = "file/amplified_nether.zip";
    public static final String NULLSCAPE_DATA_PACK = "file/nullscape.zip";

    public static final Identifier BACAP_OVERRIDE_DATA_PACK = new Identifier(ID, "bacap_override");
    public static final Identifier BACAP_OVERRIDE_HARDCORE_DATA_PACK = new Identifier(ID, "bacap_override_hardcore");
    public static final Identifier BACAP_REWARDS_ITEM_DATA_PACK_NAME = new Identifier(ID, "bacap_rewards_item");
    public static final Identifier BACAP_REWARDS_EXPERIENCE_DATA_PACK_NAME = new Identifier(ID, "bacap_rewards_experience");
    public static final Identifier BACAP_REWARDS_TROPHY_DATA_PACK_NAME = new Identifier(ID, "bacap_rewards_trophy");
    public static final Identifier BACAP_COOPERATIVE_MODE_DATA_PACK_NAME = new Identifier(ID, "bacap_cooperative_mode");

    public static final Identifier GRANT_BLOCKED_ACTION_PACKET_ID = new Identifier(ID, "grant_blocked_action");

    public static final Identifier ANCIENT_CITY_PORTAL_BLOCK_ID = new Identifier(ID, "ancient_city_portal");
    public static final AncientCityPortalBlock ANCIENT_CITY_PORTAL_BLOCK = new AncientCityPortalBlock(AbstractBlock.Settings.create()
            .noCollision()
            .ticksRandomly()
            .strength(-1.0f, 3600000.0f)
            .dropsNothing()
            .sounds(BlockSoundGroup.GLASS)
            .luminance((blockState) -> 11)
            .pistonBehavior(PistonBehavior.BLOCK));

    public static final Identifier REINFORCED_DEEPSLATE_CHARGED_BLOCK_ID = new Identifier(ID, "reinforced_deepslate_charged");
    public static final Block REINFORCED_DEEPSLATE_CHARGED_BLOCK = new Block(FabricBlockSettings.create());

    public static final Identifier REINFORCED_DEEPSLATE_BROKEN_BLOCK_ID = new Identifier(ID, "reinforced_deepslate_broken");
    public static final Block REINFORCED_DEEPSLATE_BROKEN_BLOCK = new Block(FabricBlockSettings.create());

    public static final Identifier MYSTIFIED_BLOCKED_ACTION_LABEL_ITEM_ID = new Identifier(ID, "mystified_label");
    public static final Item MYSTIFIED_BLOCKED_ACTION_LABEL_ITEM = new Item(new FabricItemSettings());

    public static final Identifier ANCIENT_CITY_PORTAL_HINT_ITEM_ID = new Identifier(ID, "ancient_city_portal_hint");
    public static final Item ANCIENT_CITY_PORTAL_HINT_ITEM = new Item(new FabricItemSettings().maxDamage(1000));

    public static final Identifier ANCIENT_CITY_PORTAL_PARTICLES_ID = new Identifier(ID, "ancient_city_portal_particles");
    public static final DefaultParticleType ANCIENT_CITY_PORTAL_PARTICLES = FabricParticleTypes.simple();

    public static final Identifier ANCIENT_CITY_PORTAL_TAB_ENTITY_ID = new Identifier(ID, "ancient_city_portal_tab_entity");
    public static final EntityType<AncientCityPortalTabEntity> ANCIENT_CITY_PORTAL_TAB = FabricEntityTypeBuilder.create(SpawnGroup.MISC, AncientCityPortalTabEntity::new)
            .dimensions(EntityDimensions.changing(0.0f, 0.0f))
            .trackRangeChunks(10)
            .trackedUpdateRate(1)
            .build();
    public static final Identifier ANCIENT_CITY_PORTAL_ADVANCEMENT_ENTITY_ID = new Identifier(ID, "ancient_city_portal_advancement_entity");
    public static final EntityType<AncientCityPortalEntity> ANCIENT_CITY_PORTAL_ADVANCEMENT = FabricEntityTypeBuilder.create(SpawnGroup.MISC, AncientCityPortalEntity::new)
            .dimensions(EntityDimensions.changing(0.0f, 0.0f))
            .trackRangeChunks(10)
            .trackedUpdateRate(1)
            .build();

    public static final Identifier ANCIENT_CITY_PORTAL_HINT_ENTITY_ID = new Identifier(ID, "ancient_city_portal_prompt_entity");
    public static final EntityType<AncientCityPortalPromptEntity> ANCIENT_CITY_PORTAL_HINT = FabricEntityTypeBuilder.create(SpawnGroup.MISC, AncientCityPortalPromptEntity::new)
            .dimensions(EntityDimensions.changing(0.0f, 0.0f))
            .trackRangeChunks(10)
            .trackedUpdateRate(1)
            .build();

    public static final Identifier ANCIENT_CITY_PORTAL_EXPERIENCE_ORB_ENTITY_ID = new Identifier(ID, "ancient_city_portal_experience_orb");
    public static final EntityType<AncientCityPortalExperienceOrbEntity> ANCIENT_CITY_PORTAL_EXPERIENCE_ORB = FabricEntityTypeBuilder.<AncientCityPortalExperienceOrbEntity>create(SpawnGroup.MISC, AncientCityPortalExperienceOrbEntity::new)
            .dimensions(EntityDimensions.changing(0.5f, 0.5f))
            .trackRangeChunks(6)
            .trackedUpdateRate(20)
            .build();

    public static final SoundEvent MUSIC_DISC_5_ACTIVATOR = SoundEvent.of(new Identifier(ID, "music_disc_5_activator"));

    private static int advancementsCount;

    public static int getScore(PlayerEntity player) {
        if (player == null) {
            return 0;
        }
        Scoreboard scoreboard = player.getScoreboard();
        if (scoreboard == null) {
            return 0;
        }
        ScoreboardObjective scoreObjective = scoreboard.getNullableObjective(BACAP_SCORE_OBJECTIVE);
        if (scoreObjective == null) {
            return 0;
        }
        ReadableScoreboardScore playerScore = scoreboard.getScore(ScoreHolder.fromName(player.getNameForScoreboard()), scoreObjective);
        if (playerScore == null) {
            return 0;
        }
        return playerScore.getScore();
    }

    @Override
    public void onInitialize() {
        registerDataPacks();
        registerBlocks();
        registerItems();
        registerParticles();
        registerEntities();

        CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, environment) -> dispatcher.register(CommandManager.literal("advRandom").executes((context -> {
            ServerCommandSource source = context.getSource();
            PlacedAdvancement placedAdvancement = RandomAdvancements.getAdvancement(source.getPlayer());
            if (placedAdvancement != null) {
                AdvancementDisplay display = placedAdvancement.getAdvancement().display().orElse(null);
                AdvancementDisplay rootDisplay = placedAdvancement.getRoot().getAdvancement().display().orElse(null);
                if (display == null || rootDisplay == null) {
                    source.sendMessage(Text.of("Parsing error for advancement: " + placedAdvancement.getAdvancementEntry().id()).copy().formatted(Formatting.RED));
                    return Command.SINGLE_SUCCESS;
                }
                final String separator = "----------";
                source.sendMessage(Text.of(separator).copy().formatted(Formatting.GOLD));
                source.sendMessage(rootDisplay.getTitle().copy().formatted(Formatting.BLUE, Formatting.BOLD));
                source.sendMessage(display.getTitle().copy().formatted(Formatting.AQUA, Formatting.ITALIC));
                source.sendMessage(display.getDescription().copy().formatted(Formatting.YELLOW));
                source.sendMessage(Text.of(separator).copy().formatted(Formatting.GOLD));
            } else {
                source.sendMessage(Text.translatable("commands.random.no_advancements"));
            }
            return Command.SINGLE_SUCCESS;
        }))));
        ServerPlayNetworking.registerGlobalReceiver(GRANT_BLOCKED_ACTION_PACKET_ID, (server, player, handler, buf, responseSender) -> {
            BlockedActionType action = buf.readEnumConstant(BlockedActionType.class);
            boolean isDemystifyOnly = buf.readBoolean();
            if (action != null) {
                server.execute(() -> grantBlockedAction(player, action, isDemystifyOnly));
            }
        });
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            ItemStack stack = player.getStackInHand(hand);
            if (world != null && world.getRegistryKey() == World.OVERWORLD && pos != null) {
                if (pos.getY() >= 0 && isActionBlocked(player, BlockedActionType.BREAK_BLOCKS_IN_POSITIVE_Y)) {
                    return ActionResult.FAIL;
                }
                if (pos.getY() < 0 && isActionBlocked(player, BlockedActionType.BREAK_BLOCKS_IN_NEGATIVE_Y)) {
                    return ActionResult.FAIL;
                }
            }
            if (isToolBlocked(player, stack)) {
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            BlockState block = world.getBlockState(hitResult.getBlockPos());
            ItemStack stack = player.getStackInHand(hand);
            if (isBlockBlocked(player, block.getBlock())) {
                return ActionResult.FAIL;
            }
            if (isToolBlocked(player, stack)) {
                return ActionResult.FAIL;
            }
            if (stack.getItem() instanceof AliasedBlockItem aliasedBlockItem) {
                if (!block.isOf(Blocks.FARMLAND) || hitResult.getSide() != Direction.UP || !aliasedBlockItem.getBlock().getDefaultState().isIn(BlockTags.MAINTAINS_FARMLAND)) {
                    if (isItemBlocked(player, stack)) {
                        return ActionResult.FAIL;
                    }
                }
            } else {
                if (isItemBlocked(player, stack)) {
                    return ActionResult.FAIL;
                }
            }
            return ActionResult.PASS;
        });
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getStackInHand(hand);
            if (isItemBlocked(player, stack)) {
                return TypedActionResult.fail(ItemStack.EMPTY);
            }
            return TypedActionResult.pass(ItemStack.EMPTY);
        });
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            ItemStack stack = player.getStackInHand(hand);
            if (isToolBlocked(player, stack)) {
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (entity instanceof BoatEntity && isActionBlocked(player, BlockedActionType.USING_BOAT)) {
                return ActionResult.FAIL;
            }
            if (entity instanceof VillagerEntity villagerEntity && isVillagerBlocked(player, villagerEntity.getVillagerData().getProfession())) {
                villagerEntity.sayNo();
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });
        ServerWorldEvents.LOAD.register((server, world) -> advancementsCount = 0);
    }

    public static void grantHintsAdvancement(ServerPlayerEntity player, String pathName) {
        if (player == null) {
            return;
        }
        AdvancementEntry advancement = player.server.getAdvancementLoader().get(new Identifier(AdvancementsTab.HINTS.getBasePath() + "/" + pathName));
        AdvancementProgress advancementProgress = player.getAdvancementTracker().getProgress(advancement);
        for (String criterion : advancementProgress.getUnobtainedCriteria()) {
            player.getAdvancementTracker().grantCriterion(advancement, criterion);
        }
    }

    private boolean isItemBlocked(PlayerEntity player, ItemStack stack) {
        if (isFoodBlocked(player, stack.getItem().getFoodComponent())) {
            return true;
        }
        if (isEquipmentBlocked(player, stack)) {
            return true;
        }
        if (stack.isOf(Items.SHIELD) && isActionBlocked(player, BlockedActionType.USING_SHIELD)) {
            return true;
        }
        if (stack.isOf(Items.BOW) && isActionBlocked(player, BlockedActionType.USING_BOW)) {
            return true;
        }
        if (stack.isOf(Items.FIREWORK_ROCKET) && player.isFallFlying() && isActionBlocked(player, BlockedActionType.USING_FIREWORKS_WHILE_FLY)) {
            return true;
        }
        if (stack.isOf(Items.SPYGLASS) && player.getWorld().isClient) {
            SpyglassPanoramaDetails panoramaDetails = SpyglassPanoramaDetails.of(stack);
            return panoramaDetails != null && !panoramaDetails.isPanoramaReady();
        }
        return false;
    }

    public static boolean isActionBlocked(PlayerEntity player, BlockedActionType action) {
        return isActionBlocked(player, action, false);
    }

    public static boolean isActionBlocked(PlayerEntity player, BlockedActionType action, boolean isCheckOnly) {
        if (action == null || player == null || player.isCreative() || action.isUnblocked(player)) {
            return false;
        }
        if (!isCheckOnly) {
            player.sendMessage(action.buildBlockedDescription(player), true);
            if (player.getWorld().isClient) {
                ClientPlayNetworking.send(GRANT_BLOCKED_ACTION_PACKET_ID, PacketByteBufs.create().writeEnumConstant(action).writeBoolean(true));
            } else if (player instanceof ServerPlayerEntity serverPlayer) {
                grantBlockedAction(serverPlayer, action, true);
            }
        }
        return true;
    }

    public static boolean isFoodBlocked(PlayerEntity player, FoodComponent food) {
        if (food == null) {
            return false;
        }
        BlockedActionType action = BlockedActionType.findBlockedFood(food);
        if (action != null) {
            return isActionBlocked(player, action);
        }
        return false;
    }

    public static boolean isBlockBlocked(PlayerEntity player, Block block) {
        if (block == null) {
            return false;
        }
        BlockedActionType action = BlockedActionType.findBlockedBlock(block);
        if (action != null) {
            return isActionBlocked(player, action);
        }
        return false;
    }

    private boolean isToolBlocked(PlayerEntity player, ItemStack stack) {
        if (stack == null) {
            return false;
        }
        BlockedActionType action = BlockedActionType.findBlockedTool(stack.getItem());
        if (action != null) {
            return isActionBlocked(player, action);
        }
        return false;
    }

    public static boolean isEquipmentBlocked(PlayerEntity player, ItemStack stack) {
        if (stack == null) {
            return false;
        }
        BlockedActionType action = BlockedActionType.findBlockedEquipment(stack.getItem());
        if (action != null) {
            return isActionBlocked(player, action);
        }
        return false;
    }

    public static boolean isDimensionBlocked(PlayerEntity player, RegistryKey<World> dimension) {
        if (dimension == null) {
            return false;
        }
        BlockedActionType action = BlockedActionType.findBlockedDimension(dimension);
        if (action != null) {
            return isActionBlocked(player, action);
        }
        return false;
    }

    private boolean isVillagerBlocked(PlayerEntity player, VillagerProfession profession) {
        if (profession == null) {
            return false;
        }
        BlockedActionType action = BlockedActionType.findBlockedVillager(profession);
        if (action != null) {
            return isActionBlocked(player, action);
        }
        return false;
    }

    @Environment(EnvType.CLIENT)
    public static void setAdvancementsCount(int count) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || count >= 0 && count <= advancementsCount) {
            return;
        }
        int oldCount = advancementsCount;
        advancementsCount = count;
        if (oldCount != 0) {
            for (BlockedActionType action : BlockedActionType.values()) {
                if (advancementsCount >= action.getUnblockAdvancementsCount() && oldCount < action.getUnblockAdvancementsCount()) {
                    ClientPlayNetworking.send(GRANT_BLOCKED_ACTION_PACKET_ID, PacketByteBufs.create().writeEnumConstant(action).writeBoolean(false));
                }
            }
        }
    }

    private static void grantBlockedAction(ServerPlayerEntity player, BlockedActionType action, boolean isDemystifyOnly) {
        AdvancementEntry advancement = player.server.getAdvancementLoader().get(action.buildAdvancementId());
        if (isDemystifyOnly) {
            player.getAdvancementTracker().grantCriterion(
                    advancement,
                    AdvancementsGenerator.BLOCKED_ACTION_DEMYSTIFIED_CRITERION_PREFIX + action.getName()
            );
        } else {
            for (String criterion : player.getAdvancementTracker().getProgress(advancement).getUnobtainedCriteria()) {
                player.getAdvancementTracker().grantCriterion(advancement, criterion);
            }
        }
    }

    private void registerDataPacks() {
        FabricLoader.getInstance().getModContainer(ID).ifPresent(modContainer -> {
            ResourceManagerHelper.registerBuiltinResourcePack(BACAP_OVERRIDE_DATA_PACK, modContainer, ResourcePackActivationType.NORMAL);
            ResourceManagerHelper.registerBuiltinResourcePack(BACAP_OVERRIDE_HARDCORE_DATA_PACK, modContainer, ResourcePackActivationType.NORMAL);
            ResourceManagerHelper.registerBuiltinResourcePack(BACAP_COOPERATIVE_MODE_DATA_PACK_NAME, modContainer, ResourcePackActivationType.NORMAL);
            ResourceManagerHelper.registerBuiltinResourcePack(BACAP_REWARDS_ITEM_DATA_PACK_NAME, modContainer, ResourcePackActivationType.NORMAL);
            ResourceManagerHelper.registerBuiltinResourcePack(BACAP_REWARDS_EXPERIENCE_DATA_PACK_NAME, modContainer, ResourcePackActivationType.NORMAL);
            ResourceManagerHelper.registerBuiltinResourcePack(BACAP_REWARDS_TROPHY_DATA_PACK_NAME, modContainer, ResourcePackActivationType.NORMAL);
        });
    }

    private void registerBlocks() {
        Registry.register(Registries.BLOCK, ANCIENT_CITY_PORTAL_BLOCK_ID, ANCIENT_CITY_PORTAL_BLOCK);
        Registry.register(Registries.BLOCK, REINFORCED_DEEPSLATE_CHARGED_BLOCK_ID, REINFORCED_DEEPSLATE_CHARGED_BLOCK);
        Registry.register(Registries.BLOCK, REINFORCED_DEEPSLATE_BROKEN_BLOCK_ID, REINFORCED_DEEPSLATE_BROKEN_BLOCK);
    }

    private void registerItems() {
        Registry.register(Registries.ITEM, MYSTIFIED_BLOCKED_ACTION_LABEL_ITEM_ID, MYSTIFIED_BLOCKED_ACTION_LABEL_ITEM);
        Registry.register(Registries.ITEM, ANCIENT_CITY_PORTAL_HINT_ITEM_ID, ANCIENT_CITY_PORTAL_HINT_ITEM);
        Registry.register(Registries.ITEM, REINFORCED_DEEPSLATE_CHARGED_BLOCK_ID, new BlockItem(REINFORCED_DEEPSLATE_CHARGED_BLOCK, new FabricItemSettings()));
        Registry.register(Registries.ITEM, REINFORCED_DEEPSLATE_BROKEN_BLOCK_ID, new BlockItem(REINFORCED_DEEPSLATE_BROKEN_BLOCK, new FabricItemSettings()));
    }

    private void registerParticles() {
        Registry.register(Registries.PARTICLE_TYPE, ANCIENT_CITY_PORTAL_PARTICLES_ID, ANCIENT_CITY_PORTAL_PARTICLES);
    }

    private void registerEntities() {
        Registry.register(Registries.ENTITY_TYPE, ANCIENT_CITY_PORTAL_TAB_ENTITY_ID, ANCIENT_CITY_PORTAL_TAB);
        Registry.register(Registries.ENTITY_TYPE, ANCIENT_CITY_PORTAL_ADVANCEMENT_ENTITY_ID, ANCIENT_CITY_PORTAL_ADVANCEMENT);
        Registry.register(Registries.ENTITY_TYPE, ANCIENT_CITY_PORTAL_HINT_ENTITY_ID, ANCIENT_CITY_PORTAL_HINT);
        Registry.register(Registries.ENTITY_TYPE, ANCIENT_CITY_PORTAL_EXPERIENCE_ORB_ENTITY_ID, ANCIENT_CITY_PORTAL_EXPERIENCE_ORB);
    }
}
