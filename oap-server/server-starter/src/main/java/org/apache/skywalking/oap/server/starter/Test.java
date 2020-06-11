package org.apache.skywalking.oap.server.starter;

import java.util.Random;

public class Test {

    public static void main(String[] args) {
        StringBuilder builder = new StringBuilder();
        Random random = new Random();

        int para = 200;

        for (int i = 0; i < para; i++) {
            for (int j = 0; j < para; j++) {
                int num = random.nextInt(para) + 1;
                int x = random.nextInt(para) + 1;
                builder.append("[" + x + ", " + num * 50 + "],");
            }
        }
        builder.append("[10, 10000]");
        System.out.println(builder.toString());
    }
}
