# jmodeltest2

jModelTest is a tool to carry out statistical selection of best-fit models of nucleotide substitution. It implements five different model selection strategies: hierarchical and dynamical likelihood ratio tests (hLRT and dLRT), Akaike and Bayesian information criteria (AIC and BIC), and a decision theory method (DT). It also provides estimates of model selection uncertainty, parameter importances and model-averaged parameter estimates, including model-averaged tree topologies. jModelTest 2 includes High Performance Computing (HPC) capabilities and additional features like new strategies for tree optimization, model-averaged phylogenetic trees (both topology and branch lenght), heuristic filtering and automatic logging of user activity.

--------
Download
--------

Check 'releases' section for the latest distributions: https://github.com/ddarriba/jmodeltest2/releases.

--------
NEWS!
--------

03/03/2016 - New revision fixed bug with equal taxa name prefixes. Fixed minor bugs.

20/02/2015 - New revision Fixed minor bug in console version. NNI tree search operation was taken as default for ML starting tree, instead of BEST.

20/11/2014 - New revision Fixed some bugs with Windows OS

03/09/2014 - New revision Added --set-local-config and --set-property arguments.

26/08/2014 - New revision Fixed bug with -O argument (hypothesis order for hLRT).

06/08/2014 - New revision Added checkpointing feature and confirmation on run cancel.

05/04/2014 - New revision Added PAUP* block to log files. Fixed bug with DT criterion. Check the whole revision updates at the wiki.

05/06/2013 - New revision Fixed bug with PAUP* block. Added argument for forwarding standard output to a file.

03/01/2013 - New revision Fixed bug with paths including whitespaces. New binaries distribution also includes updated PhyML binaries with a much better performance.

01/08/2012 - The jModelTest paper has been published: Darriba D, Taboada GL, Doallo R, Posada D. 2012. jModelTest 2: more models, new heuristics and parallel computing. Nature Methods 9: 772. Although the main text is quite short, it comes with 15 pages of supplementary material!

--------
Citation
--------

When using jModelTest you should cite all these:

Darriba D, Taboada GL, Doallo R, Posada D. 2012. jModelTest 2: more models, new heuristics and parallel computing. Nature Methods 9(8), 772.

Guindon S and Gascuel O (2003). A simple, fast and accurate method to estimate large phylogenies by maximum-likelihood". Systematic Biology 52: 696-704.

--------
Discussion group
--------

Please use the jModelTest discussion group for any question: http://groups.google.com/group/modeltest-ng

--------
Disclaimer
--------

This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version. This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA. The jModelTest distribution includes Phyml executables.

These programs are protected by their own license and conditions, and using jModelTest implies agreeing with those conditions as well. 
