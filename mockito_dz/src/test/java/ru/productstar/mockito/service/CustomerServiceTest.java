package ru.productstar.mockito.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.productstar.mockito.model.Customer;
import ru.productstar.mockito.repository.CustomerRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {
    @Test
    void getOrCreateIvan() {
        CustomerRepository mockRepository = Mockito.mock(CustomerRepository.class);
        CustomerService mockService = new CustomerService(mockRepository);

        when(mockRepository.getByName("Ivan")).thenReturn(null);
        Customer ivan = new Customer("Ivan");
        when(mockRepository.add(any(Customer.class))).thenReturn(ivan);

        mockService.getOrCreate("Ivan");

        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);

        verify(mockRepository, times(1)).getByName("Ivan");
        verify(mockRepository, times(1)).add(captor.capture());
        assertEquals("Ivan", captor.getValue().getName());

        InOrder inOrder = inOrder(mockRepository);

        inOrder.verify(mockRepository).getByName("Ivan");
        inOrder.verify(mockRepository).add(captor.capture());
    }

    @Test
    void getOrCreateOleg() {

        CustomerRepository mockRepository = Mockito.mock(CustomerRepository.class);
        CustomerService mockService = new CustomerService(mockRepository);

        when(mockRepository.getByName("Oleg")).thenReturn(null);
        Customer ivan = new Customer("Oleg");
        when(mockRepository.add(any(Customer.class))).thenReturn(ivan);

        mockService.getOrCreate("Oleg");

        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        ArgumentCaptor<String> captorString = ArgumentCaptor.forClass(String.class);

        verify(mockRepository, times(1)).getByName("Oleg");

        verify(mockRepository, times(1)).add(captor.capture());
        assertEquals("Oleg", captor.getValue().getName());

        verify(mockRepository).getByName(captorString.capture());
        assertEquals("Oleg", captorString.getValue());

        InOrder inOrder = inOrder(mockRepository);

        inOrder.verify(mockRepository).getByName("Oleg");
        inOrder.verify(mockRepository).add(captor.capture());
    }

    /**
     * Тест 1 - Получение покупателя "Ivan"
     * Проверки:
     * - очередность и точное количество вызовов каждого метода из CustomerRepository
     *
     * Тест 2 - Получение покупателя "Oleg"
     * Проверки:
     * - очередность и точное количество вызовов каждого метода из CustomerRepository
     * - в метод getOrCreate была передана строка "Oleg"
     */

}
