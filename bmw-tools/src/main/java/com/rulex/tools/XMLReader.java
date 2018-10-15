package com.rulex.tools;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import static org.fusesource.leveldbjni.JniDBFactory.bytes;


public class XMLReader {


    public Document readerXML() throws DocumentException {

        // 读取xml文件
        SAXReader sr = new SAXReader();
        File file = new File(PathSet.xmlPath + "pojo.xml");
        Document doc = sr.read(file);
        return doc;
    }


    /**
     * 解析xml文件
     *
     * @throws DocumentException
     * @throws IOException
     */
    public static void parse() throws DocumentException, IOException {
        XMLReader xmlReader = new XMLReader();
        Document doc = xmlReader.readerXML();
        File file = new File(PathSet.xmlPath + "pojo.proto");
        String protocol = String.format("package %1$s;\noption java_outer_classname = %2$s;", PathSet.packagePath, "\"RulexBean\"");
        //解析根节点
        Element root = doc.getRootElement();
        //解析record节点
        List<Element> records = root.elements("record");
        if (records == null) {
            return;
        }
        for(Element record : records) {
            String name = record.attribute("name").getValue();
            if (name == null) {
                throw new RuntimeException("record名称不能为空");
            }
            name = name.substring(0, 1).toUpperCase() + name.substring(1);
            protocol += "\nmessage " + name + " {";
            String groupable = record.attributeValue("groupable");
            //可合并
            if (groupable.equals("true")) {

                //不合并
            } else {
                List<Element> fields = record.elements();
                String paramName;
                String isnull;
                String type;
                String maxvalue;
                String minvalue;
                String maxsize;
                String minsize;
                String transforable;
                String length;
                int i = 0;
                for(Element field : fields) {
                    isnull = field.attributeValue("isnull");
                    paramName = field.attributeValue("name");
                    if (StringUtils.isBlank(paramName)) {
                        continue;
                    }
                    if (isnull.equals("") || isnull.equals("false")) {//不填或false表示不能为空
                        protocol += "\n\trequired ";
                    } else {//表示可以为空
                        protocol += "\n\toptional ";
                    }
                    type = field.attributeValue("type");
                    if (type.equals("Integer")) {
                        maxvalue = field.attributeValue("maxvalue");
                        minvalue = field.attributeValue("minvalue");
                        protocol += "int32 ";
                    } else if (type.equals("Long")) {
                        maxvalue = field.attributeValue("maxvalue");
                        minvalue = field.attributeValue("minvalue");
                        protocol += "int64 ";
                    } else if (type.equals("String")) {
                        maxsize = field.attributeValue("maxsize");
                        minsize = field.attributeValue("minsize");
                        protocol += "string ";
                    }
                    i++;
                    protocol += paramName + " = " + i + ";";
                    length = field.attributeValue("length");
                    transforable = field.attributeValue("transforable");

                }

            }


            protocol += "\n}";
        }
        FileOutputStream out = new FileOutputStream(file);
        out.write(bytes(protocol));
        out.close();
    }


    public static void main(String[] args) {
        try {
            XMLReader.parse();
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
