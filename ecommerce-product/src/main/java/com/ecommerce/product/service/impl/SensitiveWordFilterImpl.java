package com.ecommerce.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ecommerce.product.entity.SensitiveWord;
import com.ecommerce.product.mapper.SensitiveWordMapper;
import com.ecommerce.product.service.SensitiveWordFilter;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SensitiveWordFilterImpl implements SensitiveWordFilter {

    private final SensitiveWordMapper sensitiveWordMapper;
    private volatile Map<Character, Object> trie = new HashMap<>();
    private static final Object LEAF = new Object();

    @PostConstruct
    public void init() {
        reload();
    }

    @Override
    public void reload() {
        List<SensitiveWord> words = sensitiveWordMapper.selectList(
                new LambdaQueryWrapper<SensitiveWord>().eq(SensitiveWord::getDeleted, 0));
        Map<Character, Object> newTrie = new HashMap<>();
        for (SensitiveWord sw : words) {
            buildTrie(newTrie, sw.getWord().toLowerCase());
        }
        this.trie = newTrie;
        log.info("Sensitive word filter reloaded, {} words", words.size());
    }

    @SuppressWarnings("unchecked")
    private void buildTrie(Map<Character, Object> root, String word) {
        Map<Character, Object> current = root;
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            Object next = current.get(c);
            if (next == null) {
                Map<Character, Object> newNode = new HashMap<>();
                current.put(c, newNode);
                current = newNode;
            } else if (next instanceof Map) {
                current = (Map<Character, Object>) next;
            } else {
                Map<Character, Object> newNode = new HashMap<>();
                current.put(c, newNode);
                current = newNode;
            }
        }
        current.put('\0', LEAF);
    }

    @Override
    public boolean containsSensitiveWord(String text) {
        if (text == null || text.isEmpty()) return false;
        return !findSensitiveWords(text).isEmpty();
    }

    @Override
    public String filter(String text) {
        if (text == null || text.isEmpty()) return text;
        StringBuilder sb = new StringBuilder(text);
        for (int i = 0; i < sb.length(); i++) {
            int maxLen = matchAt(sb, i);
            if (maxLen > 0) {
                for (int j = i; j < i + maxLen; j++) {
                    sb.setCharAt(j, '*');
                }
                i += maxLen - 1;
            }
        }
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private int matchAt(CharSequence text, int start) {
        Map<Character, Object> current = trie;
        int maxLen = 0;
        for (int i = start; i < text.length(); i++) {
            char c = Character.toLowerCase(text.charAt(i));
            Object next = current.get(c);
            if (next == null) {
                break;
            } else if (next == LEAF) {
                maxLen = i - start + 1;
                break;
            } else {
                current = (Map<Character, Object>) next;
                if (current.containsKey('\0')) {
                    maxLen = i - start + 1;
                }
            }
        }
        return maxLen;
    }

    private java.util.Set<String> findSensitiveWords(String text) {
        java.util.Set<String> found = new java.util.HashSet<>();
        for (int i = 0; i < text.length(); i++) {
            int len = matchAt(text, i);
            if (len > 0) {
                found.add(text.substring(i, i + len));
            }
        }
        return found;
    }
}
