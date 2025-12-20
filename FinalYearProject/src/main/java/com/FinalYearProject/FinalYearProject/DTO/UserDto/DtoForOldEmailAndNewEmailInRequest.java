package com.FinalYearProject.FinalYearProject.DTO.UserDto;

/**
 * DTO for User Email Change Operations
 * PURPOSE: Data Transfer Object for changing a user's email address, requiring both old (current) and new email addresses for verification and update.
 * EMAIL CHANGE WORKFLOW: oldEmail - current email for user identification and verification. newEmail - desired new email address for the user account.
 * VERIFICATION LOGIC: Ensures user knows their current email before allowing change. Provides audit trail of email change history.
 * USAGE CONTEXT: Used in user profile management endpoints for email updates. May require additional verification (password, OTP) depending on security requirements.
 * SECURITY CONSIDERATIONS: Email changes should trigger verification email to new address before activation. Old email should receive notification of change.
 * UNIQUENESS CONSTRAINT: newEmail must be unique across system. Should be validated against existing users before acceptance.
 * INTEGRATION: Used with UserService email update methods. Service should handle uniqueness validation, verification emails, and audit logging.
 * ERROR SCENARIOS: Old email doesn't match current user email. New email already in use. New email format invalid.
 * AUDIT TRAIL: Creates record of email change including timestamp, old email, new email, and who initiated change (user or admin).
 * SERIALIZATION: Simple two-field POJO for JSON requests. Clear naming distinguishes between current and desired email addresses.
 */
public class DtoForOldEmailAndNewEmailInRequest {
    String newEmail;
    String oldEmail;

    public DtoForOldEmailAndNewEmailInRequest(){}

    public DtoForOldEmailAndNewEmailInRequest(String newEmail, String oldEmail){
        this.newEmail=newEmail;
        this.oldEmail=oldEmail;
    }

    public void setNewEmail(String newEmail){
        this.newEmail=newEmail;
    }

    public void setOldEmail(String oldEmail){
        this.oldEmail=oldEmail;
    }

    public String getNewEmail(){
        return newEmail;
    }

    public String getOldEmail(){
        return oldEmail;
    }

}
