package ru.method.clarityvisuals.media;

import ru.method.clarityvisuals.media.MediaSessionTracker;

public final class MediaSessionRegistry {
    private static final MediaSessionTracker lX = new MediaSessionTracker();

    private MediaSessionRegistry() {
    }

    public static MediaSessionTracker dy() {
        return lX;
    }
}

