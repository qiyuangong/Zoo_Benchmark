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


${SPARK_HOME}/bin/spark-submit \
    --master k8s://https://${kubernetes_master_url}:6443 \
    --deploy-mode client \
    --class  com.intel.analytics.zoo.benchmark.training.TestKeras \
    --name spark-keras \
    --conf spark.executor.instances=1 \
    --conf spark.driver.memory=2g \
    --conf spark.driver.memoryOverhead=385M \
    --conf spark.executor.cores=1 \
    --conf spark.kubernetes.executor.limit.cores=1 \
    --conf spark.executor.memory=2g \
    --conf spark.kubernetes.container.image=10.239.45.10/arda/spark:3.1.2 \
    --conf spark.kubernetes.authenticate.driver.serviceAccountName=spark \
    target/benchmark-0.3.1-SNAPSHOT-jar-with-dependencies.jar -p /home/intel/Downloads/iris.csv -b 36


${SPARK_HOME}/bin/spark-submit --master k8s://https://${kubernetes_master_url}:6443 --deploy-mode client --conf spark.kubernetes.container.image=10.239.45.10/arda/spark:3.1.2 --class com.intel.analytics.zoo.benchmark.training.TestKeras --conf spark.driver.memory=4G --conf spark.driver.memoryOverhead=385M --conf spark.driver.cores=1 --conf spark.executor.memory=4G --conf spark.executor.memoryOverhead=385M --conf spark.executor.cores=1 --conf spark.kubernetes.executor.limit.cores=1 --conf spark.executor.instances=1 --conf spark.driver.port=7077 --conf spark.driver.blockManager.port=10000 --conf spark.sql.shuffle.partitions=200 --conf spark.sql.planner.externalSort=true --conf spark.sql.hive.thriftServer.singleSession=true --conf spark.kubernetes.container.image.pullPolicy=IfNotPresent --conf spark.kubernetes.executor.volumes.emptyDir.empty-tmp.mount.path=/tmp --conf spark.kubernetes.executor.volumes.emptyDir.unused.options.claimName=OnDemand --conf spark.kubernetes.executor.volumes.emptyDir.unused.options.storageClass=default --conf spark.kubernetes.executor.volumes.emptyDir.unused.options.sizeLimit=4G --conf spark.kubernetes.executor.volumes.emptyDir.unused.mount.path=/spilltodisk --conf spark.kubernetes.executor.volumes.emptyDir.unused.mount.readOnly=false --conf spark.kubernetes.local.dirs.tmpfs=false --conf spark.files.useFetchCache=true --conf spark.executor.heartbeatInterval=30s --conf spark.kubernetes.authenticate.driver.serviceAccountName=spark target/benchmark-0.3.1-SNAPSHOT-jar-with-dependencies.jar -p /home/intel/Downloads/iris.csv -b 36

```

