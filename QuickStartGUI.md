# Quick Start (Graphical User Interface) #

1. Execute the script for the Graphical User Interface (runjmodeltest-gui.sh). The main jModelTest frame should pop up on the screen:

![https://lh6.googleusercontent.com/-Dxai4JGo6dE/TomkgcDzwfI/AAAAAAAAABc/G-pIEegopuE/jmodeltest-main.png](https://lh6.googleusercontent.com/-Dxai4JGo6dE/TomkgcDzwfI/AAAAAAAAABc/G-pIEegopuE/jmodeltest-main.png)

2. Load an input alignment file using the **File/Load Alignment** option.

3. Go to **Analysis/Compute Likelihood Scores** and select the candidate models and the options for model optimization (optionally you can set a base topology from a file). Press Enter or the "Compute Likelihoods" button.

![https://lh6.googleusercontent.com/-Ovlfd22EOYU/Tom1vLOQV9I/AAAAAAAAABs/m0X1AAvEAu0/options.png](https://lh6.googleusercontent.com/-Ovlfd22EOYU/Tom1vLOQV9I/AAAAAAAAABs/m0X1AAvEAu0/options.png)

4. Perform statistical selection among the optimized models. For example, we can calculate the Bayesian Information Criterion using **Analysis/Do BIC calculations...** option, or any other. You can find a Criteria comparison in terms of accuracy in the [supplementary material](http://www.nature.com/nmeth/journal/v9/n8/extref/nmeth.2109-S1.pdf) of the [jModelTest publication](http://www.nature.com/nmeth/journal/v9/n8/full/nmeth.2109.html).

![https://lh6.googleusercontent.com/-JXWVuR8mfz4/TonOeq5Kj2I/AAAAAAAAACc/CRoKPSez5d0/s505/compute-bic.png](https://lh6.googleusercontent.com/-JXWVuR8mfz4/TonOeq5Kj2I/AAAAAAAAACc/CRoKPSez5d0/s505/compute-bic.png)

The results will be shown in the main console.

5. Take a look at the results table in **Results/Show results table**. Best model is the one with the lowest criterion value (BIC column in the example) and therefore delta = 0.

![https://lh3.googleusercontent.com/-rO5P4wwojgM/TonOgd4QREI/AAAAAAAAAC8/n1U614gWPJA/results-table2.png](https://lh3.googleusercontent.com/-rO5P4wwojgM/TonOgd4QREI/AAAAAAAAAC8/n1U614gWPJA/results-table2.png)

6. Build a consensus tree from a given selection criteria using **Analysis/Model-averaged phylogeny**:

![https://lh5.googleusercontent.com/-eLM5rbQopmw/TonOfmUh4zI/AAAAAAAAACo/_790OgVU5c4/consensus.png](https://lh5.googleusercontent.com/-eLM5rbQopmw/TonOfmUh4zI/AAAAAAAAACo/_790OgVU5c4/consensus.png)

7. Finally, you can save the results displayed in the main console using **Edit/Save console**. Alternatively, you can get a formatted HTML document using **Results/Build HTML log**:

![https://lh5.googleusercontent.com/-hsV5OKS2qzE/TonRDVESDAI/AAAAAAAAADQ/Ux1huFna6l8/s576/html-log.png](https://lh5.googleusercontent.com/-hsV5OKS2qzE/TonRDVESDAI/AAAAAAAAADQ/Ux1huFna6l8/s576/html-log.png)

Take a look at the [Graphical User Interface](GUI.md) tutorial for further information.