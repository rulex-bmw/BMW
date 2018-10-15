package com.rulex.tools;

import com.rulex.tools.utils.ExecuteCmdUtil;
import com.rulex.tools.utils.JavaCompilerUtil;

import java.io.IOException;

public class FormatConversion {

//    public static String proto_CMD = "protoc.exe -I=. --java_out=./target/classes ./target/classes/Pojo.proto";
public static String proto_CMD = "protoc.exe -I=. --java_out=./target/classes "+PathSet.xmlPath+"pojo.proto";


    public static String javaSource = FormatConversion.class.getClass().getResource("/").getPath() + "Pojo.java";

    public static String classOut = FormatConversion.class.getClass().getResource("/").getPath();

    public static String jar_CMD = "jar cvf Pojo.jar -C target/classes/ Pojo.class";


    public static void formatConversion() throws IOException, InterruptedException {
        System.out.println(proto_CMD);
        //将.proto文件转化成.java文件
        ExecuteCmdUtil.executeCmd(proto_CMD);
        System.out.println(classOut);
        //将.java文件转化成.class文件
        if (JavaCompilerUtil.CompilerJavaFile(javaSource, classOut)) {

            System.out.println("Compiler successfully");
        } else {
            System.out.println("Compiler failure");
        }

        //将.class文件打成jar包
        ExecuteCmdUtil.executeCmd(jar_CMD);

    }


    public static void main(String[] args) {
//        try {
//            System.out.println(ExecuteCmdUtil.executeCmd("cmd.exe /c dir"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        System.out.println(PathSet.xmlPath);
        try {
            formatConversion();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    }
