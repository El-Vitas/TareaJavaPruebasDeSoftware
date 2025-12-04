package com.estacionamiento;

import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ParkingService {

    public static final long DAILY_CAP = 15_000;
    private final Map<Integer, ParkingTicket> tickets = new LinkedHashMap<>();
    private final AtomicInteger nextId = new AtomicInteger(1);

    public ParkingTicket registerEntry(String plate, VehicleType vehicleType, LocalDateTime entryTime) {
        int generatedId = nextId.getAndIncrement();
        ParkingTicket ticket = new ParkingTicket(generatedId, plate, vehicleType, entryTime);
        tickets.put(generatedId, ticket);
        return ticket;
    }

    public long calculateFee(VehicleType type, LocalDateTime entryTime, LocalDateTime exitTime) {
        if (entryTime == null || exitTime == null) return 0;
        long minutes = Duration.between(entryTime, exitTime).toMinutes();
        if (minutes <= 0) return 0;

        long blocks = (minutes + 29) / 30;
        long amount = blocks * (long) type.getRatePerBlock();

        if (amount > DAILY_CAP) {
            amount = DAILY_CAP;
        }

        DayOfWeek dayOfWeek = entryTime.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            long discounted = (amount * 90) / 100;
            amount = discounted;
        }

        return amount;
    }

    public long registerExit(int ticketId, LocalDateTime exitTime) {
        ParkingTicket ticket = tickets.get(ticketId);
        if (ticket == null) throw new IllegalArgumentException("Ticket not found: " + ticketId);
        if (ticket.getStatus() == ParkingTicket.Status.CERRADO) {
            throw new IllegalStateException("Ticket already closed: " + ticketId);
        }


        long minutes = Duration.between(ticket.getEntryTime(), exitTime).toMinutes();

        long amount = calculateFee(ticket.getVehicleType(), ticket.getEntryTime(), exitTime);
        ticket.close(exitTime, amount);
        return amount;
    }

    public List<ParkingTicket> listOpenTickets() {
        return tickets.values().stream()
                .filter(ticket -> ticket.getStatus() == ParkingTicket.Status.ABIERTO)
                .collect(Collectors.toList());
    }

    public List<ParkingTicket> listClosedTickets() {
        return tickets.values().stream()
                .filter(ticket -> ticket.getStatus() == ParkingTicket.Status.CERRADO)
                .collect(Collectors.toList());
    }

    public Optional<ParkingTicket> findById(int id) {
        return Optional.ofNullable(tickets.get(id));
    }

    public long totalCollectedToday(LocalDate today) {
        return tickets.values().stream()
                .filter(ticket -> ticket.getStatus() == ParkingTicket.Status.CERRADO)
                .filter(ticket -> {
                    LocalDate entryDate = ticket.getEntryTime().toLocalDate();
                    LocalDate exitDate = ticket.getExitTime() != null ? ticket.getExitTime().toLocalDate() : null;
                    return Objects.equals(entryDate, today) || Objects.equals(exitDate, today);
                })
                .mapToLong(ParkingTicket::getAmount)
                .sum();
    }
}
