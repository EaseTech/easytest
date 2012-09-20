package org.easetech.easytest.runner;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.easetech.easytest.annotation.DataLoader;
import org.easetech.easytest.annotation.Param;
import org.easetech.easytest.loader.DataConverter;
import org.easetech.easytest.loader.Loader;
import org.easetech.easytest.loader.LoaderFactory;
import org.easetech.easytest.loader.LoaderType;
import org.easetech.easytest.util.DataContext;
import org.easetech.easytest.util.RunAftersWithOutputData;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.ParametersSuppliedBy;
import org.junit.experimental.theories.PotentialAssignment;
import org.junit.experimental.theories.PotentialAssignment.CouldNotGenerateValueException;
import org.junit.experimental.theories.internal.Assignments;
import org.junit.experimental.theories.internal.ParameterizedAssertionError;
import org.junit.internal.AssumptionViolatedException;
import org.junit.runner.Runner;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.MultipleFailureException;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of {@link Suite} that encapsulates the {@link EasyTestRunner} in order to provide users with clear
 * indication of which test method is run and what is the input test data that the method is run with. For example, when
 * a user runs the test method with name : <B><I>getTestData</I></B> with the following test data:
 * <ul>
 * <li><B>"libraryId=1 and itemId=2"</B></li>
 * <li><B>"libraryId=2456 and itemId=789"</B></li><br>
 * <br>
 * 
 * then, {@link DataDrivenTest}, will provide the details of the executing test method in the JUnit supported IDEs like
 * this:
 * 
 * <ul>
 * <li><B><I>getTestData{libraryId=1 ,itemId=2}</I></B></li>
 * <li><B><I>getTestData{libraryId=2456 ,itemId=789}</I></B></li></br></br>
 * 
 * This gives user the clear picture of which test was run with which input test data.
 * 
 * For details on the actual Runner implementation, see {@link EasyTestRunner}
 * 
 * @author Anuj Kumar
 * 
 */
public class DataDrivenTest extends Suite {

    /**
     * The list of files that are used by the {@link Loader}s {@link Loader#writeData(String, Map)} 
     * functionality to write the test data back to the file. This is also passed to the {@link RunAftersWithOutputData} method.
     */
    private String[] dataFiles;
    /**
     * The instance of {@link Loader} that is currently being used. 
     * This is also passed to the {@link RunAftersWithOutputData}'s constructor which is responsible 
     * for calling the right {@link Loader#writeData(String, Map)} based on the {@link Loader} instance.
     * 
     */
    private Loader dataLoader = null;
    
    /**
     * An instance of {@link Map} that contains the data to be written to the File
     */
    private static Map<String, List<Map<String, Object>>> writableData = new HashMap<String, List<Map<String, Object>>>();
    
    /**
     * The default rowNum within the {@link #writableData}'s particular method data.
     */
    private static int rowNum = 0;
    
    /**
     * The name of the method currently being executed. Used for populating the {@link #writableData} map.
     */
    private String mapMethodName = "";

    /**
     * An instance of logger associated with the test framework.
     */
    protected static final Logger PARAM_LOG = LoggerFactory.getLogger(DataDrivenTest.class);

    /**
     * A {@link BlockJUnit4ClassRunner} Runner implementation that adds support of input parameters as part of the
     * {@link Test} annotation. This {@link BlockJUnit4ClassRunner} extension is modified for providing convenient Data
     * Driven Testing support to its users. This Runner is capable of generating new instances of
     * {@link FrameworkMethod} based on the test data for a given method. For eg. If there is a method
     * "testMethod(String testData)" that has three sets of test data : [{"testData1"},{"testData2"},{"testData3"}],
     * then this runner will generate three {@link FrameworkMethod} instances with the method names :<br>
     * testMethod{testData1}<br>
     * testMethod{testData2}<br>
     * and<br>
     * testMethod{testData3}<br>
     * <br>
     * 
     * <br>
     * <B> A user can specify the test data at the class level, using the {@link DataLoader} annotation and override it
     * at the method level. The Runner will take care of executing the test method with the right test data.</B><br>
     * This is extremely beneficial in cases, where the user just wants to load the data once and then reuse it for all
     * the test methods. If the user wants, then he can always override the test data at the method level by specifying
     * the {@link DataLoader} annotation at the method level. <br>
     * <br>
     * In addition, this runner also introduces a new way for the user to specify the test data using {@link DataLoader}
     * annotation.
     * 
     * <br>
     * <br>
     * There is also a {@link Param} annotation to handle boiler plate tasks on behalf of the user as well as supports
     * additional functionality that eases the life of the user. For eg. it supports Java PropertyEditors to
     * automatically convert a String to the specified Object. It also supports passing a Map to the test method that
     * contains all the available test data key / value pairs for easy consumption by the user. It also supports user
     * defined custom Objects as parameters.<br>
     * <br>
     * 
     * @author Anuj Kumar
     */
    private class EasyTestRunner extends BlockJUnit4ClassRunner {

        /**
         * The name of the test method for which this Runner instance will generate a set of new {@link FrameworkMethod}
         * s, one for each set of test data.
         */
        private final String methodName;

        /**
         * Convenient class member to get the list of {@link FrameworkMethod} that this runner will execute.
         */
        List<FrameworkMethod> frameworkMethods;

        /**
         * Get the method name
         * 
         * @return the methodName
         */
        @SuppressWarnings("unused")
        public String getMethodName() {
            return methodName;
        }

        /**
         * 
         * Construct a new EasyTestRunner
         * 
         * @param klass the test class whose test methods needs to be executed
         * @throws InitializationError if any error occurs
         */
        public EasyTestRunner(Class<?> klass) throws InitializationError {
            super(klass);
            this.methodName = superMethodName;
        }

        /**
         * Try to collect any initialization errors, if any.
         * 
         * @param errors
         */
        @Override
        protected void collectInitializationErrors(List<Throwable> errors) {
            super.collectInitializationErrors(errors);
            validateDataPointFields(errors);
        }

        /**
         * Override the name of the test. In case of EasyTest, it will be the name of the test method concatenated with
         * the input test data that the method will run with.
         * 
         * @param method the {@link FrameworkMethod}
         * @return an overridden test method Name
         */
        @Override
        protected String testName(final FrameworkMethod method) {
            return String.format("%s", method.getName());
        }

        /**
         * Overridden the compute test method to make it save the method list as class instance, so that the method does
         * not run multiple times. Also, this method now is responsible for creating multiple {@link FrameworkMethod}
         * instances for a given method with multiple test data. So, if a given test method needs to run three times
         * with three set of input test data, then this method will actually create three instances of
         * {@link FrameworkMethod}. In order to allow the user to override the default name, {@link FrameworkMethod} is
         * extended with {@link EasyFrameworkMethod} and {@link EasyFrameworkMethod#setName(String)} method introduced.
         * 
         * @return list of {@link FrameworkMethod}
         */
        @Override
        protected List<FrameworkMethod> computeTestMethods() {
            if (frameworkMethods != null && !frameworkMethods.isEmpty()) {
                return frameworkMethods;
            }
            // superMethodName variable comes from the enclosing DataDrivenTest class.
            // It holds the name of the test method on which the EasyTestRunner instance will work.
            if (superMethodName == null) {
                Assert.fail("Cannot compute Test Methods to run");
            }

            List<FrameworkMethod> finalList = new ArrayList<FrameworkMethod>();
            Iterator<FrameworkMethod> testMethodsItr = super.computeTestMethods().iterator();
            Class<?> testClass = getTestClass().getJavaClass();
            while (testMethodsItr.hasNext()) {
                FrameworkMethod method = testMethodsItr.next();
                if (superMethodName.equals(DataConverter.getFullyQualifiedTestName(method.getName(), testClass))) {
                    // Load the data,if any, at the method level
                    loadData(null, method, getTestClass().getJavaClass());
                    List<Map<String, Object>> methodData = DataContext.getData().get(superMethodName);
                    if (methodData == null) {
                        Assert.fail("Method with name : " + superMethodName
                            + " expects some input test data. But there doesnt seem to be any test "
                            + "data for the given method. Please check the Test Data file for the method data. "
                            + "Possible cause could be a spelling mismatch.");
                    }
                    for (Map<String, Object> testData : methodData) {
                        // Create a new FrameworkMethod for each set of test data
                        EasyFrameworkMethod easyMethod = new EasyFrameworkMethod(method.getMethod());
                        easyMethod.setName(method.getName().concat(testData.toString()));
                        finalList.add(easyMethod);
                    }
                    // Since the runner only ever handles a single method, we break out of the loop as soon as we have
                    // found our method.
                    break;
                }
            }
            if (finalList.isEmpty()) {
                Assert.fail("No method exists with the given name :" + superMethodName);
            }
            frameworkMethods = finalList;
            return finalList;
        }

        /**
         * Validate the {@link DataPoint} filed level annotation.
         * 
         * @param errors list of any errors while validating the {@link DataPoint} field.
         */
        private void validateDataPointFields(List<Throwable> errors) {
            Field[] fields = getTestClass().getJavaClass().getDeclaredFields();

            for (Field each : fields)
                if (each.getAnnotation(DataPoint.class) != null && !Modifier.isStatic(each.getModifiers()))
                    errors.add(new Error("DataPoint field " + each.getName() + " must be static"));
        }

        /**
         * Validate that there could ever be only one constructor.
         * 
         * @param errors list of any errors while validating the Constructor
         */
        @Override
        protected void validateConstructor(List<Throwable> errors) {
            validateOnlyOneConstructor(errors);
        }

        /**
         * Validate the test methods.
         * 
         * @param errors list of any errors while validating test method
         */
        @Override
        protected void validateTestMethods(List<Throwable> errors) {
            //Do Nothing as we now support public non void arg test methods
        }

        /**
         * Override the methodBlock to return custom {@link ParamAnchor}
         * 
         * @param method the Framework Method
         * @return a compiled {@link Statement} object to be evaluated
         */
        @Override
        public Statement methodBlock(final FrameworkMethod method) {

            return new ParamAnchor(method, getTestClass());
        }

        /**
         * Returns a {@link Statement}: run all non-overridden {@code @AfterClass} methods on this class and
         * superclasses before executing {@code statement}; all AfterClass methods are always executed: exceptions
         * thrown by previous steps are combined, if necessary, with exceptions from AfterClass methods into a
         * {@link MultipleFailureException}.
         * 
         * This method is also responsible for writing the data to the output file in case the user is returning test
         * data from the test method. This method will make sure that the data is written to the output file once after
         * the Runner has completed and not for every instance of the test method.
         */
        @Override
        protected Statement withAfterClasses(Statement statement) {
            List<FrameworkMethod> afters = getTestClass().getAnnotatedMethods(AfterClass.class);
            // THere would always be atleast on method associated with the Runner, else validation would fail.
            FrameworkMethod method = frameworkMethods.get(0);
            // Only if the return type of the Method is not VOID, we try to determine the right loader and data files.
            if (method.getMethod().getReturnType() != Void.TYPE) {
                DataLoader loaderAnnotation = method.getAnnotation(DataLoader.class);
                if (loaderAnnotation != null) {
                    determineLoader(loaderAnnotation);

                } else {
                    loaderAnnotation = getTestClass().getJavaClass().getAnnotation(DataLoader.class);
                    if (loaderAnnotation != null) {
                        determineLoader(loaderAnnotation);
                    }
                }
                if (dataLoader == null) {
                    Assert.fail("The framework currently does not support the specified Loader type. "
                        + "You can provide the custom Loader by choosing LoaderType.CUSTOM in TestData "
                        + "annotation and providing your custom loader using DataLoader annotation.");
                }
                dataFiles = loaderAnnotation.filePaths();
            } else {
                dataLoader = null;
            }

            return new RunAftersWithOutputData(statement, afters, null, dataLoader, dataFiles, writableData);
        }

        /**
         * 
         * Static inner class to support Statement evaluation.
         * 
         */
        public class ParamAnchor extends Statement {

            /**
             * An instance of logger associated with the test framework.
             */
            protected final Logger LOG = LoggerFactory.getLogger(EasyTestRunner.ParamAnchor.class);

            private int successes = 0;

            /**
             * an instance of {@link FrameworkMethod} identifying the method to be tested.
             */
            private FrameworkMethod fTestMethod;

            /**
             * An instance of {@link TestClass} identifying the class under test
             */
            private TestClass fTestClass;

            /**
             * A List of {@link Assignments}. Each member in the list corresponds to a single set of test data to be
             * passed to the test method. For eg. If the user has specified the test data in the CSV file as:<br>
             * <br>
             * <B>testGetItems,LibraryId,itemType,searchText</B> <br>
             * ,4,journal,batman <br>
             * ,1,ebook,potter <br>
             * where: <li>testGetItems is the name of the method</li> <li>
             * LibraryId,itemType,searchText are the names of the parameters that the test method expects</li> and <li>
             * ,4,journal,batman</li> <li>,1,ebook,potter</li> are the actual test data <br>
             * then this list will consists of TWO {@link Assignments} instances with values: <li>[[{LibraryId=4,
             * itemType=journal, searchText=batman}]]</li> AND <li>[[{LibraryId=1, itemType=ebook, searchText=potter}]]
             * 
             */
            private List<Assignments> listOfAssignments;

            /**
             * List of Invalid parameters
             */
            private List<AssumptionViolatedException> fInvalidParameters = new ArrayList<AssumptionViolatedException>();

            /**
             * 
             * Construct a new ParamAnchor. The constructor performs the following operations:<br>
             * <li>It sets the class variables method , testClass and initializes the instance of
             * {@link #listOfAssignments}</li> <li>
             * It searches for {@link DataLoader} annotation and if it finds one, it tries to get the right
             * {@link Loader} from the {@link LoaderFactory}. If the {@link Loader} is not found, the test fails. If the
             * Loader is found, it loads the data and makes it available to the entire test Thread using
             * {@link DataContext}
             * 
             * If the annotation {@link DataLoader} is not present, then the test assumes that the user wants to use
             * {@link ParametersSuppliedBy} annotation and does nothing.
             * 
             * @param method the method to run the test on
             * @param testClass an instance of {@link TestClass}
             */
            public ParamAnchor(FrameworkMethod method, TestClass testClass) {
                fTestMethod = method;
                fTestClass = testClass;
                listOfAssignments = new ArrayList<Assignments>();
                DataContext.setMethodName(DataConverter.getFullyQualifiedTestName(method.getName(),
                    testClass.getJavaClass()));
            }

            private TestClass getTestClass() {
                return fTestClass;
            }

            @Override
            public void evaluate() throws Throwable {
                runWithAssignment(Assignments.allUnassigned(fTestMethod.getMethod(), getTestClass()));
                LOG.debug("ParamAnchor evaluate");
                if (successes == 0)
                    Assert.fail("Never found parameters that satisfied method assumptions.  Violated assumptions: "
                        + fInvalidParameters);
            }

            /**
             * This method encapsulates the actual change in behavior from the traditional JUnit Theories way of
             * populating and supplying the test data to the test method. This method creates a list of
             * {@link Assignments} identified by {@link #listOfAssignments} and then calls
             * {@link #runWithCompleteAssignment(Assignments)} for each {@link Assignments} element in the
             * {@link #listOfAssignments}
             * 
             * @param parameterAssignment an instance of {@link Assignments} identifying the parameters that needs to be
             *            supplied test data
             * @throws Throwable if any exception occurs.
             */
            protected void runWithAssignment(Assignments parameterAssignment) throws Throwable {
                while (!parameterAssignment.isComplete()) {
                    List<PotentialAssignment> potentialAssignments = parameterAssignment.potentialsForNextUnassigned();
                    boolean isFirstSetOfArguments = listOfAssignments.isEmpty();
                    for (int i = 0; i < potentialAssignments.size(); i++) {
                        if (isFirstSetOfArguments) {
                            Assignments assignments = Assignments
                                .allUnassigned(fTestMethod.getMethod(), getTestClass());
                            listOfAssignments.add(assignments.assignNext(potentialAssignments.get(i)));
                        } else {
                            Assignments assignments = listOfAssignments.get(i);
                            try {
                                listOfAssignments.set(i, assignments.assignNext(potentialAssignments.get(i)));
                            } catch (IndexOutOfBoundsException e) {
                                listOfAssignments.add(assignments.assignNext(potentialAssignments.get(i)));
                            }
                        }

                    }
                    parameterAssignment = parameterAssignment.assignNext(null);
                }
                if (listOfAssignments.isEmpty()) {
                    LOG.debug("The list of Assignments is null. It normally happens when the user has not supplied any parameters to the test.");
                    LOG.debug(" Creating an instance of Assignments object with all its value unassigned.");
                    listOfAssignments.add(Assignments.allUnassigned(fTestMethod.getMethod(), getTestClass()));
                }
                for (Assignments assignments : listOfAssignments) {
                    runWithCompleteAssignment(assignments);
                }
            }

            /**
             * Run the test data with complete Assignments
             * 
             * @param complete the {@link Assignments}
             * @throws InstantiationException if an error occurs while instantiating the method
             * @throws IllegalAccessException if an error occurs due to illegal access to the test method
             * @throws InvocationTargetException if an error occurs because the method is not invokable
             * @throws NoSuchMethodException if an error occurs because no such method with the given name exists.
             * @throws Throwable any other error
             */
            protected void runWithCompleteAssignment(final Assignments complete) throws InstantiationException,
                IllegalAccessException, InvocationTargetException, NoSuchMethodException, Throwable {
                new BlockJUnit4ClassRunner(getTestClass().getJavaClass()) {
                    @Override
                    protected void collectInitializationErrors(List<Throwable> errors) {
                        // do nothing
                    }

                    @Override
                    public Statement methodBlock(FrameworkMethod method) {
                        final Statement statement = super.methodBlock(method);
                        return new Statement() {
                            @Override
                            public void evaluate() throws Throwable {
                                try {
                                    statement.evaluate();
                                    handleDataPointSuccess();
                                } catch (AssumptionViolatedException e) {
                                    handleAssumptionViolation(e);
                                } catch (Throwable e) {
                                    reportParameterizedError(e, complete.getArgumentStrings(true));
                                }
                            }

                        };
                    }

                    @Override
                    protected Statement methodInvoker(FrameworkMethod method, Object test) {
                        return methodCompletesWithParameters(method, complete, test);
                    }

                    @Override
                    public Object createTest() throws Exception {
                        return getTestClass().getOnlyConstructor().newInstance(complete.getConstructorArguments(true));
                    }
                }.methodBlock(fTestMethod).evaluate();
            }

            /**
             * This method is responsible for actually executing the test method as well as capturing the test data
             * returned by the test method. The algorithm to capture the output data is as follows:
             * <ol>
             * After the method has been invoked explosively, the returned value is checked. If there is a return value:
             * <li>We get the name of the method that is currently executing,
             * <li>We find the exact place in the test input data for which this method was executed,
             * <li>We put the returned result in the map of input test data. The entry in the map has the key :
             * {@link Loader#ACTUAL_RESULT} and the value is the returned value by the test method.
             * <li>If expected result{@link Loader#EXPECTED_RESULT} exist in user input data then we compare it with actual result and 
             *  put the test status either passed/failed. The entry in the map has the key :
             * {@link Loader#TEST_STATUS} and the value is the either PASSED or FAILED.
             * 
             * We finally write the test data to the file.
             * 
             * @param method an instance of {@link FrameworkMethod} that needs to be executed
             * @param complete an instance of {@link Assignments} that contains the input test data values
             * @param freshInstance a fresh instance of the class for which the method needs to be invoked.
             * @return an instance of {@link Statement}
             */
            private Statement methodCompletesWithParameters(final FrameworkMethod method, final Assignments complete,
                final Object freshInstance) {
                return new Statement() {
                    @Override
                    public void evaluate() throws Throwable {
                        try {
                            final Object[] values = complete.getMethodArguments(true);
                            Object returnObj = method.invokeExplosively(freshInstance, values);
                            if (returnObj != null) {
                                LOG.debug("returnObj:" + returnObj);
                                //checking and assigning the map method name.
                                if (!mapMethodName.equals(method.getMethod().getName())) {
                                    // if mapMethodName is not same as the current executing method name
                                	// then assign that to mapMethodName to write to writableData                                	
                                    mapMethodName = method.getMethod().getName();
                                    // initialize the row number.
                                    rowNum = 0;
                                }
                                LOG.debug("mapMethodName:" + mapMethodName + " ,rowNum:" + rowNum);
                                if (writableData.get(mapMethodName) != null) {
                                    LOG.debug("writableData.get(mapMethodName)" + writableData.get(mapMethodName)
                                        + " ,rowNum:" + rowNum);
                                    Map<String,Object> writableRow = writableData.get(mapMethodName).get(rowNum);
                                    writableRow.put(Loader.ACTUAL_RESULT, returnObj);     
                            		
                            		Object expectedResult = writableRow.get(Loader.EXPECTED_RESULT);
                            		// if expected result exist in user input test data, 
                            		// then compare that with actual output result 
                            		// and write the status back to writable map data.
                            		if(expectedResult != null) {
                            			LOG.debug("Expected result exists");
                            			if(expectedResult.toString().equals(returnObj.toString())){
                            				writableRow.put(Loader.TEST_STATUS,Loader.TEST_PASSED);
                            			} else {
                            				writableRow.put(Loader.TEST_STATUS,Loader.TEST_FAILED);
                            			}                            			
                            		}
                            		rowNum++;
                                }


                            }
                        } catch (CouldNotGenerateValueException e) {
                            // ignore
                        }
                    }
                };
            }

            protected void handleAssumptionViolation(AssumptionViolatedException e) {
                fInvalidParameters.add(e);
            }

            protected void reportParameterizedError(Throwable e, Object... params) throws Throwable {
                if (params.length == 0)
                    throw e;
                throw new ParameterizedAssertionError(e, fTestMethod.getName(), params);
            }

            protected void handleDataPointSuccess() {
                successes++;
            }
        }

    }

    /**
     * A simple Runner that will only run the given test methods. The test methods to run are provided by the outer
     * class, which normally is an extension of {@link Suite} Runner.
     * 
     */
    private class GivenTestMethodsRunner extends BlockJUnit4ClassRunner {

        /**
         * The methods that needs to be run by this runner.
         */
        private final List<FrameworkMethod> methodsToRun;

        /**
         * 
         * Construct a new GivenTestMethodsRunner
         * 
         * @param klass
         * @throws InitializationError
         */
        public GivenTestMethodsRunner(Class<?> klass) throws InitializationError {
            super(klass);
            this.methodsToRun = unusedFrameworkMethods;
        }

        /**
         * @return the methodsToRun
         */
        @SuppressWarnings("unused")
        public List<FrameworkMethod> getMethodsToRun() {
            return methodsToRun;
        }

        /**
         * Validate the test methods.
         * 
         * @param errors list of any errors while validating test method
         */
        @Override
        protected void validateTestMethods(List<Throwable> errors) {
            //Do Nothing as we now support non void methods
        }

        /**
         * Compute the list of {@link FrameworkMethod} that needs to be run with the given runner.
         * 
         * @return the list of {@link FrameworkMethod}
         */
        @Override
        protected List<FrameworkMethod> computeTestMethods() {
            List<FrameworkMethod> result = new ArrayList<FrameworkMethod>();
            List<FrameworkMethod> methodsAvailable = super.computeTestMethods();
            for (FrameworkMethod method : unusedFrameworkMethods) {
                for (FrameworkMethod fMethod : methodsAvailable) {
                    if (fMethod.getName().equals(method.getName())) {
                        result.add(fMethod);
                        break;
                    }
                }

            }
            return result;
        }
    }

    /**
     * A List of {@link EasyTestRunner}s and {@link GivenTestMethodsRunner}. If the entry in the list is an instance of
     * {@link EasyTestRunner}, then the runner corresponds to a single method in the executing test class. Since
     * EasyTest is a data driven testing framework, a single test can be run multiple times by providing multiple set of
     * test data from outside of the test. In order to give users a clear picture of the test currently in execution,
     * each method in the test class is wrapped in their own {@link EasyTestRunner}. Each {@link EasyTestRunner} will
     * internally create a list of methods based on the number i=of input test data for the given method. For ex. if
     * there is a method <B><I>getTestData</I></B> in the test class which needs to be run with two sets of input data:
     * <ul>
     * <li>libraryId=1 , itemId=2</li>
     * <li>libraryId=34 , itemId=67</li><br>
     * <br>
     * 
     * then, for such a scenario a single {@link EasyTestRunner} instance will be created for the test method
     * <B>getTestData</B> which will contain two test methods to run with the following name:
     * <ul>
     * <li>getTestData{libraryId=1,itemId=2}</li>
     * <li>getTestData{libraryId=34,itemId=67}</li>
     * 
     * <br>
     * In case the instance in the runner list is an instance of {@link GivenTestMethodsRunner}, then this runner will
     * contain ALL the test methods that does not have a data defined for them. In case it is a simple JUnit test(with @Test
     * annotation and with no parameters), then the runner will simply execute the method. In case the test method is of
     * any other type, it will throw error
     */
    private final ArrayList<Runner> runners = new ArrayList<Runner>();

    /**
     * The current name of the method. Normally used by the enclosed {@link EasyTestRunner} class to identify the right
     * method name.
     */
    private String superMethodName;

    /**
     * List of unused {@link FrameworkMethod} that will be provided to the {@link GivenTestMethodsRunner} class.
     */
    private List<FrameworkMethod> unusedFrameworkMethods;

    /**
     * Get the children Runners
     * 
     * @return a list of {@link EasyTestRunner}
     */
    @Override
    protected List<Runner> getChildren() {
        return runners;
    }

    /**
     * 
     * Construct a new DataDrivenTest. During construction, we will load the test data, and then we will create a list
     * of {@link EasyTestRunner}. each instance of {@link EasyTestRunner} in the list will correspond to a single method
     * in the Test Class under test.<br>
     * The algorithm is as follows:<br>
     * <ul>
     * <li>STEP 1: Load the test data. This will also do the check whether there exists a {@link DataLoader} annotation
     * at the class level</li>
     * <li>Iterate over each method.<br>
     * For each method:
     * <ol>
     * <li>If method has {@link DataLoader} annotation, it means that there is test data associated with the test
     * method.<br>
     * In such a case create an new {@link EasyTestRunner} which will take care of actually loading the test data.
     * <li>If method does not have a {@link DataLoader} annotation, then:
     * <ol>
     * <li>Check if there already exists data for the method. This is possible as the data could have been loaded at the
     * class level.<br>
     * <li>If the data for the given method exists, create a new {@link EasyTestRunner} instance to take care of
     * executing all the test scenarios for the given test method.
     * <li>If the data does not exists for the given test method, put it aside in a list of unused methods,
     * </ol>
     * </ol>
     * Iteration over each method ends.<br>
     * 
     * If there are unused method that do not have any data associated with it, then create an instance of
     * {@link GivenTestMethodsRunner} and pass all the unused methods to it for execution.<br>
     * This whole process will happen for each of the test class that is part of the Suite.
     * 
     * @param klass the test class
     * @throws InitializationError if an initializationError occurs
     */
    public DataDrivenTest(Class<?> klass) throws InitializationError {
        super(klass, Collections.<Runner> emptyList());
        Class<?> testClass = getTestClass().getJavaClass();
        // Load the data at the class level, if any.
        loadData(klass, null, testClass);
        List<FrameworkMethod> availableMethods = getTestClass().getAnnotatedMethods(Test.class);
        List<FrameworkMethod> methodsWithNoData = new ArrayList<FrameworkMethod>();
        for (FrameworkMethod method : availableMethods) {
            this.superMethodName = DataConverter.getFullyQualifiedTestName(method.getName(), testClass);
            // Try loading the data if any at the method level
            if (method.getAnnotation(DataLoader.class) != null) {
                runners.add(new EasyTestRunner(getTestClass().getJavaClass()));
            } else {
                // Method does not have its own dataloader annotation
                // Does method have data already loaded at the class level?
                boolean methodDataLoaded = isMethodDataLoaded(DataConverter.getFullyQualifiedTestName(method.getName(),
                    testClass));
                if (methodDataLoaded) {
                    runners.add(new EasyTestRunner(getTestClass().getJavaClass()));
                } else {
                    methodsWithNoData.add(method);
                }
            }

        }
        // Finally create a runner for methods that do not have Data specified with them.
        // These are potentially the methods with no method parameters and with @Test annotation.
        if (!methodsWithNoData.isEmpty()) {
            unusedFrameworkMethods = methodsWithNoData;
            runners.add(new GivenTestMethodsRunner(klass));
        }
        superMethodName = null;
    }

    /**
     * Check if the data for the given method is loaded or not.
     * 
     * @param methodName the name of the method whose data needs to be checked.
     * @return true if there exists data for the given method, else false.
     */
    protected boolean isMethodDataLoaded(String methodName) {

        boolean result = false;
        if (DataContext.getData() == null || DataContext.getData().keySet() == null
            || DataContext.getData().keySet().isEmpty()) {
            result = false;
        } else {
            Iterator<String> keyIterator = DataContext.getData().keySet().iterator();
            while (keyIterator.hasNext()) {
                result = methodName.equals(keyIterator.next()) ? true : false;
                if (result) {
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Load the Data for the given class or method. This method will try to find {@link DataLoader} on either the class
     * level or the method level. In case the annotation is found, this method will load the data using the specified
     * loader class and then save it in the DataContext for further use by the system. We also create another copy of
     * the input test data that we store in the {@link DataDrivenTest#writableData} field. This is done in order to
     * facilitate the writing of the data that might be returned by the test method.
     * 
     * @param testClass the class object, if any.
     * @param method current executing method, if any.
     * @param currentTestClass the currently executing test class. this is used to append in front of the method name to
     *            get unique method names as there could be methods in different classes with the same name and thus we
     *            want to avoid conflicts.
     */

    protected void loadData(Class<?> testClass, FrameworkMethod method, Class<?> currentTestClass) {
        if (testClass == null && method == null) {
            Assert
                .fail("The framework should provide either the testClass parameter or the method parameter in order to load the test data.");
        }
        // We give priority to Class Loading and then to method loading
        DataLoader testData = null;
        if (testClass != null) {
            testData = testClass.getAnnotation(DataLoader.class);
        } else {
            testData = method.getAnnotation(DataLoader.class);
        }
        if (testData != null) {
            determineLoader(testData);
            if (dataLoader == null) {
                Assert.fail("The framework currently does not support the specified Loader type. "
                    + "You can provide the custom Loader by choosing LoaderType.CUSTOM in TestData "
                    + "annotation and providing your custom loader using DataLoader annotation.");
            } else {
                Map<String, List<Map<String, Object>>> data = dataLoader.loadData(dataFiles);
                // We also maintain the copy of the actual data for our write functionality.
                writableData.putAll(data);
                DataContext.setData(DataConverter.appendClassName(data, currentTestClass));
                DataContext.setConvertedData(DataConverter.convert(data, currentTestClass));

            }
        }
    }

    /**
     * Method that determines the right Loader and the right Data Files for the "write output data" functionality
     * supported by the EasyTest Framework.
     * @param testData an instance of {@link DataLoader} that helps in identifying the right {@link Loader} to write the data back to the file.
     */
    private void determineLoader(DataLoader testData) {
        dataFiles = testData.filePaths();
        LoaderType loaderType = testData.loaderType();
        // Loader
        dataLoader = null;
        if (LoaderType.CUSTOM.equals(loaderType)) {
            PARAM_LOG.info("User specified to use custom Loader. Trying to get the custom loader.");
            if (testData.loader() == null) {
                Assert.fail("Specified the LoaderType as CUSTOM but did not specify loader"
                    + " attribute. A loaderType of CUSTOM requires the loader " + "attribute specifying "
                    + "the Custom Loader Class which implements Loader interface.");
            } else {
                try {
                    Class<? extends Loader> loaderClass = testData.loader();
                    dataLoader = loaderClass.newInstance();
                } catch (Exception e) {
                    throw new RuntimeException("Exception occured while trying to instantiate a class of type :"
                        + testData.loader(), e);
                }
            }
        } else if (dataFiles.length == 0) {
            // No files specified, implies user wants to load data with
            // custom loader
            if (testData.loader() == null) {
                Assert.fail("Specified the LoaderType as CUSTOM but did not specify loader"
                    + " attribute. A loaderType of CUSTOM requires the loader " + "attribute specifying "
                    + "the Custom Loader Class which implements Loader interface.");
            } else {
                try {
                    Class<? extends Loader> loaderClass = testData.loader();
                    dataLoader = loaderClass.newInstance();
                } catch (Exception e) {
                    throw new RuntimeException("Exception occured while trying to instantiate a class of type :"
                        + testData.loader(), e);
                }
            }
        } else {
            // user has specified data files and the data fileType is also
            // not custom.
            dataLoader = LoaderFactory.getLoader(loaderType);
        }
    }

}
