package proc.sketches;

public class ConnectionAddress {
    private Integer port;
    private String ipAddress;

    public ConnectionAddress(String ipAddress, Integer port) {
        this.port = port;
        this.ipAddress = ipAddress;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Integer getPort() {
        return port;
    }

    public String getIpAddress() {
        return ipAddress;
    }
}
