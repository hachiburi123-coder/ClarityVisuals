package ru.method.clarityvisuals.api.event;

import lombok.Generated;
import ru.method.clarityvisuals.api.accessor.MinecraftClientAccess;

public final class CancellationState
implements MinecraftClientAccess {
    private static final CancellationState Tl = new CancellationState();

    private CancellationState() {
    }

    public void cancel() {
    }

    public boolean isActive() {
        return false;
    }

    @Generated
    public static CancellationState mE() {
        return Tl;
    }
}

