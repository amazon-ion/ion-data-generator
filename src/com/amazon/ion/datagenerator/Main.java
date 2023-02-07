package com.amazon.ion.datagenerator;

import java.util.Map;
import org.docopt.Docopt;

public class Main {
    private static final String TITLE = "Ion Data Generation Tool\n\n";

    private static final String DESCRIPTION =
            "Description:\n\n"

                    + "  Tool that allows users to...\n"
                    + "    * Generate varieties of ion data which is aligned with the provided ion schema file.\n"
                    + "    * Compare the benchmark results and generate the comparison report in IonStruct format."
                    + "\n\n";

    private static final String USAGE =
            "Usage:\n"
                    + "  ion-data-generator generate [--seed <seed_value>] [--format <type>] (--data-size <data_size>) (--input-ion-schema <file_path>) <output_file>\n"

                    + "  ion-data-generator compare (--benchmark-result-previous <file_path>) (--benchmark-result-new <file_path>) <output_file>\n"

                    + "  ion-data-generator run-suite (--test-ion-data <file_path>) (--benchmark-options-combinations <file_path>) <output_file>\n"

                    + "  ion-data-generator --help\n\n";

    private static final String OPTIONS =
            "Options:\n"
                    // Option commands:
                    + "  -h --help                              Show this screen.\n"

                    // Common options:
                    + "  -f --format <type>                     Format of the generated data, from the set (ion_binary | ion_text).[default: ion_binary]\n"

                    // 'generate' options:

                    + "  -S --data-size <data_size>      The requested size of the generated data. Required by the 'generate' command."
                    + "The actual amount of data generated will be approximately equal to the requested value.\n"

                    + "  -Q --input-ion-schema <file_path>      This option will specify the path of Ion Schema file which contains all constraints that the "
                    + "generated Ion data would conform with.\n"

                    + "  -M --seed <long>      This option will be specified when users would like to get the same random data from the same schema file."
                    + "The provided value should be up to 64 bits of long seed value, which will be used for creating a pseudorandom number generator. \n"

                    // 'compare' options

                    + "  -P --benchmark-result-previous <file_path>      This option will specify the path of benchmark result from the existing ion-java commit.\n"

                    + "  -X --benchmark-result-new <file_path>      This option will specify the path of benchmark result form the new ion-java commit.\n"

                    // 'organize' options

                    + "  -G --test-ion-data <file_path>      This option will specify the path of the directory which contains all test Ion data.\n"

                    + "  -B --benchmark-options-combinations <file_path>      This option will specify the path of an Ion text file which contains all options combinations of ion-java-benchmark-cli."

                    + "\n";
    private static final String COMMANDS =
            "Commands:\n"

                    + " generate     Generate random Ion data which can be used as input to the read/write commands. "
                    + "Data size, format, the path of input and output file are required options."
                    + "The command will generate approximately the amount of data requested, but the actual size of the generated "
                    + "may be slightly larger or smaller than requested.\n"

                    + " compare     Compare the benchmark results generated by benchmarking ion-java from different commits. After "
                    + "the comparison process, relative changes of speed, heap usage, serialized size and gc.allocated.rate will be "
                    + "calculated and written into an Ion Struct.\n"

                    + "\n";

    private static final String EXAMPLES =
            "Examples:\n\n"

                    + " Generate approximately 500 bytes of text Ion Lists which conform with the constraints in Ion Schema file testList.isl .\n\n"

                    + "  ion-java-benchmark generate --data-size 500 \\\n"
                    + "                              --input-ion-schema './tst/com/amazon/ion/benchmark/testList.isl'\\\n"
                    + "                              --format ion_text\\\n"
                    + "                              example.ion\n\n";


    static Map<String, Object> parseArguments(String... args) {
        return new Docopt(USAGE + OPTIONS)
                .withVersion(new VersionInfo().toString())
                .withHelp(false)
                .parse(args);
    }

    private static void printHelpAndExit(String... messages) {
        for (String message : messages) {
            System.out.println(message);
        }
        System.out.println(TITLE + DESCRIPTION + (COMMANDS + USAGE + OPTIONS).replace("\n", "\n\n") + EXAMPLES);
        System.exit(0);
    }

    public static void main(String[] args) {
        Map<String, Object> optionsMap = parseArguments(args);
        if (optionsMap.get("--help").equals(true)) {
            printHelpAndExit();
        }

        try {
            if (optionsMap.get("generate").equals(true)) {
                GeneratorOptionsValidator.checkValid(args, optionsMap);
                GeneratorOptions.executeGenerator(optionsMap);
            } else if (optionsMap.get("compare").equals(true)) {
                ParseAndCompareBenchmarkResults.compareResult(optionsMap);
            } else {
                GenerateAndOrganizeBenchmarkResults.generateAndSaveBenchmarkResults(optionsMap);
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
