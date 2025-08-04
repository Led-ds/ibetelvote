package com.br.ibetelvote.infrastructure.resources;

import com.br.ibetelvote.infrastructure.repositories.UserJpaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/debug")
@RequiredArgsConstructor
public class DebugController {

    private final UserJpaRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/test-bcrypt")
    public ResponseEntity<String> testBCrypt() {
        try {
            // 1. Dados de teste
            String rawPassword = "123456";
            String email = "carlos.oliveira@igreja.com";

            // 2. Buscar usuário do banco
            var user = userRepository.findByEmail(email);
            if (user.isEmpty()) {
                return ResponseEntity.ok("❌ Usuário não encontrado no banco");
            }

            String hashedFromDB = user.get().getPassword();

            // 3. Testar com PasswordEncoder injetado
            boolean matchesInjected = passwordEncoder.matches(rawPassword, hashedFromDB);

            // 4. Testar com BCryptPasswordEncoder direto
            BCryptPasswordEncoder directEncoder = new BCryptPasswordEncoder();
            boolean matchesDirect = directEncoder.matches(rawPassword, hashedFromDB);

            // 5. Gerar novo hash para comparação
            String newHash = passwordEncoder.encode(rawPassword);
            boolean matchesNewHash = passwordEncoder.matches(rawPassword, newHash);

            // 6. Verificar se o hash do banco é válido
            boolean isValidBCrypt = hashedFromDB.startsWith("$2a$") || hashedFromDB.startsWith("$2b$") || hashedFromDB.startsWith("$2y$");

            StringBuilder result = new StringBuilder();
            result.append("🔍 TESTE BCRYPT DEBUG\n\n");
            result.append("📋 DADOS:\n");
            result.append("Email: ").append(email).append("\n");
            result.append("Senha raw: '").append(rawPassword).append("'\n");
            result.append("Hash do banco: ").append(hashedFromDB).append("\n");
            result.append("Tamanho hash: ").append(hashedFromDB.length()).append("\n\n");

            result.append("🧪 TESTES:\n");
            result.append("PasswordEncoder injetado: ").append(matchesInjected ? "✅ MATCH" : "❌ NO MATCH").append("\n");
            result.append("BCryptPasswordEncoder direto: ").append(matchesDirect ? "✅ MATCH" : "❌ NO MATCH").append("\n");
            result.append("Hash é BCrypt válido: ").append(isValidBCrypt ? "✅ SIM" : "❌ NÃO").append("\n");
            result.append("Novo hash funciona: ").append(matchesNewHash ? "✅ SIM" : "❌ NÃO").append("\n\n");

            result.append("🔧 DETALHES:\n");
            result.append("Classe do PasswordEncoder: ").append(passwordEncoder.getClass().getSimpleName()).append("\n");
            result.append("Novo hash gerado: ").append(newHash).append("\n\n");

            result.append("🎯 CONCLUSÃO:\n");
            if (matchesInjected) {
                result.append("✅ BCrypt está funcionando! Problema pode estar em outro lugar.");
            } else if (matchesDirect) {
                result.append("⚠️ BCrypt direto funciona, mas injetado não. Problema na configuração do Spring.");
            } else {
                result.append("❌ Hash do banco está corrompido ou incompatível.");
            }

            return ResponseEntity.ok(result.toString());

        } catch (Exception e) {
            return ResponseEntity.ok("❌ ERRO: " + e.getMessage());
        }
    }

    @GetMapping("/fix-password")
    public ResponseEntity<String> fixPassword() {
        try {
            String email = "carlos.oliveira@igreja.com";
            String newPassword = "123456";

            // Gerar novo hash com o PasswordEncoder atual
            String newHash = passwordEncoder.encode(newPassword);

            // Buscar e atualizar usuário
            var userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                return ResponseEntity.ok("Usuário não encontrado");
            }

            var user = userOpt.get();
            user.setPassword(newHash);
            userRepository.save(user);

            // Testar se funciona
            boolean works = passwordEncoder.matches(newPassword, newHash);

            return ResponseEntity.ok(
                    "SENHA ATUALIZADA!\n" +
                            "Email: " + email + "\n" +
                            "Nova senha: " + newPassword + "\n" +
                            "Novo hash: " + newHash + "\n" +
                            "Teste: " + (works ? "FUNCIONA" : "NÃO FUNCIONA")
            );

        } catch (Exception e) {
            return ResponseEntity.ok("ERRO: " + e.getMessage());
        }
    }

    @GetMapping("/debug-bcrypt")
    @Operation(summary = "Debug BCrypt", description = "Teste de validação BCrypt")
    @ApiResponse(responseCode = "200", description = "Resultado do teste")
    public ResponseEntity<String> debugBCrypt() {
        try {
            // Usar o PasswordEncoder já configurado
            PasswordEncoder encoder = new BCryptPasswordEncoder();

            String rawPassword = "123456";
            String testHash = "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi";

            boolean matches = encoder.matches(rawPassword, testHash);

            // Gerar novo hash
            String newHash = encoder.encode(rawPassword);
            boolean newMatches = encoder.matches(rawPassword, newHash);

            StringBuilder result = new StringBuilder();
            result.append("🔍 TESTE BCRYPT\n\n");
            result.append("Senha: '").append(rawPassword).append("'\n");
            result.append("Hash teste: ").append(testHash).append("\n");
            result.append("Match teste: ").append(matches ? "✅ SIM" : "❌ NÃO").append("\n\n");
            result.append("Novo hash: ").append(newHash).append("\n");
            result.append("Match novo: ").append(newMatches ? "✅ SIM" : "❌ NÃO").append("\n");

            return ResponseEntity.ok(result.toString());

        } catch (Exception e) {
            return ResponseEntity.ok("❌ ERRO: " + e.getMessage());
        }
    }

    @PostMapping("/fix-user-password")
    @Operation(summary = "Fix Password", description = "Corrige senha do usuário")
    @ApiResponse(responseCode = "200", description = "Senha corrigida")
    public ResponseEntity<String> fixUserPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.getOrDefault("email", "carlos.oliveira@igreja.com");
            String password = request.getOrDefault("password", "123456");

            // Buscar usuário
            var userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                return ResponseEntity.ok("❌ Usuário não encontrado: " + email);
            }

            // Gerar novo hash
            PasswordEncoder encoder = new BCryptPasswordEncoder();
            String newHash = encoder.encode(password);

            // Atualizar no banco via SQL direto (simulação)
            StringBuilder result = new StringBuilder();
            result.append("✅ INSTRUÇÃO PARA CORRIGIR:\n\n");
            result.append("Execute no DBeaver:\n\n");
            result.append("UPDATE users SET password = '").append(newHash).append("' ");
            result.append("WHERE email = '").append(email).append("';\n\n");
            result.append("Depois teste login com:\n");
            result.append("Email: ").append(email).append("\n");
            result.append("Senha: ").append(password);

            return ResponseEntity.ok(result.toString());

        } catch (Exception e) {
            return ResponseEntity.ok("❌ ERRO: " + e.getMessage());
        }
    }

}