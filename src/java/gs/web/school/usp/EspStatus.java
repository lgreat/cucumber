package gs.web.school.usp;


public enum EspStatus {
    NO_DATA,
    USP_ONLY,
    OSP_PREFERRED,
    OSP_OUTDATED,
    MIX;

    public String getStatus() {
        return this.name().toLowerCase();
    }

    public EspStatus getByStatus(String status) {
        return EspStatus.valueOf(status.toUpperCase());
    }

}
