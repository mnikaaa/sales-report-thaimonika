package edu.itc.salesreport.ingest;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

import edu.itc.salesreport.model.SaleTransaction;

/** The subject: loads a month of CSV files and notifies listeners. */
public final class MonthLoader {

    private final TransactionParser parser;
    private final List<LoadListener> listeners = new CopyOnWriteArrayList<>();
    private final List<String> rejectedRows = new ArrayList<>();

    public MonthLoader(TransactionParser parser) {
        this.parser = parser;
    }

    /** Registers a listener; the returned handle unsubscribes it. */
    public Runnable subscribe(LoadListener listener) {
        listeners.add(listener);
        return () -> listeners.remove(listener);
    }

    public List<String> rejectedRows() {
        return List.copyOf(rejectedRows);
    }

    public List<SaleTransaction> load(YearMonth month, Path dataDir) {
        long start = System.nanoTime();
        rejectedRows.clear();
        Path dir = dataDir.resolve(month.toString());

        List<Path> files;
        try (Stream<Path> s = Files.list(dir)) {
            files = s.filter(p -> p.toString().endsWith(".csv"))
                    .sorted()
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        var result = new ArrayList<SaleTransaction>();
        int totalOk = 0;
        int totalBad = 0;
        int done = 0;
        for (Path file : files) {
            List<String> lines;
            try {
                lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            int ok = 0;
            int bad = 0;
            for (int i = 1; i < lines.size(); i++) {   // index 0 = header
                try {
                    result.add(parser.parse(lines.get(i)));
                    ok++;
                } catch (InvalidRowException e) {
                    bad++;
                    rejectedRows.add(file.getFileName() + ":" + (i + 1)
                            + " " + e.getMessage());
                }
            }
            totalOk += ok;
            totalBad += bad;
            done++;
            publish(new LoadEvent.FileLoaded(file, ok, bad, done, files.size()));
        }
        publish(new LoadEvent.LoadFinished(totalOk, totalBad,
                Duration.ofNanos(System.nanoTime() - start)));
        return result;
    }

    private void publish(LoadEvent event) {
        listeners.forEach(l -> l.onEvent(event));
    }
}