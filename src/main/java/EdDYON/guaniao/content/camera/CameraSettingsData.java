package EdDYON.guaniao.content.camera;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class CameraSettingsData {
    private static final String TAG_FILTER = "CameraFilter";
    private static final String TAG_LENS = "CameraLens";
    private static final String TAG_SHOOTING_MODE = "CameraShootingMode";
    private static final String TAG_FOCAL_LENGTH = "CameraFocalLength";
    private static final String TAG_APERTURE = "CameraAperture";
    private static final String TAG_FOCUS_MODE = "CameraFocusMode";
    private static final String TAG_FOCUS_DISTANCE = "CameraFocusDistance";

    private CameraSettingsData() {
    }

    public static CameraFilter filter(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? CameraFilter.NONE : CameraFilter.byId(data.copyTag().getIntOr(TAG_FILTER, 0));
    }

    public static void setFilter(ItemStack stack, CameraFilter filter) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putInt(TAG_FILTER, filter.id()));
    }

    public static CameraState state(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        CameraState defaults = CameraState.defaults();
        if (data == null) {
            return defaults;
        }
        CompoundTag tag = data.copyTag();
        return new CameraState(
                CameraFilter.byId(tag.getIntOr(TAG_FILTER, 0)),
                tag.contains(TAG_LENS) ? CameraLens.byId(tag.getIntOr(TAG_LENS, 0)) : defaults.lens(),
                tag.contains(TAG_SHOOTING_MODE)
                        ? CameraShootingMode.byId(tag.getIntOr(TAG_SHOOTING_MODE, 0))
                        : defaults.shootingMode(),
                tag.contains(TAG_FOCAL_LENGTH) ? tag.getDoubleOr(TAG_FOCAL_LENGTH, 0.0D) : defaults.focalLength(),
                tag.contains(TAG_APERTURE) ? CameraAperture.byId(tag.getIntOr(TAG_APERTURE, 0)) : defaults.aperture(),
                tag.contains(TAG_FOCUS_MODE) ? CameraFocusMode.byId(tag.getIntOr(TAG_FOCUS_MODE, 0)) : defaults.focusMode(),
                tag.contains(TAG_FOCUS_DISTANCE) ? tag.getDoubleOr(TAG_FOCUS_DISTANCE, 0.0D) : defaults.focusDistance()
        );
    }

    public static void setState(ItemStack stack, CameraState state) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putInt(TAG_FILTER, state.filter().id());
            tag.putInt(TAG_LENS, state.lens().id());
            tag.putInt(TAG_SHOOTING_MODE, state.shootingMode().id());
            tag.putDouble(TAG_FOCAL_LENGTH, state.focalLength());
            tag.putInt(TAG_APERTURE, state.aperture().id());
            tag.putInt(TAG_FOCUS_MODE, state.focusMode().id());
            tag.putDouble(TAG_FOCUS_DISTANCE, state.focusDistance());
        });
    }
}
