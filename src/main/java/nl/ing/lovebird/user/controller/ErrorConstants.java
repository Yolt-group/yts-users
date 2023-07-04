package nl.ing.lovebird.user.controller;

import nl.ing.lovebird.errorhandling.ErrorInfo;

public enum ErrorConstants implements ErrorInfo {
    USER_NOT_FOUND("002", "User not found"),
    ILLEGAL_ARGUMENT("003", "Invalid parameter in call"),
    NO_COUNTRY_FOR_PHONE_NUMBER("004", "No country found for phone number"),
    ILLEGAL_VERSION_STRING("005", "Illegal version string"),
    EMAIL_ADDRESS_INVALID("006", "Invalid email address"),
    NO_COUNTRY_FOR_COUNTRY_CODE("007", "No country found for country code"),
    NO_COUNTRY_CODE_OR_PHONE_NUMBER("008", "No country and/or phone number."),
    VALIDATION_FAILED("010", "A field didn't pass validation"),
    INVALID_PHONE_REGION("011", "Phone number region does not match user country code"),
    TOO_MANY_CHANGES("012", "Too many change attempts made"),
    TOO_MANY_RESENDS("013", "Too many resend attempts made"),
    VERIFICATION_CODE_NOT_FOUND("014", "Verification code not found"),
    VERIFICATION_CODE_USED("015", "Verification code already used"),
    VERIFICATION_CODE_INVALID("016", "Verification code invalid"),
    VERIFICATION_CODE_EXPIRED("017", "Verification code expired"),
    TOO_MANY_CHANGES_BLOCKED_USER("018", "Too many change attempts made, user is blocked"),
    PASSWORD_STRENGTH("019", "Password not strong enough"),
    EMAIL_VERIFICATION_EXCEPTION("020", "Email verification exception"),
    PASSWORD_SET_NOT_ALLOWED_EXCEPTION("021", "Password cannot be set"),
    EMAIL_NOT_UNIQUE("024", "Email address is already in use"),
    ACCEPTED_TERMS_AND_CONDITIONS_OR_PRIVACY_SETTINGS_ARE_NOT_FOUND_EXCEPTION("025", "Accepted Terms And Conditions Or Privacy Settings Are Not Found."),
    ACCEPTED_TERMS_AND_CONDITIONS_OR_PRIVACY_SETTINGS_ARE_DOWNGRADED_EXCEPTION("026", "Accepted Terms And Conditions Or Privacy Settings Are Downgraded."),
    PROFILE_ASSIGN_USER_ID_FAILED_EXCEPTION("027", "Could not assign new user id to profile."),
    PROFILE_SET_STATUS_FAILED_EXCEPTION("028", "Could not change profile status."),
    PROFILE_PREPARE_FOR_PIN_CHANGE_FAILED("031", "Could not prepare profile for pin change."),
    USER_DELETE_FAILED_POSITIVE_BALANCE("032", "User still has a balance in his Yolt account and thus cannot be deleted"),
    USER_DELETE_FAILED_NEGATIVE_BALANCE("033", "User has a negative balance in his Yolt account and thus cannot be deleted"),
    USER_IS_NOT_YOLT_2("034", "User is not upgraded to yolt2 for this feature."),
    PHONE_NUMBER_IS_ALREADY_CLAIMED("035", "This phone number is already in use."),
    PHONE_NUMBER_INVALID("036", "Phone number is invalid."),
    USER_IS_BLOCKED("039", "Cannot perform action as user is blocked."),
    USER_DELETE_FAILED_UNKNOWN_BALANCE("040", "Failed to retrieve user balance. User cannot be deleted right now. Try again later."),

    FEATURE_NOT_ENABLED("999", "Feature not enabled");

    private final String code;
    private final String message;

    ErrorConstants(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
