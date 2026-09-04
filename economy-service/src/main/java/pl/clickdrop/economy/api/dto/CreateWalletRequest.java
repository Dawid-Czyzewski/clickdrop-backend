package pl.clickdrop.economy.api.dto;

import jakarta.validation.constraints.NotNull;

public record CreateWalletRequest(@NotNull Long userId) { }
