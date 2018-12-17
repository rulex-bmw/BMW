package com.rulex.tools;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.*;
import java.util.List;

import static org.fusesource.leveldbjni.JniDBFactory.bytes;

/**
 *
 */
public class XMLReader {


    /**
     * 读取用户设置的xml文件
     *
     * @return document xml文档对象
     * @throws DocumentException
     * @throws IOException
     */
    public static Document readerXML(String resoucePath) throws DocumentException, IOException {
        SAXReader sr = new SAXReader();
        InputStream is = XMLReader.class.getClassLoader().getResourceAsStream(resoucePath);
        Document doc = sr.read(is);
        is.close();
        return doc;
    }


    /**
     * 解析xml文件，输出proto文件
     *
     * @throws DocumentException
     * @throws IOException
     */
    public static void parse() throws DocumentException, IOException {
        File file = new File(PathSet.xmlPath + "pojo.proto");
        String proto = String.format("package %1$s;\noption java_outer_classname = %2$s;", PathSet.packagePath, "\"RulexBean\"");
        //解析record节点
        List<Element> records = readerXML("xml/rulex-condition.xml").getRootElement().elements("record");
        if (records == null) {
            return;
        }
        for(Element record : records) {
            String name = record.attribute("name").getValue();
            if (name == null) {
                throw new RuntimeException("record名称不能为空");
            }
            name = name.substring(0, 1).toUpperCase() + name.substring(1);
            proto += "\nmessage " + name + " {";
            //可合并
            if (record.attributeValue("groupable").equals("true")) {

                //不合并
            } else {
                List<Element> fields = record.elements();
                for(Element field : fields) {
                    String paramName = field.attributeValue("name");
                    if (StringUtils.isBlank(paramName)) {
                        continue;
                    }
                    String isnull = field.attributeValue("isnull");
                    if (isnull.equals("") || isnull.equals("false")) {//不填或false表示不能为空
                        proto += "\n\trequired ";
                    } else {//表示可以为空
                        proto += "\n\toptional ";
                    }
                    String type = field.attributeValue("type");
                    if (type.equals("Integer")) {
                        proto += "int32 ";
                    } else if (type.equals("Long")) {
                        proto += "int64 ";
                    } else if (type.equals("Float")) {
                        proto += "float ";
                    } else if (type.equals("Double")) {
                        proto += "double ";
                    } else if (type.equals("String")) {
                        proto += "string ";
                    }
                    proto += paramName + " = " + field.attributeValue("fieldId") + ";";
                }
            }
            proto += "\n}";
        }
        FileOutputStream out = new FileOutputStream(file);
        out.write(bytes(proto));
        out.close();
        System.out.println("proto文件已生成");
    }


    public static void main(String[] args) {
        try {
            XMLReader.parse();
            FormatConversion.formatConversion();
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
