package org.example.project_ice.controllers;
import org.example.project_ice.NotificationService;
import org.example.project_ice.entity.User;
import org.example.project_ice.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserRepo userrepo;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    public String viewProfile(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userrepo.findByUsername(authentication.getName());
        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("/update")
    public String updateProfile(@ModelAttribute User updatedUser, Model model) {
        User existingUser = userrepo.findByUsername(updatedUser.getUsername());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setProfileImageUrl(existingUser.getProfileImageUrl());
        existingUser.setUsername(updatedUser.getUsername());
        existingUser.setPassword(existingUser.getPassword());
        existingUser.setAdmin(false);
        userrepo.save(existingUser);
        model.addAttribute("user", updatedUser);
        return "redirect:/profile";
    }

    @PostMapping("/saveUserPhoto")
    public String saveUserPhoto(@RequestParam("photo") MultipartFile photo) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userrepo.findByUsername(authentication.getName());
        if (photo.isEmpty()) {
            System.out.println("No file uploaded.");
        }

        String uploadDir = new File("uploads").getAbsolutePath();
        File uploadDirectory = new File(uploadDir);

        if (!uploadDirectory.exists()) {
            uploadDirectory.mkdirs();
        }

        try {
            String filePath = uploadDir + File.separator + photo.getOriginalFilename();
            File destinationFile = new File(filePath);
            photo.transferTo(destinationFile);

            System.out.println("File uploaded successfully: " + filePath);
            user.setProfileImage(photo.getOriginalFilename());
            userrepo.save(user);
        } catch (IOException e) {
            System.out.println("File upload failed: " + e.getMessage());
        }
        return "redirect:/profile";
    }


    private String generateRandomPassword() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    @PostMapping("/resetPassword")
    public String resetPassword() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userrepo.findByUsername(authentication.getName());
        String tempPassword = generateRandomPassword();
        user.setPassword(passwordEncoder.encode(tempPassword));
        userrepo.save(user);

        notificationService.sendTaskNotification(user.getEmail(), "Password Reset", "Your new password is: " + tempPassword);

        return "redirect:/login";
    }
}
