import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { OpenAiService } from '../../services/open-ai.service';
import { AuthService } from '../../services/auth.service';

@Component({
    selector: 'app-ai-assistant-page',
    standalone: true,
    imports: [
        FormsModule,
        MatButtonModule,
        MatCardModule,
        MatInputModule,
        MatFormFieldModule
    ],
    templateUrl: './ai-assistant-page.component.html',
    styleUrl: './ai-assistant-page.component.scss'
})

export class AiAssistantPageComponent {
    private readonly openAiService = inject(OpenAiService);

    protected readonly taskText = signal('');
    protected readonly answer = signal('');
    protected readonly isLoading = signal(false);
    private readonly authService = inject(AuthService);

    generatePlan(): void {
        if (!this.taskText().trim()) {
            return;
        }

        this.isLoading.set(true);
        this.answer.set('');

        this.openAiService
            .generateWorkPlan(this.taskText())
            .subscribe({
                next: (response) => {
                    this.answer.set(response.answer);
                    this.isLoading.set(false);
                },
                error: () => {
                    this.answer.set('Could not generate AI suggestion.');
                    this.isLoading.set(false);
                }
            });
    }
    suggestAssignee(): void {
        if (!this.taskText().trim()) {
            return;
        }

        this.isLoading.set(true);
        this.answer.set('');

        this.openAiService
            .suggestAssignee({
                title: this.taskText(),
                description: this.taskText(),
                deadline: new Date().toISOString()
            })
            .subscribe({
                next: (response) => {
                    this.answer.set(response.answer);
                    this.isLoading.set(false);
                },
                error: () => {
                    this.answer.set('Could not generate assignee suggestion.');
                    this.isLoading.set(false);
                }
            });
    }
    protected canSuggestAssignee(): boolean {

        return this.authService.isTeamManager()
            ||
            this.authService.isDepartmentHead();

    }
}