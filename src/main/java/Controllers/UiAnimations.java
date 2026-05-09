package Controllers;

import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.Parent;

/**
 * Animations discrètes sur boutons et cartes (rafraîchi après chaque navigation).
 */
public final class UiAnimations {

    private static final String KEY_BTN = "purrly.anim.btn";

    private UiAnimations() {
    }

    public static void refresh(Scene scene) {

        if (scene == null || scene.getRoot() == null) {

            return;
        }

        traverse(scene.getRoot());
    }

    private static void traverse(Node node) {

        if (node == null) {

            return;
        }

        if (node instanceof ButtonBase bb) {

            attachButtonGrow(bb);
        }

        if (hasStyleClass(node, "cmd-order-card")) {

            attachLiftHover(
                    node,
                    "purrly.anim.cmd",
                    -8,
                    1.015,
                    220,
                    260
            );
        }

        if (hasStyleClass(node, "mes-produit-card")) {

            attachLiftHover(
                    node,
                    "purrly.anim.mes",
                    -5,
                    1.012,
                    160,
                    200
            );
        }

        /* Carte boutique : zoom / détails gérés dans ShopController (évite conflit). */

        if (hasStyleClass(node, "admin-produit-card")) {

            attachLiftHover(
                    node,
                    "purrly.anim.adminProd",
                    -4,
                    1.01,
                    150,
                    190
            );
        }

        if (hasStyleClass(node, "admin-commande-card")) {

            attachLiftHover(
                    node,
                    "purrly.anim.adminCmd",
                    -4,
                    1.01,
                    150,
                    190
            );
        }

        if (node instanceof ScrollPane sp) {

            traverse(sp.getContent());

            return;
        }

        if (node instanceof TabPane tp) {

            for (Tab t : tp.getTabs()) {

                traverse(t.getContent());
            }

            return;
        }

        if (node instanceof SplitPane split) {

            for (Node item : split.getItems()) {

                traverse(item);
            }

            return;
        }

        if (node instanceof Parent p) {

            for (Node child : p.getChildrenUnmodifiable()) {

                traverse(child);
            }
        }
    }

    private static boolean hasStyleClass(Node n, String clazz) {

        return n.getStyleClass().contains(clazz);
    }

    private static void attachButtonGrow(ButtonBase b) {

        if (Boolean.TRUE.equals(b.getProperties().get(KEY_BTN))) {

            return;
        }

        b.getProperties().put(KEY_BTN, true);

        ScaleTransition enter =
                new ScaleTransition(javafx.util.Duration.millis(130), b);

        enter.setToX(1.065);
        enter.setToY(1.065);
        enter.setInterpolator(Interpolator.EASE_OUT);

        ScaleTransition exit =
                new ScaleTransition(javafx.util.Duration.millis(200), b);

        exit.setToX(1.0);
        exit.setToY(1.0);
        exit.setInterpolator(Interpolator.EASE_OUT);

        b.setOnMouseEntered(e -> {

            if (!b.isDisabled()) {

                exit.stop();
                enter.playFromStart();
            }
        });

        b.setOnMouseExited(e -> {

            enter.stop();
            exit.playFromStart();
        });
    }

    private static void attachLiftHover(
            Node card,
            String key,
            double translateY,
            double scaleMul,
            int msEnter,
            int msExit
    ) {

        if (Boolean.TRUE.equals(card.getProperties().get(key))) {

            return;
        }

        card.getProperties().put(key, true);

        TranslateTransition tUp =
                new TranslateTransition(javafx.util.Duration.millis(msEnter), card);

        tUp.setToY(translateY);
        tUp.setInterpolator(Interpolator.EASE_OUT);

        ScaleTransition sUp =
                new ScaleTransition(javafx.util.Duration.millis(msEnter), card);

        sUp.setToX(scaleMul);
        sUp.setToY(scaleMul);
        sUp.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition up = new ParallelTransition(card, tUp, sUp);

        TranslateTransition tDown =
                new TranslateTransition(javafx.util.Duration.millis(msExit), card);

        tDown.setToY(0);
        tDown.setInterpolator(Interpolator.EASE_OUT);

        ScaleTransition sDown =
                new ScaleTransition(javafx.util.Duration.millis(msExit), card);

        sDown.setToX(1.0);
        sDown.setToY(1.0);
        sDown.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition down = new ParallelTransition(card, tDown, sDown);

        card.setOnMouseEntered(e -> {

            down.stop();
            up.playFromStart();
        });

        card.setOnMouseExited(e -> {

            up.stop();
            down.playFromStart();
        });
    }
}
