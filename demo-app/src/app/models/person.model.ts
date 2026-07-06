export type PersonRole = 'MEMBER' | 'DEPARTMENT_HEAD' | 'TEAM_MANAGER';

export interface Departament {
  id: string;
  name?: string;
}

export interface Person {
  id: string;
  name: string;
  age: number;
  email: string;
  role: PersonRole;
  departament?: Departament | null;
}

export interface CreatePersonDto {
  name: string;
  age: number;
  email: string;
  role: PersonRole;
  departamentId: string;
}

export interface UpdatePersonDto {
  name: string;
  age: number;
  email: string;
  role: PersonRole;
  departamentId: string;
}

export type PatchPersonDto = Partial<UpdatePersonDto>;