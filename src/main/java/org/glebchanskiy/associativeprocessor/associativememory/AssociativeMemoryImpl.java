package org.glebchanskiy.associativeprocessor.associativememory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

import static org.glebchanskiy.associativeprocessor.util.APUtils.toDig;

public class AssociativeMemoryImpl implements AssociativeMemory {

    private static final Logger log = LoggerFactory.getLogger(AssociativeMemoryImpl.class);
    private static final int DEFAULT_POOL_SIZE = 1 << 4;
    private final Boolean[][] associativeArray;

    public AssociativeMemoryImpl() {
        log.info("initialize Memory {}x{}", DEFAULT_POOL_SIZE, DEFAULT_POOL_SIZE);
        this.associativeArray = new Boolean[DEFAULT_POOL_SIZE][DEFAULT_POOL_SIZE];
        for (int i = 0; i < DEFAULT_POOL_SIZE; i++) {
            Random random = new Random();
            Boolean[] inner = new Boolean[DEFAULT_POOL_SIZE];
            for (int j = 0; j < DEFAULT_POOL_SIZE; j++) {
                inner[j] = random.nextBoolean();
            }
            this.associativeArray[i] = inner;
        }
    }

    public AssociativeMemoryImpl(Boolean[][] associativeArray) {
        log.info("initialize based on ass array");
        this.associativeArray = associativeArray;
    }

    @Override
    public Boolean get(int i, int j) {
        log.debug("Get {}:{} -> {}", i, j, toDig(this.associativeArray[i][j]));
        return this.associativeArray[i][j];
    }

    @Override
    public void set(int i, int j, Boolean bit) {
        log.debug("Set {}:{} <- {}", i, j, toDig(bit));
        this.associativeArray[i][j] = bit;
    }

    @Override
    public int size() {
        return DEFAULT_POOL_SIZE;
    }


    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        int i = 0;
        for (Boolean[] bits : associativeArray) {
            output.append(String.format("[%#x] ", i)).append(toDig(bits)).append('\n');
            i++;
        }
        return "Memory{\n" + output + '}';
    }
}
