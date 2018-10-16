package com.rulex.tools;

import com.rulex.tools.utils.ExecuteCmdUtil;
import com.rulex.tools.utils.JavaCompilerUtil;

import java.io.IOException;

public class FormatConversion {

    public static String packagePath = PathSet.packagePath.replace(".", "/");

    public static String resourcePath = "bmw-tools/target/classes";

    public static String proto_CMD = "protoc.exe -I=. --java_out=./" + resourcePath + " ./" + resourcePath + "/entity/pojo.proto";

    public static String javaSourcePath = PathSet.javaPath + packagePath + "/RulexBean.java";

    public static String classOutPath = PathSet.xmlPath;

    public static String jar_CMD = "jar cvf bean.jar -C " + resourcePath + "/entity com";

    public static void formatConversion() throws IOException, InterruptedException {

        //将.proto文件转化成.java文件
        System.out.println("生成.java文件：");
        ExecuteCmdUtil.executeCmd(proto_CMD);

        //将.java文件转化成.class文件
        System.out.println("生成.class文件：");
        if (JavaCompilerUtil.CompilerJavaFile(javaSourcePath, classOutPath)) {
            System.out.println("Compiler successfully");
        } else {
            System.out.println("Compiler failure");
        }
        //将.class文件打成jar包
        System.out.println("打jar包：");
        ExecuteCmdUtil.executeCmd(jar_CMD);
    }
}