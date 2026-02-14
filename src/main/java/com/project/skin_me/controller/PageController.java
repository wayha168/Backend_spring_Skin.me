package com.project.skin_me.controller;

import com.project.skin_me.dto.OrderDto;
import com.project.skin_me.model.Category;
import com.project.skin_me.model.Order;
import com.project.skin_me.model.Payment;
import com.project.skin_me.model.Product;
import com.project.skin_me.model.User;
import com.project.skin_me.repository.OrderRepository;
import com.project.skin_me.repository.PaymentRepository;
import com.project.skin_me.repository.RoleRepository;
import com.project.skin_me.repository.UserRepository;
import com.project.skin_me.request.CreateUserRequest;
import com.project.skin_me.request.UserUpdateRequest;
import com.project.skin_me.model.Role;
import com.project.skin_me.service.category.ICategoryService;
import com.project.skin_me.service.order.IOrderService;
import com.project.skin_me.service.product.IProductService;
import com.project.skin_me.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ModelAttribute;
import com.project.skin_me.dto.ProductDto;
import com.project.skin_me.request.AddProductRequest;
import com.project.skin_me.request.ProductUpdateRequest;
import com.project.skin_me.enums.ProductStatus;

@Controller
@RequiredArgsConstructor
public class PageController {

    private final ICategoryService categoryService;
    private final IProductService productService;
    private final IOrderService orderService;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final IUserService userService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @org.springframework.beans.factory.annotation.Value("${stripe.public.key}")
    private String stripePublicKey;

    @GetMapping("/login-page")
    public String loginPage(Model model, 
                           @RequestParam(required = false) String error,
                           @RequestParam(required = false) String expired,
                           @RequestParam(required = false) String logout) {
        // If user is already authenticated, redirect to dashboard
        org.springframework.security.core.Authentication auth = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && 
            !(auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {
            return "redirect:/dashboard";
        }
        
        if (expired != null) {
            model.addAttribute("error", "Your session has expired. Please login again.");
        } else if (logout != null) {
            model.addAttribute("success", "You have been logged out successfully.");
        } else if (error != null) {
            model.addAttribute("error", "Invalid credentials. Please try again.");
        }
        
        return "auth/login";
    }

    @GetMapping("/")
    public String homePage() {
        // Check if user is authenticated
        org.springframework.security.core.Authentication auth = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && 
            !(auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {
            return "redirect:/dashboard";
        }
        return "redirect:/login-page";
    }

    @GetMapping("/logout")
    public String logoutPage() {
        return "redirect:/login-page?logout";
    }

    @GetMapping("/signup")
    public String signupPage() {
        return "signup";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage() {
        return "reset-password";
    }

    @GetMapping("/dashboard")
    @PreAuthorize("isAuthenticated()")
    public String dashboard(Model model) {
        try {
            List<Product> products = productService.getAllProducts();
            List<OrderDto> orders = orderService.getAllUserOrders();
            int totalUsers = userService.getAllUsers().size();
            List<Payment> payments = paymentRepository.findAll();
            double totalRevenue = payments.stream()
                    .mapToDouble(p -> p.getAmount() != null ? p.getAmount().doubleValue() : 0)
                    .sum();

            // Calculate new registrations
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            java.time.LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
            java.time.LocalDateTime weekStart = now.minusDays(7);
            java.time.LocalDateTime monthStart = now.minusDays(30);
            
            long newUsersToday = userRepository.findAll().stream()
                    .filter(u -> u.getRegistrationDate() != null && 
                            !u.getRegistrationDate().isBefore(todayStart))
                    .count();
            
            long newUsersThisWeek = userRepository.findAll().stream()
                    .filter(u -> u.getRegistrationDate() != null && 
                            !u.getRegistrationDate().isBefore(weekStart))
                    .count();
            
            long newUsersThisMonth = userRepository.findAll().stream()
                    .filter(u -> u.getRegistrationDate() != null && 
                            !u.getRegistrationDate().isBefore(monthStart))
                    .count();

            model.addAttribute("totalProducts", products.size());
            model.addAttribute("totalOrders", orders.size());
            model.addAttribute("totalUsers", totalUsers);
            model.addAttribute("totalRevenue", totalRevenue);
            model.addAttribute("newUsersToday", newUsersToday);
            model.addAttribute("newUsersThisWeek", newUsersThisWeek);
            model.addAttribute("newUsersThisMonth", newUsersThisMonth);
        } catch (Exception e) {
            model.addAttribute("totalProducts", 0);
            model.addAttribute("totalOrders", 0);
            model.addAttribute("totalUsers", 0);
            model.addAttribute("totalRevenue", 0.0);
            model.addAttribute("newUsersToday", 0);
            model.addAttribute("newUsersThisWeek", 0);
            model.addAttribute("newUsersThisMonth", 0);
            model.addAttribute("error", "Failed to load stats: " + e.getMessage());
        }
        model.addAttribute("pageTitle", "Admin Dashboard");
        return "dashboard";
    }

    @GetMapping("/product")
    @PreAuthorize("hasRole('ADMIN')")
    public String productPage(Model model) {
        model.addAttribute("pageTitle", "Products");
        return "product";
    }

    @GetMapping("/view/categories")
    @PreAuthorize("isAuthenticated()")
    public String getAllCategoriesView(Model model) {
        try {
            List<Category> categories = categoryService.getAllCategories();
            model.addAttribute("categories", categories);
            model.addAttribute("pageTitle", "Categories Management");
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load categories: " + e.getMessage());
            model.addAttribute("categories", List.<Category>of());
        }
        return "categories";
    }

    @GetMapping("/view/categories/{categoryId}")
    @PreAuthorize("isAuthenticated()")
    public String getCategoryByIdView(@PathVariable Long categoryId, Model model) {
        try {
            Category category = categoryService.getCategoryById(categoryId);
            model.addAttribute("category", category);
            model.addAttribute("pageTitle", "Category Details");
        } catch (Exception e) {
            model.addAttribute("error", "Category not found: " + e.getMessage());
            model.addAttribute("pageTitle", "Category Not Found");
        }
        return "category-details";
    }

    @GetMapping("/views/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public String categoriesPage(Model model) {
        try {
            List<Category> categories = categoryService.getAllCategories();
            model.addAttribute("categories", categories);
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load categories: " + e.getMessage());
            model.addAttribute("categories", List.<Category>of());
        }
        model.addAttribute("pageTitle", "Categories Management");
        return "categories";
    }

    // Category CRUD Endpoints
    @GetMapping("/views/categories/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String createCategoryPage(Model model) {
        model.addAttribute("pageTitle", "Create Category");
        model.addAttribute("category", null);
        return "category-form";
    }

    @PostMapping("/views/categories/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String createCategory(@RequestParam String name, Model model) {
        try {
            Category category = new Category(name);
            categoryService.addCategory(category);
            return "redirect:/views/categories?success=Category created successfully";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to create category: " + e.getMessage());
            model.addAttribute("pageTitle", "Create Category");
            return "category-form";
        }
    }

    @GetMapping("/views/categories/{categoryId}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String editCategoryPage(@PathVariable Long categoryId, Model model) {
        try {
            Category category = categoryService.getCategoryById(categoryId);
            model.addAttribute("category", category);
            model.addAttribute("pageTitle", "Edit Category");
            return "category-form";
        } catch (Exception e) {
            return "redirect:/views/categories?error=Category not found";
        }
    }

    @PostMapping("/views/categories/{categoryId}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateCategory(@PathVariable Long categoryId, @RequestParam String name, Model model) {
        try {
            Category category = new Category(name);
            categoryService.updateCategory(category, categoryId);
            return "redirect:/views/categories?success=Category updated successfully";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to update category: " + e.getMessage());
            model.addAttribute("pageTitle", "Edit Category");
            try {
                Category category = categoryService.getCategoryById(categoryId);
                model.addAttribute("category", category);
            } catch (Exception ex) {
                // Ignore
            }
            return "category-form";
        }
    }

    @PostMapping("/views/categories/{categoryId}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteCategory(@PathVariable Long categoryId) {
        try {
            categoryService.deleteCategoryById(categoryId);
            return "redirect:/views/categories?success=Category deleted successfully";
        } catch (Exception e) {
            return "redirect:/views/categories?error=Failed to delete category: " + e.getMessage();
        }
    }

    @GetMapping("/views/products")
    @PreAuthorize("hasRole('ADMIN')")
    public String productsListPage(Model model) {
        try {
            List<Product> products = productService.getAllProducts();
            List<Category> categories = categoryService.getAllCategories();
            model.addAttribute("products", products);
            model.addAttribute("categories", categories);
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load products: " + e.getMessage());
            model.addAttribute("products", List.<Product>of());
            model.addAttribute("categories", List.<Category>of());
        }
        model.addAttribute("pageTitle", "Products Management");
        return "products";
    }

    // Product CRUD Endpoints
    @GetMapping("/views/products/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String createProductPage(Model model) {
        try {
            List<Category> categories = categoryService.getAllCategories();
            model.addAttribute("categories", categories);
            model.addAttribute("product", null);
            model.addAttribute("pageTitle", "Create Product");
            return "product-form";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load form: " + e.getMessage());
            return "redirect:/views/products";
        }
    }

    @PostMapping("/views/products/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String createProduct(
            @RequestParam String name,
            @RequestParam String brand,
            @RequestParam java.math.BigDecimal price,
            @RequestParam String productType,
            @RequestParam int inventory,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String howToUse,
            @RequestParam Long categoryId,
            Model model) {
        try {
            Category category = categoryService.getCategoryById(categoryId);
            AddProductRequest request = new AddProductRequest();
            request.setName(name);
            request.setBrand(brand);
            request.setPrice(price);
            request.setProductType(productType);
            request.setInventory(inventory);
            request.setDescription(description != null ? description : "");
            request.setHowToUse(howToUse != null ? howToUse : "");
            Category cat = new Category();
            cat.setId(categoryId);
            request.setCategory(cat);
            
            productService.addProduct(request);
            return "redirect:/views/products?success=Product created successfully";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to create product: " + e.getMessage());
            try {
                List<Category> categories = categoryService.getAllCategories();
                model.addAttribute("categories", categories);
            } catch (Exception ex) {
                // Ignore
            }
            model.addAttribute("pageTitle", "Create Product");
            return "product-form";
        }
    }

    @GetMapping("/views/products/{productId}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String editProductPage(@PathVariable Long productId, Model model) {
        try {
            Product product = productService.getProductById(productId);
            List<Category> categories = categoryService.getAllCategories();
            model.addAttribute("product", product);
            model.addAttribute("categories", categories);
            model.addAttribute("pageTitle", "Edit Product");
            return "product-form";
        } catch (Exception e) {
            return "redirect:/views/products?error=Product not found";
        }
    }

    @PostMapping("/views/products/{productId}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateProduct(
            @PathVariable Long productId,
            @RequestParam String name,
            @RequestParam String brand,
            @RequestParam java.math.BigDecimal price,
            @RequestParam String productType,
            @RequestParam int inventory,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String howToUse,
            @RequestParam Long categoryId,
            @RequestParam(required = false) String status,
            Model model) {
        try {
            Category category = categoryService.getCategoryById(categoryId);
            ProductUpdateRequest request = new ProductUpdateRequest();
            request.setName(name);
            request.setBrand(brand);
            request.setPrice(price);
            request.setProductType(productType);
            request.setInventory(inventory);
            request.setDescription(description != null ? description : "");
            request.setHowToUse(howToUse != null ? howToUse : "");
            Category cat = new Category();
            cat.setId(categoryId);
            request.setCategory(cat);
            if (status != null) {
                request.setStatus(ProductStatus.valueOf(status));
            }
            
            productService.updateProduct(request, productId);
            return "redirect:/views/products?success=Product updated successfully";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to update product: " + e.getMessage());
            try {
                Product product = productService.getProductById(productId);
                List<Category> categories = categoryService.getAllCategories();
                model.addAttribute("product", product);
                model.addAttribute("categories", categories);
            } catch (Exception ex) {
                // Ignore
            }
            model.addAttribute("pageTitle", "Edit Product");
            return "product-form";
        }
    }

    @PostMapping("/views/products/{productId}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteProduct(@PathVariable Long productId) {
        try {
            productService.deleteProductById(productId);
            return "redirect:/views/products?success=Product deleted successfully";
        } catch (Exception e) {
            return "redirect:/views/products?error=Failed to delete product: " + e.getMessage();
        }
    }

    @GetMapping("/view/products")
    @PreAuthorize("isAuthenticated()")
    public String getAllProductsView(Model model) {
        try {
            List<Product> products = productService.getAllProducts();
            List<ProductDto> productDtos = productService.getConvertedProducts(products);
            List<Category> categories = categoryService.getAllCategories();
            model.addAttribute("products", productDtos);
            model.addAttribute("categories", categories);
            model.addAttribute("pageTitle", "Products Management");
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load products: " + e.getMessage());
            model.addAttribute("products", List.<ProductDto>of());
            model.addAttribute("categories", List.<Category>of());
        }
        return "products";
    }

    @GetMapping("/view/products/{productId}")
    @PreAuthorize("isAuthenticated()")
    public String getProductByIdView(@PathVariable Long productId, Model model) {
        try {
            Product product = productService.getProductById(productId);
            List<Category> categories = categoryService.getAllCategories();
            model.addAttribute("product", product);
            model.addAttribute("categories", categories);
            model.addAttribute("pageTitle", "Product Details");
        } catch (Exception e) {
            model.addAttribute("error", "Product not found: " + e.getMessage());
            model.addAttribute("pageTitle", "Product Not Found");
        }
        return "product-details";
    }

    @GetMapping("/view/products/category/{categoryId}")
    @PreAuthorize("isAuthenticated()")
    public String getProductsByCategory(@PathVariable Long categoryId, Model model) {
        try {
            Category category = categoryService.getCategoryById(categoryId);
            
            List<Product> products = productService.getAllProductsByCategory(category.getName());
            List<ProductDto> productDtos = productService.getConvertedProducts(products);
            
            model.addAttribute("products", productDtos);
            model.addAttribute("category", category);
            model.addAttribute("pageTitle", category.getName() + " Products");
            model.addAttribute("pageIcon", "bi-box-seam");
        } catch (com.project.skin_me.exception.ResourceNotFoundException e) {
            model.addAttribute("error", "Category not found: " + e.getMessage());
            model.addAttribute("products", List.<ProductDto>of());
            model.addAttribute("category", null);
            model.addAttribute("pageTitle", "Category Not Found");
            model.addAttribute("pageIcon", "bi-exclamation-triangle");
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load products: " + e.getMessage());
            model.addAttribute("products", List.<ProductDto>of());
            model.addAttribute("category", null);
            model.addAttribute("pageTitle", "Products Error");
            model.addAttribute("pageIcon", "bi-exclamation-triangle");
        }
        return "products-by-category";
    }

    @GetMapping("/views/products/product-details")
    public String getProductDetailsPage(@RequestParam Long productId, Model model) {
        try {
            Product product = productService.getProductById(productId);
            model.addAttribute("product", product);
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load product details: " + e.getMessage());
        }
        model.addAttribute("pageTitle", "Product Details");
        return "product-details";
    }

    @PostMapping("/views/products/add-product")
    public String postAddProduct(@RequestBody String entity) {
        paymentRepository.saveAll(paymentRepository.findAll());
        return entity;
    }

    @GetMapping("/view/orders")
    @PreAuthorize("isAuthenticated()")
    public String getAllOrdersView(Model model) {
        try {
            List<OrderDto> orderDtos = orderService.getAllUserOrders();
            long completedCount = orderDtos.stream()
                    .filter(o -> o.getOrderStatus() != null && o.getOrderStatus().toString().equals("COMPLETED"))
                    .count();
            long pendingCount = orderDtos.stream()
                    .filter(o -> o.getOrderStatus() != null && o.getOrderStatus().toString().equals("PAYMENT_PENDING"))
                    .count();
            model.addAttribute("orders", orderDtos);
            model.addAttribute("pageTitle", "All Orders");
            model.addAttribute("totalOrders", orderDtos.size());
            model.addAttribute("completedOrders", completedCount);
            model.addAttribute("pendingPaymentOrders", pendingCount);
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load orders: " + e.getMessage());
            model.addAttribute("orders", List.<OrderDto>of());
            model.addAttribute("totalOrders", 0);
            model.addAttribute("completedOrders", 0);
            model.addAttribute("pendingPaymentOrders", 0);
        }
        return "orders";
    }

    @GetMapping("/view/orders/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public String getOrderByIdView(@PathVariable Long orderId, Model model) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));
            model.addAttribute("order", order);
            model.addAttribute("pageTitle", "Order Details #" + orderId);
        } catch (Exception e) {
            model.addAttribute("error", "Order not found: " + e.getMessage());
            model.addAttribute("pageTitle", "Order Not Found");
        }
        return "order-details";
    }

    @GetMapping("/views/orders")
    @PreAuthorize("hasRole('ADMIN')")
    public String ordersListPage(Model model) {
        try {
            List<OrderDto> orders = orderService.getAllUserOrders();
            long completedOrders = orders.stream()
                    .filter(o -> o.getOrderStatus() != null && o.getOrderStatus().toString().equals("COMPLETED"))
                    .count();
            long pendingPaymentOrders = orders.stream()
                    .filter(o -> o.getOrderStatus() != null && o.getOrderStatus().toString().equals("PAYMENT_PENDING"))
                    .count();
            model.addAttribute("orders", orders);
            model.addAttribute("totalOrders", orders.size());
            model.addAttribute("completedOrders", completedOrders);
            model.addAttribute("pendingPaymentOrders", pendingPaymentOrders);
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load orders: " + e.getMessage());
            model.addAttribute("orders", List.<OrderDto>of());
            model.addAttribute("totalOrders", 0);
            model.addAttribute("completedOrders", 0);
            model.addAttribute("pendingPaymentOrders", 0);
        }
        model.addAttribute("pageTitle", "All Orders");
        return "orders";
    }

    @GetMapping("/view/orders/my-orders")
    @PreAuthorize("hasRole('USER')")
    public String getMyOrdersView(Authentication authentication, Model model) {
        try {
            User user = userService.getAuthenticatedUser();
            List<OrderDto> orderDtos = orderService.getUserOrders(user.getId());
            model.addAttribute("orders", orderDtos);
            model.addAttribute("pageTitle", "My Orders");
            model.addAttribute("totalOrders", orderDtos.size());
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load your orders: " + e.getMessage());
            model.addAttribute("orders", List.<OrderDto>of());
            model.addAttribute("totalOrders", 0);
        }
        return "my-orders";
    }

    @GetMapping("/views/my-orders")
    @PreAuthorize("isAuthenticated()")
    public String myOrdersListPage(Authentication authentication, Model model) {
        try {
            User user = userService.getAuthenticatedUser();
            List<OrderDto> orders = orderService.getUserOrders(user.getId());
            long deliveredOrders = orders.stream()
                    .filter(o -> o.getOrderStatus() != null && o.getOrderStatus().equals("COMPLETED"))
                    .count();
            model.addAttribute("orders", orders);
            model.addAttribute("totalOrders", orders.size());
            model.addAttribute("deliveredOrders", deliveredOrders);
            model.addAttribute("pageTitle", "My Orders");
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load your orders: " + e.getMessage());
            model.addAttribute("orders", List.<OrderDto>of());
            model.addAttribute("totalOrders", 0);
            model.addAttribute("deliveredOrders", 0);
            model.addAttribute("pageTitle", "My Orders");
        }
        return "my-orders";
    }

    @GetMapping("/view/payments")
    @PreAuthorize("isAuthenticated()")
    public String getAllPaymentsView(Model model) {
        try {
            List<Payment> payments = paymentRepository.findAll();
            model.addAttribute("payments", payments);
            model.addAttribute("pageTitle", "Payments Record");
            model.addAttribute("totalPayments", payments.size());
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load payments: " + e.getMessage());
            model.addAttribute("payments", List.<Payment>of());
            model.addAttribute("totalPayments", 0);
        }
        return "payments";
    }

    @GetMapping("/view/payments/{paymentId}")
    @PreAuthorize("isAuthenticated()")
    public String getPaymentByIdView(@PathVariable Long paymentId, Model model) {
        try {
            Payment payment = paymentRepository.findById(paymentId).orElse(null);
            if (payment == null) {
                model.addAttribute("error", "Payment not found");
                model.addAttribute("pageTitle", "Payment Not Found");
                return "payment-details";
            }
            model.addAttribute("payment", payment);
            model.addAttribute("pageTitle", "Payment Details #" + paymentId);
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load payment: " + e.getMessage());
            model.addAttribute("pageTitle", "Payment Error");
        }
        return "payment-details";
    }

    @GetMapping("/view/payments/my-payments")
    @PreAuthorize("hasRole('USER')")
    public String getMyPaymentsView(Authentication authentication, Model model) {
        try {
            User user = userService.getAuthenticatedUser();
            List<Payment> payments = paymentRepository.findByOrderUserId(user.getId());
            double totalPaymentAmount = payments.stream()
                    .mapToDouble(p -> p.getAmount() != null ? p.getAmount().doubleValue() : 0)
                    .sum();
            model.addAttribute("payments", payments);
            model.addAttribute("pageTitle", "My Payments");
            model.addAttribute("totalPayments", payments.size());
            model.addAttribute("totalPaymentAmount", totalPaymentAmount);
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load your payments: " + e.getMessage());
            model.addAttribute("payments", List.<Payment>of());
            model.addAttribute("totalPayments", 0);
            model.addAttribute("totalPaymentAmount", 0.0);
        }
        return "my-payments";
    }

    @GetMapping("/checkout/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public String checkoutPage(@PathVariable Long orderId, Model model) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("Order not found"));
            model.addAttribute("order", order);
            model.addAttribute("pageTitle", "Checkout");
            model.addAttribute("stripePublicKey", stripePublicKey);
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load order: " + e.getMessage());
            model.addAttribute("pageTitle", "Checkout Error");
        }
        return "checkout";
    }

    @GetMapping("/views/payments")
    @PreAuthorize("isAuthenticated()")
    public String paymentsListPage(Model model) {
        try {
            List<Payment> payments = paymentRepository.findAll();
            long completedPayments = payments.stream()
                    .filter(p -> p.getStatus() != null && p.getStatus().toString().equals("COMPLETED"))
                    .count();
            double totalPaymentAmount = payments.stream()
                    .mapToDouble(p -> p.getAmount() != null ? p.getAmount().doubleValue() : 0)
                    .sum();
            model.addAttribute("payments", payments);
            model.addAttribute("totalPayments", payments.size());
            model.addAttribute("completedPayments", completedPayments);
            model.addAttribute("totalPaymentAmount", totalPaymentAmount);
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load payments: " + e.getMessage());
            model.addAttribute("payments", List.<Payment>of());
            model.addAttribute("totalPayments", 0);
            model.addAttribute("completedPayments", 0);
            model.addAttribute("totalPaymentAmount", 0.0);
        }
        model.addAttribute("pageTitle", "Payments Record");
        return "payments";
    }

    @GetMapping("/views/my-payments")
    @PreAuthorize("isAuthenticated()")
    public String myPaymentsListPage(Authentication authentication, Model model) {
        try {
            User user = userService.getAuthenticatedUser();
            List<Payment> payments = paymentRepository.findByOrderUserId(user.getId());
            double totalPaymentAmount = payments.stream()
                    .mapToDouble(p -> p.getAmount() != null ? p.getAmount().doubleValue() : 0)
                    .sum();
            model.addAttribute("payments", payments);
            model.addAttribute("totalPayments", payments.size());
            model.addAttribute("totalPaymentAmount", totalPaymentAmount);
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load your payments: " + e.getMessage());
            model.addAttribute("payments", List.<Payment>of());
            model.addAttribute("totalPayments", 0);
            model.addAttribute("totalPaymentAmount", 0.0);
        }
        model.addAttribute("pageTitle", "My Payments");
        return "my-payments";
    }

    @GetMapping("/views/delivery")
    @PreAuthorize("isAuthenticated()")
    public String deliveryListPage(Model model) {
        try {
            List<Order> allOrders = orderRepository.findAll();
            long shippedOrders = allOrders.stream()
                    .filter(o -> o.getOrderStatus() != null && 
                            (o.getOrderStatus().toString().equals("SHIPPED") || 
                             o.getOrderStatus().toString().equals("DELIVERED")))
                    .count();
            long deliveredOrders = allOrders.stream()
                    .filter(o -> o.getOrderStatus() != null && o.getOrderStatus().toString().equals("DELIVERED"))
                    .count();
            long pendingDeliveries = allOrders.stream()
                    .filter(o -> o.getOrderStatus() != null && 
                            (o.getOrderStatus().toString().equals("COMPLETED") || 
                             o.getOrderStatus().toString().equals("PAYMENT_PENDING")))
                    .count();
            
            model.addAttribute("deliveries", allOrders);
            model.addAttribute("totalDeliveries", allOrders.size());
            model.addAttribute("shippedOrders", shippedOrders);
            model.addAttribute("deliveredOrders", deliveredOrders);
            model.addAttribute("pendingDeliveries", pendingDeliveries);
            model.addAttribute("pageTitle", "Delivery Records");
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load deliveries: " + e.getMessage());
            model.addAttribute("deliveries", List.<Order>of());
            model.addAttribute("totalDeliveries", 0);
            model.addAttribute("shippedOrders", 0);
            model.addAttribute("deliveredOrders", 0);
            model.addAttribute("pendingDeliveries", 0);
        }
        return "delivery";
    }

    @GetMapping("/views/users")
    @PreAuthorize("hasRole('ADMIN')")
    public String usersListPage(Model model) {
        try {
            List<User> users = userService.getAllUsers().stream()
                    .map(dto -> userService.getUserById(dto.getId()))
                    .collect(java.util.stream.Collectors.toList());
            model.addAttribute("users", users);
            model.addAttribute("totalUsers", users.size());
            model.addAttribute("pageTitle", "User Management");
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load users: " + e.getMessage());
            model.addAttribute("users", List.<User>of());
            model.addAttribute("totalUsers", 0);
        }
        return "users";
    }

    @GetMapping("/views/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public String getUserDetailsPage(@PathVariable Long userId, Model model) {
        try {
            User user = userService.getUserById(userId);
            List<Role> allRoles = roleRepository.findAll();
            model.addAttribute("user", user);
            model.addAttribute("allRoles", allRoles);
            model.addAttribute("pageTitle", "User Details");
        } catch (Exception e) {
            model.addAttribute("error", "User not found: " + e.getMessage());
            model.addAttribute("pageTitle", "User Not Found");
        }
        return "user-details";
    }

    @GetMapping("/views/users/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String createUserPage(Model model) {
        try {
            List<Role> allRoles = roleRepository.findAll();
            model.addAttribute("allRoles", allRoles);
            model.addAttribute("pageTitle", "Create User");
            model.addAttribute("user", null);
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load roles: " + e.getMessage());
        }
        return "user-form";
    }

    @PostMapping("/views/users/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String createUser(@ModelAttribute CreateUserRequest request, @RequestParam(required = false) String roleName, Model model) {
        try {
            // Handle role assignment from form
            if (roleName != null && !roleName.isEmpty()) {
                com.project.skin_me.dto.RoleDto roleDto = new com.project.skin_me.dto.RoleDto();
                roleDto.setName(roleName);
                request.setRole(roleDto);
            }
            userService.createUser(request);
            return "redirect:/views/users?success=User created successfully";
        } catch (Exception e) {
            List<Role> allRoles = roleRepository.findAll();
            model.addAttribute("allRoles", allRoles);
            model.addAttribute("error", "Failed to create user: " + e.getMessage());
            model.addAttribute("pageTitle", "Create User");
            model.addAttribute("user", null);
            return "user-form";
        }
    }

    @GetMapping("/views/users/{userId}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String editUserPage(@PathVariable Long userId, Model model) {
        try {
            User user = userService.getUserById(userId);
            List<Role> allRoles = roleRepository.findAll();
            model.addAttribute("user", user);
            model.addAttribute("allRoles", allRoles);
            model.addAttribute("pageTitle", "Edit User");
        } catch (Exception e) {
            model.addAttribute("error", "User not found: " + e.getMessage());
            model.addAttribute("pageTitle", "User Not Found");
            return "redirect:/views/users";
        }
        return "user-form";
    }

    @PostMapping("/views/users/{userId}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateUser(@PathVariable Long userId, @ModelAttribute UserUpdateRequest request, Model model) {
        try {
            // Handle enabled checkbox - Spring will set it to true if checkbox is checked, null otherwise
            // We need to preserve current state if not provided
            if (request.getEnabled() == null) {
                User currentUser = userService.getUserById(userId);
                request.setEnabled(currentUser.isEnabled());
            }
            userService.updateUser(request, userId);
            return "redirect:/views/users/" + userId + "?success=User updated successfully";
        } catch (Exception e) {
            try {
                User user = userService.getUserById(userId);
                List<Role> allRoles = roleRepository.findAll();
                model.addAttribute("user", user);
                model.addAttribute("allRoles", allRoles);
                model.addAttribute("error", "Failed to update user: " + e.getMessage());
                model.addAttribute("pageTitle", "Edit User");
            } catch (Exception ex) {
                return "redirect:/views/users?error=User not found";
            }
            return "user-form";
        }
    }

    @PostMapping("/views/users/{userId}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteUser(@PathVariable Long userId) {
        try {
            userService.deleteUser(userId);
            return "redirect:/views/users?success=User deleted successfully";
        } catch (Exception e) {
            return "redirect:/views/users?error=Failed to delete user: " + e.getMessage();
        }
    }

    @PostMapping("/views/users/{userId}/assign-role")
    @PreAuthorize("hasRole('ADMIN')")
    public String assignRole(@PathVariable Long userId, @RequestParam String roleName) {
        try {
            userService.assignRole(userId, roleName);
            return "redirect:/views/users/" + userId + "?success=Role assigned successfully";
        } catch (Exception e) {
            return "redirect:/views/users/" + userId + "?error=Failed to assign role: " + e.getMessage();
        }
    }

    @PostMapping("/views/users/{userId}/remove-role")
    @PreAuthorize("hasRole('ADMIN')")
    public String removeRole(@PathVariable Long userId, @RequestParam String roleName) {
        try {
            userService.removeRole(userId, roleName);
            return "redirect:/views/users/" + userId + "?success=Role removed successfully";
        } catch (Exception e) {
            return "redirect:/views/users/" + userId + "?error=Failed to remove role: " + e.getMessage();
        }
    }

}
