

# Performance Analysis #

The performance of jModelTest2 was evaluated on a representative multi-core cluster
(see below) under two different scenarios:
**shared memory**, using the available cores in a machine.
**distributed memory**, running the message-passing version on up to 88 cores in a cluster (88 is the number of available substitution models).

## Benchmark Description ##

We used as test cases four simulated multiple sequence alignments consisting of 50 and 100 sequences with 1,000 and 10,000 nucleotides. In all cases the set of candidate models included all 88 available DNA substitution models. The implemented task-based workload distribution is limited by the maximum number of substitution models to be computed, so currently our implementation can not take advantage of the use of more than 88 processes. Moreover, the high variability of  runtimes among models also represents a significant source of performance penalty, as the unbalance of the workload can delay the completion of the execution due to the computation of a few costly models while most of the processing elements are already idle.

| **MSA name** | **# Sequences** | **Length** | **Base Tree** | **Sequential Runtime** |
|:-------------|:----------------|:-----------|:--------------|:-----------------------|
| 50x1K |50 |1,000 | Fixed BIONJ | 00:15:19 |
| 100x1K |100 |1,000 | Fixed BIONJ | 00:43:31 |
| 50x10K |50 |10,000 | Fixed BIONJ | 02:04:15 |
| 100x10K |100 |10,000 | Fixed BIONJ | 06:14:41 |
| 50x1K |50 |1,000 | ML Tree | 00:57:11 |
| 100x1K |100 |1,000 | ML Tree | 02:33:32 |
| 50x10K |50 |10,000 | ML Tree | 07:53:42 |
| 100x10K |100 |10,000 | ML Tree | 22:12:45 |

Furthermore, even the optimization times of the models with common rate parameters (e.g., “+I”, “+I+G”) present significant differences, which, together with the fact
that we are not able to estimate a priori this time, contribute to the existence of a performance bottleneck as the number of cores used increases. In order to reduce this overhead we implemented a heuristic consisting of increasing the priority of the most complex models. This leads to an increasing work imbalance as the number of processes increases (i.e, the less models per process, the less probability the work is balanced). Thus, the scalability using shared or distributed memory is limited by the replacement models with the highest computational load, which can represent more than 80% of the overall runtime. In these cases, the runtime is determined by the longest optimization, even if the execution is prioritized efficiently using the best selection heuristic. In fact, the execution of the entire set of 88 candidate models is expected to provide a scalability limit around 40, as the longest optimization is usually twice the mean model time. Moreover, the higher the number of cores, the higher the workload imbalance due to the runtime differences. In fact, our program usually can take advantage of only up to 55 cores, approximately.

## Experimental Configuration ##

The distributed memory testbed, a Harpertown-based cluster, consists of 32 nodes, each of them with 16 GB of RAM and 2 Intel Xeon E5420 quad-core Harpertown processors. The interconnection networks are [InfiniBand](http://en.wikipedia.org/wiki/InfiniBand) (DDR 4X: 16 Gbps of maximum theoretical bandwidth), with OFED driver 1.5.1, and Gigabit Ethernet (1 Gbps). The OS is CentOS 5.3, the C compiler is gcc 4.1.2, the JVM is Sun 1.6.0 13, and the Java message-passing libraries are MPJ Express 0.27 (for distributed memory benchmarking) and F-MPJ 0.1 (for hybrid memory benchmarking).

Two shared memory testbeds were used. The first one (Nehalem) has 8 GB of RAM and 2 Intel Xeon E5520 quad-core Nehalem processors (hence 8 cores), with hyper-threading enabled. The OS is CentOS 5.3, the C compiler is gcc 4.1.2, the JVM is Sun 1.6.0 13. The second one (Opteron node) has 32 GB of RAM and 2 AMD Opteron 6174 12-core processors (hence 24 cores). The C compiler is gcc 4.1.2 and the JVM is Sun 1.6.0 20.

When using distributed memory the processes were distributed evenly among all the available nodes (e.g., a 64-core execution on 32 nodes uses 2 cores per node). In this case the influence of the interconnection network was usually negligible (< 1% runtime overhead) due to the computationally intensive nature of the application with respect to the communications required (ML optimization accounts for nearly all the execution time). We defined speedup as Tseq/Tn, where Tseq is the time required by the sequential execution, and Tn is the time required by jModelTest 2 running with n cores.