package Controllers;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public final class UiNavigation {

    private UiNavigation() {}

    public static void showInMainShell(Scene scene, Parent content) {

        // 🐱 Cat peek + 🔊 Meow depuis GestionProduitController
        if (GestionProduitController.instance != null) {
            GestionProduitController.instance.triggerCatPeek();
            GestionProduitController.instance.playMeow();
        }

        Node root = scene.getRoot();
        StackPane mainContent = null;

        // Root est StackPane (avec chat) contenant un BorderPane
        if (root instanceof StackPane rootStack) {
            for (Node child : rootStack.getChildren()) {
                if (child instanceof BorderPane bp
                        && bp.getCenter() instanceof StackPane sp) {
                    mainContent = sp;
                    break;
                }
            }
        }
        // Root est directement un BorderPane
        else if (root instanceof BorderPane bp
                && bp.getCenter() instanceof StackPane sp) {
            mainContent = sp;
        }

        if (mainContent != null) {
            replaceStackAnimated(mainContent, content);
        } else {
            scene.setRoot(content);
        }

        Platform.runLater(() -> UiAnimations.refresh(scene));
    }

    private static void replaceStackAnimated(StackPane sp, Parent content) {
        Node old = sp.getChildren().isEmpty()
                ? null
                : sp.getChildren().get(sp.getChildren().size() - 1);

        content.setOpacity(0);
        content.setTranslateX(18);
        StackPane.setAlignment(content, Pos.CENTER);
        sp.getChildren().add(content);

        FadeTransition inFade = new FadeTransition(Duration.millis(240), content);
        inFade.setToValue(1);
        inFade.setInterpolator(Interpolator.EASE_BOTH);

        TranslateTransition inSlide = new TranslateTransition(Duration.millis(300), content);
        inSlide.setToX(0);
        inSlide.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition in = new ParallelTransition(inFade, inSlide);

        if (old == null) {
            in.play();
            return;
        }

        FadeTransition outFade = new FadeTransition(Duration.millis(160), old);
        outFade.setToValue(0);
        outFade.setInterpolator(Interpolator.EASE_BOTH);

        TranslateTransition outSlide = new TranslateTransition(Duration.millis(200), old);
        outSlide.setToX(-14);
        outSlide.setInterpolator(Interpolator.EASE_IN);

        ParallelTransition out = new ParallelTransition(outFade, outSlide);
        out.setOnFinished(e -> sp.getChildren().remove(old));

        out.play();
        in.play();
    }
}