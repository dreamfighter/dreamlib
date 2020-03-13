package id.dreamfighter.android.security;

/**
 * Created by zeger on 01/07/17.
 */

public class Rc4Encode {

    public static int SIZE = 0;
    public static int[] S = new int[SIZE]; //filled with random numbers [0,0,0]
    public static int[] T = new int[SIZE]; //filled with keytext
    //public int i, j;
    public static String key, input;

    public static String encode(String biner, String Key) {
        input = biner;
        key = Key;
        SIZE = input.length();
        S = new int[SIZE];
        T = new int[SIZE];
        StringBuilder bitacak = new StringBuilder();
        INIT();
        KSA();
        for (int i = 0; i < S.length; i++) {
            bitacak.append(input.charAt(S[i]));
        }
        return bitacak.toString();
    }

    static void INIT() {

        for (int i = 0; i < SIZE; i++) {
            S[i] = i;

        }
        int j = 0;
        while (j < SIZE) {
            T[j] = Integer.parseInt(key.charAt(j % key.length()) + "");
            System.out.println(T[j]);
            j++;
        }

    }

    static void KSA() {

        for (int i = 0; i < SIZE; i++) {
            S[i] = i;
        }
        int j = 0;
        for (int i = 0; i < SIZE; i++) {
            j = (j + S[i] + T[i]) % SIZE;
            swap(i, j);
        }

    }

    static void swap(int i, int j) {
        int temp = S[i];
        S[i] = S[j];
        S[j] = temp;
    }

    public static String leftPad(String input, int length, String fill) {
        String pad = String.format("%" + length + "s", "").replace(" ", fill) + input.trim();
        return pad.substring(pad.length() - length, pad.length());
    }
}
