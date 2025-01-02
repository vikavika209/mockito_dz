package ru.productstar.mockito.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.productstar.mockito.model.Product;
import ru.productstar.mockito.model.Stock;
import ru.productstar.mockito.model.Warehouse;
import ru.productstar.mockito.repository.InitRepository;
import ru.productstar.mockito.repository.WarehouseRepository;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WarehouseServiceTest {
    private WarehouseRepository warehouseRepository;
    private WarehouseService warehouseService;

    @BeforeEach
    void setUp() {
        warehouseRepository = Mockito.mock(WarehouseRepository.class);
        warehouseService = new WarehouseService(warehouseRepository);
    }

    @Test
    void findWarehouseTest(){

        Product phone = new Product("phone");
        Stock stock1 = new Stock(phone, 100, 10);
        Stock stock2 = new Stock(phone, 125, 3);
        Stock stock3 = new Stock(phone, 110, 5);
        Warehouse warehouse1 = new Warehouse("w1", 10);
        Warehouse warehouse2 = new Warehouse("w2", 20);
        Warehouse warehouse3 = new Warehouse("w3", 30);
        warehouse1.addStock(stock1);
        warehouse2.addStock(stock2);
        warehouse3.addStock(stock3);
        warehouseRepository.add(warehouse1);
        warehouseRepository.add(warehouse2);
        warehouseRepository.add(warehouse3);

        when(warehouseRepository.all()).thenReturn(List.of(warehouse1, warehouse2, warehouse3));

        Warehouse warehouseTest1 = warehouseService.findWarehouse("tv", 1);
        Warehouse warehouseTest2 = warehouseService.findWarehouse("phone", 5);
        Warehouse warehouseTest3 = warehouseService.findWarehouse("phone", 20);

        assertEquals(100,warehouseTest2.getStocks().getFirst().getPrice());
        assertNull(warehouseTest1);
        assertNull(warehouseTest3);
        verify(warehouseRepository, times(3)).all();
    }

    @Test
    void findClosestWarehouseTest(){

        Product phone = new Product("phone");
        Stock stock1 = new Stock(phone, 100, 10);
        Stock stock2 = new Stock(phone, 125, 3);
        Stock stock3 = new Stock(phone, 110, 5);
        Warehouse warehouse1 = new Warehouse("w1", 100);
        Warehouse warehouse2 = new Warehouse("w2", 20);
        Warehouse warehouse3 = new Warehouse("w3", 30);
        warehouse1.addStock(stock1);
        warehouse2.addStock(stock2);
        warehouse3.addStock(stock3);
        warehouseRepository.add(warehouse1);
        warehouseRepository.add(warehouse2);
        warehouseRepository.add(warehouse3);

        when(warehouseRepository.all()).thenReturn(List.of(warehouse1, warehouse2, warehouse3));

        Warehouse warehouseTest1 = warehouseService.findClosestWarehouse("tv", 1);
        Warehouse warehouseTest2 = warehouseService.findClosestWarehouse("phone", 5);
        Warehouse warehouseTest3 = warehouseService.findClosestWarehouse("phone", 20);

        assertEquals(110,warehouseTest2.getStocks().getFirst().getPrice());
        assertNull(warehouseTest1);
        assertNull(warehouseTest3);
        verify(warehouseRepository, times(3)).all();
    }

    /**
     * Покрыть тестами методы findWarehouse и findClosestWarehouse.
     * Вызывать реальные методы зависимых сервисов и репозиториев нельзя.
     * Поиск должен осуществляться как минимум на трех складах.
     *
     * Должны быть проверены следующие сценарии:
     * - поиск несуществующего товара
     * - поиск существующего товара с достаточным количеством
     * - поиск существующего товара с недостаточным количеством
     *
     * Проверки:
     * - товар находится на нужном складе, учитывается количество и расстояние до него
     * - корректная работа для несуществующего товара
     * - порядок и количество вызовов зависимых сервисов
     */
}
