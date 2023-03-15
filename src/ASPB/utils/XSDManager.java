package ASPB.utils;

import java.io.File;
import java.io.FileWriter;
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
import java.nio.file.Paths;
import java.util.Hashtable;
import java.util.Map;

import com.intersystems.jdbc.IRIS;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoMetaData;
import com.sap.conn.jco.JCoParameterList;
import com.sap.conn.jco.JCoStructure;
import com.sap.conn.jco.JCoTable;

/**
 * This class is used to create an XSD document from a JCoFunction.
 * 
 * @author Philipp Bonin
 * @version 1.0
 * 
 */
public abstract class XSDManager {

    public static String XSD_DIRECTORY = "";

    public static boolean AUTO_IMPORT = false;

    private static Map<String, String> availableXSDs = new Hashtable<String, String>();

    /**
     * Writes the XSD to a file.
     * 
     * @param xml
     * @param name
     * @throws IOException
     */
    private static void writeXSDtoFile(String xml, String name) throws IOException {
        File file = new File(Paths.get(XSD_DIRECTORY, name + ".xsd").toString());
        FileWriter writer = new FileWriter(file);
        writer.write(xml);
        writer.close();
    }

    /**
     * Imports all XSD files from the XSD_DIRECTORY to the availableXSDs map.
     */
    public static void importXSDFiles() {

        File folder = new File(XSD_DIRECTORY);
        File[] listOfFiles = folder.listFiles();

        // read all xsd files and save them in a map
        for (File file : listOfFiles) {
            if (file.isFile() && file.getName().endsWith(".xsd")) {
                try {
                    availableXSDs.put(file.getName().replace(".xsd", ""),
                            new String(java.nio.file.Files.readAllBytes(file.toPath())));
                } catch (IOException e) {
                    Logger.log("Error while reading XSD file " + file.getName() + ": " + e.getMessage());
                }
            }
        }

    }

    /**
     * Creates an XSD Schema from a JCoFunction.
     * 
     * @param function        The JCoFunction to convert
     * @param importParameter If true, the import parameter list will be used,
     *                        otherwise the export
     * @throws Exception If the XSD could not be created
     */
    public static void createXSD(JCoFunction function, boolean importParameter) throws Exception {
        String xsd = createXSDString(function, importParameter);

        // check if xsd already exists
        if (availableXSDs.containsKey(function.getName())) {
            // update xsd
            if (!availableXSDs.get(function.getName()).equals(xsd)) {
                writeXSDtoFile(xsd, function.getName());
                availableXSDs.put(function.getName(), xsd);
                Logger.log("XSD " + function.getName() + " updated");
                importXSDtoIRIS(function.getName());
            }
        } else {
            // create new xsd
            writeXSDtoFile(xsd, function.getName());
            availableXSDs.put(function.getName(), xsd);
            Logger.log("XSD " + function.getName() + " created");
            importXSDtoIRIS(function.getName());

        }
    }

    /**
     * Imports the XSD to IRIS.
     * 
     * @param name The name of the XSD
     */
    private static void importXSDtoIRIS(String name) {
        if (!AUTO_IMPORT)
            return;

        if (ServiceManager.getInstance() == null) {
            Logger.log("Could not import XSD to IRIS: No Service registered");
            return;
        }

        IRIS iris = ServiceManager.getInstance().getConnection();

        if (iris == null) {
            Logger.log("Could not import XSD to IRIS: No connection to IRIS");
            return;
        }

        iris.classMethodStatusCode("EnsLib.EDI.SchemaXSD", "Import",
                Paths.get(XSD_DIRECTORY, name + ".xsd").toString());
        Logger.log("XSD " + name + " imported to IRIS");
    }

    /**
     * This method converts a JCoFunction to an XSD String.
     * 
     * @param function        The JCoFunction to convert
     * @param importParameter If true, the import parameter list will be used,
     *                        otherwise the export
     * @return The XSD String
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws TransformerException
     */
    public static String createXSDString(JCoFunction function, boolean importParameter)
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
                    element.setAttribute("type", "xs:date");
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
                    element.setAttribute("minOccurs", "0");
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
                    element = doc.createElement("xs:complexType");
                    element.setAttribute("name", name);
                    convertStructure(parameterList.getStructure(name), element, doc, parameterList);
                    break;
                case JCoMetaData.TYPE_TABLE:
                    element = doc.createElement("xs:element");
                    element.setAttribute("name", name);
                    element.setAttribute("minOccurs", "0");
                    element.setAttribute("maxOccurs", "unbounded");
                    Element complexTypeTable = doc.createElement("xs:complexType");
                    convertTable(parameterList.getTable(name), complexTypeTable, doc, parameterList);
                    break;
                default:
                    element = doc.createElement("xs:element");
                    element.setAttribute("type", "xs:string");
                    element.setAttribute("name", name);
                    element.setAttribute("minOccurs", "0");
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

    /**
     * This method converts a JCoFunction to an XSD document.
     * 
     * @param function        The JCoFunction to convert
     * @param importParameter If true, the import parameter list will be used,
     *                        otherwise the export
     * @return The XSD document
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws TransformerException
     */
    public static Document createXSDDocument(JCoFunction function, boolean importParameter)
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
                    element.setAttribute("type", "xs:date");
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
                    element = doc.createElement("xs:complexType");
                    element.setAttribute("name", name);
                    convertStructure(parameterList.getStructure(name), element, doc, parameterList);
                    break;
                case JCoMetaData.TYPE_TABLE:
                    element = doc.createElement("xs:element");
                    element.setAttribute("name", name);
                    element.setAttribute("maxOccurs", "unbounded");
                    element.setAttribute("minOccurs", "0");

                    Element complexTypeTable = doc.createElement("xs:complexType");
                    convertTable(parameterList.getTable(name), complexTypeTable, doc, parameterList);
                    break;
                default:
                    element = doc.createElement("xs:element");
                    element.setAttribute("type", "xs:string");
                    element.setAttribute("name", name);
                    element.setAttribute("minOccurs", "0");

                    break;
            }
            schema.appendChild(element);
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
            String name = structMetadata.getName(i).replace("/", "_-");
            // String description = structMetadata.getDescription(i);
            Element element;
            System.out.println(structMetadata.getType(i));
            switch (structMetadata.getType(i)) {
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
                    element.setAttribute("type", "xs:date");
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
                    length.setAttribute("value", structMetadata.getLength(i) + "");
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
                    lengthChar.setAttribute("value", structMetadata.getLength(i) + "");
                    restrictionChar.appendChild(lengthChar);
                    simpleTypeChar.appendChild(restrictionChar);
                    element.appendChild(simpleTypeChar);
                    break;
                case JCoMetaData.TYPE_STRUCTURE:
                    element = doc.createElement("xs:element");
                    element.setAttribute("name", name);
                    element.setAttribute("minOccurs", "0");

                    Element complexTypeStruct = doc.createElement("xs:complexType");
                    convertStructure(parameterList.getStructure(name), complexTypeStruct, doc, parameterList);
                    break;
                case JCoMetaData.TYPE_TABLE:
                    element = doc.createElement("xs:element");
                    element.setAttribute("name", name);
                    element.setAttribute("minOccurs", "0");

                    element.setAttribute("maxOccurs", "unbounded");
                    Element complexTypeTable = doc.createElement("xs:complexType");
                    convertTable(parameterList.getTable(name), complexTypeTable, doc, parameterList);
                    break;
                default:
                    element = doc.createElement("xs:element");
                    element.setAttribute("type", "xs:string");
                    element.setAttribute("name", name);
                    element.setAttribute("minOccurs", "0");

                    break;

            }
            root.appendChild(element);
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
        root = sequence;

        JCoMetaData structMetadata = table.getMetaData();
        for (int i = 0; i < structMetadata.getFieldCount(); i++) {
            String name = structMetadata.getName(i).replace("/", "_-");
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
