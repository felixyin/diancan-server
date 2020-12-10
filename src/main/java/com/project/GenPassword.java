package com.project;

import com.project.util.MD5Util;

public class GenPassword {

    public static void main(String[] args) {
        String password = "123456";
        String jixMi = MD5Util.getStringMD5("xiaodaokeji" + password + "xiaodaokeji").toLowerCase();
        System.out.println(jixMi);
    }

}
