package com.dbw.spring_boot.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StartPageController {
    
    @GetMapping("/")
    public String startPage() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Start</title>
                <style>
                    body {
                        display: flex;
                        flex-direction: column;
                        justify-content: center;
                        align-items: center;
                        height: 100vh;
                        margin: 0;
                        font-family: Arial, sans-serif;
                    }
                    h1 {
                        text-align: center;
                        margin: 10px;
                    }
                    p {
                        text-align: center;
                        margin: 10px;
                    }
                </style>
            </head>
            <body>
                <h1>Datenbanken: Weiterführende Konzepte</h1>
                <p>WISE25/26</p>
            </body>
            </html>
            """;
    }
}
