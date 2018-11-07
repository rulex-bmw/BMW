package com.rulex.tools.utils;

import javax.tools.*;
import java.io.File;
import java.util.Arrays;

public class
JavaCompilerUtil {

    private static JavaCompiler javaCompiler;

    private JavaCompilerUtil() {
    }

    private static JavaCompiler getJavaCompiler() {
        if (javaCompiler == null) {
            synchronized (JavaCompilerUtil.class) {
                if (javaCompiler == null) {

                    javaCompiler = ToolProvider.getSystemJavaCompiler();
                }
            }
        }
        return javaCompiler;
    }

    /**
     * java文件编译
     *
     * @param sourceFileInputPath 待编译java文件
     * @param classFileOutputPath 编译后的class文件输出路径
     * @return boolean 执行的结果
     */
    public static boolean CompilerJavaFile(String sourceFileInputPath, String classFileOutputPath) {

        // 设置编译选项，配置class文件输出路径
        Iterable<String> options = Arrays.asList("-d", classFileOutputPath);
        StandardJavaFileManager fileManager = getJavaCompiler()
                .getStandardFileManager(null, null, null);

        Iterable<? extends JavaFileObject> compilationUnits = fileManager
                .getJavaFileObjectsFromFiles(Arrays.asList(new File(
                        sourceFileInputPath)));

        return getJavaCompiler().getTask(null, fileManager, null, options,
                null, compilationUnits).call();
    }
}

