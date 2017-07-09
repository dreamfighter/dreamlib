package com.dreamfighter.android.security;

/**
 * Created by zeger on 01/07/17.
 */

public class Rc4Decode{
    public static int SIZE=0;
    public static int[] S = new int[SIZE]; //filled with random numbers [0,0,0]
    public static int[] T = new int[SIZE]; //filled with keytext
    public static String[] R = new String[SIZE];

    //public int i,j;
    //public static String key, input;
    //String bitoriginal="";
    /*public static void main (String args[]){

        Rc4Decode rc4decode = new Rc4Decode();
        rc4decode.decode("0000011010", "1432");

    }*/
    public static String decode(String randombit, String key){

        String input = randombit;

        SIZE=input.length();
        S = new int[SIZE];
        T = new int[SIZE];
        R = new String[SIZE];

        INIT(key);
        KSA();

        for(int i=0;i<S.length;i++){
            R[S[i]]=""+input.charAt(i);
        }
        StringBuilder bitoriginal = new StringBuilder();
        for(int i=0;i<R.length;i++){
            bitoriginal.append(R[i]);
        }

        return bitoriginal.toString();
    }

    static void INIT(String key){

        for(int i=0; i<SIZE; i++){
            S[i]=i;

        }
        int j=0;
        while(j<SIZE){
            T[j]=Integer.parseInt(key.charAt(j % key.length())+"");

            j++;
        }
    }
    static void KSA(){
        for(int i=0; i<SIZE; i++){
            S[i]=i;
        }
        int j=0;
        for(int i=0; i<SIZE; i++){
            j = (j + S[i] + T[i]) % SIZE;
            swap(i, j);
        }
    }
    static void swap(int i, int j){
        int temp = S[i];
        S[i]= S[j];
        S[j] = temp;
    }
}
