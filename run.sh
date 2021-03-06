#!/bin/bash

# debug flag
#set -x

HYPER=1
if command -v lscpu &> /dev/null
then
    HYPER=`lscpu |grep "Thread(s) per core"|awk '{print $4}'`
fi

CORES=1
if command -v nproc &> /dev/null
then
    CORES=$(($(nproc) / HYPER))
fi


if [[ -z "${KMP_AFFINITY}" ]]; then
  KMP_AFFINITY=granularity=fine,compact
  if [[ $HYPER -eq 2 ]]; then
    KMP_AFFINITY=${KMP_AFFINITY},1,0
  fi
fi
echo "Hardware Core number ${CORES}"

#export OMP_NUM_THREADS=${CPU}
#export OMP_PROC_BIND=spread
#export KMP_AFFINITY=verbose,disabled
#export KMP_AFFINITY=verbose,granularity=fine,compact,1,0
export KMP_AFFINITY=verbose,granularity=fine,compact
#export KMP_AFFINITY=verbose,granularity=fine

usage()
{
    echo "usage:
       1. type, tf, bigdl, bigdlblas or ov, e,g., bigdl
       2. Model path, e.g., *.model, *.xml
       3. Iteration, e.g., 100
       4. Batch Size, e.g., 32
       as parameters in order. More concretely, you can run this command:
       bash run.sh \\
            bigdl \\
            /path/model \\
            100 \\
            32"
    exit 1
}


OPTIONS=""
PARM=""

if [ "$#" -lt 4 ]
then
    usage
else
    TYPE="$1"
    MODEL="$2"
    ITER="$3"
    BS="$4"
fi

case $TYPE in

  "tf" | "TF")
    echo "Analytics-Zoo with TensorFlow"
    CLASS=com.intel.analytics.zoo.benchmark.inference.TFNetPerf
    ;;

  "bigdl" | "BIGDL")
    echo "Analytics-Zoo with BigDL MKLDNN"
    CLASS=com.intel.analytics.zoo.benchmark.inference.BigDLPerf
    OPTIONS='-Dbigdl.engineType=mkldnn -Dbigdl.mklNumThreads='${CORES}
    ;;

  "bigdlblas" | "BIGDLBLAS")
    echo "Analytics-Zoo with BigDL BLAS"
    CLASS=com.intel.analytics.zoo.benchmark.inference.BigDLBLASPerf
    PARM="-c ${CORES}"
    ;;

  "ov" | "OV")
    echo "Analytics-Zoo with OpenVINO"
    CLASS=com.intel.analytics.zoo.benchmark.inference.OpenVINOPerf
    export OMP_NUM_THREADS=${CORES}
    export KMP_BLOCKTIME=20
    ;;

  *)
    echo "Analytics-Zoo with BigDL MKLDNN"
    CLASS=com.intel.analytics.zoo.benchmark.inference.BigDLPerf
    OPTIONS='-Dbigdl.engineType=mkldnn -Dbigdl.mklNumThreads='${CORES}
    ;;
esac


# for maven
JAR=target/benchmark-0.3.1-SNAPSHOT-jar-with-dependencies.jar

java ${OPTIONS} -cp ${JAR} ${CLASS} -m ${MODEL} --iteration ${ITER} --batchSize ${BS} ${PARM}

