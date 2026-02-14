package com.project.skin_me.controller;

import com.project.skin_me.enums.OrderStatus;
import com.project.skin_me.enums.PaymentMethod;
import com.project.skin_me.model.Order;
import com.project.skin_me.model.Payment;
import com.project.skin_me.repository.OrderRepository;
import com.project.skin_me.repository.PaymentRepository;
import com.project.skin_me.response.ApiResponse;
import com.project.skin_me.service.checkout.ICheckoutService;
import com.project.skin_me.service.order.IOrderService;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/payment")
public class PaymentController {

    private final ICheckoutService checkoutService;
    private final IOrderService orderService;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @PostMapping("/create-checkout-session/{userId}")
    public ResponseEntity<ApiResponse> createCheckoutSession(@PathVariable Long userId) {
        try {
            Order order = orderService.placeOrderItem(userId);
            long amountInCents = order.getOrderTotalAmount()
                    .multiply(BigDecimal.valueOf(100))
                    .longValue();

            Session session = checkoutService.createCheckoutSession(order.getId(), amountInCents);

            order.setStripeSessionId(session.getId());
            order.setOrderStatus(OrderStatus.PAYMENT_PENDING);
            orderService.updateOrder(order);

            // Save initial payment record
            Payment payment = Payment.builder()
                    .order(order)
                    .amount(order.getOrderTotalAmount())
                    .method(PaymentMethod.CREDIT_CARD)
                    .status(OrderStatus.PENDING)
                    .transactionRef(session.getId())
                    .transactionTime(LocalDateTime.now())
                    .build();
            paymentRepository.save(payment);

            Map<String, Object> data = Map.of(
                    "checkoutUrl", session.getUrl(),
                    "orderId", order.getId(),
                    "sessionId", session.getId()
            );
            return ResponseEntity.ok(new ApiResponse("Stripe checkout session created", data));
        } catch (StripeException e) {
            return ResponseEntity.status(500)
                    .body(new ApiResponse("Stripe error: " + e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ApiResponse("Error: " + e.getMessage(), null));
        }
    }

    @PostMapping("/create-payment-intent")
    public ResponseEntity<ApiResponse> createPaymentIntent(@RequestBody Map<String, Object> request) {
        try {
            Long orderId = Long.parseLong(request.get("orderId").toString());
            Long amountCents = Long.parseLong(request.get("amountCents").toString());
            String cardholderName = request.get("cardholderName") != null ? request.get("cardholderName").toString() : "Customer";

            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("Order not found"));

            // Create PaymentIntent
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountCents)
                    .setCurrency("usd")
                    .setDescription("Skin.me Order #" + order.getOrderId())
                    .putMetadata("orderId", orderId.toString())
                    .putMetadata("orderNumber", order.getOrderId().toString())
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            // Save payment record
            Payment payment = Payment.builder()
                    .order(order)
                    .amount(order.getOrderTotalAmount())
                    .method(PaymentMethod.CREDIT_CARD)
                    .status(OrderStatus.PENDING)
                    .transactionRef(paymentIntent.getId())
                    .transactionTime(LocalDateTime.now())
                    .build();
            paymentRepository.save(payment);

            Map<String, Object> data = new HashMap<>();
            data.put("clientSecret", paymentIntent.getClientSecret());
            data.put("paymentIntentId", paymentIntent.getId());
            data.put("orderId", orderId);

            return ResponseEntity.ok(new ApiResponse("Payment intent created", data));
        } catch (StripeException e) {
            return ResponseEntity.status(500)
                    .body(new ApiResponse("Stripe error: " + e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ApiResponse("Error: " + e.getMessage(), null));
        }
    }

    @PostMapping("/confirm-payment/{paymentIntentId}")
    public ResponseEntity<ApiResponse> confirmPayment(@PathVariable String paymentIntentId) {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

            if ("succeeded".equals(paymentIntent.getStatus())) {
                // Find order by payment intent ID
                Payment payment = paymentRepository.findByTransactionRef(paymentIntentId)
                        .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

                Order order = payment.getOrder();
                payment.setStatus(OrderStatus.SUCCESS);
                payment.setTransactionTime(LocalDateTime.now());
                paymentRepository.save(payment);

                orderService.confirmOrderPayment(order);

                Map<String, Object> data = new HashMap<>();
                data.put("orderId", order.getId());
                data.put("paymentIntentId", paymentIntentId);
                data.put("status", "succeeded");

                return ResponseEntity.ok(new ApiResponse("Payment confirmed", data));
            } else {
                return ResponseEntity.status(400)
                        .body(new ApiResponse("Payment not succeeded. Status: " + paymentIntent.getStatus(), null));
            }
        } catch (StripeException e) {
            return ResponseEntity.status(500)
                    .body(new ApiResponse("Stripe error: " + e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ApiResponse("Error: " + e.getMessage(), null));
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            
            // Handle checkout session completed
            if ("checkout.session.completed".equals(event.getType())) {
                Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
                if (session != null) {
                    orderService.getOrderByStripeSessionId(session.getId())
                            .ifPresent(order -> {
                                Payment payment = paymentRepository.findByTransactionRef(session.getId())
                                        .orElseThrow(() -> new IllegalArgumentException("Payment not found for sessionId: " + session.getId()));
                                payment.setStatus(OrderStatus.SUCCESS);
                                payment.setTransactionTime(LocalDateTime.now());
                                paymentRepository.save(payment);
                                orderService.confirmOrderPayment(order);
                            });
                }
            }
            
            // Handle payment intent succeeded
            if ("payment_intent.succeeded".equals(event.getType())) {
                PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
                if (paymentIntent != null) {
                    Payment payment = paymentRepository.findByTransactionRef(paymentIntent.getId())
                            .orElseThrow(() -> new IllegalArgumentException("Payment not found for paymentIntentId: " + paymentIntent.getId()));
                    Order order = payment.getOrder();
                    payment.setStatus(OrderStatus.SUCCESS);
                    payment.setTransactionTime(LocalDateTime.now());
                    paymentRepository.save(payment);
                    orderService.confirmOrderPayment(order);
                }
            }
            
            return ResponseEntity.ok("Webhook processed");
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Webhook error: " + e.getMessage());
        }
    }
}