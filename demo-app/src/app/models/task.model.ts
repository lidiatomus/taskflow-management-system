import { Person } from './person.model';

export type TaskStatus = 'TODO' | 'IN_PROGRESS' |'PENDING_APPROVAL' | 'DONE';

export interface Task {
  id: string;
  title: string;
  description: string;
  assignedPersons: Person[];
  status: TaskStatus;
  deadline: string;
  completedAt?: string | null;
  timeRemaining?: string | null;
  completionStatus?: string | null;
  approvalRequestedAt?: string | null;
  approvalResolvedAt?: string | null;
  approvalComment?: string | null;
}

export interface CreateTaskDto {
  title: string;
  description: string;
  personIds: string[];
  status: TaskStatus;
  deadline: string;
}

export interface UpdateTaskDto {
  title: string;
  description: string;
  personIds: string[];
  status: TaskStatus;
  deadline: string;
}

export interface PatchTaskDto {
  title?: string;
  description?: string;
  addPersonIds?: string[];
  removePersonIds?: string[];
  status?: TaskStatus;
  deadline?: string;
}