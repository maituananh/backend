package com.spring.backend.domain.payment;

import com.spring.backend.domain.enums.PaymentMethod;
import com.spring.backend.domain.enums.PaymentStatus;
import com.spring.backend.domain.shared.AggregateRoot;
import com.spring.backend.domain.shared.Money;
import java.time.Instant;

public class Payment extends AggregateRoot {

    private Long orderId;
    private String transactionId;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private Money amount;
    private String gatewayResponse;
    private String stripeEventId;
    private Instant paidAt;

    private Payment() {}

    public static Payment reconstitute(Long id, Long orderId, PaymentMethod paymentMethod,
                                       PaymentStatus status, Money amount,
                                       String transactionId, String stripeEventId,
                                       Instant paidAt) {
        Payment p = new Payment();
        p.id = id;
        p.orderId = orderId;
        p.paymentMethod = paymentMethod;
        p.status = status;
        p.amount = amount;
        p.transactionId = transactionId;
        p.stripeEventId = stripeEventId;
        p.paidAt = paidAt;
        return p;
    }

    public Long getOrderId()              { return orderId; }
    public String getTransactionId()      { return transactionId; }
    public PaymentMethod getPaymentMethod(){ return paymentMethod; }
    public PaymentStatus getStatus()      { return status; }
    public Money getAmount()              { return amount; }
    public String getGatewayResponse()    { return gatewayResponse; }
    public String getStripeEventId()      { return stripeEventId; }
    public Instant getPaidAt()            { return paidAt; }
}
