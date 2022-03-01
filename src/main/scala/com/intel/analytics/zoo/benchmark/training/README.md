# Zoo Keras API Example

## Usage

```bash
mvn clean package -DskipTests
${SPARK_HOME}/bin/spark-submit --master "local[*]" --class com.intel.analytics.zoo.benchmark.training.TestKeras target/benchmark-0.3.1-SNAPSHOT-jar-with-dependencies.jar -p /home/intel/Downloads/iris.csv -b 18

```

