package com.estacionamiento;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class Main {


    public static void main(String[] args) {
        ParkingService service = new ParkingService();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n-- Calculadora de Tarifa Estacionamiento --");
            System.out.println("1) Registrar entrada");
            System.out.println("2) Registrar salida");
            System.out.println("3) Listar tickets abiertos");
            System.out.println("4) Listar tickets cerrados");
            System.out.println("5) Mostrar detalle de un ticket");
            System.out.println("6) Total recaudado del día");
            System.out.println("7) Salir");
            System.out.print("Opción: ");

            String menuOption = scanner.nextLine().trim();
            try {
                switch (menuOption) {
                    case "1" -> doRegisterEntry(service, scanner);
                    case "2" -> doRegisterExit(service, scanner);
                    case "3" -> listOpen(service);
                    case "4" -> listClosed(service);
                    case "5" -> showDetail(service, scanner);
                    case "6" -> totalToday(service);
                    case "7" -> { System.out.println("Saliendo."); return; }
                    default -> System.out.println("Opción inválida.");
                }
            } catch (Exception ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        }
    }

    private static void doRegisterEntry(ParkingService service, Scanner scanner) {
        System.out.print("Patente: ");
        String licensePlate = scanner.nextLine().trim();
        System.out.print("Tipo (AUTO, MOTO, CAMIONETA): ");
        String vehicleTypeInput = scanner.nextLine().trim().toUpperCase();
        try {
            VehicleType vehicleType = VehicleType.valueOf(vehicleTypeInput);
            LocalDateTime entryTimestamp = LocalDateTime.now();
            ParkingTicket createdTicket = service.registerEntry(licensePlate, vehicleType, entryTimestamp);
            System.out.println("Entrada registrada. ID: " + createdTicket.getId());
        } catch (IllegalArgumentException ex) {
            System.out.println("Tipo de vehículo inválido. Use: AUTO, MOTO o CAMIONETA.");
        }
    }

    private static void doRegisterExit(ParkingService service, Scanner scanner) {
        System.out.print("ID ticket: ");
        String input = scanner.nextLine().trim();
        int ticketId;
        try {
            ticketId = Integer.parseInt(input);
        } catch (NumberFormatException ex) {
            System.out.println("ID inválido. Ingrese un número de ticket válido.");
            return;
        }

        LocalDateTime exitTimestamp = LocalDateTime.now();
        try {
            long amount = service.registerExit(ticketId, exitTimestamp);
            System.out.println("Ticket cerrado. Monto: $" + amount);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            System.out.println("No se pudo registrar la salida: " + ex.getMessage());
        }
    }

    private static void listOpen(ParkingService service) {
        printTicketList(service.listOpenTickets(), "No hay tickets abiertos.");
    }

    private static void listClosed(ParkingService service) {
        printTicketList(service.listClosedTickets(), "No hay tickets cerrados.");
    }

    private static void printTicketList(List<ParkingTicket> tickets, String emptyMessage) {
        if (tickets == null || tickets.isEmpty()) {
            System.out.println(emptyMessage);
            return;
        }
        tickets.forEach(parkingTicket -> System.out.println(parkingTicket.displayString()));
    }

    private static void showDetail(ParkingService service, Scanner scanner) {
        System.out.print("ID ticket: ");
        String input = scanner.nextLine().trim();
        int ticketId;
        try {
            ticketId = Integer.parseInt(input);
        } catch (NumberFormatException ex) {
            System.out.println("ID inválido. Ingrese un número de ticket válido.");
            return;
        }
        service.findById(ticketId).ifPresentOrElse(
            ticket -> System.out.println(ticket.displayString()),
            () -> System.out.println("Ticket no encontrado."));
    }

    private static void totalToday(ParkingService service) {
        long total = service.totalCollectedToday(LocalDate.now());
        System.out.println("Total recaudado hoy: $" + total);
    }
}
