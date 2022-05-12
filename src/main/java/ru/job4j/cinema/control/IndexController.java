package ru.job4j.cinema.control;

import net.jcip.annotations.ThreadSafe;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.job4j.cinema.model.User;
import ru.job4j.cinema.service.SessionService;

import javax.servlet.http.HttpSession;

@ThreadSafe
@Controller
public class IndexController {

    private final SessionService sessionService;

    public IndexController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping("/")
    public String index(Model model,
                        @RequestParam(name = "failTicket", required = false) Boolean failTicket,
                        @RequestParam(name = "failUser", required = false) Boolean failUser,
                        HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            user = new User(0, "Гость", "guest@email.com", "00000000000");
        }
        model.addAttribute("user", user);
        model.addAttribute("failTicket", failTicket != null);
        model.addAttribute("failUser", failUser != null);
        model.addAttribute("sessions", sessionService.findAll());
        return "index";
    }
}