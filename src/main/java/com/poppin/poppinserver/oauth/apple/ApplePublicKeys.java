package com.poppin.poppinserver.oauth.apple;

import java.util.List;

public record ApplePublicKeys(
        List<ApplePublicKey> keys
) {
}
