package com.constructions.client;

import com.constructions.ConstructionsConfig;
import com.constructions.items.StructureItem;
import com.constructions.utils.StructurePlacementUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/**
 * Клиентские события для рендера голограммы структуры.
 */
@Mod.EventBusSubscriber(modid = com.constructions.ConstructionsMod.MODID, value = Dist.CLIENT)
public final class ClientEvents {
    private ClientEvents() {
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || minecraft.hitResult == null) {
            return;
        }

        if (!ConstructionsConfig.Common.ENABLE_HOLOGRAM_PREVIEW) {
            return;
        }

        ItemStack stack = minecraft.player.getMainHandItem();
        if (!(stack.getItem() instanceof StructureItem structureItem)) {
            return;
        }

        if (!(minecraft.hitResult instanceof BlockHitResult blockHitResult)) {
            return;
        }

        BlockPos basePosition = blockHitResult.getBlockPos();
        StructurePlacementUtils.PlacementPreview preview = StructurePlacementUtils.getPlacementPreview(
            minecraft.level,
            structureItem.getStructureType(),
            basePosition,
            blockHitResult.getDirection(),
            minecraft.player.getUUID(),
            minecraft.player.getYRot()
        );
        List<BlockPos> previewBlocks = preview.blocks();

        PoseStack poseStack = event.getPoseStack();
        Vec3 cameraPos = event.getCamera().getPosition();
        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lines());
        float red = preview.valid() ? 0.2F : 1.0F;
        float green = preview.valid() ? 0.85F : 0.2F;
        float blue = preview.valid() ? 1.0F : 0.2F;

        for (BlockPos pos : previewBlocks) {
            LevelRenderer.renderLineBox(
                    poseStack,
                    consumer,
                    new net.minecraft.world.phys.AABB(pos),
                red,
                green,
                blue,
                    0.8F
            );
        }

        bufferSource.endBatch(RenderType.lines());
        poseStack.popPose();
    }
}