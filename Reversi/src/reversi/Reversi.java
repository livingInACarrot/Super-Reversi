package reversi;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;
/*
draw()
possibilities()
rowIsOk(int, int, int, int)
move()
redrawRow(int, int, int, int)
chipsInRow(int, int, int, int)
copy(int[][], int[][])
cancel()
menu()
end()
bot()

Main {
    main()
}



 */




// 0 = _, 1 = ●, 2 = ⭘, 3 = возможные ходы
// Не обращайте внимание на то, что название Reversi, это финский манер.
public class Reversi {
    public static int[][] F;                // Field - игровое поле
    public static int player;               // Номер ходящего игрока - 1 или 2
    public static boolean singleplayer;     // PvE или PvP
    public static Vector<Integer> possible; // Возможные для хода поля
    public static int best;                 // Лучший счёт за сессию (в режиме против бота)
    public static int[][] previousF;        // Как выглядело поле ход назад (нужно для отмены хода)
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";

    // Выводит на экран игровое поле.
    public static void draw() {
        System.out.print(ANSI_RESET);
        int count = 1;
        for (int[] row : F) {
            for (int el : row) {
                switch (el) {
                    case 0 -> System.out.print(".  ");
                    case 1 -> System.out.print("●  ");
                    case 2 -> System.out.print("⭘  ");
                    case 3 -> {
                        if (count >= 10) {
                            System.out.print(count++ + " ");
                        } else {
                            System.out.print(count++ + "  ");
                        }
                    }
                }
            }
            System.out.print("\n");
        }
    }

    // Выявляет в поле возможные для хода клетки.
    public static void possibilities() {
        for (int i = 0; i < 8; ++i) {
            for (int j = 0; j < 8; ++j) {
                if (F[i][j] == 0 || F[i][j] == 3) {
                    F[i][j] = 0; // Чтобы все тройки убрать
                    if (j < 7 && F[i][j + 1] == 3 - player && rowIsOk(i, j, 0, 1))
                        F[i][j] = 3;
                    else if (j < 7 && i < 7 && F[i + 1][j + 1] == 3 - player && rowIsOk(i, j, 1, 1))
                        F[i][j] = 3;
                    else if (i < 7 && F[i + 1][j] == 3 - player && rowIsOk(i, j, 1, 0))
                        F[i][j] = 3;
                    else if (i < 7 && j > 0 && F[i + 1][j - 1] == 3 - player && rowIsOk(i, j, 1, -1))
                        F[i][j] = 3;
                    else if (j > 0 && F[i][j - 1] == 3 - player && rowIsOk(i, j, 0, -1))
                        F[i][j] = 3;
                    else if (i > 0 && j > 0 && F[i - 1][j - 1] == 3 - player && rowIsOk(i, j, -1, -1))
                        F[i][j] = 3;
                    else if (i > 0 && F[i - 1][j] == 3 - player && rowIsOk(i, j, -1, 0))
                        F[i][j] = 3;
                    else if (i > 0 && j < 7 && F[i - 1][j + 1] == 3 - player && rowIsOk(i, j, -1, 1))
                        F[i][j] = 3;
                }
            }
        }
        possible = new Vector<>();
        for (int i = 0; i < 8; ++i) {
            for (int j = 0; j < 8; ++j) {
                if (F[i][j] == 3)
                    possible.add(i * 10 + j);
            }
        }
    }

    // Проверяет, можно ли в конце ряда поставить фишку.
    public static boolean rowIsOk(int row, int col, int vert, int hor) {
        row += vert;
        col += hor;
        while (true) {
            row += vert;
            col += hor;
            if (row == -1 || row == 8 || col == -1 || col == 8)
                return false;
            if (F[row][col] == player)
                return true;
            if (F[row][col] == 0 || F[row][col] == 3)
                return false;
        }
    }

    // Ходыт... хадит... ход делат тут вв общем.
    // Самый важный метод в программе.
    public static void move() {
        int r, c;

        System.out.print(ANSI_BLUE);
        if (player == 1)
            System.out.println("Ход ●.");
        else if (player == 2 && !singleplayer)
            System.out.println("Ход ⭘.");
        else
            System.out.println(ANSI_CYAN + "Ход бота.");

        // Если ходов нет
        if (possible.size() == 0) {
            player = 3 - player;
            possibilities();
            if (possible.size() == 0) {
                System.out.println(ANSI_PURPLE + "Ходов не осталось.\nИгра окончена.");
                end();
                return;
            }
            System.out.println(ANSI_RED + "Ходов нет. Очередь другого игрока.");
            draw();
            move();
        }

        if (singleplayer && player == 2) {
            int result = bot();
            r = result / 10;
            c = result % 10;
        } else {
            int[][] prev = new int[8][8];
            copy(prev, F);

            int move;
            Scanner in = new Scanner(System.in);
            while (true) {
                String input = in.nextLine();
                try {
                    move = Integer.parseInt(input.trim());
                    if (move == -1) {
                        cancel();
                    } else if (move < 1 || move > possible.size()) {
                        System.out.println(ANSI_RED + "Введите одно из чисел на поле.");
                    } else {
                        break;
                    }
                } catch (Exception ex) {
                    System.out.println(ANSI_RED + "Введите одно из чисел на поле.");
                }
            }
            --move;
            r = possible.get(move) / 10;
            c = possible.get(move) % 10;
            previousF = prev.clone();
        }

        F[r][c] = player;
        if (c < 7 && F[r][c + 1] == 3 - player && rowIsOk(r, c, 0, 1))
            redrawRow(r, c, 0, 1);
        if (r < 7 && c < 7 && F[r + 1][c + 1] == 3 - player && rowIsOk(r, c, 1, 1))
            redrawRow(r, c, 1, 1);
        if (r < 7 && F[r + 1][c] == 3 - player && rowIsOk(r, c, 1, 0))
            redrawRow(r, c, 1, 0);
        if (r < 7 && c > 0 && F[r + 1][c - 1] == 3 - player && rowIsOk(r, c, 1, -1))
            redrawRow(r, c, 1, -1);
        if (c > 0 && F[r][c - 1] == 3 - player && rowIsOk(r, c, 0, -1))
            redrawRow(r, c, 0, -1);
        if (r > 0 && c > 0 && F[r - 1][c - 1] == 3 - player && rowIsOk(r, c, -1, -1))
            redrawRow(r, c, -1, -1);
        if (r > 0 && F[r - 1][c] == 3 - player && rowIsOk(r, c, -1, 0))
            redrawRow(r, c, -1, 0);
        if (r > 0 && c < 7 && F[r - 1][c + 1] == 3 - player && rowIsOk(r, c, -1, 1))
            redrawRow(r, c, -1, 1);

        // Переход хода
        player = 3 - player;
        possibilities();
        draw();
        move();
    }

    // Перекрашивает ряд фишек в цвет фишек противника при их захвате.
    public static void redrawRow(int row, int col, int vert, int hor) {
        while (true) {
            row += vert;
            col += hor;
            if (F[row][col] == player)
                return;
            F[row][col] = player;
        }
    }

    // Подсчёт фишек в ряду (нужно для бота, чтобы он взвесил выгоду разных ходов).
    public static int chipsInRow(int row, int col, int vert, int hor) {
        int chips = 0;
        while (true) {
            row += vert;
            col += hor;
            if (F[row][col] == player)
                return chips;
            ++chips;
        }
    }

    // Обычное копирование массивов, листайте дальше. (как оказалось, .clone() не работает)
    public static void copy(int[][] arr1, int[][] arr2) {
        // Элементы arr2 копируем в arr1
        for (int i = 0; i < 8; i++) {
            System.arraycopy(arr2[i], 0, arr1[i], 0, 8);
        }
    }

    // Отмена хода.
    public static void cancel() {
        if (previousF.length == 0) {
            System.out.println(ANSI_RED + "Сейчас отменить ход невозможно.");
        } else {
            System.out.println(ANSI_PURPLE + "Ход отменён.");
            F = previousF.clone();
            previousF = new int[0][0];
            draw();
            move();
        }
    }

    // Главное меню собсна.
    public static void menu() {
        player = 1;
        previousF = new int[0][0];
        F = new int[8][8];
        F[3][3] = 2;
        F[4][4] = 2;
        F[3][4] = 1;
        F[4][3] = 1;

        String score;
        if (best < 10)
            score = best + "   │";
        else
            score = best + "  │";
        System.out.print(ANSI_RESET);
        System.out.println("┌───────────────────────────────┐");
        System.out.println("│" + ANSI_PURPLE + "            REVERSI            " + ANSI_RESET + "│");
        System.out.println("│                               │");
        System.out.println("│  Лучший счёт за сегодня - " + score);
        System.out.println("│                               │");
        System.out.println("│     - - - - - - - - - - -     │");
        System.out.println("│           Подсказка:          │");
        System.out.println("│    Чтобы сделать ход, введи   │");
        System.out.println("│     одно из чисел на поле.    │");
        System.out.println("│             - - -             │");
        System.out.println("│  Для отмены хода введи \"-1\",  │");
        System.out.println("│     но это сработает лишь     │");
        System.out.println("│          раз за ход.          │");
        System.out.println("│     - - - - - - - - - - -     │");
        System.out.println("│ Если хочешь начать новую игру,│");
        System.out.println("│       введи режим игры.       │");
        System.out.println("└───────────────────────────────┘");
        System.out.println(ANSI_BLUE + "PvP(2 игрока) или PvE(против бота)?");
        System.out.println("Напишите три буквы." + ANSI_RESET);
        Scanner in = new Scanner(System.in);
        while (true) {
            String input = in.nextLine().trim().toLowerCase();
            if (Objects.equals(input, "pvp")) {
                singleplayer = false;
                break;
            } else if (Objects.equals(input, "pve")) {
                singleplayer = true;
                break;
            } else {
                System.out.println(ANSI_RED + "Введите \"PvP\" или \"PvE\"." + ANSI_RESET);
            }
        }
        possibilities();
        draw();
        move();
    }

    // Конец игры.......
    public static void end() {
        int score1 = 0;
        int score2 = 0;
        for (int[] row : F) {
            for (int el : row) {
                if (el == 1) {
                    ++score1;
                } else if (el == 2) {
                    ++score2;
                }
            }
        }
        System.out.println();

        if (singleplayer) {
            if (score1 > best)
                best = score1;

            String str;
            if (score1 < 10)
                str = score1 + "         │";
            else
                str = score1 + "        │";
            if (score1 > score2) {
                System.out.print(ANSI_GREEN);
                System.out.println("┌───────────────────────────────┐");
                System.out.println("│            ПОБЕДА!            │");
            } else {
                System.out.print(ANSI_RED);
                System.out.println("┌───────────────────────────────┐");
                System.out.println("│           ПОРАЖЕНИЕ           │");
            }
            System.out.println("│         Твой счёт - " + str);
            System.out.println("└───────────────────────────────┘");
        } else {
            System.out.print(ANSI_BLUE);
            String str1, str2;
            if (score1 < 10) {
                str1 = score1 + "] ";
                str2 = score2 + "]";
            } else {
                str1 = score1 + "]";
                if (score1 > 54) {
                    str2 = score2 + "] ";
                } else {
                    str2 = score2 + "]";
                }
            }
            System.out.println("┌───────────────────────────────┐");
            if (score1 > score2) {
                System.out.println("│     ИГРОК \"●\" ВЫИГРАЛ!        │");
            } else {
                System.out.println("│     ИГРОК \"⭘\" ВЫИГРАЛ!        │");
            }
            System.out.println("│             Счёт:             │");
            System.out.println("│     ● [" + str1 + "         ⭘ [" + str2 + "     │");
            System.out.println("└───────────────────────────────┘");
        }
        System.out.println();
        menu();
    }

    // Всем тихо, бот думоет!
    // Его стратегия простая - он выбирает из возможных ходов самые сытные по поеданию чужих фишек
    // и из самых сытных вариантов выбирает уже наобум. В общем, играет примерно как я.
    public static int bot() {
        Random random = new Random();
        int r, c, good;
        int maxGood = -1;
        Vector<Integer> maxis = new Vector<>();
        for (int coord : possible) {
            r = coord / 10;
            c = coord % 10;
            good = 0;
            if (c < 7 && F[r][c + 1] == 3 - player && rowIsOk(r, c, 0, 1))
                good += chipsInRow(r, c, 0, 1);
            if (r < 7 && c < 7 && F[r + 1][c + 1] == 3 - player && rowIsOk(r, c, 1, 1))
                good += chipsInRow(r, c, 1, 1);
            if (r < 7 && F[r + 1][c] == 3 - player && rowIsOk(r, c, 1, 0))
                good += chipsInRow(r, c, 1, 0);
            if (r < 7 && c > 0 && F[r + 1][c - 1] == 3 - player && rowIsOk(r, c, 1, -1))
                good += chipsInRow(r, c, 1, -1);
            if (c > 0 && F[r][c - 1] == 3 - player && rowIsOk(r, c, 0, -1))
                good += chipsInRow(r, c, 0, -1);
            if (r > 0 && c > 0 && F[r - 1][c - 1] == 3 - player && rowIsOk(r, c, -1, -1))
                good += chipsInRow(r, c, -1, -1);
            if (r > 0 && F[r - 1][c] == 3 - player && rowIsOk(r, c, -1, 0))
                good += chipsInRow(r, c, -1, 0);
            if (r > 0 && c < 7 && F[r - 1][c + 1] == 3 - player && rowIsOk(r, c, -1, 1))
                good += chipsInRow(r, c, -1, 1);

            if (good > maxGood) {
                maxGood = good;
                maxis = new Vector<>();
                maxis.add(coord);
            } else if (good == maxGood) {
                maxis.add(coord);
            }
        }
        return maxis.get(random.nextInt(maxis.size()));
    }
}