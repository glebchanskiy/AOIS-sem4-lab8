package org.glebchanskiy.associativeprocessor;

import org.glebchanskiy.associativeprocessor.associativememory.AssociativeMemory;
import org.glebchanskiy.associativeprocessor.enums.Load;
import org.glebchanskiy.associativeprocessor.enums.LogicOperationType;
import org.glebchanskiy.associativeprocessor.enums.Store;
import org.glebchanskiy.associativeprocessor.registers.Register;
import org.glebchanskiy.associativeprocessor.registers.RegisterImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.glebchanskiy.associativeprocessor.util.APUtils.*;


public class AssociativeProcessorImpl implements AssociativeProcessor {
    private static final Logger log = LoggerFactory.getLogger(AssociativeProcessorImpl.class);
    private final AssociativeMemory memory;
    private final Register bufferRegister;
    private final Register maskRegister;

    public AssociativeProcessorImpl(AssociativeMemory memory) {
        this.memory = memory;
        this.bufferRegister = new RegisterImpl(16, "buffer");
        this.maskRegister = new RegisterImpl(16, "mask");
        this.maskRegister.save(Collections.nCopies(16, true));
    }

    /**
     * Считывает значение по адресу и записывает:
     * 1. в буферный регистр
     * 2. по маске в буферный регистр
     * 3. в регистр маски по маске
     */
    @Override
    public void loadBitSlice(int address, Load type) {
        log.info("LoadBitSlice {} {}", toHex(address), type.name());
        switch (type) {
            case TO_BUFFER_REGISTER -> bufferRegister.save(read(address));
            case MASKED_TO_BUFFER_REGISTER -> bufferRegister.save(maskFilter(read(address), bufferRegister.load()));
            case MASKED_TO_MASK_REGISTER -> maskRegister.save(maskFilter(read(address), maskRegister.load()));
        }
    }

    /**
     * Выгружает значение по адресу. Значение:
     * 1. буферного регистра
     * 2. регистра маски
     * 3. по маске из буферного регистра
     */
    @Override
    public void storeBitSlice(int address, Store type) {
        log.info("StoreBitSlice {} {}", toHex(address), type.name());
        switch (type) {
            case FROM_BUFFER_REGISTER -> write(address, bufferRegister.load());
            case FROM_MASK_REGISTER -> write(address, maskRegister.load());
            case MASKED_FROM_BUFFER_REGISTER -> write(address, maskFilter(bufferRegister.load(), read(address)));
        }
    }

    /**
     * Перемещает значение в другой адрес. (через буферный регистр)
     */
    @Override
    public void moveBitSlice(int fromAddress, int toAddress) {
        log.info("MoveBitSlice from: {} to: {}", toHex(fromAddress), toHex(toAddress));
        loadBitSlice(fromAddress, Load.TO_BUFFER_REGISTER);
        storeBitSlice(toAddress, Store.FROM_BUFFER_REGISTER);
    }

    /**
     * При выполнении логических операций два разрядных столбца (S1 и S2)
     * подвергаются логической операции и результат этой операции
     * помещается в позицию (адрес) некоторого третьего столбца.
     * Первым столбцом выступает содержимое буферного регистра
     * Второй столбец - берётся по адресу (address)
     * Результат записывается в буферный регистр. (перезатирается Первый столбец)
     */
    @Override
    public void execLogicalOperation(int address, LogicOperationType operation) {
        List<Boolean> x1 = bufferRegister.load();
        List<Boolean> x2 = read(address);
        switch (operation) {
            case FUNC_2 -> bufferRegister.save(conjunction(x1, not(x2)));
            case FUNC_7 -> bufferRegister.save(disjunction(x1, x2));
            case FUNC_8 -> bufferRegister.save(not(disjunction(x1, x2)));
            case FUNC_13 -> bufferRegister.save(disjunction(not(x1), x2));
        }
    }

    /**
     * Сложение полей Aj и Bj в словах Sj, у которых Vj совпадает с заданным V=000-111 (по условию).
     *
     * Метод суммирует Aj и Bj для всех значений в памяти,
     * первых 3 бит которых совпадают с 3 первыми битами регистра маски.
     */
    @Override
    public void maskedSum() {
        log.info("MaskedSum {}", maskRegister);
        List<Boolean> mask = maskRegister.load();
        for (int i = 0; i < memory.size(); i++) {
            List<Boolean> current = read(i);
            if (isVjEquals(current, mask)) {
                sum(current);
                storeBitSlice(i, Store.FROM_BUFFER_REGISTER);
            }
        }
    }

    private void sum(List<Boolean> bits) {
        List<Boolean> result = new ArrayList<>(bits);
        List<Boolean> aj = new ArrayList<>();
        List<Boolean> bj = new ArrayList<>();

        for (int i = 3; i < 7; i++) {
            aj.add(bits.get(i));
            bj.add(bits.get(i + 4));
        }

        List<Boolean> sj = add(aj, bj);

        int index = 0;
        for (int i = 11; i < 16; i++) {
            result.set(i, sj.get(index));
            index++;
        }

        log.info("Sum {} + {} = {}", toDig(aj), toDig(bj), toDig(sj));

        bufferRegister.save(result);
    }


    private boolean isVjEquals(List<Boolean> operand, List<Boolean> mask) {
        return Objects.equals(operand.get(0), mask.get(0)) && Objects.equals(operand.get(1), mask.get(1)) && Objects.equals(operand.get(2), mask.get(2));
    }

    /**
     * Находит все значения в интервале между двумя переданными (по адресам)
     * Ответ будет храниться в буферном регистре, где начиная со старших битов
     * (в порядке адресации) будут помечены подходящие значения - 1, остальные 0.
     * Пример:
     * NormalMemory{
     * [0x0] 0001
     * [0x1] 0011
     * [0x2] 0111
     * [0x3] 1111
     * }
     * getSlicesInInterval(0x0, 0x2) запишет в буферный регистр следующее:
     * BufferRegister -> 0100
     * <p>
     * так как:
     * [0x0] 0
     * [0x1] 1
     * [0x2] 0
     * [0x3] 0
     */
    @Override
    public void getSlicesInInterval(int addressOfLower, int addressOfUpper) {
        List<Boolean> lowerLimit = read(addressOfLower);
        List<Boolean> upperLimit = read(addressOfUpper);
        if (compare(lowerLimit, upperLimit) == -1) {
            List<Boolean> temp = lowerLimit;
            lowerLimit = upperLimit;
            upperLimit = temp;
        }
        log.info("GetSlicesInInterval {} < x < {}", toDig(lowerLimit), toDig(upperLimit));
        List<Boolean> buffer = new ArrayList<>();

        for (int i = 0; i < memory.size(); i++) {
            List<Boolean> current = read(i);
            log.info("compare {} < {} < {} is {}", toDig(lowerLimit), toDig(current), toDig(upperLimit), compare(current, lowerLimit) == -1 && compare(current, upperLimit) == 1);
            buffer.add(i, compare(current, lowerLimit) == -1 && compare(current, upperLimit) == 1);
        }
        bufferRegister.save(buffer);
    }

    /**
     * Трансформирует матрицу памяти в понятную для чтения форму
     */
    @Override
    public String memoryAsNormal() {
        StringBuilder output = new StringBuilder();
        output.append("NormalMemory{\n");
        for (int i = 0; i < memory.size(); i++) {
            List<Boolean> bin = read(i);
            output.append(toHex(i)).append(toDig(bin)).append(" [").append(toDecimal(bin)).append("]").append('\n');
        }
        output.append("}\n");
        return output.toString();
    }

    /**
     * Записывает значения через маску (Регистр маски) с учётом предыдущего состояния.
     * mask:           001100
     * bitsToBeMasked: 011000
     * previousState:  100101
     * result ->       101101
     */
    private List<Boolean> maskFilter(List<Boolean> bitsToBeMasked, List<Boolean> previousState) {
        List<Boolean> mask = this.maskRegister.load();
        List<Boolean> maskedBits = new ArrayList<>(previousState);
        for (int i = 0; i < bitsToBeMasked.size(); i++) {
            if (Boolean.TRUE.equals(mask.get(i))) maskedBits.set(i, bitsToBeMasked.get(i));
        }
        return maskedBits;
    }

    /**
     * Сделать срез по адресу (Прочитать).
     */
    private List<Boolean> read(int address) {

        List<Boolean> slice = new ArrayList<>();
        for (int i = address; i < memory.size(); i++) {
            slice.add(memory.get(i, address));
        }
        for (int i = 0; i < address; i++) {
            slice.add(memory.get(i, address));
        }
        log.info("READ {} {}", toHex(address), toDig(slice));
        return slice;
    }

    /**
     * Записать срез по адресу.
     */
    private void write(int address, List<Boolean> bits) {
        for (int i = address; i < memory.size(); i++) {
            memory.set(i, address, bits.get(i - address));
        }
        for (int i = 0; i < address; i++) {
            memory.set(i, address, bits.get(memory.size() + i - address));
        }
        log.info("WRITE {} {}", toHex(address), toDig(bits));
    }

    public static List<Boolean> disjunction(List<Boolean> binary1, List<Boolean> binary2) {
        List<Boolean> disjunction = new ArrayList<>();
        for (int i = 0; i < binary1.size(); i++) {
            disjunction.add(binary1.get(i) || binary2.get(i));
        }
        return disjunction;
    }

    public static List<Boolean> conjunction(List<Boolean> binary1, List<Boolean> binary2) {
        List<Boolean> conjunction = new ArrayList<>();
        for (int i = 0; i < binary1.size(); i++) {
            conjunction.add(binary1.get(i) && binary2.get(i));
        }
        return conjunction;
    }

    public static List<Boolean> not(List<Boolean> binary) {
        List<Boolean> not = new ArrayList<>();
        for (Boolean bit : binary) {
            not.add(!bit);
        }
        return not;
    }

    private static List<Boolean> add(List<Boolean> summand1, List<Boolean> summand2) {
        List<Boolean> result = new ArrayList<>();
        boolean trans = false;

        for (int i = summand1.size() - 1; i >= 0; i--) {
            if (!summand1.get(i) && !summand2.get(i) && !trans) {
                result.add(false);
                trans = false;
            } else if (!summand1.get(i) && !summand2.get(i) && trans) {
                result.add(true);
                trans = false;
            } else if (summand1.get(i) && !summand2.get(i) && !trans) {
                result.add(true);
                trans = false;
            } else if (summand1.get(i) && !summand2.get(i) && trans) {
                result.add(false);
                trans = true;
            } else if (!summand1.get(i) && summand2.get(i) && !trans) {
                result.add(true);
                trans = false;
            } else if (!summand1.get(i) && summand2.get(i) && trans) {
                result.add(false);
                trans = true;
            } else if (summand1.get(i) && summand2.get(i) && !trans) {
                result.add(false);
                trans = true;
            } else if (summand1.get(i) && summand2.get(i) && trans) {
                result.add(true);
                trans = true;
            }
        }
        if (trans) result.add(true);
        else result.add(false);

        Collections.reverse(result);
        return result;
    }
}
