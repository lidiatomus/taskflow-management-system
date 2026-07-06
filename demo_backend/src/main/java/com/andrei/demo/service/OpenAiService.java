package com.andrei.demo.service;

import com.andrei.demo.model.Person;
import com.andrei.demo.model.PersonRole;
import com.andrei.demo.model.Task;
import com.andrei.demo.model.TaskStatus;
import com.andrei.demo.repository.PersonRepository;
import com.andrei.demo.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OpenAiService {

    @Value("${openai.api.key}")
    private String apiKey;

    private final WebClient webClient =
            WebClient.builder()
                    .baseUrl("https://api.openai.com")
                    .build();

    private final PersonRepository personRepository;
    private final TaskRepository taskRepository;

    public String ask(String prompt) {

        Map<String, Object> body =
                Map.of(
                        "model", "gpt-4.1-mini",
                        "messages",
                        new Object[]{
                                Map.of(
                                        "role", "user",
                                        "content", prompt
                                )
                        }
                );

        Map response =
                webClient
                        .post()
                        .uri("/v1/chat/completions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(
                                "Authorization",
                                "Bearer " + apiKey
                        )
                        .bodyValue(body)
                        .retrieve()
                        .bodyToMono(Map.class)
                        .block();

        List choices =
                (List) response.get("choices");

        Map firstChoice =
                (Map) choices.get(0);

        Map message =
                (Map) firstChoice.get("message");

        return (String) message.get("content");
    }

    public String generateWorkPlan(String taskText) {

        String prompt =
                "Create a short and practical work plan " +
                        "for the following task. " +
                        "Use simple steps and keep the answer concise. " +
                        "Task: " + taskText;

        return ask(prompt);
    }

    public String suggestAssignee(
            Person loggedUser,
            String title,
            String description,
            String deadline
    ) {

        List<Person> persons = personRepository.findAll();
        List<Task> tasks = taskRepository.findAll();

        if (loggedUser.getRole() == PersonRole.DEPARTMENT_HEAD) {
            persons = persons.stream()
                    .filter(person ->
                            person.getDepartament() != null &&
                                    loggedUser.getDepartament() != null &&
                                    person.getDepartament().getId()
                                            .equals(loggedUser.getDepartament().getId())
                    )
                    .toList();
        }

        StringBuilder prompt = new StringBuilder();

        prompt.append("You are an assistant for a task management application.\n");
        prompt.append("Recommend the best person for assigning a new task.\n");
        prompt.append("Consider department, past completed tasks, late completions, current workload and similarity with previous tasks.\n\n");

        prompt.append("New task:\n");
        prompt.append("Title: ").append(title).append("\n");
        prompt.append("Description: ").append(description).append("\n");
        prompt.append("Deadline: ").append(deadline).append("\n\n");

        prompt.append("Available members:\n");

        for (Person person : persons) {

            List<Task> personTasks = tasks.stream()
                    .filter(task ->
                            task.getAssignedPersons() != null &&
                                    task.getAssignedPersons()
                                            .stream()
                                            .anyMatch(p -> p.getId().equals(person.getId()))
                    )
                    .toList();

            long completedOnTime = personTasks.stream()
                    .filter(task ->
                            task.getStatus() == TaskStatus.DONE &&
                                    task.getCompletedAt() != null &&
                                    task.getDeadline() != null &&
                                    !task.getCompletedAt().isAfter(task.getDeadline())
                    )
                    .count();

            long completedLate = personTasks.stream()
                    .filter(task ->
                            task.getStatus() == TaskStatus.DONE &&
                                    task.getCompletedAt() != null &&
                                    task.getDeadline() != null &&
                                    task.getCompletedAt().isAfter(task.getDeadline())
                    )
                    .count();

            long inProgress = personTasks.stream()
                    .filter(task ->
                            task.getStatus() == TaskStatus.IN_PROGRESS ||
                                    task.getStatus() == TaskStatus.PENDING_APPROVAL
                    )
                    .count();

            prompt.append("- Name: ").append(person.getName()).append("\n");
            prompt.append("  Email: ").append(person.getEmail()).append("\n");
            prompt.append("  Role: ").append(person.getRole()).append("\n");
            prompt.append("  Department: ")
                    .append(person.getDepartament() != null
                            ? person.getDepartament().getName()
                            : "none")
                    .append("\n");
            prompt.append("  Completed on time: ").append(completedOnTime).append("\n");
            prompt.append("  Completed late: ").append(completedLate).append("\n");
            prompt.append("  Current active tasks: ").append(inProgress).append("\n");

            prompt.append("  Previous task titles: ");
            personTasks.stream()
                    .limit(5)
                    .forEach(task -> prompt.append(task.getTitle()).append("; "));

            prompt.append("\n\n");
        }

        prompt.append("Answer in this format:\n");
        prompt.append("Recommended assignee: <name>\n");
        prompt.append("Reason: <short reason>\n");
        prompt.append("Alternative: <name or none>\n");

        return ask(prompt.toString());
    }
}