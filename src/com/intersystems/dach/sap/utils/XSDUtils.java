package com.intersystems.dach.sap.utils;

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
import java.util.Hashtable;
import java.util.Map;

import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoMetaData;
import com.sap.conn.jco.JCoParameterList;
import com.sap.conn.jco.JCoRecord;
import com.sap.conn.jco.JCoStructure;
import com.sap.conn.jco.JCoTable;

/**
 * This class is used to create an XSD document from a JCoFunction.
 * 
 * @author Philipp Bonin
 * @version 1.0
 * 
 */

public final class XSDUtils {

    // Make this a static class
    private XSDUtils() {
    }

    private static Map<String, String> schemaCache = new Hashtable<String, String>();

    /**
     * This method converts a JCoFunction to an XSD String.
     * 
     * @param function          The JCoFunction to convert
     * @param isImportParameter If true, the import parameter list will be used,
     *                          otherwise the export parameter list
     * @param force             Re-create XSD even if it's already in cache.
     * @return The XSD String
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws TransformerException
     */
    public static String createXSDString(JCoFunction function, boolean isImportParameter, boolean force)
            throws ParserConfigurationException, SAXException, IOException, TransformerException {

        if (!force && schemaCache.containsKey(function.getName())) {
            return schemaCache.get(function.getName());
        }

        Document doc = createXSDDocument(function, isImportParameter);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        StringWriter stringWriter = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(stringWriter));
        String xsdString = stringWriter.toString();
        schemaCache.put(function.getName(), xsdString);
        return xsdString;
    }

    /**
     * This method converts a JCoFunction to an XSD document.
     * 
     * @param function          The JCoFunction to convert
     * @param isImportParameter If true, the import parameter list will be used,
     *                          otherwise the export parameter list
     * @return The XSD document
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws TransformerException
     */
    public static Document createXSDDocument(JCoFunction function, boolean isImportParameter)
            throws ParserConfigurationException, SAXException, IOException, TransformerException {

        final String namespace = "xmlns:xs";
        // create the XSD document
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document doc = dbf.newDocumentBuilder().newDocument();

        // create the schema element
        Element schema = doc.createElementNS(XMLConstants.W3C_XML_SCHEMA_NS_URI,
                "xs:schema");
        schema.setAttribute(namespace, XMLConstants.W3C_XML_SCHEMA_NS_URI);
        schema.setAttribute("attributeFormDefault", "unqualified");
        schema.setAttribute("elementFormDefault", "qualified");
        schema.setAttribute("targetNamespace", XMLUtils.getXmlnamespace());
        doc.appendChild(schema);

        // create the root element
        Element functionName = doc.createElement("xs:element");
        functionName.setAttribute("name", function.getName());
        schema.appendChild(functionName);
        Element complexType = doc.createElement("xs:complexType");
        functionName.appendChild(complexType);
        Element sequence = doc.createElement("xs:sequence");
        complexType.appendChild(sequence);

        JCoParameterList parameterList = (isImportParameter ? function
                .getImportParameterList() : function.getExportParameterList());

        JCoMetaData metadata = parameterList.getMetaData();

        for (int i = 0; i < metadata.getFieldCount(); i++) {
            sequence.appendChild(convertElement(i, metadata, doc, parameterList, null));
        }

        return doc;
    }

    /**
     * This method converts a JCoStructure to an XSD document.
     * 
     * @param structure     The JCoStructure to convert
     * @param root          The root element of the XSD document
     * @param doc           The XSD document
     * @param parameterList The parameter list of the JCoFunction
     */
    private static void convertStructure(JCoStructure structure, Element root, Document doc,
            JCoParameterList parameterList) {
        // add sequence
        Element sequence = doc.createElement("xs:sequence");
        root.appendChild(sequence);
        root = sequence;

        JCoMetaData structMetadata = structure.getMetaData();
        for (int i = 0; i < structMetadata.getFieldCount(); i++) {
            root.appendChild(convertElement(i, structMetadata, doc, parameterList, structure));
        }
    }

    /**
     * This method converts a JCoTable to an XSD document.
     * 
     * @param table         The JCoTable to convert
     * @param root          The root element of the XSD document
     * @param doc           The XSD document
     * @param parameterList The parameter list of the JCoFunction
     */
    private static void convertTable(JCoTable table, Element root, Document doc,
            JCoParameterList parameterList) {

        // add sequence
        Element sequence = doc.createElement("xs:sequence");
        root.appendChild(sequence);

        // add item element
        Element itemElement = doc.createElement("xs:element");
        itemElement.setAttribute("name", "item");
        itemElement.setAttribute("maxOccurs", "unbounded");
        itemElement.setAttribute("minOccurs", "0");
        sequence.appendChild(itemElement);

        // complex type for item element
        Element complexTypeItem = doc.createElement("xs:complexType");
        itemElement.appendChild(complexTypeItem);

        // sequence for item element
        Element sequenceItem = doc.createElement("xs:sequence");
        complexTypeItem.appendChild(sequenceItem);

        if (table.isEmpty()) {
            return;
        }

        root = sequenceItem;

        JCoMetaData tableMetadata = table.getMetaData();
        for (int i = 0; i < tableMetadata.getFieldCount(); i++) {
            root.appendChild(convertElement(i, tableMetadata, doc, parameterList, table));
        }
    }

    /**
     * This method converts a JCoMetaData element to an XSD element.
     * 
     * @param i             The index of the element
     * @param metadata      The JCoMetaData
     * @param doc           The XSD document
     * @param parameterList The parameter list of the JCoFunction
     * @return The XSD element
     */
    private static Element convertElement(int i, JCoMetaData metadata, Document doc, JCoParameterList parameterList,
            JCoRecord parent) {
        String name = metadata.getName(i).replace("/", "_-");

        Element element;

        switch (metadata.getType(i)) {
            case JCoMetaData.TYPE_INT:
                element = doc.createElement("xs:element");
                element.setAttribute("type", "xs:int");
                element.setAttribute("name", name);
                element.setAttribute("minOccurs", "0");

                break;
            case JCoMetaData.TYPE_FLOAT:
                element = doc.createElement("xs:element");
                element.setAttribute("type", "xs:float");
                element.setAttribute("name", name);
                element.setAttribute("minOccurs", "0");

                break;
            case JCoMetaData.TYPE_DATE:
                element = doc.createElement("xs:element");
                // TODO use better type to include 0000-00-00
                element.setAttribute("type", "xs:string");
                element.setAttribute("name", name);
                element.setAttribute("minOccurs", "0");

                break;
            case JCoMetaData.TYPE_TIME:
                element = doc.createElement("xs:element");
                element.setAttribute("type", "xs:time");
                element.setAttribute("name", name);
                element.setAttribute("minOccurs", "0");

                break;
            case JCoMetaData.TYPE_STRING:
                element = doc.createElement("xs:element");
                element.setAttribute("name", name);
                element.setAttribute("minOccurs", "0");

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
                element.setAttribute("minOccurs", "0");

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
                element = doc.createElement("xs:element");
                element.setAttribute("name", name);
                element.setAttribute("minOccurs", "0");

                Element complexTypeStruct = doc.createElement("xs:complexType");

                JCoStructure struct = parent == null ? parameterList.getStructure(name) : parent.getStructure(name);
                convertStructure(struct, complexTypeStruct, doc, parameterList);

                element.appendChild(complexTypeStruct);
                break;
            case JCoMetaData.TYPE_TABLE:
                element = doc.createElement("xs:element");
                element.setAttribute("name", name);
                element.setAttribute("minOccurs", "0");

                Element complexTypeTable = doc.createElement("xs:complexType");

                JCoTable table = parent == null ? parameterList.getTable(name) : parent.getTable(name);
                convertTable(table, complexTypeTable, doc, parameterList);

                element.appendChild(complexTypeTable);
                break;
            default:
                element = doc.createElement("xs:element");
                element.setAttribute("type", "xs:string");
                element.setAttribute("name", name);
                element.setAttribute("minOccurs", "0");

                break;
        }
        return element;
    }

}
