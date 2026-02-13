package com.project.skin_me.controller;

import com.project.skin_me.dto.OrderDto;
import com.project.skin_me.model.Category;
import com.project.skin_me.model.Payment;
import com.project.skin_me.model.Product;
import com.project.skin_me.model.User;
import com.project.skin_me.repository.PaymentRepository;
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

import java.util.List;

@Controller
@RequiredArgsConstructor
public class PageController {

    private final ICategoryService categoryService;
    private final IProductService productService;
    private final IOrderService orderService;
    private final PaymentRepository paymentRepository;
    private final IUserService userService;

    @GetMapping("/login-page")
    public String loginPage() {
        return "auth/login";
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
    @PreAuthorize("hasRole('ADMIN')")
    public String dashboard(Model model) {
        try {
            List<Product> products = productService.getAllProducts();
            List<OrderDto> orders = orderService.getAllUserOrders();
            int totalUsers = userService.getAllUsers().size();
            List<Payment> payments = paymentRepository.findAll();
            double totalRevenue = payments.stream()
                    .mapToDouble(p -> p.getAmount() != null ? p.getAmount().doubleValue() : 0)
                    .sum();

            model.addAttribute("totalProducts", products.size());
            model.addAttribute("totalOrders", orders.size());
            model.addAttribute("totalUsers", totalUsers);
            model.addAttribute("totalRevenue", totalRevenue);
        } catch (Exception e) {
            model.addAttribute("totalProducts", 0);
            model.addAttribute("totalOrders", 0);
            model.addAttribute("totalUsers", 0);
            model.addAttribute("totalRevenue", 0.0);
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

    @GetMapping("/")
    public String homePage() {
        return "redirect:/dashboard";
    }

    @GetMapping("/logout")
    public String logoutPage() {
        return "redirect:/login-page?logout";
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
        return "views/admin/categories";
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
        return "views/admin/products";
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
        return "views/admin/orders";
    }

    @GetMapping("/views/my-orders")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public String myOrdersListPage(Authentication authentication, Model model) {
        try {
            User user = userService.getAuthenticatedUser();
            List<OrderDto> orders = orderService.getUserOrders(user.getId());
            long deliveredOrders = orders.stream()
                    .filter(o -> o.getOrderStatus() != null && o.getOrderStatus().toString().equals("COMPLETED"))
                    .count();
            model.addAttribute("orders", orders);
            model.addAttribute("totalOrders", orders.size());
            model.addAttribute("deliveredOrders", deliveredOrders);
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load your orders: " + e.getMessage());
            model.addAttribute("orders", List.<OrderDto>of());
            model.addAttribute("totalOrders", 0);
            model.addAttribute("deliveredOrders", 0);
        }
        model.addAttribute("pageTitle", "My Orders");
        return "views/user/my-orders";
    }

    @GetMapping("/views/payments")
    @PreAuthorize("hasRole('ADMIN')")
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
        return "views/admin/payments";
    }

    @GetMapping("/views/my-payments")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
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
        return "views/user/my-payments";
    }

}
