package org.osteo.gui.listeners;

import imagej.display.event.input.MsEvent;
import org.scijava.AbstractContextual;
import org.scijava.event.EventHandler;

import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: davidr
 * Date: 7/9/13
 * Time: 5:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class ImagePlusPanelListener extends AbstractContextual {

    private Point previousPos;
    private Point currentPos;

    private Point originROI;
    private Point endROI;

    private void clearPos() {
        this.previousPos = null;
        this.currentPos = null;
    }

    private Point getCurrentPos() {
        return currentPos;
    }

    private void setCurrentPos(Point p) {
        currentPos = p;
    }

    private Point getPreviousPos() {
        return previousPos;
    }

    private void setPreviousPos(Point p) {
        previousPos = p;
    }

    @EventHandler
    public void onEvent(final MsEvent mouseEvent) {
        if (mouseEvent.getModifiers().isLeftButtonDown()) {
            setCurrentPos(new Point(mouseEvent.getX(), mouseEvent.getY()));

            // start making ROI
            if (getPreviousPos() == null) {
                this.originROI = getCurrentPos();
            }

            setPreviousPos(getCurrentPos());
        } else if (getPreviousPos() != null) {
            setCurrentPos(new Point(mouseEvent.getX(), mouseEvent.getY()));

            //

            clearPos();
        }
    }
}
