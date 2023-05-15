package org.glebchanskiy;

import org.glebchanskiy.associativeprocessor.AssociativeProcessor;
import org.glebchanskiy.associativeprocessor.AssociativeProcessorImpl;
import org.glebchanskiy.associativeprocessor.associativememory.AssociativeMemory;
import org.glebchanskiy.associativeprocessor.associativememory.AssociativeMemoryImpl;
import org.glebchanskiy.associativeprocessor.enums.Load;
import org.glebchanskiy.associativeprocessor.enums.LogicOperationType;
import org.glebchanskiy.associativeprocessor.enums.Store;

public class Application {

    public static void main(String... args) {
        printTask0();
        printTask1();
        printTask2();
        printTask3();
    }

    private static void printTask0() {
        AssociativeMemory memory = new AssociativeMemoryImpl();
        AssociativeProcessor processor = new AssociativeProcessorImpl(memory);

        System.out.println("Associative Memory Array: " + memory);

        System.out.println("\nИз-за сложности восприятия диагональной адресации, далее будет применяться формат Нормального отображения:");
        System.out.println("(Логи чтения из памяти появляются из-за использования метода read в memoryAsNormal()\n");
        System.out.println(processor.memoryAsNormal());
    }

    private static void printTask1() {
        AssociativeMemory memory = new AssociativeMemoryImpl();
        AssociativeProcessor processor = new AssociativeProcessorImpl(memory);

        System.out.println("____".repeat(25));
        System.out.println("TASK1 f2 f7 f8 f13 [Логические функции над разрядными столбцами]");

        System.out.println("\n\ninitial state: ");
        System.out.println(processor.memoryAsNormal());
        System.out.println();

        System.out.println("f2 | x1*!x2 | Запрет 1-го аргумента (НЕТ):");
        System.out.println("0x2 = f2(0x0, 0x1)");
        processor.loadBitSlice(0x0, Load.TO_BUFFER_REGISTER);
        processor.execLogicalOperation(0x1, LogicOperationType.FUNC_2);
        processor.storeBitSlice(0x2, Store.FROM_BUFFER_REGISTER);

        System.out.println();

        System.out.println("f7 | x1+x2 | Дизьюнкция (ИЛИ):");
        System.out.println("0x5 = f7(0x3, 0x4)");
        processor.loadBitSlice(0x3, Load.TO_BUFFER_REGISTER);
        processor.execLogicalOperation(0x4, LogicOperationType.FUNC_7);
        processor.storeBitSlice(0x5, Store.FROM_BUFFER_REGISTER);

        System.out.println();

        System.out.println("f8 | !(x1+x2) | Операция Пирса (ИЛИ-НЕ):");
        System.out.println("0x8 = f8(0x6, 0x7)");
        processor.loadBitSlice(0x6, Load.TO_BUFFER_REGISTER);
        processor.execLogicalOperation(0x7, LogicOperationType.FUNC_8);
        processor.storeBitSlice(0x8, Store.FROM_BUFFER_REGISTER);

        System.out.println();

        System.out.println("f13 | !x1+x2 | Импликация от 1-го аргумента:");
        System.out.println("0xB = f13(0x9, 0xA)");
        processor.loadBitSlice(0x9, Load.TO_BUFFER_REGISTER);
        processor.execLogicalOperation(0xA, LogicOperationType.FUNC_13);
        processor.storeBitSlice(0xB, Store.FROM_BUFFER_REGISTER);

        System.out.println("\n\nafter manipulations: ");
        System.out.println(processor.memoryAsNormal());
    }

    private static void printTask2() {
        AssociativeMemory memory = new AssociativeMemoryImpl();
        AssociativeProcessor processor = new AssociativeProcessorImpl(memory);

        System.out.println("____".repeat(25));
        System.out.println("TASK2 [Поиск величин, заключенных в интервале]");

        System.out.println("\n\ninitial state: ");
        System.out.println(processor.memoryAsNormal());
        System.out.println();

        // Получить все значения в интервале между значениями по адресам 0x0 and 0x1.
        // Если значения lowerAddress > addressUpper, то поиск ведётся наоборот
        // от addressUpper до lowerAddress (это сделано тк память задаётся случайными значениями)
        processor.getSlicesInInterval(0x0, 0x1);
        // результат поиска находится в буферном регистре
        // 8:44:05.186 INFO  RegisterImpl                    Register[buffer][0011110000000011] Save   <----

        System.out.println("\n\nafter manipulations: ");
        System.out.println(processor.memoryAsNormal());
    }

    private static void printTask3() {
        AssociativeMemory memory = new AssociativeMemoryImpl();
        AssociativeProcessor processor = new AssociativeProcessorImpl(memory);

        System.out.println("____".repeat(25));
        System.out.println("TASK3 [Сложение полей Aj и Bj  в словах Sj, у которых Vj совпадает с заданным V=000-111]");

        System.out.println("\n\ninitial state: ");
        System.out.println(processor.memoryAsNormal());
        System.out.println();
        
        processor.maskedSum();

        System.out.println("\n\nafter manipulations: ");
        System.out.println(processor.memoryAsNormal());
    }
}
