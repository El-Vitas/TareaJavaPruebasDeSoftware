package com.estacionamiento;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ParkingServiceTest {

    @Test
    void calculateBlocksAndRounding() {
        ParkingService service = new ParkingService();
        LocalDateTime entryTime = LocalDateTime.of(2025, Month.DECEMBER, 1, 8, 0);

        assertEquals(800, service.calculateFee(VehicleType.AUTO, entryTime, entryTime.plusMinutes(1)));
        assertEquals(1600, service.calculateFee(VehicleType.AUTO, entryTime, entryTime.plusMinutes(31)));
        assertEquals(1600, service.calculateFee(VehicleType.AUTO, entryTime, entryTime.plusMinutes(60)));
        assertEquals(2400, service.calculateFee(VehicleType.AUTO, entryTime, entryTime.plusMinutes(61)));
    }

    @Test
    void dailyCapAndWeekendDiscount() {
        ParkingService service = new ParkingService();

        LocalDateTime saturdayEntry = LocalDateTime.of(2025, Month.DECEMBER, 6, 9, 0);
        LocalDateTime saturdayExit = saturdayEntry.plusHours(24);

        long computedAmount = service.calculateFee(VehicleType.CAMIONETA, saturdayEntry, saturdayExit);
        assertEquals(13_500, computedAmount);
    }

    @Test
    void closingTicketRulesAndZeroDuration() {
        ParkingService service = new ParkingService();
        LocalDateTime entryTime = LocalDateTime.of(2025, Month.DECEMBER, 3, 10, 0);
        ParkingTicket ticketImmediate = service.registerEntry("ABC123", VehicleType.MOTO, entryTime);
        long paidImmediate = service.registerExit(ticketImmediate.getId(), entryTime);
        assertEquals(0, paidImmediate);
        assertEquals(ParkingTicket.Status.CERRADO, ticketImmediate.getStatus());

        ParkingTicket ticketLater = service.registerEntry("XYZ789", VehicleType.MOTO, entryTime);
        long paidLater = service.registerExit(ticketLater.getId(), entryTime.plusHours(1));
        assertTrue(paidLater > 0);
        assertEquals(ParkingTicket.Status.CERRADO, ticketLater.getStatus());
    }

    @Test
    void registerEntryCreatesTicket() {
        ParkingService service = new ParkingService();
        LocalDateTime entry = LocalDateTime.of(2025, Month.DECEMBER, 4, 9, 0);
        ParkingTicket ticket = service.registerEntry("AAA111", VehicleType.AUTO, entry);

        assertNotNull(ticket);
        assertEquals(1, ticket.getId());
        assertEquals("AAA111", ticket.getPlate());
        assertEquals(VehicleType.AUTO, ticket.getVehicleType());
        assertEquals(ParkingTicket.Status.ABIERTO, ticket.getStatus());
        assertEquals(entry, ticket.getEntryTime());
    }

    @Test
    void registerExitForNonExistingTicketThrows() {
        ParkingService service = new ParkingService();
        LocalDateTime now = LocalDateTime.of(2025, Month.DECEMBER, 4, 10, 0);
        assertThrows(IllegalArgumentException.class, () -> service.registerExit(999, now));
    }

    @Test
    void cannotCloseAlreadyClosedTicket() {
        ParkingService service = new ParkingService();
        LocalDateTime entry = LocalDateTime.of(2025, Month.DECEMBER, 4, 8, 0);
        ParkingTicket ticket = service.registerEntry("BBB222", VehicleType.MOTO, entry);

        long paid = service.registerExit(ticket.getId(), entry.plusMinutes(30));
        assertEquals(ParkingTicket.Status.CERRADO, ticket.getStatus());
        assertTrue(paid >= 0);

        assertThrows(IllegalStateException.class, () -> service.registerExit(ticket.getId(), entry.plusHours(1)));
    }

    @Test
    void listOpenAndClosedTicketsBehavior() {
        ParkingService service = new ParkingService();
        LocalDateTime now = LocalDateTime.of(2025, Month.DECEMBER, 4, 7, 0);
        ParkingTicket t1 = service.registerEntry("T1", VehicleType.AUTO, now);
        ParkingTicket t2 = service.registerEntry("T2", VehicleType.MOTO, now.plusMinutes(5));

        service.registerExit(t1.getId(), now.plusHours(1));

        List<ParkingTicket> open = service.listOpenTickets();
        List<ParkingTicket> closed = service.listClosedTickets();

        assertEquals(1, open.size());
        assertEquals(t2.getId(), open.get(0).getId());
        assertEquals(1, closed.size());
        assertEquals(t1.getId(), closed.get(0).getId());
    }

    @Test
    void totalCollectedTodayCountsEntryOrExitDate() {
        ParkingService service = new ParkingService();

        LocalDate today = LocalDate.of(2025, Month.DECEMBER, 4);

        
        LocalDateTime entryYesterday = LocalDateTime.of(2025, Month.DECEMBER, 3, 23, 30);
        ParkingTicket t1 = service.registerEntry("Y1", VehicleType.AUTO, entryYesterday);
        service.registerExit(t1.getId(), LocalDateTime.of(2025, Month.DECEMBER, 4, 0, 30));

        
        LocalDateTime entryToday = LocalDateTime.of(2025, Month.DECEMBER, 4, 9, 0);
        ParkingTicket t2 = service.registerEntry("T2", VehicleType.MOTO, entryToday);
        service.registerExit(t2.getId(), entryToday.plusMinutes(30));

        long total = service.totalCollectedToday(today);
        assertTrue(total > 0);
        assertEquals(t1.getAmount() + t2.getAmount(), total);
    }

    @Test
    void findByIdReturnsOptional() {
        ParkingService service = new ParkingService();
        LocalDateTime entry = LocalDateTime.of(2025, Month.DECEMBER, 4, 11, 0);
        ParkingTicket ticket = service.registerEntry("ZZZ999", VehicleType.CAMIONETA, entry);

        assertTrue(service.findById(ticket.getId()).isPresent());
        assertFalse(service.findById(12345).isPresent());
    }

    @Test
    void dailyCapAppliedOnWeekday() {
        ParkingService service = new ParkingService();
        
        LocalDateTime entry = LocalDateTime.of(2025, Month.DECEMBER, 1, 0, 0);
        ParkingTicket ticket = service.registerEntry("CAP1", VehicleType.AUTO, entry);

        long paid = service.registerExit(ticket.getId(), entry.plusDays(2));
        assertEquals(ParkingService.DAILY_CAP, paid);
    }

    @Test
    void ratesPerVehicleAndMultipleBlocks() {
        ParkingService service = new ParkingService();
        LocalDateTime entry = LocalDateTime.of(2025, Month.DECEMBER, 2, 8, 0);

        assertEquals(500, service.calculateFee(VehicleType.MOTO, entry, entry.plusMinutes(30)));
        assertEquals(1000, service.calculateFee(VehicleType.MOTO, entry, entry.plusMinutes(31)));

        assertEquals(800, service.calculateFee(VehicleType.AUTO, entry, entry.plusMinutes(30)));
        assertEquals(1600, service.calculateFee(VehicleType.AUTO, entry, entry.plusMinutes(31)));

        assertEquals(1000, service.calculateFee(VehicleType.CAMIONETA, entry, entry.plusMinutes(30)));
        assertEquals(3000, service.calculateFee(VehicleType.CAMIONETA, entry, entry.plusMinutes(61)));
    }

    @Test
    void negativeDurationReturnsZero() {
        ParkingService service = new ParkingService();
        LocalDateTime entry = LocalDateTime.of(2025, Month.DECEMBER, 3, 12, 0);
        LocalDateTime exitBefore = entry.minusMinutes(10);
        assertEquals(0, service.calculateFee(VehicleType.AUTO, entry, exitBefore));
    }

    @Test
    void listEmptyAndIdIncrementingAndTotalZeroWhenNoClosed() {
        ParkingService service = new ParkingService();
        assertTrue(service.listOpenTickets().isEmpty());
        assertTrue(service.listClosedTickets().isEmpty());

        ParkingTicket a = service.registerEntry("A1", VehicleType.AUTO, LocalDateTime.of(2025, Month.DECEMBER, 4, 6, 0));
        ParkingTicket b = service.registerEntry("A2", VehicleType.AUTO, LocalDateTime.of(2025, Month.DECEMBER, 4, 7, 0));
        ParkingTicket c = service.registerEntry("A3", VehicleType.AUTO, LocalDateTime.of(2025, Month.DECEMBER, 4, 8, 0));

        assertEquals(1, a.getId());
        assertEquals(2, b.getId());
        assertEquals(3, c.getId());

        long total = service.totalCollectedToday(LocalDate.of(2025, Month.DECEMBER, 4));
        assertEquals(0, total);
    }

    @Test
    void weekendSmallDiscount() {
        ParkingService service = new ParkingService();
        LocalDateTime saturdayEntry = LocalDateTime.of(2025, Month.DECEMBER, 6, 10, 0); 
        long amount = service.calculateFee(VehicleType.CAMIONETA, saturdayEntry, saturdayEntry.plusMinutes(30));
        assertEquals(900, amount);
    }
}
