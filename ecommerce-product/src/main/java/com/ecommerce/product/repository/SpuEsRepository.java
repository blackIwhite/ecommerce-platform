package com.ecommerce.product.repository;

import com.ecommerce.product.document.SpuDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface SpuEsRepository extends ElasticsearchRepository<SpuDocument, Long> {
}
