package org.example.project_ice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.security.core.context.SecurityContextHolder;

@Controller
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskRepo taskRepo;

    @Autowired
    private CategoryRepo categoryRepo;

    @Autowired
    private UserRepo userRepo;

    @GetMapping()
    public String viewTasks(@RequestParam(required = false) Long category,
                            @RequestParam(required = false) String status,
                            @RequestParam(required = false) String sort,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(value = "search", required = false) String search,
                            Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Page<Task> tasks;
        Pageable pageable;

        if ("desc".equals(sort)) {
            pageable = PageRequest.of(page, 10, Sort.by("dueDate").descending());
        } else {
            pageable = PageRequest.of(page, 10, Sort.by("dueDate").ascending());
        }

        if (category != null) {
            Optional<Category> cate = categoryRepo.findById(category);
            tasks = taskRepo.findByUser_UsernameAndCategory(authentication.getName(), cate.get(), pageable);
        } else if (status != null && !status.isEmpty()) {
            tasks = taskRepo.findByUser_UsernameAndStatus(authentication.getName(), status, pageable);
        } else if (search != null && !search.isEmpty()) {
            tasks = taskRepo.findByTitleContaining(search, pageable);
        }
        else {
            tasks = taskRepo.findByUser_Username(authentication.getName(), pageable);
        }

        model.addAttribute("tasks", tasks);
        model.addAttribute("categories", categoryRepo.findAll());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", tasks.getTotalPages());

        return "tasks";
    }

    @GetMapping("/add")
    public String showAddTaskForm(Model model) {
        model.addAttribute("task", new Task());
        model.addAttribute("categories", categoryRepo.findAll());
        return "task-form";
    }

    @PostMapping("/add")
    public String addTask(@Valid @ModelAttribute("task") Task task, BindingResult result,
                           Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryRepo.findAll());
            return "task-form";
        }
        User user = userRepo.findByUsername(authentication.getName());
        task.setUser(user);
        taskRepo.save(task);
        return "redirect:/tasks";
    }

    @GetMapping("/edit/{id}")
    public String showEditTaskForm(@PathVariable("id") Long taskId, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Task task = taskRepo.findByTaskIdAndUser_Username(taskId, authentication.getName());
        if (task == null) return "redirect:/tasks";
        model.addAttribute("task", task);
        model.addAttribute("categories", categoryRepo.findAll());
        return "task-form";
    }

    @PostMapping("/edit/{id}")
    public String editTask(@PathVariable("id") Long taskId, @Valid @ModelAttribute("task") Task task,
                           BindingResult result, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryRepo.findAll());
            return "task-form";
        }
        Task n_task = taskRepo.findByTaskIdAndUser_Username(taskId, authentication.getName());
        if (n_task != null) {
            n_task.setTitle(task.getTitle());
            n_task.setDescription(task.getDescription());
            n_task.setDueDate(task.getDueDate());
            n_task.setStatus(task.getStatus());
            n_task.setPriority(task.getPriority());
            n_task.setCategory(task.getCategory());
            taskRepo.save(n_task);
        }

        return "redirect:/tasks";
    }

    @GetMapping("/delete/{id}")
    public String deleteTask(@PathVariable("id") Long taskId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Task task = taskRepo.findByTaskIdAndUser_Username(taskId, authentication.getName());
        if (task != null) {
            taskRepo.delete(task);
        }
        return "redirect:/tasks";
    }
}


