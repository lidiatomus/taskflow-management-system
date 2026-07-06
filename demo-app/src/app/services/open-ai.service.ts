import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

const OPENAI_API_URL = 'http://localhost:8080/openai/work-plan';

export interface OpenAiResponse {
    answer: string;
}

@Injectable({
    providedIn: 'root'
})
export class OpenAiService {
    private readonly http = inject(HttpClient);
    private readonly authService = inject(AuthService);

    private getAuthHeaders(): HttpHeaders {
        const token = this.authService.getToken();

        return new HttpHeaders({
            Authorization: `Bearer ${token ?? ''}`,
        });
    }

    generateWorkPlan(task: string): Observable<OpenAiResponse> {
        return this.http.post<OpenAiResponse>(
            OPENAI_API_URL,
            { task },
            {
                headers: this.getAuthHeaders(),
            }
        );
    }
    suggestAssignee(payload: {
        title: string;
        description: string;
        deadline: string;
    }) {
        return this.http.post<OpenAiResponse>(
            'http://localhost:8080/openai/suggest-assignee',
            payload,
            {
                headers: this.getAuthHeaders(),
            }
        );
    }
}