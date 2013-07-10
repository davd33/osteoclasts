package org.osteo.scripts.util;

import javax.swing.filechooser.FileFilter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Option implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Type {

        FILE, STRING, CHECK, CRITICAL_INFO
    }
    private List<String> possibleValues;
    private String name;
    private Type optionType;
    private String currentValue;
    private FileFilter fileFilter;
    private boolean selected = false;
    private boolean nameDisplayed = true;

    public boolean isNameDisplayed() {
        return nameDisplayed;
    }

    public void setNameDisplayed(boolean nameDisplayed) {
        this.nameDisplayed = nameDisplayed;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public FileFilter getFileFilter() {
        return fileFilter;
    }

    public void setFileFilter(FileFilter fileFilter) {
        this.fileFilter = fileFilter;
    }

    public String getCurrentValue() {
        if (currentValue == null) {
            currentValue = "";
        }
        return currentValue;
    }

    public void setCurrentValue(String currentValue) {
        this.currentValue = currentValue;
    }

    public List<String> getPossibleValues() {
        return possibleValues;
    }

    public void setPossibleValues(List<String> possibleValues) {
        this.possibleValues = possibleValues;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Type getOptionType() {
        return optionType;
    }

    public void setOptionType(Type optionType) {
        this.optionType = optionType;
    }

    public Option(String name, Type optionType) {
        this.name = name;
        this.optionType = optionType;
    }

    public Option(String name, Type optionType, String value) {
        this.name = name;
        this.optionType = optionType;
        this.currentValue = value;
    }

    public Option(String name, Type optionType, String... possibleValues) {
        this.name = name;
        this.optionType = optionType;
        this.possibleValues = new ArrayList<String>();
        Collections.addAll(this.possibleValues, possibleValues);
    }
    
    @Override
    public String toString() {
        String str = "";
        
        str += "\"";
        str += getName();
        str += "\"";
        str += ": ";
        str += "\"";
        str += getCurrentValue();
        str += "\"";
        
        return str;
    }
}
