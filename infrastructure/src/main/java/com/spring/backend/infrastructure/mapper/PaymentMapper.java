package com.spring.backend.infrastructure.mapper;

import com.spring.backend.domain.payment.Payment;
import com.spring.backend.domain.shared.Money;
import com.spring.backend.infrastructure.entity.PaymentEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ObjectFactory;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @BeanMapping(ignoreByDefault = true)
    Payment toDomain(PaymentEntity entity);

    @ObjectFactory
    default Payment createPayment(PaymentEntity entity) {
        return Payment.reconstitute(
            entity.getId(),
            entity.getOrder() != null ? entity.getOrder().getId() : null,
            entity.getPaymentMethod(),
            entity.getStatus(),
            entity.getAmount() != null ? new Money(entity.getAmount()) : null,
            entity.getTransactionId(),
            entity.getStripeEventId(),
            entity.getPaidAt()
        );
    }

    // Domain → Entity: MapStruct auto-maps matching fields
    // order relation is set by the adapter (requires managed entity reference)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "amount", expression = "java(payment.getAmount() != null ? payment.getAmount().getAmount() : null)")
    PaymentEntity toEntity(Payment payment);
}
