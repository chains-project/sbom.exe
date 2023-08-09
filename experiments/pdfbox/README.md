I tried workloads set up by [Deepika](https://github.com/Deee92).
See https://github.com/ASSERT-KTH/rick-experiments/tree/main/pdfbox.

I slightly modified the workloads to run them on my machine.
1. `PDFMerger` and `OverlayPDF` were throwing some errors, so
    I commented them out. The errors are written in the script itself.
2. I could not find `WriteDecodedDoc` in the new version of `pdfbox`.

Simply run `run.sh` to run the workloads.

### Reproduction details

1. `maven`: Apache Maven 3.9.1 (2e178502fcdbffc201671fb2537d0cb4b4cc58f8)
2. `java`: 17.0.5 oracle
3. `OS`: Ubuntu 22.04
4. `classfile-fingerprint`: `f38a62df1bbee4cb232c4fe4ccdf220e8ec50a60`
5. `watchdog-agent`: `f38a62df1bbee4cb232c4fe4ccdf220e8ec50a60`
6. `pdfbox`: `3.0.0-beta1`

## Results

I get the following errors when running the workloads:

```text
[NOT WHITELISTED]: org/jcp/xml/dsig/internal/dom/XMLDSigRI
[NOT WHITELISTED]: org/jcp/xml/dsig/internal/dom/XMLDSigRI$1
[NOT WHITELISTED]: org/jcp/xml/dsig/internal/dom/XMLDSigRI$2
[NOT WHITELISTED]: org/jcp/xml/dsig/internal/dom/XMLDSigRI$ProviderService
```

The subcommands of pdfbox where the errors occur are:

```text
ExtractText
ExtractImages
PDFSplit
Encrypt
Decrypt
PDFToImage
```
