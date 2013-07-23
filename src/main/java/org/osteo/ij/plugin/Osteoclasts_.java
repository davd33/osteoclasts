/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.osteo.ij.plugin;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.plugin.ChannelSplitter;
import ij.plugin.Converter;
import ij.plugin.ImageCalculator;
import ij.plugin.Thresholder;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author davidr
 */
public class Osteoclasts_ implements PlugInFilter {

    private ImagePlus imp;
    private JFrame miniWin;
    public static final Color OVERLAY_COLOR = Color.CYAN;

    @Override
    public int setup(String arg, ImagePlus imp) {
        this.imp = imp;
        return DOES_ALL;
    }

    @Override
    public void run(ImageProcessor ip) {
        runMiniWin();

        if (imp.getOverlay() != null) {
            imp.getOverlay().setFillColor(Osteoclasts_.OVERLAY_COLOR);
            imp.getOverlay().drawBackgrounds(true);
            imp.getOverlay().drawLabels(true);
        } else {
            imp.setOverlay(new Overlay());
        }

        final ImagePlus impML = this.imp;
        this.imp.getWindow().addWindowListener(new WindowListener() {
            public void windowOpened(WindowEvent we) {
            }

            public void windowClosing(WindowEvent we) {
            }

            public void windowClosed(WindowEvent we) {
                closeMiniWin();
            }

            public void windowIconified(WindowEvent we) {
            }

            public void windowDeiconified(WindowEvent we) {
            }

            public void windowActivated(WindowEvent we) {
            }

            public void windowDeactivated(WindowEvent we) {
            }
        });
        this.imp.getCanvas().addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent me) {
                if (me.isControlDown() && impML.getOverlay() != null && impML.getRoi() != null) {
                    impML.getOverlay().add(impML.getRoi());
                    impML.getOverlay().setFillColor(Color.GREEN);
                    impML.getOverlay().drawBackgrounds(true);
                    impML.getOverlay().drawLabels(true);
                }
            }

            @Override
            public void mousePressed(MouseEvent me) {
            }

            @Override
            public void mouseReleased(MouseEvent me) {
            }

            @Override
            public void mouseEntered(MouseEvent me) {
            }

            @Override
            public void mouseExited(MouseEvent me) {
            }
        });
    }

    private enum Actions {

        PA("Analyze Particles", "compute the final results and save them in a csv file"),
        OPEN("Open Images", "give the plugin the images to analyze"),
        CLASS("Run Classifier", "analyze the original images"),
        OVERLAYS("Get Overlays", "analyze classified images"),
        OPTIONS("Options", "change options");
        private String name;
        private String desc;

        public String getDesc() {
            return this.desc;
        }

        public String getName() {
            return this.name;
        }

        Actions(String name, String desc) {
            this.name = name;
            this.desc = desc;
        }
    }

    private void closeMiniWin() {
        if (miniWin != null) {
            miniWin.setVisible(false);
        }
        miniWin = null;
    }

    private void runMiniWin() {
        miniWin = new JFrame();
        miniWin.setLayout(new BorderLayout());
        miniWin.setVisible(true);

        JPanel actionsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;

        ActionListener actionListener = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                JButton source = (JButton) ae.getSource();

                if (source.getText().equals(Actions.PA.getName())) {
                    pa();
                } else if (source.getText().equals(Actions.CLASS.getName())) {
                    classify();
                } else if (source.getText().equals(Actions.OPEN.getName())) {
                    open();
                } else if (source.getText().equals(Actions.OPTIONS.getName())) {
                    options();
                } else if (source.getText().equals(Actions.OVERLAYS.getName())) {
                    overlays();
                }
            }
        };

        for (Actions action : Actions.values()) {
            JButton actionButton = new JButton(action.getName());
            actionButton.setToolTipText(action.getDesc());
            actionsPanel.add(actionButton, c);
            actionButton.addActionListener(actionListener);
        }

        miniWin.getContentPane().add(actionsPanel, BorderLayout.CENTER);
        miniWin.pack();
    }

    private void classify() {
        System.out.println("classifier");
    }

    private void options() {
        System.out.println("options");
    }

    private void overlays() {
        System.out.println("overlays");
    }

    private void open() {
        System.out.println("open images");
    }

    private void pa() {
        Overlay o = imp.getOverlay();
        o.drawLabels(false);
        o.drawNames(false);
        o.drawBackgrounds(true);
        
        IJ.log(o.size() + " detected osteoclasts");

        ImagePlus impLocal = imp.flatten();
        overlayToMask(impLocal, Osteoclasts_.OVERLAY_COLOR);
    }

    private ImagePlus overlayToMask(ImagePlus impLocal, Color color) {
        // Color Thresholder 1.47u
        // Autogenerated macro, single images only!
        int[] min = new int[3];
        int[] max = new int[3];
        String[] filter = new String[3];

        String a = impLocal.getTitle();
        
        ImagePlus[] channels = ChannelSplitter.split(impLocal);

        min[0] = color.getRed();
        max[0] = 255;
        filter[0] = "pass";
        min[1] = color.getGreen();
        max[1] = 255;
        filter[1] = "pass";
        min[2] = color.getBlue();
        max[2] = 255;
        filter[2] = "pass";

        for (int i = 0; i < 3; i++) {
            ImagePlus channel = channels[0];
            ImageProcessor channelProcessor = channel.getProcessor();
            channelProcessor.setThreshold(min[i], max[i], ImageProcessor.BLACK_AND_WHITE_LUT);
            channelProcessor.threshold(min[i]);
            
            if (filter[i].equals("stop")) {
                IJ.run("Invert");
            }
        }

        ImageCalculator ic = new ImageCalculator();
        ImagePlus binary = ic.run("AND create", channels[0], channels[1]);
        binary = ic.run("AND create", binary, channels[2]);
        
        binary.show();
        return binary;
}
private void log(String message) {
        System.out.println(message);
    }
}
