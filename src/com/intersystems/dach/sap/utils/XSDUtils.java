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

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.StringWriter;
import java.util.Hashtable;
import java.util.Map;

import com.intersystems.dach.sap.SAPServerArgs;
import com.intersystems.dach.utils.TraceManager;
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

public class XSDUtils {

    private Map<String, XSDSchema> schemaCache;

    private SAPServerArgs sapServerArgs;

    private Map<Document, Boolean> documentStatusMap;

    public XSDUtils(SAPServerArgs sapServerArgs) {
        this.sapServerArgs = sapServerArgs;
        this.schemaCache = new Hashtable<String, XSDSchema>();
        this.documentStatusMap = new Hashtable<Document, Boolean>();

    }

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
    public XSDSchema createXSD(JCoFunction function, boolean isImportParameter, boolean force)
            throws ParserConfigurationException, SAXException, IOException, TransformerException {

        if (!force && schemaCache.containsKey(function.getName())) {
            return schemaCache.get(function.getName());
        }

        Document doc = createXSDDocument(function, isImportParameter);
        this.documentStatusMap.put(doc, true);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        StringWriter stringWriter = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(stringWriter));
        String xsdString = stringWriter.toString();
        XSDSchema xsdSchema = new XSDSchema(xsdString, documentStatusMap.remove(doc));
        schemaCache.put(function.getName(), xsdSchema);
        return xsdSchema;
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
    private Document createXSDDocument(JCoFunction function, boolean isImportParameter)
            throws ParserConfigurationException, SAXException, IOException, TransformerException {

        // create the XSD document
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document doc = dbf.newDocumentBuilder().newDocument();

        // create the schema element
        Element schema = doc.createElementNS(XMLConstants.W3C_XML_SCHEMA_NS_URI,
                "xs:schema");
        schema.setAttribute("xmlns:xs", XMLConstants.W3C_XML_SCHEMA_NS_URI);
        schema.setAttribute("attributeFormDefault", "unqualified");
        schema.setAttribute("elementFormDefault", "qualified");
        schema.setAttribute("targetNamespace", XMLUtils.getXmlnamespace());
        schema.setAttribute("xmlns:tns", XMLUtils.getXmlnamespace());
        doc.appendChild(schema);

        Comment comment = doc.createComment("Generated at " + new java.util.Date().toString());
        doc.getDocumentElement().appendChild(comment);

        // create nullDate type
        Element nullDateType = doc.createElement("xs:simpleType");
        nullDateType.setAttribute("name", "nullDate");
        schema.appendChild(nullDateType);
        Element restriction = doc.createElement("xs:restriction");
        restriction.setAttribute("base", "xs:string");
        nullDateType.appendChild(restriction);
        Element enumeration = doc.createElement("xs:enumeration");
        enumeration.setAttribute("value", "0000-00-00");
        restriction.appendChild(enumeration);

        // create dateOrNullDate type
        Element dateOrNullDateType = doc.createElement("xs:simpleType");
        dateOrNullDateType.setAttribute("name", "dateOrNullDate");
        schema.appendChild(dateOrNullDateType);
        Element union = doc.createElement("xs:union");
        union.setAttribute("memberTypes", "xs:date tns:nullDate");
        dateOrNullDateType.appendChild(union);

        // create the root element
        Element functionName = doc.createElement("xs:element");
        functionName.setAttribute("name", function.getName());
        schema.appendChild(functionName);
        Element complexType = doc.createElement("xs:complexType");
        functionName.appendChild(complexType);
        // TODO use choice?
        // TODO when FlattenTablesItems is false, use sequence instead of all
        Element sequence = doc.createElement("xs:all");
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
    private void convertStructure(JCoStructure structure, Element root, Document doc,
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
    private void convertTable(JCoTable table, Element root, Document doc,
            JCoParameterList parameterList, String name) {

        // add sequence
        Element sequence = doc.createElement("xs:sequence");
        root.appendChild(sequence);
        root = sequence;

        // add item element
        if (!sapServerArgs.isFlattenTablesItems()) {
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

            root = sequenceItem;
        }

        // TODO
        if (table.isEmpty()) {

            Element anyElement = doc.createElement("xs:any");
            anyElement.setAttribute("maxOccurs", "unbounded");
            anyElement.setAttribute("minOccurs", "0");

            anyElement.setAttribute("processContents", "skip");

            root.appendChild(anyElement);
            documentStatusMap.put(doc, false);
            sapServerArgs.getTraceManager().traceMessage("Table " + name + " is empty, generic XSD element is used!");
            return;
        }

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
    private Element convertElement(int i, JCoMetaData metadata, Document doc, JCoParameterList parameterList,
            JCoRecord parent) {
        String name = metadata.getName(i).replace("/", "_-");
        String description = metadata.getDescription(i);

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
                element.setAttribute("type", "tns:dateOrNullDate");
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

                if (metadata.getLength(i) == 0) {
                    Element simpleType = doc.createElement("xs:simpleType");
                    Element restriction = doc.createElement("xs:restriction");
                    restriction.setAttribute("base", "xs:string");
                    Element length = doc.createElement("xs:maxLength");
                    length.setAttribute("value", metadata.getLength(i) + "");
                    restriction.appendChild(length);
                    simpleType.appendChild(restriction);
                    element.appendChild(simpleType);
                } else {
                    description += ";" + name + " has length 0. This is not supported by XSD.";

                    element.setAttribute("type", "xs:string");
                }

                break;
            case JCoMetaData.TYPE_CHAR:
                element = doc.createElement("xs:element");
                element.setAttribute("name", name);
                element.setAttribute("minOccurs", "0");

                if (metadata.getLength(i) != 0) {
                    Element simpleTypeChar = doc.createElement("xs:simpleType");
                    Element restrictionChar = doc.createElement("xs:restriction");
                    restrictionChar.setAttribute("base", "xs:string");
                    Element lengthChar = doc.createElement("xs:maxLength");
                    lengthChar.setAttribute("value", metadata.getLength(i) + "");
                    restrictionChar.appendChild(lengthChar);
                    simpleTypeChar.appendChild(restrictionChar);
                    element.appendChild(simpleTypeChar);
                } else {
                    description += ";" + name + " has length 0. This is not supported by XSD.";

                    element.setAttribute("type", "xs:string");
                }

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

                if (sapServerArgs.isFlattenTablesItems()) {
                    element.setAttribute("maxOccurs", "unbounded");
                }

                Element complexTypeTable = doc.createElement("xs:complexType");

                JCoTable table = parent == null ? parameterList.getTable(name) : parent.getTable(name);
                convertTable(table, complexTypeTable, doc, parameterList, name);

                element.appendChild(complexTypeTable);
                break;
            default:
                element = doc.createElement("xs:element");
                element.setAttribute("type", "xs:string");
                element.setAttribute("name", name);
                element.setAttribute("minOccurs", "0");

                break;
        }

        // TODO add description at a better place
        Comment comment = doc.createComment(name + " : " + description);
        element.appendChild(comment);
        return element;
    }

}
