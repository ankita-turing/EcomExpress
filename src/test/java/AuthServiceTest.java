
import org.ecom.entity.User;
import org.ecom.model.AuthRequest;
import org.ecom.model.AuthResponse;
import org.ecom.repository.UserRepository;
import org.ecom.security.JwtService;
import org.ecom.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private AuthRequest authRequest;

    @BeforeEach
    void setUp() {
        authRequest = new AuthRequest();
        authRequest.setName("Test User");
        authRequest.setEmail("test@example.com");
        authRequest.setPassword("password123");
    }

    @Test
    void register_Success() {
        // Mock save
        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail(authRequest.getEmail());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(passwordEncoder.encode(authRequest.getPassword())).thenReturn("encodedPass");
        when(jwtService.generateToken(any(User.class))).thenReturn("mock-jwt");

        AuthResponse response = authService.register(authRequest);

        assertNotNull(response.getToken());
        assertEquals("Test User", response.getName());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void login_Success() {
        // Mock findByEmail
        User user = new User();
        user.setId(1L);
        user.setEmail(authRequest.getEmail());
        user.setPassword("encodedPass");
        when(userRepository.findByEmail(authRequest.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(authRequest.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtService.generateToken(any(User.class))).thenReturn("mock-jwt");

        AuthResponse response = authService.login(authRequest);

        assertNotNull(response.getToken());
        verify(passwordEncoder, times(1)).matches(anyString(), anyString());
    }

    @Test
    void login_UserNotFound_ThrowsException() {
        when(userRepository.findByEmail(authRequest.getEmail())).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> authService.login(authRequest));
    }

    @Test
    void login_InvalidPassword_ThrowsException() {
        User user = new User();
        user.setId(1L);
        user.setEmail(authRequest.getEmail());
        user.setPassword("encodedPass");
        when(userRepository.findByEmail(authRequest.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(authRequest.getPassword(), user.getPassword())).thenReturn(false);

        assertThrows(org.springframework.security.authentication.BadCredentialsException.class, () -> authService.login(authRequest));
    }
}