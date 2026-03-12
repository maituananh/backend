stripe login --api-key sk_test_xxxxxx
stripe listen --events checkout.session.completed,checkout.session.expired,payment_intent.payment_failed --forward-to localhost:8080/api/payment/webhook

# for testing local
stripe trigger checkout.session.completed
