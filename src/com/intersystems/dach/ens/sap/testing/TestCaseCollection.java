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
                                                "<xs:schema attributeFormDefault=\"unqualified\" elementFormDefault=\"qualified\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"><xs:element name=\"Z_BRUSHES\"><xs:complexType><xs:sequence><xs:element name=\"BRUSH\" maxOccurs=\"unbounded\" minOccurs=\"0\"><xs:complexType><xs:sequence><xs:element type=\"xs:string\" name=\"NAME\"/><xs:element type=\"xs:string\" name=\"VALUE\"/></xs:sequence></xs:complexType></xs:element></xs:sequence></xs:complexType></xs:element></xs:schema>",
                                                true),
                                700);

                TestCase tc2 = new TestCase(
                                new SAPImportData(
                                                "Z_COLORS",
                                                "<?xml version=\"1.0\" ?>\r\n<Z_COLOR><COLOR><NAME>red</NAME><VALUE>#f00</VALUE></COLOR><COLOR><NAME>green</NAME><VALUE>#0f0</VALUE></COLOR></Z_COLOR>",
                                                false,
                                                "<xs:schema attributeFormDefault=\"unqualified\" elementFormDefault=\"qualified\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"><xs:element name=\"Z_COLOR\"><xs:complexType><xs:sequence><xs:element name=\"COLOR\" maxOccurs=\"unbounded\" minOccurs=\"0\"><xs:complexType><xs:sequence><xs:element type=\"xs:string\" name=\"NAME\"/><xs:element type=\"xs:string\" name=\"VALUE\"/></xs:sequence></xs:complexType></xs:element></xs:sequence></xs:complexType></xs:element></xs:schema>",
                                                false),
                                1500);

                TestCase tc3 = new TestCase(
                                new SAPImportData(
                                                "Z_ISH_DISPO_INFO",
                                                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?><Z_ISH_PATIENT_VERSAND_TEST_CASE xmlns=\"urn:isc:rfc\"><IS_PATIENT_DATA><CLIENT>300</CLIENT><PATIENTID>0055112093</PATIENTID><CHKDIGITPAT>7</CHKDIGITPAT><EXT_PAT_ID/><INSTITUTION>2240</INSTITUTION><INST_STEXT/><LAST_NAME_PAT>testpatient_AK</LAST_NAME_PAT><STD_LNAME_PAT>TESTPATIENT_AK</STD_LNAME_PAT><FRST_NAME_PAT>hhbstump_AK</FRST_NAME_PAT><STD_FNAME_PAT>HHBSTUMP_AK</STD_FNAME_PAT><BIRTH_NAME/><STD_BNAME_PAT/><DOB>1960-01-01</DOB><BIRTHCTRY/><BIRTHCTRY_ISO/><BIRTHCTRY_TEXT/><BIRTHPLACE/><SEX>2</SEX><SEX_EXT>W</SEX_EXT><SEX_STEXT>weiblich</SEX_STEXT><PSEUDO/><FORM_ADDRS>02</FORM_ADDRS><FORM_ADDRS_STXT>Frau</FORM_ADDRS_STXT><FORM_ADDRS_TXT>Frau</FORM_ADDRS_TXT><TITLE/><AFFIX/><PREFIX/><FULL_NAME_PAT>testpatient_AK, hhbstump_AK</FULL_NAME_PAT><PAT_ADDR_NO/><PAT_ADDR2_NO/><MAR_STAT/><MAR_STAT_TEXT/><MAR_STAT_STEXT/><REL_DENOM/><REL_DENOM_STXT/><REL_DENOM_TEXT/><NATIONALITY>CH</NATIONALITY><NATIONALITY_ISO>CH</NATIONALITY_ISO><NATION_TEXT>schweizerisch</NATION_TEXT><PAT_LANGU>D</PAT_LANGU><PAT_LANGU_ISO>DE</PAT_LANGU_ISO><LANGU_TEXT>Deutsch</LANGU_TEXT><ORGAN_DONOR/><VIP/><EXPIRED/><DT_O_DEATH_FROM>0000-00-00</DT_O_DEATH_FROM><TM_O_DEATH_FROM>00:00:00</TM_O_DEATH_FROM><DT_O_DEATH_TO>0000-00-00</DT_O_DEATH_TO><TM_O_DEATH_TO>00:00:00</TM_O_DEATH_TO><C_OF_DEATH/><C_OF_DEATHTXT/><EMERG_ADM/><QUICK_ADM/><NON_RESIDENT/><INACTIVE/><PUBLISTBLOCK/><RELIG_LIST/><PARISH/><PARISH_DISTRICT/><PARISH_TEXT/><RACE/><RACE_TEXT/><SSN/><DOC_TYPE/><DOC_TYPE_TEXT/><DOC_NO/><OCCUPATION/><EMPLOYERID/><EMP_NAME/><EMP_ADDR_NO/><FAM_PHYS/><REF_PHYS/><REF_PHYS2/><USER1/><USER2/><USER3>0000-00-00</USER3><USER4>0000-00-00</USER4><USER5/><USER6/><CREATION_DATE>2023-03-28</CREATION_DATE><CREATION_USER>HHBSTUMP</CREATION_USER><UPDATE_DATE>0000-00-00</UPDATE_DATE><UPDATE_USER>HHBSTUMP</UPDATE_USER><CANCEL_IND/><CANCEL_USER/><CANCEL_DATE>0000-00-00</CANCEL_DATE><EMPLOYED_SINCE>0000-00-00</EMPLOYED_SINCE><UNKNOWN_DOB/><ISTAT_BIRTHPL/><TAXNUMBER/><TAXNUMBER_INDIC/><STPCODE/><STPCODE_EXPIRY>0000-00-00</STPCODE_EXPIRY><CONSENT_PERSDATA/><BIRTH_RANK>0</BIRTH_RANK><FLAG_EXT_ORDERER/><DTH_LOC/><POST_DIS_PHYS/><POST_DIS_PHYS_OUTP/><LAST_NAME_PAT_LONG/><STD_LNAME_PAT_LONG/><FRST_NAME_PAT_LONG/><STD_FNAME_PAT_LONG/><BIRTH_NAME_LONG/><STD_BNAME_PAT_LONG/><TITLE_ACA2/><TITLE_ACA2TXT/><SEX_SPECIALIZATION/><SEX_SPECIALIZATION_TXT/><PARTNER>0055112093</PARTNER><PERSNUMBER>0008100817</PERSNUMBER><ADDRESS/><TITLE_KEY>0001</TITLE_KEY><TITLE_TEXT>Frau</TITLE_TEXT><FULL_NAME_STRING>Frau hhbstump_AK testpatient_AK</FULL_NAME_STRING><CASE_OTH_INST/><CORRESPONDLANGUAGEISO>DE</CORRESPONDLANGUAGEISO><PROFORMA_X/><BLACK_LIST/><BLACK_LIST_OTH_INST/><BLACK_LIST_COMMENT/><PUBLIC_BLACK_LIST/><PUBLIC_BLACK_LIST_COMMENT/><EMP_CITY/><EMP_STREET/><EMP_HOUSE_NO/><EMP_POSTL_COD1/><INACTIVE_ALLOWED/><PROTECTED_PAT/><ZZ_OPPID/><ZZISO/><ETAG/><PSEUDO_PAT/><LOG_PATIENT/><EPDKZ/></IS_PATIENT_DATA><IT_ADDRESS><item><ADDRNUMBER>0008100818</ADDRNUMBER><PARTNER>0055112093</PARTNER><PERSNUMBER>0008100817</PERSNUMBER><PATIENTID>0055112093</PATIENTID><INSTITUTION/><ADDRESS><STANDARDADDRESS>X</STANDARDADDRESS><C_O_NAME/><CITY>Zürich</CITY><DISTRICT/><REGIOGROUP/><POSTL_COD1>8000</POSTL_COD1><POSTL_COD2/><POSTL_COD3/><PCODE1_EXT/><PCODE2_EXT/><PCODE3_EXT/><PO_BOX/><PO_W_O_NO/><PO_BOX_CIT/><PO_BOX_REG/><POBOX_CTRY/><PO_CTRYISO/><STREET>Teststrasse 1</STREET><STR_ABBR/><HOUSE_NO/><HOUSE_NO2/><HOUSE_NO3/><STR_SUPPL1/><STR_SUPPL2/><STR_SUPPL3/><LOCATION/><BUILDING/><FLOOR/><ROOM_NO/><COUNTRY>CH</COUNTRY><COUNTRYISO>CH</COUNTRYISO><REGION>ZH</REGION><TIME_ZONE>CET</TIME_ZONE><TAXJURCODE/><HOME_CITY/><TRANSPZONE/><LANGU>D</LANGU><LANGUISO>DE</LANGUISO><COMM_TYPE/><EXTADDRESSNUMBER/><DONT_USE_P/><DONT_USE_S/><MOVE_DATE>0000-00-00</MOVE_DATE><MOVE_ADDRESS/><VALIDFROMDATE>2023-03-28</VALIDFROMDATE><VALIDTODATE>9999-12-31</VALIDTODATE><MOVE_ADDR_GUID/><CITY_NO/><DISTRCT_NO/><CHCKSTATUS/><PBOXCIT_NO/><STREET_NO/><HOMECITYNO/><PO_BOX_LOBBY/><DELI_SERV_TYPE/><DELI_SERV_NUMBER/><COUNTY/><COUNTY_NO/><TOWNSHIP/><TOWNSHIP_NO/></ADDRESS><ADDRESSTYPE>XXDEFAULT</ADDRESSTYPE><ADDRESSTYPE_TEXT>Standardadresse</ADDRESSTYPE_TEXT><STANDARDADDRESSUSAGE/><USAGEVALIDFROM>2023-03-28</USAGEVALIDFROM><USAGEVALIDTO>9999-12-31</USAGEVALIDTO><VALIDTOREADFORCHANGE>0000-00-00</VALIDTOREADFORCHANGE><E_MAIL_G/><E_MAIL_P/><TELEPHONE_P_COUNTRY/><TELEPHONE_P/><MOBILE_COUNTRY/><MOBILE/><TELEPHONE_G_COUNTRY/><TELEPHONE_G/><NO_MAIL/><FAVNUMBRKIND/><FAVNUMBRTXT/><GEOG_AREA/><LFDNR>000</LFDNR></item></IT_ADDRESS><IT_CONTACT/><IT_FAX/><IT_MAIL/><IT_PAGER/><IT_PHONE/><IT_RISK/><IV_INSTITUTION>2240</IV_INSTITUTION><IV_LANGUAGE>HIT2020DE</IV_LANGUAGE><SAP_EVENT>NP0100</SAP_EVENT><SENDER>DEVCLNT300</SENDER><SEQNR>0002731677</SEQNR><TIMESTAMP>20230328110051.0000000</TIMESTAMP></Z_ISH_PATIENT_VERSAND_TEST_CASE>",
                                                false),
                                5000);

                testCases.add(tc1);
                testCases.add(tc2);
                testCases.add(tc3);

                return testCases;
        }

}
