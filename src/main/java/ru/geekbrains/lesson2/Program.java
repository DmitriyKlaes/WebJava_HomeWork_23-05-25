package ru.geekbrains.lesson2;

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class Program {


    private static final int WIN_COUNT = 4;
    private static final char DOT_HUMAN = 'X';
    private static final char DOT_AI = 'O';
    private static final char DOT_EMPTY = '•';

    private static final Scanner SCANNER = new Scanner(System.in);

    private static char[][] field; // Двумерный массив хранит текущее состояние игрового поля

    private static final Random random = new Random();

    private static int fieldSizeX; // Размерность игрового поля по горизонтали
    private static int fieldSizeY; // Размерность игрового поля по вертикали

    private static ArrayList<Integer> listMoves = new ArrayList<>(); // вспомогательный список для нахождения хода.


    public static void main(String[] args) {
        while (true) {
            initialize();
            printField();
            while (true) {
                humanTurn();
                printField();
                if (gameCheck(DOT_HUMAN, "Вы победили!"))
                    break;
                aiTurn();
                printField();
                if (gameCheck(DOT_AI, "Компьютер победил!"))
                    break;
            }
            System.out.println("Желаете сыграть еще раз? (Y - да)");
            if (!SCANNER.next().equalsIgnoreCase("Y"))
                break;
        }
    }

    /**
     * Инициализация игрового поля
     */
    private static void initialize() {
        // Установим размерность игрового поля
        fieldSizeX = 5;
        fieldSizeY = 5;

        field = new char[fieldSizeY][fieldSizeX];
        // Пройдем по всем элементам массива
        for (int y = 0; y < fieldSizeY; y++) {
            for (int x = 0; x < fieldSizeX; x++) {
                // Проинициализируем все элементы массива DOT_EMPTY (признак пустого поля)
                turnThisCell(x, y, DOT_EMPTY);
            }
        }
    }

    /**
     * Отрисовка игрового поля
     */
    private static void printField() {
        String delimiterCoordX = "";
        if (fieldSizeX > 9) {
            delimiterCoordX = " ";
        }

        System.out.print("+ ");
        for (int i = 0; i < fieldSizeX * 2 + 1; i++) {
            if (i % 2 == 0) {
                System.out.print("-");
                if (i < 18) {
                    System.out.print(delimiterCoordX);
                }
            } else {
                System.out.print(i / 2 + 1);
            }

        }
        System.out.println();

        for (int i = 0; i < fieldSizeY; i++) {
            if (i < 9) {
                System.out.print(i + 1 + " |");
            } else {
                System.out.print(i + 1 + "|");
            }

            for (int j = 0; j < fieldSizeX; j++)
                System.out.print(delimiterCoordX + field[i][j] + "|");

            System.out.println();
        }

        for (int i = 0; i < fieldSizeX * 2 + 2; i++) {
            System.out.print("-");
        }
        System.out.println();
    }

    /**
     * Обработка хода игрока (человек)
     */
    private static void humanTurn() {
        int x, y;
        do {
            System.out.print("Введите координаты хода X и Y (от 1 до 3) через пробел >>> ");
            x = SCANNER.nextInt() - 1;
            y = SCANNER.nextInt() - 1;
        }
        while (!isCellValid(y, x) || !isCellEmpty(y, x));
        turnThisCell(x, y, DOT_HUMAN);
    }

    /**
     * Проверка, ячейка является пустой
     *
     * @param x
     * @param y
     * @return
     */
    static boolean isCellEmpty(int y, int x) {
        return field[y][x] == DOT_EMPTY;
    }

    /**
     * Проверка корректности ввода
     * (координаты хода не должны превышать размерность массива, игрового поля)
     *
     * @param x
     * @param y
     * @return
     */
    static boolean isCellValid(int y, int x) {
        return x >= 0 && x < fieldSizeX && y >= 0 && y < fieldSizeY;
    }

    /**
     * Ход компьютера
     */
    private static void aiTurn() {

        //сначала бот проверяет все свои возможные выйгрышные линии и ходит туда
        int moveFlag = checkAllPossibleWinAndTurn(DOT_AI, WIN_COUNT);
        if (moveFlag == 0) {
            //если ничего не нашел, то проверяет все тоже самое для игрока и ходит туда
            moveFlag = checkAllPossibleWinAndTurn(DOT_HUMAN, WIN_COUNT);
        }
        if (moveFlag == 0) {
            //потом проверит все мозможные направления,
            //начиная с пустой ячейки с уменьшенным счетчиком победы на 1 для себя
            moveFlag = checkPossibleWinAndTurnStartCell(DOT_AI, 1);
        }
        if (moveFlag == 0) {
            //потом тоже самое для игрока
            moveFlag = checkPossibleWinAndTurnStartCell(DOT_HUMAN, 1);
        }

        //далее если счетчик победы больше 3, то будут произведены дополнительные проверки
        //но уже с уменьшенным счетчиком победы на 2... проверки будет сначала для себя, потом для игрока
        if (WIN_COUNT > 3) {
            if (moveFlag == 0) {
                moveFlag = checkPossibleWinAndTurnStartCell(DOT_AI, 2);
            }
            if (moveFlag == 0) {
                moveFlag = checkPossibleWinAndTurnStartCell(DOT_HUMAN, 2);
            }
        }

        //следующие 2 метода призваны продолжать линию победы для бота
        //и пресекать линию победы для игрока
        if (moveFlag == 0) {
            //этот метод будет продолжать линию победы для бота, если вдруг такая образуется
            //в процессе пресечений линий победы игрока
            moveFlag = checkAllPossiblePreWinAndTurn(DOT_AI, WIN_COUNT);
        }
        if (moveFlag == 0) {
            //этот метод будет пресекать хитросплетения линий победы игрока... метод нужен для
            //обхода хитрости игрока
            moveFlag = checkAllPossiblePreWinAndTurn(DOT_HUMAN, WIN_COUNT);
        }

        //если ход после всех этих проверок так и не сделан, то запускается цикл, в котором
        //уменьшение счетчика победы и так до тех пор, пока аргумент счетчика победы не
        //станет равным 1. Этот метод помогает боту начинать линию победы, если ход игры
        //является хаотичным со стороны игрока.
        if (WIN_COUNT > 3 && moveFlag == 0) {
            for (int i = WIN_COUNT - 1; i >= 1; i--) {
                moveFlag = checkAllPossibleWinAndTurn(DOT_AI, i);
                if (moveFlag == 1) {
                    break;
                }
            }
        }

        //ну и наконец бот совершает рандомный ход, если ни одно условие не совпало
        if (moveFlag == 0) {
            int x, y;
            do {
                x = random.nextInt(fieldSizeX);
                y = random.nextInt(fieldSizeY);
            }
            while (!isCellEmpty(y, x));
            turnThisCell(x, y, DOT_AI);
        }
    }

    /**
     * Метод проверки победы
     *
     * @param playerChip
     * @param winCount
     * @return
     */
    private static boolean checkWin(char playerChip, int winCount) {
        for (int y = 0; y < fieldSizeY; y++) {
            for (int x = 0; x < fieldSizeX; x++) {
                if (checkVertical(x, y, playerChip, winCount)) return true;
                if (checkHorizontal(x, y, playerChip, winCount)) return true;
                if (checkDiagonalLeftTopRightBottom(x, y, playerChip, winCount)) return true;
                if (checkDiagonalLeftBottomRightTop(x, y, playerChip, winCount)) return true;
            }
        }
        return false;
    }

    /**
     * Метод проверки возможной победы и совершения хода в начальную проверяемую ячейку
     *
     * @param playerChip
     * @param winCounterDecrement
     * @return
     */
    private static int checkPossibleWinAndTurnStartCell(char playerChip, int winCounterDecrement) {
        for (int y = 0; y < fieldSizeY; y++) {
            for (int x = 0; x < fieldSizeX; x++) {
                if (isCellEmpty(y, x) && checkPossibleWin(x, y, playerChip, WIN_COUNT, winCounterDecrement)) {
                    turnThisCell(x, y, DOT_AI);
                    return 1;
                }
            }
        }
        return 0;
    }

    /**
     * Метод для своевременного присечения развития победы у игрока.
     * В этом методе реализовано смещение счетчика победы WIN_COUNT
     * и одновременная проверка на возможную победу без смещения счетчика.
     *
     * @param cordX
     * @param cordY
     * @param playerChip
     * @param winCount
     * @param winCounterDecrement
     * @return
     */
    private static boolean checkPossibleWin(int cordX, int cordY, char playerChip, int winCount,
                                            int winCounterDecrement) {
        int nextCell = 1;
        if (checkVertical(cordX, cordY + nextCell, playerChip, winCount - winCounterDecrement) &&
                cordY + nextCell + winCounterDecrement < fieldSizeY &&
                (field[cordY + nextCell + winCounterDecrement][cordX] == playerChip ||
                        field[cordY + nextCell + winCounterDecrement][cordX] == DOT_EMPTY)) return true;
        if (checkHorizontal(cordX + nextCell, cordY, playerChip, winCount - winCounterDecrement) &&
                cordX + nextCell + winCounterDecrement < fieldSizeX &&
                (field[cordY][cordX + nextCell + winCounterDecrement] == playerChip ||
                        field[cordY][cordX + nextCell + winCounterDecrement] == DOT_EMPTY)) return true;
        if (checkDiagonalLeftTopRightBottom(cordX + nextCell, cordY + nextCell, playerChip, winCount - winCounterDecrement) &&
                cordY + nextCell + winCounterDecrement < fieldSizeY && cordX + nextCell + winCounterDecrement < fieldSizeX &&
                (field[cordY + nextCell + winCounterDecrement][cordX + nextCell + winCounterDecrement] == playerChip ||
                        field[cordY + nextCell + winCounterDecrement][cordX + nextCell + winCounterDecrement] == DOT_EMPTY))
            return true;
        return checkDiagonalLeftBottomRightTop(cordX + nextCell, cordY - nextCell, playerChip, winCount - winCounterDecrement) &&
                cordY - nextCell - winCounterDecrement >= 0 && cordX + nextCell + winCounterDecrement < fieldSizeX &&
                (field[cordY - nextCell - winCounterDecrement][cordX + nextCell + winCounterDecrement] == playerChip ||
                        field[cordY - nextCell - winCounterDecrement][cordX + nextCell + winCounterDecrement] == DOT_EMPTY);
    }

    /**
     * Метод проверки по вертикали
     *
     * @param cordX
     * @param cordY
     * @param playerChip
     * @param winCount
     * @return
     */
    private static boolean checkVertical(int cordX, int cordY, char playerChip, int winCount) {
        int winCounter = 0;
        for (int i = cordY; i < winCount + cordY; i++) {
            if (i >= fieldSizeY) {
                return false;
            }
            if (field[i][cordX] == playerChip) {
                winCounter++;
            }
        }
        return winCounter == winCount;
    }

    /**
     * Метод проверки по горизонтали
     *
     * @param cordX
     * @param cordY
     * @param playerChip
     * @param winCount
     * @return
     */
    private static boolean checkHorizontal(int cordX, int cordY, char playerChip, int winCount) {
        int winCounter = 0;
        for (int i = cordX; i < winCount + cordX; i++) {
            if (i >= fieldSizeX) {
                return false;
            }
            if (field[cordY][i] == playerChip) {
                winCounter++;
            }
        }
        return winCounter == winCount;
    }

    /**
     * Метод проверки по диагонали \
     *
     * @param cordX
     * @param cordY
     * @param playerChip
     * @param winCount
     * @return
     */
    private static boolean checkDiagonalLeftTopRightBottom(int cordX, int cordY, char playerChip, int winCount) {
        int winCounter = 0;
        for (int i = 0; i < winCount; i++) {
            if (cordY + i >= fieldSizeY || cordX + i >= fieldSizeX) {
                return false;
            }
            if (field[cordY + i][cordX + i] == playerChip) {
                winCounter++;
            }
        }
        return winCounter == winCount;
    }

    /**
     * Метод проверки по диагонали /
     *
     * @param cordX
     * @param cordY
     * @param playerChip
     * @param winCount
     * @return
     */
    private static boolean checkDiagonalLeftBottomRightTop(int cordX, int cordY, char playerChip, int winCount) {
        int winCounter = 0;
        for (int i = 0; i < winCount; i++) {
            if (cordY - i < 0 || cordX + i >= fieldSizeX) {
                return false;
            }
            if (field[cordY - i][cordX + i] == playerChip) {
                winCounter++;
            }
        }
        return winCounter == winCount;
    }

    /**
     * Метод проверки всех возможных побед на поле без привязки к ячейке.
     * Ход совершается в ту ячейку, которая осталась последней в полосе победы
     *
     * @param playerChip
     * @param winCount
     * @return
     */
    static int checkAllPossibleWinAndTurn(char playerChip, int winCount) {
        for (int y = 0; y < fieldSizeY; y++) {
            for (int x = 0; x < fieldSizeX; x++) {
                if (checkVerticalAndTurn(x, y, playerChip, winCount)) {
                    return 1;
                } else if (checkHorizontalAndTurn(x, y, playerChip, winCount)) {
                    return 1;
                } else if (checkDiagonalLeftTopRightBottomAndTurn(x, y, playerChip, winCount)) {
                    return 1;
                } else if (checkDiagonalLeftBottomRightTopAndTurn(x, y, playerChip, winCount)) {
                    return 1;
                }
            }
        }
        return 0;
    }

    /**
     * Вспомогательный метод, который совершает ход в последнюю оставшуюся ячейку на полосе победы по вертикали
     *
     * @param cordX
     * @param cordY
     * @param playerChip
     * @param winCount
     * @return
     */
    private static boolean checkVerticalAndTurn(int cordX, int cordY, char playerChip, int winCount) {
        listMoves.clear();
        listOperationVertical(cordX, cordY, playerChip, winCount);
        if (listMoves.size() == 1) {
            turnThisCell(cordX, cordY + listMoves.get(0), DOT_AI);
            return true;
        }
        return false;
    }

    /**
     * Вспомогательный метод, который совершает ход в последнюю оставшуюся ячейку на полосе победы по горизонтали
     *
     * @param cordX
     * @param cordY
     * @param playerChip
     * @param winCount
     * @return
     */
    private static boolean checkHorizontalAndTurn(int cordX, int cordY, char playerChip, int winCount) {
        listMoves.clear();
        listOperationHorizontal(cordX, cordY, playerChip, winCount);
        if (listMoves.size() == 1) {
            turnThisCell(cordX + listMoves.get(0), cordY, DOT_AI);
            return true;
        }
        return false;
    }

    /**
     * Вспомогательный метод, который совершает ход в последнюю оставшуюся ячейку на полосе победы по диагонали \
     *
     * @param cordX
     * @param cordY
     * @param playerChip
     * @param winCount
     * @return
     */
    private static boolean checkDiagonalLeftTopRightBottomAndTurn(int cordX, int cordY, char playerChip,
                                                                  int winCount) {
        listMoves.clear();
        listOperationDiagonalLeftTopRightBottom(cordX, cordY, playerChip, winCount);
        if (listMoves.size() == 1) {
            turnThisCell(cordX + listMoves.get(0), cordY + listMoves.get(0), DOT_AI);
            return true;
        }
        return false;
    }

    /**
     * Вспомогательный метод, который совершает ход в последнюю оставшуюся ячейку на полосе победы по диагонали /
     *
     * @param cordX
     * @param cordY
     * @param playerChip
     * @param winCount
     * @return
     */
    private static boolean checkDiagonalLeftBottomRightTopAndTurn(int cordX, int cordY, char playerChip,
                                                                  int winCount) {
        listMoves.clear();
        listOperationDiagonalLeftBottomRightTop(cordX, cordY, playerChip, winCount);
        if (listMoves.size() == 1) {
            turnThisCell(cordX + listMoves.get(0), cordY - listMoves.get(0), DOT_AI);
            return true;
        }
        return false;
    }

    /**
     * Метод для своевременного пресечения составления выйгрышной комбинации
     * с совершением хода без привязке к ячейке.
     * Совершает ход в одну из центральных ячеек, если до победы осталось поставить 2 фишки
     * в соответствующую полосу победы.
     *
     * @param playerChip
     * @param winCount
     * @return
     */
    static int checkAllPossiblePreWinAndTurn(char playerChip, int winCount) {
        for (int y = 0; y < fieldSizeY; y++) {
            for (int x = 0; x < fieldSizeX; x++) {
                if (checkVerticalAndTurnForPre(x, y, playerChip, winCount)) {
                    return 1;
                } else if (checkHorizontalAndTurnForPre(x, y, playerChip, winCount)) {
                    return 1;
                } else if (checkDiagonalLeftTopRightBottomAndTurnForPre(x, y, playerChip, winCount)) {
                    return 1;
                } else if (checkDiagonalLeftBottomRightTopAndTurnForPre(x, y, playerChip, winCount)) {
                    return 1;
                }
            }
        }
        return 0;
    }

    /**
     * Вспомогательный метод для своевременного пресечения составления выйгрышной комбинации по вертикали
     * Совершает ход в одну из центральных ячеек, если до победы осталось поставить 2 фишки
     *
     * @param cordX
     * @param cordY
     * @param playerChip
     * @param winCount
     * @return
     */
    private static boolean checkVerticalAndTurnForPre(int cordX, int cordY, char playerChip, int winCount) {
        listMoves.clear();
        listOperationVertical(cordX, cordY, playerChip, winCount);
        if (listMoves.size() == 2) {
            if (listMoves.contains(1)) {
                turnThisCell(cordX, cordY + 1, DOT_AI);
                return true;
            } else if (listMoves.contains(2)) {
                turnThisCell(cordX, cordY + 2, DOT_AI);
                return true;
            }
        }
        return false;
    }

    /**
     * Вспомогательный метод для своевременного пресечения составления выйгрышной комбинации по горизонтали
     * Совершает ход в одну из центральных ячеек, если до победы осталось поставить 2 фишки
     *
     * @param cordX
     * @param cordY
     * @param playerChip
     * @param winCount
     * @return
     */
    private static boolean checkHorizontalAndTurnForPre(int cordX, int cordY, char playerChip, int winCount) {
        listMoves.clear();
        listOperationHorizontal(cordX, cordY, playerChip, winCount);
        if (listMoves.size() == 2) {
            if (listMoves.contains(1)) {
                turnThisCell(cordX + 1, cordY, DOT_AI);
                return true;
            } else if (listMoves.contains(2)) {
                turnThisCell(cordX + 2, cordY, DOT_AI);
                return true;
            }
        }
        return false;
    }

    /**
     * Вспомогательный метод для своевременного пресечения составления выйгрышной комбинации по диагонали \
     * Совершает ход в одну из центральных ячеек, если до победы осталось поставить 2 фишки
     *
     * @param cordX
     * @param cordY
     * @param playerChip
     * @param winCount
     * @return
     */
    private static boolean checkDiagonalLeftTopRightBottomAndTurnForPre(int cordX, int cordY, char playerChip,
                                                                        int winCount) {
        listMoves.clear();
        listOperationDiagonalLeftTopRightBottom(cordX, cordY, playerChip, winCount);
        if (listMoves.size() == 2) {
            if (listMoves.contains(1)) {
                turnThisCell(cordX + 1, cordY + 1, DOT_AI);
                return true;
            } else if (listMoves.contains(2)) {
                turnThisCell(cordX + 2, cordY + 2, DOT_AI);
                return true;
            }
        }
        return false;
    }

    /**
     * Вспомогательный метод для своевременного пресечения составления выйгрышной комбинации по диагонали /
     * Совершает ход в одну из центральных ячеек, если до победы осталось поставить 2 фишки
     *
     * @param cordX
     * @param cordY
     * @param playerChip
     * @param winCount
     * @return
     */
    private static boolean checkDiagonalLeftBottomRightTopAndTurnForPre(int cordX, int cordY, char playerChip,
                                                                        int winCount) {
        listMoves.clear();
        listOperationDiagonalLeftBottomRightTop(cordX, cordY, playerChip, winCount);
        if (listMoves.size() == 2) {
            if (listMoves.contains(1)) {
                turnThisCell(cordX + 1, cordY - 1, DOT_AI);
                return true;
            } else if (listMoves.contains(2)) {
                turnThisCell(cordX + 2, cordY - 2, DOT_AI);
                return true;
            }
        }
        return false;
    }

    /**
     * Метод для итерации по списку возможных ходов для вспомогательного метода
     * пресечения победы по вертикали
     *
     * @param cordX
     * @param cordY
     * @param playerChip
     * @param winCount
     */
    private static void listOperationVertical(int cordX, int cordY, char playerChip, int winCount) {
        int bias = 0;
        for (int i = cordY; i < winCount + cordY; i++) {
            if (i >= fieldSizeY ||
                    field[i][cordX] != playerChip && field[i][cordX] != DOT_EMPTY) {
                listMoves.clear();
                return;
            }
            if (field[i][cordX] == DOT_EMPTY) {
                listMoves.add(bias);
            }
            bias++;
        }
    }

    /**
     * Метод для итерации по списку возможных ходов для вспомогательного метода
     * пресечения победы по горизонтали
     *
     * @param cordX
     * @param cordY
     * @param playerChip
     * @param winCount
     */
    private static void listOperationHorizontal(int cordX, int cordY, char playerChip, int winCount) {
        int bias = 0;
        for (int i = cordX; i < winCount + cordX; i++) {
            if (i >= fieldSizeX ||
                    field[cordY][i] != playerChip && field[cordY][i] != DOT_EMPTY) {
                listMoves.clear();
                return;
            }
            if (field[cordY][i] == DOT_EMPTY) {
                listMoves.add(bias);
            }
            bias++;
        }
    }

    /**
     * Метод для итерации по списку возможных ходов для вспомогательного метода
     * пресечения победы по диагонали \
     *
     * @param cordX
     * @param cordY
     * @param playerChip
     * @param winCount
     */
    private static void listOperationDiagonalLeftTopRightBottom(int cordX, int cordY, char playerChip,
                                                                int winCount) {
        int bias = 0;
        for (int i = 0; i < winCount; i++) {
            if (cordY + i >= fieldSizeY ||
                    cordX + i >= fieldSizeX ||
                    field[cordY + i][cordX + i] != playerChip && field[cordY + i][cordX + i] != DOT_EMPTY) {
                listMoves.clear();
                return;
            }
            if (field[cordY + i][cordX + i] == DOT_EMPTY) {
                listMoves.add(bias);
            }
            bias++;
        }
    }

    /**
     * Метод для итерации по списку возможных ходов для вспомогательного метода
     * пресечения победы по диагонали /
     *
     * @param cordX
     * @param cordY
     * @param playerChip
     * @param winCount
     */
    private static void listOperationDiagonalLeftBottomRightTop(int cordX, int cordY, char playerChip,
                                                                int winCount) {
        int bias = 0;
        for (int i = 0; i < winCount; i++) {
            if (cordY - i < 0 ||
                    cordX + i >= fieldSizeX ||
                    field[cordY - i][cordX + i] != playerChip && field[cordY - i][cordX + i] != DOT_EMPTY) {
                listMoves.clear();
                return;
            }
            if (field[cordY - i][cordX + i] == DOT_EMPTY) {
                listMoves.add(bias);
            }
            bias++;
        }
    }

    /**
     * Метод для совершения хода в выбранную ячейку
     *
     * @param x
     * @param y
     * @param playerChip
     */
    private static void turnThisCell(int x, int y, char playerChip) {
        field[y][x] = playerChip;
    }

    /**
     * Проверка на ничью
     *
     * @return
     */
    static boolean checkDraw() {
        for (int x = 0; x < fieldSizeX; x++) {
            for (int y = 0; y < fieldSizeY; y++)
                if (isCellEmpty(x, y)) return false;
        }
        return true;
    }

    /**
     * Метод проверки состояния игры
     *
     * @param c
     * @param str
     * @return
     */
    static boolean gameCheck(char c, String str) {
        if (checkWin(c, WIN_COUNT)) {
            System.out.println(str);
            return true;
        }
        if (checkDraw()) {
            System.out.println("Ничья!");
            return true;
        }

        return false; // Игра продолжается
    }
}
