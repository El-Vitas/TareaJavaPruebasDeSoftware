package com.estacionamiento;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ParkingTicket {

    public enum Status { ABIERTO, CERRADO }

    private final int id;
    private final String plate;
    private final VehicleType vehicleType;
    private final LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private long amount;
    private Status status;

    public ParkingTicket(int id, String plate, VehicleType vehicleType, LocalDateTime entryTime) {
        this.id = id;
        this.plate = plate;
        this.vehicleType = vehicleType;
        this.entryTime = entryTime;
        this.status = Status.ABIERTO;
        this.amount = 0;
    }

    public int getId() { return id; }
    public String getPlate() { return plate; }
    public VehicleType getVehicleType() { return vehicleType; }
    public LocalDateTime getEntryTime() { return entryTime; }
    public LocalDateTime getExitTime() { return exitTime; }
    public long getAmount() { return amount; }
    public Status getStatus() { return status; }

    public void close(LocalDateTime exitTime, long amount) {
        this.exitTime = exitTime;
        this.amount = amount;
        this.status = Status.CERRADO;
    }

    @Override
    public String toString() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        String entryFormatted = entryTime != null ? entryTime.format(fmt) : "-";
        String exitFormatted = exitTime != null ? exitTime.format(fmt) : "-";
        return "ParkingTicket{" +
            "id=" + id +
            ", plate='" + plate + '\'' +
            ", vehicleType=" + vehicleType +
            ", entryTime=" + entryFormatted +
            ", exitTime=" + exitFormatted +
            ", amount=$" + amount +
            ", status=" + status +
            '}';
    }

    public String displayString() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        String entryFormatted = entryTime != null ? entryTime.format(fmt) : "-";
        String exitFormatted = exitTime != null ? exitTime.format(fmt) : "-";
        StringBuilder builder = new StringBuilder();
        builder.append("[Ticket #").append(id).append("] ");
        builder.append("Patente: ").append(plate).append(" | ");
        builder.append("Tipo: ").append(vehicleType).append(" | ");
        builder.append("Ingreso: ").append(entryFormatted).append(" | ");
        builder.append("Salida: ").append(exitFormatted).append(" | ");
        builder.append("Monto: $").append(amount).append(" | ");
        builder.append("Estado: ").append(status);
        return builder.toString();
    }
}
