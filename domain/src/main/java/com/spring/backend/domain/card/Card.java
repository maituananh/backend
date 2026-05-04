package com.spring.backend.domain.card;

import com.spring.backend.domain.shared.AggregateRoot;

public class Card extends AggregateRoot {

    private Long customerId;
    private String numberOfCard;

    private Card() {}

    public static Card reconstitute(Long id, Long customerId, String numberOfCard) {
        Card c = new Card();
        c.id = id;
        c.customerId = customerId;
        c.numberOfCard = numberOfCard;
        return c;
    }

    public Long getCustomerId()     { return customerId; }
    public String getNumberOfCard() { return numberOfCard; }
}
