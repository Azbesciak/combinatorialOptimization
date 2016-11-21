package model;

import java.util.List;

public class Machine {
    private List<Operation> operations;
    private List<Maintenance> maintenanceList;

    public Machine(List<Operation> operations, List<Maintenance> maintenanceList) {
        this.operations = operations;
        this.maintenanceList = maintenanceList;
    }

    public List<Operation> getOperations() {
        return operations;
    }

    public void setOperations(List<Operation> operations) {
        this.operations = operations;
    }

    public List<Maintenance> getMaintenanceList() {
        return maintenanceList;
    }

    public void setMaintenanceList(List<Maintenance> maintenanceList) {
        this.maintenanceList = maintenanceList;
    }

    @Override
    public String toString() {
        return "Machine{" +
                "operations=" + operations +
                ", maintenanceList=" + maintenanceList +
                '}';
    }
}
