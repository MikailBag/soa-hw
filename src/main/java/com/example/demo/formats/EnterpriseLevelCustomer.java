package com.example.demo.formats;

import java.util.List;
import java.util.Map;

// to make comparison more fair, there are no serialization code or annotations
public record EnterpriseLevelCustomer (
        String name,
        int age,
        double balance,
        CustomerStatus status,
        List<String> cats,
        // what history is this?
        // nobody knows, but PM insists it is important feature
        Map<String, List<String>> history
) {
}
