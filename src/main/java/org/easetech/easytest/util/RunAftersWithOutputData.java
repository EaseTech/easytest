
package org.easetech.easytest.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.easetech.easytest.loader.Loader;
import org.junit.AfterClass;
import org.junit.internal.runners.statements.RunAfters;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.MultipleFailureException;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An extension of {@link RunAfters} method to write 
 * the test data to the file at the end of executing all the test methods in the test cases.
 * 
 */
public class RunAftersWithOutputData extends Statement {

    /**
     * An instance of logger associated with the test framework.
     */
    protected static final Logger LOG = LoggerFactory.getLogger(RunAftersWithOutputData.class);
    
	/**
     * An instance of {@link Loader} responsible for writing the data to the file.
     */
    private final Loader loader;

    /**
     * An array of File paths containing the path to the file to which data needs to be written. 
     * Currently we take the first path in the array and try to write the output data to that file. 
     */
    private final String[] filePath;

    /**
     * The actual data structure that contains both the input as well as output data
     */
    private Map<String, List<Map<String, Object>>> writableData;

    /**
     * An instance of {@link Statement} 
     */
    private final Statement fNext;

    /**
     * The target class on which to invoke the {@link AfterClass} annotated method
     */
    private final Object fTarget;

    /**
     * List of {@link FrameworkMethod} that should be run as part of teh {@link AfterClass} annotation.
     */
    private final List<FrameworkMethod> fAfters;

    /**
     * Construct a new RunAftersWithOutputData
     * 
     * @param next the instance of {@link Statement} object
     * @param afters the list of {@link FrameworkMethod} that needs to be run after all the methods in the test class
     *            have been executed.
     * @param target the target instance of the class. In this case it will always be null since methods with
     *            {@link AfterClass} are always declared as static.
     * @param loader the instance of loader responsible for writing the test data to the output file
     * @param filePath an array of files that contain the input test data
     * @param writableData the writable data that needs to be written to the file.
     */
    public RunAftersWithOutputData(Statement next, List<FrameworkMethod> afters, Object target, Loader loader,
        String[] filePath, Map<String, List<Map<String, Object>>> writableData) {
        super();
        this.fNext = next;
        this.fAfters = afters;
        this.fTarget = target;
        this.loader = loader;
        this.filePath = filePath;
        this.writableData = writableData;
    }

    /**
     * @see {@link RunAfters#evaluate()}
     * @throws Throwable
     */
    @Override
    public void evaluate() throws Throwable {
    	LOG.info("evaluate started");
        List<Throwable> errors = new ArrayList<Throwable>();
        try {
            fNext.evaluate();
        } catch (Throwable e) {
            errors.add(e);
        } finally {
            for (FrameworkMethod each : fAfters)
                try {
                    each.invokeExplosively(fTarget);
                } catch (Throwable e) {
                    errors.add(e);
                }
        }
        MultipleFailureException.assertEmpty(errors);
        // Write any output test data to the file only if there is a write data associated with the test method.
        if (loader != null && filePath.length > 0) {
        	LOG.debug("Loader:"+loader+", filePath:"+filePath[0]);
        	LOG.debug("writableData:"+writableData);
            loader.writeData(filePath[0], writableData);
        }
        LOG.info("evaluate finished");
    }

}
