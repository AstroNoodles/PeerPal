package com.github.astronoodles.peerpal.extras;

import javafx.animation.*;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 * Animate a shake effect on the given node
 * <p>
 * Based on CachedTimelineTransition, a Transition that uses a Timeline internally
 * and turns SPEED caching on for the animated node during the animation.
 * <p>
 * https://github.com/fxexperience/code/blob/master/FXExperienceControls/src/com/fxexperience/javafx/animation/CachedTimelineTransition.java
 * <p>
 * and ShakeTransition
 * <p>
 * https://github.com/fxexperience/code/blob/master/FXExperienceControls/src/com/fxexperience/javafx/animation/ShakeTransition.java
 *
 * Taken from: https://stackoverflow.com/questions/29911552/how-to-shake-a-login-dialog-in-javafx-8
 * @author Jasper Potts
 */
public class ShakeTransition extends Transition {

    private final Interpolator WEB_EASE = Interpolator.SPLINE(0.25, 0.1, 0.25, 1);
    private final Timeline timeline;
    private final Node node;
    private boolean oldCache = false;
    private CacheHint oldCacheHint = CacheHint.DEFAULT;
    private final boolean useCache = true;
    private final double xIni;

    private final DoubleProperty x = new SimpleDoubleProperty();

    /**
     * Create new ShakeTransition
     *
     * @param node The node to affect
     */
    public ShakeTransition(final Node node, EventHandler<ActionEvent> event) {
        this.node = node;
        statusProperty().addListener((ov, t, newStatus) -> {
            if (newStatus == Status.RUNNING) {
                starting();
            } else {
                stopping();
            }
        });

        this.timeline = new Timeline(
                new KeyFrame(Duration.millis(0), new KeyValue(x, 0, WEB_EASE)),
                new KeyFrame(Duration.millis(100), new KeyValue(x, -10, WEB_EASE)),
                new KeyFrame(Duration.millis(200), new KeyValue(x, 10, WEB_EASE)),
                new KeyFrame(Duration.millis(300), new KeyValue(x, -10, WEB_EASE)),
                new KeyFrame(Duration.millis(400), new KeyValue(x, 10, WEB_EASE)),
                new KeyFrame(Duration.millis(500), new KeyValue(x, -10, WEB_EASE)),
                new KeyFrame(Duration.millis(600), new KeyValue(x, 10, WEB_EASE)),
                new KeyFrame(Duration.millis(700), new KeyValue(x, -10, WEB_EASE)),
                new KeyFrame(Duration.millis(800), new KeyValue(x, 10, WEB_EASE)),
                new KeyFrame(Duration.millis(900), new KeyValue(x, -10, WEB_EASE)),
                new KeyFrame(Duration.millis(1000), new KeyValue(x, 0, WEB_EASE))
        );
        System.out.println(node.getScene());
        xIni = node.getScene().getWindow().getX();
        x.addListener((ob, n, n1) -> (node.getScene().getWindow()).setX(xIni + n1.doubleValue()));

        setCycleDuration(Duration.seconds(1));
        setDelay(Duration.seconds(0.2));
        setOnFinished(event);
    }

    /**
     * Called when the animation is starting
     */
    protected final void starting() {
        if (useCache) {
            oldCache = node.isCache();
            oldCacheHint = node.getCacheHint();
            node.setCache(true);
            node.setCacheHint(CacheHint.SPEED);
        }
    }

    /**
     * Called when the animation is stopping
     */
    protected final void stopping() {
        if (useCache) {
            node.setCache(oldCache);
            node.setCacheHint(oldCacheHint);
        }
    }

    @Override
    protected void interpolate(double d) {
        timeline.playFrom(Duration.seconds(d));
        timeline.stop();
    }
}
