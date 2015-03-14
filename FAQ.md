# Frequently Asked Questions #

### I got a Class Format Error when running jModelTest ###

If you get a `java.lang.ClassFormatError`, it probably means that you have a non compatible Java Runtime Environment (JRE) version. jModelTest 2 binaries are compiled with Java 1.6, so you need at least a JRE 1.6. You can update your JRE, or you can download the source code of jModelTest and build the tool with your JDK.

### jModelTest 2 GUI crashes in Windows when attempting to compute the likelihood scores ###

If you can open the Graphical User Interface but it suddenly closes when you attempt to compute the likelihood scores, probably JMT is located in a path that includes white spaces, like "C:\Documents And Settings\...". We are fixing this bug, but meanwhile you can move the jModeltest folder to C:\ or another one without white spaces.

### My alignment file is not accepted by jModelTest ###

One of the most common causes for this error is that there exist single-character taxon names. Just by adding one more is enough for fixing this.

e.g, (1,2,3,...,12,13,14) could be replaced by (01,02,03,...,12,13,14) or (T1,T2,T3,...,T12,T13,T14)

Otherwise, you can try to convert your alignment into any other format with [ALTER](http://sing.ei.uvigo.es/ALTER/), the tool jModelTest uses for converting the alignments.

### I want to use the best-fit model into `MrBayes`. What should I do? ###

As `MrBayes` does not implement all models supported in jModelTest, you should select the 3 substitution schemes option in the "Likelihood Settings" window (right before computing the likelihoods).

If you have already performed the selection with a higher number of substitution schemes, you can choose the best one implemented in `MrBayes`. Criterion weights might change and maybe there is another model with a better fitness, but criterion values would remain the same.