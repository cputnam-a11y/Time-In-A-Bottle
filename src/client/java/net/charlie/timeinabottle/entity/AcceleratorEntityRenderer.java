package net.charlie.timeinabottle.entity;

import net.charlie.timeinabottle.TimeInABottle;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import org.joml.AxisAngle4d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector4f;

public class AcceleratorEntityRenderer extends EntityRenderer<AcceleratorEntity> {
    private static final Identifier RING_TEXTURE = new Identifier(TimeInABottle.MOD_ID, "textures/time_ring.png");

    public AcceleratorEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }
    @Override
    public void render(AcceleratorEntity entity,
                       float yaw, float tickDelta,
                       MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers,
                       int light) {
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
        VertexConsumer consumer = vertexConsumers.getBuffer(
                RenderLayer.getEntityTranslucent(this.getTexture(entity)));
        matrices.push();
        matrices.translate(0.5, 0.5, 0.5);
        Direction[] var12 = Direction.values();
        for (Direction dir : var12) {
            matrices.push();
            Vec3i dirVector = dir.getVector();
            float angle = (float) entity.age + tickDelta;
            matrices.multiply(
                    new Quaternionf(
                            new AxisAngle4d(
                                    Math.toRadians(angle),
                                    dirVector.getX(),
                                    dirVector.getY(),
                                    dirVector.getZ()
                            )
                    )
            );
            MatrixStack.Entry entry = matrices.peek();
            float offset = 0.5001F * (float) (dir.getDirection() == Direction.AxisDirection.NEGATIVE ? -1 : 1);
            Vector4f vec1;
            Vector4f vec2;
            Vector4f vec3;
            Vector4f vec4;
            if (dir.getAxis() == Direction.Axis.X) {
                vec1 = new Vector4f(offset, -0.5F, -0.5F, 1.0F);
                vec2 = new Vector4f(offset, 0.5F, -0.5F, 1.0F);
                vec3 = new Vector4f(offset, 0.5F, 0.5F, 1.0F);
                vec4 = new Vector4f(offset, -0.5F, 0.5F, 1.0F);
            } else if (dir.getAxis() == Direction.Axis.Y) {
                vec1 = new Vector4f(-0.5F, offset, -0.5F, 1.0F);
                vec2 = new Vector4f(-0.5F, offset, 0.5F, 1.0F);
                vec3 = new Vector4f(0.5F, offset, 0.5F, 1.0F);
                vec4 = new Vector4f(0.5F, offset, -0.5F, 1.0F);
            } else {
                vec1 = new Vector4f(-0.5F, -0.5F, offset, 1.0F);
                vec2 = new Vector4f(-0.5F, 0.5F, offset, 1.0F);
                vec3 = new Vector4f(0.5F, 0.5F, offset, 1.0F);
                vec4 = new Vector4f(0.5F, -0.5F, offset, 1.0F);
            }

            Matrix4f matrix = entry.getPositionMatrix();
            matrix.transform(vec1);
            matrix.transform(vec2);
            matrix.transform(vec3);
            matrix.transform(vec4);
            byte frame;
            if (entity.getTimeRate() >= 32) {
                frame = 5;
            } else if (entity.getTimeRate() >= 16) {
                frame = 4;
            } else if (entity.getTimeRate() >= 8) {
                frame = 3;
            } else if (entity.getTimeRate() >= 4) {
                frame = 2;
            } else if (entity.getTimeRate() >= 2) {
                frame = 1;
            } else {
                frame = 0;
            }

            float minU = (float) frame / 6.0F;
            float maxU = (float) (frame + 1) / 6.0F;
            consumer.vertex(vec1.x(), vec1.y(), vec1.z()).color(1.0F, 1.0F, 1.0F, 1.0F).texture(minU, 1.0F).overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal((float) dirVector.getX(), (float) dirVector.getY(), (float) dirVector.getZ()).next();
            consumer.vertex(vec2.x(), vec2.y(), vec2.z()).color(1.0F, 1.0F, 1.0F, 1.0F).texture(minU, 0.0F).overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal((float) dirVector.getX(), (float) dirVector.getY(), (float) dirVector.getZ()).next();
            consumer.vertex(vec3.x(), vec3.y(), vec3.z()).color(1.0F, 1.0F, 1.0F, 1.0F).texture(maxU, 0.0F).overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal((float) dirVector.getX(), (float) dirVector.getY(), (float) dirVector.getZ()).next();
            consumer.vertex(vec4.x(), vec4.y(), vec4.z()).color(1.0F, 1.0F, 1.0F, 1.0F).texture(maxU, 1.0F).overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal((float) dirVector.getX(), (float) dirVector.getY(), (float) dirVector.getZ()).next();
            matrices.pop();
        }

        matrices.pop();
    }

    @Override
    public Identifier getTexture(AcceleratorEntity entity) {
        return RING_TEXTURE;
    }

}
