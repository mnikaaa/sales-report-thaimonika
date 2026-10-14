package edu.itc.salesreport.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class SaleTransactionTest {

    private static SaleTransaction tx(int qty, String price, String discount) {
        return new SaleTransaction("PNH", LocalDate.of(2026, 9, 1), "PNH-1",
                "SKU-1", "Rice", "Grocery", qty, new BigDecimal(price),
                new BigDecimal(discount), PaymentMethod.CASH);
    }

    @Test
    void roundsHalfUp() {
        assertEquals(new BigDecimal("1.01"), tx(3, "0.335", "0.00").revenue());
    }

    @Test
    void rejectsDiscountAboveLineTotal() {
        assertThrows(IllegalArgumentException.class, () -> tx(1, "1.00", "1.01"));
    }

    @Test
    void topProductsIsUnmodifiable() {
        var mutable = new ArrayList<>(
                List.of(new ProductTotal("S", "P", BigDecimal.ONE, 1)));
        var b = new BranchSummary("PNH", BigDecimal.ONE, BigDecimal.ZERO, 1,
                Map.of(), mutable, Map.of());
        assertThrows(UnsupportedOperationException.class,
                () -> b.topProducts().add(new ProductTotal("X", "Y", BigDecimal.ONE, 1)));
        mutable.clear();
        assertEquals(1, b.topProducts().size());
    }
}