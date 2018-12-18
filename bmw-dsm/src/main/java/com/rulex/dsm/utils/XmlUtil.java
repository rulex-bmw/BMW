package com.rulex.dsm.utils;

import com.rulex.bsb.utils.TypeUtils;
import com.rulex.dsm.bean.Field;
import com.rulex.dsm.bean.Source;
import com.rulex.dsm.interceptor.SqlStatementInterceptor;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class XmlUtil {

    public static Document readerXML() throws DocumentException, IOException {

        // 读取xml文件
        SAXReader sr = new SAXReader();
        //本地测试使用
//        File file = new File(SqlStatementInterceptor.class.getResource("/").getPath() + "rulex-condition1.xml");

        InputStream inputStream = SqlStatementInterceptor.class.getClassLoader().getResourceAsStream("xml/rulex-condition.xml");
        Document doc = sr.read(inputStream);
        inputStream.close();
        return doc;
    }

    public static List<Source> parseXML() throws DocumentException, IOException {
        List<Source> sourceList = new ArrayList<>();
        //解析source节点
        List<Element> sources = readerXML().getRootElement().elements("record");
        if (sources == null) {
            return null;
        }
        for(Element s : sources) {
            Source source = new Source();
            source.setName(TypeUtils.InitialsLow2Up(s.attribute("name").getValue()));
            source.setGroupable(Boolean.valueOf(s.attributeValue("groupable")));
            source.setTable(s.attributeValue("table"));
            source.setPojo(s.attributeValue("pojo"));
            List<Field> field = new ArrayList<>();
            List<Element> fields = s.elements();
            for(Element f : fields) {
                Field fi = new Field();
                fi.setName(f.attributeValue("name"));
                fi.setColumn(f.attributeValue("column"));
                String isnull = f.attributeValue("isnull");
                fi.setIsnull((isnull.equals("false") || StringUtils.isBlank(isnull)) ? false : true);
                String type = f.attributeValue("type");
                fi.setType(type);
                if (type.equals("Integer") || type.equals("Long") || type.equals("Float") || type.equals("Double")) {
                    String maxvalue = f.attributeValue("maxvalue");
                    String minvalue = f.attributeValue("minvalue");
                    if (!StringUtils.isBlank(maxvalue)) fi.setMaxvalue(maxvalue);
                    if (!StringUtils.isBlank(minvalue)) fi.setMinvalue(minvalue);
                } else if (type.equals("String")) {
                    String maxsize = f.attributeValue("maxsize");
                    String minsize = f.attributeValue("minsize");
                    if (!StringUtils.isBlank(maxsize)) fi.setMaxsize(Integer.valueOf(maxsize));
                    if (!StringUtils.isBlank(minsize)) fi.setMinsize(Integer.valueOf(minsize));
                }
                String length = f.attributeValue("length");
                if (!StringUtils.isBlank(length)) fi.setLength(Integer.valueOf(length));
                String transforable = f.attributeValue("transforable");
                fi.setTransforable(transforable.equals("false") || StringUtils.isBlank(transforable) ? false : true);
                field.add(fi);
            }
            source.setFields(field);
            sourceList.add(source);
        }
        return sourceList;
    }


}
