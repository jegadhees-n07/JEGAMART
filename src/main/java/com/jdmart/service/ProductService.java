package com.jdmart.service;

import com.jdmart.dto.ProductDto;
import com.jdmart.exception.ResourceNotFoundException;
import com.jdmart.model.Category;
import com.jdmart.model.Product;
import com.jdmart.repository.CategoryRepository;
import com.jdmart.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductDto> getAllProducts() {
        return productRepository.findByActiveTrue().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ProductDto> getProductsPaged(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return productRepository.findByActiveTrue(pageable).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public ProductDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return mapToDto(product);
    }

    @Transactional(readOnly = true)
    public List<ProductDto> getFeaturedProducts() {
        return productRepository.findByFeaturedTrueAndActiveTrue().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductDto> getTrendingProducts() {
        return productRepository.findByTrendingTrueAndActiveTrue().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductDto> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryIdAndActiveTrue(categoryId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductDto> searchProducts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllProducts();
        }
        return productRepository.searchProducts(keyword.trim()).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ProductDto> filterProducts(Long categoryId, Double minPrice, Double maxPrice, Double minRating,
                                          String sortOption, int page, int size) {
        Sort sort = switch (sortOption != null ? sortOption : "") {
            case "price_asc" -> Sort.by("sellingPrice").ascending();
            case "price_desc" -> Sort.by("sellingPrice").descending();
            case "rating" -> Sort.by("rating").descending();
            case "newest" -> Sort.by("createdAt").descending();
            default -> Sort.by("id").ascending();
        };

        Pageable pageable = PageRequest.of(page, size, sort);
        return productRepository.filterProducts(categoryId, minPrice, maxPrice, minRating, pageable)
                .map(this::mapToDto);
    }

    @Transactional
    public ProductDto createProduct(ProductDto dto) {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", dto.getCategoryId()));

        Product product = new Product();
        copyDtoToEntity(dto, product);
        product.setCategory(category);

        Product saved = productRepository.save(product);
        return mapToDto(saved);
    }

    @Transactional
    public ProductDto updateProduct(Long id, ProductDto dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        if (dto.getCategoryId() != null && !dto.getCategoryId().equals(product.getCategory().getId())) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", dto.getCategoryId()));
            product.setCategory(category);
        }

        copyDtoToEntity(dto, product);
        Product saved = productRepository.save(product);
        return mapToDto(saved);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        product.setActive(false);
        productRepository.save(product);
    }

    @Transactional
    public ProductDto updateStock(Long id, int quantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        product.setStockQuantity(quantity);
        Product saved = productRepository.save(product);
        return mapToDto(saved);
    }

    public ProductDto mapToDto(Product p) {
        ProductDto dto = new ProductDto();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setShortDescription(p.getShortDescription());
        dto.setDescription(p.getDescription());
        dto.setOriginalPrice(p.getOriginalPrice());
        dto.setDiscountPercentage(p.getDiscountPercentage());
        dto.setSellingPrice(p.getSellingPrice());
        dto.setStockQuantity(p.getStockQuantity());
        dto.setRating(p.getRating());
        dto.setReviewCount(p.getReviewCount());
        dto.setImageUrl(p.getImageUrl());
        dto.setSpecifications(p.getSpecifications());
        dto.setFeatured(p.isFeatured());
        dto.setTrending(p.isTrending());
        dto.setActive(p.isActive());

        if (p.getCategory() != null) {
            dto.setCategoryId(p.getCategory().getId());
            dto.setCategoryName(p.getCategory().getName());
            dto.setCategorySlug(p.getCategory().getSlug());
        }
        return dto;
    }

    private void copyDtoToEntity(ProductDto dto, Product p) {
        p.setName(dto.getName());
        p.setShortDescription(dto.getShortDescription());
        p.setDescription(dto.getDescription());
        p.setOriginalPrice(dto.getOriginalPrice());
        p.setDiscountPercentage(dto.getDiscountPercentage() != null ? dto.getDiscountPercentage() : 0);

        if (dto.getSellingPrice() != null) {
            p.setSellingPrice(dto.getSellingPrice());
        } else if (p.getOriginalPrice() != null) {
            int discount = p.getDiscountPercentage() != null ? p.getDiscountPercentage() : 0;
            p.setSellingPrice(Math.round(p.getOriginalPrice() * (1.0 - (discount / 100.0)) * 100.0) / 100.0);
        }

        p.setStockQuantity(dto.getStockQuantity());
        p.setImageUrl(dto.getImageUrl());
        p.setSpecifications(dto.getSpecifications());
        p.setFeatured(dto.isFeatured());
        p.setTrending(dto.isTrending());
        p.setActive(dto.isActive());
        if (dto.getRating() != null) p.setRating(dto.getRating());
        if (dto.getReviewCount() != null) p.setReviewCount(dto.getReviewCount());
    }
}
