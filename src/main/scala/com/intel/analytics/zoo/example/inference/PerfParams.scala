package com.intel.analytics.zoo.example.inference


case class PerfParams (
                        batchSize: Int = 4,
                        iteration: Int = 10,
                        model: String = "resnet50",
                        quantize: Boolean = false,
                        outputFormat: String = "nc",
                        coreNumber: Int = 18
                      )
