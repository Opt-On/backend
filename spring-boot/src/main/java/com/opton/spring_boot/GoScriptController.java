package com.opton.spring_boot;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/run")
public class GoScriptController {

    @GetMapping("/goscript")
    public ResponseEntity<String> runGoScript() {
        try {
            System.out.println("Current working directory: " + System.getProperty("user.dir")); // remove

            // Command to execute the Go script
            ProcessBuilder processBuilder = new ProcessBuilder("./main.exe"); // Or "go", "run", "script.go"
            processBuilder.directory(new File("src/go_scripts")); // script dir
            processBuilder.redirectErrorStream(true);

            // TODO: Use one of the following for io? 
            // processBuilder.inheritIO(); // use io in java
            // redirectInput(File file), redirectOutput(File file), redirectError(File file)

            // Start the process
            Process process = processBuilder.start();

            // Capture output
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            // Wait for the process to complete
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                return ResponseEntity.ok(output.toString());
            } else {
                System.out.println("output: " + output);
                String response = "Error executing Go script. Exit code: "  + String.valueOf(exitCode);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }
}

