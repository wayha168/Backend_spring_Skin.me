package com.project.skin_me.service.category;

import java.util.List;
import com.project.skin_me.model.Category;

public interface ICategoryService {
    Category getCategoryById(Long id);

    Category getCategoryByName(String name);

    List<Category> getAllCategories();

    Category addCategory(Category category);

    Category updateCategory(Category category);

    void deleteCategoryById(Long id);

}
