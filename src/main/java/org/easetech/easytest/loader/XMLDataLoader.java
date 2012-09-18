
package org.easetech.easytest.loader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.easetech.easytest._1.Entry;
import org.easetech.easytest._1.InputTestData;
import org.easetech.easytest._1.ObjectFactory;
import org.easetech.easytest._1.TestMethod;
import org.easetech.easytest._1.TestRecord;
import org.easetech.easytest.util.ResourceLoader;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of {@link Loader} for the XML based files. This Loader is responsible for reading a list of XML
 * Files based on the testDataSchema.xsd file of EasyTest and converting them into a data structure which is
 * understandable by the EasyTest framework. The XML data can be provided by the user in the following format :<br>
 * <code>
 * &lt;easytest:InputTestData xmlns:easytest="urn:easetech:easytest:1.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="urn:oclc:merlins:test:Group:1.0 group-reg.xsd"&gt;<br>
 * <B>&lt;TestMethod name="getSimpleData"&gt;</B><br>
 * &nbsp;&nbsp;&lt;TestRecord&gt;<br>
 * &nbsp;&nbsp;&lt;InputData&gt;<br>
 * &nbsp;&nbsp;&nbsp;&lt;Entry key="libraryId" value="91475" /&gt;<br>
 * &nbsp;&nbsp;&nbsp;&lt;Entry key="itemId" value="12" /&gt;<br>
 * &nbsp;&nbsp;&nbsp;&lt;Entry key="itemType" value="book" /&gt;<br>
 * &nbsp;&nbsp;&lt;/InputData&gt;<br>
 * &nbsp;&nbsp;&lt;/TestRecord&gt;<br>
 * &nbsp;&nbsp;&lt;TestRecord&gt;<br>
 * &nbsp;&nbsp;&lt;InputData&gt;<br>
 * &nbsp;&nbsp;&nbsp;&lt;Entry key="libraryId" value="234" /&gt;<br>
 * &nbsp;&nbsp;&nbsp;&lt;Entry key="itemId" value="1452" /&gt;<br>
 * &nbsp;&nbsp;&nbsp;&lt;Entry key="itemType" value="journal" /&gt;<br>
 * &nbsp;&nbsp;&lt;/InputData&gt;<br>
 * &nbsp;&nbsp;&lt;/TestRecord&gt;<br>
 * <B>&lt;/TestMethod&gt;</B><br>
 * <B>&lt;TestMethod name="getAnotherData"&gt;</B><br>
 * &nbsp;&nbsp;&lt;TestRecord&gt;<br>
 * &nbsp;&nbsp;&lt;InputData&gt;<br>
 * &nbsp;&nbsp;&nbsp;&lt;Entry key="picId" value="1111" /&gt;<br>
 * &nbsp;&nbsp;&nbsp;&lt;Entry key="picNum" value="12" /&gt;<br>
 * &nbsp;&nbsp;&nbsp;&lt;Entry key="picFormat" value="jpeg" /&gt;<br>
 * &nbsp;&nbsp;&lt;/InputData&gt;<br>
 * &nbsp;&nbsp;&lt;/TestRecord&gt;<br>
 * &nbsp;&nbsp;&lt;TestRecord&gt;<br>
 * &nbsp;&nbsp;&lt;InputData&gt;<br>
 * &nbsp;&nbsp;&nbsp;&lt;Entry key="picId" value="1561" /&gt;<br>
 * &nbsp;&nbsp;&nbsp;&lt;Entry key="picNum" value="178" /&gt;<br>
 * &nbsp;&nbsp;&nbsp;&lt;Entry key="picFormat" value="raw" /&gt;<br>
 * &nbsp;&nbsp;&lt;/InputData&gt;<br>
 * &nbsp;&nbsp;&lt;/TestRecord&gt;<br>
 * <B>&lt;/TestMethod&gt;</B><br>
 * <B>&lt;/easytest:InputTestData&gt;</B><br><br>
 * 
 * As you can guess, the root element is InputTestData that can have multiple TestMethod elements in it.<br> 
 * Each TestMethod element identifies a method to test with its name attribute.<br>
 * Each TestMethod can have many TestRecords. Each Record identifies data for a single test execution.<br>
 * Each Entry element identifies a method parameter.
 * 
 * @author Anuj Kumar
 * 
 */
public class XMLDataLoader implements Loader {

    /**
     * An instance of logger associated with the test framework.
     */
    protected static final Logger LOG = LoggerFactory.getLogger(XMLDataLoader.class);

    /**
     * Load the data from the specified list of filePaths
     * 
     * @param filePaths the list of File paths
     * @return the data
     */
    @Override
    public Map<String, List<Map<String, Object>>> loadData(String[] filePaths) {
        Map<String, List<Map<String, Object>>> result = new HashMap<String, List<Map<String, Object>>>();
        try {
            result = loadXMLData(Arrays.asList(filePaths));
        } catch (IOException e) {
            Assert.fail("An I/O exception occured while reading the files from the path :" + filePaths.toString());
        }
        return result;
    }

    /**
     * Load the XML data.
     * 
     * @param dataFiles the list of input stream string files to load the data from
     * @return a Map of method name and the list of associated test data with that method name
     * @throws IOException if an IO Exception occurs
     */
    private Map<String, List<Map<String, Object>>> loadXMLData(final List<String> dataFiles) throws IOException {
        Map<String, List<Map<String, Object>>> data = null;
        Map<String, List<Map<String, Object>>> finalData = new HashMap<String, List<Map<String, Object>>>();
        for (String filePath : dataFiles) {
            try {
                ResourceLoader resource = new ResourceLoader(filePath);
                data = load(resource.getInputStream());
            } catch (FileNotFoundException e) {
                LOG.error("The specified file was not found. The path is : {}", filePath);
                LOG.error("Continuing with the loading of next file.");
                continue;
            } catch (IOException e) {
                LOG.error("IO Exception occured while trying to read the data from the file : {}", filePath);
                LOG.error("Continuing with the loading of next file.");
                continue;
            }
            finalData.putAll(data);
        }
        return finalData;

    }

    /**
     * Load the XML data.
     * 
     * @param xmlFile inputStream representation of user provided XML file.
     * @return a Map of method name and the list of associated test data with that method name
     * @throws IOException if an IO Exception occurs
     */
    private Map<String, List<Map<String, Object>>> load(final InputStream xmlFile) throws IOException {
        Map<String, List<Map<String, Object>>> data = new HashMap<String, List<Map<String, Object>>>();
        JAXBContext context = getJAXBContext();
        try {
            if (context != null) {
                Unmarshaller unmarshaller = context.createUnmarshaller();
                InputTestData testData = (InputTestData) unmarshaller.unmarshal(xmlFile);
                convertFromInputTestData(testData, data);
            }
        } catch (JAXBException e) {
            LOG.error("JAXBException occured while trying to unmarshal the data.", e);
            throw new RuntimeException("JAXBException occured while trying to unmarshal the data.", e);
        }

        return data;

    }

    /**
     * Convert the data from {@link InputTestData} to a Map representation as understood by the EasyTest Framework
     * 
     * @param source an instance of {@link InputTestData}
     * @param destination an instance of {@link Map}
     */
    private void convertFromInputTestData(InputTestData source, Map<String, List<Map<String, Object>>> destination) {
        List<TestMethod> testMethods = source.getTestMethod();
        for (TestMethod method : testMethods) {
            List<Map<String, Object>> testMethodData = convertFromLIstOfTestRecords(method.getTestRecord());
            destination.put(method.getName(), testMethodData);

        }
    }

    /**
     * Convert the data from List of {@link TestRecord} to a List of map representation. The LIst of map represents the
     * list of test data for a single test method.
     * 
     * @param dataRecords an instance of List of {@link TestRecord}
     * @return an instance of {@link List} of Map
     */
    private List<Map<String, Object>> convertFromLIstOfTestRecords(List<TestRecord> dataRecords) {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        if (dataRecords != null) {
            for (TestRecord record : dataRecords) {
                Map<String, Object> singleTestData = convertFromListOfEntry(record.getInputData().getEntry());
                result.add(singleTestData);
            }
        }
        return result;
    }

    /**
     * Returns a Map representation of a Single data set for a given method. This data is used to run the test method
     * once.
     * 
     * @param testEntry a list of {@link Entry} objects
     * @return a Map
     */
    Map<String, Object> convertFromListOfEntry(List<Entry> testEntry) {
        Map<String, Object> testData = new HashMap<String, Object>();
        if (testEntry != null) {
            for (Entry entry : testEntry) {
                testData.put(entry.getKey(), entry.getValue());
            }
        }
        return testData;
    }

    /**
     * Get the JAXBContext
     * 
     * @return an instance of {@link JAXBContext}
     */
    private JAXBContext getJAXBContext() {
        JAXBContext context = null;
        try {
            context = JAXBContext.newInstance(ObjectFactory.class);
        } catch (JAXBException e) {
            LOG.error("Error occured while creating JAXB COntext.", e);
            throw new RuntimeException("Error occured while creating JAXB Context.", e);
        }
        return context;
    }

    /**
     * Write Data to the existing XML File.
     * @param filePath the path to the file to which the data needs to be written
     * @param actualData the actual data that needs to be written to the file.
     */
    @Override
    public void writeData(String filePath, Map<String, List<Map<String, Object>>> actualData) {
        //Do nothing

    }

}
