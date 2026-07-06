export type DepartamentName =
  | 'ELECTRICAL'
  | 'SOFTWARE'
  | 'MECHANICAL'
  | 'PR'
  | 'SPONSORSHIP'
  | 'MANAGEMENT';

export interface Departament {
  id: string;
  name: DepartamentName;
  description: string;
}

export interface CreateDepartamentDto {
  name: DepartamentName;
  description: string;
}

export interface UpdateDepartamentDto {
  name: DepartamentName;
  description: string;
}

export type PatchDepartamentDto = Partial<UpdateDepartamentDto>;