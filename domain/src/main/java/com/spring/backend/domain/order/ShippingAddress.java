package com.spring.backend.domain.order;

import java.util.Objects;

public final class ShippingAddress {

    private final String name;
    private final String phone;
    private final String address;

    public ShippingAddress(String name, String phone, String address) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Shipping name required");
        if (phone == null || phone.isBlank())
            throw new IllegalArgumentException("Shipping phone required");
        if (address == null || address.isBlank())
            throw new IllegalArgumentException("Shipping address required");
        this.name = name;
        this.phone = phone;
        this.address = address;
    }

    public String getName()    { return name; }
    public String getPhone()   { return phone; }
    public String getAddress() { return address; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShippingAddress)) return false;
        ShippingAddress that = (ShippingAddress) o;
        return Objects.equals(name, that.name)
            && Objects.equals(phone, that.phone)
            && Objects.equals(address, that.address);
    }

    @Override
    public int hashCode() { return Objects.hash(name, phone, address); }

    @Override
    public String toString() {
        return "ShippingAddress{name='" + name + "', phone='" + phone
               + "', address='" + address + "'}";
    }
}
