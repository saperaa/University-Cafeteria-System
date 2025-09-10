package com.university.cafeteria.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Interface for reporting and analytics services
 * Demonstrates Single Responsibility Principle - focused on reporting only
 */
public interface ReportingService {
    
    /**
     * Generate daily sales report
     * @param date Date for the report
     * @return Daily sales report
     */
    DailySalesReport generateDailySalesReport(LocalDate date);
    
    /**
     * Generate weekly sales report
     * @param startDate Start of the week
     * @return Weekly sales report
     */
    WeeklySalesReport generateWeeklySalesReport(LocalDate startDate);
    
    /**
     * Generate monthly sales report
     * @param year Year
     * @param month Month (1-12)
     * @return Monthly sales report
     */
    MonthlySalesReport generateMonthlySalesReport(int year, int month);
    
    /**
     * Generate loyalty points report
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return Loyalty points report
     */
    LoyaltyPointsReport generateLoyaltyPointsReport(LocalDate startDate, LocalDate endDate);
    
    /**
     * Generate top-selling items report
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @param limit Number of top items to include
     * @return Top-selling items report
     */
    TopSellingItemsReport generateTopSellingItemsReport(LocalDate startDate, LocalDate endDate, int limit);
    
    /**
     * Generate student activity report
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return Student activity report
     */
    StudentActivityReport generateStudentActivityReport(LocalDate startDate, LocalDate endDate);
    
    /**
     * Get real-time dashboard data
     * @return Current system statistics
     */
    DashboardData getDashboardData();
    
    /**
     * Export report to CSV format
     * @param report Report to export
     * @return CSV content as string
     */
    String exportReportToCSV(Object report);
    
    /**
     * Base interface for all reports
     */
    interface Report {
        LocalDate getStartDate();
        LocalDate getEndDate();
        String getReportType();
        java.time.LocalDateTime getGeneratedAt();
    }
    
    /**
     * Daily sales report data
     */
    class DailySalesReport implements Report {
        private final LocalDate date;
        private final BigDecimal totalSales;
        private final int totalOrders;
        private final int totalCustomers;
        private final BigDecimal averageOrderValue;
        private final Map<String, Integer> itemsSold;
        private final java.time.LocalDateTime generatedAt;
        
        public DailySalesReport(LocalDate date, BigDecimal totalSales, int totalOrders, 
                               int totalCustomers, BigDecimal averageOrderValue, 
                               Map<String, Integer> itemsSold) {
            this.date = date;
            this.totalSales = totalSales;
            this.totalOrders = totalOrders;
            this.totalCustomers = totalCustomers;
            this.averageOrderValue = averageOrderValue;
            this.itemsSold = itemsSold;
            this.generatedAt = java.time.LocalDateTime.now();
        }
        
        @Override
        public LocalDate getStartDate() { return date; }
        @Override
        public LocalDate getEndDate() { return date; }
        @Override
        public String getReportType() { return "Daily Sales Report"; }
        @Override
        public java.time.LocalDateTime getGeneratedAt() { return generatedAt; }
        
        public LocalDate getDate() { return date; }
        public BigDecimal getTotalSales() { return totalSales; }
        public int getTotalOrders() { return totalOrders; }
        public int getTotalCustomers() { return totalCustomers; }
        public BigDecimal getAverageOrderValue() { return averageOrderValue; }
        public Map<String, Integer> getItemsSold() { return itemsSold; }
    }
    
    /**
     * Weekly sales report data
     */
    class WeeklySalesReport implements Report {
        private final LocalDate startDate;
        private final LocalDate endDate;
        private final List<DailySalesReport> dailyReports;
        private final BigDecimal totalWeeklySales;
        private final int totalWeeklyOrders;
        private final java.time.LocalDateTime generatedAt;
        
        public WeeklySalesReport(LocalDate startDate, LocalDate endDate, 
                                List<DailySalesReport> dailyReports, 
                                BigDecimal totalWeeklySales, int totalWeeklyOrders) {
            this.startDate = startDate;
            this.endDate = endDate;
            this.dailyReports = dailyReports;
            this.totalWeeklySales = totalWeeklySales;
            this.totalWeeklyOrders = totalWeeklyOrders;
            this.generatedAt = java.time.LocalDateTime.now();
        }
        
        @Override
        public LocalDate getStartDate() { return startDate; }
        @Override
        public LocalDate getEndDate() { return endDate; }
        @Override
        public String getReportType() { return "Weekly Sales Report"; }
        @Override
        public java.time.LocalDateTime getGeneratedAt() { return generatedAt; }
        
        public List<DailySalesReport> getDailyReports() { return dailyReports; }
        public BigDecimal getTotalWeeklySales() { return totalWeeklySales; }
        public int getTotalWeeklyOrders() { return totalWeeklyOrders; }
    }
    
    /**
     * Monthly sales report data
     */
    class MonthlySalesReport implements Report {
        private final LocalDate startDate;
        private final LocalDate endDate;
        private final int year;
        private final int month;
        private final BigDecimal totalMonthlySales;
        private final int totalMonthlyOrders;
        private final List<DailySalesReport> dailyReports;
        private final java.time.LocalDateTime generatedAt;
        
        public MonthlySalesReport(int year, int month, LocalDate startDate, LocalDate endDate,
                                 BigDecimal totalMonthlySales, int totalMonthlyOrders,
                                 List<DailySalesReport> dailyReports) {
            this.year = year;
            this.month = month;
            this.startDate = startDate;
            this.endDate = endDate;
            this.totalMonthlySales = totalMonthlySales;
            this.totalMonthlyOrders = totalMonthlyOrders;
            this.dailyReports = dailyReports;
            this.generatedAt = java.time.LocalDateTime.now();
        }
        
        @Override
        public LocalDate getStartDate() { return startDate; }
        @Override
        public LocalDate getEndDate() { return endDate; }
        @Override
        public String getReportType() { return "Monthly Sales Report"; }
        @Override
        public java.time.LocalDateTime getGeneratedAt() { return generatedAt; }
        
        public int getYear() { return year; }
        public int getMonth() { return month; }
        public BigDecimal getTotalMonthlySales() { return totalMonthlySales; }
        public int getTotalMonthlyOrders() { return totalMonthlyOrders; }
        public List<DailySalesReport> getDailyReports() { return dailyReports; }
    }
    
    /**
     * Loyalty points report data
     */
    class LoyaltyPointsReport implements Report {
        private final LocalDate startDate;
        private final LocalDate endDate;
        private final int totalPointsEarned;
        private final int totalPointsRedeemed;
        private final BigDecimal totalDiscountsGiven;
        private final Map<String, Integer> topPointEarners;
        private final java.time.LocalDateTime generatedAt;
        
        public LoyaltyPointsReport(LocalDate startDate, LocalDate endDate,
                                  int totalPointsEarned, int totalPointsRedeemed,
                                  BigDecimal totalDiscountsGiven,
                                  Map<String, Integer> topPointEarners) {
            this.startDate = startDate;
            this.endDate = endDate;
            this.totalPointsEarned = totalPointsEarned;
            this.totalPointsRedeemed = totalPointsRedeemed;
            this.totalDiscountsGiven = totalDiscountsGiven;
            this.topPointEarners = topPointEarners;
            this.generatedAt = java.time.LocalDateTime.now();
        }
        
        @Override
        public LocalDate getStartDate() { return startDate; }
        @Override
        public LocalDate getEndDate() { return endDate; }
        @Override
        public String getReportType() { return "Loyalty Points Report"; }
        @Override
        public java.time.LocalDateTime getGeneratedAt() { return generatedAt; }
        
        public int getTotalPointsEarned() { return totalPointsEarned; }
        public int getTotalPointsRedeemed() { return totalPointsRedeemed; }
        public BigDecimal getTotalDiscountsGiven() { return totalDiscountsGiven; }
        public Map<String, Integer> getTopPointEarners() { return topPointEarners; }
    }
    
    /**
     * Top-selling items report data
     */
    class TopSellingItemsReport implements Report {
        private final LocalDate startDate;
        private final LocalDate endDate;
        private final List<ItemSalesData> topItems;
        private final java.time.LocalDateTime generatedAt;
        
        public TopSellingItemsReport(LocalDate startDate, LocalDate endDate,
                                    List<ItemSalesData> topItems) {
            this.startDate = startDate;
            this.endDate = endDate;
            this.topItems = topItems;
            this.generatedAt = java.time.LocalDateTime.now();
        }
        
        @Override
        public LocalDate getStartDate() { return startDate; }
        @Override
        public LocalDate getEndDate() { return endDate; }
        @Override
        public String getReportType() { return "Top Selling Items Report"; }
        @Override
        public java.time.LocalDateTime getGeneratedAt() { return generatedAt; }
        
        public List<ItemSalesData> getTopItems() { return topItems; }
        
        public static class ItemSalesData {
            private final String itemId;
            private final String itemName;
            private final int quantitySold;
            private final BigDecimal totalRevenue;
            
            public ItemSalesData(String itemId, String itemName, int quantitySold, BigDecimal totalRevenue) {
                this.itemId = itemId;
                this.itemName = itemName;
                this.quantitySold = quantitySold;
                this.totalRevenue = totalRevenue;
            }
            
            public String getItemId() { return itemId; }
            public String getItemName() { return itemName; }
            public int getQuantitySold() { return quantitySold; }
            public BigDecimal getTotalRevenue() { return totalRevenue; }
        }
    }
    
    /**
     * Student activity report data
     */
    class StudentActivityReport implements Report {
        private final LocalDate startDate;
        private final LocalDate endDate;
        private final int totalActiveStudents;
        private final int newStudentRegistrations;
        private final Map<String, Integer> ordersByStudent;
        private final java.time.LocalDateTime generatedAt;
        
        public StudentActivityReport(LocalDate startDate, LocalDate endDate,
                                   int totalActiveStudents, int newStudentRegistrations,
                                   Map<String, Integer> ordersByStudent) {
            this.startDate = startDate;
            this.endDate = endDate;
            this.totalActiveStudents = totalActiveStudents;
            this.newStudentRegistrations = newStudentRegistrations;
            this.ordersByStudent = ordersByStudent;
            this.generatedAt = java.time.LocalDateTime.now();
        }
        
        @Override
        public LocalDate getStartDate() { return startDate; }
        @Override
        public LocalDate getEndDate() { return endDate; }
        @Override
        public String getReportType() { return "Student Activity Report"; }
        @Override
        public java.time.LocalDateTime getGeneratedAt() { return generatedAt; }
        
        public int getTotalActiveStudents() { return totalActiveStudents; }
        public int getNewStudentRegistrations() { return newStudentRegistrations; }
        public Map<String, Integer> getOrdersByStudent() { return ordersByStudent; }
    }
    
    /**
     * Dashboard data for real-time statistics
     */
    class DashboardData {
        private final int pendingOrders;
        private final int ordersInPreparation;
        private final int ordersReadyForPickup;
        private final BigDecimal todaysSales;
        private final int todaysOrders;
        private final int activeStudents;
        private final java.time.LocalDateTime lastUpdated;
        
        public DashboardData(int pendingOrders, int ordersInPreparation, int ordersReadyForPickup,
                           BigDecimal todaysSales, int todaysOrders, int activeStudents) {
            this.pendingOrders = pendingOrders;
            this.ordersInPreparation = ordersInPreparation;
            this.ordersReadyForPickup = ordersReadyForPickup;
            this.todaysSales = todaysSales;
            this.todaysOrders = todaysOrders;
            this.activeStudents = activeStudents;
            this.lastUpdated = java.time.LocalDateTime.now();
        }
        
        public int getPendingOrders() { return pendingOrders; }
        public int getOrdersInPreparation() { return ordersInPreparation; }
        public int getOrdersReadyForPickup() { return ordersReadyForPickup; }
        public BigDecimal getTodaysSales() { return todaysSales; }
        public int getTodaysOrders() { return todaysOrders; }
        public int getActiveStudents() { return activeStudents; }
        public java.time.LocalDateTime getLastUpdated() { return lastUpdated; }
    }
}