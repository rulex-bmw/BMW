package com.rulex.tools.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ExecuteCmdUtil {

    /**
     * 执行cmd
     *
     * @param cmd 执行的语句
     * @return String 执行的结果
     * @throws IOException, InterruptedException
     */
    public static String executeCmd(String cmd) throws IOException, InterruptedException {

        Runtime rt = Runtime.getRuntime();
        //执行命令
        Process process = rt.exec(cmd);
        int status = process.waitFor();
        if (status == 0) {
            System.out.println("Execute successfully");
        } else {
            System.out.println("Execute failure");
        }
        //执行结果,得到进程的标准输出信息流
        InputStream stderr = process.getInputStream();
        //将字节流转化成字符流
        InputStreamReader isr = new InputStreamReader(stderr);
        //将字符流以缓存的形式一行一行输出
        BufferedReader br = new BufferedReader(isr);

        StringBuilder build = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {

            build.append(line + "\n");
        }

        br.close();
        isr.close();
        stderr.close();

        return build.toString();
    }
}
