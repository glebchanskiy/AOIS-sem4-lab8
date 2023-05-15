package org.glebchanskiy.associativeprocessor.registers;

import java.util.List;

public interface Register {
    void save(List<Boolean> bits);

    List<Boolean> load();
}
