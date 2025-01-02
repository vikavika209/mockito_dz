package ru.productstar.mockito.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.productstar.mockito.ProductNotFoundException;
import ru.productstar.mockito.model.*;
import ru.productstar.mockito.repository.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    private ProductRepository productRepository;
    private CustomerRepository customerRepository;
    private OrderRepository orderRepository;
    private WarehouseService warehouseService;
    private CustomerService customerService;
    private OrderService orderService;

    @BeforeEach
    void setup() {
        productRepository = Mockito.mock(ProductRepository.class);
        customerRepository = Mockito.mock(CustomerRepository.class);
        orderRepository = Mockito.mock(OrderRepository.class);
        warehouseService = Mockito.mock(WarehouseService.class);

        customerService = new CustomerService(customerRepository);
        orderService = new OrderService(customerService, warehouseService, orderRepository, productRepository);
    }

    @Test
    void create() {
        when(customerRepository.getByName("Oleg")).thenReturn(null);

        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        when(customerRepository.add(any(Customer.class))).thenAnswer(invocationOnMock -> {
            return invocationOnMock.getArgument(0);
        });

        customerService.getOrCreate("Oleg");

        verify(customerRepository).add(captor.capture());
        assertEquals("Oleg", captor.getValue().getName());

        when(orderRepository.create(any(Customer.class))).thenAnswer(invocationOnMock -> {
            return new Order(invocationOnMock.getArgument(0));
        });

        Order orderForOleg = orderService.create("Oleg");
        assertEquals("Oleg", orderForOleg.getCustomer().getName());

        Order orderForNewClient = orderService.create("Ivan");

        verify(orderRepository, times(2)).create(any(Customer.class));

    }
    @Test
    void addExistingProduct() throws ProductNotFoundException {
       Product existingProduct = new Product("phone");
        when(productRepository.getByName("phone")).thenReturn(existingProduct);

        Warehouse warehouse = new Warehouse("MainWarehouse", 20);
        when(warehouseService.findWarehouse("phone", 10)).thenReturn(warehouse);

        Stock stock = new Stock(existingProduct, 100, 200);
        when(warehouseService.getStock(warehouse, "phone")).thenReturn(stock);

        Order order = new Order(customerService.getOrCreate("Ivan"));

        orderService.addProduct(order, "phone", 10, false);

        verify(productRepository).getByName("phone");
        verify(warehouseService).findWarehouse("phone", 10);
    }

    @Test
    void addNonExistingProductThrowsException() {
        when(warehouseService.findWarehouse("nonexistent", 10)).thenReturn(null);

        Order order = new Order(customerService.getOrCreate("Ivan"));

        assertThrows(ProductNotFoundException.class, () -> {
            orderService.addProduct(order, "nonexistent", 10, false);
        });

    }

    @Test
    void addSufficientQty() throws ProductNotFoundException {
        Order order = new Order(customerService.getOrCreate("Ivan"));
        Warehouse warehouse = new Warehouse("warehouse", 20);
        Stock stock = new Stock(new Product("phone"), 100, 20);

        when(warehouseService.findWarehouse(any(String.class), any(Integer.class))).thenReturn(warehouse);
        when(warehouseService.getStock(any(Warehouse.class), any(String.class))).thenReturn(stock);
        when(productRepository.getByName(any(String.class))).thenAnswer(invocationOnMock -> {
            return new Product(invocationOnMock.getArgument(0, String.class));});

        orderService.addProduct(order, "phone", 10, false);
        verify(productRepository).getByName("phone");
    }

    @Test
    void addNotSufficientQty() throws ProductNotFoundException {
        Product product = new Product("phone");
        Stock stock = new Stock(product, 100, 20);

        assertNull(warehouseService.findWarehouse("phone", 25));
    }

    @Test
    void addProductWithFastestDelivery() throws ProductNotFoundException {
        Order order = new Order(customerService.getOrCreate("Ivan"));
        Product phone = new Product("phone");
        Warehouse warehouse = new Warehouse("warehouse", 10);
        Stock stock = new Stock(phone, 100, 50);

        when(warehouseService.findClosestWarehouse("phone", 20)).thenReturn(warehouse);
        when(warehouseService.getStock(warehouse, "phone")).thenReturn(stock);

        orderService.addProduct(order, "phone", 20, true);

        verify(productRepository).getByName("phone");
        verify(warehouseService).findClosestWarehouse("phone", 20);
        verify(warehouseService).getStock(warehouse, "phone");
    }

    @Test
    void verifyTotalAmount() throws ProductNotFoundException {
        Product phone = new Product("phone");
        Product laptop = new Product("laptop");
        Warehouse warehouse = new Warehouse("warehouse", 20);
        Stock stock1 = new Stock(phone, 100, 10);
        Stock stock2 = new Stock(laptop, 300, 50);
        warehouse.addStock(stock1);
        warehouse.addStock(stock2);

        when(warehouseService.findWarehouse(any(String.class), any(Integer.class))).thenReturn(warehouse);
        when(warehouseService.getStock(warehouse, "phone")).thenReturn(stock1);
        when(warehouseService.getStock(warehouse, "laptop")).thenReturn(stock2);
        when(productRepository.getByName("phone")).thenReturn(phone);
        when(productRepository.getByName("laptop")).thenReturn(laptop);

        Customer petr = customerService.getOrCreate("Petr");
        Order order = new Order(petr);

        when(orderRepository.addDelivery(any(Integer.class), any(Delivery.class))).thenAnswer(invocation -> {
            Delivery capturedDelivery = invocation.getArgument(1, Delivery.class);
            order.addDelivery(capturedDelivery);
            return order;
        });

        orderService.addProduct(order, "phone", 1, false);
        orderService.addProduct(order, "laptop", 1, false);
        orderService.addProduct(order, "phone", 1, false);

        verify(warehouseService, times(2)).findWarehouse("phone", 1);
        verify(warehouseService, times(1)).findWarehouse("laptop", 1);
        verify(orderRepository, times(3)).addDelivery(any(Integer.class), any(Delivery.class));
        assertEquals(500, order.getTotal());
    }

    /**
     * Покрыть тестами методы create и addProduct.
     * Можно использовать вызовы реальных методов.
     *
     * Должны быть проверены следующие сценарии:
     * + создание ордера для существующего и нового клиента
     * + добавление существующего и несуществующего товара
     * + добавление товара в достаточном и не достаточном количестве
     * + заказ товара с быстрой доставкой
     *
     * Проверки:
     * + общая сумма заказа соответствует ожидаемой
     * + корректная работа для несуществующего товара
     * + порядок и количество вызовов зависимых сервисов
     * + факт выбрасывания ProductNotFoundException
     */
}
