# Updates #

## _20 Feb 2015_ Version 2.1.7 rev 20150220 ##

  * Fixed minor bug in console version. NNI tree search operation was taken as default for ML starting tree, instead of BEST.

## _20 Nov 2014_ Version 2.1.7 rev 20141120 ##

  * Fixed bug with special characters in paths
  * Added initial check of PhyML binaries
  * [Added notification in case AICc produces negative values](https://code.google.com/p/jmodeltest2/issues/detail?id=30)
  * Allow for environment variables on configuration file

## _03 Sep 2014_ Version 2.1.6 rev 20140903 ##

  * Added **--set-local-config** and **--set-property** arguments (check the pdf manual for more info)
  * Added full revision number in the main header


## _26 Aug 2014_ Version 2.1.6 rev 20140826 ##

  * Fixed bug with -O argument (hypothesis order for hLRT)
  * Updated help messages

## _06 Aug 2014_ Version 2.1.6 ##

  * Added confirmation window when cancelling running jobs in the GUI
  * Added automatic checkpointing files generation
  * Added **"-ckp"** argument for loading checkpointing files

## _05 Apr 2014_ Version 2.1.5 ##

  * [Updated OS X binary](http://code.google.com/p/jmodeltest2/issues/detail?id=27)
  * [Fixed bug with computation of JC model for "fixed" topology](http://code.google.com/p/jmodeltest2/issues/detail?id=25&can=1)
  * [Fixed bug with DT criterion computation](http://code.google.com/p/jmodeltest2/issues/detail?id=26)
  * Added **"-n"** argument for naming executions (the name is included in the log filenames)
  * Added **"-getphylip"** argument for converting alignments into **PHYLIP** format with **ALTER**
  * Fixed bug in PhyML logging in GUI. Added a unique ID for every model in the log file
  * Added PAUP`*` block into log files if required ("-w" argument)
  * Added more verbose error messages

## _10 Jul 2013_ Version 2.1.4 ##

  * Added phyml **auto-logging**.
  * Added phyml command lines for best-fit models.
  * Added phyml log tab in the GUI.
  * Removed sample size modes (and "-n" argument). Sample size is fixed to **alignment size**.
  * Fixed bug with relative paths when calling from a different path.
  * Fixed typos in the GUI.

## _05 Mar 2013_ Version 2.1.3 ##

  * [Fixed bug with PAUP`\*` command block.](http://code.google.com/p/jmodeltest2/issues/detail?id=17)
  * [Added the possibility to change Inforation Criterion used with the clustering algorithm for the 203 matrices.](http://code.google.com/p/jmodeltest2/issues/detail?id=18)
  * Changed **"-o"** argument for the hypothesis order into **"-O"**
  * [Added \*"-o"\* argument for forwarding the standard output to a file: -o FILENAME](http://code.google.com/p/jmodeltest2/issues/detail?id=15)

## _01 Jan 2013_ Version 2.1.2 ##
[Revision 20130103](http://code.google.com/p/jmodeltest2/downloads/detail?name=jmodeltest-2.1.2-20130103.tar.gz)

  * Fixed bug in paths with whitespaces.
  * Updated PhyML binaries.

## _31 Jul 2012_ Version 2.1.1 ##

[Revision 20120731](http://code.google.com/p/jmodeltest2/downloads/detail?name=jmodeltest-2.1.1-20120727.tar.gz)

  * Fixed bug with hLRT selection when attempting to use a user-defined topology.

## _11 Mar 2012_ Version 2.1 ##

[Revision 20120511](http://code.google.com/p/jmodeltest2/downloads/detail?name=jmodeltest-2.1-20120511.tar.gz)
### Major updates: ###

  * **Exhaustive GTR submodels:** All the 203 different partitions of the GTR rate matrix can be included in the candidate set of models. When combined with rate variation (+I,+G, +I+G) and equal/unequal  base frequencies the total number of possible models is 203 x 8 = 1624.

  * **Hill climbing hierarchical clustering:** Calculating the likelihood score for a large number of models can be extremely time-consuming. This hill-climbing algorithm implements a hierarchical clustering to search for the best-fit models within the full set of 1624 models, but optimizing at most 288 models while maintaining model selection accuracy.

  * **Heuristic filtering:** Heuristic reduction of the candidate models set based on a similarity filtering threshold among the GTR rates and the estimates of among-site rate variation.

  * **Absolute model fit:** Information criterion distances can be calculated for the best-fit model against the unconstrained multinomial model (based on site pattern frequencies). This is computed by default when the alignment does not contain missing data/ambiguities, but can also be approximated otherwise.

  * **Topological summary:** Tree topologies supported by the different candidate models are summarized in the html log, including confidence intervals constructed from cumulative models weights, plus Robinson-Foulds and Euclidean distances to the best-fit tree for each.

### Minor updates: ###

  * Corrected a bug in the fixed BIONJ-JC starting topology. F81+I+G was executed instead of JC.
  * "Best" is now the default tree search operation instead of NNI. "Best" computes both NNI and SPR algorithms and selects the best of them.
  * User can select the number of threads from GUI.

## _1 Feb 2012_ Version 2.0.2 ##

[Revision 20120201](http://code.google.com/p/jmodeltest2/downloads/detail?name=jmodeltest-2.0.2-20120201.tar.gz)

  * Added a **selection summary** at the end of the console output.
  * Corrected the table header in the DT results frame (sorting).
  * Corrected a bug in DT Criterion where selection could not take place with large alignments.
  * Corrected a bug with command console version, where the execution crashed with certain arguments.
  * Unified LOCALE for English format.

## _2 Nov 2011_ Version 2.0.1 ##

  * Improved thread scheduling algorithm.
  * OpenMP phyml patch for hybrid execution.
  * New argument (machinesfile) for hybrid execution on heterogeneous architectures, or heterogeneous resources distribution.

## _13 Oct 2011_ ##

[Revision 20111013](http://code.google.com/p/jmodeltest2/downloads/detail?name=jmodeltest-2.0-20111013.tar.gz)

  * Added **_conf/jmodeltest.conf_** file, where you can:
    * Enable/Disable the automatic logging:
> > > You might be running a huge dataset and you don't want to generate hundreds or thousands of log files.
    * Set the PhyML binaries location:
> > > If you already have installed PhyML in your machine, you can setup jModelTest for use your own binaries.
  * Enhanced the html log output.

## _27 Sep 2011_ ##

[Revision 20110927](http://code.google.com/p/jmodeltest2/downloads/detail?name=jmodeltest-2.0-20110927.tar.gz)

  * See [What's new in jModelTest 2](NewFeatures.md) for learning about the features included in this version.