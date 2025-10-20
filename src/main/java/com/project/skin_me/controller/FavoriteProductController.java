package com.project.skin_me.controller;

import com.project.skin_me.dto.FavoriteProductDto;
import com.project.skin_me.exception.AlreadyExistsException;
import com.project.skin_me.exception.ResourceNotFoundException;
import com.project.skin_me.model.Product;
import com.project.skin_me.response.ApiResponse;
import com.project.skin_me.service.favorite.IFavoriteProductService;
import com.project.skin_me.service.product.IProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/favorites")
public class FavoriteProductController {

    private final IFavoriteProductService favoriteProductService;
    private final IProductService productService;

    // Add favorite
    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addFavorite(@RequestParam Long userId,
                                                   @RequestParam Long productId) {
        try {
            FavoriteProductDto favoriteDto = favoriteProductService.addFavorite(userId, productId);
            return ResponseEntity.ok(new ApiResponse("Product added to favorites successfully", favoriteDto));
        } catch (AlreadyExistsException e) {
            return ResponseEntity.status(CONFLICT)
                    .body(new ApiResponse(e.getMessage(), "Error Occurred!"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(e.getMessage(), "Error Occurred!"));
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse> getFavoritesByUser(@PathVariable Long userId) {
        try {
            List<FavoriteProductDto> favorites = favoriteProductService.getFavoritesByUser(userId);

            // Map each favorite to include full product object
            List<FavoriteProductDto> favoritesWithFullProduct = favorites.stream().map(fav -> {
                try {
                    Product product = productService.getProductById(fav.getProductId());
                    fav.setProduct(product);
                } catch (Exception e) {
                    fav.setProduct(null); // fallback
                }
                return fav;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(new ApiResponse("Favorites retrieved successfully", favoritesWithFullProduct));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Oops!", e.getMessage()));
        }
    }

    @DeleteMapping("/remove")
    public ResponseEntity<ApiResponse> removeFavorite(@RequestParam Long userId,
                                                      @RequestParam Long productId) {
        try {
            favoriteProductService.removeFavorite(userId, productId);
            return ResponseEntity.ok(new ApiResponse("Product removed from favorites successfully", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Oops!", e.getMessage()));
        }
    }

    @GetMapping("/favorite/all")
    public ResponseEntity<ApiResponse> getAllFavorites() {
        List<FavoriteProductDto> favorites = favoriteProductService.getAllFavorites();

        // Map each favorite to include full product object
        List<FavoriteProductDto> favoritesWithFullProduct = favorites.stream().map(fav -> {
            try {
                Product product = productService.getProductById(fav.getProductId());
                fav.setProduct(product);
            } catch (ResourceNotFoundException e) {
                fav.setProduct(null);
            }
            return fav;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(new ApiResponse("All favorites retrieved successfully", favoritesWithFullProduct));
    }
}
