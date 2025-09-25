// package com.yern.service.secrets.gcp;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.Order;

// import java.io.IOException;
// import java.util.HashMap;
// import java.util.Optional;
// import java.util.Map;

// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.beans.factory.annotation.Autowired;

// @SpringBootTest(classes = GoogleSecretManager.class)
// public class GoogleSecretManagerIntegrationTest {
//     @Autowired
//     private GoogleSecretManager manager;
//     private HashMap<String, String> secretsByNames;

//     public GoogleSecretManagerIntegrationTest(GoogleSecretManager manager) {
//         this.manager = manager;
//     }

//     @BeforeEach 
//     public void setup() {
//         if (!(secretsByNames.isEmpty())) {
//             secretsByNames.put("boop", "bop");
//             secretsByNames.put("boop1", "bop1");
//             secretsByNames.put("boop2", "bop2");
//             secretsByNames.put("boop3", "bop3");
//         }
//     }

//     @Test 
//     @Order(1)
//     public void createInitialSecretVersions() {
//         for(Map.Entry<String, String>entry : secretsByNames.entrySet()) {
//             assertDoesNotThrow(() -> manager.create(entry.getKey(), entry.getValue()));
//         }
//     }

//     @Test 
//     @Order(2)
//     public void getInitialSecrets() throws IOException {
//         for(Map.Entry<String, String>entry : secretsByNames.entrySet()) {
//             assertEquals(
//                 this.manager.get(entry.getKey(), Optional.empty()),
//                 entry.getValue()
//             );
//         }
//     }
// }
