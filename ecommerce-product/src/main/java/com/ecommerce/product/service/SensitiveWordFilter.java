package com.ecommerce.product.service;

public interface SensitiveWordFilter {

    /**
     * Check if text contains sensitive words.
     */
    boolean containsSensitiveWord(String text);

    /**
     * Replace sensitive words with asterisks.
     */
    String filter(String text);

    /**
     * Reload the sensitive word trie from database.
     */
    void reload();
}
