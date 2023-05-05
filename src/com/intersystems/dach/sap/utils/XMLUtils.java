package com.intersystems.dach.sap.utils;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.intersystems.dach.sap.SAPServerArgs;

/**
 * A class to parse XML
 */
public class XMLUtils {

    private SAPServerArgs sapServerArgs;

    public XMLUtils(SAPServerArgs sapServerArgs) {
        this.sapServerArgs = sapServerArgs;
    }

    // XML header
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
    public String convert(String xml, String functionName)
            throws ParserConfigurationException, SAXException, IOException, TransformerException {
        // get document
        Document doc = convertStringToXMLDocument(xml);

        // rename parent node
        if (!doc.getFirstChild().getNodeName().equals(functionName)) {
            doc.renameNode(doc.getFirstChild(),
                    sapServerArgs.getConvertedXMLNamespace(functionName),
                    functionName);
        }

        // add header
        doc.insertBefore(doc.createProcessingInstruction("xml", XMLHEADER),
                doc.getFirstChild());

        if (sapServerArgs.isFlattenTablesItems()) {
            List<Node> nodesToRemove = new ArrayList<Node>();
            replaceItem(doc.getDocumentElement(), doc, nodesToRemove);

            // remove nodes
            removeNodes(nodesToRemove);
        }

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
    private Document convertStringToXMLDocument(String xmlString)
            throws ParserConfigurationException, SAXException, IOException {
        // Parser that produces DOM object trees from XML content
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        // API to obtain DOM Document instance
        DocumentBuilder builder = null;
        // Create DocumentBuilder with default configuration
        builder = factory.newDocumentBuilder();

        // Parse the content to Document object
        return builder.parse(new InputSource(new StringReader(xmlString)));
    }

    /**
     * Convert XML Document to String
     * 
     * @param doc - XML Document
     * @return XML in String format
     * @throws TransformerException
     */
    private String convertXMLDocumentToString(Document doc) throws TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.getBuffer().toString();
    }

    /**
     * Replace item with parent node name
     * 
     * @param node        - current node
     * @param doc         - XML Document
     * @param removeNodes - list of nodes to remove later
     */
    private void replaceItem(Node node, Document doc, List<Node> removeNodes) {

        if (node.getNodeName().equals("item")) {
            // rename item to parent node name
            doc.renameNode(node, null, node.getParentNode().getNodeName());
            // add parent node to remove list
            if (!removeNodes.contains(node.getParentNode())) {
                removeNodes.add(node.getParentNode());
            }
        }

        // iterate through child nodes
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node currentNode = nodeList.item(i);
            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                // calls this method for all the children which is Element
                replaceItem(currentNode, doc, removeNodes);
            }
        }
    }

    /**
     * Remove nodes from XML Document
     * 
     * @param nodesToRemove - list of nodes to remove
     */
    private void removeNodes(List<Node> nodesToRemove) {
        if (nodesToRemove.isEmpty()) {
            return;
        }

        List<Node> childs = new ArrayList<Node>();

        // get child of removed nodes
        for (Node node : nodesToRemove) {

            NodeList nodeList = node.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node currentNode = nodeList.item(i);

                if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                    childs.add(currentNode);
                }
            }
        }

        // appent child to new parent
        for (Node node : childs) {
            node.getParentNode().getParentNode().appendChild(node);
        }

        // remove nodes
        for (Node node : nodesToRemove) {
            node.getParentNode().removeChild(node);
        }
    }

}
