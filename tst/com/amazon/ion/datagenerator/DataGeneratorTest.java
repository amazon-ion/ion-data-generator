package com.amazon.ion.datagenerator;

import com.amazon.ion.*;
import com.amazon.ion.system.IonReaderBuilder;
import com.amazon.ion.system.IonSystemBuilder;
import com.amazon.ion.util.IonStreamUtils;
import com.amazon.ionschema.IonSchemaSystem;
import com.amazon.ionschema.Schema;
import com.amazon.ionschema.Type;
import com.amazon.ionschema.Violations;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class DataGeneratorTest {
    private static String outputFile = null;
    private final static IonSystem SYSTEM = IonSystemBuilder.standard().build();
    private final static IonLoader LOADER = SYSTEM.newLoader();
    private final static String INPUT_ION_STRUCT_FILE_PATH = "./tst/com/amazon/ion/datagenerator/testData/testStruct.isl";
    private final static String INPUT_ION_LIST_FILE_PATH = "./tst/com/amazon/ion/datagenerator/testData/testList.isl";
    private final static String INPUT_NESTED_ION_LIST_PATH = "./tst/com/amazon/ion/datagenerator/testData/testNestedList.isl";
    private final static String INPUT_NESTED_ION_STRUCT_PATH = "./tst/com/amazon/ion/datagenerator/testData/testNestedStruct.isl";
    private final static String INPUT_ION_DECIMAL_FILE_PATH = "./tst/com/amazon/ion/datagenerator/testData/testDecimal.isl";
    private final static String INPUT_ION_TIMESTAMP_FILE_PATH = "./tst/com/amazon/ion/datagenerator/testData/testTimestamp.isl";
    private final static String INPUT_SCHEMA_CONTAINS_CODEPOINT_LENGTH = "./tst/com/amazon/ion/datagenerator/testData/testStringCodepointLength.isl";
    private final static String INPUT_ION_STRUCT_SCHEMA_CONTAINS_ELEMENT_FILE_PATH = "./tst/com/amazon/ion/datagenerator/testData/testSchemaContainsElement.isl";
    private final static String INPUT_ION_SEXP_FILE_PATH = "./tst/com/amazon/ion/datagenerator/testData/testSexp.isl";
    private final static String INPUT_ION_INT_SCHEMA = "./tst/com/amazon/ion/datagenerator/testData/testIntWithoutConstraint.isl";
    private final static String INPUT_TEST_ELEMENT_SCHEMA = "./tst/com/amazon/ion/datagenerator/testData/testElement.isl";
    private final static String INPUT_ION_CLOB_FILE_PATH = "./tst/com/amazon/ion/datagenerator/testData/testClob.isl";
    private final static String INPUT_ION_BLOB_FILE_PATH = "./tst/com/amazon/ion/datagenerator/testData/testBlob.isl";
    private final static String INPUT_ION_FLOAT_FILE_PATH = "./tst/com/amazon/ion/datagenerator/testData/testFloat.isl";
    private final static String INPUT_ION_FLOAT_VALID_VALUE_FILE_PATH = "./tst/com/amazon/ion/datagenerator/testData/testFloatValidValue.isl";
    private final static String INPUT_ION_SYMBOL_FILE_PATH = "./tst/com/amazon/ion/datagenerator/testData/testSymbol.isl";
    private final static String INPUT_SCHEMA_CONTAINS_ANNOTATIONS = "./tst/com/amazon/ion/datagenerator/testData/testAnnotations.isl";
    private final static String INPUT_ION_STRING_FILE_PATH = "./tst/com/amazon/ion/datagenerator/testData/testString.isl";
    private final static String INPUT_ION_INT_FILE_PATH = "./tst/com/amazon/ion/datagenerator/testData/testInt.isl";
    private final static String INPUT_SCHEMA_WITH_ANY_OF = "./tst/com/amazon/ion/datagenerator/testData/testAnyOf.isl";
    private final static String INPUT_SCHEMA_WITH_ONE_OF = "./tst/com/amazon/ion/datagenerator/testData/testOneOf.isl";
    private final static String SCORE_DIFFERENCE = "scoreDifference";
    private final static String COMPARISON_REPORT_WITHOUT_REGRESSION = "./tst/com/amazon/ion/workflow/testComparisonReportWithoutRegression.ion";
    private final static String COMPARISON_REPORT = "./tst/com/amazon/ion/workflow/testComparisonReport.ion";
    private final static String BENCHMARK_RESULT_PREVIOUS = "./tst/com/amazon/ion/workflow/IonLoaderBenchmarkResultPrevious.ion";
    private final static String BENCHMARK_RESULT_NEW = "./tst/com/amazon/ion/workflow/IonLoaderBenchmarkResultNew.ion";
    private final static BigDecimal EXPECTED_GC_ALLOCATE_THRESHOLD = new BigDecimal("-0.010774139119162");
    private final static BigDecimal EXPECTED_SPEED_THRESHOLD = new BigDecimal("-0.326936");
    private final static BigDecimal EXPECTED_HEAP_USAGE_THRESHOLD = new BigDecimal("-0.184482");
    private final static BigDecimal EXPECTED_REGRESSION_VALUE = new BigDecimal("-0.2851051607559");
    private final static BigDecimal EXPECTED_SERIALIZED_SIZE = new BigDecimal("0.000000");
    private final static String GC_ALLOCATE = "·gc.alloc.rate";
    private final static String HEAP_USAGE = "Heap usage";
    private final static String SERIALIZED_SIZE = "Serialized size";
    private final static String SPEED = "speed";
    private final static File[] TEST_ISL_FILES = new File("./tst/com/amazon/ion/datagenerator/testData/").listFiles();

    /**
     * Construct IonReader for current output file in order to finish the following test process.
     * @param optionsMap is the hash map which generated by the command line parser which match the option name and its value appropriately.
     * @return constructed IonReader.
     * @throws Exception if errors occur during executing data generator process.
     */
    public static IonReader executeAndRead(Map<String, Object> optionsMap) throws Exception {
        outputFile = optionsMap.get("<output_file>").toString();
        GeneratorOptions.executeGenerator(optionsMap);
        return IonReaderBuilder.standard().build(new BufferedInputStream(new FileInputStream(outputFile)));
    }

    /**
     * Detect if violation occurs by comparing every single data in the generated file with Ion Schema constraints.
     * @param inputFile is the Ion Schema file.
     * @throws Exception if error occurs when checking if there is violation in the generated data.
     */
    public static void violationDetect(String inputFile) throws Exception {
        Map <String, Object> optionsMap = Main.parseArguments("generate", "--data-size", "5000", "--format", "ion_text", "--input-ion-schema", inputFile, "test8.ion");
        String inputFilePath = optionsMap.get("--input-ion-schema").toString();
        outputFile = optionsMap.get("<output_file>").toString();
        String schemaID = inputFile.substring(inputFile.lastIndexOf('/') + 1);
        try (
                IonReader readerInput = IonReaderBuilder.standard().build(new BufferedInputStream(new FileInputStream(inputFilePath)));
                IonReader reader = DataGeneratorTest.executeAndRead(optionsMap);
        ) {
            // Get the name of Ion Schema.
            IonDatagram schema = ReadGeneralConstraints.LOADER.load(readerInput);
            String ionSchemaName = null;
            for (int i = 0; i < schema.size(); i++) {
                IonValue schemaValue = schema.get(i);
                if (schemaValue.getType().equals(IonType.STRUCT) && schemaValue.getTypeAnnotations()[0].equals(IonSchemaUtilities.KEYWORD_TYPE)) {
                    IonStruct constraintStruct = (IonStruct) schemaValue;
                    ionSchemaName = constraintStruct.get(IonSchemaUtilities.KEYWORD_NAME).toString();
                    break;
                }
            }
            //Load schema file and get the type of the Ion Schema.
            IonSchemaSystem ISS = IonSchemaUtilities.buildIonSchemaSystem(inputFile);
            Schema newSchema = ISS.loadSchema(schemaID);
            Type type = newSchema.getType(ionSchemaName);
            while (reader.next() != null) {
                IonValue value = SYSTEM.newValue(reader);
                Violations violations = type.validate(value);
                assertTrue("Violations " + violations + "found in value " + value, violations.isValid());
            }
        }
    }

    /**
     * This method executes Ion Data Generation twice and compare the generated data then provide the comparison result which represents by the boolean value.
     * @param seedOption represents whether the '--seed' option is specified. If it is specified, the value of this option is 'true'. Otherwise, the value is 'false'.
     * @return Boolean value which represents the comparison result. If there is any difference between two generated ion files, the return value will be set to 'false'.
     * @throws Exception if there is error when executing data generation.
     */
    public Boolean generateAndCompare(Boolean seedOption) throws Exception {
        Boolean result = true;
        String outputBefore = "testSeed0.ion";
        String outputAfter = "testSeed1.ion";
        for (File testFile : TEST_ISL_FILES) {
            String testFilePath = testFile.getPath();
            for (int i = 0; i < 2; i++) {
                String outputFile = "testSeed" + i + ".ion";
                Map<String, Object> optionsMap;
                if (seedOption == true) {
                    optionsMap = Main.parseArguments("generate", "--data-size", "5000", "--seed", "200", "--format", "ion_text", "--input-ion-schema", testFilePath, outputFile);
                } else {
                    optionsMap = Main.parseArguments("generate", "--data-size", "5000", "--format", "ion_text", "--input-ion-schema", testFilePath, outputFile);
                }
                GeneratorOptions.executeGenerator(optionsMap);
            }
            IonDatagram datagramBefore = LOADER.load(new FileInputStream(outputBefore));
            IonDatagram datagramAfter = LOADER.load(new FileInputStream(outputAfter));
            if (datagramBefore.size() == datagramAfter.size()) {
                for (int i = 0; i < datagramBefore.size(); i++) {
                    if (!datagramBefore.get(i).equals(datagramAfter.get(i))) {
                        result = false;
                        break;
                    }
                }
            } else {
                result = false;
                continue;
            }
            Files.delete(Paths.get(outputBefore));
            Files.delete(Paths.get(outputAfter));
        }
        return result;
    }

    /**
     * Assert the format of generated file is conform with the expected format [ion_binary|ion_text].
     * @throws Exception if error occurs when executing Ion data generator.
     */
    @Test
    public void testGeneratedFormat() throws Exception {
        List<String> inputs = new ArrayList<>(Arrays.asList("ion_text","ion_binary"));
        for (int i = 0; i < 2; i++ ) {
            Map<String, Object> optionsMap = Main.parseArguments("generate", "--data-size", "5000", "--format", "ion_text", "--input-ion-schema", INPUT_ION_DECIMAL_FILE_PATH, "test8.ion");
            GeneratorOptions.executeGenerator(optionsMap);
            String format = String.valueOf(optionsMap.get("--format"));
            outputFile = optionsMap.get("<output_file>").toString();
            Path path = Paths.get(outputFile);
            byte[] buffer = Files.readAllBytes(path);
            assertEquals(format == IonSchemaUtilities.ION_BINARY, IonStreamUtils.isIonBinary(buffer));
        }
    }

    /**
     * Test if there's violation when generating ion data from the schema which contains constraint 'any_of'.
     * @throws Exception if error occurs during violation detecting process.
     */
    @Test
    public void testLogicConstraintAnyOf() throws Exception {
        DataGeneratorTest.violationDetect(INPUT_SCHEMA_WITH_ANY_OF);
    }

    /**
     * Test if there's violation when generating ion data from the schema which contains constraint 'one_of'.
     * @throws Exception if error occurs during violation detecting process.
     */
    @Test
    public void testLogicConstraintOneOf() throws Exception {
        DataGeneratorTest.violationDetect(INPUT_SCHEMA_WITH_ONE_OF);
    }

    /**
     * Assert the generated data size in bytes has an 10% difference with the expected size, this range is not available for Ion symbol, because the size of symbol is predicted.
     * @throws Exception if error occurs when executing Ion data generator.
     */
    @Test
    public void testSizeOfGeneratedData() throws Exception {
        Map<String, Object> optionsMap = Main.parseArguments("generate", "--data-size", "5000", "--format", "ion_text", "--input-ion-schema", INPUT_ION_TIMESTAMP_FILE_PATH, "test8.ion");
        GeneratorOptions.executeGenerator(optionsMap);
        int expectedSize = Integer.parseInt(optionsMap.get("--data-size").toString());
        outputFile = optionsMap.get("<output_file>").toString();
        Path filePath = Paths.get(outputFile);
        FileChannel fileChannel;
        fileChannel = FileChannel.open(filePath);
        int fileSize = (int)fileChannel.size();
        fileChannel.close();
        int difference = Math.abs(expectedSize - fileSize);
        assertTrue(difference <= 0.1 * expectedSize);
    }

    /**
     * Test if there's violation when generating Ion Struct based on Ion Schema.
     * @throws Exception if error occurs during the violation detecting process.
     */
    @Test
    public void testViolationOfIonStruct() throws Exception {
        DataGeneratorTest.violationDetect(INPUT_ION_STRUCT_FILE_PATH);
    }

    /**
     * Test if there's violation when generating Ion List based on Ion Schema.
     * @throws Exception if error occurs during the violation detecting process.
     */
    @Test
    public void testViolationOfIonList() throws Exception {
        DataGeneratorTest.violationDetect(INPUT_ION_LIST_FILE_PATH);
    }

    /**
     * Test if there's violation when generating nested IonStruct based on Ion Schema.
     * @throws Exception if error occurs during the violation detecting process.
     */
    @Test
    public void testViolationOfNestedIonStruct() throws Exception {
        DataGeneratorTest.violationDetect(INPUT_NESTED_ION_STRUCT_PATH);
    }

    /**
     * Test if there's violation when generating string based on the ISL contains constraint 'codepoint_length'.
     * @throws Exception if error occurs during the violation detecting process.
     */
    @Test
    public void testCodepointLength() throws Exception {
        DataGeneratorTest.violationDetect(INPUT_SCHEMA_CONTAINS_CODEPOINT_LENGTH);
    }

    /**
     * Test if the '--seed' option allows us to generate the same random value.
     * @throws Exception if error occurs during the executing and comparison process.
     */
    @Test
    public void testSeedOption() throws Exception {
        assertTrue(generateAndCompare(true));
    }

    /**
     * Test if the generated data is randomized when there is no '--seed' option provided.
     * @throws Exception if error occurs during the executing and comparison process.
     */
    @Test
    public void testWithoutSeed() throws Exception {
        assertFalse(generateAndCompare(false));
    }

    /**
     * Test if there's violation when generating IonValue from ISL which contains constraint 'annotations'.
     * @throws Exception if error occurs during the violation detecting process.
     */
    @Test
    public void testAnnotations() throws Exception {
        DataGeneratorTest.violationDetect(INPUT_SCHEMA_CONTAINS_ANNOTATIONS);
    }

    /**
     * Test if there's violation when generating Ion Sexp based on Ion Schema.
     * @throws Exception if error occurs during the violation detecting process.
     */
    @Test
    public void testViolationOfSexp() throws Exception {
        DataGeneratorTest.violationDetect(INPUT_ION_SEXP_FILE_PATH);
    }

    /**
     * Test if there's violation when generating nested Ion Struct based on Ion Schema.
     * @throws Exception if error occurs during the violation detecting process.
     */
    @Test
    public void testViolationOfNestedIonList() throws Exception {
        DataGeneratorTest.violationDetect(INPUT_NESTED_ION_LIST_PATH);
    }

    /**
     * Test if there's violation detected when generating IonInt from ISL without constraint.
     * @throws Exception if error occurs during the violation detecting process.
     */
    @Test
    public void testViolationOfIonInt() throws Exception {
        DataGeneratorTest.violationDetect(INPUT_ION_INT_SCHEMA);
    }

    /**
     * Test if there's violation when generating IonList based on ISL that specifies constraint 'element' without specifying 'container_length'.
     * @throws Exception if error occurs during the violation detecting process.
     */
    @Test
    public void testViolationOfConstraintElement() throws Exception {
        DataGeneratorTest.violationDetect(INPUT_TEST_ELEMENT_SCHEMA);
    }

    /**
     * Test if there's violation when generating IonStruct based on Ion Schema contains constraint 'element'.
     * @throws Exception if error occurs during the violation detecting process.
     */
    @Test
    public void testViolationOfIonStructWithElement() throws Exception {
        DataGeneratorTest.violationDetect(INPUT_ION_STRUCT_SCHEMA_CONTAINS_ELEMENT_FILE_PATH);
    }

    /**
     * Test if there's violation when generating Ion Timestamp based on Ion Schema.
     * @throws Exception if error occurs during the violation detecting process.
     */
    @Test
    public void testViolationOfTimestamp() throws Exception {
        DataGeneratorTest.violationDetect(INPUT_ION_TIMESTAMP_FILE_PATH);
    }

    /**
     * Test if there's violation when generating decimals based on Ion Schema.
     * @throws Exception if error occurs during the violation detecting process.
     */
    @Test
    public void testViolationOfIonDecimal() throws Exception {
        DataGeneratorTest.violationDetect(INPUT_ION_DECIMAL_FILE_PATH);
    }

    /**
     * Test if there's violation when generating clobs based on Ion Schema.
     * @throws Exception if error occurs during the violation detecting process.
     */
    @Test
    public void testViolationOfIonClob() throws Exception {
        DataGeneratorTest.violationDetect(INPUT_ION_CLOB_FILE_PATH);
    }

    /**
     * Test if there's violation when generating blobs based on Ion Schema.
     * @throws Exception if error occurs during the violation detecting process.
     */
    @Test
    public void testViolationOfIonBlob() throws Exception {
        DataGeneratorTest.violationDetect(INPUT_ION_BLOB_FILE_PATH);
    }

    /**
     * Test if there's violation when generating floats from Ion Schema file.
     * @throws Exception if error occurs during the violation detecting process.
     */
    @Test
    public void testViolationOfFloat() throws Exception {
        DataGeneratorTest.violationDetect(INPUT_ION_FLOAT_FILE_PATH);
    }

    /**
     * Test if there's violation when generating symbols from Ion Schema file.
     * @throws Exception if error occurs during the violation detecting process.
     */
    @Test
    public void testViolationOfSymbol() throws Exception {
        DataGeneratorTest.violationDetect(INPUT_ION_SYMBOL_FILE_PATH);
    }

    /**
     * Test if there's violation when generating strings from Ion Schema file.
     * @throws Exception if error occurs during the violation detecting process.
     */
    @Test
    public void testViolationOfString() throws Exception {
        DataGeneratorTest.violationDetect(INPUT_ION_STRING_FILE_PATH);
    }

    /**
     * Test if there's violation when generating floats from Ion Schema file which specifies valid value.
     * @throws Exception if error occurs during the violation detecting process.
     */
    @Test
    public void testViolationOfFloatValidValue() throws Exception {
        DataGeneratorTest.violationDetect(INPUT_ION_FLOAT_VALID_VALUE_FILE_PATH);
    }

    /**
     * Test if there's violation when generating int from Ion Schema file which specifies valid value range.
     * @throws Exception if error occurs during the violation detecting process.
     */
    @Test
    public void testViolationOfIntValidValue() throws Exception {
        DataGeneratorTest.violationDetect(INPUT_ION_INT_FILE_PATH);
    }

    /**
     * Test the accuracy of the calculated results in the generated file.
     * @throws Exception if error occurs when reading the input file.
     */
    @Test
    public void testParseBenchmark() throws Exception {
        Map<String, Object> optionsMap = Main.parseArguments("compare", "--benchmark-result-previous", BENCHMARK_RESULT_PREVIOUS, "--benchmark-result-new", BENCHMARK_RESULT_NEW, "test11.ion");
        ParseAndCompareBenchmarkResults.compareResult(optionsMap);
        outputFile = optionsMap.get("<output_file>").toString();
        try (IonReader reader = IonReaderBuilder.standard().build(new BufferedInputStream(new FileInputStream(outputFile)))) {
            reader.next();
            reader.stepIn();
            while (reader.next() != null) {
                if (reader.getFieldName().equals(SCORE_DIFFERENCE)) {
                    reader.stepIn();
                    while (reader.next() != null) {
                        String benchmarkResultPrevious = optionsMap.get("--benchmark-result-previous").toString();
                        String benchmarkResultNew = optionsMap.get("--benchmark-result-new").toString();
                        BigDecimal previousScore = ParseAndCompareBenchmarkResults.getScore(benchmarkResultPrevious, reader.getFieldName());
                        BigDecimal newScore = ParseAndCompareBenchmarkResults.getScore(benchmarkResultNew, reader.getFieldName());
                        BigDecimal scoreDifference = newScore.subtract(previousScore);
                        BigDecimal relativeDifference = scoreDifference.divide(previousScore, RoundingMode.HALF_UP);
                        assertTrue(relativeDifference.equals(reader.decimalValue()));
                    }
                    reader.stepOut();
                }
            }
        }
    }

    /**
     * Test whether the method which calculate and construct threshold map can return the expected result.
     * @throws Exception if errors occur when constructing threshold map.
     */
    @Test
    public void testThresholdMapParser() throws Exception {
        Map<String, BigDecimal> thresholdMap = ParseAndCompareBenchmarkResults.getThresholdMap(BENCHMARK_RESULT_PREVIOUS, BENCHMARK_RESULT_NEW);
        for (String keyWord : thresholdMap.keySet()) {
            switch (keyWord) {
                case GC_ALLOCATE:
                    assertTrue(thresholdMap.get(keyWord).equals(EXPECTED_GC_ALLOCATE_THRESHOLD));
                    break;
                case SPEED:
                    assertTrue(thresholdMap.get(keyWord).equals(EXPECTED_SPEED_THRESHOLD));
                    break;
                case HEAP_USAGE:
                    assertTrue(thresholdMap.get(keyWord).equals(EXPECTED_HEAP_USAGE_THRESHOLD));
                    break;
                case SERIALIZED_SIZE:
                    assertTrue(thresholdMap.get(keyWord).equals(EXPECTED_SERIALIZED_SIZE));
                    break;
                default:
                    throw new IllegalStateException("This aspect of benchmark result is not supported when generating threshold map.");
            }
        }
    }

    /**
     * Test whether the detecting regression process can return the expected result when there is performance regression in the test file.
     * In this unit test we use an Ion file which contain regression on [·gc.alloc.rate] as input to test the detectRegression method.
     * @throws Exception if error occur when reading Ion data.
     */
    @Test
    public void testRegressionDetected() throws Exception {
        Map<String, BigDecimal> scoreMap = constructScoreMap(COMPARISON_REPORT);
        Map<String, BigDecimal> thresholdMap = ParseAndCompareBenchmarkResults.getThresholdMap(BENCHMARK_RESULT_PREVIOUS, BENCHMARK_RESULT_NEW);
        Map<String, BigDecimal> detectionResult = ParseAndCompareBenchmarkResults.detectRegression(thresholdMap, scoreMap, COMPARISON_REPORT);
        Map<String, BigDecimal> expectedResult = new HashMap<>();
        expectedResult.put(GC_ALLOCATE, EXPECTED_REGRESSION_VALUE);
        assertEquals(expectedResult, detectionResult);
    }

    /**
     * Test whether the detecting regression process can return the expected result when there is no performance regression in the test file.
     * In this unit test we use an Ion file which contain regression on [·gc.alloc.rate] as input to test the detectRegression method.
     * @throws Exception if error occur when reading Ion data.
     */
    @Test
    public void testRegressionNotDetected() throws Exception {
        Map<String, BigDecimal> scoreMap = constructScoreMap(COMPARISON_REPORT_WITHOUT_REGRESSION);
        Map<String, BigDecimal> thresholdMap = ParseAndCompareBenchmarkResults.getThresholdMap(BENCHMARK_RESULT_PREVIOUS, BENCHMARK_RESULT_NEW);
        Map<String, BigDecimal> detectionResult = ParseAndCompareBenchmarkResults.detectRegression(thresholdMap, scoreMap, COMPARISON_REPORT_WITHOUT_REGRESSION);
        assertTrue(detectionResult.size() == 0);
    }

    /**
     * Construct the score map which matches the benchmark aspect with its score from the comparison report.
     * @param inputFile specify the path of comparison report which is generated after the comparing benchmark results from different commits.
     * @return a Map<String, BigDecimal> contains scores information.
     * @throws Exception if error occurs when reading data.
     */
    private static Map<String, BigDecimal> constructScoreMap(String inputFile) throws Exception {
        Map<String, BigDecimal> scoreMap = new HashMap<>();
        IonStruct scoresStruct;
        try (IonReader reader = IonReaderBuilder.standard().build(new BufferedInputStream(new FileInputStream(inputFile)))) {
            reader.next();
            if (reader.getType().equals(IonType.STRUCT)) {
                IonStruct comparisonResult = (IonStruct) ReadGeneralConstraints.LOADER.load(reader).get(0);
                scoresStruct = (IonStruct) comparisonResult.get(ParseAndCompareBenchmarkResults.RELATIVE_DIFFERENCE_SCORE);
            } else {
                throw new IllegalStateException("The data structure of the comparison report is not supported.");
            }
        }
        for (String keyWord : ParseAndCompareBenchmarkResults.BENCHMARK_SCORE_KEYWORDS) {
            IonValue score = scoresStruct.get(keyWord);
            if (score.getType().equals(IonType.FLOAT)) {
                IonFloat scoreFloat = (IonFloat) score;
                scoreMap.put(keyWord, scoreFloat.bigDecimalValue());
            } else {
                IonDecimal scoreDecimal = (IonDecimal) score;
                scoreMap.put(keyWord, scoreDecimal.bigDecimalValue());
            }
        }
        return scoreMap;
    }

    /**
     * Delete all files generated in the test process.
     * @throws IOException if an error occur when deleting files.
     */
    @After
    public void deleteGeneratedFile() throws IOException {
        if (outputFile != null) {
            Path filePath = Paths.get(outputFile);
            if(Files.exists(filePath)) {
                Files.delete(filePath);
            }
        }
    }
}
