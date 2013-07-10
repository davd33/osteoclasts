/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.osteo.scripts.spiders;

/**
 *
 * @author davidr
 */
public class Spider {
    
    private Integer refValue;
    private Integer selectivity;
    
    private Double backProbability;
    private Integer perceptionRadius;
    
    private Object pDraglines;   //TODO: define type
    private Object attractSelf;  //TODO: define type
    private Object attractOther; //TODO: define type
    
    private Stake currentPosition;
    private Stake lastFixed;
    
    public Stakes neighbors() {
        return null; //TODO: implement it!
    }
    
    public Stakes draglines() {
        return null; //TODO: implement it!
    }
    
    public Stakes access() {
        return null; //TODO: implement it!
    }
    
    public Integer number(Stake s1, Stake s2) {
        return null; //TODO: implement it!
    }
    
    public Integer number(Stake s1, Stake s2, Spider spider) {
        return null; //TODO: implement it!
    }
}
