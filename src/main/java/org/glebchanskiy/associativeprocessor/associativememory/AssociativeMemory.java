package org.glebchanskiy.associativeprocessor.associativememory;

public interface AssociativeMemory {
    Boolean get(int i, int j);

    void set(int i, int j, Boolean bit);

    int size();
}
