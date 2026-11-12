package ru.method.clarityvisuals.punchy.client.modelparts;

import net.minecraft.class_2960;
import net.minecraft.class_3300;
import ru.method.clarityvisuals.punchy.client.model.ResourcePackGeoRegistry;
import ru.method.clarityvisuals.punchy.client.model.ResourcePackItemModelRegistry;
import ru.method.clarityvisuals.punchy.client.model.SimpleBedrockGeo;
import ru.method.clarityvisuals.punchy.client.model.SimpleBedrockGeoThirdPerson;
import ru.method.clarityvisuals.punchy.util.ResourceLocationCompat;

final class ResourceStateMachine {
    private State state = State.UNLOADED;

    ResourceStateMachine() {
    }

    void load(class_3300 class_33002) {
        if (class_33002 == null) {
            this.state = State.FAILED;
        } else {
            this.state = State.LOADING;
            try {
                class_2960 class_29602 = ResourceLocationCompat.fromNamespaceAndPath("punchy", "geo/player.geo.json");
                SimpleBedrockGeo.load(class_29602);
                class_2960 class_29603 = ResourceLocationCompat.fromNamespaceAndPath("punchy", "geo/player_3_per.geo.json");
                SimpleBedrockGeoThirdPerson.load(class_29603);
                ResourcePackGeoRegistry.load(class_33002);
                ResourcePackItemModelRegistry.load(class_33002);
                this.state = State.READY;
            }
            catch (Throwable throwable) {
                this.state = State.FAILED;
            }
        }
    }

    State getState() {
        return this.state;
    }

    static enum State {
        UNLOADED,
        LOADING,
        READY,
        FAILED;

    }
}

