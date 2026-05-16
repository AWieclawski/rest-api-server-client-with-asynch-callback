package edu.orders.client.data.model;

import lombok.*;

@Getter
@Builder
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
public class Customer {
    private String customerId;
    private String firstName;
    private String lastName;
    private String password;
    private String email;
}
