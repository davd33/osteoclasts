package org.osteo.gui.listeners;

import imagej.data.overlay.Overlay;
import imagej.data.overlay.PointOverlay;
import imagej.display.event.input.MsEvent;
import org.osteo.gui.ImagePlusPanel;
import org.osteo.main.App;
import org.scijava.AbstractContextual;
import org.scijava.event.EventHandler;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: davidr
 * Date: 7/9/13
 * Time: 5:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class ImagePlusPanelListener extends AbstractContextual {

    public ImagePlusPanelListener(ImagePlusPanel parent) {
        this.parent = parent;
    }

    private ImagePlusPanel parent;

    private Point previousPos;
    private Point currentPos;

    private List<Point> ROIPoints = new ArrayList<Point>();

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
            ROIPoints.add(new Point(getCurrentPos()));

            setPreviousPos(getCurrentPos());
        } else if (getPreviousPos() != null) {
            setCurrentPos(new Point(mouseEvent.getX(), mouseEvent.getY()));

            // create overlay
            final PointOverlay points = new PointOverlay();
            int i = 0;
            for (Iterator<Point> pIt = ROIPoints.iterator(); pIt.hasNext(); ) {
                Point p = pIt.next();
                points.setPoint(i, new double[] { p.getX(), p.getY() });
                i++;
            }

//            parent.getImageDisplay().
            List<Overlay> overlays = new ArrayList<Overlay>();
            overlays.add(points);

            System.out.println(points);
            App.getImageJ().overlay().addOverlays(parent.getImageDisplay(), overlays);

            clearPos();
        }
    }
}
