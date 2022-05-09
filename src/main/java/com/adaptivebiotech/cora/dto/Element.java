package com.adaptivebiotech.cora.dto;

import static com.adaptivebiotech.test.utils.TestHelper.toStringOverride;

/**
 * Save all data related to HTML tag (element)
 * 
 * @author jpatel
 * 
 */
public final class Element {

    public String text;
    public String href;
    public String target;
    public String color;
    public String src;

    @Override
    public String toString () {
        return toStringOverride (this);
    }
}
