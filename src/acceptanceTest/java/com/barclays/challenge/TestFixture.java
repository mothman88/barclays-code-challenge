package com.barclays.challenge;

import com.barclays.challenge.model.Account;
import com.barclays.challenge.model.Address;
import com.barclays.challenge.model.User;

import java.util.Random;
import java.util.UUID;

public class TestFixture {

    public static User createUser(String email, String password) {
        return User.builder()
            .id(UUID.randomUUID().toString())
            .name("user-" + new Random().nextInt(99999))
            .email(email)
            .password(password)
            .phoneNumber("+07777777777")
            .address(Address.builder()
                .line1("line1")
                .line2("line2")
                .county("county")
                .town("town")
                .postcode("postcode")
                .build())
            .build();
    }

    public static Account createAccount(String accountNumber, String userId) {
        return Account.builder()
            .name(accountNumber)
            .accountType("personal")
            .userId(userId)
            .accountNumber(accountNumber)
            .sortCode("00-00-00")
            .build();
    }
}
