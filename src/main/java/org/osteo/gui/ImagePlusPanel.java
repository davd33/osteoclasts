/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.osteo.gui;

import ij.ImagePlus;
import imagej.core.tools.PaintBrushTool;
import imagej.data.Dataset;
import imagej.data.display.DefaultImageDisplay;
import imagej.data.overlay.Overlay;
import imagej.ui.swing.sdi.viewer.SwingDisplayWindow;
import imagej.ui.swing.sdi.viewer.SwingSdiImageDisplayViewer;
import imagej.ui.swing.viewer.image.SwingDisplayPanel;
import imagej.ui.swing.viewer.image.SwingImageDisplayViewer;
import org.osteo.gui.listeners.ImagePlusPanelListener;
import org.osteo.main.App;
import org.scijava.thread.ThreadService;

import javax.swing.*;
import java.io.File;

/**
 * @author davidr
 */
public class ImagePlusPanel extends JPanel {

    private ImagePlus ip;
    private File file;
    protected static ImagePlusPanelListener imgListener;
    protected DefaultImageDisplay imageDisplay;
    protected SwingDisplayPanel displayPanel;

    public SwingDisplayPanel getDisplayPanel() {
        return this.displayPanel;
    }

    public DefaultImageDisplay getImageDisplay() {
        if (imageDisplay == null) {
            imageDisplay = new DefaultImageDisplay();
            imageDisplay.setContext(App.getIoService().getContext());
        }
        return this.imageDisplay;
    }

    public ImagePlus getIp() {
        return ip;
    }

    public void setIp(ImagePlus ip) {
        this.ip = ip;
    }

    public ImagePlusPanel() {
    }

    public void update(File ip) {
        this.ip = new ImagePlus(ip.getAbsolutePath());
        this.file = ip; //

        removeAll();

//            this.add(new JLabel(new ImageIcon(this.ip.getImage())));

        try {
            final String path = this.file.getAbsolutePath();
            final Dataset data = App.getIoService().loadDataset(path);

            getImageDisplay().display(data);

            final ThreadService threadService = App.getImageJ().get(ThreadService.class);
            threadService.queue(new Runnable() {

                @Override
                public void run() {
                    final SwingImageDisplayViewer displayViewer = new SwingSdiImageDisplayViewer();
                    displayViewer.setContext(App.getImageJ().getContext());
                    App.getImageJ().ui().addDisplayViewer(displayViewer);
                    final SwingDisplayWindow displayWindow = new SwingDisplayWindow();
                    displayViewer.view(displayWindow, getImageDisplay());
                    displayPanel = displayViewer.getPanel();
//                        displayPanel.repaint();
//                        displayPanel.setSize(100, 100);
                    displayPanel.redoLayout();
                    displayPanel.redraw();

                    // add mouse events
//                    imgListener = new ImagePlusPanelListener(ImagePlusPanel.this);
//                    imgListener.setContext(App.getIJContext());

                    ImagePlusPanel.this.add(displayPanel);
                    ImagePlusPanel.this.updateUI();

                    App.getImageJ().ui().getToolService().getTool(PaintBrushTool.class);
                }
            });
        } catch (Exception e) {
            App.log(e.getMessage());
        }

        updateUI();
    }
}
