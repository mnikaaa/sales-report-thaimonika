# Design review: cohesion and coupling

Counts come from the `import edu.itc.salesreport.*` lines in `src/main/java`.
Ce = project packages it imports; Ca = project packages that import it;
I = Ce / (Ca + Ce).

| Package | Responsibility (cohesion) | Ce | Ca | I |
|---------|---------------------------|----|----|---|
| model | Canonical domain records and their invariants (money rules, validation). | 0 | 2 (ingest, render) | 0 / (2+0) = 0.00 |
| ingest | Turns CSV lines into validated `SaleTransaction`s and reports load progress. | 1 (model) | 1 (App) | 1 / (1+1) = 0.50 |
| render | Turns a `MonthlyReport` into output bytes in one format. | 1 (model) | 0 | 1 / (0+1) = 1.00 |
| App (root) | Composition root: wires loader and console observer. | 1 (ingest) | 0 | 1 / (0+1) = 1.00 |

Cohesion: each package has one reason to change (domain rules, input format,
output format). `ingest` holds two related things, parsing and load events,
which both belong to getting data in.

**Accepted coupling:** `ingest -> model`. The parser must build
`SaleTransaction`, and `model` is the most stable package (I = 0.00), so
depending on it is safe. `DependencyRulesTest` stops `model` from importing
anything else and `ingest` from importing `render`.

**Sealed vs open for QA-4:** A sealed `ReportRenderer` gives compile-time
exhaustive `switch` and makes the set of formats explicit. Adding PDF or XLSX
(Lab-08) costs one `permits` entry, one new class and one case in
`forExtension`, which fits the QA-4 measure of at most 2 person-days. An open
interface would let others add formats without touching the interface, but we
own all renderers, and we would lose the exhaustiveness check. Sealed fits
better for now; revisit if third-party plugins are ever needed.