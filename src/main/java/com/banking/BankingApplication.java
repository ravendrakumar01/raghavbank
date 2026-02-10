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
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
@Controller
public class BankingApplication {

    private static double balance = 75500.00;
    private static List<String[]> transactions = new ArrayList<>();

    static {
        transactions.add(new String[]{"Amazon India", "10 Feb 2026", "Shopping", "DEBIT", "2499.00"});
        transactions.add(new String[]{"Monthly Salary", "01 Feb 2026", "Income", "CREDIT", "120000.00"});
    }

    public static void main(String[] args) {
        SpringApplication.run(BankingApplication.class, args);
    }

    @GetMapping("/login")
    public String login() { return "login"; }

    @PostMapping("/login")
    public String doLogin(@RequestParam String username, @RequestParam String password, HttpSession session) {
        if("admin".equals(username) && "password123".equals(password)) {
            session.setAttribute("user", "Admin");
            return "redirect:/";
        }
        return "login";
    }

    @GetMapping("/")
    public String dashboard(HttpSession session, Model model) {
        if(session.getAttribute("user") == null) return "redirect:/login";
        model.addAttribute("accountHolder", "Admin User");
        model.addAttribute("balance", String.format("₹%,.2f", balance));
        model.addAttribute("transactions", transactions);
        return "index";
    }

    @PostMapping("/transfer")
    public String transfer(@RequestParam double amount, @RequestParam String account, HttpSession session) {
        if(session.getAttribute("user") != null && amount > 0 && amount <= balance) {
            balance -= amount;
            transactions.add(0, new String[]{"Sent to " + account, "10 Feb 2026", "Transfer", "DEBIT", String.valueOf(amount)});
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
        document.add(new Paragraph("RAGHAVBANK - OFFICIAL STATEMENT").setBold().setFontSize(18));
        document.add(new Paragraph("User: Admin User\n\n"));

        Table table = new Table(new float[]{3, 4, 2, 3});
        table.addCell("Date"); table.addCell("Description"); table.addCell("Type"); table.addCell("Amount");

        for (String[] t : transactions) {
            table.addCell(t[1]); table.addCell(t[0]); table.addCell(t[3]); table.addCell("INR " + t[4]);
        }
        document.add(table);
        document.close();
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
