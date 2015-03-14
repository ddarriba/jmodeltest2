# What's new in jModelTest2? #

## High Performance Computing ##

The most important difference between jModelTest2 and the previous versions is the High Performance Computing (HPC) implementation. jModelTest2 is now capable to efficiently exploit current multicore desktop computers, while HPC clusters provide jModelTest2 with the highest speedup. The Message-Passing parallel implementation scales, in most common cases, up to half the total number of the candidate models. The total execution time can be improved even further to a lesser extent, up to the total number of candidate models.

## Exhaustive GTR submodels ##

All the 203 different partitions of the GTR rate matrix can be included in the candidate set of models. When combined with rate variation (+I,+G, +I+G) and equal/unequal  base frequencies the total number of possible models is 203 x 8 = 1624.

## Hill climbing hierarchical clustering ##

Calculating the likelihood score for a large number of models can be extremely time-consuming. This hill-climbing algorithm implements a hierarchical clustering to search for the best-fit models within the full set of 1624 models, but optimizing at most 288 models while maintaining model selection accuracy.

## Heuristic filtering ##

Heuristic reduction of the candidate models set based on a similarity filtering threshold among the GTR rates and the estimates of among-site rate variation.

## Absolute model fit ##

Information criterion distances can be calculated for the best-fit model against the unconstrained multinomial model (based on site pattern frequencies). This is computed by default when the alignment does not contain missing data/ambiguities, but can also be approximated otherwise.

## Topological summary ##

Tree topologies supported by the different candidate models are summarized in the html log, including confidence intervals constructed from cumulative models weights, plus Robinson-Foulds and Euclidean distances to the best-fit tree for each.

## Phylogenetic Averaging ##

jModelTest2 does not depend on external applications for phylogenetic averaging anymore. The native implementation allows now more flexibility for displaying intermediate results, as split support. Also different options can be taken into account, like the consensus threshold or the computation of  consensus branch lengths (weighted median or weighted average).

## Multiformat Input Data ##

jModelTest2 uses ALTER API for MSA format conversion, supporting now ALN, FASTA, GDE, MSF, NEXUS, PHYLIP and PIR formats.