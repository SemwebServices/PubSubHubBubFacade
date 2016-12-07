package feedfacade;

public class Utils {



  public static String XmlToJson(String xml_document) {
    net.sf.json.xml.XMLSerializer xs = new net.sf.json.xml.XMLSerializer();
    xs.setSkipNamespaces(true);
    xs.setSkipWhitespace(true);
    xs.setTrimSpaces(true);
    xs.setRemoveNamespacePrefixFromElements(true);

    net.sf.json.JSON json_obj = xs.read(xml_document);
    String json_text = json_obj.toString();

    return json_text;
  }
 
}
