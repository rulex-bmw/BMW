package com.rulex.tools;

import com.rulex.tools.utils.ExecuteCmdUtil;
import com.rulex.tools.utils.JavaCompilerUtil;

import java.io.IOException;

public class FormatConversion {

    public static String path1 = PathSet.packagePath.replace(".", "/");

    public static String path2 = "bmw-tools/target/classes/entity";

    public static String proto_CMD = "protoc.exe -I=. --java_out=./" + path2 + " ./" + path2 + "/pojo.proto";

    public static String javaSource = PathSet.xmlPath + path1 + "/RulexBean.java";

    public static String classOut = PathSet.xmlPath;

//    public static String jar_CMD = "jar cvf bean.jar -C " + path2 + "/" + path1 + " RulexBean.class";
public static String jar_CMD = "jar cvf bean.jar -C " + path2  + " com";

    public static void formatConversion() throws IOException, InterruptedException {

        //将.proto文件转化成.java文件
        ExecuteCmdUtil.executeCmd(proto_CMD);

        //将.java文件转化成.class文件
        if (JavaCompilerUtil.CompilerJavaFile(javaSource, classOut)) {
            System.out.println("Compiler successfully");
        } else {
            System.out.println("Compiler failure");
        }
        //将.class文件打成jar包
        ExecuteCmdUtil.executeCmd(jar_CMD);
    }
}