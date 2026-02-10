package vn.com.nws.cms.domain.enums;

public enum RoleType {
    ADMIN,
    TEACHER,
    STUDENT;

    public String authority() {
        return "ROLE_" + this.name();
    }
}
