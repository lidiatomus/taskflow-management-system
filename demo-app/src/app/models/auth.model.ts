export type UserRole = 'MEMBER' | 'DEPARTMENT_HEAD' | 'TEAM_MANAGER';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  id: string;
  name: string;
  email: string;
  role: UserRole;
  departamentId: string | null;
  token: string;
  refreshToken: string;
}

export interface AuthUser {
  id: string;
  name: string;
  email: string;
  role: UserRole;
  departamentId: string | null;
  token: string;
  refreshToken: string;
}