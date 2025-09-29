package com.project.skin_me.service.image;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.project.skin_me.dto.ImageDto;
import com.project.skin_me.model.Image;

public interface IImageService {
    Image getImageById(Long id);

    void deleteImageById(Long id);

    List<ImageDto> saveImages(Long productId, List<MultipartFile> files);

    void updateImage(MultipartFile file, Long imageId);
}
