package org.glebchanskiy.associativeprocessor.registers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class RegisterImpl implements Register {

    private static final Logger log = LoggerFactory.getLogger(RegisterImpl.class);
    private final List<Boolean> bits;
    private final String name;

    public RegisterImpl(int size, String name) {
        this.bits = new ArrayList<>(Collections.nCopies(size, false));
        this.name = name;
    }

    @Override
    public void save(List<Boolean> bits) {
        Collections.copy(this.bits, bits);
        log.info("{} Save ", this);
    }

    @Override
    public List<Boolean> load() {
        log.info("{} Load", this);
        return this.bits.stream().toList();
    }

    @Override
    public String toString() {
        return "Register[" + name + "][" + bits.stream().map(b -> Boolean.TRUE.equals(b) ? "1" : "0").collect(Collectors.joining()) + "]";
    }
}
