package com.ecommerce.product.service;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.ecommerce.common.core.page.PageResult;
import com.ecommerce.product.document.SpuDocument;
import com.ecommerce.product.dto.SkuDTO;
import com.ecommerce.product.dto.SpuPageRequest;
import com.ecommerce.product.entity.Brand;
import com.ecommerce.product.entity.Category;
import com.ecommerce.product.entity.Spu;
import com.ecommerce.product.mapper.BrandMapper;
import com.ecommerce.product.mapper.CategoryMapper;
import com.ecommerce.product.mapper.SpuMapper;
import com.ecommerce.product.repository.SpuEsRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpuEsService {

    private final SpuEsRepository spuEsRepository;
    private final ElasticsearchOperations esOperations;
    private final SpuMapper spuMapper;
    private final CategoryMapper categoryMapper;
    private final BrandMapper brandMapper;
    private final SkuService skuService;

    @PostConstruct
    public void initIndex() {
        try {
            var indexOps = esOperations.indexOps(SpuDocument.class);
            if (!indexOps.exists()) {
                Map<String, Object> settings = new HashMap<>();
                Map<String, Object> analysis = new HashMap<>();
                Map<String, Object> analyzers = new HashMap<>();

                Map<String, Object> ikSmart = new HashMap<>();
                ikSmart.put("type", "standard");
                ikSmart.put("tokenizer", "standard");
                analyzers.put("ik_smart", ikSmart);

                analysis.put("analyzer", analyzers);
                settings.put("analysis", analysis);

                indexOps.createWithMapping();
                log.info("Created Elasticsearch index: spu");
            }
        } catch (Exception e) {
            log.warn("Failed to initialize ES index: {}", e.getMessage());
        }
    }

    public PageResult<Long> searchSpuIds(SpuPageRequest request) {
        BoolQuery.Builder boolBuilder = new BoolQuery.Builder();

        if (StringUtils.hasText(request.getKeyword())) {
            boolBuilder.must(m -> m.match(t -> t.field("name").query(request.getKeyword())));
            boolBuilder.should(s -> s.match(t -> t.field("description").query(request.getKeyword())));
        } else {
            boolBuilder.must(m -> m.matchAll(x -> x));
        }

        if (request.getCategoryId() != null) {
            boolBuilder.filter(f -> f.term(t -> t.field("categoryId").value(request.getCategoryId())));
        }
        if (request.getBrandId() != null) {
            boolBuilder.filter(f -> f.term(t -> t.field("brandId").value(request.getBrandId())));
        }
        if (request.getStatus() != null) {
            boolBuilder.filter(f -> f.term(t -> t.field("status").value(request.getStatus())));
        }

        BoolQuery boolQuery = boolBuilder.build();

        var queryBuilder = NativeQuery.builder()
                .withQuery(new Query(boolQuery))
                .withPageable(PageRequest.of(request.getPageNum() - 1, request.getPageSize()));

        if (request.getSort() != null) {
            switch (request.getSort()) {
                case "sales_desc" -> queryBuilder.withSort(org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Direction.DESC, "salesCount"));
                case "price_asc" -> queryBuilder.withSort(org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Direction.ASC, "minPrice"));
                case "price_desc" -> queryBuilder.withSort(org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Direction.DESC, "minPrice"));
                case "newest" -> queryBuilder.withSort(org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Direction.DESC, "createTime"));
            }
        }

        NativeQuery nativeQuery = queryBuilder.build();

        SearchHits<SpuDocument> searchHits = esOperations.search(nativeQuery, SpuDocument.class);

        List<Long> ids = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(SpuDocument::getId)
                .toList();

        return PageResult.of(ids, searchHits.getTotalHits(), request.getPageNum(), request.getPageSize());
    }

    public void indexSpu(Long spuId) {
        try {
            Spu spu = spuMapper.selectById(spuId);
            if (spu == null) {
                spuEsRepository.deleteById(spuId);
                return;
            }
            SpuDocument doc = buildDocument(spu);
            spuEsRepository.save(doc);
        } catch (Exception e) {
            log.error("Failed to index SPU {}: {}", spuId, e.getMessage());
        }
    }

    public void deleteSpu(Long spuId) {
        try {
            spuEsRepository.deleteById(spuId);
        } catch (Exception e) {
            log.error("Failed to delete SPU {} from ES: {}", spuId, e.getMessage());
        }
    }

    public void reindexAll() {
        log.info("Starting full ES re-index...");
        try {
            var indexOps = esOperations.indexOps(SpuDocument.class);
            if (indexOps.exists()) {
                indexOps.delete();
            }
            indexOps.createWithMapping();

            int pageNum = 1;
            int batchSize = 500;
            int total = 0;

            while (true) {
                var page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<Spu>(pageNum, batchSize);
                var result = spuMapper.selectPage(page, new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>());
                if (result.getRecords().isEmpty()) break;

                List<SpuDocument> docs = result.getRecords().stream()
                        .map(this::buildDocument)
                        .toList();
                spuEsRepository.saveAll(docs);
                total += docs.size();

                if (result.getRecords().size() < batchSize) break;
                pageNum++;
            }
            log.info("Full ES re-index complete. Indexed {} documents.", total);
        } catch (Exception e) {
            log.error("Full ES re-index failed: {}", e.getMessage(), e);
        }
    }

    private SpuDocument buildDocument(Spu spu) {
        String categoryName = null;
        if (spu.getCategoryId() != null) {
            Category category = categoryMapper.selectById(spu.getCategoryId());
            if (category != null) categoryName = category.getName();
        }

        String brandName = null;
        if (spu.getBrandId() != null) {
            Brand brand = brandMapper.selectById(spu.getBrandId());
            if (brand != null) brandName = brand.getName();
        }

        BigDecimal minPrice = null;
        BigDecimal maxPrice = null;
        List<SkuDTO> skus = skuService.getSkuListBySpuId(spu.getId());
        if (skus != null && !skus.isEmpty()) {
            minPrice = skus.stream().map(SkuDTO::getPrice).filter(Objects::nonNull).min(BigDecimal::compareTo).orElse(null);
            maxPrice = skus.stream().map(SkuDTO::getPrice).filter(Objects::nonNull).max(BigDecimal::compareTo).orElse(null);
        }

        return SpuDocument.builder()
                .id(spu.getId())
                .name(spu.getName())
                .categoryId(spu.getCategoryId())
                .brandId(spu.getBrandId())
                .description(spu.getDescription())
                .status(spu.getStatus())
                .categoryName(categoryName)
                .brandName(brandName)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .salesCount(spu.getSalesCount())
                .viewCount(spu.getViewCount())
                .avgRating(spu.getAvgRating() != null ? spu.getAvgRating().doubleValue() : null)
                .reviewCount(spu.getReviewCount())
                .createTime(spu.getCreateTime())
                .build();
    }
}
