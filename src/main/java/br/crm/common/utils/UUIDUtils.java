package br.crm.common.utils;

import java.util.UUID;

public class UUIDUtils {

    /**
     * 自动生成ID
     */
    public static String getId() {

        return UUID.randomUUID().toString().replace("-", "");

    }

    public static void main(String[] args) {
        System.out.println(UUIDUtils.getId().length());
    }
}
