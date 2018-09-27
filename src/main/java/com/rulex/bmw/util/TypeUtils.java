package com.rulex.bmw.util;

import java.text.DecimalFormat;

public class TypeUtils {

    public static String doubleToString(double i){
        DecimalFormat decimalFormat = new DecimalFormat("###################.###########");
        return decimalFormat.format(i);
    }


}
