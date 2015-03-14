# Quick Start (Console interface) #

1. Execute the following command line:
```
$ java -jar jModelTest.jar -d example-data/aP6.fas -g 4 -i -f -AIC -BIC -a
```

This will test all 88 models (gamma models with 4 rate categories), and then perform the model selection using Akaike (AIC) and Bayesian (BIC) criteria, calculating also a model averaged phylogeny (-a).

See [Application Arguments](ApplicationArguments.md) for information about supported arguments.

2. This will generate the following output:

a) Header:

```
----------------------------- jModeltest 2.0 -----------------------------
(c) 2011-onwards Diego Darriba, David Posada,
Department of Biochemistry, Genetics and Immunology
University of Vigo, 36310 Vigo, Spain. e-mail: ddarriba@udc.es, dposada@uvigo.es
--------------------------------------------------------------------------------
Wed Oct 05 12:56:47 CEST 2011
Linux 2.6.38-11-generic-pae, arch: i386, bits: 32, numcores: 2

jModelTest 2.0  Copyright (C) 2011 Diego Darriba, David Posada
This program comes with ABSOLUTELY NO WARRANTY
This is free software, and you are welcome to redistribute it
under certain conditions
 
Notice: This program may contain errors. Please inspect results carefully.
```

b) Execution options:

```
Arguments = -d example-data/aP6.fas -g 4 -i -f -AIC -BIC -a

Reading data file "aP6.fas"... OK.
  number of sequences: 6
  number of sites: 631
 
 
---------------------------------------------------------------
*                                                             *
*        COMPUTATION OF LIKELIHOOD SCORES WITH PHYML          *
*                                                             *
---------------------------------------------------------------
 
::Settings::
 Phyml version = 3.0
 Phyml binary = PhyML_3.0_linux32
 Candidate models = 24
  number of substitution schemes = 3
  including models with equal/unequal base frequencies (+F)
  including models with/without a proportion of invariable sites (+I)
  including models with/without rate variation among sites (+G) (nCat = 4)
 Optimized free parameters (K) = substitution parameters + 9 branch lengths + topology 
 Base tree for likelihood calculations = ML tree
 Tree topology search operation = NNI
computing likelihood scores for 24 models with Phyml 3.0
```

c) Real time optimization results (progress):

```
::Progress::

Model 		 Exec. Time 	 Total Time 	 -lnL
-------------------------------------------------------------------------
JC		00h:00:00:01	00h:00:00:01	1114,9772
JC+G		00h:00:00:04	00h:00:00:05	1106,4431
...
GTR+G		00h:00:00:06	00h:00:06:07	1054,7203
GTR+I+G		00h:00:01:02	00h:00:07:05	1051,8403
```

d) Sorted and complete optimization results:

```
   Model = JC
   partition = 000000
   -lnL = 1114.9772
   K = 10 
 
   Model = JC+I
   partition = 000000
   -lnL = 1103.1113
   K = 11
   p-inv = 0.9080 

...

   Model = GTR+I+G
   partition = 012345
   -lnL = 1051.8403
   K = 20
   freqA = 0.4235 
   freqC = 0.1520 
   freqG = 0.2022 
   freqT = 0.2224 
   R(a) [AC] =  0.8709
   R(b) [AG] =  0.4152
   R(c) [AT] =  0.6049
   R(d) [CG] =  1.2523
   R(e) [CT] =  0.9482
   R(f) [GT] =  1.0000
   p-inv = 0.5940
   gamma shape = 0.0120 
 
 
Computation of likelihood scores completed. It took 00h:00:07:05.
```

e) Selected Information Criteria (best model and all models sorted according to each criterion):

```
---------------------------------------------------------------
*                                                             *
*             AKAIKE INFORMATION CRITERION (AIC)              *
*                                                             *
---------------------------------------------------------------
 
 Model selected: 
   Model = F81+I
   partition = 000000
   -lnL = 1053.5428
   K = 14
   freqA = 0.4200 
   freqC = 0.1558 
   freqG = 0.2015 
   freqT = 0.2227 
   p-inv = 0.9030 
 
ML tree (NNI) for the best AIC model = (((P5:0.01021829,P4:0.00719757):0.00151199,(P6:0.00680664,P1:0.00000003):0.00204596):0.01267608,P3:0.01665876,P2:0.00459802);
 
 
* AIC MODEL SELECTION : Selection uncertainty
 
Model             -lnL    K         AIC      delta      weight cumWeight
------------------------------------------------------------------------ 
F81+I        1053.5428   14   2135.0855     0.0000      0.4332    0.4332 
HKY+I        1053.0700   15   2136.1401     1.0545      0.2557    0.6890 
F81+I+G      1053.5430   15   2137.0859     2.0004      0.1594    0.8483
...
K80          1114.5049   11   2251.0098   115.9243   2.91e-026    1.0000 
SYM          1114.4117   15   2258.8235   123.7380   5.85e-028    1.0000
------------------------------------------------------------------------
-lnL:	negative log likelihod
 K:	number of estimated parameters
 AIC:	Akaike Information Criterion
 delta:	AIC difference
 weight:	AIC weight
 cumWeight:	cumulative AIC weight
 
 
* AIC MODEL SELECTION : Confidence interval
 
There are 24 models in the 100% confidence interval: [ F81+I HKY+I F81+I+G HKY+I+G F81+G GTR+I HKY+G GTR+I+G GTR+G F81 HKY GTR JC+I K80+I JC+I+G K80+I+G JC+G K80+G SYM+I SYM+I+G SYM+G JC K80 SYM ] 
```

f) Consensus tree of the optimized phylogenies using the criterion weights:

```
---------------------------------------------------------------
*                                                             *
*                    MODEL AVERAGED PHYLOGENY                 *
*                                                             *
---------------------------------------------------------------
 
Selection criterion: . . . . AIC
Confidence interval: . . . . 1.00
Consensus type:. . . . . . . 50% majority rule
 
 
Using 24 models in the 1.00 confidence interval = F81+I HKY+I F81+I+G HKY+I+G F81+G GTR+I HKY+G GTR+I+G GTR+G F81 HKY GTR JC+I K80+I JC+I+G K80+I+G JC+G K80+G SYM+I SYM+I+G SYM+G JC K80 SYM  

Bipartitions included in the consensus tree
 
    123456
    ****** ( 1.0 )
    ****-- ( 1.0 )
    **---- ( 0.94244 )
    --**-- ( 1.0 )

 
                        +-----------6 P4
                      +-8
                      | +----------------5 P5
+---------------------9
|                     |  +-4 P1
|                     +--7
|                        +----------3 P6
|
+------2 P2
|
+---------------------------1 P3

 
(P3:0.016613,P2:0.004598,((P6:0.006790,P1:0.000000)1.00:0.002046,(P5:0.010191,P4:0.007198)0.94:0.001510)1.00:0.012665);
 
Note: this tree is unrooted. Branch lengths are the expected number of substitutions per site. Labels next to parentheses represent phylogenetic uncertainty due to model selection (see documentation)
```

g) Also a HTML log is automatically stored in the **log** directory.