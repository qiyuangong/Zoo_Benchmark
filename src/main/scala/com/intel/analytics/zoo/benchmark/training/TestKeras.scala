
package com.intel.analytics.zoo.benchmark.training

import com.intel.analytics.bigdl.dataset.Sample
import com.intel.analytics.bigdl.nn.{BCECriterion, CrossEntropyCriterion}
import com.intel.analytics.bigdl.nn.keras.{Dense, Sequential}
import com.intel.analytics.bigdl.optim.{SGD, Top1Accuracy, ValidationMethod}
import com.intel.analytics.bigdl.tensor.Tensor
import com.intel.analytics.bigdl.utils.{Engine, Shape}
import com.intel.analytics.zoo.common.NNContext
import org.apache.log4j.Logger
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, SparkSession}
import com.intel.analytics.bigdl.numeric.NumericFloat
import scopt.OptionParser

case class TestParams (
                        batchSize: Int = 16,
                        epoch: Int = 10,
                        path: String = "iris.csv"
                      )


object TestKeras {

  val logger = Logger.getLogger(getClass)

  def main(argv: Array[String]): Unit = {

    val params = parser.parse(argv, new TestParams).get
    def setBigDLConf(conf: SparkConf): SparkConf = {
      conf.set("spark.shuffle.reduceLocality.enabled", "false")
        .set("spark.shuffle.blockTransferService", "nio")
        .set("spark.scheduler.minRegisteredResourcesRatio", "1.0")
        .set("spark.scheduler.maxRegisteredResourcesWaitingTime", "3600s")
        .set("spark.speculation", "false")
        .setAppName("analytics-zoo-demo")
        .setMaster("local[*]")
      val driverCores = conf.get("spark.driver.cores", "0").toInt                        // in my test this is 1
      val executorCores = conf.get("spark.executor.cores", "0").toInt              // in my test this is 1
      val executorInstances = conf.get("spark.executor.instances", "0").toInt // in my test this is 1
      val maxCores = String.valueOf(driverCores + executorCores * executorInstances) // in my test this becomes 2
      logger.info("Updating Spark configuration spark.cores.max={}" + maxCores)
      conf.set("spark.cores.max", maxCores)
    }
    val conf = setBigDLConf(new SparkConf())
    val sc = NNContext.initNNContext(conf)
    val spark = SparkSession.builder().config(conf).getOrCreate()

    // Disable redirecting logs of Spark and BigDL
    System.setProperty("bigdl.utils.LoggerFilter.disable", "true")

    def prepareDatasetForFitting(df: DataFrame, featureColumns: Array[String], labelColumn: String, labels: Array[String]): RDD[Sample[Float]] = {
      val labelIndex = df.columns.indexOf(labelColumn)
      val featureIndices = featureColumns.map(fc => df.columns.indexOf(fc))
      val dimInput = featureColumns.length
      df.rdd.map{row =>
        val features = featureIndices.map(row.getDouble(_).toFloat)
        val featureTensor = Tensor[Float](features, Array(dimInput))
        val labelTensor = Tensor[Float](1)
        labelTensor(Array(1)) = labels.indexOf(String.valueOf(row.get(labelIndex))) + 1
        Sample[Float](featureTensor, labelTensor)
      }
    }

    val path = getClass.getClassLoader.getResource("iris.csv").toString

    val dataset = spark.read.option("header", true).option("inferSchema", true).csv(path)

    val labels = Array("Iris-setosa", "Iris-versicolor", "Iris-virginica")
    val labelCol = "class"
    val featureCols = Array("sepal_len", "sepal_wid", "petal_len", "petal_wid")
    val Array(trainDF, validDF, evalDF) = dataset.randomSplit(Array(0.8, 0.1, 0.1), 31)

    val trainRDD = prepareDatasetForFitting(trainDF, featureCols, labelCol, labels)
    val validRDD = prepareDatasetForFitting(validDF, featureCols, labelCol, labels)
    val evalRDD = prepareDatasetForFitting(evalDF, featureCols, labelCol, labels)

    val dimInput = 4
    val dimOutput = 3
    val nHidden = 100
    val model = Sequential[Float]()
    model.add(Dense[Float](nHidden, activation = "relu", inputShape = Shape(dimInput)).setName("fc_1"))
    model.add(Dense[Float](nHidden, activation = "relu").setName("fc_2"))
    model.add(Dense[Float](dimOutput, activation = "softmax").setName("fc_3"))

    val optimizer = new SGD[Float](0.001)
    val loss = CrossEntropyCriterion[Float]()
    val metrics = Array[ValidationMethod[Float]](new Top1Accuracy[Float]())

    model.compile(optimizer, loss, metrics)
    model.fit(trainRDD, params.batchSize, params.epoch, validRDD)

  }

  val parser: OptionParser[TestParams] = new OptionParser[TestParams]("TestKeras") {
    opt[String]('p', "path")
      .text("path of iris.csv")
      .action((v, p) => p.copy(path = v))

    opt[Int]('b', "batchSize")
      .text("Batch size of input data")
      .action((v, p) => p.copy(batchSize = v))

    opt[Int]('e', "epoch")
      .text("Epoch for training")
      .action((v, p) => p.copy(epoch = v))
  }

}