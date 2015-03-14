# Getting Started #

jModelTest 2 is a high performance Java tool for nucleotide substitution model selection. It comes in three flavours depending on the architecture used and the user preferences.

This guide provides information on:



## Basic features ##

## Operating Systems ##

Since jModelTest is a Java application, it can be used in every OS that can execute a Java Runtime Environment (JRE). The most common Operating Systems and many other include a JRE (OpenJDK, Sun JRE, ...), or at least it is possible to download one. However, jModelTest depends on third-party binaries (PhyML), that are distributed for Windows, Linux and OsX, and it is even possible to download PhyML sources (http://code.google.com/p/phyml) and compile them for a particular architecture.

## Working with the repository ##

This tool is distributed under GPL v3 license. The source code is freely available at google code repository. You can checkout the repository at http://code.google.com/p/jmodeltest2/source.

## User interfaces ##

jModelTest can be executed from two different user interfaces, GUI or Console. The Graphical User Interface (GUI) is intended for execution on common desktop computers with multicore processors -most users will probably use this. On the other hand, HPC environments, like multicore clusters, require a non-interactive processing (batch processes), so jModelTest has to be executed from the Command Console Interface. Results are given in plain text format, but an html log is also created.

### Graphical User Interface ###

See the [Quick Start (Graphical User Interface)](QuickStartGUI.md) Section, or the [Graphical User Interface](GUI.md) tutorial for details.

### Command Console Interface ###

See the [Quick Start (Console)](QuickStartConsole.md) Section, or the [Application Arguments](ApplicationArguments.md) reference for details.

## High Performance Environments ##

### Shared memory architectures (multicore systems) ###

Both the GUI and Console interfaces can be used for shared memory architectures. See [Graphical User Interface](GettingStarted#Graphical_User_Interface.md) or [Command Console Interface](GettingStarted#Command_Console_Interface.md). In some dedicated HPC environments you can only use the console interface, for example when using a bath-queuing system like Oracle Grid Engine. Additionally, in the  console version you can specify the number of threads you want to use using the **-tr" option. By default, the total number of cores in the machine is used.**

### Distributed memory architectures (HPC clusters) ###

1. Besides the multithreading support, it is possible to run jModelTest in a cluster. This feature has been implemented using a Java message-passing (MPJ) library, MPJ Express (http://mpj-express.org/). To execute jModelTest in a cluster environment you have to:

```
$ export $JMODELTEST_HOME=[path_to_jModelTest]
$ cd $JMODELTEST_HOME
$ tar zvxf mpj.tar.gz
$ export MPJ_HOME=$JMODELTEST_HOME/mpj
$ export PATH=$MPJ_HOME/bin:$PATH
$ cp $JMODELTEST_HOME/extra/machines $JMODELTEST_HOME
```

You can also add the last two lines to ~/.bashrc to automatically set these variables at console startup.

2. $JMODELTEST\_HOME/machines file contains the set of computing nodes where the mpj processes will be executed. By default it points to the localhost machine, so you should change it if you want to run a parallel execution over a cluster machine, just writing on each line the particular computing nodes (e.g. see filecluster8.conf.template).

3. Start the MPJ Express daemons:
```
$ mpjboot machines
```
The application "mpjboot" should be in the execution path (it is located at $MPJ\_HOME/bin). A ssh service must be running in the machines listed in the machines file. Moreover, port 10000 should be free. For more details refer to the MPJ Express documentation.

4. Run jModelTest. For this, the jModelTest distribution provides a bash script: 'runjmodeltest-cluster.sh'

The basic syntax is:
> ./runjmodeltest-cluster.sh $NUMBER\_OF\_PROCESSORS $APPLICATION\_PARAMETERS
```
   $ ./runjmodeltest-cluster.sh 2 -d example-data/aP6.fas -s 11 -i -g 4 -f -AIC -a
```