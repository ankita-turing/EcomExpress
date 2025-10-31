
import org.ecom.entity.auth.User;
import org.ecom.model.auth.AuthRequest;
import org.ecom.model.auth.AuthResponse;
import org.ecom.model.auth.DeleteRequest;
import org.ecom.repository.auth.UserRepository;
import org.ecom.security.JwtService;
import org.ecom.service.auth.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @Test
    void deleteSelf_success() {
        // Mock current authenticated user
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");

        DeleteRequest request = new DeleteRequest();
        request.setPassword("rawPassword");

        Authentication authentication = new UsernamePasswordAuthenticationToken(user.getEmail(), null);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Mock repository and password encoder
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("rawPassword", "encodedPassword")).thenReturn(true);

        // Call method
        authService.deleteSelf(request);

        // Verify deletion
        verify(userRepository, times(1)).deleteById(user.getId());
    }

    @Test
    void deleteSelf_userNotFound_throwsException() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("test@example.com");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        DeleteRequest request = new DeleteRequest();
        request.setPassword("password");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.deleteSelf(request));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void deleteSelf_invalidPassword_throwsException() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("test@example.com");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        DeleteRequest request = new DeleteRequest();
        request.setPassword("wrongPassword");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.deleteSelf(request));
        assertEquals("Invalid password confirmation", exception.getMessage());
    }

    // ========================= deleteById Tests =========================
    @Test
    void deleteById_success() {
        Long userId = 1L;

        when(userRepository.existsById(userId)).thenReturn(true);

        authService.deleteById(userId);

        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    void deleteById_userNotFound_throwsException() {
        Long userId = 1L;

        when(userRepository.existsById(userId)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.deleteById(userId));
        assertEquals("User not found", exception.getMessage());
    }
}