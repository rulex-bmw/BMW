package com.rulex.tools;

public class PathSet {

    //用于生成proto的xml文件位置
    public static String xmlPath = XMLReader.class.getClass().getResource("/entity/").getPath();

    //生成的proto文件package路径
    public static String packagePath = "com.rulex.tools.pojo";

    public static String javaPath = XMLReader.class.getClass().getResource("/").getPath();

}
