package com.xy.mapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        ArrayList<Integer> arrayList =  new ArrayList<>();
        Scanner sc =  new Scanner(System.in);
        int M = sc.nextInt(),N = sc.nextInt();
        xy[] arr = new xy[N];
        int[][] r = new int[N][3];

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < 3; j++) {
                System.out.println(r[i][j]);
            }
        }

        for(int i=0;i<M;i++) {
            for(int j=0;j<N;j++)
                arr[j] = new xy(j,sc.nextInt());

            Arrays.sort(arr, new Comparator<xy>() {
                @Override
                public int compare(xy o1, xy o2) {
                    return o2.sorce-o1.sorce;
                }
            });
//            r[arr[0].a].a +=1 ;
//            r[arr[1].a].b +=1;
//            r[arr[2].a].c +=1;
        }
//        for(int i=0;i<N;i++) {
//            System.out.println(r[i].a+" "+r[i].b+" "+r[i].c);
//        }
    }

    static class xy{
        int a;
        int sorce;
        public xy(int a,int sorce) {
            this.a=a;
            this.sorce=sorce;
        }
    }

    static class result{
        int a;
        int b;
        int c;
        public result(int a,int b,int c) {
            this.a=a;
            this.b=b;
            this.c=c;
        }
    }
}