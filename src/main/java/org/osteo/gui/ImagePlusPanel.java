/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.osteo.gui;

import ij.ImagePlus;
import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.data.display.DefaultImageDisplay;
import imagej.io.IOService;
import imagej.ui.swing.sdi.viewer.SwingDisplayWindow;
import imagej.ui.swing.sdi.viewer.SwingSdiImageDisplayViewer;
import imagej.ui.swing.viewer.image.SwingDisplayPanel;
import imagej.ui.swing.viewer.image.SwingImageDisplayViewer;
import org.osteo.gui.listeners.ImagePlusPanelListener;
import org.osteo.main.App;
import org.scijava.Context;
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

    public ImagePlus getIp() {
        return ip;
    }

    public void setIp(ImagePlus ip) {
        this.ip = ip;
    }

    public ImagePlusPanel() {
    }

    public ImagePlusPanel(File ip) {
        this.ip = new ImagePlus(ip.getAbsolutePath());
        this.file = ip;
        this.setAutoscrolls(true);
    }

    public void update(File ip) {
        this.ip = new ImagePlus(ip.getAbsolutePath());
        this.file = ip; //

        removeAll();
        if (ip != null) {
//            this.add(new JLabel(new ImageIcon(this.ip.getImage())));

            try {
                final String path = this.file.getAbsolutePath();
                final Dataset data = App.getIoService().loadDataset(path);
                final DefaultImageDisplay display = new DefaultImageDisplay();
                display.setContext(App.getIoService().getContext());
                display.display(data);

                final ThreadService threadService = App.getImageJ().get(ThreadService.class);
                threadService.queue(new Runnable() {

                    @Override
                    public void run() {
                        final SwingImageDisplayViewer displayViewer = new SwingSdiImageDisplayViewer();
                        displayViewer.setContext(App.getImageJ().getContext());
                        App.getImageJ().ui().addDisplayViewer(displayViewer);
                        final SwingDisplayWindow displayWindow = new SwingDisplayWindow();
                        displayViewer.view(displayWindow, display);
                        final SwingDisplayPanel displayPanel = displayViewer.getPanel();
//                        displayPanel.repaint();
//                        displayPanel.setSize(100, 100);
                        displayPanel.redoLayout();
                        displayPanel.redraw();

                        // add mouse events
                        imgListener = new ImagePlusPanelListener();
                        imgListener.setContext(App.getIJContext());

                        ImagePlusPanel.this.add(displayPanel);
                        ImagePlusPanel.this.updateUI();
                    }
                });
            } catch (Exception e) {
                App.log("impossible to display the selected image.");
            }
        }
        updateUI();
    }
}
