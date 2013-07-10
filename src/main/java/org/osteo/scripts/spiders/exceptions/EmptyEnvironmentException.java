/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.osteo.scripts.spiders.exceptions;

/**
 *
 * @author davd33
 */
public class EmptyEnvironmentException extends Exception {
    
    private String message;
    
    @Override
    public String getMessage() {
        return message;
    }
    
    public EmptyEnvironmentException(String message) {
        this.message = message;
    }
}
