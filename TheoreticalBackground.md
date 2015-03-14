

# Theoretical Background #

All phylogenetic methods make assumptions, whether explicit or implicit, about the process of DNA substitution (Felsenstein 1988). Consequently, all the methods of phylogenetic inference depend on their underlying substitution models. To have confidence in inferences it is necessary to have confidence in the models (Goldman 1993b). Because of this, it makes sense to justify the use of a particular model. Statistical model selection is one way of doing this. For a review of model selection in phylogenetics see Sullivan and Joyce (2005) and Johnson and Omland (2003). The strategies includes in jModelTest include sequential likelihood ratio tests (LRTs), Akaike Information Criterion (AIC), Bayesian Information Criterion (BIC) and performance-based decision theory (DT).


## Models of nucleotide substitution ##

Models of evolution are sets of assumptions about the process of nucleotide substitution. They describe the different probabilities of change from one nucleotide to another along a phylogenetic tree, allowing us to choose among different phylogenetic hypotheses to explain the data at hand. Comprehensive reviews of model of evolution are offered elsewhere. jmodeltest implementes all 203 types of reversible substitution matrices, with when combined with unequal/equal base frequencies, gamma-distributed among-site rate variation and a proportion of invariable sites makes a total of 1624 models. Some of the models have received names (Table 1):

Table 1. Named  substitution models jModelTest2 (a few of the 1624 possible). Any of these models can include invariable sites (+I), rate variation among sites (+G), or both (+I+G).

![http://darwin.uvigo.es/pictures/named_models_jmt.png](http://darwin.uvigo.es/pictures/named_models_jmt.png)


## Sequential Likelihood Ratio Tests (sLRT) ##

In traditional statistical theory, a widely accepted statistic for testing the goodness of fit of models is the likelihood ratio test (LRT):

<wiki:gadget url="http://mathml-gadget.googlecode.com/svn/trunk/mathml-gadget.xml" border="0" up\_content="LRT=2(l\_1-l\_0)" height="15"/>

where <wiki:gadget url="http://mathml-gadget.googlecode.com/svn/trunk/mathml-gadget.xml" border="0" up\_content="l\_1" width="20" height="10"/> is the maximum likelihood under the more parameter-rich, complex model (alternative hypothesis) and <wiki:gadget url="http://mathml-gadget.googlecode.com/svn/trunk/mathml-gadget.xml" border="0" up\_content="l\_0" width="20" height="10"/> is the maximum likelihood under the less parameter-rich simple model (null hypothesis). When the models compared are nested (the null hypothesis is a special case of the alternative hypothesis) and the null hypothesis is correct, the LRT statistic is asymptotically distributed as a χ2 with q degrees of freedom, where q is the difference in number of free parameters between the two models (Kendall and Stuart 1979; Goldman 1993b). Note that, to preserve the nesting of the models, the likelihood scores need to be estimated upon the same tree. When some parameter is fixed at its boundary (p-inv, α), a mixed χ2 is used instead (Ohta 1992; Goldman and Whelan 2000). The behavior of the χ2 approximation for the LRT has been investigated with quite a bit of detail (Goldman 1993a; Goldman 1993b; Yang, Goldman, and Friday 1995; Whelan and Goldman 1999; Goldman and Whelan 2000).

## Hierarchical Likelihood Ratio Tests (hLRT) ##

Likelihood ratio tests can be carried out sequentially by adding parameters (forward selection) to a simple model (JC), or by removing parameters (backward selection) from a complex model (GTR+I+G) in a specific order or hierarchy (hLRT; see Figure below). The performance of hierarchical LRTs for phylogenetic model selection has been discussed by Posada and Buckley (2004a) .

![https://lh3.googleusercontent.com/-lNVbbGg8hBI/TrkAWteb2RI/AAAAAAAAAEM/4JD5OUlB8tw/s800/hLRT.png](https://lh3.googleusercontent.com/-lNVbbGg8hBI/TrkAWteb2RI/AAAAAAAAAEM/4JD5OUlB8tw/s800/hLRT.png)

Figure. Example of a particular forward hierarchy of likelihood ratio tests for 24 models. At any level the null hypothesis (model on top) is either accepted (A) or rejected (R). In this example the model selected is GTR+I.

## Dynamical Likelihood Ratio Tests (dLRT) ##

Alternatively, the order in which parameters are added or removed can be selected automatically. One option to accomplish this is to add the parameter that maximizes a significant gain in likelihood during forward selection, or to add the parameter that minimizes a non-significant loss in likelihood during backward selection (Posada and Crandall 2001a). In this case, the order of the tests is not specified a priori, but it will depend on the particular data.

![https://lh4.googleusercontent.com/-1fPFU_d0yzc/TrkA6q8o86I/AAAAAAAAAEg/DhuIX_Eutb4/s720/dLRT.png](https://lh4.googleusercontent.com/-1fPFU_d0yzc/TrkA6q8o86I/AAAAAAAAAEg/DhuIX_Eutb4/s720/dLRT.png)

Figure. Dynamical likelihood ratio tests for 24 models. At any level a hypothesis is either accepted (A) or rejected (R). In this example the model selected is GTR+I. Hypotheses tested are: F = base frequencies; S = substitution type; I = proportion of invariable sites; G = gamma rates.

## Information Criteria ##
### Akaike Information Criterion ###

The Akaike information criterion (AIC, (Akaike 1974) is an asymptotically unbiased estimator of the Kullback-Leibler information quantity (Kullback and Leibler 1951). We can think of the AIC as the amount of information lost when we use a specific model to approximate the real process of molecular evolution. Therefore, the model with the smallest AIC is preferred. The AIC is computed as:

<wiki:gadget url="http://mathml-gadget.googlecode.com/svn/trunk/mathml-gadget.xml" border="0" up\_content="AIC=-2l+2k" height="15"/>

where l is the maximum log-likelihood value of the data under this model and Ki is the number of free parameters in the model, including branch lengths if they were estimated _de novo_. When sample size (n) is small compared to the number of parameters (say, n/K < 40) the use of a second order AIC, AICc (Sugiura 1978; Hurvich and Tsai 1989), is recommended:

<wiki:gadget url="http://mathml-gadget.googlecode.com/svn/trunk/mathml-gadget.xml" border="0" up\_content="AIC\_c=AIC+(2k(k+1))/(n-k-1)" height="15"/>

The AIC compares several candidate models simultaneously, it can be used to compare both nested and non-nested models, and model-selection uncertainty can be easily quantified using the AIC differences and Akaike weights (see Model uncertainty below). Burnham and Anderson (2003) provide an excellent introduction to the AIC and model selection in general.

### Bayesian Information Criterion ###

An alternative to the use of the AIC is the Bayesian Information Criterion (BIC) (Schwarz 1978):

<wiki:gadget url="http://mathml-gadget.googlecode.com/svn/trunk/mathml-gadget.xml" border="0" up\_content="BIC=-2l + k log(n)" height="15"/>

Given equal priors for all competing models, choosing the model with the smallest BIC is equivalent to selecting the model with the maximum posterior probability. Alternatively, Bayes factors for models of molecular evolution can be calculated using reversible jump MCMC (Huelsenbeck, Larget and Alfaro 2004). We can easily use the BIC instead of the AIC to calculate BIC differences or BIC weights.

### Performance Based Selection ###

Minin et al. (2003) developed a novel approach that selects models on the basis of their phylogenetic performance, measured as the expected error on branch lengths estimates weighted by their BIC. Under this **decision theoretic** framework (DT) the best model is the one with that minimizes the risk function:

<wiki:gadget url="http://mathml-gadget.googlecode.com/svn/trunk/mathml-gadget.xml" border="0" up\_content="C\_i~~sum\_(j=1)^n||hat B\_i - hat B\_j||(e^(-(BIC\_j)/2))/(sum\_(j=1)^R(e^(-(BIC\_i)/2))" height="15"/>

where

<wiki:gadget url="http://mathml-gadget.googlecode.com/svn/trunk/mathml-gadget.xml" border="0" up\_content="||hat B\_i - hat B\_j||^2=sum\_(l=1)^(2t-3)(hat B\_(il) - hat B\_(jl))^2" height="15"/>

and where t is the number of taxa. Indeed, simulations suggested that models selected with this criterion result in slightly more accurate branch length estimates than those obtained under models selected by the hLRTs (Minin et al. 2003; Abdo et al. 2005).

## Model Uncertainty ##

The AIC, Bayesian and DT methods can rank the models, allowing us to assess how confident we are in the model selected. For these measures we could present their differences (Δ). For example, for the ith model, the AIC (BIC, DT) difference is:

<wiki:gadget url="http://mathml-gadget.googlecode.com/svn/trunk/mathml-gadget.xml" border="0" up\_content="Delta\_i = AIC\_i - min(AIC)" height="15"/>

where min AIC is the smallest AIC value among all candidate models. The AIC differences are easy to interpret and allow a quick comparison and ranking of candidate models. As a rough rule of thumb, models having Δi within 1-2 of the best model have substantial support and should receive consideration. Models having Δi within 3-7 of the best model have considerably less support, while models with Δi > 10 have essentially no support. Very conveniently, we can use these differences to obtain the relative AIC (BIC) **weight** (wi) of each model:

<wiki:gadget url="http://mathml-gadget.googlecode.com/svn/trunk/mathml-gadget.xml" border="0" up\_content="omega\_i = e^(-1/(2 Delta\_i))/(sum\_(r=1)^R(e^(-1/(2 Delta\_r)))" height="15"/>

which can be interpreted, from a Bayesian perspective, as the probability that a model is the best approximation to the truth given the data. The weights for every model add to 1, so we can establish an approximate 95% **confidence set of models for the best models** by summing the weights from largest to smallest from largest to smallest until the sum is 0.95 (Burnham and Anderson 1998, pp.169-171; Burnham and Anderson 2003). This interval can also be set up stochastically (see above “Model selection and averaging”). **Note that this equation will not work for the DT (see the DT explanation on “Model selection and averaging”**).

## Model Averaging ##

Often there is some uncertainty in selecting the best candidate model. In such cases, or just one when does not want to rely on a single model, inferences can be drawn from all models (or an optimal subset) simultaneously. This is known as model averaging or multimodel inference. See Posada and Buckley (2004a) and references therein for an explanation of application of these techniques in the context of phylogenetics.

Within the AIC or Bayesian frameworks, it is straightforward to obtain a model-averaged estimate of any parameter (Madigan and Raftery 1994; Raftery 1996; Hoeting, Madigan, and Raftery 1999; Wasserman 2000; Burnham and Anderson 2003; Posada 2003). For example, a **model-averaged estimate** of the substitution rate between adenine and cytosine using the Akaike weights for R candidate models would be:

<wiki:gadget url="http://mathml-gadget.googlecode.com/svn/trunk/mathml-gadget.xml" border="0" up\_content="hat bar phi\_(A-C) = (sum\_(r=1)^R omega\_i I\_(phi\_(A-C))(M\_i) phi\_(A-C\_i))/(omega\_+(phi\_(A-C)))" height="15"/>

where

<wiki:gadget url="http://mathml-gadget.googlecode.com/svn/trunk/mathml-gadget.xml" border="0" up\_content="omega\_+(phi\_(A-C)) = sum\_(i=1)^R omega\_i I\_(phi\_(A-C))(M\_i)" height="15"/>

and

<wiki:gadget url="http://mathml-gadget.googlecode.com/svn/trunk/mathml-gadget.xml" border="0" up\_content="I\_(phi\_(A-C))(M\_i) = {(1, if phi\_(A-C) text{is in model M\_i}),(0, text{otherwise}):}" width="500" height="35"/>

Note that need to be careful when interpreting the relative importance of parameters. When the number of candidate models is less than the number of possible combinations of parameters, the presence-absence of some pairs of parameters can be correlated, and so their relative importances.

## Model Averaged Phylogeny ##

Indeed, the averaged parameter could be the topology itself, so we could construct a model-averaged estimate of phylogeny. For example, one could estimate a ML tree for all models (or a best subset) and with those one could build a weighted consensus tree using the corresponding Akaike weights. See Posada and Buckley (2004a) for a practical example.

## Parameter Importance ##

It is possible to estimate the relative importance of any parameter by summing the weights across all models that include the parameters we are interested in. For example, the relative importance of the substitution rate between adenine and cytosine across all candidate models is simply the denominator above,

<wiki:gadget url="http://mathml-gadget.googlecode.com/svn/trunk/mathml-gadget.xml" border="0" up\_content="omega\_+(phi\_(A-C))" height="15"/>