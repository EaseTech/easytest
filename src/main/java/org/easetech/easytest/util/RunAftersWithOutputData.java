
package org.easetech.easytest.util;

import org.junit.AfterClass;

import java.util.ArrayList;
import org.junit.runners.model.MultipleFailureException;

import java.util.List;
import java.util.Map;
import org.easetech.easytest.loader.Loader;
import org.junit.internal.runners.statements.RunAfters;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * An extension of {@link RunAfters} method to write 
 * the test data to the file at the end of executing all the test methods in the test cases.
 * 
 */
public class RunAftersWithOutputData extends RunAfters {

    private final Loader loader;

    private final String[] filePath;

    private final Map<String, List<Map<String, Object>>> writableData;

    private final Statement fNext;

    private final Object fTarget;

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
        super(next, afters, target);
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
        //Write any output test data to the file.
        if (loader != null && filePath.length > 0) {
            loader.writeData(filePath[0], writableData);
        }
    }

}
