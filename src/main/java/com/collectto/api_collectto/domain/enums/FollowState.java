package com.collectto.api_collectto.domain.enums;

public enum FollowState {

    NONE("NONE"),
    REQUESTED("REQUESTED"),
    FOLLOWING("FOLLOWING");

    private final String code;

    FollowState(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static FollowState fromStatus(FollowStatus status) {
        return switch (status) {
            case PENDING -> REQUESTED;
            case ACCEPTED -> FOLLOWING;
            case DECLINED -> NONE; // Declined requests are not exposed to the requester
        };
    }

    public static FollowState fromCode(String code) {
        for (FollowState state : FollowState.values()) {
            if (state.code.equals(code)) {
                return state;
            }
        }
        throw new IllegalArgumentException("Unknown FollowState code: " + code);
    }
}
