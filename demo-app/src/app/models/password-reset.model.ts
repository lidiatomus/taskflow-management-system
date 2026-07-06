export interface PasswordResetRequest {
  email: string;
  newPassword: string;
  confirmPassword: string;
}

export interface PasswordResetConfirm {
  email: string;
  code: string;
}