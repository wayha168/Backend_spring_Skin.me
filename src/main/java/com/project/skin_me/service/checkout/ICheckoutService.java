package com.project.skin_me.service.checkout;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;

public interface ICheckoutService{

    Session createCheckoutSession(Long orderId, Long amountCents) throws StripeException;

}
