## Ion Data Generator
* This tool allows users to generate synthetic Ion data by matching the structure and type distribution of a schema derived from real-world data.

## Installation

Building the tool generates a self-contained executable `jar`.

To build the tool, simply run

```
mvn clean install
```

When build completes, there will be a jar named ion-data-generator-< version >-SNAPSHOT.jar generated in the target/ sub-directory. This jar may be executed using java -jar <path-to-jar>  and users may create an alias to java -jar <path-to-jar> named, e.g., ion-data-generator to simplify invocation of the tool.
Users may use the following pattern to generate ion data:
```
ion-data-generator generate (--data-size <data_size>) [--format <type>] [--input-ion-schema <file-path>] <output_file>
```

## Adding an option for Ion Data Generator

Adding an option to specify generated scalar type Ion Data involves the following steps.
* In `Main`, add the option to the `USAGE` and `OPTIONS` strings, mimicking the existing format.
* Add invalid options hashset to `GeneratorOptionsValidator` , mimicking the existing format.
* Add parsing options logic to `GeneratorOptions` and consider which type of Ion data the parsed parameters should be passed to,
  mimicking the existing logic of passing options parameters.
* In `WriteRandomIonValues`, add logic to the methods of constructing Ion Data to process the added option.

## Adding logic to process constraints in Ion Schema

Adding logic to process constraints in Ion Schema involves the following steps.
* Add a class under the package `schema/constraints` and name the class name with the constraint name. Developers should follow the rules provided in [Ion Schema Specification 1.0](https://amzn.github.io/ion-schema/docs/spec.html) while creating the constraint classes.
* Add logic in `DataConstructor.java` to process the relevant constraints while constructing synthetic data.


## Security

See [CONTRIBUTING](CONTRIBUTING.md#security-issue-notifications) for more information.

## License

This project is licensed under the Apache-2.0 License.

