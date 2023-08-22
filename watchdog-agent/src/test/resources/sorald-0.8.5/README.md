## Results

After including the external jar while generating the fingerprints, I get the following errors when running the workloads:

```text
[NOT WHITELISTED]: org/sonarsource/sonarlint/shaded/org/springframework/core/$Proxy40
```
> This is a false positive because it is a runtime generated class.
