package order;

import org.ecom.entity.auth.User;
import org.ecom.entity.order.Order;
import org.ecom.entity.product.Product;
import org.ecom.model.order.OrderRequest;
import org.ecom.repository.auth.UserRepository;
import org.ecom.repository.order.OrderRepository;
import org.ecom.repository.product.ProductRepository;
import org.ecom.service.order.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProductRepository productRepository;
    @Mock private Authentication authentication;
    @Mock private SecurityContext securityContext;

    @InjectMocks private OrderService orderService;

    private User user;
    private Product product1;
    private Product product2;

    private Order order1;
    private Order order2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        product1 = new Product();
        product1.setId(1L);
        product1.setName("Laptop");
        product1.setPrice(50000.0);

        product2 = new Product();
        product2.setId(2L);
        product2.setName("Mouse");
        product2.setPrice(1500.0);

        order1 = new Order();
        order1.setId(100L);
        order1.setUser(user);

        order2 = new Order();
        order2.setId(101L);
        order2.setUser(user);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        SecurityContextHolder.setContext(securityContext);
    }

    /**
     * ✅ 1. Logged-in user can submit an order request.
     */
    @Test
    void placeOrder_UserAuthenticated_Success() {
        OrderRequest.OrderItemRequest item = new OrderRequest.OrderItemRequest();
        item.setProductId(1L);
        item.setQuantity(1);

        OrderRequest request = new OrderRequest();
        request.setItems(List.of(item));

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.placeOrder(request);

        assertNotNull(result);
        assertEquals(user, result.getUser());
        verify(orderRepository).save(any(Order.class));
    }

    /**
     * ✅ 2. The system calculates total order amount automatically.
     */
    @Test
    void placeOrder_TotalCalculatedCorrectly() {
        OrderRequest.OrderItemRequest i1 = new OrderRequest.OrderItemRequest(1L, 1);
        OrderRequest.OrderItemRequest i2 = new OrderRequest.OrderItemRequest(2L, 2);
        OrderRequest request = new OrderRequest();
        request.setItems(List.of(i1, i2));

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product2));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.placeOrder(request);

        double expectedTotal = (1 * 50000.0) + (2 * 1500.0);
        assertEquals(expectedTotal, result.getTotalAmount());
    }

    /**
     * ✅ 3. The order and items are persisted in the database (verified via repository call).
     */
    @Test
    void placeOrder_PersistsOrderAndItems() {
        OrderRequest.OrderItemRequest item = new OrderRequest.OrderItemRequest(1L, 3);
        OrderRequest request = new OrderRequest();
        request.setItems(List.of(item));

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.placeOrder(request);

        verify(orderRepository, times(1)).save(any(Order.class));
        assertEquals(1, result.getItems().size());
        assertEquals(3, result.getItems().get(0).getQuantity());
    }

    /**
     * ✅ 4. Each order is linked to the authenticated user.
     */
    @Test
    void placeOrder_OrderLinkedToAuthenticatedUser() {
        OrderRequest.OrderItemRequest item = new OrderRequest.OrderItemRequest(2L, 1);
        OrderRequest request = new OrderRequest();
        request.setItems(List.of(item));

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product2));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.placeOrder(request);

        assertEquals("test@example.com", result.getUser().getEmail());
    }

    /**
     * ✅ 5. Returned order details include items and total.
     */
    @Test
    void placeOrder_ReturnsFullOrderDetails() {
        OrderRequest.OrderItemRequest i1 = new OrderRequest.OrderItemRequest(1L, 1);
        OrderRequest request = new OrderRequest();
        request.setItems(List.of(i1));

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order order = orderService.placeOrder(request);

        assertNotNull(order);
        assertEquals(1, order.getItems().size());
        assertEquals(50000.0, order.getTotalAmount());
        assertEquals("Laptop", order.getItems().get(0).getProduct().getName());
    }

    @Test
    void getUserOrders_ShouldFetchAndResaveOrders() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(orderRepository.findByUser(user)).thenReturn(List.of(order1, order2));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<Order> result = orderService.getUserOrders();

        assertEquals(2, result.size());
        verify(orderRepository, times(2)).save(any(Order.class)); // each order re-saved
    }

    /**
     * ✅ Fetch one order by ID, persist it again.
     */
    @Test
    void getOrderById_ShouldFetchAndResaveOrder() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order1));
        when(orderRepository.save(order1)).thenReturn(order1);

        Order result = orderService.getOrderById(100L);

        assertNotNull(result);
        assertEquals(order1, result);
        verify(orderRepository, times(1)).save(order1); // order re-saved
    }

    /**
     * ✅ Throw when accessing another user’s order.
     */
    @Test
    void getOrderById_ShouldThrowIfNotUserOrder() {
        User another = new User();
        another.setId(2L);
        Order otherOrder = new Order();
        otherOrder.setId(200L);
        otherOrder.setUser(another);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(orderRepository.findById(200L)).thenReturn(Optional.of(otherOrder));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> orderService.getOrderById(200L));
        assertEquals("Access denied: not your order", ex.getMessage());
    }

    /**
     * ✅ Throw when order not found.
     */
    @Test
    void getOrderById_ShouldThrowIfOrderNotFound() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> orderService.getOrderById(999L));
    }

    /**
     * ✅ Throw when user not found.
     */
    @Test
    void getUserOrders_ShouldThrowIfUserNotFound() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> orderService.getUserOrders());
    }
}
