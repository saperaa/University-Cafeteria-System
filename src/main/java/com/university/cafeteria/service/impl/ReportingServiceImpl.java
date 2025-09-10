package com.university.cafeteria.service.impl;

import com.university.cafeteria.model.Order;
import com.university.cafeteria.model.Order.OrderStatus;
import com.university.cafeteria.model.Student;
import com.university.cafeteria.model.MenuItem;
import com.university.cafeteria.repository.OrderRepository;
import com.university.cafeteria.repository.UserRepository;
import com.university.cafeteria.repository.MenuRepository;
import com.university.cafeteria.service.ReportingService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of ReportingService
 * Demonstrates extensive use of Java Streams for data aggregation and reporting
 * Uses multiple repository dependencies following Dependency Injection principle
 */
public class ReportingServiceImpl implements ReportingService {
    
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final MenuRepository menuRepository;
    
    public ReportingServiceImpl(OrderRepository orderRepository, 
                               UserRepository userRepository,
                               MenuRepository menuRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.menuRepository = menuRepository;
    }
    
    @Override
    public DailySalesReport generateDailySalesReport(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        
        List<Order> dayOrders = orderRepository.findByDate(date).stream()
                .filter(order -> order.getStatus() == OrderStatus.COMPLETED)
                .collect(Collectors.toList());
        
        BigDecimal totalSales = dayOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        int totalOrders = dayOrders.size();
        
        Set<String> uniqueCustomers = dayOrders.stream()
                .map(Order::getStudentId)
                .collect(Collectors.toSet());
        int totalCustomers = uniqueCustomers.size();
        
        BigDecimal averageOrderValue = totalOrders > 0 ?
                totalSales.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;
        
        // Calculate items sold
        Map<String, Integer> itemsSold = dayOrders.stream()
                .flatMap(order -> order.getItems().stream())
                .collect(Collectors.groupingBy(
                    item -> item.getMenuItem().getName(),
                    Collectors.summingInt(item -> item.getQuantity())
                ));
        
        return new DailySalesReport(date, totalSales, totalOrders, totalCustomers, averageOrderValue, itemsSold);
    }
    
    @Override
    public WeeklySalesReport generateWeeklySalesReport(LocalDate startDate) {
        if (startDate == null) {
            throw new IllegalArgumentException("Start date cannot be null");
        }
        
        LocalDate endDate = startDate.plusDays(6);
        List<DailySalesReport> dailyReports = new ArrayList<>();
        
        // Generate daily reports for the week
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            dailyReports.add(generateDailySalesReport(currentDate));
            currentDate = currentDate.plusDays(1);
        }
        
        BigDecimal totalWeeklySales = dailyReports.stream()
                .map(DailySalesReport::getTotalSales)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        int totalWeeklyOrders = dailyReports.stream()
                .mapToInt(DailySalesReport::getTotalOrders)
                .sum();
        
        return new WeeklySalesReport(startDate, endDate, dailyReports, totalWeeklySales, totalWeeklyOrders);
    }
    
    @Override
    public MonthlySalesReport generateMonthlySalesReport(int year, int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }
        
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        
        List<DailySalesReport> dailyReports = new ArrayList<>();
        
        // Generate daily reports for the month
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            dailyReports.add(generateDailySalesReport(currentDate));
            currentDate = currentDate.plusDays(1);
        }
        
        BigDecimal totalMonthlySales = dailyReports.stream()
                .map(DailySalesReport::getTotalSales)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        int totalMonthlyOrders = dailyReports.stream()
                .mapToInt(DailySalesReport::getTotalOrders)
                .sum();
        
        return new MonthlySalesReport(year, month, startDate, endDate, 
                                    totalMonthlySales, totalMonthlyOrders, dailyReports);
    }
    
    @Override
    public LoyaltyPointsReport generateLoyaltyPointsReport(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date cannot be null");
        }
        
        List<Order> ordersInRange = orderRepository.findByDateRange(startDate, endDate);
        
        int totalPointsEarned = ordersInRange.stream()
                .filter(order -> order.getStatus() == OrderStatus.COMPLETED)
                .mapToInt(Order::getLoyaltyPointsEarned)
                .sum();
        
        int totalPointsRedeemed = ordersInRange.stream()
                .mapToInt(Order::getLoyaltyPointsRedeemed)
                .sum();
        
        BigDecimal totalDiscountsGiven = ordersInRange.stream()
                .map(Order::getDiscountAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Get top point earners in the period
        Map<String, Integer> topPointEarners = ordersInRange.stream()
                .filter(order -> order.getStatus() == OrderStatus.COMPLETED)
                .collect(Collectors.groupingBy(
                    Order::getStudentId,
                    Collectors.summingInt(Order::getLoyaltyPointsEarned)
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (e1, e2) -> e1,
                    LinkedHashMap::new
                ));
        
        return new LoyaltyPointsReport(startDate, endDate, totalPointsEarned, 
                                     totalPointsRedeemed, totalDiscountsGiven, topPointEarners);
    }
    
    @Override
    public TopSellingItemsReport generateTopSellingItemsReport(LocalDate startDate, LocalDate endDate, int limit) {
        if (startDate == null || endDate == null || limit <= 0) {
            throw new IllegalArgumentException("Invalid parameters for top selling items report");
        }
        
        List<Order> ordersInRange = orderRepository.findByDateRange(startDate, endDate).stream()
                .filter(order -> order.getStatus() == OrderStatus.COMPLETED)
                .collect(Collectors.toList());
        
        // Aggregate item sales data
        Map<String, TopSellingItemsReport.ItemSalesData> itemSalesMap = new HashMap<>();
        
        ordersInRange.stream()
                .flatMap(order -> order.getItems().stream())
                .forEach(orderItem -> {
                    String itemId = orderItem.getMenuItem().getItemId();
                    String itemName = orderItem.getMenuItem().getName();
                    int quantity = orderItem.getQuantity();
                    BigDecimal revenue = orderItem.getSubtotal();
                    
                    itemSalesMap.merge(itemId, 
                        new TopSellingItemsReport.ItemSalesData(itemId, itemName, quantity, revenue),
                        (existing, newData) -> new TopSellingItemsReport.ItemSalesData(
                            itemId, 
                            itemName,
                            existing.getQuantitySold() + newData.getQuantitySold(),
                            existing.getTotalRevenue().add(newData.getTotalRevenue())
                        )
                    );
                });
        
        List<TopSellingItemsReport.ItemSalesData> topItems = itemSalesMap.values().stream()
                .sorted((item1, item2) -> Integer.compare(item2.getQuantitySold(), item1.getQuantitySold()))
                .limit(limit)
                .collect(Collectors.toList());
        
        return new TopSellingItemsReport(startDate, endDate, topItems);
    }
    
    @Override
    public StudentActivityReport generateStudentActivityReport(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date cannot be null");
        }
        
        List<Order> ordersInRange = orderRepository.findByDateRange(startDate, endDate);
        
        Set<String> activeStudents = ordersInRange.stream()
                .map(Order::getStudentId)
                .collect(Collectors.toSet());
        
        int totalActiveStudents = activeStudents.size();
        
        // Count new student registrations in the period
        int newStudentRegistrations = (int) userRepository.findAllStudents().stream()
                .filter(student -> {
                    LocalDate registrationDate = student.getCreatedAt().toLocalDate();
                    return !registrationDate.isBefore(startDate) && !registrationDate.isAfter(endDate);
                })
                .count();
        
        // Orders by student
        Map<String, Integer> ordersByStudent = ordersInRange.stream()
                .collect(Collectors.groupingBy(
                    Order::getStudentId,
                    Collectors.collectingAndThen(
                        Collectors.counting(),
                        Long::intValue
                    )
                ));
        
        return new StudentActivityReport(startDate, endDate, totalActiveStudents, 
                                       newStudentRegistrations, ordersByStudent);
    }
    
    @Override
    public DashboardData getDashboardData() {
        long[] orderCounts = orderRepository.getCountByStatus();
        
        int pendingOrders = (int) orderCounts[OrderStatus.PENDING.ordinal()];
        int ordersInPreparation = (int) orderCounts[OrderStatus.CONFIRMED.ordinal()] + 
                                 (int) orderCounts[OrderStatus.PREPARING.ordinal()];
        int ordersReadyForPickup = (int) orderCounts[OrderStatus.READY.ordinal()];
        
        // Today's sales
        LocalDate today = LocalDate.now();
        BigDecimal todaysSales = orderRepository.findByDate(today).stream()
                .filter(order -> order.getStatus() == OrderStatus.COMPLETED)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        int todaysOrders = (int) orderRepository.findByDate(today).stream()
                .filter(order -> order.getStatus() == OrderStatus.COMPLETED)
                .count();
        
        // Active students (students with at least one order in the last 30 days)
        LocalDate thirtyDaysAgo = today.minusDays(30);
        int activeStudents = (int) orderRepository.findByDateRange(thirtyDaysAgo, today).stream()
                .map(Order::getStudentId)
                .distinct()
                .count();
        
        return new DashboardData(pendingOrders, ordersInPreparation, ordersReadyForPickup,
                               todaysSales, todaysOrders, activeStudents);
    }
    
    @Override
    public String exportReportToCSV(Object report) {
        if (report == null) {
            return "";
        }
        
        StringBuilder csv = new StringBuilder();
        
        if (report instanceof DailySalesReport) {
            DailySalesReport dailyReport = (DailySalesReport) report;
            csv.append("Daily Sales Report\n");
            csv.append("Date,Total Sales,Total Orders,Total Customers,Average Order Value\n");
            csv.append(String.format("%s,%.2f,%d,%d,%.2f\n",
                dailyReport.getDate(),
                dailyReport.getTotalSales(),
                dailyReport.getTotalOrders(),
                dailyReport.getTotalCustomers(),
                dailyReport.getAverageOrderValue()));
            
        } else if (report instanceof TopSellingItemsReport) {
            TopSellingItemsReport topItemsReport = (TopSellingItemsReport) report;
            csv.append("Top Selling Items Report\n");
            csv.append("Item Name,Quantity Sold,Total Revenue\n");
            topItemsReport.getTopItems().forEach(item ->
                csv.append(String.format("%s,%d,%.2f\n",
                    item.getItemName(),
                    item.getQuantitySold(),
                    item.getTotalRevenue()))
            );
            
        } else if (report instanceof LoyaltyPointsReport) {
            LoyaltyPointsReport loyaltyReport = (LoyaltyPointsReport) report;
            csv.append("Loyalty Points Report\n");
            csv.append("Period,Points Earned,Points Redeemed,Discounts Given\n");
            csv.append(String.format("%s to %s,%d,%d,%.2f\n",
                loyaltyReport.getStartDate(),
                loyaltyReport.getEndDate(),
                loyaltyReport.getTotalPointsEarned(),
                loyaltyReport.getTotalPointsRedeemed(),
                loyaltyReport.getTotalDiscountsGiven()));
        }
        
        return csv.toString();
    }
    

}