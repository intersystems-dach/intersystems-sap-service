package com.intersystems.dach.sap.utils;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * A class to parse XML
 * 
 * @author Philipp Bonin
 * @version 1.0
 * 
 */
public final class XMLUtils {

    // make this a static class
    private XMLUtils() {}

    // XML namespace and header
    private static final String XMLNAMESPACE = "urn:isc:rfc";
    private static final String XMLHEADER = "version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"";

    /**
     * Parse the given XML String in a correct format and returns it as String
     * 
     * @param xml          - XML in String format
     * @param functionName - name of the function
     * @return XML in String format
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws TransformerException
     */
    public static String convert(String xml, String functionName)
            throws ParserConfigurationException, SAXException, IOException, TransformerException {
        // get document
        Document doc = convertStringToXMLDocument(xml);

        // rename parent node
        if (!doc.getFirstChild().getNodeName().equals(functionName)) {
            doc.renameNode(doc.getFirstChild(), XMLNAMESPACE, functionName);
        }

        // add header
        doc.insertBefore(doc.createProcessingInstruction("xml", XMLHEADER),
                doc.getFirstChild());

        // convert document to string
        return convertXMLDocumentToString(doc);
    }

    /**
     * Convert String to XML Document
     * 
     * @param xmlString - XML in String format
     * @return XML Document
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    private static Document convertStringToXMLDocument(String xmlString)
            throws ParserConfigurationException, SAXException, IOException {
        // Parser that produces DOM object trees from XML content
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        // API to obtain DOM Document instance
        DocumentBuilder builder = null;
        // Create DocumentBuilder with default configuration
        builder = factory.newDocumentBuilder();

        // Parse the content to Document object
        Document doc = builder.parse(new InputSource(new StringReader(xmlString)));
        return doc;
    }

    /**
     * Convert XML Document to String
     * 
     * @param doc - XML Document
     * @return XML in String format
     * @throws TransformerException
     */
    private static String convertXMLDocumentToString(Document doc) throws TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        String output = writer.getBuffer().toString();
        return output;
    }

}
