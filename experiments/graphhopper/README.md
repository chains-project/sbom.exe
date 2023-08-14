Reference: https://github.com/ASSERT-KTH/rick-experiments/tree/main/graphhopper.

Simply run the following command:

```
java -javaagent:watchdog-agent-0.8.1-SNAPSHOT.jar=fingerprints=classfile.sha256.jsonl,skipShutdown=true  -jar graphhopper-web-7.0-SNAPSHOT.jar server config-example.ym
```

### Reproduction details

1. `maven`: Apache Maven 3.9.1 (2e178502fcdbffc201671fb2537d0cb4b4cc58f8)
2. `java`: 17.0.5 oracle
3. `OS`: Ubuntu 22.04
4. `classfile-fingerprint`: `f38a62df1bbee4cb232c4fe4ccdf220e8ec50a60`
5. `watchdog-agent`: `f38a62df1bbee4cb232c4fe4ccdf220e8ec50a60`
6. `graphhopper/web`: `7.0@b0a129cc0c7266c0299854f0f73335d83ef4ec52`
    > We cannot use the [jar linked on the README](https://repo1.maven.org/maven2/com/graphhopper/graphhopper-web/7.0/graphhopper-web-7.0.jar)
   > because we don't have sources for it and cannot create fingerprint.     

## Results

Classes that were not whitelisted just before starting the server.

⚠️ We also notice `org/apache/log4j/LogManager` as the first class loaded!

> The source code _does not_ seem vulnerable to `Log4Shell` because it uses
> `logback`. Dropwizard seems to load this class for some reason.

```text
[NOT WHITELISTED]: org/apache/log4j/LogManager
[NOT WHITELISTED]: io/dropwizard/jersey/DropwizardResourceConfig$SpecificBinderb8eaee19-4a93-4f14-806f-1cf04771e3bf
[NOT WHITELISTED]: io/dropwizard/jersey/DropwizardResourceConfig$SpecificBinderdac412df-b454-4e0b-8bd4-90a33e279061
[NOT WHITELISTED]: com/graphhopper/routing/weighting/custom/JaninoCustomWeightingHelperSubclass2
[NOT WHITELISTED]: org/glassfish/jersey/inject/hk2/Hk2InjectionManagerFactory
[NOT WHITELISTED]: org/glassfish/jersey/inject/hk2/Hk2InjectionManagerFactory$Hk2InjectionManagerStrategy
[NOT WHITELISTED]: org/glassfish/jersey/inject/hk2/Hk2InjectionManagerFactory$Hk2InjectionManagerStrategy$1
[NOT WHITELISTED]: org/glassfish/jersey/inject/hk2/Hk2InjectionManagerFactory$Hk2InjectionManagerStrategy$2
[NOT WHITELISTED]: org/glassfish/jersey/inject/hk2/ImmediateHk2InjectionManager
[NOT WHITELISTED]: org/glassfish/jersey/inject/hk2/AbstractHk2InjectionManager
[NOT WHITELISTED]: org/jvnet/hk2/external/generator/ServiceLocatorGeneratorImpl
[NOT WHITELISTED]: org/jvnet/hk2/internal/ServiceLocatorImpl
[NOT WHITELISTED]: org/jvnet/hk2/internal/ServiceLocatorImpl$1
[NOT WHITELISTED]: org/jvnet/hk2/internal/ServiceLocatorImpl$2
[NOT WHITELISTED]: org/jvnet/hk2/internal/DescriptorComparator
[NOT WHITELISTED]: org/jvnet/hk2/internal/ServiceHandleComparator
[NOT WHITELISTED]: org/jvnet/hk2/internal/PerLocatorUtilities
[NOT WHITELISTED]: org/jvnet/hk2/internal/PerLocatorUtilities$1
[NOT WHITELISTED]: org/jvnet/hk2/internal/PerLocatorUtilities$2
[NOT WHITELISTED]: org/jvnet/hk2/internal/PerLocatorUtilities$3
[NOT WHITELISTED]: org/jvnet/hk2/internal/IndexedListData
[NOT WHITELISTED]: org/jvnet/hk2/internal/SingletonContext
[NOT WHITELISTED]: org/jvnet/hk2/internal/SingletonContext$1
[NOT WHITELISTED]: org/jvnet/hk2/internal/SingletonContext$2
[NOT WHITELISTED]: org/jvnet/hk2/internal/PerLookupContext
[NOT WHITELISTED]: org/jvnet/hk2/internal/ServiceLocatorImpl$3
[NOT WHITELISTED]: org/jvnet/hk2/internal/ServiceLocatorImpl$4
[NOT WHITELISTED]: org/jvnet/hk2/internal/ServiceLocatorImpl$8
[NOT WHITELISTED]: org/jvnet/hk2/internal/ServiceLocatorImpl$10
[NOT WHITELISTED]: org/jvnet/hk2/internal/DynamicConfigurationImpl
[NOT WHITELISTED]: org/jvnet/hk2/internal/Utilities
[NOT WHITELISTED]: org/jvnet/hk2/internal/Utilities$Interceptors
[NOT WHITELISTED]: org/jvnet/hk2/internal/Creator
[NOT WHITELISTED]: org/jvnet/hk2/internal/Utilities$1
[NOT WHITELISTED]: org/jvnet/hk2/internal/Utilities$AnnotationInformation
[NOT WHITELISTED]: org/jvnet/hk2/internal/Utilities$4
[NOT WHITELISTED]: org/jvnet/hk2/internal/ConstantActiveDescriptor
[NOT WHITELISTED]: org/jvnet/hk2/internal/SystemDescriptor
[NOT WHITELISTED]: org/jvnet/hk2/internal/Closeable
[NOT WHITELISTED]: org/jvnet/hk2/internal/AutoActiveDescriptor
[NOT WHITELISTED]: org/jvnet/hk2/internal/ThreeThirtyResolver
[NOT WHITELISTED]: org/jvnet/hk2/internal/DynamicConfigurationServiceImpl
[NOT WHITELISTED]: org/jvnet/hk2/internal/DefaultClassAnalyzer
[NOT WHITELISTED]: org/jvnet/hk2/internal/ServiceLocatorRuntimeImpl
[NOT WHITELISTED]: org/jvnet/hk2/external/runtime/ServiceLocatorRuntimeBean
[NOT WHITELISTED]: org/jvnet/hk2/internal/InstantiationServiceImpl
[NOT WHITELISTED]: org/jvnet/hk2/internal/TwoPhaseTransactionDataImpl
[NOT WHITELISTED]: org/jvnet/hk2/internal/ServiceLocatorImpl$CheckConfigurationData
[NOT WHITELISTED]: org/jvnet/hk2/internal/ServiceLocatorImpl$12
[NOT WHITELISTED]: org/jvnet/hk2/internal/ServiceLocatorImpl$5
[NOT WHITELISTED]: org/jvnet/hk2/internal/ServiceHandleImpl
[NOT WHITELISTED]: org/jvnet/hk2/internal/InstanceLifecycleEventImpl
[NOT WHITELISTED]: org/jvnet/hk2/internal/ServiceLocatorImpl$6
[NOT WHITELISTED]: org/jvnet/hk2/internal/CacheKey
[NOT WHITELISTED]: org/jvnet/hk2/internal/ServiceLocatorImpl$UnqualifiedIndexedFilter
[NOT WHITELISTED]: org/jvnet/hk2/internal/ServiceLocatorImpl$IgdCacheKey
[NOT WHITELISTED]: org/jvnet/hk2/internal/ImmediateResults
[NOT WHITELISTED]: org/jvnet/hk2/internal/NarrowResults
[NOT WHITELISTED]: org/jvnet/hk2/internal/ServiceLocatorImpl$IgdValue
[NOT WHITELISTED]: org/jvnet/hk2/internal/ServiceLocatorImpl$9
[NOT WHITELISTED]: org/jvnet/hk2/internal/Collector
[NOT WHITELISTED]: org/jvnet/hk2/internal/ClazzCreator
[NOT WHITELISTED]: org/jvnet/hk2/internal/ConstructorAction
[NOT WHITELISTED]: org/jvnet/hk2/internal/Utilities$3
[NOT WHITELISTED]: org/jvnet/hk2/internal/Utilities$2
[NOT WHITELISTED]: org/jvnet/hk2/internal/SystemInjecteeImpl
[NOT WHITELISTED]: org/jvnet/hk2/internal/ClazzCreator$ResolutionInfo
[NOT WHITELISTED]: org/jvnet/hk2/internal/AnnotatedElementAnnotationInfo
[NOT WHITELISTED]: org/jvnet/hk2/internal/SoftAnnotatedElementAnnotationInfo
[NOT WHITELISTED]: org/jvnet/hk2/internal/PopulatorImpl
[NOT WHITELISTED]: org/glassfish/jersey/inject/hk2/Hk2BootstrapBinder
[NOT WHITELISTED]: org/glassfish/jersey/inject/hk2/JerseyClassAnalyzer$Binder
[NOT WHITELISTED]: org/glassfish/jersey/inject/hk2/RequestContext$Binder
[NOT WHITELISTED]: org/glassfish/jersey/inject/hk2/ContextInjectionResolverImpl$Binder
[NOT WHITELISTED]: org/glassfish/jersey/inject/hk2/ContextInjectionResolverImpl$Binder$2
[NOT WHITELISTED]: org/glassfish/jersey/inject/hk2/ContextInjectionResolverImpl$Binder$1
[NOT WHITELISTED]: org/glassfish/jersey/inject/hk2/JerseyErrorService$Binder
[NOT WHITELISTED]: org/glassfish/jersey/inject/hk2/JerseyClassAnalyzer
[NOT WHITELISTED]: org/glassfish/jersey/inject/hk2/RequestContext
[NOT WHITELISTED]: org/glassfish/jersey/inject/hk2/RequestContext$Binder$1
[NOT WHITELISTED]: org/glassfish/jersey/inject/hk2/ContextInjectionResolverImpl
[NOT WHITELISTED]: org/glassfish/jersey/inject/hk2/JerseyErrorService
[NOT WHITELISTED]: org/glassfish/jersey/inject/hk2/Hk2RequestScope
[NOT WHITELISTED]: org/glassfish/jersey/inject/hk2/ContextInjectionResolverImpl$1
[NOT WHITELISTED]: org/glassfish/jersey/inject/hk2/ContextInjectionResolverImpl$CacheKey
[NOT WHITELISTED]: org/glassfish/jersey/inject/hk2/ContextInjectionResolverImpl$3
[NOT WHITELISTED]: org/glassfish/jersey/inject/hk2/Hk2Helper
[NOT WHITELISTED]: org/glassfish/jersey/servlet/async/AsyncContextDelegateProviderImpl
[NOT WHITELISTED]: org/glassfish/jersey/servlet/init/FilterUrlMappingsProviderImpl
[NOT WHITELISTED]: org/glassfish/jersey/inject/hk2/Hk2Helper$1
[NOT WHITELISTED]: org/glassfish/jersey/inject/hk2/SupplierFactoryBridge
[NOT WHITELISTED]: org/glassfish/jersey/inject/hk2/InstanceSupplierFactoryBridge
[NOT WHITELISTED]: org/glassfish/jersey/inject/hk2/InjectionResolverWrapper
[NOT WHITELISTED]: org/glassfish/jersey/spidiscovery/internal/MetaInfServicesAutoDiscoverable
[NOT WHITELISTED]: org/glassfish/jersey/spidiscovery/internal/MetaInfServicesAutoDiscoverable$1
[NOT WHITELISTED]: org/jvnet/hk2/internal/IterableProviderImpl
[NOT WHITELISTED]: org/jvnet/hk2/internal/SystemDescriptor$1
[NOT WHITELISTED]: org/jvnet/hk2/internal/FactoryCreator
[NOT WHITELISTED]: org/jvnet/hk2/internal/CacheKey$1
```

Classes that were not whitelisted when we quit the server.

```text
[NOT WHITELISTED]: org/jvnet/hk2/internal/ServiceLocatorImpl$7
[NOT WHITELISTED]: org/jvnet/hk2/internal/SingletonContext$GenerationComparator
```