# Graphical User Interface #



## Launching the Graphical User Interface ##

The main distribution includes a script for launching the interface,  _**runjmodeltest-gui.sh**_, located under the jModelTest home folder.

Other possibility is running the following command line:
```
$ java -jar jModelTest.jar
```

Moreover, in Windows and MacOS X, it is often possible to double-click the jModelTest.jar file to launch the graphical interface.

The following window will show on the screen:

![https://lh6.googleusercontent.com/-Dxai4JGo6dE/TomkgcDzwfI/AAAAAAAAABc/G-pIEegopuE/jmodeltest-main.png](https://lh6.googleusercontent.com/-Dxai4JGo6dE/TomkgcDzwfI/AAAAAAAAABc/G-pIEegopuE/jmodeltest-main.png)

## Menu description ##

| **Menu** | **Submenu** | **Description** | **Enabled** |
|:---------|:------------|:----------------|:------------|
| File |
|  | Load Alignment | Load an input alignment |  |
|  | Quit | Exit the program |  |
| Analysis |
|  | Compute likelihood scores | Optimize the set of candidate models | After loading an alignment |
|  | Do AIC calculations | Calculate Akaike Information Criterion | After computing the likelihood scores |
|  | Do BIC calculations | Calculate Bayesian Information Criterion | After computing the likelihood scores |
|  | Do DT calculations | Calculate Decision Theory | After computing the likelihood scores |
|  | Do hLRT calculations | Calculate hierarchical likelihood ratio test `[1]` | After computing the likelihood scores |
|  | Model-averaged phylogeny | Calculate the consensus tree | If the base tree is not fixed, after calculating an Information Criterion |
| Results |
|  | Show results table |  |  |
|  | Build HTML log |  |  |
| Tools |
|  | LRT calculator |  |  |
| Help |
  * About

`[1]` This test is only available for 3,5,7 and 11 substitution schemes and for fixed topologies (fixed BIONJ-JC tree or user-defined topology)