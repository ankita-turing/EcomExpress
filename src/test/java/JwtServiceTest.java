

import org.ecom.entity.auth.User;
import org.ecom.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import java.lang.reflect.Field;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private String secret;
    private long expiration;

    @BeforeEach
    void setUp() throws Exception {
        jwtService = new JwtService();

        // A strong 256-bit secret key (minimum 32 bytes for HS256)
        secret = "12345678901234567890123456789012";
        expiration = 1000 * 60 * 60; // 1 hour

        // Inject @Value fields manually using reflection since Spring isn’t running
        setField(jwtService, "secret", secret);
        setField(jwtService, "expiration", expiration);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void shouldGenerateAndExtractEmailFromToken() {
        User user = new User(1L, "John Doe", "john@example.com", "pass", "ROLE_USER");

        String token = jwtService.generateToken(user);
        assertNotNull(token);

        String extractedEmail = jwtService.extractEmail(token);
        assertEquals(user.getEmail(), extractedEmail);
    }

    @Test
    void shouldValidateTokenSuccessfully() {
        User userEntity = new User(1L, "John", "john@example.com", "pass", "ROLE_USER");
        String token = jwtService.generateToken(userEntity);

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                userEntity.getEmail(),
                userEntity.getPassword(),
                Collections.emptyList()
        );

        boolean isValid = jwtService.isValid(token, userDetails);
        assertTrue(isValid);
    }

    @Test
    void shouldReturnFalseForInvalidUserEmail() {
        User userEntity = new User(1L, "John", "john@example.com", "pass", "ROLE_USER");
        String token = jwtService.generateToken(userEntity);

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                "jo@example.com",
                userEntity.getPassword(),
                Collections.emptyList()
        );

        boolean isValid = jwtService.isValid(token, userDetails);
        assertFalse(isValid);
    }

}
