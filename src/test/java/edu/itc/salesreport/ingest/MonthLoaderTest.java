package edu.itc.salesreport.ingest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MonthLoaderTest {

    static final String H = CsvTransactionParser.HEADER;
    static final String OK1 = "PNH,2026-09-01,PNH-1,SKU-1001,Jasmine Rice 5kg,Grocery,2,6.50,0.00,KHQR";
    static final String OK2 = "PNH,2026-09-01,PNH-2,SKU-2001,Mineral Water 1.5L,Beverages,1,0.45,0.00,CASH";
    static final String OK3 = "REP,2026-09-01,REP-1,SKU-6001,Notebook A5,Stationery,1,0.75,0.00,CARD";
    static final String BAD = "REP,2026-09-01,REP-2,SKU-6001,Notebook A5,Stationery,two,0.75,0.00,CARD";

    private Path setup(Path tmp) throws IOException {
        Path dir = Files.createDirectories(tmp.resolve("2026-09"));
        Files.write(dir.resolve("PNH-2026-09-01.csv"), List.of(H, OK1, OK2));
        Files.write(dir.resolve("REP-2026-09-01.csv"), List.of(H, OK3, BAD));
        return tmp;
    }

    @Test
    void publishesTwoFileEventsThenFinished(@TempDir Path tmp) throws IOException {
        setup(tmp);
        var events = new ArrayList<LoadEvent>();
        var loader = new MonthLoader(new CsvTransactionParser());
        loader.subscribe(events::add);

        loader.load(YearMonth.of(2026, 9), tmp);

        assertEquals(3, events.size());
        var f1 = assertInstanceOf(LoadEvent.FileLoaded.class, events.get(0));
        var f2 = assertInstanceOf(LoadEvent.FileLoaded.class, events.get(1));
        var fin = assertInstanceOf(LoadEvent.LoadFinished.class, events.get(2));
        assertEquals(2, f1.accepted());
        assertEquals(0, f1.rejected());
        assertEquals(1, f2.accepted());
        assertEquals(1, f2.rejected());
        assertEquals(3, fin.accepted());
        assertEquals(1, fin.rejected());
    }

    @Test
    void unsubscribedListenerReceivesNothing(@TempDir Path tmp) throws IOException {
        setup(tmp);
        var events = new ArrayList<LoadEvent>();
        var loader = new MonthLoader(new CsvTransactionParser());
        Runnable unsubscribe = loader.subscribe(events::add);
        unsubscribe.run();

        loader.load(YearMonth.of(2026, 9), tmp);

        assertTrue(events.isEmpty());
    }
}