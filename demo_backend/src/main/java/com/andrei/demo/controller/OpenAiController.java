package com.andrei.demo.controller;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Person;
import com.andrei.demo.service.OpenAiService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@AllArgsConstructor
@CrossOrigin
@RequestMapping("/openai")
public class OpenAiController {

    private final OpenAiService openAiService;

    @PostMapping("/work-plan")
    public Map<String,String> generateWorkPlan(

            @AuthenticationPrincipal
            Person loggedUser,

            @RequestBody
            Map<String,String> body
    ){

        String response =
                openAiService.generateWorkPlan(
                        body.get("task")
                );

        return Map.of(
                "answer",
                response
        );
    }
    @PostMapping("/suggest-assignee")
    public Map<String, String> suggestAssignee(
            @AuthenticationPrincipal Person loggedUser,
            @RequestBody Map<String, String> body
    ) throws ValidationException {

        String title = body.get("title");
        String description = body.get("description");
        String deadline = body.get("deadline");

        String response = openAiService.suggestAssignee(
                loggedUser,
                title,
                description,
                deadline
        );

        return Map.of("answer", response);
    }
}