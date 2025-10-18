package com.project.skin_me.service.product;

import java.util.List;
import java.util.Optional;

import com.project.skin_me.exception.AlreadyExistsException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.project.skin_me.dto.ImageDto;
import com.project.skin_me.dto.ProductDto;
import com.project.skin_me.exception.ProductNotFoundException;
import com.project.skin_me.exception.ResourceNotFoundException;
import com.project.skin_me.model.Category;
import com.project.skin_me.model.Image;
import com.project.skin_me.model.Product;
import com.project.skin_me.repository.CategoryRepository;
import com.project.skin_me.repository.ImageRepository;
import com.project.skin_me.repository.ProductRepository;
import com.project.skin_me.request.AddProductRequest;
import com.project.skin_me.request.ProductUpdateRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService implements IProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ImageRepository imageRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Product addProduct(AddProductRequest request) {

        if (productExists(request.getBrand(), request.getName())) {
            throw new AlreadyExistsException(request.getBrand() + " " + request.getName() + " already exists, you might need to update");
        }

        Category category;
        if (request.getCategory() != null && request.getCategory().getId() != null) {
            category = categoryRepository.findById(request.getCategory().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + request.getCategory().getId()));
        } else if (request.getCategory() != null && request.getCategory().getName() != null) {
            category = Optional.ofNullable(categoryRepository.findByname(request.getCategory().getName()))
                    .orElseGet(() -> categoryRepository.save(new Category(request.getCategory().getName())));
        } else {
            throw new ResourceNotFoundException("Category information is missing!");
        }

        request.setCategory(category);
        return productRepository.save(createProduct(request, category));

    }

    private boolean productExists(String brand, String name) {
        return productRepository.existsByNameAndBrand(name, brand);
    }

    private Product createProduct(AddProductRequest request, Category category) {
        return new Product(
                request.getName(),
                request.getBrand(),
                request.getPrice(),
                request.getProductType(),
                request.getInventory(),
                request.getDescription(),
                category);
    }

    @Override
    public Product getProductById(Long productId) {
        return productRepository.findById(
                productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found!"));
    }

    @Override
    public void deleteProductById(Long id) {
        productRepository.findById(id)
                .ifPresentOrElse(productRepository::delete, () -> {
                    throw new ProductNotFoundException("Product not found!");
                });
    }

    @Override
    public Product updateProduct(ProductUpdateRequest product, Long productId) {
        return productRepository.findById(productId)
                .map(existingProduct -> updateExistingProduct(existingProduct, product))
                .map(productRepository::save)
                .orElseThrow(() -> new ProductNotFoundException("Product not found!!"));
    }

    private Product updateExistingProduct(Product existingProduct, ProductUpdateRequest request) {
        existingProduct.setName(request.getName());
        existingProduct.setBrand(request.getBrand());
        existingProduct.setPrice(request.getPrice());
        existingProduct.setProductType(request.getProductType());
        existingProduct.setInventory(request.getInventory());
        existingProduct.setDescription(request.getDescription());

        Category category = categoryRepository.findByname(request.getCategory().getName());
        existingProduct.setCategory(category);
        return existingProduct;
    }

    @Override
    public List<Product> getAllProductsByCategory(String category) {
        return productRepository.findByCategory_Name(category);
    }

    @Override
    public List<Product> getProductsByBrand(String brand) {

        return productRepository.findByBrand(brand);
    }

    @Override
    public List<Product> getProductsByName(String name) {
        return productRepository.findByName(name);
    }

    @Override
    public List<Product> getProductsByProductType(String productType) {
        return productRepository.findByProductType(productType);
    }

    @Override
    public List<Product> getProductsByCategoryAndBrand(String category, String brand) {
        return productRepository.findByCategory_NameAndBrand(category, brand);
    }

    @Override
    public List<Product> getProductsByBrandAndName(String brand, String name) {
        return productRepository.findByBrandAndName(brand, name);
    }

    @Override
    public List<Product> getProductsByProductTypeAndName(String productType, String name) {
        return productRepository.findByProductTypeAndName(productType, name);
    }

    @Override
    public Long countProductsByBrandAndName(String brand, String name) {
        return productRepository.countByBrandAndName(brand, name);
    }

    @Override
    public List<ProductDto> getConvertedProducts(List<Product> products) {
        return products.stream().map(this::convertToDto).toList();
    }

    @Override
    public ProductDto convertToDto(Product product) {
        ProductDto productDto = modelMapper.map(product, ProductDto.class);

        List<Image> images = imageRepository.findByProductId(product.getId());
        List<ImageDto> imageDtos = images.stream().map(image -> {
            ImageDto dto = new ImageDto();
            dto.setImageId(image.getId());
            dto.setFileName(image.getFileName());
            dto.setDownloadUrl(image.getDownloadUrl());
            return dto;
        }).toList();

        productDto.setImages(imageDtos);

        return productDto;
    }

    @Override
    public List<Product> getPopularProducts() {
        return productRepository.findAll()
                .stream()
                .filter(p -> p.getPopularProduct() != null)
                .toList();
    }

    // @Override
    // public List<Product> getProductsByCategoryAndProductType(String category,
    // String productType) {
    // throw new UnsupportedOperationException("Unimplemented method
    // 'getProductsByCategoryAndProductType'");
    // }

}
