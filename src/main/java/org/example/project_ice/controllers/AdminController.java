package org.example.project_ice.controllers;
import jakarta.validation.Valid;
import org.example.project_ice.Category;
import org.example.project_ice.NotificationService;
import org.example.project_ice.entity.Product;
import org.example.project_ice.entity.Task;
import org.example.project_ice.entity.User;
import org.example.project_ice.repository.CategoryRepo;
import org.example.project_ice.repository.ProductRepository;
import org.example.project_ice.repository.TaskRepo;
import org.example.project_ice.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

@Controller()
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private UserRepo userRepo;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private TaskRepo taskRepo;

    @Autowired
    private CategoryRepo categoryRepo;

    @Autowired
    ProductRepository prodrepo;

    @GetMapping()
    public String adminDashboard(@RequestParam(required = false) Long category,
                                 @RequestParam(required = false) String status,
                                 @RequestParam(required = false) String sort,
                                 @RequestParam(value = "search", required = false) String search,
                                 Model model) {

//        int pageSize = 10;
//        Page<Task> tasksPage;
//        Pageable pageable;
//
//        if (sort != null && !sort.isEmpty()) {
//            Sort sortOption = Sort.by("dueDate");
//            sortOption = "desc".equals(sort) ? sortOption.descending() : sortOption.ascending();
//            pageable = PageRequest.of(page, pageSize, sortOption);
//        } else {
//            pageable = PageRequest.of(page, pageSize);
//        }
//
//        if (category != null) {
//            Optional<Category> cate = categoryRepo.findById(category);
//            tasksPage = taskRepo.findByCategory(cate.get(), pageable);
//        } else if (status != null && !status.isEmpty()) {
//            tasksPage = taskRepo.findByStatus(status, pageable);
//        }  else if (search != null && !search.isEmpty()) {
//            tasksPage = taskRepo.findByTitleContaining(search, pageable);
//        }
//        else {
//            tasksPage = taskRepo.findAll(pageable);
//        }
//
//        model.addAttribute("users", userRepo.findAll());
//        model.addAttribute("tasks", tasksPage.getContent());
//        model.addAttribute("categories", categoryRepo.findAll());
//        model.addAttribute("currentPage", page);
//        model.addAttribute("totalPages", tasksPage.getTotalPages()); // Total number of pages
        model.addAttribute("ice_creams", prodrepo.findAll());
        return "FrameThree_admin";
    }

    @GetMapping("/add")
    public String showAddTaskForm(Model model) {
        model.addAttribute("users", userRepo.findAll());
        model.addAttribute("task", new Task());
        model.addAttribute("categories", categoryRepo.findAll());
        return "task-form-admin";
    }

    @PostMapping("/add")
    public String addTask(@Valid @ModelAttribute("task") Task task, BindingResult result,
                          Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryRepo.findAll());
            model.addAttribute("users", userRepo.findAll());
            return "task-form-admin";
        }
        System.out.println(task.toString());
        taskRepo.save(task);
        try{
            User user = userRepo.findByUsername(task.getUser().getUsername());
            notificationService.sendTaskNotification(
                    user.getEmail(),
                    "Task Notification",
                    "You have a new task assigned by the admin."
            );
            model.addAttribute("message", "Notification sent to " + user.getUsername());}
        catch (Exception e){return "redirect:/admin";}
        return "redirect:/admin";
    }

    @PostMapping("/edit")
    public String editTask(@Valid @ModelAttribute("product") Product prod,
                           BindingResult result, @RequestParam("photo") MultipartFile photo, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryRepo.findAll());
            model.addAttribute("users", userRepo.findAll());
            return "admin";
        }
        Optional<Product> n_prod = prodrepo.findById(prod.getId());
        if (n_prod.isPresent()) {
            Product new_product = n_prod.get();
            new_product.setId(prod.getId());
            new_product.setDescription(prod.getDescription());
            new_product.setName(prod.getName());
            new_product.setPrice(prod.getPrice());
            new_product.setAvailableAmount(prod.getAvailableAmount());

            if (photo.isEmpty()) {
                System.out.println("No file uploaded.");
                prodrepo.save(new_product);
                return "redirect:/admin";
            }

            String uploadDir = new File("src/main/resources/templates/public/images").getAbsolutePath();
            File uploadDirectory = new File(uploadDir);

            if (!uploadDirectory.exists()) {
                uploadDirectory.mkdirs();
            }

            try {
                String filePath = uploadDir + File.separator + photo.getOriginalFilename();
                File destinationFile = new File(filePath);
                photo.transferTo(destinationFile);

                System.out.println("File uploaded successfully: " + filePath);
                new_product.setImage(photo.getOriginalFilename());
                prodrepo.save(new_product);
            } catch (IOException e) {
                System.out.println("File upload failed: " + e.getMessage());
                try {
                    String filePath = uploadDir + File.separator + "copy_" + photo.getOriginalFilename();
                    File destinationFile = new File(filePath);
                    photo.transferTo(destinationFile);

                    System.out.println("File uploaded successfully: " + filePath);
                    new_product.setImage(photo.getOriginalFilename());
                    prodrepo.save(new_product);
                }catch (IOException ex){
                    System.out.println("File upload failed: " + e.getMessage());
                }
            }

//
        }

        return "redirect:/admin";
    }

    @GetMapping("/delete/{id}")
    public String deleteTask(@PathVariable("id") Long prodId) {
        Optional<Product> prod = prodrepo.findById(prodId);
        if (prod.isPresent()) {
            prodrepo.delete(prod.get());
        }
        return "redirect:/admin";
    }
}
