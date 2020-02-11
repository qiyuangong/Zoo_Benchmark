package com.intel.analytics.zoo.example.inference

import com.intel.analytics.bigdl.tensor.Tensor
import org.apache.log4j.Logger
import scopt.OptionParser
import com.intel.analytics.zoo.common.NNContext
import com.intel.analytics.zoo.pipeline.inference.InferenceModel
import com.intel.analytics.zoo.example.inference.PerfUtils.{time, get_throughput}

import org.apache.spark.util.DoubleAccumulator


object OpenVINOSparkPerf {
  def main(argv: Array[String]): Unit = {
    val params = parser.parse(argv, new PerfParams).get

    val sc = NNContext.initNNContext("OpenVINO Perf on Spark")
    // Load model
    val model = new InferenceModel(1)
    val weight = params.model.substring(0,
      params.model.lastIndexOf(".")) + ".bin"
    model.doLoadOpenVINO(params.model, weight, params.batchSize)
    // BroadCast model
    val bcModel = sc.broadcast(model)
    // Register Accumulator
    val accPredict = new DoubleAccumulator
    sc.register(accPredict, "Predict Time")

    // Prepare Test RDD
    val input = Tensor[Float](Array(1, params.batchSize, 224, 224, 3)).rand(0, 255)

    val testData = sc.parallelize(input)
    // warm up


    testData.mapPartition()
      model.doPredict(input)

    // do the true performance

  }


  val parser: OptionParser[PerfParams] = new OptionParser[PerfParams]("OpenVINO w/ Dnn Spark Model Performance Test") {
    opt[String]('m', "model")
      .text("serialized model, which is protobuf format")
      .action((v, p) => p.copy(model = v))
      .required()

    opt[Int]('b', "batchSize")
      .text("Batch size of input data")
      .action((v, p) => p.copy(batchSize = v))

    opt[Int]('i', "iteration")
      .text("Iteration of perf test. The result will be average of each iteration time cost")
      .action((v, p) => p.copy(iteration = v))
  }

  val logger = Logger.getLogger(getClass)
}
