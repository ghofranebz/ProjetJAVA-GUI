package Controllers;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * Toast notifications animées (non bloquantes).
 */
public final class UiToasts {

    private static final String KEY_LAYER = "purrly.toast.layer";

    private UiToasts() {
    }

    public static void showSuccess(Scene scene, String message) {
        show(scene, message, "toast toast-success");
    }

    public static void showInfo(Scene scene, String message) {
        show(scene, message, "toast toast-info");
    }

    public static void showError(Scene scene, String message) {
        show(scene, message, "toast toast-error");
    }

    private static void show(Scene scene, String message, String styleClasses) {
        if (scene == null || scene.getRoot() == null) {
            return;
        }
        Platform.runLater(() -> {
            StackPane layer = ensureLayer(scene);
            HBox toast = buildToast(message, styleClasses);
            layer.getChildren().add(toast);
            StackPane.setAlignment(toast, Pos.BOTTOM_RIGHT);
            StackPane.setMargin(toast, new Insets(0, 24, 22, 0));
            playToastLifecycle(layer, toast);
        });
    }

    private static HBox buildToast(String message, String styleClasses) {
        Label txt = new Label(message == null ? "" : message);
        txt.getStyleClass().add("toast-text");
        txt.setWrapText(true);
        HBox box = new HBox(txt);
        for (String c : styleClasses.split("\\s+")) {
            if (!c.isBlank()) {
                box.getStyleClass().add(c.trim());
            }
        }
        box.setOpacity(0);
        box.setTranslateY(10);
        return box;
    }

    private static void playToastLifecycle(Pane layer, Node toast) {
        FadeTransition inFade = new FadeTransition(Duration.millis(180), toast);
        inFade.setToValue(1);
        inFade.setInterpolator(Interpolator.EASE_OUT);

        TranslateTransition inSlide = new TranslateTransition(Duration.millis(220), toast);
        inSlide.setToY(0);
        inSlide.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition in = new ParallelTransition(inFade, inSlide);

        PauseTransition stay = new PauseTransition(Duration.millis(2200));

        FadeTransition outFade = new FadeTransition(Duration.millis(220), toast);
        outFade.setToValue(0);
        outFade.setInterpolator(Interpolator.EASE_BOTH);

        TranslateTransition outSlide = new TranslateTransition(Duration.millis(240), toast);
        outSlide.setToY(10);
        outSlide.setInterpolator(Interpolator.EASE_IN);

        ParallelTransition out = new ParallelTransition(outFade, outSlide);
        out.setOnFinished(e -> layer.getChildren().remove(toast));

        new javafx.animation.SequentialTransition(in, stay, out).play();
    }

    private static StackPane ensureLayer(Scene scene) {
        Object cached = scene.getProperties().get(KEY_LAYER);
        if (cached instanceof StackPane sp) {
            return sp;
        }

        if (scene.getRoot() instanceof StackPane existing) {
            scene.getProperties().put(KEY_LAYER, existing);
            return existing;
        }

        StackPane layered = new StackPane(scene.getRoot());
        layered.setPickOnBounds(false);
        scene.setRoot(layered);
        scene.getProperties().put(KEY_LAYER, layered);
        return layered;
    }
}

