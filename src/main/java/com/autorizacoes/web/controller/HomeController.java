package com.autorizacoes.web.controller;

import com.autorizacoes.core.model.User;
import com.autorizacoes.core.security.SecurityLevel;
import com.autorizacoes.service.AccessControlService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/login";
    }

    @GetMapping("/home")
    public String home(HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");

        if (user == null) {
            return "redirect:/login";
        }

        populatePermissionMatrix(user, model);

        return "home";
    }

    @GetMapping("/access-check")
    public String accessCheck(@RequestParam String action,
                              @RequestParam String level,
                              HttpSession session,
                              Model model) {

        User user = (User) session.getAttribute("currentUser");
        if (user == null) return "redirect:/login";

        populatePermissionMatrix(user, model);

        SecurityLevel targetLevel = SecurityLevel.valueOf(level);
        boolean result;

        if ("read".equals(action)) {
            result = AccessControlService.canRead(user, targetLevel);
            model.addAttribute("action", "LER");
        } else {
            result = AccessControlService.canWrite(user, targetLevel);
            model.addAttribute("action", "ESCREVER");
        }

        model.addAttribute("user", user);
        model.addAttribute("targetLevel", targetLevel);
        model.addAttribute("result", result);
        model.addAttribute("rule", "read".equals(action) ? "No-Read-Up" : "No-Write-Down");

        return "home";
    }

    private void populatePermissionMatrix(User user, Model model) {
        model.addAttribute("user", user);
        model.addAttribute("allLevels", SecurityLevel.values());

        model.addAttribute("canReadPublic",       AccessControlService.canRead(user, SecurityLevel.PUBLIC));
        model.addAttribute("canReadConfidential", AccessControlService.canRead(user, SecurityLevel.CONFIDENTIAL));
        model.addAttribute("canReadSecret",       AccessControlService.canRead(user, SecurityLevel.SECRET));

        model.addAttribute("canWritePublic",       AccessControlService.canWrite(user, SecurityLevel.PUBLIC));
        model.addAttribute("canWriteConfidential", AccessControlService.canWrite(user, SecurityLevel.CONFIDENTIAL));
        model.addAttribute("canWriteSecret",       AccessControlService.canWrite(user, SecurityLevel.SECRET));
    }
}
