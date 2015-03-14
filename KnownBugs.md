# Known Bugs #

  * PhyML error in Windows execution.
> > jModelTest attempts to read the optimization results from an incorrect file. This error was fixed.

  * The program crashes and suddenly closes in Windows when attempting to compute the likelihood scores if jModelTest 2 is installed in a path with white spaces (e.g. C:\Documents And Settings\...).
> > You should move the folder to another one without white spaces (e.g. C:\jModelTest)