package com.project.skin_me.service.favorite;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.skin_me.dto.FavoriteProductDto;
import com.project.skin_me.exception.AlreadyExistsException;
import com.project.skin_me.exception.ResourceNotFoundException;
import com.project.skin_me.model.FavoriteProduct;
import com.project.skin_me.model.Product;
import com.project.skin_me.model.User;
import com.project.skin_me.repository.FavoriteProductRepository;
import com.project.skin_me.repository.ProductRepository;
import com.project.skin_me.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FavoriteProductService implements IFavoriteProductService {

    private final FavoriteProductRepository favoriteProductRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public FavoriteProductDto addFavorite(Long userId, Long productId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        // Prevent duplicate
        favoriteProductRepository.findByUserAndProduct(user, product)
                .ifPresent(fav -> {
                    throw new AlreadyExistsException("Product already in favorites");
                });

        FavoriteProduct favorite = FavoriteProduct.builder()
                .user(user)
                .product(product)
                .build();
        favoriteProductRepository.save(favorite);

        return convertToDto(favorite);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FavoriteProductDto> getFavoritesByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<FavoriteProduct> favorites = favoriteProductRepository.findByUser(user);
        return favorites.stream().map(this::convertToDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FavoriteProductDto> getAllFavorites() {
        List<FavoriteProduct> favorites = favoriteProductRepository.findAll();
        return favorites.stream().map(this::convertToDto).toList();
    }

    @Override
    @Transactional
    public void removeFavorite(Long userId, Long productId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        favoriteProductRepository.deleteByUserAndProduct(user, product);
    }

    @Override
    public FavoriteProductDto convertToDto(FavoriteProduct favoriteProduct) {
        return FavoriteProductDto.builder()
                .id(favoriteProduct.getId())
                .userId(favoriteProduct.getUser().getId())
                .productId(favoriteProduct.getProduct().getId())
                .productName(favoriteProduct.getProduct().getName())
                .productBrand(favoriteProduct.getProduct().getBrand())
                .build();
    }
}
