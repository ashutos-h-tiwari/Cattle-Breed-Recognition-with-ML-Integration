package com.example.animalbreed;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@RestController
@CrossOrigin(origins = "*")
public class PredictionController {

    @PostMapping("/predict")
    public ResponseEntity<String> predictBreed(@RequestParam("image") MultipartFile imageFile) {
        try {
            if (imageFile.isEmpty()) {
                return ResponseEntity.badRequest().body("Image file is missing");
            }

            System.out.println("Image received with size: " + imageFile.getSize() + " bytes");

            String mlApiUrl = "http://localhost:5000/predict"; // <-- yahan sahi URL lagega 

            InputStream inputStream = imageFile.getInputStream();
            InputStreamResource inputStreamResource = new InputStreamResource(inputStream) {
                @Override
                public String getFilename() {
                    return imageFile.getOriginalFilename();
                }
                @Override
                public long contentLength() throws IOException {
                    return imageFile.getSize();
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("image", inputStreamResource);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> mlResponse = restTemplate.postForEntity(mlApiUrl, requestEntity, String.class);

            if (mlResponse.getStatusCode() == HttpStatus.OK) {
                return ResponseEntity.ok(mlResponse.getBody());
            } else {
                return ResponseEntity.status(mlResponse.getStatusCode())
                        .body("ML API returned error: " + mlResponse.getBody());
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Server error occurred: " + e.getMessage());
        }
    }
}
