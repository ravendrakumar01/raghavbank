package com.banking;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@Controller
public class BankingApplication {

    // Learning purpose ke liye memory mein data save karenge
    private static double currentBalance = 75500.00;
    private static List<String[]> transactions = new ArrayList<>();

    static {
        transactions.add(new String[]{"Amazon India", "09 Feb 2026", "Shopping", "DEBIT", "2499.00"});
        transactions.add(new String[]{"Salary Credit", "01 Feb 2026", "Income", "CREDIT", "120000.00"});
    }

    public static void main(String[] args) {
        SpringApplication.run(BankingApplication.class, args);
    }

    @GetMapping("/login")
    public String loginPage() { return "login"; }

    @PostMapping("/login")
    public String doLogin(@RequestParam String username, @RequestParam String password, HttpSession session, Model model) {
        if ("admin".equals(username) && "password123".equals(password)) {
            session.setAttribute("user", username);
            return "redirect:/";
        }
        model.addAttribute("error", true);
        return "login";
    }

    @GetMapping("/")
    public String dashboard(HttpSession session, Model model) {
        if (session.getAttribute("user") == null) return "redirect:/login";
        model.addAttribute("accountHolder", "Admin User");
        model.addAttribute("balance", String.format("₹%,.2f", currentBalance));
        model.addAttribute("transactions", transactions);
        return "index";
    }

    @PostMapping("/transfer")
    public String transferMoney(@RequestParam double amount, @RequestParam String account, HttpSession session) {
        if (session.getAttribute("user") == null) return "redirect:/login";
        if (amount > 0 && amount <= currentBalance) {
            currentBalance -= amount;
            transactions.add(0, new String[]{"Transfer to " + account, "10 Feb 2026", "Transfer", "DEBIT", String.valueOf(amount)});
        }
        return "redirect:/";
    }

    @GetMapping("/download-statement")
    public void downloadStatement(HttpServletResponse response, HttpSession session) throws IOException {
        if (session.getAttribute("user") == null) return;

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=RaghavBank_Statement.pdf");

        PdfWriter writer = new PdfWriter(response.getOutputStream());
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("RAGHAVBANK - OFFICIAL STATEMENT").setBold().setFontSize(20));
        document.add(new Paragraph("Account Holder: Admin User"));
        document.add(new Paragraph("\n"));

        Table table = new Table(new float[]{3, 4, 2, 3});
        table.addCell("Date"); table.addCell("Description"); table.addCell("Type"); table.addCell("Amount");

        for (String[] t : transactions) {
            table.addCell(t[1]); table.addCell(t[0]); table.addCell(t[3]); table.addCell("INR " + t[4]);
        }

        document.add(table);
        document.close();
    }

    @GetMapping("/admin")
    public String adminDashboard(HttpSession session, Model model) {
        if (session.getAttribute("user") == null) return "redirect:/login";
        model.addAttribute("totalTransactions", transactions.size());
        model.addAttribute("systemStatus", "Memory-Mode (No DB)");
        return "admin";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
