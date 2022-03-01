# Zoo Keras API Example

## Usage

```bash
mvn clean package -DskipTests

${SPARK_HOME}/bin/spark-submit \
    --master "local[*]" \
    --class com.intel.analytics.zoo.benchmark.training.TestKeras \
    target/benchmark-0.3.1-SNAPSHOT-jar-with-dependencies.jar -p /home/intel/Downloads/iris.csv -b 18


${SPARK_HOME}/bin/spark-submit \
    --master k8s://https://${kubernetes_master_url}:6443 \
    --deploy-mode client \
    --class  com.intel.analytics.zoo.benchmark.training.TestKeras \
    --name spark-keras \
    --conf spark.executor.instances=3 \
    --conf spark.driver.memory=5g \
    --conf spark.executor.cores=6 \
    --conf --total-executor-cores=18 \
    --conf spark.executor.memory=5g \
    --conf spark.kubernetes.container.image=10.239.45.10/arda/spark:3.1.2 \
    --conf spark.kubernetes.authenticate.driver.serviceAccountName=spark \
    target/benchmark-0.3.1-SNAPSHOT-jar-with-dependencies.jar -p /home/intel/Downloads/iris.csv -b 36

```

