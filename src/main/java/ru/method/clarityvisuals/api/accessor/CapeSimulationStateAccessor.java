package ru.method.clarityvisuals.api.accessor;

import ru.method.clarityvisuals.modules.impl.render.cosmetic.CapeSimulation;

public interface CapeSimulationStateAccessor {
    public CapeSimulation clarityvisuals$capeSimulation();

    public void clarityvisuals$setCapeSimulation(CapeSimulation var1);

    public float clarityvisuals$capeTickDelta();

    public void clarityvisuals$setCapeTickDelta(float var1);

    public boolean clarityvisuals$capeCrouching();

    public void clarityvisuals$setCapeCrouching(boolean var1);
}

