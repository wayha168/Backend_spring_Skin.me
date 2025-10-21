//package com.project.skin_me.controller;
//
//import com.project.skin_me.model.Category;
//import com.project.skin_me.model.Product;
//import com.project.skin_me.service.category.ICategoryService;
//import com.project.skin_me.service.product.IProductService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.math.BigDecimal;
//import java.util.List;
//
//@Controller
//@RequiredArgsConstructor
//@PreAuthorize("hasRole('ADMIN')")
//public class AdminController {
//
//    private final IProductService productService;
//    private final ICategoryService categoryService;
//    private final ISaleRecordService saleRecordService;
//
//    @GetMapping("/dashboard")
//    public String dashboard(Model model) {
//        List<Product> products = productService.getAllProducts();
//        List<Category> categories = categoryService.getAllCategories();
//        List<SaleRecord> saleRecords = saleRecordService.getAllSaleRecords();
//        model.addAttribute("products", products);
//        model.addAttribute("categories", categories);
//        model.addAttribute("saleRecords", saleRecords);
//        return "dashboard";
//    }
//
//    // Product CRUD
//    @PostMapping("/products/add")
//    public String addProduct(
//            @RequestParam String name,
//            @RequestParam Long categoryId,
//            @RequestParam String brand,
//            @RequestParam BigDecimal price,
//            @RequestParam MultipartFile imageFile,
//            Model model) {
//        try {
//            productService.createProduct(name, categoryId, brand, price, imageFile);
//            return "redirect:/dashboard?success";
//        } catch (Exception e) {
//            model.addAttribute("error", e.getMessage());
//            return "dashboard";
//        }
//    }
//
//    @GetMapping("/products/edit/{id}")
//    public String editProductPage(@PathVariable Long id, Model model) {
//        Product product = productService.getProductById(id)
//                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
//        model.addAttribute("product", product);
//        model.addAttribute("categories", categoryService.getAllCategories());
//        return "product-edit";
//    }
//
//    @PostMapping("/products/edit/{id}")
//    public String editProduct(
//            @PathVariable Long id,
//            @RequestParam String name,
//            @RequestParam Long categoryId,
//            @RequestParam String brand,
//            @RequestParam BigDecimal price,
//            @RequestParam MultipartFile imageFile,
//            Model model) {
//        try {
//            productService.updateProduct(id, name, categoryId, brand, price, imageFile);
//            return "redirect:/dashboard?success";
//        } catch (Exception e) {
//            model.addAttribute("error", e.getMessage());
//            return "product-edit";
//        }
//    }
//
//    @PostMapping("/products/delete/{id}")
//    public String deleteProduct(@PathVariable Long id) {
//        productService.deleteProduct(id);
//        return "redirect:/dashboard?success";
//    }
//
//    // Category CRUD
//    @PostMapping("/categories/add")
//    public String addCategory(@RequestParam String name, Model model) {
//        try {
//            categoryService.createCategory(name);
//            return "redirect:/dashboard?success";
//        } catch (Exception e) {
//            model.addAttribute("error", e.getMessage());
//            return "dashboard";
//        }
//    }
//
//    @GetMapping("/categories/edit/{id}")
//    public String editCategoryPage(@PathVariable Long id, Model model) {
//        Category category = categoryService.getCategoryById(id)
//                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
//        model.addAttribute("category", category);
//        return "category-edit";
//    }
//
//    @PostMapping("/categories/edit/{id}")
//    public String editCategory(@PathVariable Long id, @RequestParam String name, Model model) {
//        try {
//            categoryService.updateCategory(id, name);
//            return "redirect:/dashboard?success";
//        } catch (Exception e) {
//            model.addAttribute("error", e.getMessage());
//            return "category-edit";
//        }
//    }
//
//    @PostMapping("/categories/delete/{id}")
//    public String deleteCategory(@PathVariable Long id) {
//        categoryService.deleteCategory(id);
//        return "redirect:/dashboard?success";
//    }
//
//    // Sale Record CRUD
//    @PostMapping("/sales/add")
//    public String addSaleRecord(
//            @RequestParam Long productId,
//            @RequestParam Integer quantity,
//            @RequestParam BigDecimal totalPrice,
//            Model model) {
//        try {
//            saleRecordService.createSaleRecord(productId, quantity, totalPrice);
//            return "redirect:/dashboard?success";
//        } catch (Exception e) {
//            model.addAttribute("error", e.getMessage());
//            return "dashboard";
//        }
//    }
//
//    @GetMapping("/sales/edit/{id}")
//    public String editSaleRecordPage(@PathVariable Long id, Model model) {
//        SaleRecord saleRecord = saleRecordService.getSaleRecordById(id)
//                .orElseThrow(() -> new IllegalArgumentException("Sale record not found"));
//        model.addAttribute("saleRecord", saleRecord);
//        model.addAttribute("products", productService.getAllProducts());
//        return "sale-edit";
//    }
//
//    @PostMapping("/sales/edit/{id}")
//    public String editSaleRecord(
//            @PathVariable Long id,
//            @RequestParam Long productId,
//            @RequestParam Integer quantity,
//            @RequestParam BigDecimal totalPrice,
//            Model model) {
//        try {
//            saleRecordService.updateSaleRecord(id, productId, quantity, totalPrice);
//            return "redirect:/dashboard?success";
//        } catch (Exception e) {
//            model.addAttribute("error", e.getMessage());
//            return "sale-edit";
//        }
//    }
//
//    @PostMapping("/sales/delete/{id}")
//    public String deleteSaleRecord(@PathVariable Long id) {
//        saleRecordService.deleteSaleRecord(id);
//        return "redirect:/dashboard?success";
//    }
//}