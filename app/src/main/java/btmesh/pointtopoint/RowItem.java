package btmesh.pointtopoint;

/**
 * Created by julia on 11.09.2017.
 */

public class RowItem {
    private String uuid;
    private String mac;

    public RowItem(String uuid, String mac) {
        this.uuid = uuid;
        this.mac = mac;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid){
        this.uuid = uuid;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    @Override
    public String toString() {
        return uuid + "\n" + mac;
    }
}
