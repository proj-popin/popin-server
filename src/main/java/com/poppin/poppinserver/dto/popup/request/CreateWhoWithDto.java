package com.poppin.poppinserver.dto.popup.request;

import jakarta.validation.constraints.NotNull;

public record CreateWhoWithDto(
        @NotNull
        Boolean solo,
        @NotNull
        Boolean withFriend,
        @NotNull
        Boolean withFamily,
        @NotNull
        Boolean withLover) {
}
