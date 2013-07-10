/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.osteo.scripts.util;

import java.awt.Point;

/**
 *
 * @author davidr
 */
public class IntegerPixel implements Pixel {
    
    private Integer value;
    private Point position;

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public Point getPosition() {
        return position;
    }
}
