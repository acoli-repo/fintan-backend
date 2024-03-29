![Fintan](https://github.com/acoli-repo/fintan-doc/blob/main/img/Fintan.PNG)
## Software Documentation
[fintan-doc](https://github.com/acoli-repo/fintan-doc)
## Quick start and demo
### Install
```shell
git clone https://github.com/acoli-repo/fintan-backend.git
cd fintan-backend/
(. build.sh) 
```
### Demo
```shell
cd samples/xslt/apertium/
. _apertium_demo.sh 

```

## Authors and Maintainers
* **Christian Fäth** - faeth@em.uni-frankfurt.de
* **Christian Chiarcos** - chiarcos@informatik.uni-frankfurt.de
* **Maxim Ionov** 
* **Leo Gottfried** 

See also the list of [contributors](https://github.com/acoli-repo/fintan-backend/graphs/contributors) who participated in this project.

## Reference
* Fäth C., Chiarcos C., Ebbrecht B., Ionov M. (2020), Fintan - Flexible, Integrated Transformation and Annotation eNgineering. In: Proceedings of the 12th Language Resources and Evaluation Conference. LREC 2020. pp 7212-7221.

## Acknowledgments
This repository has been created in context of
* Applied Computational Linguistics ([ACoLi](http://acoli.cs.uni-frankfurt.de))
* Prêt-á-LLOD. Ready-to-use Multilingual Linked Language Data for Knowledge Services across Sectors ([Pret-a-LLOD](https://cordis.europa.eu/project/id/825182/results))
  * Research and Innovation Action of the H2020 programme (ERC, grant agreement 825182)
  * In this project, CoNLL-RDF has been applied/developed/restructured to serve as backend of the Flexible Integrated Transformation and Annotation Engineering ([FINTAN](https://github.com/Pret-a-LLOD/Fintan)) Platform.

## Licenses
The repositories for Fintan are being published under multiple licenses. All native code and documentation falls under an Apache 2.0 license. [LICENSE.main](LICENSE.main.txt). The examples in the backend repository contain data and some SPARQL scripts from external sources: CC-BY 4.0 for all data from universal dependencies and SPARQL scripts from the CoNLL-RDF repository, see [LICENSE.data](LICENSE.data.txt). The included Apertium data maintains its original copyright, i.e., GNU GPL 3.0, see [LICENSE.data.apertium](LICENSE.data.apertium.txt). Code from external dependencies and submodules is not redistributed with this package but fetched directly from the respective source repositories during build process and thus adheres to the respective Licenses. 

### LICENSE.main (Apache 2.0)
```
├── https://github.com/acoli-repo/fintan-doc/ 
├── https://github.com/acoli-repo/fintan-core/  
├── https://github.com/acoli-repo/fintan-swagger-client/
└── https://github.com/acoli-repo/fintan-backend/
	└──[ see exceptions below ]
```
### LICENSE.data (CC-BY 4.0)
```
└── https://github.com/acoli-repo/fintan-backend/  
	├── samples/conll-rdf/  
	│	└──[ all scripts and data ]
	└── samples/splitter/  
		├── en-ud-dev.conllu.gz.linked.ttl
		├── en-ud-tiny.conllu.gz.linked.ttl
		└── en-ud-train.conllu.gz.linked.ttl
```
### LICENSE.data.apertium (GNU GPL 3.0)
```
└── https://github.com/acoli-repo/fintan-backend/  
	└── samples/xslt/apertium/data
```


Please cite *Fäth C., Chiarcos C., Ebbrecht B., Ionov M. (2020), Fintan - Flexible, Integrated Transformation and Annotation eNgineering. In: Proceedings of the 12th Language Resources and Evaluation Conference. LREC 2020. pp 7212-7221.*.
