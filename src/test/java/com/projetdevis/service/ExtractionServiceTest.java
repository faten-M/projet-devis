package com.projetdevis.service;

import com.projetdevis.model.ExtractedInfo;
import com.projetdevis.model.ItemRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ExtractionServiceTest {

    private final ExtractionService extractor = new ExtractionService();

    @Test
    void extractWithHumanQuantities_listFormat() {
        String text = "- quelques chaises\n" +
                      "- une dizaine de tables\n" +
                      "- bcp de bureaux\n" +
                      "- 5 panneaux";
        ExtractedInfo info = extractor.extract(text);

        assertNotNull(info);
        List<ItemRequest> items = info.getItems();
        System.out.println("DEBUG listFormat items: " + items);
        assertEquals(4, items.size());

        assertTrue(items.stream().anyMatch(i -> "chaises".equals(i.getProduct()) && i.getQuantity() == 3));
        assertTrue(items.stream().anyMatch(i -> "tables".equals(i.getProduct()) && i.getQuantity() == 10));
        assertTrue(items.stream().anyMatch(i -> "bureaux".equals(i.getProduct()) && i.getQuantity() == 20));
        assertTrue(items.stream().anyMatch(i -> "panneaux".equals(i.getProduct()) && i.getQuantity() == 5));
    }

    @Test
    void extractWithHumanQuantities_altFormat() {
        String text = "chaises : plusieurs\n" +
                      "bureaux (une dizaine)\n" +
                      "tables : env 12\n" +
                      "armoires (10+)";
        ExtractedInfo info = extractor.extract(text);
        assertNotNull(info);
        List<ItemRequest> items = info.getItems();
        System.out.println("DEBUG altFormat items: " + items);
        assertEquals(4, items.size());

        assertTrue(items.stream().anyMatch(i -> i.getProduct() != null && i.getProduct().contains("chaise") && i.getQuantity() == 5));
        assertTrue(items.stream().anyMatch(i -> i.getProduct() != null && i.getProduct().contains("bureau") && i.getQuantity() == 10));
        assertTrue(items.stream().anyMatch(i -> i.getProduct() != null && i.getProduct().contains("table") && i.getQuantity() == 12));
        assertTrue(items.stream().anyMatch(i -> i.getProduct() != null && i.getProduct().contains("armoire") && i.getQuantity() == 10));
    }
}
