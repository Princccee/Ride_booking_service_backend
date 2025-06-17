package com.ridebooking.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    @Autowired
    private RazorpayClient razorpayClient;

    public Order createOrder(double amountInRupees, String receiptId) throws RazorpayException {
        JSONObject options = new JSONObject();
        options.put("amount", (int)(amountInRupees * 100)); // Convert to paise
        options.put("currency", "INR");
        options.put("receipt", receiptId);
        options.put("payment_capture", 1); // Auto-capture after payment

        return razorpayClient.orders.create(options);
    }
}
