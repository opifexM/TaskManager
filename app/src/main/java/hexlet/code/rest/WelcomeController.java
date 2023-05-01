package hexlet.code.rest;

import hexlet.code.constant.ViewConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
@Tag(name = "Index", description = "Welcome message")
@Slf4j
public class WelcomeController {

    @GetMapping(value = "/welcome")
    @Operation(summary = "Get welcome message", description = "Returns a welcome message to the user.")
    public String index(Model model) {
        log.info("Index page");
        model.addAttribute("welcomeMessage", "Welcome to Spring");
        return ViewConstants.FORWARD_WELCOME;
    }
}
