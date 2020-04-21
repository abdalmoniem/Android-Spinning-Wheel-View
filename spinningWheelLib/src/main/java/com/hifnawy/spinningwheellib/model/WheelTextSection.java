package com.hifnawy.spinningwheellib.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * Created by AbdAlMoniem AlHifnawy
 */

public class WheelTextSection extends WheelSection implements Serializable {
    private String text;
    private int backgroundColor;
    private int foregroundColor;

    public WheelTextSection(String text) {
        this.text = text;
    }


    public WheelTextSection setSectionBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }

    public WheelTextSection setSectionForegroundColor(int foregroundColor) {
        this.foregroundColor = foregroundColor;
        return this;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public int getForegroundColor() {
        return foregroundColor;
    }

    public SectionType getType() {
        return SectionType.TEXT;
    }

    public String getText() {
        return text;
    }


    @Override
    public String toString() {
        return
                "SectionType= " +  getType() +
                        ", Text= " +      text;
    }

    // serialization support

    private static final long serialVersionUID = 1L;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeUTF(text);
        out.writeInt(foregroundColor);
        out.writeInt(backgroundColor);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        text = in.readUTF();
        foregroundColor = in.readInt();
        backgroundColor = in.readInt();
    }

    private void readObjectNoData() throws ObjectStreamException {
        // nothing to do
    }
}
