package hello.itemservice.web;

public enum SessionConst {
    LOGIN_MEMBER("loginMember");
    private String value;

    SessionConst(String value) {
        this.value = value;
    }
    public String getValue() {
        return value;
    }
}
