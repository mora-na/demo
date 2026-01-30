package cn.com.inspire.framework.common.util;

import cn.com.lianzhan.boc.syd.communication.model.boc.cpa05.rep.Cpa05Rep;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.naming.NameCoder;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class XStreamTool {
    private static final XStream xstream;

    static {
        // 自定义 NameCoder，禁止将 "_" 转为 "$"
        NameCoder nc = new NameCoder() {
            @Override
            public String encodeNode(String name) {
                return name;
            }

            @Override
            public String encodeAttribute(String name) {
                return name;
            }

            @Override
            public String decodeNode(String nodeName) {
                return nodeName;
            }

            @Override
            public String decodeAttribute(String attributeName) {
                return attributeName;
            }
        };

        xstream = new XStream(new DomDriver("UTF-8", nc));
        xstream.autodetectAnnotations(true);
        xstream.ignoreUnknownElements();
    }

    public static <T> T fromXML(String xml, Class<T> clazz) {
        xstream.allowTypes(new Class[]{clazz});
        xstream.setClassLoader(clazz.getClassLoader());
        xstream.processAnnotations(clazz); // 如果类上有注解
        return (T) xstream.fromXML(xml);
    }

    /**
     * 将Java对象转换为XML字符串
     */
    public static String toXML(Object obj) {
        Class<?> clazz = obj.getClass();
        xstream.allowTypes(new Class[]{clazz});
        xstream.processAnnotations(clazz); // 如果类中有注解
        return xstream.toXML(obj);
    }

    public static void main(String[] args) {
        Cpa05Rep rsp = new Cpa05Rep();
        System.out.println(toXML(rsp));
    }

}
