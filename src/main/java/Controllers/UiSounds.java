package Controllers;

import javafx.scene.media.AudioClip;

/**
 * Sons UI (optionnels) avec fallback silencieux si ressource absente.
 */
public final class UiSounds {

    private static final String NAV_MEOW_RESOURCE = "/sounds/meow.mp3";

    private static AudioClip navMeow;

    private UiSounds() {
    }

    public static void playNavMeow() {
        AudioClip clip = getNavMeow();
        if (clip == null) {
            return;
        }
        clip.play(0.22);
    }

    private static AudioClip getNavMeow() {
        if (navMeow != null) {
            return navMeow;
        }
        try {
            var url = UiSounds.class.getResource(NAV_MEOW_RESOURCE);
            if (url == null) {
                return null;
            }
            navMeow = new AudioClip(url.toExternalForm());
            return navMeow;
        } catch (Exception e) {
            return null;
        }
    }
}

