package com.rulex.dsm.utils;

import com.rulex.bsb.utils.TypeUtils;
import com.rulex.dsm.bean.ConnectionProperties;
import com.rulex.dsm.bean.Field;
import com.rulex.dsm.bean.Primary;
import com.rulex.dsm.bean.Source;
import com.rulex.dsm.interceptor.BMWStmtInterceptor;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XmlUtil {

    public static Document readerXML() throws DocumentException, IOException {

        // 读取xml文件
        SAXReader sr = new SAXReader();
        //本地测试使用
//        File file = new File(SqlStatementInterceptor.class.getResource("/").getPath() + "rulex-condition1.xml");

        InputStream inputStream = BMWStmtInterceptor.class.getClassLoader().getResourceAsStream("xml/rulex-condition.xml");
        Document doc = sr.read(inputStream);
        inputStream.close();
        return doc;
    }

    /**
     * 解析xml文件，获取所有配置参数信息
     *
     * @return 解析的参数信息对象
     * @throws DocumentException
     * @throws IOException
     */
    public static List<Source> parseXML() throws DocumentException, IOException {
        List<Source> sourceList = new ArrayList<>();
        //解析source节点
        List<Element> sources = readerXML().getRootElement().elements("record");
        if (sources == null) {
            return null;
        }
        for(Element s : sources) {
            Source source = new Source();
            source.setId(Integer.valueOf(s.attributeValue("id")));
            source.setName(TypeUtils.InitialsLow2Up(s.attribute("name").getValue()));
            source.setGroupable(Boolean.valueOf(s.attributeValue("groupable")));
            source.setTable(s.attributeValue("table"));
            source.setPojo(s.attributeValue("pojo"));
            source.setConProperties(parseConnection(s.element("connection")));// 解析数据库连接信息
            source.setKeys(parsePrimary(s.elements("key")));// 解析所有主键
            source.setFields(parseFields(s.elements("field")));// 解析所有上链信息
            sourceList.add(source);
        }
        return sourceList;
    }


    /**
     * 解析所有上链信息
     *
     * @param field 上链字段信息
     * @return 上链信息
     */
    public static List<Field> parseFields(List<Element> field) {
        List<Field> fields = new ArrayList<>();
        for (Element f : field) {
            Field fi = new Field();
            fi.setName(f.attributeValue("name"));
            fi.setColumn(f.attributeValue("column"));
            if (!StringUtils.isBlank(f.attributeValue("fieldId"))) {
                fi.setFieldId(Integer.valueOf(f.attributeValue("fieldId")));
            }
            String isnull = f.attributeValue("isnull");
            fi.setIsnull(isnull.equals("false") || StringUtils.isBlank(isnull) ? false : true);
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
            fi.setTransforable(StringUtils.isBlank(transforable) || transforable.equals("false") ? false : true);
            String fieldId = f.attributeValue("fieldId");
            fi.setFieldId(Integer.valueOf(fieldId));
            fields.add(fi);
        }
        return fields;
    }


    /**
     * 解析所有主键
     *
     * @param keys 上链信息主键
     * @return 主键：自增主键或联合主键
     */
    public static List<Primary> parsePrimary(List<Element> keys) {
        List<Primary> primays = new ArrayList<>();
        for(Element key : keys) {
            Primary primary = new Primary();
            primary.setName(key.attributeValue("name"));
            primary.setColumn(key.attributeValue("column"));
            primary.setType(key.attributeValue("type"));
            String isnull = key.attributeValue("isAuto");
            primary.setIsAuto(isnull.equals("false") || StringUtils.isBlank(isnull) ? false : true);
            primays.add(primary);
        }
        return primays;
    }

    /**
     * 解析数据库连接信息
     *
     * @param con 数据库连接信息Element
     * @return ConnectionProperties：数据库连接信息
     */
    public static ConnectionProperties parseConnection(Element con) {

        ConnectionProperties conProperties = new ConnectionProperties();
        Map<String, String> connectionMap = new HashMap<>();
        List<Element> fields = con.elements("field");
        for(Element fie : fields) {
            connectionMap.put(fie.attributeValue("name"), fie.attributeValue("value"));
        }

        conProperties.setField(connectionMap);
        return conProperties;
    }

}
