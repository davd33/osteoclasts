/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.osteo.scripts.spiders;

import ij.ImagePlus;
import org.osteo.main.Bundle;
import org.osteo.scripts.spiders.exceptions.EmptyEnvironmentException;

/**
 *
 * @author davidr
 */
public class Environment {
    
    private ImagePlus imp;
    private Stakes stakes;
    
    public Environment(ImagePlus imp) {
        this.imp = imp;
    }
    
    public Stakes findNeighbors(Stake stake, Integer radius) throws EmptyEnvironmentException {
        if (imp == null) throw new EmptyEnvironmentException(Bundle.UI.getString("spiders_findNeighbors_nullEnv"));
        ImagePlus impLocal = imp;
        
        Integer w = impLocal.getWidth(),
                h = impLocal.getHeight(),
                len = w * h;
        for (Integer i = 0; i < len; i++) {
            Integer x = i % w;
            Integer y = new Double(Math.floor(i / w)).intValue();
        }
        return null;
    }
}
