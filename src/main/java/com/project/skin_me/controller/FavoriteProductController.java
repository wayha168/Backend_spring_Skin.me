package com.project.skin_me.controller;

import com.project.skin_me.dto.FavoriteProductDto;
import com.project.skin_me.exception.AlreadyExistsException;
import com.project.skin_me.exception.ResourceNotFoundException;
import com.project.skin_me.response.ApiResponse;
import com.project.skin_me.service.favorite.IFavoriteProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/favorites")
public class FavoriteProductController {

    private final IFavoriteProductService favoriteProductService;

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
            return ResponseEntity.ok(new ApiResponse("Favorites retrieved successfully", favorites));
        } catch (ResourceNotFoundException e) {
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
}
