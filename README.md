# measure-doc

A library for computing Javadoc coverage on Java source files. Documentation coverage is a measure of how mature and
easy to use a software project is.

## Opinions

The library holds the following opinions about what should be covered with Javadoc. These opinions may be made more
configurable in the future.

* Public classes, interfaces, annotation definitions, and `enum` should have Javadoc with a non-empty description, 
and all type parameters should be documented with `@param <T>` and have a non-empty description
* Public methods and constructors in public classes should have Javadoc with a non-empty description, and these tags
when applicable
    * type parameters
    * parameters
    * return
    * throws
* Public fields in public classes should have Javadoc with a non-empty description
    
## Usage

Basic usage to measure a Java source file is:

```
val measurer = new MeasurerCheckStyle(Seq(new JavadocTypeMeasure(), new JavadocMethodMeasure()))
measurer.measure(Seq(new File("<path-to-java-file>.java"))).map { coverages =>
  // use
}
```

There are three concepts:

* `Measurer` is an object that can measure documentation coverage for a given set of files. There is currently only one
implementation, `MeasurerCheckStyle` which works for Java files with Javadoc only. We plan to add support for Scala
files with Scaladoc in the future (and other popular languages). `MeasurerCheckStyle` creates an abstract syntax tree
for the source file and delegates to given `Measure` objects to measure the coverage of different code items.
* `Measure` is an object that can measure a specific kind of documentation coverage. Currently these are coupled to the
CheckStyle Java syntax tree.
* `Coverage` is the model object that represents the documentation coverage that was measured. It contains information
about which class or method the coverage is for, how many of the expected documentation items (listed above) were 
covered, the total number of expected documentation items (listed above), and for any missing coverage, the description
of what was missing.