**jModelTest** is a tool to carry out statistical selection of best-fit models of nucleotide substitution. It implements five different model selection strategies: hierarchical and dynamical likelihood ratio tests (hLRT and dLRT), Akaike and Bayesian information criteria (AIC and BIC), and a decision theory method (DT). It also provides estimates of model selection uncertainty, parameter importances and model-averaged parameter estimates, including model-averaged tree topologies. **jModelTest** 2 includes High Performance Computing (HPC) capabilities and additional features like new strategies for tree optimization, model-averaged phylogenetic trees (both topology and branch lenght), heuristic filtering and automatic logging of user activity.


---

# Download #

Google Code downloads are now longer available. New distributions of jModelTest will be hosted in google drive: [jModelTest Downloads](https://drive.google.com/folderview?id=0ByrkKOPtF_n_OUs3d0dNcnJPYXM#list)

In case you don't know how to proceed, please check the [Download Instructions](Download.md) in the wiki.



---

**NEWS!**

_20/02/2015_ - [New revision](https://code.google.com/p/jmodeltest2/wiki/Updates)
Fixed minor bug in console version. NNI tree search operation was taken as default for ML starting tree, instead of BEST.

_20/11/2014_ - [New revision](https://code.google.com/p/jmodeltest2/wiki/Updates)
Fixed some bugs with Windows OS

_03/09/2014_ - [New revision](https://code.google.com/p/jmodeltest2/wiki/Updates)
Added --set-local-config and --set-property arguments.

_26/08/2014_ - [New revision](https://code.google.com/p/jmodeltest2/wiki/Updates)
Fixed bug with -O argument (hypothesis order for hLRT).

_06/08/2014_ - [New revision](https://code.google.com/p/jmodeltest2/wiki/Updates)
Added checkpointing feature and confirmation on run cancel.

_05/04/2014_ - [New revision](https://code.google.com/p/jmodeltest2/wiki/Updates)
Added PAUP`*` block to log files. Fixed bug with DT criterion. Check the whole revision updates at the wiki.

_05/06/2013_ - [New revision](https://code.google.com/p/jmodeltest2/wiki/Updates)
Fixed bug with PAUP`*` block. Added argument for forwarding standard output to a file.

_03/01/2013_ - [New revision](https://code.google.com/p/jmodeltest2/wiki/Updates)
Fixed bug with paths including whitespaces. New binaries distribution also includes updated PhyML binaries with a much better performance.

_01/08/2012_ - The jModelTest paper has been published: Darriba D, Taboada GL, Doallo R, Posada D. 2012. jModelTest 2: more models, new heuristics and parallel computing. [Nature Methods 9: 772.](http://www.nature.com/nmeth/journal/v9/n8/full/nmeth.2109.html) Although the main text is quite short, it comes with 15 pages of supplementary material!


---

# Program Documentation #

[Getting Started](http://code.google.com/p/jmodeltest2/wiki/GettingStarted)


---

# Citation #
When using jModelTest you should cite all these:

Darriba D, Taboada GL, Doallo R, Posada D. 2012. jModelTest 2: more models, new heuristics and parallel computing. [Nature Methods 9(8), 772.](http://www.nature.com/nmeth/journal/v9/n8/full/nmeth.2109.html)

Guindon S and Gascuel O (2003). A simple, fast and accurate method to estimate large phylogenies by maximum-likelihood". Systematic Biology 52: 696-704.


---

# Discussion group #

Please use the [jModelTest discussion group ](http://groups.google.com/group/jmodeltest) for any question.


---


# Disclaimer #

This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version. This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA. The jModelTest distribution includes Phyml executables.

These programs are protected by their own license and conditions, and using jModelTest implies agreeing with those conditions as well.