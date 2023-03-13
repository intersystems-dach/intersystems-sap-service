package ASPB.utils;

import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.StringWriter;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoMetaData;
import com.sap.conn.jco.JCoParameterList;
import com.sap.conn.jco.JCoStructure;
import com.sap.conn.jco.JCoTable;

public abstract class XSDConverter {

    public static String createXSD(JCoFunction function, boolean importParameter)
            throws ParserConfigurationException, SAXException, IOException, TransformerException {

        final String namespace = "xmlns:xs";
        // create the XSD document
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document doc = dbf.newDocumentBuilder().newDocument();
        Element schema = doc.createElementNS(XMLConstants.W3C_XML_SCHEMA_NS_URI,
                "xs:schema");
        schema.setAttribute(namespace, XMLConstants.W3C_XML_SCHEMA_NS_URI);
        doc.appendChild(schema);

        JCoParameterList parameterList;
        if (importParameter)
            parameterList = function.getImportParameterList();
        else
            parameterList = function.getExportParameterList();

        JCoMetaData metadata = parameterList.getMetaData();

        for (int i = 0; i < metadata.getFieldCount(); i++) {
            String name = metadata.getName(i).replace("/", "u");

            Element element;

            switch (metadata.getType(i)) {
                case JCoMetaData.TYPE_INT:
                    element = doc.createElement("xs:element");
                    element.setAttribute("type", "xs:int");
                    element.setAttribute("name", name);
                    break;
                case JCoMetaData.TYPE_FLOAT:
                    element = doc.createElement("xs:element");
                    element.setAttribute("type", "xs:float");
                    element.setAttribute("name", name);
                    break;
                case JCoMetaData.TYPE_DATE:
                    element = doc.createElement("xs:element");
                    element.setAttribute("type", "xs:date");
                    element.setAttribute("name", name);
                    break;
                case JCoMetaData.TYPE_TIME:
                    element = doc.createElement("xs:element");
                    element.setAttribute("type", "xs:time");
                    element.setAttribute("name", name);
                    break;
                case JCoMetaData.TYPE_STRING:
                    element = doc.createElement("xs:element");
                    element.setAttribute("name", name);
                    Element simpleType = doc.createElement("xs:simpleType");
                    Element restriction = doc.createElement("xs:restriction");
                    restriction.setAttribute("base", "xs:string");
                    Element length = doc.createElement("xs:maxLength");
                    length.setAttribute("value", metadata.getLength(i) + "");
                    restriction.appendChild(length);
                    simpleType.appendChild(restriction);
                    element.appendChild(simpleType);
                    break;
                case JCoMetaData.TYPE_CHAR:
                    element = doc.createElement("xs:element");
                    element.setAttribute("name", name);
                    Element simpleTypeChar = doc.createElement("xs:simpleType");
                    Element restrictionChar = doc.createElement("xs:restriction");
                    restrictionChar.setAttribute("base", "xs:string");
                    Element lengthChar = doc.createElement("xs:maxLength");
                    lengthChar.setAttribute("value", metadata.getLength(i) + "");
                    restrictionChar.appendChild(lengthChar);
                    simpleTypeChar.appendChild(restrictionChar);
                    element.appendChild(simpleTypeChar);
                    break;
                case JCoMetaData.TYPE_STRUCTURE:
                    element = doc.createElement("xs:complexType");
                    element.setAttribute("name", name);
                    convertStructure(parameterList.getStructure(name), element, doc, parameterList);
                    break;
                case JCoMetaData.TYPE_TABLE:
                    element = doc.createElement("xs:element");
                    element.setAttribute("name", name);
                    element.setAttribute("maxOccurs", "unbounded");
                    Element complexTypeTable = doc.createElement("xs:complexType");
                    convertTable(parameterList.getTable(name), complexTypeTable, doc, parameterList);
                    break;
                default:
                    element = doc.createElement("xs:element");
                    element.setAttribute("type", "xs:string");
                    element.setAttribute("name", name);
                    break;
            }
            schema.appendChild(element);
        }

        // Validate the XSD document against the XSD schema to ensure it is well-formed
        /*
         * SchemaFactory schemaFactory =
         * SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
         * Schema newSchema = schemaFactory.newSchema();
         * Validator validator = newSchema.newValidator();
         * validator.validate(new DOMSource(doc));
         */

        // Transform the document to a string and return it
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        StringWriter stringWriter = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(stringWriter));
        return stringWriter.toString();
    }

    private static void convertStructure(JCoStructure structure, Element root, Document doc,
            JCoParameterList parameterList) {

        // add sequence
        Element sequence = doc.createElement("xs:sequence");
        root.appendChild(sequence);
        root = sequence;

        JCoMetaData structMetadata = structure.getMetaData();
        for (int i = 0; i < structMetadata.getFieldCount(); i++) {
            String name = structMetadata.getName(i).replace("/", "u");
            // String description = structMetadata.getDescription(i);
            Element element;
            System.out.println(structMetadata.getType(i));
            switch (structMetadata.getType(i)) {
                case JCoMetaData.TYPE_INT:
                    element = doc.createElement("xs:element");
                    element.setAttribute("type", "xs:int");
                    element.setAttribute("name", name);
                    break;
                case JCoMetaData.TYPE_FLOAT:
                    element = doc.createElement("xs:element");
                    element.setAttribute("type", "xs:float");
                    element.setAttribute("name", name);
                    break;
                case JCoMetaData.TYPE_DATE:
                    element = doc.createElement("xs:element");
                    element.setAttribute("type", "xs:date");
                    element.setAttribute("name", name);
                    break;
                case JCoMetaData.TYPE_TIME:
                    element = doc.createElement("xs:element");
                    element.setAttribute("type", "xs:time");
                    element.setAttribute("name", name);
                    break;
                case JCoMetaData.TYPE_STRING:
                    element = doc.createElement("xs:element");
                    element.setAttribute("name", name);
                    Element simpleType = doc.createElement("xs:simpleType");
                    Element restriction = doc.createElement("xs:restriction");
                    restriction.setAttribute("base", "xs:string");
                    Element length = doc.createElement("xs:maxLength");
                    length.setAttribute("value", structMetadata.getLength(i) + "");
                    restriction.appendChild(length);
                    simpleType.appendChild(restriction);
                    element.appendChild(simpleType);
                    break;
                case JCoMetaData.TYPE_CHAR:
                    element = doc.createElement("xs:element");
                    element.setAttribute("name", name);
                    Element simpleTypeChar = doc.createElement("xs:simpleType");
                    Element restrictionChar = doc.createElement("xs:restriction");
                    restrictionChar.setAttribute("base", "xs:string");
                    Element lengthChar = doc.createElement("xs:maxLength");
                    lengthChar.setAttribute("value", structMetadata.getLength(i) + "");
                    restrictionChar.appendChild(lengthChar);
                    simpleTypeChar.appendChild(restrictionChar);
                    element.appendChild(simpleTypeChar);
                    break;
                case JCoMetaData.TYPE_STRUCTURE:
                    element = doc.createElement("xs:element");
                    element.setAttribute("name", name);
                    Element complexTypeStruct = doc.createElement("xs:complexType");
                    convertStructure(parameterList.getStructure(name), complexTypeStruct, doc, parameterList);
                    break;
                case JCoMetaData.TYPE_TABLE:
                    element = doc.createElement("xs:element");
                    element.setAttribute("name", name);
                    element.setAttribute("maxOccurs", "unbounded");
                    Element complexTypeTable = doc.createElement("xs:complexType");
                    convertTable(parameterList.getTable(name), complexTypeTable, doc, parameterList);
                    break;
                default:
                    element = doc.createElement("xs:element");
                    element.setAttribute("type", "xs:string");
                    element.setAttribute("name", name);
                    break;

            }
            root.appendChild(element);
        }
    }

    public static void convertTable(JCoTable table, Element root, Document doc,
            JCoParameterList parameterList) {

        // add sequence
        Element sequence = doc.createElement("xs:sequence");
        root.appendChild(sequence);
        root = sequence;

        JCoMetaData structMetadata = table.getMetaData();
        for (int i = 0; i < structMetadata.getFieldCount(); i++) {
            String name = structMetadata.getName(i).replace("/", "u");
            // String description = structMetadata.getDescription(i);
            Element element;
            switch (structMetadata.getType(i)) {
                case JCoMetaData.TYPE_INT:
                    element = doc.createElement("xs:element");
                    element.setAttribute("type", "xs:int");
                    element.setAttribute("name", name);
                    break;
                case JCoMetaData.TYPE_FLOAT:
                    element = doc.createElement("xs:element");
                    element.setAttribute("type", "xs:float");
                    element.setAttribute("name", name);
                    break;
                case JCoMetaData.TYPE_DATE:
                    element = doc.createElement("xs:element");
                    element.setAttribute("type", "xs:date");
                    element.setAttribute("name", name);
                    break;
                case JCoMetaData.TYPE_TIME:
                    element = doc.createElement("xs:element");
                    element.setAttribute("type", "xs:time");
                    element.setAttribute("name", name);
                    break;
                case JCoMetaData.TYPE_STRING:
                    element = doc.createElement("xs:element");
                    element.setAttribute("name", name);
                    Element simpleType = doc.createElement("xs:simpleType");
                    Element restriction = doc.createElement("xs:restriction");
                    restriction.setAttribute("base", "xs:string");
                    Element length = doc.createElement("xs:maxLength");
                    length.setAttribute("value", structMetadata.getLength(i) + "");
                    restriction.appendChild(length);
                    simpleType.appendChild(restriction);
                    element.appendChild(simpleType);
                    break;
                case JCoMetaData.TYPE_CHAR:
                    element = doc.createElement("xs:element");
                    element.setAttribute("name", name);
                    Element simpleTypeChar = doc.createElement("xs:simpleType");
                    Element restrictionChar = doc.createElement("xs:restriction");
                    restrictionChar.setAttribute("base", "xs:string");
                    Element lengthChar = doc.createElement("xs:maxLength");
                    lengthChar.setAttribute("value", structMetadata.getLength(i) + "");
                    restrictionChar.appendChild(lengthChar);
                    simpleTypeChar.appendChild(restrictionChar);
                    element.appendChild(simpleTypeChar);
                    break;
                case JCoMetaData.TYPE_STRUCTURE:
                    element = doc.createElement("xs:element");
                    element.setAttribute("name", name);
                    Element complexTypeStruct = doc.createElement("xs:complexType");
                    convertStructure(parameterList.getStructure(name), complexTypeStruct, doc, parameterList);
                    break;
                case JCoMetaData.TYPE_TABLE:
                    element = doc.createElement("xs:element");
                    element.setAttribute("name", name);
                    element.setAttribute("maxOccurs", "unbounded");
                    Element complexTypeTable = doc.createElement("xs:complexType");
                    convertTable(parameterList.getTable(name), complexTypeTable, doc, parameterList);
                    break;
                default:
                    element = doc.createElement("xs:element");
                    element.setAttribute("type", "xs:string");
                    element.setAttribute("name", name);
                    break;

            }
            root.appendChild(element);
        }
    }

}
