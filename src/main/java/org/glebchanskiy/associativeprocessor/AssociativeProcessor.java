package org.glebchanskiy.associativeprocessor;

import org.glebchanskiy.associativeprocessor.enums.Load;
import org.glebchanskiy.associativeprocessor.enums.LogicOperationType;
import org.glebchanskiy.associativeprocessor.enums.Store;

public interface AssociativeProcessor {
    void loadBitSlice(int address, Load type);

    void storeBitSlice(int address, Store type);

    void moveBitSlice(int fromAddress, int toAddress);

    void execLogicalOperation(int address, LogicOperationType operation);

    void maskedSum();

    void getSlicesInInterval(int addressOfLower, int addressOfUpper);

    String memoryAsNormal();
}
