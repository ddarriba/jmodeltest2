# Application Arguments for the Command Console version #

WARNING: This section might be outdated. Please check the tool help (run with -help argument) or the pdf manual instead.

| **Arg** | **Values** | **Default** | **Description** |
|:--------|:-----------|:------------|:----------------|
| -a | - | False | Estimate model-averaged phylogeny for each active criterion (e.g., -a) |
| -AIC | - | False | Calculate the Akaike Information Criterion (e.g., -AIC) |
| -AICc  | - | False | Calculate the corrected Akaike Information Criterion (e.g., -AICc) |
| -BIC | - | False | Calculate the Bayesian Information Criterion (e.g., -BIC) |
| -DT | - | False | Calculate the decision theory criterion (e.g., -DT) |
| -c | {0.0..1.0} | 1.0 | Confidence interval (e.g., -c 0.9) |
| -ckp | CHECKPOINT\_FILENAME | - | Loads a checkpointing file |
| -d | Input Data | - | Input data file (e.g., -d data.phy) (Required) |
| -dLRT | - | False | Do dynamical likelihood ratio tests (e.g., -dLRT)|
| -f | - | False | Include models with unequals base frecuencies (e.g., -f) |
| -g | NCat | False | Include models with rate variation among sites and number of categories (e.g., -g 4) |
| -G | {0.0..1.0} | None | Heuristic search. Requires a threshold > 0 (e.g., -G 0.1) |
| -h | {0.0..1.0} | 0.1 | Confidence level for the hLRTs (e.g., -a 0.002)|
| -help |  |  | Displays a help message |
| -H | [AIC,AICc,BIC] | BIC | Information criterion for clustering search (AIC, AICc, BIC). (e.g., -H AIC) |
| -hLRT | - | False | Do hierarchical likelihood ratio tests |
| -i | - | False | Include models with a proportion invariable sites (e.g., -i) |
| -machinesfile | MACHINES FILE | - | Gets the processors per host from a machines file. Just for thread scheduling parallel algorithm |
| -n | Sample size | Number of sites | Sample size (e.g., -n 235) |
| -o | OUTPUT\_FILE |  | Redirects the output to a file |
| -O |  |  | Hypothesis order for the hLRTs (e.g., -hLRT gpftv) (default is ftvgp/ftvwgp/ftvwxgp) |
|  |  |  | f=freq, t=titvi, v=2ti4tv(subst=3)/2ti(subst>3), w=2tv, x=4tv, g=gamma, p=pinv |
| -p | - | False |  Calculate parameter importances (e.g., -p)|
| -r | - | False | Backward selection for the hLRT (e.g., -r) (default is forward) |
| -s | {3,5,7,11,203} | 3 | Number of substitution schemes (e.g., -s 11) |
| --set-local-config | LOCAL\_CONF\_FILE | - | Sets a local configuration file in replacement of conf/jmodeltest.conf |
| --set-property | PROP\_NAME=PROP\_VALUE | - |Sets a new value for a property contained in the configuration file (conf/jmodeltest.conf) |
| -S | {NNI, SPR, BEST} | NNI | Tree topology search operation option: NNI (fast), SPR (a bit slower), BEST (best of NNI and SPR) |
| -t | {fixed, BIONJ, ML} | ML | Base tree for likelihood calculations (fixed (BIONJ-JC), BIONJ or ML) (e.g., -t BIONJ) (default is ML) |
| -tr | N | Machine cores | Number of threads to execute (e.g., -tr 2) |
| -u | User Tree | - | User tree for likelihood calculations  (e.g., -u data.tre)|
| -uLnL | - | False | Force calculation of delta AIC,AICc,BIC against unconstrained likelihood in case the alignment has gaps or ambiguities (e.g., -uLnL) |
| -v | - | False | Do model averaging and parameter importances (e.g., -v)|
| -w | - | False | Write PAUP block (e.g., -w) |
| -z | - | False | Strict consensus type for model-averaged phylogeny (e.g., -z) |