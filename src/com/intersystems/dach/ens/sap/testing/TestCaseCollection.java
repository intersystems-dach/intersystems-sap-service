package com.intersystems.dach.ens.sap.testing;

import java.util.ArrayList;
import java.util.Collection;

import com.intersystems.dach.sap.SAPImportData;

/**
 * This class holds the test cases for the TestRunner
 * 
 * @author Andreas Schütz
 * @version 1.0
 */
public final class TestCaseCollection {

    // Make this class static
    private TestCaseCollection() {
    }

    /**
     * @return A collection of JSON test cases for the SAPService.
     */
    public static Collection<TestCase> getJSONTestCases() {
        Collection<TestCase> testCases = new ArrayList<TestCase>();

        TestCase tc1 = new TestCase(
                new SAPImportData(
                        "Z_FUNC1",
                        "[ { color: 'red', value: '#f00'},{ color: 'green', value: '#0f0'}]",
                        true),
                2000);

        TestCase tc2 = new TestCase(
                new SAPImportData(
                        "Z_FUNC1",
                        "[ { color: 'blue', value: '#00f'},{ color: 'magenta', value: '#f0f'}]",
                        true),
                3000);

        testCases.add(tc1);
        testCases.add(tc2);
        testCases.add(tc1);
        testCases.add(tc1);
        testCases.add(tc1);
        testCases.add(tc2);

        return testCases;
    }

    /**
     * @return collection of XML test cases for the SAPService.
     */
    public static Collection<TestCase> getXMLTestCases() {
        Collection<TestCase> testCases = new ArrayList<TestCase>();

        TestCase tc1 = new TestCase(
                new SAPImportData(
                        "Z_BRUSHES",
                        "<?xml version=\"1.0\" ?>\r\n<Z_BRUSHES><BRUSH><NAME>red</NAME><VALUE>#f00</VALUE></BRUSH><BRUSH><NAME>green</NAME><VALUE>#0f0</VALUE></BRUSH></Z_BRUSHES>",
                        false,
                        "<xs:schema attributeFormDefault=\"unqualified\" elementFormDefault=\"qualified\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"><xs:element name=\"Z_BRUSHES\"><xs:complexType><xs:sequence><xs:element name=\"BRUSH\" maxOccurs=\"unbounded\" minOccurs=\"0\"><xs:complexType><xs:sequence><xs:element type=\"xs:string\" name=\"NAME\"/><xs:element type=\"xs:string\" name=\"VALUE\"/></xs:sequence></xs:complexType></xs:element></xs:sequence></xs:complexType></xs:element></xs:schema>"),
                700);

        TestCase tc2 = new TestCase(
                new SAPImportData(
                        "Z_COLORS",
                        "<?xml version=\"1.0\" ?>\r\n<Z_COLOR><COLOR><NAME>red</NAME><VALUE>#f00</VALUE></COLOR><COLOR><NAME>green</NAME><VALUE>#0f0</VALUE></COLOR></Z_COLOR>",
                        false,
                        "<xs:schema attributeFormDefault=\"unqualified\" elementFormDefault=\"qualified\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"><xs:element name=\"Z_COLOR\"><xs:complexType><xs:sequence><xs:element name=\"COLOR\" maxOccurs=\"unbounded\" minOccurs=\"0\"><xs:complexType><xs:sequence><xs:element type=\"xs:string\" name=\"NAME\"/><xs:element type=\"xs:string\" name=\"VALUE\"/></xs:sequence></xs:complexType></xs:element></xs:sequence></xs:complexType></xs:element></xs:schema>"),
                1500);

        TestCase tc3 = new TestCase(
                new SAPImportData(
                        "Z_ISH_DISPO_INFO",
                        "<?xml version=\"1.0\" ?>\r\n<Z_ISH_DISPO_INFO xmlns=\"urn:isc:rfc\"><IS_CASE_DATA><PATIENTID>7</PATIENTID></IS_CASE_DATA></Z_ISH_DISPO_INFO>",
                        false,
                        ""),
                5000);

        testCases.add(tc1);
        testCases.add(tc1);
        testCases.add(tc1);
        testCases.add(tc1);
        testCases.add(tc1);
        testCases.add(tc2);
        testCases.add(tc1);
        testCases.add(tc2);
        testCases.add(tc1);
        testCases.add(tc2);
        testCases.add(tc3);

        return testCases;
    }

}
